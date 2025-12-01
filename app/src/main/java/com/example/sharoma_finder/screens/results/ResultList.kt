package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.sharoma_finder.R

@Composable

fun ResultList(
    id:String,
    title:String,
    onBackClick:()->Unit,
    onStoreClick:(String)->Unit,
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color= colorResource(R.color.black2))
    ) {
        item { TopTile(title,onBackClick) }
        item{ Search() }
    }
}

@Preview
@Composable
fun ResultListPreeview(){
    ResultList(
        id="1",
        title="Sample Title",
        onBackClick = {},
        onStoreClick = {}
    )

}