package com.example.sharoma_finder.screens.dashboard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.BannerModel
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.repository.DashboardRepository

@Composable
fun DashboardScreen(onCategoryClick:(id:String,title:String)->Unit )
{
    val viewModel= DashboardRepository()

    val categories=remember{mutableStateListOf<CategoryModel>()}

    val banners= remember { mutableStateListOf<BannerModel>() }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showBannerLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.loadCategory().observeForever{
            categories.clear()
            categories.addAll(it)
            showCategoryLoading=false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadBanner().observeForever{
            banners.clear()
            banners.addAll(it)
            showBannerLoading=false
        }
    }

    Scaffold(
        containerColor = colorResource(R.color.black2),
        bottomBar = { BottomBar() }
    ) { paddingValues ->
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues=paddingValues)
        ) {
            item{ TopBar() }
            item { CategorySection (categories,showCategoryLoading, onCategoryClick  ) }
            item { Banner(banners,showBannerLoading) }

        }

        }
    }
