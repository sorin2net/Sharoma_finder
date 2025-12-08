package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel

@Composable
fun ItemsPopular(
    item:StoreModel,
    onClick:()->Unit
){
    Column(
        modifier=Modifier
            .padding(vertical=8.dp)
            .wrapContentSize()
            .background(colorResource(R.color.black3),
                shape= RoundedCornerShape(10.dp)
            )
            .padding(8.dp)
            .clickable{onClick()}
    ){
        AsyncImage(
            model=item.ImagePath,
            contentDescription=null,
            modifier=Modifier
                .size(135.dp,90.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(R.color.grey),
                    shape= RoundedCornerShape(10.dp)),
            contentScale=ContentScale.Crop
        )
        Text(
            text=item.Title,
            color= colorResource(R.color.white),
            modifier = Modifier.padding(top=8.dp),
            maxLines=1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize=14.sp
        )

        Row(
            Modifier.padding(top=8.dp)
        ){
            Image(painter= painterResource(R.drawable.location),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text=item.ShortAddress,
                color= colorResource(R.color.white),
                maxLines=1,
                fontWeight = FontWeight.SemiBold,
                modifier=Modifier.padding(start=8.dp),
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )

        }
    }
}


@Preview
@Composable
fun ItemsPopularView(){
    val item=StoreModel(
        Id=0,
        CategoryId = "",
        Title = "Store Title",
        Latitude = 0.0,
        Longitude = 0.0,
        Address = "123 Main St",
        ShortAddress="Main St",
        ImagePath = ""
    )
    ItemsPopular(item=item, onClick = {})
}

@Composable
fun PopularSection(
    list: SnapshotStateList<StoreModel>,
    showPopularLoading: Boolean,
    onStoreClick: (StoreModel) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Popular Stores",
                color = colorResource(R.color.gold),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "See all",
                color = Color.White,
                fontSize = 16.sp,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }
        if (showPopularLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                items(list.size) { index ->
                    val item = list[index]
                    ItemsPopular(item = item, onClick = { onStoreClick(item) })
                }
            }
        }
    }
}
@Preview
@Composable

fun PopularSectionPreview(){

    val list=SnapshotStateList<StoreModel>()
    list.add(StoreModel(Title="Store 1", ShortAddress = "Address 1"))
    list.add(StoreModel(Title="Store 2", ShortAddress = "Address 2"))
    PopularSection(
        list=list,
        showPopularLoading = false,
        onStoreClick = {} ,
        onSeeAllClick = {}
    )
}
