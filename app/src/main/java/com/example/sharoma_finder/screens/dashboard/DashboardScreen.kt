package com.example.sharoma_finder.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.viewModel.DashboardViewModel



@Composable
fun DashboardScreen(onCategoryClick: (id: String, title: String) -> Unit) {
    val viewModel: DashboardViewModel = viewModel()

    var selectedTab by remember { mutableStateOf("Home") }

    val categoryList by viewModel.loadCategory().observeAsState(initial = emptyList())
    val bannerList by viewModel.loadBanner().observeAsState(initial = emptyList())

    val categories = remember { mutableStateListOf<CategoryModel>() }
    val banners = remember { mutableStateListOf<BannerModel>() }

    LaunchedEffect(categoryList) {
        categories.clear()
        categories.addAll(categoryList)
    }

    LaunchedEffect(bannerList) {
        banners.clear()
        banners.addAll(bannerList)
    }

    val showCategoryLoading = categories.isEmpty()
    val showBannerLoading = banners.isEmpty()

    Scaffold(
        containerColor = colorResource(R.color.black2),
        bottomBar = {

            BottomBar(
                selected = selectedTab,
                onItemClick = { newTab -> selectedTab = newTab }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {
            when (selectedTab) {
                "Home" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { TopBar() }
                        item { CategorySection(categories, showCategoryLoading, onCategoryClick) }
                        item { Banner(banners, showBannerLoading) }
                    }
                }
                "Support" -> SupportScreen()
                "Wishlist" -> WishlistScreen()
                "Profile" -> ProfileScreen()
            }
        }
    }
}