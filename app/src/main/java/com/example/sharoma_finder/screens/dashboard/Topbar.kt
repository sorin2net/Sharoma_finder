package com.example.sharoma_finder.screens.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.constraintlayout.compose.ConstraintLayout
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import java.io.File

@Composable
fun TopBar(
    userName: String,
    userImagePath: String?
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (title1, title2, profile, box) = createRefs()

        // --- 1. LOGICA PENTRU POZA DE PROFIL ---
        if (userImagePath != null) {
            // Dacă utilizatorul a ales o poză, o afișăm pe aceea
            AsyncImage(
                model = File(userImagePath),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .size(60.dp) // Dimensiune fixă ca să arate bine
                    .clip(CircleShape) // O facem rotundă
                    .constrainAs(profile) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )
        } else {
            // Dacă nu, afișăm poza default din resurse
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = "Default Profile",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .size(60.dp) // Dimensiune fixă
                    .clip(CircleShape) // O facem rotundă
                    .constrainAs(profile) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )
        }

        // --- 2. NUMELE UTILIZATORULUI (DINAMIC) ---
        Text(
            text = "Neata, $userName", // Aici inserăm variabila
            fontSize = 20.sp,
            color = colorResource(R.color.gold),
            modifier = Modifier
                .constrainAs(title1) {
                    top.linkTo(profile.top)
                    start.linkTo(parent.start, margin = 16.dp)
                    bottom.linkTo(profile.bottom)
                    // Am scos end.linkTo(parent.end) ca să nu se suprapună cu poza dacă numele e lung
                    end.linkTo(profile.start, margin = 8.dp)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
        )

        // --- 3. TEXTUL SECUNDAR ---
        Text(
            text = "Ce mananci azi bun?",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.gold),
            modifier = Modifier
                .padding(top = 24.dp)
                .constrainAs(title2) {
                    top.linkTo(profile.bottom)
                    start.linkTo(parent.start, margin = 16.dp) // Am adăugat margin explicit
                    end.linkTo(parent.end)
                    width = androidx.constraintlayout.compose.Dimension.fillToConstraints
                }
        )

        // --- 4. SECȚIUNEA PORTOFEL (CODUL TĂU VECHI INTEGRAT) ---
        ConstraintLayout(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    color = colorResource(R.color.black3),
                    shape = RoundedCornerShape(10.dp)
                )
                .constrainAs(box) {
                    bottom.linkTo(parent.bottom)
                    top.linkTo(title2.bottom)
                }
                .clip(RoundedCornerShape(10.dp))
        ) {
            val (icon1, icon2, balance, amount, reward, wallet, line1, line2) = createRefs()

            Image(
                painter = painterResource(R.drawable.wallet),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp)
                    .constrainAs(icon1) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
            )
            Image(
                painter = painterResource(R.drawable.medal),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 16.dp)
                    .constrainAs(icon2) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
            )
            Text(
                text = "Portofel",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .constrainAs(wallet) {
                        bottom.linkTo(icon1.bottom)
                        start.linkTo(icon1.end)
                    }
            )
            Text(
                text = "Reward",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .constrainAs(reward) {
                        top.linkTo(icon2.top)
                        start.linkTo(icon2.end)
                    }
            )
            Box(modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
                .background(colorResource(R.color.grey))
                .constrainAs(line1) {
                    centerTo(parent)
                }
            )
            Box(Modifier
                .height(1.dp)
                .width(170.dp)
                .padding(horizontal = 16.dp)
                .background(colorResource(R.color.grey))
                .constrainAs(line2) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                }
            )
            Text(
                text = "Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 32.dp)
                    .constrainAs(balance) {
                        top.linkTo(parent.top)
                        start.linkTo(line1.end)
                    }
            )
            Text(
                text = "150.00 RON",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp)
                    .constrainAs(amount) {
                        top.linkTo(balance.bottom)
                        start.linkTo(balance.start)
                    }
            )
        }
    }
}

// Preview separat pentru a putea vedea design-ul în Android Studio
@Preview
@Composable
fun TopBarPreview() {
    TopBar(userName = "Costi", userImagePath = null)
}