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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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

    // Animație pentru rotația butonului de spin
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
            // === HEADER CU BACK BUTTON ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                // Back Button
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

                // Title
                Text(
                    text = "Random Picker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gold),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // === CARD PENTRU AFIȘARE RESTAURANT ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
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
                        // STARE INIȚIALĂ - Niciun restaurant selectat
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

                        // SPINNING - Afișează restaurante rapid
                        isSpinning && currentDisplayStore != null -> {
                            StoreDisplayCard(
                                store = currentDisplayStore!!,
                                isSpinning = true,
                                onClick = { }
                            )
                        }

                        // FINAL - Afișează restaurantul câștigător
                        !isSpinning && finalStore != null -> {
                            StoreDisplayCard(
                                store = finalStore!!,
                                isSpinning = false,
                                onClick = { onStoreClick(finalStore!!) }
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
                            // Animație de spin - 2 secunde
                            val spinDuration = 2000L
                            val intervalMs = 80L // Schimbă restaurantul la fiecare 80ms
                            val iterations = (spinDuration / intervalMs).toInt()

                            repeat(iterations) { iteration ->
                                // Randomizează restaurantul afișat
                                currentDisplayStore = allStores.random()

                                // Delay progresiv pentru efect de slow-down
                                val progressFactor = iteration.toFloat() / iterations
                                val currentDelay = (intervalMs * (1 + progressFactor * 2)).toLong()
                                delay(currentDelay)
                            }

                            // Selectează restaurantul final
                            val winner = allStores[Random.nextInt(allStores.size)]
                            currentDisplayStore = winner
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

            // === INFO TEXT ===
            Text(
                text = when {
                    allStores.isEmpty() -> "No stores available"
                    isSpinning -> "Spinning..."
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

/**
 * Card pentru afișarea restaurantului
 */
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagine restaurant
        AsyncImage(
            model = store.ImagePath,
            contentDescription = store.Title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colorResource(R.color.grey)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Titlu
        Text(
            text = store.Title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Adresă
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.location),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = store.ShortAddress,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Activitate
        Text(
            text = store.Activity,
            fontSize = 14.sp,
            color = colorResource(R.color.gold),
            textAlign = TextAlign.Center
        )

        // Indicator vizual pentru starea finală
        if (!isSpinning) {
            Spacer(modifier = Modifier.height(16.dp))
            Icon(
                imageVector = Icons.Default.Casino,
                contentDescription = "Winner",
                tint = colorResource(R.color.gold),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}