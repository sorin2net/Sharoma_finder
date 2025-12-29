package com.example.sharoma_finder.screens.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation // ✅ Import nou pentru iconița de locație
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.screens.results.ItemsNearest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory // ✅ Import nou pentru animația camerei
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch // ✅ Import nou pentru corutine

@Composable
fun MapScreen(
    store: StoreModel,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    // ✅ Validare coordonate magazin
    if (store.Latitude == 0.0 || store.Longitude == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.black2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Location not available for this store\n\nPlease contact the store directly",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val storeLatlng = LatLng(store.Latitude, store.Longitude)

    // ✅ MODIFICARE: State-uri pentru locația utilizatorului
    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // ✅ OPTIMIZARE: MarkerState creat o singură dată (nu se recreează la mișcare)
    val userMarkerState = remember { MarkerState() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(storeLatlng, 15f)
    }

    val storeMarkerState = remember { MarkerState(position = storeLatlng) }

    fun checkPermissions(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentPermission = checkPermissions()
                if (hasLocationPermission && !currentPermission) {
                    userLocation = null
                }
                hasLocationPermission = currentPermission
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ✅ TRACKING LOCAȚIE LIVE OPTIMIZAT
    DisposableEffect(hasLocationPermission) {
        val currentPermission = checkPermissions()
        hasLocationPermission = currentPermission

        if (!currentPermission) {
            return@DisposableEffect onDispose { }
        }

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setMaxUpdateDelayMillis(10000L)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!checkPermissions()) {
                    userLocation = null
                    fusedLocationClient.removeLocationUpdates(this)
                    return
                }
                locationResult.lastLocation?.let { location ->
                    val newLatLng = LatLng(location.latitude, location.longitude)
                    // ✅ Actualizăm ambele stări
                    userLocation = newLatLng
                    userMarkerState.position = newLatLng // Markerul se mută lin pe hartă
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            Log.e("MapScreen", "❌ Security exception: ${e.message}")
        }

        onDispose {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                userLocation = null
            } catch (e: Exception) {
                Log.e("MapScreen", "❌ Cleanup error: ${e.message}")
            }
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (map, detail, backBtn, centerBtn) = createRefs()

        GoogleMap(
            modifier = Modifier.fillMaxSize().constrainAs(map) { centerTo(parent) },
            cameraPositionState = cameraPositionState
        ) {
            // Marker Magazin
            Marker(
                state = storeMarkerState,
                title = store.Title,
                snippet = store.Address,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )

            // ✅ Marker Utilizator Optimizat
            if (hasLocationPermission && userLocation != null) {
                Marker(
                    state = userMarkerState, // Folosește starea persistentă
                    title = "Your Location",
                    snippet = "You are here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }
        }

        // Buton Back
        Box(
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .size(45.dp)
                .background(colorResource(R.color.black3).copy(alpha = 0.8f), CircleShape)
                .clickable { onBackClick() }
                .constrainAs(backBtn) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                },
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(R.drawable.back), contentDescription = "Back", modifier = Modifier.size(24.dp))
        }

        // Buton Center on me
        if (hasLocationPermission && userLocation != null) {
            Box(
                modifier = Modifier
                    .padding(top = 48.dp, end = 16.dp)
                    .size(45.dp)
                    .background(colorResource(R.color.black3).copy(alpha = 0.8f), CircleShape)
                    .clickable {
                        userLocation?.let { latLng ->
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                    durationMs = 1000
                                )
                            }
                        }
                    }
                    .constrainAs(centerBtn) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on me",
                    tint = colorResource(R.color.gold),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Card Detalii
        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .padding(start = 8.dp, end = 48.dp, bottom = 32.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.black3), shape = RoundedCornerShape(10.dp))
                .padding(6.dp)
                .constrainAs(detail) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            item {
                ItemsNearest(item = store, isFavorite = isFavorite, onFavoriteClick = onFavoriteClick)
            }
            item {
                Button(
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.gold)),
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.Call}"))
                        context.startActivity(dialIntent)
                    }
                ) {
                    Text("Call to Store", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}