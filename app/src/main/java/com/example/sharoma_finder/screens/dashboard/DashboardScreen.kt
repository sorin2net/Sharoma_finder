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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.utils.LockScreenOrientation
import com.example.sharoma_finder.viewModel.DashboardViewModel

@Composable
fun DashboardScreen(
    onCategoryClick: (id: String, title: String) -> Unit,
    onStoreClick: (StoreModel) -> Unit,
    onBannerClick: () -> Unit,
    viewModel: DashboardViewModel
) {
    LockScreenOrientation()

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

    val onStoreClickWithLog: (StoreModel) -> Unit = { store ->
        viewModel.logViewStore(store)
        onStoreClick(store)
    }

    Scaffold(
        containerColor = colorResource(R.color.black2),
        bottomBar = {
            BottomBar(

                selected = viewModel.selectedTab.value,

                onItemClick = { newTab -> viewModel.updateTab(newTab) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
        ) {

            when (viewModel.selectedTab.value) {
                "Acasă" -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            TopBar(
                                userName = viewModel.userName.value,
                                userImagePath = viewModel.userImagePath.value,
                                wishlistCount = viewModel.favoriteStores.size,
                                points = viewModel.userPoints.value,
                                viewModel = viewModel
                            )
                        }
                        item { CategorySection(categories, showCategoryLoading, onCategoryClick) }
                        item { Banner(banners, showBannerLoading,onBannerClick = onBannerClick) }
                    }
                }
                "Suport" -> SupportScreen()
                "Favorite" -> {
                    WishlistScreen(
                        favoriteStores = viewModel.favoriteStores,
                        isDataLoaded = viewModel.isDataLoaded.value,
                        onFavoriteToggle = { store -> viewModel.toggleFavorite(store) },
                        onStoreClick = onStoreClickWithLog,
                        isStoreFavorite = { store -> viewModel.isFavorite(store) }
                    )
                }
                "Profil" -> {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}