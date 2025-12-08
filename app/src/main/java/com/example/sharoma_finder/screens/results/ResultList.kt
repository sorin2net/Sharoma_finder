package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
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
    onSeeAllClick: (String) -> Unit
) {
    val viewModel: ResultsViewModel = viewModel()

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

    val filteredPopularList = remember(popularList, selectedCategoryName) {
        if (selectedCategoryName.isEmpty()) popularList
        else popularList.filter { it.Activity.equals(selectedCategoryName, ignoreCase = true) }
    }

    val filteredNearestList = remember(nearestList, selectedCategoryName) {
        if (selectedCategoryName.isEmpty()) nearestList
        else nearestList.filter { it.Activity.equals(selectedCategoryName, ignoreCase = true) }
    }


    val subCategorySnapshot = remember(subCategoryList) { listToSnapshot(subCategoryList) }
    val popularSnapshot = remember(filteredPopularList) { listToSnapshot(filteredPopularList) }
    val nearestSnapshot = remember(filteredNearestList) { listToSnapshot(filteredNearestList) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.black2))
    ) {
        item { TopTile(title, onBackClick) }
        item { Search() }

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

        item {
            if (!showPopularLoading && filteredPopularList.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text("No stores found for $selectedCategoryName", color = Color.Gray)
                }
            } else {
                PopularSection(
                    list = popularSnapshot,
                    showPopularLoading = showPopularLoading,
                    onStoreClick = onStoreClick,
                    onSeeAllClick = { onSeeAllClick("popular") }
                )
            }
        }

        item {
            if (!showNearestLoading && filteredNearestList.isEmpty()) {

            } else {
                NearestList(
                    list = nearestSnapshot,
                    showNearestLoading = showNearestLoading,
                    onStoreClick = onStoreClick,
                    onSeeAllClick = { onSeeAllClick("nearest") }
                )
            }
        }
    }
}

fun <T> listToSnapshot(list: List<T>): SnapshotStateList<T> {
    val snapshot = mutableStateListOf<T>()
    snapshot.addAll(list)
    return snapshot
}