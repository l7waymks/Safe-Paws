package com.example.ui.ui


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.theme.*
import androidx.compose.ui.text.TextStyle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

import com.example.*
import com.example.ui.components.*
import com.example.ui.screens.*

fun CommunityScreen(viewModel: MainViewModel) {
    val incidents by viewModel.strayIncidents.collectAsState()
    var postText by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    
    var selectedChip by remember { mutableStateOf("الكل") }
    var communitySearchQuery by remember { mutableStateOf("") }

    val filteredIncidents = incidents.filter { incident ->
        val matchesSearch = incident.title.contains(communitySearchQuery, true) || 
                            incident.description.contains(communitySearchQuery, true) || 
                            incident.reporter.contains(communitySearchQuery, true)
        
        val matchesChip = when (selectedChip) {
            "الكل" -> true
            "قصص نجاح" -> !incident.isEmergency && (incident.title.contains("نجاح") || incident.description.contains("إنقاذ") || incident.id == "s3")
            "حالات عاجلة" -> incident.isEmergency
            "مناقشات" -> !incident.isEmergency && !incident.title.contains("نجاح") && !incident.description.contains("إنقاذ") && incident.id != "s3"
            else -> true
        }
        
        matchesSearch && matchesChip
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Column {
                Text(
                    text = "تواصل أبطال الرعاية 🌟",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "المناقشات المجتمعية وقصص الحيوانات المنقذة بنهج Pro-Life رحيم.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 2. Search Bar
        item {
            BasicTextField(
                value = communitySearchQuery,
                onValueChange = { communitySearchQuery = it },
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("community_search_input"),
                decorationBox = @Composable { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color(0xFF1E1E1E),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (communitySearchQuery.isEmpty()) {
                                Text(
                                    text = "ابحث عن منشور، ناشر، موضوع...",
                                    color = Color.Gray,
                                    fontSize = 13.sp
                                )
                            }
                            innerTextField()
                        }
                        if (communitySearchQuery.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { communitySearchQuery = "" }
                            )
                        }
                    }
                }
            )
        }

        // 3. Quick Tags Row
        item {
            val chips = listOf("الكل", "قصص نجاح", "حالات عاجلة", "مناقشات")
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chips) { chip ->
                    val isSelected = selectedChip == chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary 
                                else Color(0xFF1E1E1E)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedChip = chip }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = chip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }

        // 4. Composer Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("أ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("شارك شيئاً مع المجتمع...", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                    }

                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        placeholder = { Text("كيف حال أليفك؟ أو أضف سؤالاً بخصوص رعاية الشارع وتأمين طعام الشتاء...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_composer_input"),
                        textStyle = TextStyle(fontSize = 13.sp),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {}) { Icon(Icons.Default.AddPhotoAlternate, contentDescription = "إضافة صورة", tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = {}) { Icon(Icons.Default.LocationOn, contentDescription = "الموقع", tint = MaterialTheme.colorScheme.primary) }
                        }
                        Button(
                            onClick = {
                                if (postText.isNotBlank()) {
                                    viewModel.submitNewRescue(
                                        title = "مشاركة جديدة من أبطال الرعاية",
                                        desc = postText,
                                        location = "حي الملقا، الرياض"
                                    )
                                    postText = ""
                                }
                            },
                            modifier = Modifier.testTag("publish_post_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("نشر", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 5. Post Feed List
        if (filteredIncidents.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لم نجد منشورات مطابقة لبحثك في المجتمع.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredIncidents) { incident ->
                CommunityPostCard(incident = incident)
            }
        }
    }
}

@Composable
fun CommunityPostCard(incident: StrayIncident) {
    var likes by rememberSaveable { mutableStateOf(incident.likesCount) }
    var userLiked by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("community_post_card_${incident.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE1E8ED))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (Avatar, Name, Handle, Time, More Option)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Reporter Avatar
                    if (incident.reporter == "rengosport18") {
                        // Custom gold/yellow circle with "RS" exactly like screenshot
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF2C94C))
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RS",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    } else if (incident.reporterAvatarUrl != null) {
                        AsyncImage(
                            model = incident.reporterAvatarUrl,
                            contentDescription = "صورة الناشر",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFE0E0E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = incident.reporter.take(1),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = incident.reporter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            if (incident.reporter == "rengosport18" || incident.reporter == "أحمد محمد") {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "موثق",
                                    tint = Color(0xFF1DA1F2),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (incident.reporter == "rengosport18") "@rengosport18" else "@${incident.reporter.replace(" ", "_")}",
                                fontSize = 12.sp,
                                color = Color(0xFF657786)
                            )
                            Text(
                                text = "·",
                                fontSize = 12.sp,
                                color = Color(0xFF657786)
                            )
                            Text(
                                text = incident.timestamp,
                                fontSize = 12.sp,
                                color = Color(0xFF657786)
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "خيارات المنشور",
                    tint = Color(0xFF657786),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Description / Body
            if (incident.title.isNotBlank() && !incident.title.startsWith("مشاركة جديدة")) {
                Text(
                    text = incident.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
            
            Text(
                text = incident.description,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Color(0xFF0F1419),
                modifier = Modifier.fillMaxWidth()
            )

            // Location badge if helpful
            if (incident.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "الموقع",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = incident.location,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (incident.isEmergency) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD32F2F).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color(0xFFD32F2F).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "حالة عاجلة 🚨",
                                color = Color(0xFFD32F2F),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Image Section
            if (incident.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = incident.imageUrl,
                    contentDescription = incident.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE1E8ED), RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row (Exactly matching the Twitter/X screenshot layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes Button (Heart)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable {
                            likes = if (userLiked) likes - 1 else likes + 1
                            userLiked = !userLiked
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (userLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "أعجبني",
                        tint = if (userLiked) Color(0xFFE0245E) else Color(0xFF657786),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = likes.toString(),
                        color = if (userLiked) Color(0xFFE0245E) else Color(0xFF657786),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comments Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "التعليقات",
                        tint = Color(0xFF657786),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = incident.commentsCount.toString(),
                        color = Color(0xFF657786),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Reposts Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "إعادة نشر",
                        tint = Color(0xFF657786),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = incident.repostsCount.toString(),
                        color = Color(0xFF657786),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "مشاركة",
                        tint = Color(0xFF657786),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = incident.sharesCount.toString(),
                        color = Color(0xFF657786),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


// ---------------------- 5. GEOSPATIAL HELPMAP (SCREENSHOT 3) ----------------------
// Native Pet Place representation


