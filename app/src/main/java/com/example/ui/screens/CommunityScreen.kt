package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.*

enum class CommunityFeedTab(val title: String) {
    FOR_YOU("لك"),
    FOLLOWING("أتابعه"),
    ARTICLES("المقالات"),
    SUCCESS_STORIES("💚 قصص نجاح")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val incidents by viewModel.strayIncidents.collectAsState()
    val animals by viewModel.animalsList.collectAsState()
    val profileImage by viewModel.profileImageUrl.collectAsState()
    val trustScore by viewModel.trustScore.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(CommunityFeedTab.FOR_YOU) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var selectedHashtag by rememberSaveable { mutableStateOf<String?>(null) }
    
    var showTweetComposer by remember { mutableStateOf(false) }
    var activeThreadIncident by remember { mutableStateOf<StrayIncident?>(null) }

    val trendingTopics = listOf(
        "#بيت_الأمان" to "5.4K تغريدة",
        "#إنقاذ_حيوانات" to "3.2K تغريدة",
        "#تبنى_لا_تشتري" to "2.8K تغريدة",
        "#طعام_الشتاء" to "1.9K تغريدة",
        "#كلاب_الرياض" to "1.4K تغريدة",
        "#قطط_جدة" to "980 تغريدة"
    )

    // Filter Feed based on active Tab, Search query and Hashtag
    val filteredIncidents = remember(incidents, selectedTab, searchQuery, selectedHashtag) {
        incidents.filter { incident ->
            val matchesTab = when (selectedTab) {
                CommunityFeedTab.FOR_YOU -> true
                CommunityFeedTab.FOLLOWING -> incident.isVerified || incident.isShelter || incident.reporter == "أحمد محمد"
                CommunityFeedTab.ARTICLES -> incident.title.contains("مقال") || incident.title.contains("نصيح") || incident.title.contains("دليل") || incident.title.contains("توعية") || incident.title.contains("طرق") || incident.title.contains("كيف") || incident.description.contains("نصيح") || incident.description.contains("دليل") || incident.description.contains("توعية") || incident.description.contains("طرق") || incident.description.contains("كيف") || incident.isShelter
                CommunityFeedTab.SUCCESS_STORIES -> !incident.isEmergency && (incident.title.contains("نجاح") || incident.description.contains("تعافي") || incident.likesCount > 200)
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                incident.title.contains(searchQuery, ignoreCase = true) ||
                incident.description.contains(searchQuery, ignoreCase = true) ||
                incident.reporter.contains(searchQuery, ignoreCase = true) ||
                incident.handle.contains(searchQuery, ignoreCase = true) ||
                incident.location.contains(searchQuery, ignoreCase = true)
            }

            val matchesHashtag = if (selectedHashtag == null) true else {
                incident.description.contains(selectedHashtag!!, ignoreCase = true) ||
                incident.title.contains(selectedHashtag!!, ignoreCase = true)
            }

            matchesTab && matchesSearch && matchesHashtag
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Twitter / X App Bar
            XTopHeader(
                userAvatarUrl = profileImage,
                trustScore = trustScore,
                isSearchOpen = isSearchActive,
                onSearchToggle = { isSearchActive = !isSearchActive },
                onComposeClick = { showTweetComposer = true }
            )

            // Animated Expandable Search Input
            AnimatedVisibility(
                visible = isSearchActive,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                XSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        searchQuery = ""
                        isSearchActive = false
                    }
                )
            }

