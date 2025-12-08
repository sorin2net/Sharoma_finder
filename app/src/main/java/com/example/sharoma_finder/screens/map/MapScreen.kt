package com.example.sharoma_finder.screens.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.sharoma_finder.R
import com.example.sharoma_finder.domain.StoreModel
import com.example.sharoma_finder.screens.results.ItemsNearest
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapScreen(
    store: StoreModel,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {}
) {
    // ✅ ADĂUGAT: Validare coordonate
    if (store.Latitude == 0.0 || store.Longitude == 0.0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(R.color.black2)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Location not available for this store\n\nPlease contact the store directly",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        }
        return
    }

    val latlng = LatLng(store.Latitude, store.Longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latlng, 15f)
    }
    val markerState = remember {
        MarkerState(position = latlng)
    }
    val context = LocalContext.current

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (map, detail) = createRefs()

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(map) {
                    centerTo(parent)
                },
            cameraPositionState = cameraPositionState
        ) {
            Marker(state = markerState, title = store.Title)
        }

        LazyColumn(
            modifier = Modifier
                .wrapContentHeight()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .fillMaxWidth()
                .background(colorResource(R.color.black3), shape = RoundedCornerShape(10.dp))
                .padding(16.dp)
                .constrainAs(detail) {
                    centerHorizontallyTo(parent)
                    bottom.linkTo(parent.bottom)
                }
        ) {
            item {
                ItemsNearest(
                    item = store,
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick
                )
            }

            item {
                Button(
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.gold)
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        val phoneNumber = "tel:" + store.Call
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
                        context.startActivity(dialIntent)
                    }
                ) {
                    Text(
                        "Call to Store",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}