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
import androidx.compose.runtime.remember
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
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.example.sharoma_finder.R
import java.io.File
import java.util.Calendar // ✅ Import necesar pentru oră

@Composable
fun TopBar(
    userName: String,
    userImagePath: String?,
    wishlistCount: Int
) {
    // 1. LOGICA PENTRU TITLUL DE REWARD (bazat pe wishlist)
    // 1. LOGICA PENTRU TITLUL DE REWARD (bazat pe wishlist)
    val userRankTitle = when (wishlistCount) {
        0 -> "La Dietă"          // 0 iteme (Ironic: nu a ales nimic încă)
        in 1..2 -> "Ciugulitor"  // 1-2 iteme (Doar gustă puțin)
        in 3..4 -> "Pofticios"   // 3-4 iteme (Începe să îi fie foame)
        in 5..6 -> "Mâncăcios"   // 5-6 iteme (Îi place mâncarea)
        in 7..8 -> "Gurmand"     // 7-8 iteme (Apreciază gustul bun)
        in 9..10 -> "Devorator"  // 9-10 iteme (Nu iartă nimic)
        else -> "Sultan"         // 11+ (Nivel suprem, ospăț regal)
    }

    // 2. LOGICA PENTRU SALUT (bazat pe oră)
    // Folosim 'remember' pentru a nu recalcul ora la fiecare mică redesenare, deși nu e critic aici.
    val greetingText = remember {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY) // Ora în format 0-23

        when (hour) {
            in 6..10 -> "Neata"      // 06:00 - 10:59
            in 11..17 -> "Buna ziua" // 11:00 - 17:59 (5 PM)
            else -> "Buna seara"     // 18:00 - 05:59
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        val (title1, title2, profile, box) = createRefs()

        // --- POZA DE PROFIL ---
        if (userImagePath != null) {
            AsyncImage(
                model = File(userImagePath),
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .constrainAs(profile) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )
        } else {
            Image(
                painter = painterResource(R.drawable.profile),
                contentDescription = "Default profile picture",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .size(60.dp)
                    .clip(CircleShape)
                    .constrainAs(profile) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )
        }

        // --- SALUTUL DINAMIC ---
        Text(
            text = "$greetingText, $userName", // ✅ Aici am pus variabila calculată
            fontSize = 20.sp,
            color = colorResource(R.color.gold),
            modifier = Modifier
                .constrainAs(title1) {
                    top.linkTo(profile.top)
                    start.linkTo(parent.start, margin = 16.dp)
                    bottom.linkTo(profile.bottom)
                    end.linkTo(profile.start, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
        )

        // --- TEXTUL SECUNDAR ---
        Text(
            text = "Ce mananci azi bun?",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.gold),
            modifier = Modifier
                .padding(top = 24.dp)
                .constrainAs(title2) {
                    top.linkTo(profile.bottom)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )

        // --- SECȚIUNEA STATISTICI ---
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
            val (icon1, icon2, wishTitle, wishCount, reward, wallet, line1, line2) = createRefs()

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

            // --- REWARD DINAMIC ---
            Text(
                text = userRankTitle, // ✅ "Appetizer", "Gourmand", etc.
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorResource(R.color.gold),
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

            // --- WISHLIST TITLE ---
            Text(
                text = "My Wishlist",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 32.dp)
                    .constrainAs(wishTitle) {
                        top.linkTo(parent.top)
                        start.linkTo(line1.end)
                    }
            )

            // --- WISHLIST COUNT ---
            Text(
                text = "$wishlistCount ${if (wishlistCount == 1) "Item" else "Items"}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp)
                    .constrainAs(wishCount) {
                        top.linkTo(wishTitle.bottom)
                        start.linkTo(wishTitle.start)
                    }
            )
        }
    }
}