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
import com.example.sharoma_finder.screens.common.InternetConsentDialog
import com.example.sharoma_finder.viewModel.DashboardViewModel
import java.io.File

@Composable
fun ProfileScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }

    var tempName by remember { mutableStateOf("") }

    val isSystemOnline by rememberConnectivityState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateUserImage(uri)
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.checkLocationPermission()
            Toast.makeText(context, "Locație activată!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Este necesară permisiunea pentru locale apropiate.", Toast.LENGTH_SHORT).show()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spin_angle"
    )

    if (showConsentDialog) {
        InternetConsentDialog(
            onAccept = {
                viewModel.grantInternetConsentFromProfile()
                showConsentDialog = false
                Toast.makeText(context, "Acces la internet acordat!", Toast.LENGTH_SHORT).show()
            },
            onDecline = {
                showConsentDialog = false
                Toast.makeText(context, "Acces la internet refuzat", Toast.LENGTH_SHORT).show()
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
            Text(
                text = "Profilul meu",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(bottom = 32.dp)
            )

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
                        contentDescription = "Poza de profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.profile),
                        contentDescription = "Profil implicit",
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

            Spacer(modifier = Modifier.height(16.dp))

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
                        contentDescription = "Editați numele",
                        tint = colorResource(R.color.gold)
                    )
                }
            }

            Text(
                text = viewModel.getUserRank(),
                fontSize = 18.sp,
                color = colorResource(R.color.gold),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
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
                        progress = viewModel.getRankProgress(),
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                        color = colorResource(R.color.gold),
                        trackColor = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isSystemOnline) "Acces la Internet" else "Nicio conexiune",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if(isSystemOnline) Color.White else Color.LightGray
                        )
                        Text(
                            text = when {
                                !isSystemOnline -> "Verificați setările dispozitivului"
                                viewModel.hasInternetAccess.value -> "Modul online"
                                else -> "Modul offline"
                            },
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked = viewModel.hasInternetAccess.value && isSystemOnline,
                        enabled = isSystemOnline,
                        onCheckedChange = { isChecked ->
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
                text = if (!isSystemOnline) "Sistem offline. Doar date memorate în cache." else "Dezactivați pentru a utiliza doar datele din cache",
                color = if(!isSystemOnline) Color.Red else Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))


            if (!viewModel.isLocationPermissionGranted.value) {
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
                                text = "Activați locația GPS",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Necesar pentru locurile aproapiate",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Text(
                    text = "Butonul nu funcționează? Deschiți Setările",
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
                        text = "Locație activă",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canRefresh = !viewModel.isRefreshing.value && viewModel.hasInternetAccess.value && isSystemOnline

            Button(
                onClick = {
                    viewModel.forceRefreshAllData {
                        Toast.makeText(context, "Reîncărcat cu succes!", Toast.LENGTH_SHORT).show()
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
                        contentDescription = "Reîncarcă",
                        tint = if (canRefresh) Color.Black else Color.Gray,
                        modifier = Modifier.rotate(if (viewModel.isRefreshing.value) angle else 0f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when {
                            !isSystemOnline -> "Fără internet"
                            !viewModel.hasInternetAccess.value -> "Modul offline"
                            viewModel.isRefreshing.value -> "Reîncărcare..."
                            else -> "Actualizează datele"
                        },
                        color = if (canRefresh) Color.Black else Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = when {
                    !isSystemOnline -> "Nu se poate actualiza fără conexiune la internet"
                    viewModel.hasInternetAccess.value -> "Forțați descărcarea celor mai recente date"
                    else -> "Activați accesul la internet pentru a actualiza datele"
                },
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = colorResource(R.color.black3),
                title = { Text("Schimbă numele", color = colorResource(R.color.gold)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { newValue ->
                                if (newValue.length <= 20) {
                                    tempName = newValue
                                }
                            },
                            singleLine = true,
                            label = { Text("Nume (max 20 caractere)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = colorResource(R.color.gold),
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${tempName.length} / 20",
                            color = if (tempName.length >= 20) Color.Red else Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                        )
                    }
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
                        Text("Salvează", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Anulează", color = Color.White)
                    }
                }
            )
        }
    }
}


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