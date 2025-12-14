package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun StoreDetail(item: StoreModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item.Title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.location),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = item.Address,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 1,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // ✅ MODIFICARE: Afișare inteligentă distanță (metri/km)
        if (item.distanceToUser > 0) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val distanceText = if (item.distanceToUser < 1000) {
                    // Sub 1 km → afișează în metri
                    "${item.distanceToUser.toInt()} m away"
                } else {
                    // Peste 1 km → afișează în kilometri cu 1 zecimală
                    String.format("%.1f km away", item.distanceToUser / 1000)
                }

                Text(
                    text = distanceText,
                    color = colorResource(R.color.gold),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            text = item.Activity,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = "Hours: ${item.Hours}",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StoreImage(item: StoreModel) {
    AsyncImage(
        model = item.ImagePath,
        contentDescription = "Image of ${item.Title}",
        modifier = Modifier
            .size(95.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colorResource(R.color.grey), shape = RoundedCornerShape(10.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun ItemsNearest(
    item: StoreModel,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.black3), shape = RoundedCornerShape(10.dp))
            .wrapContentHeight()
            .padding(8.dp)
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        StoreImage(item = item)

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            StoreDetail(item = item)
        }

        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isFavorite) "Remove ${item.Title} from favorites" else "Add ${item.Title} to favorites",
                tint = colorResource(R.color.gold),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NearestList(
    list: SnapshotStateList<StoreModel>,
    showNearestLoading: Boolean,
    onStoreClick: (StoreModel) -> Unit,
    onSeeAllClick: () -> Unit,
    isStoreFavorite: (StoreModel) -> Boolean,
    onFavoriteToggle: (StoreModel) -> Unit
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
                    val item = list[index]
                    ItemsNearest(
                        item = item,
                        isFavorite = isStoreFavorite(item),
                        onFavoriteClick = { onFavoriteToggle(item) },
                        onClick = { onStoreClick(item) }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun NearestListPreview() {
    val list = remember {
        androidx.compose.runtime.mutableStateListOf(
            StoreModel(
                Title = "Store 1",
                Address = "123 Main St",
                ShortAddress = "Main St",
                Activity = "Retail",
                Hours = "9am"
            ),
            StoreModel(
                Title = "Store 2",
                Address = "456 Oak St",
                ShortAddress = "Oak St",
                Activity = "Cafe",
                Hours = "7am"
            )
        )
    }
    NearestList(
        list = list,
        showNearestLoading = false,
        onStoreClick = {},
        onSeeAllClick = {},
        isStoreFavorite = { false },
        onFavoriteToggle = {}
    )
}

@Preview
@Composable
fun ItemsNearestPreview() {
    val item = StoreModel(
        Title = "Store Title",
        Address = "123 Main St",
        ShortAddress = "Main St",
        Activity = "test",
        Hours = "9am"
    )
    ItemsNearest(item = item, isFavorite = false, onFavoriteClick = {}, onClick = {})
}

@Preview
@Composable
fun StoreDetailPreview() {
    val item = StoreModel(
        Title = "Store Title",
        Address = "123 Main St",
        ShortAddress = "Main St",
        Activity = "test",
        Hours = "9am"
    )
    StoreDetail(item)
}