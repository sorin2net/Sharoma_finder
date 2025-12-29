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
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect // ✅ Import nou
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle // ✅ Import nou
import androidx.lifecycle.LifecycleEventObserver // ✅ Import nou
import androidx.lifecycle.compose.LocalLifecycleOwner // ✅ Import nou
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.InternetConsentManager
import com.example.sharoma_finder.screens.common.InternetConsentDialog
import com.example.sharoma_finder.screens.dashboard.DashboardScreen
import com.example.sharoma_finder.screens.map.MapScreen
import com.example.sharoma_finder.screens.results.AllStoresScreen
import com.example.sharoma_finder.screens.results.ResultList
import com.example.sharoma_finder.viewModel.DashboardViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    // ✅ ADĂUGAT: Manager pentru consimțământ internet
    private lateinit var internetConsentManager: InternetConsentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Inițializare manager
        internetConsentManager = InternetConsentManager(applicationContext)

        // ===== LOCATION PERMISSION (rămâne la fel) =====
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocation = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            val coarseLocation = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)

            if (fineLocation || coarseLocation) {
                Log.d("MainActivity", "✅ Location permission granted")
                dashboardViewModel.fetchUserLocation()
                dashboardViewModel.checkLocationPermission() // Actualizăm și starea explicit
            } else {
                Log.w("MainActivity", "⚠️ Location permission denied")
                dashboardViewModel.checkLocationPermission() // Actualizăm starea ca fiind false
            }
        }

        val hasFineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            Log.d("MainActivity", "✅ Permissions already granted")
            dashboardViewModel.fetchUserLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }

        setContent {
            // ✅ ADĂUGAT: Lifecycle observer pentru a verifica permisiunea la onResume
            // Asta ajută dacă userul iese din app, activează locația în Setări și revine.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        Log.d("MainActivity", "🔄 App Resumed - Checking location permission")
                        dashboardViewModel.checkLocationPermission()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            MainApp(
                dashboardViewModel = dashboardViewModel,
                internetConsentManager = internetConsentManager
            )
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
    dashboardViewModel: DashboardViewModel,
    internetConsentManager: InternetConsentManager
) {
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        systemUiController.isNavigationBarVisible = false
        systemUiController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    systemUiController.setStatusBarColor(color = colorResource(R.color.white))

    val backStack = remember { mutableStateListOf<Screen>(Screen.Dashboard) }
    val currentScreen = backStack.last()

    // ✅ ADĂUGAT: State pentru dialog internet consent
    var showInternetConsentDialog by remember { mutableStateOf(false) }

    // ✅ LOGICĂ: Verificăm la pornire dacă trebuie să cerem consimțământul
    LaunchedEffect(Unit) {
        Log.d("MainActivity", "🌐 Checking internet consent...")

        // Dacă nu am întrebat încă și nu are consimțământ
        if (!internetConsentManager.hasAskedForConsent() && !internetConsentManager.hasInternetConsent()) {
            Log.d("MainActivity", "❓ Showing internet consent dialog")
            showInternetConsentDialog = true
        } else if (internetConsentManager.hasInternetConsent()) {
            Log.d("MainActivity", "✅ Internet consent already granted")
            // Dacă are consimțământ, pornim sincronizarea
            dashboardViewModel.enableInternetFeatures()
        } else {
            Log.d("MainActivity", "❌ Internet consent declined previously")
            // Utilizatorul a refuzat înainte - nu mai întrebăm
        }
    }

    // ✅ DIALOG DE CONSIMȚĂMÂNT
    if (showInternetConsentDialog) {
        InternetConsentDialog(
            onAccept = {
                Log.d("MainActivity", "✅ User ACCEPTED internet consent")
                internetConsentManager.grantConsent()
                dashboardViewModel.enableInternetFeatures()
                showInternetConsentDialog = false
            },
            onDecline = {
                Log.d("MainActivity", "❌ User DECLINED internet consent")
                internetConsentManager.markConsentAsked()
                dashboardViewModel.disableInternetFeatures()
                showInternetConsentDialog = false
            }
        )
    }

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
            val listToSend = when (screen.mode) {
                "popular" -> {
                    dashboardViewModel.getGlobalStoreList()
                        .filter { it.CategoryIds.contains(screen.id) && it.IsPopular }
                        .sortedBy { if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser }
                }
                "nearest", "nearest_all" -> {
                    dashboardViewModel.getGlobalStoreList()
                        .filter { it.CategoryIds.contains(screen.id) }
                        .sortedBy { if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser }
                }
                else -> emptyList()
            }

            Log.d("MainActivity", "📦 Sending ${listToSend.size} stores for mode: ${screen.mode}")

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
                onFavoriteClick = { dashboardViewModel.toggleFavorite(screen.store) },
                onBackClick = { popBackStack() } // ✅ Transmitem funcția care șterge ultimul ecran din listă
            )
        }
    }
}