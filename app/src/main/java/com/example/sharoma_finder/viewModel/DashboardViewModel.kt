package com.example.sharoma_finder.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.DashboardRepository
import com.example.sharoma_finder.repository.FavoritesManager
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.repository.ResultsRepository
import com.example.sharoma_finder.repository.UserManager // Asigură-te că ai acest import

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository()
    private val resultsRepository = ResultsRepository()
    private val favoritesManager = FavoritesManager(application.applicationContext)

    // --- 1. MANAGER PENTRU PROFIL (NOU) ---
    private val userManager = UserManager(application.applicationContext)

    // Listele pentru UI (Magazine)
    val favoriteStoreIds = mutableStateListOf<String>()
    val favoriteStores = mutableStateListOf<StoreModel>()

    // Lista internă cu toate magazinele descărcate
    private val allStores = mutableStateListOf<StoreModel>()

    // Variabila care controlează Loading-ul din Wishlist
    val isDataLoaded = mutableStateOf(false)

    // --- 2. VARIABILE PENTRU PROFIL (NOU) ---
    // Folosim mutableStateOf pentru ca UI-ul (TopBar, ProfileScreen) să se actualizeze instantaneu
    var userName = mutableStateOf("Costi")
    var userImagePath = mutableStateOf<String?>(null)

    init {
        Log.d("DashboardViewModel", "=== INIT START ===")

        // Încărcăm datele utilizatorului la pornire
        loadUserData()

        // Încărcăm favoritele și magazinele
        loadFavorites()
        loadAllStoresData()
    }

    // --- 3. FUNCȚII PENTRU PROFIL (NOU) ---
    private fun loadUserData() {
        userName.value = userManager.getName()
        userImagePath.value = userManager.getImagePath()
        Log.d("DashboardViewModel", "User loaded: ${userName.value}, Image: ${userImagePath.value}")
    }

    fun updateUserName(newName: String) {
        userName.value = newName
        userManager.saveName(newName)
    }

    fun updateUserImage(uri: android.net.Uri) {
        // 1. Copiem imaginea în memoria internă a aplicației (prin UserManager)
        val internalPath = userManager.copyImageToInternalStorage(uri)

        // 2. Actualizăm starea și salvăm calea dacă operațiunea a reușit
        if (internalPath != null) {
            userImagePath.value = internalPath
            userManager.saveImagePath(internalPath)
            Log.d("DashboardViewModel", "Image updated successfully: $internalPath")
        } else {
            Log.e("DashboardViewModel", "Failed to save image")
        }
    }

    // --- LOGICA EXISTENTĂ PENTRU MAGAZINE ȘI FAVORITE ---

    private fun loadFavorites() {
        favoriteStoreIds.clear()
        val savedFavorites = favoritesManager.getFavorites()
        favoriteStoreIds.addAll(savedFavorites)
        Log.d("DashboardViewModel", "✅ Loaded ${favoriteStoreIds.size} saved favorites")
    }

    private fun loadAllStoresData() {
        // Avem 6 cereri de făcut (Popular/Nearest pentru cat 0, 1, 2)
        var finishedQueries = 0
        val totalQueries = 6

        // Funcție internă care verifică dacă s-a terminat tot
        fun checkAllFinished() {
            finishedQueries++

            if (finishedQueries >= totalQueries) {
                isDataLoaded.value = true
                Log.d("DashboardViewModel", "🏁 ALL DATA LOADED. Hide loading spinner.")
                updateFavoriteStores()
            }
        }

        // Funcție helper pentru a face cererile
        fun observeAndAdd(categoryId: String, mode: String) {
            val liveData = if (mode == "popular") {
                resultsRepository.loadPopular(categoryId, limit = null)
            } else {
                resultsRepository.loadNearest(categoryId, limit = null)
            }

            liveData.observeForever { resource ->
                if (resource !is Resource.Loading) {
                    if (resource is Resource.Success) {
                        resource.data?.let { newStores ->
                            // Adăugăm în allStores doar dacă nu există deja
                            newStores.forEach { store ->
                                if (allStores.none { it.getUniqueId() == store.getUniqueId() }) {
                                    allStores.add(store)
                                }
                            }
                            // Actualizăm favoritele imediat ce avem date noi (ca să apară în Wishlist instant)
                            if (newStores.isNotEmpty()) {
                                updateFavoriteStores()
                            }
                        }
                    }
                    // Marcăm cererea ca terminată indiferent de rezultat
                    checkAllFinished()
                }
            }
        }

        // --- Încărcăm toate categoriile necesare ---
        observeAndAdd("0", "popular")
        observeAndAdd("0", "nearest")

        observeAndAdd("1", "popular")
        observeAndAdd("1", "nearest")

        observeAndAdd("2", "popular")
        observeAndAdd("2", "nearest")
    }

    private fun updateFavoriteStores() {
        // Filtrăm din toate magazinele (allStores) doar pe cele care au ID-ul în lista de favorite
        val favorites = allStores.filter { store ->
            favoriteStoreIds.contains(store.getUniqueId())
        }

        favoriteStores.clear()
        favoriteStores.addAll(favorites)

        Log.d("DashboardViewModel", "🔄 Wishlist updated: ${favoriteStores.size} stores shown.")
    }

    fun isFavorite(store: StoreModel): Boolean {
        return favoriteStoreIds.contains(store.getUniqueId())
    }

    fun toggleFavorite(store: StoreModel) {
        val uniqueKey = store.getUniqueId()

        if (favoriteStoreIds.contains(uniqueKey)) {
            favoritesManager.removeFavorite(uniqueKey)
            favoriteStoreIds.remove(uniqueKey)
        } else {
            favoritesManager.addFavorite(uniqueKey)
            favoriteStoreIds.add(uniqueKey)
        }

        // Actualizăm lista de obiecte StoreModel pentru Wishlist
        updateFavoriteStores()
    }

    // Funcții standard
    fun loadCategory(): LiveData<MutableList<CategoryModel>> = repository.loadCategory()
    fun loadBanner(): LiveData<MutableList<BannerModel>> = repository.loadBanner()
}