package com.example.sharoma_finder.screens.results

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R

@Composable
fun Search(
    text: String,
    onValueChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = text,
        onValueChange = { newText ->
            // ✅ Filtrăm caracterele newline/enter
            val cleanText = newText.replace("\n", "").replace("\r", "")
            onValueChange(cleanText)
        },
        label = {
            Text(
                text = "Find restaurants...",
                fontSize = 16.sp,
                color = Color.White
            )
        },
        // ✅ ADĂUGAT: Configurăm tastatura să aibă buton de Search
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        // ✅ ADĂUGAT: Ascundem tastatura când user-ul apasă Search/Enter
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
            }
        ),
        // ✅ IMPORTANT: maxLines = 1 previne enter-ul să creeze linii noi
        maxLines = 1,
        singleLine = true,
        shape = RoundedCornerShape(10.dp),
        leadingIcon = {
            Image(
                painterResource(R.drawable.search_icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            backgroundColor = colorResource(R.color.black3),
            focusedBorderColor = Color.Transparent,
            unfocusedLabelColor = Color.Transparent,
            textColor = Color.White,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = colorResource(R.color.gold)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp)
            .background(colorResource(R.color.white), CircleShape)
    )
}