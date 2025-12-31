package com.example.sharoma_finder.screens.results

import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel

@Composable
fun AllStoresScreen(
    categoryId: String,
    mode: String,
    onBackClick: () -> Unit,
    onStoreClick: (StoreModel) -> Unit,
    isStoreFavorite: (StoreModel) -> Boolean,
    onFavoriteToggle: (StoreModel) -> Unit,
    preLoadedList: List<StoreModel>? = null,
    userLocation: Location? = null
) {
    LaunchedEffect(preLoadedList?.size) {
    }

    val isLoading = preLoadedList == null || preLoadedList.isEmpty()

    val listToDisplay = remember(preLoadedList, userLocation) {
        if (preLoadedList != null && userLocation != null) {
            preLoadedList.sortedBy {
                if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
            }
        } else {
            preLoadedList ?: emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
    ) {
        Column {
            TopTile(
                title = if (mode == "popular") "Populare" else "Cele mai apropiate",
                onBackClick = onBackClick
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colorResource(R.color.gold))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Se încarcă restaurantele...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (listToDisplay.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nu am găsit restaurante ${if (mode == "popular") "populare" else "din apropiere"}",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listToDisplay.size) { index ->
                        val item = listToDisplay[index]
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ItemsPopular(
                                item = item,
                                isFavorite = isStoreFavorite(item),
                                onFavoriteClick = { onFavoriteToggle(item) },
                                onClick = { onStoreClick(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}