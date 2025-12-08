package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.util.Locale

@Composable
fun ItemsPopular(
    item: StoreModel,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .wrapContentSize()
            .background(
                colorResource(R.color.black3),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(8.dp)
            .clickable { onClick() }
    ) {

        Box(modifier = Modifier.size(135.dp, 90.dp)) {
            AsyncImage(
                model = item.ImagePath,
                // --- ACCESIBILITATE: Descriere imagine ---
                contentDescription = "Photo of ${item.Title}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        colorResource(R.color.grey),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(
                        colorResource(R.color.black3).copy(alpha = 0.6f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    // --- ACCESIBILITATE: Descriere buton dinamică ---
                    contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = colorResource(R.color.gold),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Text(
            text = item.Title,
            color = colorResource(R.color.white),
            modifier = Modifier.padding(top = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.location),
                // Iconița este decorativă, textul de lângă explică adresa, deci lăsăm null
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = item.ShortAddress,
                color = colorResource(R.color.white),
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }

        // Afișăm distanța sub adresă dacă este calculată
        if (item.distanceToUser > 0) {
            Text(
                text = String.format(Locale.US, "%.2f km", item.distanceToUser / 1000),
                color = colorResource(R.color.gold),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PopularSection(
    list: SnapshotStateList<StoreModel>,
    showPopularLoading: Boolean,
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
                    ItemsPopular(
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
fun ItemsPopularView() {
    val item = StoreModel(
        Id = 0,
        Title = "Store Title",
        Address = "123 Main St",
        ShortAddress = "Main St",
        ImagePath = ""
    )
    ItemsPopular(item = item, isFavorite = true, onFavoriteClick = {}, onClick = {})
}

@Preview
@Composable
fun PopularSectionPreview() {
    val list = SnapshotStateList<StoreModel>()
    list.add(StoreModel(Title = "Store 1", ShortAddress = "Address 1"))
    list.add(StoreModel(Title = "Store 2", ShortAddress = "Address 2"))
    PopularSection(
        list = list,
        showPopularLoading = false,
        onStoreClick = {},
        onSeeAllClick = {},
        isStoreFavorite = { false },
        onFavoriteToggle = {}
    )
}