package com.example.sharoma_finder.screens.dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sharoma_finder.R

data class FAQItem(
    val question: String,
    val answer: String
)

data class ContactOption(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val action: () -> Unit
)

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun SupportScreen() {
    val context = LocalContext.current

    val faqList = remember {
        listOf(
            FAQItem(
                "Nu mă pot decide ce să mănânc. Mă poate ajuta aplicația?",
                "Da! Folosește funcția de alegere aleatorie (Random Picker). Aceasta va selecta automat un restaurant din lista disponibilă pentru a te ajuta să iei o decizie rapidă când ești nehotărât."
            ),
            FAQItem(
                "Cum funcționează sistemul de puncte XP?",
                "Primești puncte XP prin interacțiunea cu aplicația, cum ar fi salvarea restaurantelor la favorite. Pe măsură ce aduni puncte, rangul tău crește de la 'La Dietă' până la cel de 'Sultan'."
            ),
            FAQItem(
                "De ce îmi apare mesajul 'Nicio conexiune' în Profil?",
                "Acest mesaj apare când dispozitivul tău nu are acces la internet. În acest caz, aplicația trece automat în Modul Offline, permițându-ți să vezi doar datele deja salvate în memoria cache."
            ),
            FAQItem(
                "Pot vedea traseul până la o shaormerie?",
                "Da, în ecranul de detalii al fiecărui magazin poți deschide harta. Aceasta îți va arăta locația exactă și distanța calculată în timp real prin GPS față de poziția ta actuală."
            ),
            FAQItem(
                "Ce fac dacă butonul de activare GPS nu funcționează?",
                "Dacă ai refuzat permisiunea de mai multe ori, Android poate bloca fereastra pop-up. Folosește link-ul 'Deschide Setările' din ecranul Profil pentru a activa manual locația din setările telefonului."
            ),
            FAQItem(
                "Sunt datele mele în siguranță?",
                "Aplicația colectează doar date minime (cum ar fi locația aproximativă) pentru a funcționa corect. Poți consulta oricând Politica de Confidențialitate direct din acest ecran pentru detalii complete."
            ),
            FAQItem(
                "Cum pot vedea doar shaormeriile care au și gyros?",
                "Poți folosi sistemul de categorii și subcategorii pentru a filtra rezultatele. Alege categoria dorită pentru a vedea doar localurile care servesc produsele respective."
            )
        )
    }

    val contactOptions = remember {
        listOf(
            ContactOption(
                title = "Contact Email",
                subtitle = "dumitriudenisgabriel@gmail.com",
                icon = Icons.Default.Email,
                action = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:dumitriudenisgabriel@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Solicitare Suport Shaorma Finder")
                    }
                    context.startActivity(Intent.createChooser(intent, "Trimite Email"))
                }
            ),
            /*
            ContactOption(
                title = "Call Us",
                subtitle = "+40 123 456 789",
                icon = Icons.Default.Phone,
                action = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+40123456789"))
                    context.startActivity(intent)
                }
            ),
            */
            ContactOption(
                title = "Messenger",
                subtitle = "Mesaj Direct",
                icon = Icons.Default.Chat,
                action = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://m.me/denis.dumitriu.1")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/denis.dumitriu.1"))
                        context.startActivity(intent)
                    }
                }
            ),
            ContactOption(
                title = "Susține-mă",
                subtitle = "Doar dacă vrei să îmi susții munca altfel",
                icon = Icons.Default.Language,
                action = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://revolut.me/teoremareziduri"))
                    context.startActivity(intent)
                }
            )
        )
    }

    val quickActions = remember {
        listOf(
            QuickAction(
                title = "Evaluează aplicația",
                icon = Icons.Default.Star,
                action = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                        context.startActivity(intent)
                    }
                }
            ),
            QuickAction(
                title = "Distribuie prietenilor",
                icon = Icons.Default.Share,
                action = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Încearcă aplicația Shaorma Finder! https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Distribuie prin"))
                }
            ),
/*
            QuickAction(
                title = "Tutorial Videos",
                icon = Icons.Default.PlayCircleOutline,
                action = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@foodfinder"))
                    context.startActivity(intent)
                }
            )
*/
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Suport",
                    tint = colorResource(R.color.gold),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Nevoie de ajutor?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gold)
                )
                Text(
                    text = "Sunt aici să te ajut",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        item {
            Text(
                text = "Contact",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(contactOptions) { option ->
            ContactCard(option)
        }

        item {
            Text(
                text = "Acțiuni",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(quickActions) { action ->
            QuickActionCard(action)
        }

        item {
            Text(
                text = "Întrebări frecvente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(faqList) { faq ->
            FAQCard(faq)
        }

        item {
            AppInfoSection()
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ContactCard(option: ContactOption) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { option.action() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.black3)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.title,
                tint = colorResource(R.color.gold),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = option.subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Deschide",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun QuickActionCard(action: QuickAction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { action.action() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.black3)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = colorResource(R.color.gold),
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = action.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Mergi",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.black3)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Restrânge" else "Extinde",
                    tint = colorResource(R.color.gold)
                )
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Color.Gray.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun AppInfoSection() {
    val context = LocalContext.current
    val packageInfo = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(R.color.black3)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Shaorma Finder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Versiunea ${packageInfo?.versionName ?: "1.0"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "© 2025 Shaorma Finder. Toate drepturile rezervate.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1YbqtiGqcBa-IwRyncbLYT7Z6PJ87KaGWQfGyLd4a_J4/edit?usp=sharing"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Confidențialitate", color = colorResource(R.color.gold), fontSize = 12.sp)
                }
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1Vi7UO31JB-qnZt6K9qWIDp2WhDS-BIMcRFDzU-8cGMY/edit?usp=sharing"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Termeni și condiții", color = colorResource(R.color.gold), fontSize = 12.sp)
                }
            }
        }
    }
}