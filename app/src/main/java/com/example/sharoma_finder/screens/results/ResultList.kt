package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.CategoryModel
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.viewModel.ResultsViewModel

@Composable

fun ResultList(
    id:String,
    title:String,
    onBackClick:()->Unit,
    onStoreClick:(StoreModel)->Unit,
){
    val viewModel=ResultsViewModel()

    val subCategory=remember{ mutableStateListOf<CategoryModel>() }
    val popular=remember{ mutableStateListOf<StoreModel>() }
    val nearest= remember { mutableStateListOf<StoreModel>() }

    var showSubCategoryLoading by remember { mutableStateOf(true)}
    var showPopularLoading by remember { mutableStateOf(true) }
    var showNearestLoading by remember { mutableStateOf(true) }

    LaunchedEffect(id) {
        viewModel.loadSubCategory(id).observeForever{
            subCategory.clear()
            subCategory.addAll(it)
            showSubCategoryLoading=false

        }
    }
    LaunchedEffect(id) {
        viewModel.loadPopular(id).observeForever{
            popular.clear()
            popular.addAll(it)
            showPopularLoading=false
        }
    }

    LaunchedEffect(id) {
        viewModel.loadNearest(id).observeForever{
            nearest.clear()
            nearest.addAll(it)
            showNearestLoading=false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color= colorResource(R.color.black2))
    ) {
        item { TopTile(title,onBackClick) }
        item{ Search() }
        item { SubCategory(subCategory,showSubCategoryLoading) }
        item { PopularSection(popular,showPopularLoading, onStoreClick )  }
        item { NearestList(nearest,showNearestLoading,onStoreClick) }

    }
}

@Preview
@Composable
fun ResultListPreview(){
    ResultList(
        id="1",
        title="Sample Title",
        onBackClick = {},
        onStoreClick = {}
    )

}