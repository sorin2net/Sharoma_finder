package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.repository.Resource
import com.example.sharoma_finder.viewModel.ResultsViewModel

@Composable
fun AllStoresScreen(
    categoryId: String,
    mode: String,
    onBackClick: () -> Unit,
    onStoreClick: (StoreModel) -> Unit
) {
    val viewModel: ResultsViewModel = viewModel()

    val dataState = if (mode == "popular") {
        remember(categoryId) { viewModel.loadPopular(categoryId) }
    } else {
        remember(categoryId) { viewModel.loadNearest(categoryId) }
    }

    val resource by dataState.observeAsState(Resource.Loading())
    val list = resource.data ?: emptyList()
    val isLoading = resource is Resource.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
    ) {

        androidx.compose.foundation.layout.Column {
            TopTile(
                title = if (mode == "popular") "Popular" else "Nearest",
                onBackClick = onBackClick
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list.size) { index ->

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ItemsPopular(item = list[index], onClick = { onStoreClick(list[index]) })
                        }
                    }
                }
            }
        }
    }
}