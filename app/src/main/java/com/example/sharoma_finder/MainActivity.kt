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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.screens.dashboard.DashboardScreen
import com.example.sharoma_finder.screens.map.MapScreen
import com.example.sharoma_finder.screens.results.AllStoresScreen
import com.example.sharoma_finder.screens.results.ResultList
import com.example.sharoma_finder.viewModel.DashboardViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cerem permisiunile la start
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Avem locație precisă
                    Log.d("MainActivity", "Fine location permission granted")
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Avem locație aproximativă
                    Log.d("MainActivity", "Coarse location permission granted")
                }
                else -> {
                    // Nu avem permisiune
                    Log.w("MainActivity", "Location permission denied")
                }
            }
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            MainApp()
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
fun MainApp() {
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val dashboardViewModel: DashboardViewModel = viewModel()

    // Immersive Mode
    LaunchedEffect(Unit) {
        systemUiController.isNavigationBarVisible = false
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    // --- IMPROVED GPS LOGIC ---
    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Verificăm permisiunile
        val hasPermission = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            dashboardViewModel.updateUserLocation(location)
                            Log.d("MainActivity", "GPS location obtained successfully: ${location.latitude}, ${location.longitude}")
                        } else {
                            // GPS e pornit dar nu avem încă locație (poate fi null pe emulator uneori)
                            Log.w("MainActivity", "GPS enabled but no location available yet")
                            // Aici s-ar putea adăuga logică pentru a cere actualizări active de locație, dar e mai complex
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MainActivity", "Failed to get GPS location", exception)
                        // App-ul va funcționa fără sortare după distanță
                    }
            } catch (e: SecurityException) {
                Log.e("MainActivity", "Security exception when accessing GPS", e)
            }
        } else {
            Log.w("MainActivity", "GPS permissions not granted - distance sorting disabled")
            // App-ul va funcționa, dar fără sortare după distanță
        }
    }
    // -------------------------

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
            // Verificăm dacă userul a dat click pe "See All" la Nearest
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
                // ✅ ADĂUGAT: Trimitem locația pentru calcul distanță
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