package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R

@Preview
@Composable


fun Search(){
    var text by rememberSaveable { mutableStateOf("") }
    TextField(
        value=text,
        onValueChange = {text=it},
        label= {
            Text(text="Find stores, restaurants, products...",
            fontSize=16.sp,
                color= Color.White
            )
        },shape= RoundedCornerShape(10.dp),
        leadingIcon = {
            Image(painterResource(R.drawable.search_icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
                )
        },
        colors= TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = colorResource(R.color.black3),
            focusedBorderColor=Color.Transparent,
            unfocusedLabelColor = Color.Transparent,
            textColor = Color.White,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(colorResource(R.color.white), CircleShape)


    )
}