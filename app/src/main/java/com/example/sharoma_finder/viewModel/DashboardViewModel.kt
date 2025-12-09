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
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.DashboardRepository
import com.example.sharoma_finder.repository.FavoritesManager
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.repository.ResultsRepository
import com.example.sharoma_finder.repository.UserManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository()
    private val resultsRepository = ResultsRepository()
    private val favoritesManager = FavoritesManager(application.applicationContext)
    private val userManager = UserManager(application.applicationContext)
    private val analytics = FirebaseAnalytics.getInstance(application.applicationContext)

    // Observer pentru a preveni memory leaks
    private var storesObserver: Observer<Resource<MutableList<StoreModel>>>? = null
    private var storesLiveData: LiveData<Resource<MutableList<StoreModel>>>? = null

    // Liste pentru UI
    val favoriteStoreIds = mutableStateListOf<String>()
    val favoriteStores = mutableStateListOf<StoreModel>()
    val nearestStoresTop5 = mutableStateListOf<StoreModel>()
    val popularStores = mutableStateListOf<StoreModel>()
    val nearestStoresAllSorted = mutableStateListOf<StoreModel>()

    // Liste interne
    private val allStoresRaw = mutableListOf<StoreModel>()

    // State
    val isDataLoaded = mutableStateOf(false)
    var userName = mutableStateOf("Costi")
    var userImagePath = mutableStateOf<String?>(null)
    var currentUserLocation: Location? = null
        private set

    init {
        Log.d("DashboardViewModel", "=== INIT START ===")
        loadUserData()
        loadFavorites()
        loadInitialData()
    }

    // --- LOGICA DE LOCAȚIE MUTATĂ AICI ---
    fun fetchUserLocation() {
        val context = getApplication<Application>().applicationContext

        // Verificăm permisiunile din nou pentru siguranță (deși UI-ul a verificat deja)
        val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                // Folosim ApplicationContext, deci nu avem memory leak
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            // Updatăm locația și recalculăm distanțele
                            updateUserLocation(location)
                            Log.d("DashboardVM", "GPS location found in VM: ${location.latitude}, ${location.longitude}")
                        } else {
                            Log.w("DashboardVM", "GPS enabled but location is null")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("DashboardVM", "Failed to get GPS location in VM", exception)
                    }
            } catch (e: SecurityException) {
                Log.e("DashboardVM", "GPS Security Error", e)
            }
        } else {
            Log.w("DashboardVM", "Cannot fetch location: Permissions missing")
        }
    }

    private fun loadInitialData() {
        storesLiveData = resultsRepository.loadAllStores()

        storesObserver = Observer { resource ->
            when (resource) {
                is Resource.Success -> {
                    resource.data?.let { list ->
                        allStoresRaw.clear()
                        allStoresRaw.addAll(list)
                        Log.d("DashboardVM", "📦 Loaded ${allStoresRaw.size} total stores")
                        processData()

                        if (currentUserLocation != null) {
                            recalculateDistances()
                        }

                        isDataLoaded.value = true
                    }
                }
                is Resource.Error -> {
                    Log.e("DashboardVM", "Error loading stores: ${resource.message}")
                    isDataLoaded.value = true
                }
                is Resource.Loading -> {
                    Log.d("DashboardVM", "Loading stores...")
                }
            }
        }

        storesLiveData?.observeForever(storesObserver!!)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DashboardViewModel", "=== CLEANUP START ===")

        storesObserver?.let { observer ->
            storesLiveData?.removeObserver(observer)
        }

        storesObserver = null
        storesLiveData = null

        Log.d("DashboardViewModel", "=== CLEANUP COMPLETE ===")
    }

    // Analytics
    fun logViewStore(store: StoreModel) {
        val bundle = android.os.Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, store.getUniqueId())
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, store.Title)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "store")
        bundle.putString("store_category", store.CategoryId)
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        Log.d("Analytics", "Logged view for: ${store.Title}")
    }

    fun getGlobalStoreList(): List<StoreModel> {
        return allStoresRaw
    }

    fun updateUserLocation(location: Location) {
        currentUserLocation = location
        Log.d("DashboardVM", "📍 User location updated: ${location.latitude}, ${location.longitude}")
        recalculateDistances()
    }

    private fun recalculateDistances() {
        val location = currentUserLocation ?: return
        if (allStoresRaw.isEmpty()) return

        Log.d("DashboardVM", "📏 Recalculating distances...")

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

        Log.d("DashboardVM", "✅ Data processed. Nearest: ${nearestStoresTop5.size}, Popular: ${popularStores.size}")
    }

    // User Profile
    private fun loadUserData() {
        userName.value = userManager.getName()
        userImagePath.value = userManager.getImagePath()
    }

    fun updateUserName(newName: String) {
        userName.value = newName
        userManager.saveName(newName)
    }

    // În DashboardViewModel
    fun updateUserImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) { // Execută pe fundal
            val internalPath = userManager.copyImageToInternalStorage(uri)
            withContext(Dispatchers.Main) { // Revino pe UI pentru update
                if (internalPath != null) {
                    userImagePath.value = internalPath
                    userManager.saveImagePath(internalPath)
                }
            }
        }
    }

    // Favorites
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
        Log.d("DashboardViewModel", "🔄 Wishlist updated & sorted: ${favoriteStores.size} stores shown")
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

    fun loadCategory(): LiveData<MutableList<CategoryModel>> = repository.loadCategory()
    fun loadBanner(): LiveData<MutableList<BannerModel>> = repository.loadBanner()
}