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
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
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
) {
    val viewModel: ResultsViewModel = viewModel()

    val subCategoryState by remember(id) { viewModel.loadSubCategory(id) }.observeAsState(Resource.Loading())
    val popularState by remember(id) { viewModel.loadPopular(id) }.observeAsState(Resource.Loading())
    val nearestState by remember(id) { viewModel.loadNearest(id) }.observeAsState(Resource.Loading())

    val subCategoryList = subCategoryState.data ?: emptyList()
    val popularList = popularState.data ?: emptyList()
    val nearestList = nearestState.data ?: emptyList()

    val showSubCategoryLoading = subCategoryState is Resource.Loading
    val showPopularLoading = popularState is Resource.Loading
    val showNearestLoading = nearestState is Resource.Loading

    val subCategorySnapshot = remember(subCategoryList) { listToSnapshot(subCategoryList) }
    val popularSnapshot = remember(popularList) { listToSnapshot(popularList) }
    val nearestSnapshot = remember(nearestList) { listToSnapshot(nearestList) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.black2))
    ) {
        item { TopTile(title, onBackClick) }
        item { Search() }

        item {

            if (!showSubCategoryLoading && subCategoryList.isEmpty()) {

            }
            SubCategory(subCategorySnapshot, showSubCategoryLoading)
        }

        item {
            if (!showPopularLoading && popularList.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Text("No popular stores found", color = Color.Gray)
                }
            } else {
                PopularSection(popularSnapshot, showPopularLoading, onStoreClick)
            }
        }

        item {
            if (!showNearestLoading && nearestList.isEmpty()) {

            } else {
                NearestList(nearestSnapshot, showNearestLoading, onStoreClick)
            }
        }
    }
}

fun <T> listToSnapshot(list: List<T>): SnapshotStateList<T> {
    val snapshot = mutableStateListOf<T>()
    snapshot.addAll(list)
    return snapshot
}