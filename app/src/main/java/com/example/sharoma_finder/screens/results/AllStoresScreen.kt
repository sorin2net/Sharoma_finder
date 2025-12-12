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
    // ✅ DEBUGGING
    LaunchedEffect(preLoadedList?.size) {
        Log.d("AllStoresScreen", "📦 Received ${preLoadedList?.size ?: 0} stores for mode: $mode")
    }

    // ✅ FIX: Dacă lista e null SAU goală, afișăm loading
    val isLoading = preLoadedList == null || preLoadedList.isEmpty()

    // ✅ Sortăm lista după distanță (dacă avem locație)
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
                title = if (mode == "popular") "Popular" else "Nearest",
                onBackClick = onBackClick
            )

            if (isLoading) {
                // ✅ LOADING STATE
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colorResource(R.color.gold))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Loading stores...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else if (listToDisplay.isEmpty()) {
                // ✅ EMPTY STATE
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${if (mode == "popular") "popular" else "nearby"} stores found",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            } else {
                // ✅ SUCCESS STATE - Afișăm grid-ul
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