            // 2. Twitter / X Navigation Tab Bar
            XSegmentedTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            // 3. Main Timeline Feed
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("community_timeline_list"),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // Quick What's Happening Composer Row (like X home)
                item {
                    XQuickComposerRow(
                        userAvatarUrl = profileImage,
                        onClick = { showTweetComposer = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)
                }

                // Trending Hashtags Carousel
                item {
                    XTrendingTopicsBar(
                        trends = trendingTopics,
                        selectedTag = selectedHashtag,
                        onTagClick = { tag ->
                            selectedHashtag = if (selectedHashtag == tag) null else tag
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)
                }

                // Empty State
                if (filteredIncidents.isEmpty()) {
                    item {
                        XEmptyFeedState(
                            tab = selectedTab,
                            hasFilter = searchQuery.isNotBlank() || selectedHashtag != null,
                            onClearFilter = {
                                searchQuery = ""
                                selectedHashtag = null
                            }
                        )
                    }
                } else {
                    // Tweet Stream
                    itemsIndexed(filteredIncidents, key = { _, item -> item.id }) { index, incident ->
                        CommunityPostCard(
                            incident = incident,
                            onLike = { viewModel.toggleLikeIncident(incident.id) },
                            onRepost = {
                                viewModel.toggleRepostIncident(incident.id)
                                Toast.makeText(context, if (!incident.isRepostedByMe) "تمت إعادة النشر 🔁" else "تم إلغاء إعادة النشر", Toast.LENGTH_SHORT).show()
                            },
                            onBookmark = {
                                viewModel.toggleBookmarkIncident(incident.id)
                                Toast.makeText(context, if (!incident.isBookmarkedByMe) "تمت إضافة التغريدة إلى الإشارات المرجعية 🔖" else "تمت الإزالة من المحفوظات", Toast.LENGTH_SHORT).show()
                            },
                            onCommentClick = { activeThreadIncident = incident },
                            onImageClick = {
                                val matched = animals.find { 
                                    it.imageUrl == incident.imageUrl || 
                                    (it.name.isNotBlank() && incident.title.contains(it.name)) ||
                                    (it.name.isNotBlank() && incident.description.contains(it.name))
                                } ?: AnimalItem(
                                    id = incident.id,
                                    name = incident.title.replace("🚨", "").replace("عاجل:", "").trim().take(16).ifBlank { "أليف الإنقاذ" },
                                    species = if (incident.description.contains("كلب") || incident.title.contains("كلب")) "كلب" else "قطة",
                                    breed = "بلدي / هجين أليف",
                                    age = "سنة واحدة",
                                    gender = "ذكر",
                                    size = "متوسط",
                                    description = incident.description,
                                    imageUrl = incident.imageUrl ?: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                                    likesCount = incident.likesCount,
                                    specialNeeds = incident.isEmergency,
                                    vaccinated = true,
                                    neutered = false,
                                    compatibility = "أليف ولطيف واجتماعي مع البشر",
                                    backstory = "تم إنقاذه ونشر تفاصيله في المجتمع بـ ${incident.location} بواسطة ${incident.reporter}."
                                )
                                viewModel.selectAnimal(matched)
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${incident.reporter} عبر بيت الأمان:\n${incident.description}\n📍 ${incident.location}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "مشاركة المنشور"))
                            },
                            onHashtagClick = { tag ->
                                selectedHashtag = if (selectedHashtag == tag) null else tag
                            }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )
                    }
                }
            }
        }

        // 4. Floating Action Button (X Tweet Composer FAB)
        FloatingActionButton(
            onClick = { showTweetComposer = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("floating_tweet_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "تغريد",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "نشر",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Tweet Composer Sheet
        if (showTweetComposer) {
            XTweetComposerDialog(
                userAvatarUrl = profileImage,
                onDismiss = { showTweetComposer = false },
                onPost = { text, location, isEmergency, imageUrl ->
                    viewModel.submitNewRescue(
                        title = if (isEmergency) "🚨 بلاغ عاجل من المجتمع" else "🐾 مشاركة جديدة",
                        desc = text,
                        location = location,
                        isEmergency = isEmergency,
                        customImageUrl = imageUrl
                    )
                    showTweetComposer = false
                    Toast.makeText(context, "تم نشر تغريدتك في مجتمع بيت الأمان 🐾", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Active Reply Thread Dialog
        activeThreadIncident?.let { threadIncident ->
            val latestIncident = incidents.find { it.id == threadIncident.id } ?: threadIncident
            XReplyThreadDialog(
                incident = latestIncident,
                userAvatarUrl = profileImage,
                onDismiss = { activeThreadIncident = null },
                onSendReply = { replyText ->
                    viewModel.addIncidentReply(latestIncident.id, replyText)
                    Toast.makeText(context, "تم إرسال ردك بنجاح ✨", Toast.LENGTH_SHORT).show()
                },
                onLike = { viewModel.toggleLikeIncident(latestIncident.id) },
                onRepost = { viewModel.toggleRepostIncident(latestIncident.id) }
            )
        }
    }
}

// ----------------------------------------------------
// 1. Top Twitter / X Header
// ----------------------------------------------------
@Composable
private fun XTopHeader(
    userAvatarUrl: String,
    trustScore: Int,
    isSearchOpen: Boolean,
    onSearchToggle: () -> Unit,
    onComposeClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // User Avatar with trust score ring
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = "الملف الشخصي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("🛡️", fontSize = 11.sp)
                        Text(
                            text = "$trustScore",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Center Logo / Title (Safe Paws in X style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = "Safe Paws",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "مجتمع الأمان",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Right Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onSearchToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = if (isSearchOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onComposeClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddComment,
                        contentDescription = "نشر سريع",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. X Search Bar (Animated)
// ----------------------------------------------------
@Composable
private fun XSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "ابحث في التغريدات، الهاشتاقات، أسماء المنقذين...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "مسح",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 3. X Segmented Tabs ("لك", "أتابعه", "طوارئ", "قصص نجاح")
// ----------------------------------------------------
@Composable
private fun XSegmentedTabBar(
    selectedTab: CommunityFeedTab,
    onTabSelected: (CommunityFeedTab) -> Unit
) {
    val tabs = CommunityFeedTab.values()
    val selectedIndex = tabs.indexOf(selectedTab)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedIndex])
                            .height(3.5.dp)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("tab_${tab.name}"),
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            fontSize = if (isSelected) 14.sp else 13.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 4. Quick Composer Row (What's happening?)
// ----------------------------------------------------
@Composable
private fun XQuickComposerRow(
    userAvatarUrl: String,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = userAvatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "ماذا يحدث في عالم الإنقاذ والرعاية؟ 🐾",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "صورة",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 5. Trending Topics Carousel
// ----------------------------------------------------
@Composable
private fun XTrendingTopicsBar(
    trends: List<Pair<String, String>>,
    selectedTag: String?,
    onTagClick: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🔥", fontSize = 13.sp)
                    Text(
                        text = "المتداول في الرعاية والإنقاذ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (selectedTag != null) {
                    Text(
                        text = "إلغاء التصفية",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onTagClick(selectedTag) }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trends) { (tag, count) ->
                    val isSelected = selectedTag == tag
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.clickable { onTagClick(tag) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = tag,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = count,
                                fontSize = 10.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 6. CommunityPostCard / X Tweet Item (Accessible by HomeScreen & Timeline)
// ----------------------------------------------------
@Composable
fun CommunityPostCard(
    incident: StrayIncident,
    onLike: () -> Unit = {},
    onRepost: () -> Unit = {},
    onBookmark: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onShare: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onImageClick: () -> Unit = onCommentClick
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tweet_card_${incident.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Column: User Avatar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (incident.reporter == "rengosport18") Color(0xFFF2C94C)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (incident.reporter == "rengosport18") {
                        Text("RS", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 13.sp)
                    } else if (!incident.reporterAvatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = incident.reporterAvatarUrl,
                            contentDescription = incident.reporter,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = incident.reporter.take(1),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Right Column: Tweet Content & Action Bar
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header Line (Author Name + Verified Badge + Handle + Time + More Menu)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = incident.reporter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Verified or Shelter Badge
                        if (incident.isShelter) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFB300).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "ملجأ معتمد",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text("ملجأ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                        } else if (incident.isVerified || incident.reporter == "rengosport18" || incident.reporter == "أحمد محمد") {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "موثق",
                                tint = Color(0xFF1DA1F2), // Twitter Blue
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Handle (@username)
                        val displayHandle = if (incident.handle.isNotBlank()) "@${incident.handle}"
                        else "@${incident.reporter.replace(" ", "_")}"
                        Text(
                            text = displayHandle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "·",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = incident.timestamp,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // More Options Dropdown
                    Box {
                        IconButton(
                            onClick = { showOptionsMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "خيارات",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("نسخ نص المنشور") },
                                onClick = { showOptionsMenu = false },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("مشاركة عبر تطبيقات أخرى") },
                                onClick = {
                                    showOptionsMenu = false
                                    onShare()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("كتم ${incident.reporter}") },
                                onClick = { showOptionsMenu = false },
                                leadingIcon = { Icon(Icons.Default.VolumeOff, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("إبلاغ عن محتوى غير لائق", color = MaterialTheme.colorScheme.error) },
                                onClick = { showOptionsMenu = false },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }

                // Emergency Badge if active
                if (incident.isEmergency) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                        border = BorderStroke(0.6.dp, Color(0xFFD32F2F).copy(alpha = 0.3f)),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🚨", fontSize = 10.sp)
                            Text(
                                text = "حالة إنقاذ طارئة - تحتاج تدخلاً عاجلاً",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Optional Title (if distinct)
                if (incident.title.isNotBlank() && !incident.title.startsWith("مشاركة جديدة") && !incident.title.startsWith("🚨 بلاغ عاجل")) {
                    Text(
                        text = incident.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }

                // Tweet Body with Highlighted Hashtags & Mentions
                val annotatedBody = buildAnnotatedString {
                    val words = incident.description.split(" ")
                    words.forEachIndexed { i, word ->
                        if (word.startsWith("#") || word.startsWith("@")) {
                            pushStringAnnotation(tag = "HASHTAG", annotation = word)
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(word)
                            }
                            pop()
                        } else {
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                                append(word)
                            }
                        }
                        if (i < words.size - 1) append(" ")
                    }
                }

                Text(
                    text = annotatedBody,
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Location Pin Tag
                if (incident.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
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
                    }
                }

                // Tweet Media Container (Image)
                if (!incident.imageUrl.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                0.8.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onImageClick() }
                    ) {
                        AsyncImage(
                            model = incident.imageUrl,
                            contentDescription = incident.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { onImageClick() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // X Bottom Metrics Action Bar (Reply, Repost, Like, Views, Share/Bookmark)
                XTweetActionBar(
                    incident = incident,
                    onReply = onCommentClick,
                    onRepost = onRepost,
                    onLike = onLike,
                    onBookmark = onBookmark,
                    onShare = onShare
                )
            }
        }
    }
}

// ----------------------------------------------------
// 7. X Tweet Action Bar (5 Engagement Columns with Spring pop effects)
// ----------------------------------------------------
@Composable
private fun XTweetActionBar(
    incident: StrayIncident,
    onReply: () -> Unit,
    onRepost: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit
) {
    val heartScale = remember { Animatable(1f) }
    val repostScale = remember { Animatable(1f) }

    LaunchedEffect(incident.isLikedByMe) {
        if (incident.isLikedByMe) {
            heartScale.animateTo(1.35f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f))
            heartScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }
    }

    LaunchedEffect(incident.isRepostedByMe) {
        if (incident.isRepostedByMe) {
            repostScale.animateTo(1.35f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 600f))
            repostScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, end = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Reply Action
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onReply() }
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "الردود",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(17.dp)
            )
            if (incident.commentsCount > 0) {
                Text(
                    text = "${incident.commentsCount}",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Repost Action (Turns Vibrant Green when active)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onRepost() }
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Repeat,
                contentDescription = "إعادة نشر",
                tint = if (incident.isRepostedByMe) Color(0xFF00BA7C) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .scale(repostScale.value)
            )
            if (incident.repostsCount > 0) {
                Text(
                    text = "${incident.repostsCount}",
                    fontSize = 11.5.sp,
                    color = if (incident.isRepostedByMe) Color(0xFF00BA7C) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Like Action (Turns Hot Pink / Red with pop animation)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onLike() }
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (incident.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "إعجاب",
                tint = if (incident.isLikedByMe) Color(0xFFF91880) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(17.dp)
                    .scale(heartScale.value)
            )
            if (incident.likesCount > 0) {
                Text(
                    text = "${incident.likesCount}",
                    fontSize = 11.5.sp,
                    color = if (incident.isLikedByMe) Color(0xFFF91880) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 4. Views / Impressions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = "المشاهدات",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = incident.viewsCount,
                fontSize = 11.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal
            )
        }

        // 5. Bookmark & Share Icons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onBookmark,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (incident.isBookmarkedByMe) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "حفظ",
                    tint = if (incident.isBookmarkedByMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "مشاركة",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// 8. Full X-Style Tweet Composer Dialog
// ----------------------------------------------------
@Composable
private fun XTweetComposerDialog(
    userAvatarUrl: String,
    onDismiss: () -> Unit,
    onPost: (text: String, location: String, isEmergency: Boolean, imageUrl: String?) -> Unit
) {
    var tweetText by rememberSaveable { mutableStateOf("") }
    var tweetLocation by rememberSaveable { mutableStateOf("حي الملقا، الرياض") }
    var isEmergency by rememberSaveable { mutableStateOf(false) }
    var selectedImageUrl by rememberSaveable { mutableStateOf<String?>(null) }
    var showLocationEdit by rememberSaveable { mutableStateOf(false) }

    val sampleMedia = listOf(
        "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
        "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
        "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
        "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600"
    )

    val charLimit = 280
    val charCount = tweetText.length
    val progress = (charCount.toFloat() / charLimit).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Circular Character Counter Ring (X style)
                        if (charCount > 0) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(22.dp),
                                    color = if (charCount > 260) Color(0xFFE0245E) else MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (tweetText.isNotBlank()) {
                                    onPost(tweetText, tweetLocation, isEmergency, selectedImageUrl)
                                }
                            },
                            enabled = tweetText.isNotBlank() && charCount <= charLimit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEmergency) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("submit_tweet_btn")
                        ) {
                            Text(
                                text = if (isEmergency) "نشر كبلاغ طارئ 🚨" else "تغريد 🐾",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Composer Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = userAvatarUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            // Audience Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("الجميع يمكنهم الرد 🌐", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            BasicTextField(
                                value = tweetText,
                                onValueChange = { tweetText = it },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tweet_text_input"),
                                decorationBox = { innerTextField ->
                                    if (tweetText.isEmpty()) {
                                        Text(
                                            text = "ماذا يحدث؟ شارك قصة إنقاذ، نصيحة رعاية، أو اطلب المساعدة لأليف في منطقتك...",
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }

                    // Attached Media Preview
                    if (selectedImageUrl != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = selectedImageUrl,
                                contentDescription = "الصورة المرفقة",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { selectedImageUrl = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(28.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "حذف الصورة", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Emergency Rescue Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEmergency) Color(0xFFD32F2F).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(0.8.dp, if (isEmergency) Color(0xFFD32F2F) else Color.Transparent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isEmergency = !isEmergency }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🚨", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "تحديد كحالة إنقاذ طارئة",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isEmergency) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "سيتم تمييز المنشور بلون تنبيهي أحمر وتثبيته في تبويب الطوارئ",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isEmergency,
                                onCheckedChange = { isEmergency = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFD32F2F)
                                )
                            )
                        }
                    }

                    // Location Input
                    if (showLocationEdit) {
                        OutlinedTextField(
                            value = tweetLocation,
                            onValueChange = { tweetLocation = it },
                            label = { Text("الموقع الجغرافي / الحي") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Quick Hashtag Suggestions
                    Text(
                        text = "هاشتاقات مقترحة:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("#بيت_الأمان", "#إنقاذ_حيوانات", "#تبنى_لا_تشتري", "#رعاية_الشارع").forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                modifier = Modifier.clickable {
                                    if (!tweetText.contains(tag)) {
                                        tweetText = if (tweetText.isBlank()) tag else "$tweetText $tag"
                                    }
                                }
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom Toolbar (Media selector, Location, Emergency)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attach Photo Button
                        IconButton(onClick = {
                            selectedImageUrl = sampleMedia.random()
                        }) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "إضافة صورة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Location Button
                        IconButton(onClick = { showLocationEdit = !showLocationEdit }) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "تحديد الموقع",
                                tint = if (showLocationEdit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Emergency Flag
                        IconButton(onClick = { isEmergency = !isEmergency }) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "طارئ",
                                tint = if (isEmergency) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = "${charLimit - charCount}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (charLimit - charCount < 20) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 9. Interactive Reply Thread Dialog (Twitter Thread View)
// ----------------------------------------------------
@Composable
private fun XReplyThreadDialog(
    incident: StrayIncident,
    userAvatarUrl: String,
    onDismiss: () -> Unit,
    onSendReply: (String) -> Unit,
    onLike: () -> Unit,
    onRepost: () -> Unit
) {
    var replyText by rememberSaveable { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Text(
                        text = "التغريدة والردود 💬",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Box(modifier = Modifier.size(40.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // Scrollable Thread Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Original Main Tweet
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                ) {
                                    if (!incident.reporterAvatarUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = incident.reporterAvatarUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(
                                            text = incident.reporter.take(1),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 18.sp,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(text = incident.reporter, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (incident.isVerified) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(15.dp))
                                        }
                                    }
                                    Text(
                                        text = if (incident.handle.isNotBlank()) "@${incident.handle}" else "@${incident.reporter.replace(" ", "_")}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = incident.description,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (!incident.imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = incident.imageUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(incident.timestamp, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("·", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(incident.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                            // Action stats count row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "${incident.repostsCount} إعادة نشر",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${incident.likesCount} إعجاب",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${incident.viewsCount} مشاهدة",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        }
                    }

                    // Comments Title
                    item {
                        Text(
                            text = "الردود المجتمعية (${incident.comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // List of Replies
                    if (incident.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "كن أول من يرد ويقدم المساعدة أو الدعم 🐾",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(incident.comments) { comment ->
                            XReplyItem(comment = comment)
                        }
                    }
                }

                // Bottom Reply Input Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        if (replyText.isEmpty()) {
                            Text(
                                text = "أضف ردك على ${incident.reporter}...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            textStyle = TextStyle(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendReply(replyText)
                                replyText = ""
                            }
                        },
                        enabled = replyText.isNotBlank(),
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "إرسال",
                            tint = if (replyText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 10. Individual Reply Item
// ----------------------------------------------------
@Composable
private fun XReplyItem(comment: CommunityComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
        ) {
            if (!comment.avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = comment.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = comment.author.take(1),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = comment.author, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (comment.isVerified) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1DA1F2), modifier = Modifier.size(13.dp))
                }
                Text(text = "@${comment.handle}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("·", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(comment.timestamp, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = comment.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ----------------------------------------------------
// 11. Empty Feed State
// ----------------------------------------------------
@Composable
private fun XEmptyFeedState(
    tab: CommunityFeedTab,
    hasFilter: Boolean,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("🐾", fontSize = 48.sp)
        Text(
            text = if (hasFilter) "لم يتم العثور على تغريدات مطابقة للبحث" else "لا توجد منشورات حالياً في ${tab.title}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (hasFilter) "جرب البحث بكلمة أو هاشتاق آخر" else "كن أول من يشارك قصة أو يبدأ نقاشاً مجتمعياً!",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (hasFilter) {
            Button(
                onClick = onClearFilter,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("مسح التصفية والبحث")
            }
        }
    }
}
