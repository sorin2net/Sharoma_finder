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

// ===== DATA CLASSES =====
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

// ===== MAIN COMPOSABLE =====
@Composable
fun SupportScreen() {
    val context = LocalContext.current

    // ✅ FAQ LIST
    val faqList = remember {
        listOf(
            FAQItem(
                "How do I find restaurants near me?",
                "The app uses GPS to show nearby restaurants. Make sure location is enabled, then check the 'Nearest' section on the Results page."
            ),
            FAQItem(
                "How do I save my favorite restaurants?",
                "Tap the heart icon on any restaurant card to add it to your Wishlist. Access your favorites from the Wishlist tab."
            ),
            FAQItem(
                "What do the subcategories mean?",
                "Subcategories (Burger, Pizza, Sushi, etc.) help filter restaurants by food type. Tap one to see only matching restaurants."
            ),
            FAQItem(
                "How accurate is the distance shown?",
                "Distance is calculated in real-time using GPS. Make sure location services are enabled for best accuracy."
            ),
            FAQItem(
                "Can I use the app without GPS?",
                "Yes, but you won't see distance calculations or 'Nearest' sorting. You can still browse all restaurants and search."
            ),
            FAQItem(
                "How do I contact a restaurant?",
                "Open the restaurant's map view and tap 'Call to Store' to dial directly from the app."
            ),
            FAQItem(
                "How do I change my profile picture?",
                "Go to Profile tab, tap your current picture, and select a new photo from your gallery."
            ),
            FAQItem(
                "What does the Reward rank mean?",
                "Your rank (Newcomer, Appetizer, Gourmand, Connoisseur) increases based on how many restaurants you add to your Wishlist."
            )
        )
    }

    // ✅ CONTACT OPTIONS
    val contactOptions = remember {
        listOf(
            ContactOption(
                title = "Email Support",
                subtitle = "support@foodfinder.com",
                icon = Icons.Default.Email,
                action = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@foodfinder.com")
                        putExtra(Intent.EXTRA_SUBJECT, "Food Finder Support Request")
                    }
                    context.startActivity(Intent.createChooser(intent, "Send Email"))
                }
            ),
            ContactOption(
                title = "Call Us",
                subtitle = "+40 123 456 789",
                icon = Icons.Default.Phone,
                action = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+40123456789"))
                    context.startActivity(intent)
                }
            ),
            ContactOption(
                title = "WhatsApp",
                subtitle = "Chat with us",
                icon = Icons.Default.Chat,
                action = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/40123456789?text=Hello, I need help with Food Finder app")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback dacă WhatsApp nu e instalat
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/40123456789"))
                        context.startActivity(intent)
                    }
                }
            ),
            ContactOption(
                title = "Website",
                subtitle = "www.foodfinder.com",
                icon = Icons.Default.Language,
                action = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://foodfinder.com"))
                    context.startActivity(intent)
                }
            )
        )
    }

    // ✅ QUICK ACTIONS
    val quickActions = remember {
        listOf(
            QuickAction(
                title = "Rate Our App",
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
                title = "Share with Friends",
                icon = Icons.Default.Share,
                action = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out Food Finder app! https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                }
            ),
            QuickAction(
                title = "Tutorial Videos",
                icon = Icons.Default.PlayCircleOutline,
                action = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@foodfinder"))
                    context.startActivity(intent)
                }
            )
        )
    }

    // ===== UI =====
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black2))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ===== HEADER =====
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Support",
                    tint = colorResource(R.color.gold),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "How can we help?",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.gold)
                )
                Text(
                    text = "We're here to assist you",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }

        // ===== CONTACT OPTIONS =====
        item {
            Text(
                text = "Contact Us",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(contactOptions) { option ->
            ContactCard(option)
        }

        // ===== QUICK ACTIONS =====
        item {
            Text(
                text = "Quick Actions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(quickActions) { action ->
            QuickActionCard(action)
        }

        // ===== FAQ SECTION =====
        item {
            Text(
                text = "Frequently Asked Questions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold),
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        items(faqList) { faq ->
            FAQCard(faq)
        }

        // ===== APP INFO =====
        item {
            AppInfoSection()
        }

        // Spacing la final
        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ===== CONTACT CARD =====
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
                contentDescription = "Open",
                tint = Color.Gray
            )
        }
    }
}

// ===== QUICK ACTION CARD =====
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
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

// ===== FAQ CARD =====
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
                    contentDescription = if (expanded) "Collapse" else "Expand",
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

// ===== APP INFO SECTION =====
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
                text = "Food Finder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.gold)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Version ${packageInfo?.versionName ?: "1.0"}",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "© 2025 Food Finder. All rights reserved.",
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
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://foodfinder.com/privacy"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Privacy Policy", color = colorResource(R.color.gold), fontSize = 12.sp)
                }
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://foodfinder.com/terms"))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Terms of Service", color = colorResource(R.color.gold), fontSize = 12.sp)
                }
            }
        }
    }
}