package com.example.sharoma_finder.screens.results

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.viewModel.ResultsViewModel

@Composable
fun ResultList(
    id: String,
    title: String,
    onBackClick: () -> Unit,
    onStoreClick: (StoreModel) -> Unit,
    onSeeAllClick: (String) -> Unit,
    isStoreFavorite: (StoreModel) -> Boolean,
    onFavoriteToggle: (StoreModel) -> Unit,
    allGlobalStores: List<StoreModel> = emptyList(),
    userLocation: Location? = null
) {
    val viewModel: ResultsViewModel = viewModel()

    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedCategoryName by remember { mutableStateOf("") }

    val subCategoryState by remember(id) { viewModel.loadSubCategory(id) }.observeAsState(Resource.Loading())
    val popularState by remember(id) { viewModel.loadPopular(id, limit = 100) }.observeAsState(Resource.Loading())
    val nearestState by remember(id) { viewModel.loadNearest(id, limit = 100) }.observeAsState(Resource.Loading())

    val subCategoryList = subCategoryState.data ?: emptyList()
    val popularList = popularState.data ?: emptyList()
    val nearestList = nearestState.data ?: emptyList()

    val showSubCategoryLoading = subCategoryState is Resource.Loading
    val showPopularLoading = popularState is Resource.Loading
    val showNearestLoading = nearestState is Resource.Loading

    // --- 1. CALCULĂM DISTANȚELE DIRECT ÎN LISTE ---

    // Funcție ajutătoare pentru calcul
    fun calculateDistanceForList(list: List<StoreModel>, location: Location?) {
        if (location != null) {
            list.forEach { store ->
                val storeLoc = Location("store")
                storeLoc.latitude = store.Latitude
                storeLoc.longitude = store.Longitude
                store.distanceToUser = location.distanceTo(storeLoc)
            }
        }
    }

    val subCategorySnapshot = remember(subCategoryList) { listToSnapshot(subCategoryList) }

    // Lista POPULAR: Calculăm distanța, dar păstrăm ordinea originală (de popularitate)
    val popularSnapshot = remember(popularList, userLocation) {
        calculateDistanceForList(popularList, userLocation)
        listToSnapshot(popularList)
    }

    // Lista NEAREST: Calculăm distanța ȘI SORTĂM crescător
    val nearestSnapshot = remember(nearestList, userLocation) {
        calculateDistanceForList(nearestList, userLocation)
        // Sortăm doar dacă avem locația
        val sortedList = if (userLocation != null) {
            nearestList.sortedBy { it.distanceToUser }
        } else {
            nearestList
        }
        listToSnapshot(sortedList)
    }

    // --- 2. LOGICA PENTRU SEARCH (SORTATĂ DUPĂ DISTANȚĂ) ---
    val searchResults = remember(searchText, allGlobalStores) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            allGlobalStores
                .filter { store ->
                    store.Title.contains(searchText, ignoreCase = true) ||
                            store.Address.contains(searchText, ignoreCase = true)
                }
                .sortedBy { store ->
                    // Sortare: distanța mică prima.
                    if (store.distanceToUser < 0) Float.MAX_VALUE else store.distanceToUser
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.black2))
    ) {
        item { TopTile(title, onBackClick) }

        item {
            Search(
                text = searchText,
                onValueChange = { newText -> searchText = newText }
            )
        }

        if (searchText.isNotEmpty()) {
            // --- REZULTATE CĂUTARE ---
            item {
                Text(
                    text = "Search Results (${searchResults.size})",
                    color = colorResource(R.color.gold),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (searchResults.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No stores found matching \"$searchText\"", color = Color.Gray)
                    }
                }
            } else {
                item {
                    val rows = searchResults.chunked(2)
                    Column(Modifier.padding(16.dp)) {
                        rows.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowItems.forEach { store ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ItemsPopular(
                                            item = store,
                                            isFavorite = isStoreFavorite(store),
                                            onFavoriteClick = { onFavoriteToggle(store) },
                                            onClick = { onStoreClick(store) }
                                        )
                                    }
                                }
                                if (rowItems.size < 2) Spacer(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        } else {
            // --- LISTELE STANDARD ---

            item {
                SubCategory(
                    subCategory = subCategorySnapshot,
                    showSubCategoryLoading = showSubCategoryLoading,
                    selectedCategoryName = selectedCategoryName,
                    onCategoryClick = { clickedName ->
                        selectedCategoryName = if (selectedCategoryName == clickedName) "" else clickedName
                    }
                )
            }

            // Filtrare locală pe baza sub-categoriei selectate
            val filteredPopular = if (selectedCategoryName.isEmpty()) popularSnapshot else {
                val filtered = popularList.filter { it.Activity.equals(selectedCategoryName, ignoreCase = true) }
                listToSnapshot(filtered)
            }

            val filteredNearest = if (selectedCategoryName.isEmpty()) nearestSnapshot else {
                val filtered = nearestList.filter { it.Activity.equals(selectedCategoryName, ignoreCase = true) }
                // Re-sortăm și lista filtrată dacă e cazul
                val sortedFiltered = if(userLocation != null) filtered.sortedBy { it.distanceToUser } else filtered
                listToSnapshot(sortedFiltered)
            }

            item {
                if (!showPopularLoading && filteredPopular.isNotEmpty()) {
                    PopularSection(
                        list = filteredPopular,
                        showPopularLoading = showPopularLoading,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = { onSeeAllClick("popular") },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }

            item {
                if (!showNearestLoading && filteredNearest.isNotEmpty()) {
                    NearestList(
                        list = filteredNearest,
                        showNearestLoading = showNearestLoading,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = { onSeeAllClick("nearest") },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
        }
    }
}

fun <T> listToSnapshot(list: List<T>): SnapshotStateList<T> {
    val snapshot = androidx.compose.runtime.mutableStateListOf<T>()
    snapshot.addAll(list)
    return snapshot
}