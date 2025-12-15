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

    var searchText by rememberSaveable { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // ✅ DEBUGGING: Logăm ce primim
    LaunchedEffect(allGlobalStores.size, id) {
        Log.d("ResultList", """
            📦 ResultList launched:
            - CategoryId: $id
            - Title: $title
            - Total stores available: ${allGlobalStores.size}
            - Stores in this category: ${allGlobalStores.count { it.CategoryId == id }}
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

    // 1. Calculăm lista COMPLETĂ Popular (pentru logică)
    val categoryPopularList = remember(allGlobalStores, id, selectedTag) {
        try {
            allGlobalStores.filter { store ->
                val matchesCategory = store.CategoryId == id && store.IsPopular && store.isValid()

                if (selectedTag.isEmpty()) {
                    matchesCategory
                } else {
                    matchesCategory && store.hasTag(selectedTag)
                }
            }
        } catch (e: Exception) {
            hasError = true
            errorMessage = "Error filtering popular stores: ${e.message}"
            emptyList()
        }
    }

    // 2. Calculăm lista COMPLETĂ Nearest (pentru logică și sortare)
    val categoryNearestList = remember(allGlobalStores, id, userLocation, selectedTag) {
        try {
            val filtered = allGlobalStores.filter { store ->
                val matchesCategory = store.CategoryId == id && store.isValid()

                if (selectedTag.isEmpty()) {
                    matchesCategory
                } else {
                    matchesCategory && store.hasTag(selectedTag)
                }
            }

            if (userLocation != null) {
                filtered.sortedBy {
                    if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
                }
            } else {
                filtered
            }
        } catch (e: Exception) {
            hasError = true
            errorMessage = "Error filtering nearest stores: ${e.message}"
            emptyList()
        }
    }

    // ✅ MODIFICARE: Creăm snapshot-uri LIMITATE la 6 elemente pentru afișare
    // Folosim .take(6) pentru a arăta doar primele 6, dar listele de mai sus rămân complete.
    val popularSnapshot = remember(categoryPopularList) {
        listToSnapshot(categoryPopularList.take(6))
    }

    val nearestSnapshot = remember(categoryNearestList) {
        listToSnapshot(categoryNearestList.take(6))
    }

    // ✅ DEBUGGING: Logăm diferența dintre total și afișat
    LaunchedEffect(popularSnapshot.size, nearestSnapshot.size) {
        Log.d("ResultList", """
            📊 Filtered results:
            - Popular Total: ${categoryPopularList.size} -> Displayed: ${popularSnapshot.size}
            - Nearest Total: ${categoryNearestList.size} -> Displayed: ${nearestSnapshot.size}
        """.trimIndent())
    }

    // ✅ Search (Rămâne neschimbat - arată tot ce găsește)
    val searchResults = remember(searchText, allGlobalStores) {
        if (searchText.isEmpty()) {
            emptyList()
        } else {
            try {
                allGlobalStores
                    .filter { store ->
                        store.isValid() && (
                                store.Title.contains(searchText, ignoreCase = true) ||
                                        store.Address.contains(searchText, ignoreCase = true) ||
                                        store.Tags.any { tag ->
                                            tag.contains(searchText, ignoreCase = true)
                                        }
                                )
                    }
                    .sortedBy {
                        if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser
                    }
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
            Search(
                text = searchText,
                onValueChange = { newText -> searchText = newText }
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
            // ===== SUBCATEGORIES (Burger, Pizza, Sushi) =====
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

            // ===== POPULAR SECTION (LIMITAT LA 6) =====
            item {
                if (popularSnapshot.isNotEmpty()) {
                    PopularSection(
                        list = popularSnapshot, // Conține max 6 iteme
                        showPopularLoading = false,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = {
                            Log.d("ResultList", "📤 See All clicked for POPULAR")
                            onSeeAllClick("popular") // MainActivity va încărca lista completă
                        },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                } else if (selectedTag.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No popular stores found with tag \"$selectedTag\"",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // ===== NEAREST SECTION (LIMITAT LA 6) =====
            item {
                if (nearestSnapshot.isNotEmpty()) {
                    NearestList(
                        list = nearestSnapshot, // Conține max 6 iteme
                        showNearestLoading = false,
                        onStoreClick = onStoreClick,
                        onSeeAllClick = {
                            Log.d("ResultList", "📤 See All clicked for NEAREST")
                            onSeeAllClick("nearest") // MainActivity va încărca lista completă
                        },
                        isStoreFavorite = isStoreFavorite,
                        onFavoriteToggle = onFavoriteToggle
                    )
                } else if (selectedTag.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No nearby stores found with tag \"$selectedTag\"",
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ✅ Helper function
fun <T> listToSnapshot(list: List<T>): SnapshotStateList<T> {
    val snapshot = androidx.compose.runtime.mutableStateListOf<T>()
    snapshot.addAll(list)
    return snapshot
}