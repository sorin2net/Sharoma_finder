package com.example.sharoma_finder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels // ✅ IMPORTANT: Necesar pentru instanțiere în Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.screens.dashboard.DashboardScreen
import com.example.sharoma_finder.screens.map.MapScreen
import com.example.sharoma_finder.screens.results.AllStoresScreen
import com.example.sharoma_finder.screens.results.ResultList
import com.example.sharoma_finder.viewModel.DashboardViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    // ✅ 1. Instanțiem ViewModel-ul aici pentru a avea acces la el în callback-uri
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ 2. Definim ce se întâmplă când utilizatorul răspunde la popup
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocation = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocation = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            if (fineLocation || coarseLocation) {
                Log.d("MainActivity", "Permission granted by user. Fetching location NOW.")
                // ✅ FIX: Apelăm fetch imediat ce primim permisiunea
                dashboardViewModel.fetchUserLocation()
            } else {
                Log.w("MainActivity", "Location permission denied by user")
            }
        }

        // ✅ 3. Verificăm starea inițială la pornirea aplicației
        val hasFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            // Dacă avem deja permisiunea (de la o rulare anterioară), luăm locația
            Log.d("MainActivity", "Permissions already granted. Fetching location.")
            dashboardViewModel.fetchUserLocation()
        } else {
            // Dacă nu avem permisiunea, lansăm cererea
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        setContent {
            // ✅ 4. Trimitem ViewModel-ul deja creat către UI
            MainApp(dashboardViewModel)
        }
    }
}

sealed class Screen {
    data object Dashboard : Screen()
    data class Results(val id: String, val title: String) : Screen()
    data class Map(val store: StoreModel) : Screen()
    data class ViewAll(val id: String, val mode: String) : Screen()
}

@Composable
fun MainApp(
    // ✅ 5. Primim ViewModel-ul ca parametru
    dashboardViewModel: DashboardViewModel
) {
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current

    // Immersive Mode
    LaunchedEffect(Unit) {
        systemUiController.isNavigationBarVisible = false
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // NOTĂ: Am eliminat LaunchedEffect-ul de aici pentru permisiuni,
    // deoarece acum MainActivity se ocupă complet de acest flow.

    systemUiController.setStatusBarColor(color = colorResource(R.color.white))

    val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    val currentScreen = backStack.last()

    fun popBackStack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    BackHandler(enabled = backStack.size > 1) {
        popBackStack()
    }

    when (val screen = currentScreen) {
        Screen.Dashboard -> {
            DashboardScreen(
                onCategoryClick = { id, title ->
                    backStack.add(Screen.Results(id, title))
                },
                onStoreClick = { store ->
                    backStack.add(Screen.Map(store))
                },
                viewModel = dashboardViewModel
            )
        }
        is Screen.Results -> {
            ResultList(
                id = screen.id,
                title = screen.title,
                onBackClick = { popBackStack() },
                onStoreClick = { store -> backStack.add(Screen.Map(store)) },
                onSeeAllClick = { mode ->
                    backStack.add(Screen.ViewAll(screen.id, mode))
                },
                isStoreFavorite = { store -> dashboardViewModel.isFavorite(store) },
                onFavoriteToggle = { store -> dashboardViewModel.toggleFavorite(store) },
                allGlobalStores = dashboardViewModel.getGlobalStoreList(),
                userLocation = dashboardViewModel.currentUserLocation
            )
        }
        is Screen.ViewAll -> {
            val listToSend = if (screen.mode == "nearest" || screen.mode == "nearest_all") {
                dashboardViewModel.nearestStoresAllSorted
            } else {
                null
            }

            AllStoresScreen(
                categoryId = screen.id,
                mode = screen.mode,
                onBackClick = { popBackStack() },
                onStoreClick = { store -> backStack.add(Screen.Map(store)) },
                isStoreFavorite = { store -> dashboardViewModel.isFavorite(store) },
                onFavoriteToggle = { store -> dashboardViewModel.toggleFavorite(store) },
                preLoadedList = listToSend,
                userLocation = dashboardViewModel.currentUserLocation
            )
        }
        is Screen.Map -> {
            MapScreen(
                store = screen.store,
                isFavorite = dashboardViewModel.isFavorite(screen.store),
                onFavoriteClick = { dashboardViewModel.toggleFavorite(screen.store) }
            )
        }
    }
}