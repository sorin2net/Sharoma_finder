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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel

@Composable
fun StoreDetail(item: StoreModel){
    Column(modifier=Modifier
        .fillMaxWidth()
        .padding(start=8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text=item.Title,
            color= Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines=1
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter= painterResource(id= R.drawable.location),
                contentDescription = null)
            Text(
                text=item.Address,
                color=Color.White,
                fontSize=12.sp,
                maxLines=1,
                modifier = Modifier.padding(start=4.dp)
            )
        }
        Text(text=item.Activity,
            color=Color.White,
            fontSize=14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines=1)
        Text(text="Hours: ${item.Hours}",
            color=Color.White,
            fontSize=14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines=1)

    }
}

@Composable
fun StoreImage(item:StoreModel){
    AsyncImage(
        model=item.ImagePath,
        contentDescription = null,
        modifier = Modifier
            .size(95.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.grey),shape=RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ItemsNearest(
    item:StoreModel,
    onClick:(()->Unit)?=null
){
    Row (modifier = Modifier
        .fillMaxWidth()
        .background(colorResource(R.color.black3),shape=RoundedCornerShape(10.dp))
        .wrapContentHeight()
        .padding(8.dp)
        .clickable(enabled = onClick!=null){
            onClick?.invoke()
        }
    ) {
        StoreImage(item=item)
        StoreDetail(item=item)
    }
}

@Composable
fun NearestList(
    list: SnapshotStateList<StoreModel>,
    showNearestLoading: Boolean,
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
                text = "Nearest Stores",
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
        if (showNearestLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .height(400.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
            ) {
                items(list.size) { index ->
                    ItemsNearest(item = list[index], onClick = {
                        onStoreClick(list[index])
                    })
                }
            }
        }
    }
}

@Preview
@Composable
fun NearestListPreview(){
    val list= remember {
        androidx.compose.runtime.mutableStateListOf<StoreModel>(
            StoreModel(
                Title="Store 1",
                Address="123 Main St",
                ShortAddress = "Main St",
                Activity = "Retail",
                Hours = "9am - 5pm"
            ),
            StoreModel(
                Title="Store 2",
                Address="456 Oak St",
                ShortAddress = "Oak St",
                Activity = "Cafe",
                Hours = "7am - 3pm"
            )
        )
    }


    NearestList(list=list, showNearestLoading = false, onStoreClick = {}, onSeeAllClick = {})
}


@Preview
@Composable
fun ItemsNearestPreview()
{
    val item=StoreModel(
        Title="Store Title",
        Address="123 Main St",
        ShortAddress = "Main St",
        Activity = "test",
        Hours = "9am - 5pm"
    )
    ItemsNearest(item=item, onClick = {})
}


@Preview
@Composable
fun StoreDetailPreview(){
    val item=StoreModel(
        Title="Store Title",
        Address="123 Main St",
        ShortAddress = "Main St",
        Activity="test",
        Hours="9am - 5pm"
    )
    StoreDetail(item)
}