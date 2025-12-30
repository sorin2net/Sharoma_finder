package com.example.sharoma_finder.screens.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import com.example.sharoma_finder.screens.common.InternetConsentDialog // ✅ Asigură-te că ai acest import
import com.example.sharoma_finder.viewModel.DashboardViewModel
import java.io.File

@Composable
fun ProfileScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    // ✅ STATE PENTRU DIALOGUL DE CONSIMȚĂMÂNT DIN PROFIL
    var showConsentDialog by remember { mutableStateOf(false) }

    var tempName by remember { mutableStateOf("") }

    // ✅ MONITORIZARE INTERNET LIVE
    val isSystemOnline by rememberConnectivityState()

    // ✅ LAUNCHER PENTRU POZĂ
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateUserImage(uri)
        }
    }

    // ✅ LAUNCHER PENTRU PERMISIUNI LOCAȚIE
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.checkLocationPermission()
            Toast.makeText(context, "Location activated! 📍", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission needed for nearest stores.", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ ANIMAȚIE PENTRU REFRESH
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    // ✅ AICI INSERĂM DIALOGUL (Apare peste ecran când showConsentDialog este true)
    if (showConsentDialog) {
        InternetConsentDialog(
            onAccept = {
                // Utilizatorul a acceptat: Salvăm consimțământul și pornim netul
                viewModel.grantInternetConsentFromProfile()
                showConsentDialog = false
                Toast.makeText(context, "Internet Access Granted! 🌍", Toast.LENGTH_SHORT).show()
            },
            onDecline = {
                // Utilizatorul a refuzat din nou: Switch-ul rămâne OFF
                showConsentDialog = false
                Toast.makeText(context, "Internet Access Denied ❌", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 64.dp)
                .padding(horizontal = 16.dp)
        ) {
            // --- TITLE ---
            Text(
                text = "My Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // --- PROFILE PICTURE ---
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
            ) {
                if (viewModel.userImagePath.value != null) {
                    AsyncImage(
                        model = File(viewModel.userImagePath.value!!),
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.profile),
                        contentDescription = "Default Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Icon(
                    painter = painterResource(android.R.drawable.ic_menu_camera),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NAME + EDIT BUTTON ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = viewModel.userName.value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = {
                    tempName = viewModel.userName.value
                    showEditDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Name",
                        tint = colorResource(R.color.gold)
                    )
                }
            }

// ✅ RANGUL (Acum este SUB rând, deci pe o linie nouă)
            Text(
                text = viewModel.getUserRank(),
                fontSize = 18.sp,
                color = colorResource(R.color.gold),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = colorResource(R.color.black3)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progres Rang", color = Color.White, fontSize = 14.sp)
                        Text("${viewModel.userPoints.value} XP", color = colorResource(R.color.gold), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = viewModel.getRankProgress(), // ✅ Sultan (692 XP) va avea bara 100% plină
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = colorResource(R.color.gold),
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ ========== INTERNET TOGGLE SWITCH ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemOnline) colorResource(R.color.black3) else Color.DarkGray.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when {
                        !isSystemOnline -> Icons.Default.SignalWifiOff
                        viewModel.hasInternetAccess.value -> Icons.Default.Wifi
                        else -> Icons.Default.WifiOff
                    }

                    val iconColor = when {
                        !isSystemOnline -> Color.Red
                        viewModel.hasInternetAccess.value -> colorResource(R.color.gold)
                        else -> Color.Gray
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = "Internet",
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSystemOnline) "Internet Access" else "No Connection",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(isSystemOnline) Color.White else Color.LightGray
                        )
                        Text(
                            text = when {
                                !isSystemOnline -> "Check device settings"
                                viewModel.hasInternetAccess.value -> "Online mode"
                                else -> "Offline mode"
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    // ✅ MODIFICARE PRINCIPALĂ: Logica Switch-ului
                    Switch(
                        checked = viewModel.hasInternetAccess.value && isSystemOnline,
                        enabled = isSystemOnline,
                        onCheckedChange = { isChecked ->
                            // Folosim noua funcție din ViewModel care decide dacă arată dialogul
                            viewModel.onInternetSwitchToggled(
                                enabled = isChecked,
                                onShowConsentDialog = {
                                    showConsentDialog = true
                                }
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(R.color.gold),
                            checkedTrackColor = colorResource(R.color.gold).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray,
                            disabledCheckedTrackColor = Color.DarkGray,
                            disabledUncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }

            Text(
                text = if (!isSystemOnline) "System offline. Cached data only." else "Disable to use only cached data (offline mode)",
                color = if(!isSystemOnline) Color.Red else Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
            )

            // ✅ ========== LOCATION PERMISSION SECTION ==========
            Spacer(modifier = Modifier.height(16.dp))


            if (!viewModel.isLocationPermissionGranted.value) {
                // CAZUL 1: Permisiune lipsă - Buton Roșu Activ
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(
                            color = colorResource(R.color.black3),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = "Activate GPS Location",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Required for Nearest Stores",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Text(
                    text = "Button not working? Open Settings",
                    color = colorResource(R.color.gold),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                )

            } else {
                // CAZUL 2: Permisiune Acordată - Card Verde informativ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(colorResource(R.color.black3), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Location Active",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            // ========================================================

            Spacer(modifier = Modifier.height(24.dp))

            // --- FORCE REFRESH BUTTON ---
            val canRefresh = !viewModel.isRefreshing.value && viewModel.hasInternetAccess.value && isSystemOnline

            Button(
                onClick = {
                    viewModel.forceRefreshAllData {
                        Toast.makeText(context, "Everything refreshed! 🚀", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = canRefresh,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.gold),
                    disabledContainerColor = Color.DarkGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (canRefresh) Color.Black else Color.Gray,
                        modifier = Modifier.rotate(if (viewModel.isRefreshing.value) angle else 0f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when {
                            !isSystemOnline -> "No Internet"
                            !viewModel.hasInternetAccess.value -> "Offline Mode"
                            viewModel.isRefreshing.value -> "Refreshing..."
                            else -> "Force Refresh Data"
                        },
                        color = if (canRefresh) Color.Black else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = when {
                    !isSystemOnline -> "Cannot refresh without connection"
                    viewModel.hasInternetAccess.value -> "Force download latest data from Firebase"
                    else -> "Enable internet access to refresh data"
                },
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        // --- EDIT NAME DIALOG ---
        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = colorResource(R.color.black3),
                title = { Text("Change Name", color = colorResource(R.color.gold)) },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = colorResource(R.color.gold),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                viewModel.updateUserName(tempName)
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.gold))
                    ) {
                        Text("Save", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                }
            )
        }
    }
}

/**
 * ✅ HELPER PENTRU CONEXIUNE
 */
@Composable
fun rememberConnectivityState(): State<Boolean> {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    val isOnline = remember { mutableStateOf(isConnected()) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline.value = true
            }

            override fun onLost(network: Network) {
                isOnline.value = false
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return isOnline
}