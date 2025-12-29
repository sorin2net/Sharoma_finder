package com.example.sharoma_finder.screens.results

import android.location.Location
import android.util.Log
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
import com.example.sharoma_finder.R
import com.example.sharoma_finder.data.AppDatabase
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.repository.ResultsRepository
import com.example.sharoma_finder.screens.common.ErrorScreen
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

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
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = ResultsRepository(database.subCategoryDao())

    // ✅ MODIFICARE: Două stări pentru căutare (Input instant vs Filtrare debounced)
    var searchTextInput by rememberSaveable { mutableStateOf("") }
    var searchText by rememberSaveable { mutableStateOf("") }

    var selectedTag by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ✅ LOGICĂ DE DEBOUNCING: Așteptăm 300ms după ultima tastare
    LaunchedEffect(searchTextInput) {
        if (searchTextInput.isEmpty()) {
            searchText = "" // Resetăm instant dacă șterge tot
        } else {
            delay(300) // Așteaptă 300ms
            searchText = searchTextInput
        }
    }

    // ✅ DEBUGGING: Logăm ce primim
    LaunchedEffect(allGlobalStores.size, id) {
        Log.d("ResultList", """
            📦 ResultList launched:
            - CategoryId: $id
            - Title: $title
            - Total stores available: ${allGlobalStores.size}
            - Stores in this category: ${allGlobalStores.count { it.CategoryIds.contains(id) }}
        """.trimIndent())
    }

    val subCategoryState by remember(id) {
        repository.loadSubCategory(id)
    }.observeAsState(Resource.Loading())

    val subCategoryList = when (subCategoryState) {
        is Resource.Success -> subCategoryState.data ?: emptyList()
        is Resource.Error -> {
            LaunchedEffect(Unit) {
                hasError = true
                errorMessage = subCategoryState.message ?: "Failed to load categories"
            }
            emptyList()
        }
        else -> emptyList()
    }

    val showSubCategoryLoading = subCategoryState is Resource.Loading
    val subCategorySnapshot = remember(subCategoryList) { listToSnapshot(subCategoryList) }

    // 1. Calculăm lista Popular (Folosește searchText debounced)
    val categoryPopularList = remember(allGlobalStores.size, id, selectedTag) {
        try {
            allGlobalStores.asSequence()
                .filter { store ->
                    store.CategoryIds.contains(id) &&
                            store.IsPopular &&
                            store.isValid() &&
                            (selectedTag.isEmpty() || store.hasTag(selectedTag))
                }
                .toList()
        } catch (e: Exception) {
            Log.e("ResultList", "Filter popular error: ${e.message}")
            hasError = true
            errorMessage = "Error filtering popular stores: ${e.message}"
            emptyList()
        }
    }

    // 2. Calculăm lista Nearest
    val categoryNearestList = remember(allGlobalStores.size, id, userLocation, selectedTag) {
        try {
            val filteredSequence = allGlobalStores.asSequence()
                .filter { store ->
                    store.CategoryIds.contains(id) &&
                            store.isValid() &&
                            (selectedTag.isEmpty() || store.hasTag(selectedTag))
                }

            if (userLocation != null) {
                filteredSequence.sortedBy {
                    if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
                }.toList()
            } else {
                filteredSequence.toList()
            }
        } catch (e: Exception) {
            Log.e("ResultList", "Filter nearest error: ${e.message}")
            hasError = true
            errorMessage = "Error filtering nearest stores: ${e.message}"
            emptyList()
        }
    }

    val popularSnapshot = remember(categoryPopularList) {
        listToSnapshot(categoryPopularList.take(6))
    }

    val nearestSnapshot = remember(categoryNearestList) {
        listToSnapshot(categoryNearestList.take(6))
    }

    // ✅ Search Result (Folosește searchText debounced pentru performanță)
    val searchResults = remember(searchText, allGlobalStores.size, id) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            try {
                allGlobalStores.asSequence()
                    .filter { store ->
                        val belongsToCategory = store.CategoryIds.contains(id)
                        val matchesTitle = store.Title.contains(searchText, ignoreCase = true)
                        belongsToCategory && store.isValid() && matchesTitle
                    }
                    .sortedBy {
                        if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
                    }
                    .toList()
            } catch (e: Exception) {
                hasError = true
                errorMessage = "Search error: ${e.message}"
                emptyList()
            }
        }
    }

    if (hasError) {
        ErrorScreen(
            message = errorMessage,
            onRetry = {
                hasError = false
                errorMessage = ""
            }
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.black2))
    ) {
        item { TopTile(title, onBackClick) }

        item {
            // ✅ MODIFICARE: Legăm Search-ul de searchTextInput pentru tastare instantanee
            Search(
                text = searchTextInput,
                onValueChange = { newText -> searchTextInput = newText }
            )
        }

        if (searchText.isNotEmpty()) {
            // ===== SEARCH RESULTS =====
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No stores found matching \"$searchText\"",
                            color = Color.Gray
                        )
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
            // ===== SUBCATEGORIES =====
            item {
                SubCategory(
                    subCategory = subCategorySnapshot,
                    showSubCategoryLoading = showSubCategoryLoading,
                    selectedCategoryName = selectedTag,
                    onCategoryClick = { clickedTag ->
                        selectedTag = if (selectedTag == clickedTag) "" else clickedTag
                    }
                )
            }

            // ===== POPULAR SECTION =====
            item {
                if (popularSnapshot.isNotEmpty()) {
                    PopularSection(
                        list = popularSnapshot,
                        showPopularLoading = false,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = { onSeeAllClick("popular") },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                } else if (selectedTag.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No popular stores with tag \"$selectedTag\"", color = Color.Gray)
                    }
                }
            }

            // ===== NEAREST SECTION =====
            item {
                if (nearestSnapshot.isNotEmpty()) {
                    NearestList(
                        list = nearestSnapshot,
                        showNearestLoading = false,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = { onSeeAllClick("nearest") },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                } else if (selectedTag.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No nearby stores with tag \"$selectedTag\"", color = Color.Gray)
                    }
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