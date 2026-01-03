package com.example.sharoma_finder.viewModel

import kotlinx.coroutines.isActive
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import coil.Coil
import coil.request.ImageRequest
import com.example.sharoma_finder.data.AppDatabase
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.DashboardRepository
import com.example.sharoma_finder.repository.FavoritesManager
import com.example.sharoma_finder.repository.InternetConsentManager
import com.example.sharoma_finder.repository.StoreRepository
import com.example.sharoma_finder.repository.UserManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val recalculationMutex = Mutex()
    private val favoritesManager = FavoritesManager(application.applicationContext)
    private val userManager = UserManager(application.applicationContext)
    private val internetConsentManager = InternetConsentManager(application.applicationContext)

    private var lastTimerSaveTimestamp: Long = 0L
    private val analytics = FirebaseAnalytics.getInstance(application.applicationContext)
    private var usageTimerJob: kotlinx.coroutines.Job? = null
    private val database = AppDatabase.getDatabase(application)
    private var isCheckingPermission = false

    private val storeRepository = StoreRepository(
        database.storeDao(),
        database.cacheMetadataDao()
    )

    private val dashboardRepository = DashboardRepository(
        database.categoryDao(),
        database.bannerDao(),
        database.subCategoryDao()
    )

    private lateinit var localStoreObserver: Observer<List<StoreModel>>

    var selectedTab = mutableStateOf("Acasă")

    fun updateTab(newTab: String) {
        selectedTab.value = newTab
    }

    val favoriteStoreIds = mutableStateListOf<String>()
    val favoriteStores = mutableStateListOf<StoreModel>()
    val nearestStoresTop5 = mutableStateListOf<StoreModel>()
    val popularStores = mutableStateListOf<StoreModel>()
    val nearestStoresAllSorted = mutableStateListOf<StoreModel>()

    private val allStoresRaw = mutableListOf<StoreModel>()

    val isDataLoaded = mutableStateOf(false)
    var isRefreshing = mutableStateOf(false)
        private set

    var hasInternetAccess = mutableStateOf(false)
        private set

    var isLocationPermissionGranted = mutableStateOf(false)

    var userName = mutableStateOf("Utilizatorule")
    var userImagePath = mutableStateOf<String?>(null)
    var currentUserLocation by mutableStateOf<Location?>(null)
        private set

    var userPoints = mutableStateOf(0)

    init {
        loadUserData()
        loadFavorites()
        checkInternetConsent()

        viewModelScope.launch {
            checkLocalCache()
            delay(500)
            observeLocalDatabase()

            delay(500)
            startUsageTimer()

            if (internetConsentManager.canUseInternet()) {
                refreshEssentialData()
            }
        }
    }

    private suspend fun refreshEssentialData() = withContext(Dispatchers.IO) {
        dashboardRepository.refreshCategories()
        delay(800)
        dashboardRepository.refreshBanners()
        delay(500)
        storeRepository.refreshStores()
    }

    fun addPoints(amount: Int) {
        userPoints.value += amount
        userManager.savePoints(userPoints.value)
    }

    fun removePoints(amount: Int) {
        userPoints.value = (userPoints.value - amount).coerceAtLeast(0)
        userManager.savePoints(userPoints.value)
    }

    fun startUsageTimer() {
        if (usageTimerJob?.isActive == true) return

        usageTimerJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(60000)

                withContext(Dispatchers.Main) {
                    addPoints(1)
                    userManager.saveLastTimerTimestamp(System.currentTimeMillis())
                }
            }
        }
    }

    fun stopUsageTimer() {
        usageTimerJob?.cancel()
        usageTimerJob = null

        userManager.saveLastTimerTimestamp(System.currentTimeMillis())
    }

    fun onStoreOpenedOnMap() {
        addPoints(10)
    }

    private fun checkInternetConsent() {
        hasInternetAccess.value = internetConsentManager.canUseInternet()
    }

    fun checkLocationPermission() {
        if (isCheckingPermission) return
        isCheckingPermission = true

        val context = getApplication<Application>().applicationContext
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val isGranted = fineLocation || coarseLocation

        if (isLocationPermissionGranted.value != isGranted) {
            viewModelScope.launch(Dispatchers.Main) {
                isLocationPermissionGranted.value = isGranted

                if (isGranted) {
                    fetchUserLocation()
                }
                isCheckingPermission = false
            }
        } else {
            isCheckingPermission = false
        }
    }

    fun onAppResumed() {
        checkLocationPermission()
    }

    fun onInternetSwitchToggled(enabled: Boolean, onShowConsentDialog: () -> Unit) {
        if (enabled) {
            if (internetConsentManager.hasInternetConsent()) {
                enableInternetFeatures()
            } else {
                onShowConsentDialog()
            }
        } else {
            disableInternetFeatures()
        }
    }

    fun grantInternetConsentFromProfile() {
        internetConsentManager.grantConsent()
        enableInternetFeatures()
    }

    fun enableInternetFeatures() {
        if (internetConsentManager.canUseInternet()) {
            hasInternetAccess.value = true
            refreshDataFromNetwork()
        } else {
            hasInternetAccess.value = false
        }
    }

    fun disableInternetFeatures() {
        hasInternetAccess.value = false
    }

    private fun checkLocalCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachedStores = database.storeDao().getAllStoresSync()
                val cachedCategories = database.categoryDao().getAllCategoriesSync()
                val cachedBanners = database.bannerDao().getAllBannersSync()

                withContext(Dispatchers.Main) {
                    if (cachedStores.isNotEmpty()) {
                        allStoresRaw.clear()
                        allStoresRaw.addAll(cachedStores)

                        if (currentUserLocation != null) {
                            recalculateDistances()
                        } else {
                            processData()
                        }
                    }
                    isDataLoaded.value = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDataLoaded.value = true
                }
            }
        }
    }

    private fun observeLocalDatabase() {
        localStoreObserver = Observer { stores ->
            if (stores != null) {
                viewModelScope.launch(Dispatchers.Default) {

                    synchronized(allStoresRaw) {
                        allStoresRaw.clear()
                        allStoresRaw.addAll(stores)
                    }

                    if (currentUserLocation != null) {
                        recalculateDistances()
                    } else {
                        processData()
                    }

                    isDataLoaded.value = true
                }
            }
        }
        storeRepository.allStores.observeForever(localStoreObserver)
    }

    private fun refreshDataFromNetwork() {
        if (!internetConsentManager.hasInternetConsent()) {
            return
        }
        if (!internetConsentManager.isInternetAvailable()) {
            return
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    launch { storeRepository.refreshStores() }
                    launch { dashboardRepository.refreshCategories() }
                    launch { dashboardRepository.refreshBanners() }
                    launch { dashboardRepository.refreshSubCategories() }
                }
            } catch (e: Exception) {
                // Fail silently
            }

            delay(5000)
            if (!isDataLoaded.value) {
                isDataLoaded.value = true
            }
        }
    }

    fun forceRefreshAllData(onFinished: () -> Unit) {
        if (!internetConsentManager.isInternetAvailable()) {
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(getApplication(), "Fără conexiune la internet!", Toast.LENGTH_LONG).show()
                onFinished()
            }
            return
        }

        if (isRefreshing.value) return
        isRefreshing.value = true

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    storeRepository.refreshStores(forceRefresh = true)
                    val imageLoader = Coil.imageLoader(application)
                    allStoresRaw.forEach { store ->
                        val request = ImageRequest.Builder(application)
                            .data(store.ImagePath)
                            .build()
                        imageLoader.enqueue(request)
                    }
                    dashboardRepository.refreshCategories()
                    dashboardRepository.refreshBanners()
                    dashboardRepository.refreshSubCategories()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Eroare la actualizare: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isRefreshing.value = false
                    onFinished()
                }
            }
        }
    }

    fun fetchUserLocation() {
        val context = getApplication<Application>().applicationContext
        val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            updateUserLocation(location)
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle failure
                    }
            } catch (e: SecurityException) {
                // Handle exception
            }
        }
    }

    private var lastCalculationLocation: Location? = null
    var lastCalculationTimestamp by mutableStateOf(0L)
        private set

    fun updateUserLocation(location: Location) {
        val distanceMoved = lastCalculationLocation?.distanceTo(location) ?: Float.MAX_VALUE
        val timeSinceLastCalc = System.currentTimeMillis() - lastCalculationTimestamp
        val shouldRecalculate = distanceMoved >= 10f || timeSinceLastCalc > 120_000L

        if (!shouldRecalculate) return

        currentUserLocation = location
        lastCalculationLocation = location

        viewModelScope.launch(Dispatchers.Default) {
            recalculateDistances()
        }
    }

    private suspend fun recalculateDistances() {
        recalculationMutex.withLock {
            withContext(Dispatchers.Default) {
                val storesCopy = synchronized(allStoresRaw) { allStoresRaw.toList() }
                if (storesCopy.isEmpty()) return@withContext

                val userLoc = currentUserLocation ?: return@withContext
                val results = FloatArray(1)

                val updatedStores = storesCopy.map { originalStore ->
                    android.location.Location.distanceBetween(
                        userLoc.latitude, userLoc.longitude,
                        originalStore.Latitude, originalStore.Longitude,
                        results
                    )
                    originalStore.copy().apply {
                        distanceToUser = results[0]
                    }
                }

                synchronized(allStoresRaw) {
                    allStoresRaw.clear()
                    allStoresRaw.addAll(updatedStores)
                }

                val sortedAll = updatedStores.sortedBy {
                    if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
                }

                withContext(Dispatchers.Main) {
                    nearestStoresTop5.clear()
                    nearestStoresTop5.addAll(sortedAll.take(5))

                    nearestStoresAllSorted.clear()
                    nearestStoresAllSorted.addAll(sortedAll)

                    popularStores.clear()
                    popularStores.addAll(sortedAll.filter { it.IsPopular })

                    favoriteStores.clear()
                    favoriteStores.addAll(sortedAll.filter { isFavorite(it) })

                    lastCalculationTimestamp = System.currentTimeMillis()
                }
            }
        }
    }

    private fun processData() {
        viewModelScope.launch(Dispatchers.Default) {
            recalculateDistances()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopUsageTimer()
        if (::localStoreObserver.isInitialized) {
            storeRepository.allStores.removeObserver(localStoreObserver)
        }
    }

    fun logViewStore(store: StoreModel) {
        val bundle = android.os.Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, store.getUniqueId())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, store.Title)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "store")
        val categoriesText = store.CategoryIds.joinToString(separator = ", ")
        bundle.putString("store_category", categoriesText)
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun getGlobalStoreList(): List<StoreModel> {
        return synchronized(allStoresRaw) {
            allStoresRaw.toList()
        }
    }

    private fun loadUserData() {
        userName.value = userManager.getName()
        userImagePath.value = userManager.getImagePath()
        userPoints.value = userManager.getPoints()
    }

    fun updateUserName(newName: String) {
        if (newName.isBlank() || newName == userName.value) {
            return
        }

        if (userPoints.value >= 50) {
            removePoints(50)
            userName.value = newName
            userManager.saveName(newName)
            Toast.makeText(getApplication(), "Nume actualizat! (-50 XP)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Nu ai destule puncte! (Necesar: 50 XP)", Toast.LENGTH_LONG).show()
        }
    }

    fun updateUserImage(uri: Uri) {
        if (userPoints.value >= 100) {
            viewModelScope.launch(Dispatchers.IO) {
                val internalPath = userManager.copyImageToInternalStorage(uri)
                withContext(Dispatchers.Main) {
                    if (internalPath != null) {
                        removePoints(100)
                        userImagePath.value = internalPath
                        userManager.saveImagePath(internalPath)
                        Toast.makeText(getApplication(), "Poză actualizată! (-100 XP)", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(getApplication(), "Nu ai destule puncte! (Necesar: 100 XP)", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadFavorites() {
        favoriteStoreIds.clear()
        favoriteStoreIds.addAll(favoritesManager.getFavorites())
    }

    private fun updateFavoriteStores() {
        val favorites = allStoresRaw.filter { store ->
            favoriteStoreIds.contains(store.getUniqueId())
        }
        val sortedFavorites = favorites.sortedBy {
            if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
        }
        favoriteStores.clear()
        favoriteStores.addAll(sortedFavorites)
    }

    fun isFavorite(store: StoreModel): Boolean = favoriteStoreIds.contains(store.getUniqueId())

    fun toggleFavorite(store: StoreModel) {
        val uniqueKey = store.getUniqueId()
        if (favoriteStoreIds.contains(uniqueKey)) {
            favoritesManager.removeFavorite(uniqueKey)
            favoriteStoreIds.remove(uniqueKey)
            removePoints(10)
        } else {
            favoritesManager.addFavorite(uniqueKey)
            favoriteStoreIds.add(uniqueKey)
            addPoints(10)
        }
        processData()
    }

    fun loadCategory(): LiveData<List<CategoryModel>> = dashboardRepository.allCategories
    fun loadBanner(): LiveData<List<BannerModel>> = dashboardRepository.allBanners

    fun getUserRank(): String {
        val points = userPoints.value
        return when {
            points < 100 -> "La Dietă"
            points in 100..199 -> "Ciugulitor"
            points in 200..349 -> "Pofticios"
            points in 350..499 -> "Mâncăcios"
            points in 500..749 -> "Gurmand"
            points in 750..999 -> "Devorator"
            else -> "Sultan"
        }
    }

    fun getRankProgress(): Float {
        val points = userPoints.value
        val (start, end) = when {
            points < 100 -> 0 to 100
            points in 100..199 -> 100 to 200
            points in 200..349 -> 200 to 350
            points in 350..499 -> 350 to 500
            points in 500..749 -> 500 to 750
            points in 750..999 -> 750 to 1000
            else -> return 1.0f
        }
        return ((points - start).toFloat() / (end - start)).coerceIn(0f, 1f)
    }
}