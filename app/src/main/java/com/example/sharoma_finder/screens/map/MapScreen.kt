package com.example.sharoma_finder.screens.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.sharoma_finder.utils.LockScreenOrientation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    store: StoreModel,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onBackClick: () -> Unit
) {
    LockScreenOrientation()
    if (store.Latitude == 0.0 || store.Longitude == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.black2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Locația nu este disponibilă pentru acest restaurant\n\nTe rugăm să contactezi localul direct",
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

    var hasLocationPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val userMarkerState = remember { MarkerState() }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(storeLatlng, 15f)
    }

    LaunchedEffect(store.firebaseKey) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(storeLatlng, 15f)
    }

    val storeMarkerState = remember(store.firebaseKey) {
        MarkerState(position = storeLatlng)
    }

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

    DisposableEffect(hasLocationPermission) {
        val currentPermission = checkPermissions()
        hasLocationPermission = currentPermission

        if (!currentPermission) {
            return@DisposableEffect onDispose { }
        }

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10000L
        ).apply {
            setMinUpdateIntervalMillis(5000L)
            setMaxUpdateDelayMillis(15000L)
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
                    userLocation = newLatLng
                    userMarkerState.position = newLatLng
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
        }

        onDispose {
            try {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                userLocation = null
            } catch (e: Exception) {
            }
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (map, detail, backBtn, centerBtn) = createRefs()

        val mapProperties = remember {
            MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            )
        }

        val uiSettings = remember {
            MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true
            )
        }

        GoogleMap(
            modifier = Modifier.fillMaxSize().constrainAs(map) { centerTo(parent) },
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            Marker(
                state = storeMarkerState,
                title = store.Title,
                snippet = store.Address,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )

            if (hasLocationPermission && userLocation != null) {
                Marker(
                    state = userMarkerState,
                    title = "Locația ta",
                    snippet = "Ești aici",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }
        }

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
            Image(
                painter = painterResource(R.drawable.back),
                contentDescription = "Înapoi",
                modifier = Modifier.size(24.dp)
            )
        }

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
                    contentDescription = "Centrează pe mine",
                    tint = colorResource(R.color.gold),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

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
                ItemsNearest(
                    item = store,
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick
                )
            }
            item {
                Button(
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.gold),
                        disabledContainerColor = Color.Gray
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    enabled = store.hasValidPhoneNumber(),
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${store.getCleanPhoneNumber()}"))
                        context.startActivity(dialIntent)
                    }
                ) {
                    Text(
                        text = if (store.hasValidPhoneNumber()) "Sună" else "Număr indisponibil",
                        fontSize = 16.sp,
                        color = if (store.hasValidPhoneNumber()) Color.Black else Color.LightGray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}