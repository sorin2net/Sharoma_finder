package com.example.sharoma_finder.screens.random

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
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
import coil.imageLoader
import coil.request.ImageRequest
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.sharoma_finder.viewModel.DashboardViewModel

@Composable
fun RandomRecommenderScreen(
    allStores: List<StoreModel>,
    onBackClick: () -> Unit,
    viewModel: DashboardViewModel,
    onStoreClick: (StoreModel) -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var currentDisplayStore by remember { mutableStateOf<StoreModel?>(null) }
    var finalStore by remember { mutableStateOf<StoreModel?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "spin_rotation")
    val spinButtonRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "button_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // === HEADER ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(
                            color = colorResource(R.color.black3).copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .clickable { onBackClick() }
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Random Picker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gold),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === CARD DISPLAY ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.black3)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        currentDisplayStore == null && finalStore == null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = colorResource(R.color.gold),
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Press SPIN to get\na random restaurant",
                                    fontSize = 18.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        else -> {
                            StoreDisplayCard(
                                store = if (isSpinning) currentDisplayStore!! else finalStore!!,
                                isSpinning = isSpinning,
                                onClick = { if (!isSpinning) onStoreClick(finalStore!!) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // === SPIN BUTTON ===
            Button(
                onClick = {
                    viewModel.addPoints(10)
                    if (!isSpinning && allStores.isNotEmpty()) {
                        isSpinning = true
                        finalStore = null

                        scope.launch {
                            val iterations = 12
                            val baseDelay = 200L

                            // Alegem câștigătorul de la început, dar îl afișăm doar la final
                            val winner = allStores.random()

                            repeat(iterations) { iteration ->
                                // Ultimele 2 iterații vor tinde spre câștigător sau rămân random
                                val nextStore = if (iteration == iterations - 1) winner else allStores.random()

                                val request = ImageRequest.Builder(context)
                                    .data(nextStore.ImagePath)
                                    .build()

                                context.imageLoader.execute(request)
                                currentDisplayStore = nextStore

                                val progressFactor = iteration.toFloat() / iterations
                                val currentDelay = (baseDelay * (1 + progressFactor * 2)).toLong()
                                delay(currentDelay)
                            }

                            finalStore = winner
                            isSpinning = false
                        }
                    }
                },
                enabled = !isSpinning && allStores.isNotEmpty(),
                modifier = Modifier
                    .size(120.dp)
                    .rotate(if (isSpinning) spinButtonRotation else 0f),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.gold),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Spin",
                        tint = Color.Black,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSpinning) "..." else "SPIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    allStores.isEmpty() -> "No stores available"
                    isSpinning -> "Searching..."
                    finalStore != null -> "Tap the card to view on map"
                    else -> "Ready to spin!"
                },
                fontSize = 14.sp,
                color = if (isSpinning) colorResource(R.color.gold) else Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StoreDisplayCard(
    store: StoreModel,
    isSpinning: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = !isSpinning) { onClick() }
            // ✅ MODIFIED: Reduced overall padding, removed top padding completely
            .padding(top = 0.dp, start = 12.dp, end = 12.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = store.ImagePath,
            contentDescription = store.Title,
            modifier = Modifier
                .fillMaxWidth()
                // ✅ MODIFIED: Increased image height from 200.dp to 250.dp
                .height(250.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorResource(R.color.grey)),
            contentScale = ContentScale.Crop
        )

        // ✅ MODIFIED: Reduced spacing after the larger image
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = store.Title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (store.distanceToUser >= 0) {
            val distanceKm = store.distanceToUser / 1000
            Text(
                text = "${String.format("%.1f", distanceKm)} km away",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.gold),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.location),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = store.Address,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = store.Activity,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        if (!isSpinning) {
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Winner",
                tint = colorResource(R.color.gold),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}