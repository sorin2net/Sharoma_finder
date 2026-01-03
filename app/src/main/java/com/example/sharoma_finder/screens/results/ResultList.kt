package com.example.sharoma_finder.screens.results

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

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
    userLocation: Location? = null,
    lastUpdateTick: Long = 0L
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = ResultsRepository(database.subCategoryDao())

    var searchTextInput by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var isProcessingData by remember { mutableStateOf(true) }
    val popularStores = remember { mutableStateListOf<StoreModel>() }
    val nearestStores = remember { mutableStateListOf<StoreModel>() }
    val searchResults = remember { mutableStateListOf<StoreModel>() }

    LaunchedEffect(searchTextInput) {
        delay(300)
        searchText = searchTextInput
    }

    val subCategoryState by remember(id) {
        repository.loadSubCategory(id)
    }.observeAsState(Resource.Loading())

    val subCategoryList = when (subCategoryState) {
        is Resource.Success -> subCategoryState.data ?: emptyList()
        is Resource.Error -> {
            errorMessage = subCategoryState.message ?: "Eroare la categorii"
            hasError = true
            emptyList()
        }
        else -> emptyList()
    }
    val subCategorySnapshot = remember(subCategoryList) { listToSnapshot(subCategoryList) }

    LaunchedEffect(id, selectedTag, userLocation, searchText, allGlobalStores.size,lastUpdateTick) {
        isProcessingData = true

        withContext(Dispatchers.Default) {
            try {
                val filteredByCategory = allGlobalStores.filter {
                    it.CategoryIds.contains(id) && it.isValid() &&
                            (selectedTag.isEmpty() || it.hasTag(selectedTag))
                }

                val popular = filteredByCategory
                    .filter { it.IsPopular }
                    .sortedBy { if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser }
                    .take(6)

                val nearest = filteredByCategory
                    .sortedBy { if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser }
                    .take(6)

                val searchRes = if (searchText.isNotEmpty()) {
                    filteredByCategory.filter {
                        it.Title.contains(searchText, ignoreCase = true)
                    }.sortedBy { if (it.distanceToUser < 0) Float.MAX_VALUE else it.distanceToUser }
                } else emptyList()

                withContext(Dispatchers.Main) {
                    popularStores.clear()
                    popularStores.addAll(popular)

                    nearestStores.clear()
                    nearestStores.addAll(nearest)

                    searchResults.clear()
                    searchResults.addAll(searchRes)

                    isProcessingData = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hasError = true
                    errorMessage = "Eroare la procesarea datelor"
                    isProcessingData = false
                }
            }
        }
    }

    if (hasError) {
        ErrorScreen(message = errorMessage, onRetry = { hasError = false })
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
                text = searchTextInput,
                onValueChange = { searchTextInput = it }
            )
        }

        if (searchText.isNotEmpty()) {
            item {
                Text(
                    text = "Rezultatele căutării (${searchResults.size})",
                    color = colorResource(R.color.gold),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (isProcessingData) {
                item { LoadingIndicator() }
            } else if (searchResults.isEmpty()) {

                item {
                    EmptyStateMessage("Nu am găsit niciun local care să conțină \"$searchText\".")
                }
            } else {
                items(
                    items = searchResults.chunked(2),
                    key = { row -> row.joinToString("-") { it.getUniqueId() } }
                ) { rowItems ->
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                }
            }
        }
        else {
            item {
                SubCategory(
                    subCategory = subCategorySnapshot,
                    showSubCategoryLoading = subCategoryState is Resource.Loading,
                    selectedCategoryName = selectedTag,
                    onCategoryClick = { selectedTag = if (selectedTag == it) "" else it }
                )
            }

            if (isProcessingData) {
                item { LoadingIndicator() }
            } else {
                if (popularStores.isEmpty() && nearestStores.isEmpty()) {
                    item {
                        EmptyStateMessage(
                            "Momentan nu am găsit locații în categoria $title${if(selectedTag.isNotEmpty()) " pentru $selectedTag" else ""}."
                        )
                    }
                } else {
                    if (popularStores.isNotEmpty()) {
                        item {
                            PopularSection(
                                list = popularStores,
                                showPopularLoading = false,
                                categoryName = title,
                                onStoreClick = onStoreClick,
                                onSeeAllClick = { onSeeAllClick("popular") },
                                isStoreFavorite = isStoreFavorite,
                                onFavoriteToggle = onFavoriteToggle
                            )
                        }
                    }

                    if (nearestStores.isNotEmpty()) {
                        item {
                            NearestList(
                                list = nearestStores,
                                showNearestLoading = false,
                                categoryName = title,
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
    }
}

@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(
                        color = colorResource(R.color.black3),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(95.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 80.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.SearchOff,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            color = Color.Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

fun <T> listToSnapshot(list: List<T>): SnapshotStateList<T> {
    val snapshot = mutableStateListOf<T>()
    snapshot.addAll(list)
    return snapshot
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    val shimmerColors = listOf(
        Color.Gray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.2f),
        Color.Gray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )
    this.background(brush)
}