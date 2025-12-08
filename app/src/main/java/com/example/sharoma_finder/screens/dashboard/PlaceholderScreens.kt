package com.example.sharoma_finder.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R

@Composable
fun SupportScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Support Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.gold))
    }
}

@Composable
fun WishlistScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Wishlist Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.gold))
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.gold))
    }
}