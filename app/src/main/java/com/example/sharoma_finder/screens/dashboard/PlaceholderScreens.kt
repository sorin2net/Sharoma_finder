package com.example.sharoma_finder.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.screens.results.ItemsNearest

@Composable
fun WishlistScreen(
    favoriteStores: SnapshotStateList<StoreModel>,
    isDataLoaded: Boolean,
    onFavoriteToggle: (StoreModel) -> Unit,
    onStoreClick: (StoreModel) -> Unit,
    isStoreFavorite: (StoreModel) -> Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
    ) {
        when {
            // Dacă datele încă se încarcă
            !isDataLoaded -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorResource(R.color.gold))
                }
            }
            // Dacă lista e goală
            favoriteStores.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Your wishlist is empty\n\nTap the heart icon on stores to add them here!",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }
            // Afișează lista de favorite
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Locuri preferate (${favoriteStores.size})",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.gold),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    items(favoriteStores.size) { index ->
                        val store = favoriteStores[index]
                        ItemsNearest(
                            item = store,
                            isFavorite = isStoreFavorite(store),
                            onFavoriteClick = { onFavoriteToggle(store) },
                            onClick = { onStoreClick(store) }
                        )
                    }
                }
            }
        }
    }
}