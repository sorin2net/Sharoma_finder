package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.CategoryModel

@Composable
fun Category(
    item: CategoryModel,
    isSelected: Boolean,
    onItemClick: () -> Unit
) {

    val backgroundColor = if (isSelected) colorResource(R.color.gold) else colorResource(R.color.black3)
    val textColor = if (isSelected) Color.Black else Color.White

    Column(
        modifier = Modifier
            .size(85.dp, 95.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onItemClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = item.ImagePath,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 16.dp)
                .size(45.dp, 40.dp)
        )
        Spacer(Modifier.padding(top = 12.dp))
        Text(
            text = item.Name,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SubCategory(
    subCategory: SnapshotStateList<CategoryModel>,
    showSubCategoryLoading: Boolean,
    selectedCategoryName: String = "",
    onCategoryClick: (String) -> Unit = {}
) {
    if (showSubCategoryLoading) {
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
            items(subCategory.size) { index ->
                val item = subCategory[index]
                Category(
                    item = item,
                    isSelected = item.Name == selectedCategoryName,
                    onItemClick = { onCategoryClick(item.Name) }
                )
            }
        }
    }
}

@Preview
@Composable
fun CategoryPreview() {
    val item = CategoryModel(Id = 0, ImagePath = "", Name = "Burger")
    Category(item = item, isSelected = true, onItemClick = {})
}