package com.example.sharoma_finder.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application.applicationContext)
    private val userManager = UserManager(application.applicationContext)

    // ✅ Manager pentru consimțământ internet
    private val internetConsentManager = InternetConsentManager(application.applicationContext)

    private val analytics = FirebaseAnalytics.getInstance(application.applicationContext)

    private val database = AppDatabase.getDatabase(application)

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

    val favoriteStoreIds = mutableStateListOf<String>()
    val favoriteStores = mutableStateListOf<StoreModel>()
    val nearestStoresTop5 = mutableStateListOf<StoreModel>()
    val popularStores = mutableStateListOf<StoreModel>()
    val nearestStoresAllSorted = mutableStateListOf<StoreModel>()

    private val allStoresRaw = mutableListOf<StoreModel>()

    val isDataLoaded = mutableStateOf(false)
    var isRefreshing = mutableStateOf(false)
        private set

    // ✅ Flag pentru starea internetului
    var hasInternetAccess = mutableStateOf(false)
        private set

    // ✅ ADĂUGAT: Starea permisiunii de locație (folosim mutableStateOf pentru compatibilitate cu Compose)
    var isLocationPermissionGranted = mutableStateOf(false)

    var userName = mutableStateOf("Utilizatorule")
    var userImagePath = mutableStateOf<String?>(null)
    var currentUserLocation: Location? = null
        private set

    init {
        Log.d("DashboardViewModel", "=== INIT START ===")
        loadUserData()
        loadFavorites()

        // ✅ Verificăm consimțământul la pornire
        checkInternetConsent()

        // ✅ Verificăm permisiunea de locație la pornire
        checkLocationPermission()

        checkLocalCache()
        observeLocalDatabase()

        // ✅ Sincronizarea se face doar dacă avem consimțământ ȘI internet fizic
        if (internetConsentManager.canUseInternet()) {
            refreshDataFromNetwork()
        } else {
            Log.w("DashboardViewModel", "⚠️ No internet access/consent - skipping network sync")
        }
    }

    private fun checkInternetConsent() {
        hasInternetAccess.value = internetConsentManager.canUseInternet()
        Log.d("DashboardViewModel", "Internet access: ${hasInternetAccess.value}")
    }

    // ✅ FIX APLICAT: Verificare sigură a permisiunii
    fun checkLocationPermission() {
        val context = getApplication<Application>().applicationContext

        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isGranted = fineLocation || coarseLocation

        // ✅ OPTIMIZARE: Actualizăm doar dacă starea s-a schimbat
        // Acest lucru previne bucle infinite de recomposition în UI
        if (isLocationPermissionGranted.value != isGranted) {

            // Ne asigurăm că actualizarea UI se face pe Main Thread
            viewModelScope.launch(Dispatchers.Main) {
                isLocationPermissionGranted.value = isGranted
                Log.d("DashboardViewModel", "📍 Permission state changed: $isGranted")

                if (isGranted) {
                    // Dacă tocmai am primit permisiunea, luăm locația
                    fetchUserLocation()
                }
            }
        }
    }

    // ✅ BONUS: Helper pentru MainActivity onResume
    fun onAppResumed() {
        checkLocationPermission()
    }

    /**
     * ✅ LOGICĂ NOUĂ PENTRU SWITCH-UL DIN PROFIL
     */
    fun onInternetSwitchToggled(enabled: Boolean, onShowConsentDialog: () -> Unit) {
        if (enabled) {
            // Verificăm dacă a dat deja consimțământ anterior
            if (internetConsentManager.hasInternetConsent()) {
                enableInternetFeatures()
            } else {
                onShowConsentDialog()
            }
        } else {
            disableInternetFeatures()
        }
    }

    /**
     * ✅ Chemată când userul dă ACCEPT în dialogul din Profil
     */
    fun grantInternetConsentFromProfile() {
        internetConsentManager.grantConsent()
        enableInternetFeatures()
    }

    fun enableInternetFeatures() {
        Log.d("DashboardViewModel", "✅ Enabling internet features")
        if (internetConsentManager.isInternetAvailable()) {
            hasInternetAccess.value = true
            refreshDataFromNetwork()
        } else {
            Log.w("DashboardViewModel", "❌ Internet enabled by user but NO CONNECTION detected")
            hasInternetAccess.value = true
        }
    }

    fun disableInternetFeatures() {
        Log.d("DashboardViewModel", "❌ Disabling internet features")
        hasInternetAccess.value = false
        Log.d("DashboardViewModel", "Operating in OFFLINE mode - using cached data only")
    }

    private fun checkLocalCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cachedStores = database.storeDao().getAllStoresSync()
                val cachedCategories = database.categoryDao().getAllCategoriesSync()
                val cachedBanners = database.bannerDao().getAllBannersSync()

                Log.d("DashboardVM", "📦 Cache check: Stores=${cachedStores.size}, Categories=${cachedCategories.size}, Banners=${cachedBanners.size}")

                withContext(Dispatchers.Main) {
                    if (cachedStores.isNotEmpty()) {
                        Log.d("DashboardVM", "✅ Loading ${cachedStores.size} stores from cache")
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
                Log.e("DashboardVM", "❌ Cache check failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    isDataLoaded.value = true
                }
            }
        }
    }

    private fun observeLocalDatabase() {
        localStoreObserver = Observer { stores ->
            if (stores != null) {
                Log.d("DashboardVM", "🔄 Room LiveData update: ${stores.size} stores")

                allStoresRaw.clear()
                allStoresRaw.addAll(stores)

                if (currentUserLocation != null) {
                    recalculateDistances()
                } else {
                    processData()
                }

                if (!isDataLoaded.value) {
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
            Log.w("DashboardVM", "⚠️ No physical internet connection - Sync skipped")
            return
        }

        viewModelScope.launch {
            Log.d("DashboardVM", "🌐 Starting network sync...")
            try {
                withContext(Dispatchers.IO) {
                    launch { storeRepository.refreshStores() }
                    launch { dashboardRepository.refreshCategories() }
                    launch { dashboardRepository.refreshBanners() }
                    launch { dashboardRepository.refreshSubCategories() }
                }
                Log.d("DashboardVM", "✅ Network sync completed")
            } catch (e: Exception) {
                Log.e("DashboardVM", "❌ Network sync failed: ${e.message}")
            }

            delay(5000)
            if (!isDataLoaded.value) {
                Log.w("DashboardVM", "⏰ Timeout - forcing loaded state")
                isDataLoaded.value = true
            }
        }
    }

    fun forceRefreshAllData(onFinished: () -> Unit) {
        if (!internetConsentManager.hasInternetConsent()) {
            Log.w("DashboardVM", "⚠️ Cannot refresh - Internet access disabled in settings")
            onFinished()
            return
        }

        if (!internetConsentManager.isInternetAvailable()) {
            Log.e("DashboardVM", "⛔ BLOCKED: Attempted to wipe cache without internet connection!")
            viewModelScope.launch(Dispatchers.Main) {
                Toast.makeText(getApplication(), "No internet connection! Cache kept safe. 🛡️", Toast.LENGTH_LONG).show()
                onFinished()
            }
            return
        }

        if (isRefreshing.value) return

        isRefreshing.value = true

        viewModelScope.launch {
            Log.d("DashboardVM", "🔄 FORCE REFRESH STARTED")
            try {
                withContext(Dispatchers.IO) {
                    launch { storeRepository.clearCache() }
                    launch { database.categoryDao().deleteAll() }
                    launch { database.bannerDao().deleteAll() }
                    launch { database.subCategoryDao().deleteAll() }
                }

                delay(500)
                refreshDataFromNetwork()
                delay(1000)
                Log.d("DashboardVM", "✅ FORCE REFRESH COMPLETED")
            } catch (e: Exception) {
                Log.e("DashboardVM", "❌ Force refresh failed: ${e.message}")
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
                            Log.d("DashboardVM", "📍 GPS: ${location.latitude}, ${location.longitude}")
                        } else {
                            Log.w("DashboardVM", "⚠️ GPS null")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DashboardVM", "❌ GPS failed: ${e.message}")
                    }
            } catch (e: SecurityException) {
                Log.e("DashboardVM", "🔒 GPS Security Error", e)
            }
        } else {
            Log.w("DashboardVM", "⚠️ No location permissions")
        }
    }

    fun updateUserLocation(location: Location) {
        currentUserLocation = location
        recalculateDistances()
    }

    private fun recalculateDistances() {
        val location = currentUserLocation ?: return
        if (allStoresRaw.isEmpty()) return

        Log.d("DashboardVM", "📏 Calculating distances for ${allStoresRaw.size} stores")

        allStoresRaw.forEach { store ->
            val storeLoc = Location("store")
            storeLoc.latitude = store.Latitude
            storeLoc.longitude = store.Longitude
            store.distanceToUser = location.distanceTo(storeLoc)
        }
        processData()
    }

    private fun processData() {
        val sortedList = allStoresRaw.sortedBy {
            if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
        }

        nearestStoresTop5.clear()
        nearestStoresTop5.addAll(sortedList.take(5))

        nearestStoresAllSorted.clear()
        nearestStoresAllSorted.addAll(sortedList)

        val popular = sortedList.filter { it.IsPopular }
        popularStores.clear()
        popularStores.addAll(popular)

        updateFavoriteStores()
        Log.d("DashboardVM", "✅ Processed: ${allStoresRaw.size} stores, ${popular.size} popular")
    }

    override fun onCleared() {
        super.onCleared()
        if (::localStoreObserver.isInitialized) {
            storeRepository.allStores.removeObserver(localStoreObserver)
        }
        Log.d("DashboardViewModel", "=== CLEANUP ===")
    }

    fun logViewStore(store: StoreModel) {
        val bundle = android.os.Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, store.getUniqueId())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, store.Title)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "store")
        bundle.putString("store_category", store.CategoryId)
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun getGlobalStoreList(): List<StoreModel> = allStoresRaw

    private fun loadUserData() {
        userName.value = userManager.getName()
        userImagePath.value = userManager.getImagePath()
    }

    fun updateUserName(newName: String) {
        userName.value = newName
        userManager.saveName(newName)
    }

    fun updateUserImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val internalPath = userManager.copyImageToInternalStorage(uri)
            withContext(Dispatchers.Main) {
                if (internalPath != null) {
                    userImagePath.value = internalPath
                    userManager.saveImagePath(internalPath)
                }
            }
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
        } else {
            favoritesManager.addFavorite(uniqueKey)
            favoriteStoreIds.add(uniqueKey)
        }
        updateFavoriteStores()
    }

    fun loadCategory(): LiveData<List<CategoryModel>> = dashboardRepository.allCategories
    fun loadBanner(): LiveData<List<BannerModel>> = dashboardRepository.allBanners
}