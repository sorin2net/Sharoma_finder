package com.example.sharoma_finder.screens.dashboard

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
import androidx.compose.material.icons.filled.Refresh
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
import com.example.sharoma_finder.viewModel.DashboardViewModel
import java.io.File

@Composable
fun ProfileScreen(viewModel: DashboardViewModel) {
    val context = LocalContext.current

    var showEditDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateUserImage(uri)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 64.dp)
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

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ ========== INTERNET TOGGLE SWITCH ==========
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.black3)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (viewModel.hasInternetAccess.value) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = "Internet",
                        tint = if (viewModel.hasInternetAccess.value) colorResource(R.color.gold) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Internet Access",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (viewModel.hasInternetAccess.value) "Online mode" else "Offline mode",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Switch(
                        checked = viewModel.hasInternetAccess.value,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // ✅ FIX: Use enableInternetFeatures instead of grantInternetConsent
                                viewModel.enableInternetFeatures()
                                Toast.makeText(context, "Internet enabled ✅", Toast.LENGTH_SHORT).show()
                            } else {
                                // ✅ FIX: Use disableInternetFeatures instead of revokeInternetConsent
                                viewModel.disableInternetFeatures()
                                Toast.makeText(context, "Internet disabled ❌", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorResource(R.color.gold),
                            checkedTrackColor = colorResource(R.color.gold).copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                }
            }

            Text(
                text = "Disable to use only cached data (offline mode)",
                color = Color.Gray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )
            // ==========================================

            Spacer(modifier = Modifier.height(24.dp))

            // --- FORCE REFRESH BUTTON ---
            Button(
                onClick = {
                    viewModel.forceRefreshAllData {
                        Toast.makeText(context, "Everything refreshed! 🚀", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !viewModel.isRefreshing.value && viewModel.hasInternetAccess.value, // ✅ Dezactivat dacă e offline
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.gold),
                    disabledContainerColor = Color.DarkGray
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = if (viewModel.isRefreshing.value || !viewModel.hasInternetAccess.value) Color.Gray else Color.Black,
                        modifier = Modifier.rotate(if (viewModel.isRefreshing.value) angle else 0f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = when {
                            !viewModel.hasInternetAccess.value -> "Offline"
                            viewModel.isRefreshing.value -> "Refreshing..."
                            else -> "Force Refresh Data"
                        },
                        color = if (viewModel.isRefreshing.value || !viewModel.hasInternetAccess.value) Color.Gray else Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = if (viewModel.hasInternetAccess.value) {
                    "Force download latest data from Firebase"
                } else {
                    "Enable internet access to refresh data"
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