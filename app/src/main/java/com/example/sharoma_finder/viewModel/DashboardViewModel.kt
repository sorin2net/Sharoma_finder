package com.example.sharoma_finder.viewModel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
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
import com.example.sharoma_finder.repository.StoreRepository
import com.example.sharoma_finder.repository.UserManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application.applicationContext)
    private val userManager = UserManager(application.applicationContext)
    private val analytics = FirebaseAnalytics.getInstance(application.applicationContext)

    private val database = AppDatabase.getDatabase(application)
    private val storeRepository = StoreRepository(database.storeDao())
    private val dashboardRepository = DashboardRepository(
        database.categoryDao(),
        database.bannerDao(),
        database.subCategoryDao()  // ✅ ADĂUGAT
    )

    private lateinit var localStoreObserver: Observer<List<StoreModel>>

    val favoriteStoreIds = mutableStateListOf<String>()
    val favoriteStores = mutableStateListOf<StoreModel>()
    val nearestStoresTop5 = mutableStateListOf<StoreModel>()
    val popularStores = mutableStateListOf<StoreModel>()
    val nearestStoresAllSorted = mutableStateListOf<StoreModel>()

    private val allStoresRaw = mutableListOf<StoreModel>()

    val isDataLoaded = mutableStateOf(false)
    var userName = mutableStateOf("Utilizatorule")
    var userImagePath = mutableStateOf<String?>(null)
    var currentUserLocation: Location? = null
        private set

    init {
        Log.d("DashboardViewModel", "=== INIT START ===")
        loadUserData()
        loadFavorites()

        checkLocalCache()
        observeLocalDatabase()
        refreshDataFromNetwork()
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

                    // ✅ FIX CRITIC: Setăm isDataLoaded = true IMEDIAT
                    // Categoriile și banner-ele se încarcă prin LiveData observeAsState
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
        viewModelScope.launch {
            Log.d("DashboardVM", "🌐 Starting network sync...")

            try {
                withContext(Dispatchers.IO) {
                    // Rulează toate sincronizările în paralel pentru viteză maximă
                    launch { storeRepository.refreshStores() }
                    launch { dashboardRepository.refreshCategories() }
                    launch { dashboardRepository.refreshBanners() }
                    launch { dashboardRepository.refreshSubCategories() }  // ✅ ADĂUGAT
                }
                Log.d("DashboardVM", "✅ Network sync completed")
            } catch (e: Exception) {
                Log.e("DashboardVM", "❌ Network sync failed: ${e.message}")
            }

            // Safety timeout
            kotlinx.coroutines.delay(5000)
            if (!isDataLoaded.value) {
                Log.w("DashboardVM", "⏰ Timeout - forcing loaded state")
                isDataLoaded.value = true
            }
        }
    }

    /**
     * ✅ FUNCȚIE DE DEBUGGING: Forțează refresh complet
     * Șterge tot cache-ul și descarcă date noi de pe Firebase
     */
    fun forceRefreshAllData() {
        viewModelScope.launch {
            Log.d("DashboardVM", "🔄 FORCE REFRESH STARTED")

            try {
                withContext(Dispatchers.IO) {
                    // Șterge tot cache-ul
                    // NOTA: Asigură-te că StoreRepository are metoda clearCache().
                    // Dacă nu, poți folosi: database.storeDao().deleteAll()
                    launch { storeRepository.clearCache() }
                    launch { database.categoryDao().deleteAll() }
                    launch { database.bannerDao().deleteAll() }
                    launch { database.subCategoryDao().deleteAll() }
                }

                // Așteaptă 500ms să se finalizeze ștergerea
                kotlinx.coroutines.delay(500)

                // Reîncarcă de pe Firebase
                refreshDataFromNetwork()

                Log.d("DashboardVM", "✅ FORCE REFRESH COMPLETED")

            } catch (e: Exception) {
                Log.e("DashboardVM", "❌ Force refresh failed: ${e.message}")
            }
        }
    }

    fun fetchUserLocation() {
        val context = getApplication<Application>().applicationContext

        val hasFine = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

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