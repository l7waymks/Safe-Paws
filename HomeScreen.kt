package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.*
import com.example.ui.theme.PrimaryTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Data item representing a full-screen TikTok / Reels video & post item
 */
data class TikTokFeedItem(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String,
    val title: String,
    val description: String,
    val musicTrack: String,
    val mediaUrl: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val bookmarksCount: Int,
    val isVerified: Boolean = false,
    val isEmergency: Boolean = false,
    val isFollowing: Boolean = false,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val location: String = "الرياض، المملكة العربية السعودية",
    val animalItem: AnimalItem? = null,
    val incidentItem: StrayIncident? = null,
    val commentsList: List<CommunityComment> = emptyList()
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val animals by viewModel.animalsList.collectAsState()
    val incidents by viewModel.strayIncidents.collectAsState()

    // TikTok Top Bar Active Tab: "following" (أتابعه) or "for_you" (لك)
    var selectedTopTab by remember { mutableStateOf("for_you") }
    var activeCommentsItem by remember { mutableStateOf<TikTokFeedItem?>(null) }
    var activeCommentTimestamp by remember { mutableStateOf<String?>(null) }
    var activeShareItem by remember { mutableStateOf<TikTokFeedItem?>(null) }
    var isDanmakuEnabled by remember { mutableStateOf(true) }

    // Convert Animals and Incidents into unified TikTok items
    val tikTokItems = remember(animals, incidents, selectedTopTab) {
        val list = mutableListOf<TikTokFeedItem>()

        // 1. Convert stray incidents
        incidents.forEach { inc ->
            list.add(
                TikTokFeedItem(
                    id = "inc_${inc.id}",
                    authorName = inc.reporter,
                    authorHandle = if (inc.handle.isNotBlank()) "@${inc.handle}" else "@${inc.reporter.replace(" ", "_")}",
                    authorAvatarUrl = inc.reporterAvatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                    title = inc.title,
                    description = inc.description,
                    musicTrack = "أصوات الطبيعة وخرخرة الحيوانات الأليفة • صوت أصلي",
                    mediaUrl = inc.imageUrl ?: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800",
                    likesCount = inc.likesCount,
                    commentsCount = inc.commentsCount,
                    sharesCount = inc.sharesCount.coerceAtLeast(12),
                    bookmarksCount = (inc.likesCount / 4).coerceAtLeast(5),
                    isVerified = inc.isVerified,
                    isEmergency = false,
                    isFollowing = inc.isVerified,
                    isLiked = inc.isLikedByMe,
                    isBookmarked = inc.isBookmarkedByMe,
                    location = inc.location,
                    incidentItem = inc,
                    commentsList = inc.comments
                )
            )
        }

        // 2. Convert animals for adoption
        animals.forEach { animal ->
            list.add(
                TikTokFeedItem(
                    id = "pet_${animal.id}",
                    authorName = "مبادرة بيت الأمان",
                    authorHandle = "@safe_paws_official",
                    authorAvatarUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=120",
                    title = "فرصة تبني: ${animal.name} (${animal.breed})",
                    description = "${animal.name} هو ${animal.species} أليف ورائع، يبلغ من العمر ${animal.age} وهو ${animal.gender}. ${animal.description} #تبنى_لا_تشتري #حيوانات_أليفة",
                    musicTrack = "Cute Pet Vibes • نغمة مرحة للحيوانات الأليفة",
                    mediaUrl = animal.imageUrl,
                    likesCount = animal.likesCount,
                    commentsCount = (animal.likesCount / 3).coerceAtLeast(8),
                    sharesCount = (animal.likesCount / 5).coerceAtLeast(15),
                    bookmarksCount = (animal.likesCount / 2).coerceAtLeast(10),
                    isVerified = true,
                    isEmergency = false,
                    isFollowing = true,
                    isLiked = false,
                    isBookmarked = false,
                    location = "الرياض، المملكة العربية السعودية",
                    animalItem = animal
                )
            )
        }

        if (selectedTopTab == "following") {
            list.filter { it.isFollowing || it.isVerified }
        } else {
            list
        }
    }

    val pagerState = rememberPagerState(pageCount = { tikTokItems.size.coerceAtLeast(1) })

    val isVideoPlaying by viewModel.isVideoPlaying.collectAsState()
    val isScrubbingVideo by viewModel.isScrubbingVideo.collectAsState()

    // Reset progress to 0 when user swipes to another video
    var lastPage by remember { mutableIntStateOf(0) }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != lastPage) {
            lastPage = pagerState.currentPage
            viewModel.seekVideoProgress(0f)
            viewModel.setVideoPlaying(true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("tiktok_home_screen")
    ) {
        if (tikTokItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else {
            // Full Screen Vertical Pager (TikTok style)
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val item = tikTokItems.getOrNull(page) ?: return@VerticalPager
                TikTokVideoPage(
                    item = item,
                    isDanmakuEnabled = isDanmakuEnabled,
                    isPlaying = isVideoPlaying,
                    onTogglePlay = { viewModel.toggleVideoPlayback() },
                    onAvatarClick = {
                        viewModel.openUserProfileByInfo(
                            name = item.authorName,
                            handle = item.authorHandle,
                            avatarUrl = item.authorAvatarUrl,
                            isVerified = item.isVerified,
                            location = item.location
                        )
                    },
                    onLike = {
                        if (item.incidentItem != null) {
                            viewModel.toggleLikeIncident(item.incidentItem.id)
                        } else if (item.animalItem != null) {
                            viewModel.incrementLikes(item.animalItem)
                        }
                    },
                    onComment = { timestamp ->
                        activeCommentTimestamp = timestamp
                        activeCommentsItem = item
                    },
                    onBookmark = {
                        if (item.incidentItem != null) {
                            viewModel.toggleBookmarkIncident(item.incidentItem.id)
                        } else {
                            Toast.makeText(context, "تم الحفظ في المفضلة ⭐", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        activeShareItem = item
                    }
                )
            }
        }

        // Top Header Bar (TikTok Style: Following | For You + Search)
        TikTokTopHeader(
            selectedTab = selectedTopTab,
            onTabSelected = { selectedTopTab = it },
            onSearchClick = { viewModel.openSearch() }
        )

        // Unique Rescue Comments Sheet Dialog
        activeCommentsItem?.let { item ->
            RescueCommentsDialog(
                item = item,
                isDanmakuEnabled = isDanmakuEnabled,
                onToggleDanmaku = { isDanmakuEnabled = !isDanmakuEnabled },
                onDismiss = {
                    activeCommentsItem = null
                    activeCommentTimestamp = null
                },
                onUserClick = { name, handle, avatarUrl, isVerified ->
                    activeCommentsItem = null
                    activeCommentTimestamp = null
                    viewModel.openUserProfileByInfo(
                        name = name,
                        handle = handle,
                        avatarUrl = avatarUrl,
                        isVerified = isVerified
                    )
                },
                onSendComment = { commentText ->
                    if (item.incidentItem != null) {
                        viewModel.addIncidentReply(
                            incidentId = item.incidentItem.id,
                            replyText = commentText
                        )
                    } else if (item.animalItem != null) {
                        viewModel.selectAnimal(item.animalItem)
                        viewModel.addComment(commentText)
                    }
                    Toast.makeText(context, "تم نشر تعليقك المميز على الفيديو بنجاح", Toast.LENGTH_SHORT).show()
                },
                onToggleLikeComment = { commentId ->
                    if (item.incidentItem != null) {
                        viewModel.toggleLikeComment(item.incidentItem.id, commentId)
                    }
                }
            )
        }

        // Share Bottom Sheet Dialog
        activeShareItem?.let { item ->
            TikTokShareDialog(
                item = item,
                onDismiss = { activeShareItem = null },
                onShareToSocial = { platform ->
                    activeShareItem = null
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "شاهد هذا المحتوى الرائع على تطبيق بيت الأمان:\n${item.title}\n${item.description}\nhttps://safepaws.app/reel/${item.id}"
                        )
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة عبر"))
                }
            )
        }
    }
}

/**
 * Top Header Navigation Bar in classic TikTok style
 */
@Composable
private fun TikTokTopHeader(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
    ) {
        // Center Tabs: أتابعه (Following) | لك (For You)
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Following Tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("following") }
            ) {
                Text(
                    text = "أتابعه",
                    color = if (selectedTab == "following") Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = if (selectedTab == "following") 17.sp else 15.sp,
                    fontWeight = if (selectedTab == "following") FontWeight.Black else FontWeight.SemiBold
                )
                if (selectedTab == "following") {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(28.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                }
            }

            // For You Tab (لك)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected("for_you") }
            ) {
                Text(
                    text = "لك",
                    color = if (selectedTab == "for_you") Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = if (selectedTab == "for_you") 17.sp else 15.sp,
                    fontWeight = if (selectedTab == "for_you") FontWeight.Black else FontWeight.SemiBold
                )
                if (selectedTab == "for_you") {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(24.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        // Right Side: Search Icon
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "بحث",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Individual Full-screen TikTok page with rich media, animated interactions, and side action buttons
 */
@Composable
private fun TikTokVideoPage(
    item: TikTokFeedItem,
    isDanmakuEnabled: Boolean = true,
    isPlaying: Boolean = true,
    onTogglePlay: () -> Unit = {},
    onAvatarClick: () -> Unit,
    onLike: () -> Unit,
    onComment: (timestamp: String?) -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    var isLikedLocal by remember(item.id) { mutableStateOf(item.isLiked) }
    var likesCountLocal by remember(item.id) { mutableStateOf(item.likesCount) }
    var isBookmarkedLocal by remember(item.id) { mutableStateOf(item.isBookmarked) }
    var showHeartPop by remember { mutableStateOf(false) }
    var heartOffset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "music_disc")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_spin"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        heartOffset = offset
                        showHeartPop = true
                        if (!isLikedLocal) {
                            isLikedLocal = true
                            likesCountLocal += 1
                            onLike()
                        }
                    },
                    onTap = {
                        // Double tap likes, single tap toggles pause/play
                        onTogglePlay()
                    }
                )
            }
    ) {
        // 1. Background Media (Image/Video simulation with gradient overlay)
        AsyncImage(
            model = item.mediaUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Centered Play button indicator when paused
        AnimatedVisibility(
            visible = !isPlaying,
            enter = fadeIn() + scaleIn(initialScale = 0.75f),
            exit = fadeOut() + scaleOut(targetScale = 0.75f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "تشغيل الفيديو",
                    tint = Color.White,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        // Gradient Shades (Top for status bar & Bottom for text clarity)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f), Color.Black)
                    )
                )
        )

        // 2. Danmaku Live Floating Comments Layer over Video
        DanmakuFloatingCommentsOverlay(
            comments = item.commentsList,
            isVisible = isDanmakuEnabled,
            onCommentClick = { clickedComment ->
                onComment(clickedComment.videoTimestamp ?: "00:15")
            },
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )

        // 3. Heart Pop Animation on Double Tap
        if (showHeartPop) {
            LaunchedEffect(showHeartPop) {
                delay(800)
                showHeartPop = false
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFE2C55),
                    modifier = Modifier
                        .size(110.dp)
                        .scale(1.2f)
                )
            }
        }

        // 4. Right Action Column (Avatar with follow, Like, Comment, Bookmark, Share, Music Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Avatar with "+" Follow badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onAvatarClick() }
            ) {
                AsyncImage(
                    model = item.authorAvatarUrl,
                    contentDescription = item.authorName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.TopCenter)
                )

                // Pink "+" Follow Icon
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFE2C55))
                        .align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "متابعة",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Like Button
            TikTokActionButton(
                icon = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = formatCount(likesCountLocal),
                iconColor = if (isLikedLocal) Color(0xFFFE2C55) else Color.White,
                onClick = {
                    isLikedLocal = !isLikedLocal
                    likesCountLocal = if (isLikedLocal) likesCountLocal + 1 else (likesCountLocal - 1).coerceAtLeast(0)
                    onLike()
                }
            )

            // Comment Button
            TikTokActionButton(
                icon = Icons.Default.Comment,
                label = formatCount(item.commentsCount),
                onClick = { onComment(null) }
            )

            // Bookmark / Save Button
            TikTokActionButton(
                icon = if (isBookmarkedLocal) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = formatCount(item.bookmarksCount),
                iconColor = if (isBookmarkedLocal) Color(0xFFFFD700) else Color.White,
                onClick = {
                    isBookmarkedLocal = !isBookmarkedLocal
                    onBookmark()
                }
            )

            // Share Button
            TikTokActionButton(
                icon = Icons.AutoMirrored.Filled.Send,
                label = formatCount(item.sharesCount),
                onClick = onShare
            )

            // Rotating Music Vinyl Disc
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .rotate(discRotation),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = item.authorAvatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
            }
        }

        // 5. Bottom Left Content Overlay (Author handle, Description, Location, Audio ticker)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.80f)
                .padding(start = 16.dp, end = 8.dp, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Author Name & Verified Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAvatarClick() }
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = item.authorHandle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (item.isVerified) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "موثق",
                        tint = Color(0xFF20D5EC),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Description / Post text
            Text(
                text = item.description,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Location Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF20D5EC),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = item.location,
                    color = Color(0xFF20D5EC),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Music / Audio Ticker Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = item.musicTrack,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Danmaku Live Floating Comments on Video Surface (التعليقات العائمة)
 */
@Composable
private fun DanmakuFloatingCommentsOverlay(
    comments: List<CommunityComment>,
    isVisible: Boolean,
    onCommentClick: (CommunityComment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible || comments.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "danmaku")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "danmaku_lane1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, delayMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "danmaku_lane2"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Lane 1
        comments.getOrNull(0)?.let { comment ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (animOffset1 * 220).dp)
            ) {
                DanmakuCommentChip(comment = comment, onClick = { onCommentClick(comment) })
            }
        }

        // Lane 2
        comments.getOrNull(1)?.let { comment ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = (animOffset2 * 200).dp)
            ) {
                DanmakuCommentChip(comment = comment, onClick = { onCommentClick(comment) })
            }
        }

        // Lane 3 (if exists)
        comments.getOrNull(2)?.let { comment ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = ((animOffset1 * -180).dp))
            ) {
                DanmakuCommentChip(comment = comment, onClick = { onCommentClick(comment) })
            }
        }
    }
}

@Composable
private fun DanmakuCommentChip(
    comment: CommunityComment,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xD9141824),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x6620D5EC)),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AsyncImage(
                model = comment.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
            )

            Text(
                text = "${comment.author}: ${comment.text}",
                color = Color.White,
                fontSize = 11.5.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Reusable vertical action item with icon and formatted count
 */
@Composable
private fun TikTokActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Distinctive, Innovative Animal Rescue & Video Comment Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RescueCommentsDialog(
    item: TikTokFeedItem,
    isDanmakuEnabled: Boolean = true,
    onToggleDanmaku: () -> Unit = {},
    onDismiss: () -> Unit,
    onUserClick: (name: String, handle: String, avatarUrl: String?, isVerified: Boolean) -> Unit = { _, _, _, _ -> },
    onSendComment: (text: String) -> Unit,
    onToggleLikeComment: (commentId: String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var newCommentText by remember { mutableStateOf("") }

    val rawComments = item.commentsList.ifEmpty {
        listOf(
            CommunityComment(
                id = "c1",
                author = "سارة المنصور",
                handle = "sara_mansoor",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100",
                text = "جزاكم الله كل خير على سرعة التدخل ورعاية الأليف!",
                timestamp = "منذ ساعة",
                likesCount = 18,
                isVerified = true,
                isLikedByMe = true
            ),
            CommunityComment(
                id = "c2",
                author = "د. خالد البيطري",
                handle = "dr_khaled_vet",
                avatarUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=100",
                text = "جاهزون لاستقبال الحالة وإجراء الفحص الشامل والتطعيمات مجاناً في عيادتنا",
                timestamp = "منذ ٤٥ د",
                likesCount = 34,
                isVerified = true,
                isLikedByMe = false
            ),
            CommunityComment(
                id = "c3",
                author = "نورة العتيبي",
                handle = "noura_al",
                avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100",
                text = "أستطيع توفير استضافة مؤقتة في فناء منزلي المجهز، تواصلوا معي فوراً!",
                timestamp = "منذ ٣ س",
                likesCount = 12,
                isVerified = false,
                isLikedByMe = false
            )
        )
    }

    val quickReactions = remember {
        listOf(
            Pair("عمل رائع", Icons.Default.ThumbUp),
            Pair("شكراً لكم", Icons.Default.Favorite),
            Pair("مستعد للتبني", Icons.Default.VolunteerActivism),
            Pair("جاهز للمساعدة", Icons.Default.LocalHospital),
            Pair("مأوى مؤقت", Icons.Default.Home),
            Pair("تمت المشاركة", Icons.Default.Share)
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131620),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Surface(
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(44.dp)
                    .height(4.dp)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp)
        ) {
            // 1. Header: Total Comments Count & Action Icons (Danmaku Toggle + Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${rawComments.size} تعليقات",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Floating Comments Toggle Button beside X
                    IconButton(
                        onClick = {
                            onToggleDanmaku()
                            Toast.makeText(
                                context,
                                if (!isDanmakuEnabled) "تم تفعيل التعليقات العائمة على الفيديو" else "تم إيقاف التعليقات العائمة",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isDanmakuEnabled) Icons.Default.ChatBubble else Icons.Default.ChatBubbleOutline,
                            contentDescription = "التعليقات العائمة على الفيديو",
                            tint = if (isDanmakuEnabled) Color(0xFF20D5EC) else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 10.dp))

            // 2. Comments List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                if (rawComments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text("لا توجد تعليقات بعد", color = Color.LightGray, fontSize = 13.sp)
                                Text("كن أول من يشارك رأيه أو يقدم الدعم لهذا الأليف!", color = Color.Gray, fontSize = 11.5.sp)
                            }
                        }
                    }
                } else {
                    items(rawComments) { comment ->
                        RescueCommentCard(
                            comment = comment,
                            onUserClick = { onUserClick(comment.author, comment.handle, comment.avatarUrl, comment.isVerified) },
                            onToggleLike = { onToggleLikeComment(comment.id) },
                            onReply = {
                                newCommentText = "@${comment.handle} "
                            },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(comment.text))
                                Toast.makeText(context, "تم نسخ نص التعليق", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // 3. Clean Comment Composer (إيموجي الحيوانات + حقل الإدخال وزر الإرسال)
            Surface(
                color = Color(0xFF1A1F2C),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFF283244)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Action Chips Row
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickReactions) { (label, icon) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF222838),
                                border = BorderStroke(1.dp, Color(0xFF2D374A)),
                                modifier = Modifier.clickable {
                                    newCommentText = if (newCommentText.isBlank()) label else "$newCommentText $label"
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color(0xFF20D5EC),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Text Input & Send Button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color(0xFF222838),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF2D374A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            BasicTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                cursorBrush = SolidColor(Color(0xFF20D5EC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                decorationBox = { innerTextField ->
                                    if (newCommentText.isEmpty()) {
                                        Text(
                                            text = "اكتب تعليقك اللطيف للأليف...",
                                            color = Color.Gray,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        // Send Button
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    onSendComment(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            enabled = newCommentText.isNotBlank(),
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (newCommentText.isNotBlank()) {
                                        Brush.linearGradient(listOf(Color(0xFF20D5EC), Color(0xFF00B4D8)))
                                    } else {
                                        SolidColor(Color(0xFF2A3142))
                                    }
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = if (newCommentText.isNotBlank()) Color(0xFF0F172A) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich, Clean Individual Comment Card for Pet Rescue Feed
 */
@Composable
private fun RescueCommentCard(
    comment: CommunityComment,
    onUserClick: () -> Unit,
    onToggleLike: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit
) {
    var isLiked by remember(comment.id, comment.isLikedByMe) { mutableStateOf(comment.isLikedByMe) }
    var likesCount by remember(comment.id, comment.likesCount) { mutableStateOf(comment.likesCount) }

    Surface(
        color = Color(0xFF1A1F2C),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF283244)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Avatar, Name, Verified, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onUserClick() }
                ) {
                    AsyncImage(
                        model = comment.avatarUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100",
                        contentDescription = comment.author,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF20D5EC).copy(alpha = 0.5f), CircleShape)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = comment.author,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (comment.isVerified) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "موثق",
                                    tint = Color(0xFF20D5EC),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = "@${comment.handle} • ${comment.timestamp}",
                            color = Color.Gray,
                            fontSize = 10.5.sp
                        )
                    }
                }
            }

            // Comment Text Content
            Text(
                text = comment.text,
                color = Color(0xFFF1F5F9),
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            // Footer Action Row: Like, Reply, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like button with counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            isLiked = !isLiked
                            likesCount = if (isLiked) likesCount + 1 else (likesCount - 1).coerceAtLeast(0)
                            onToggleLike()
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (isLiked) Color(0xFFFE2C55) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$likesCount",
                        color = if (isLiked) Color(0xFFFE2C55) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reply & Copy Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onReply() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "رد",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "رد",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onCopy() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "نسخ",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * TikTok Style Share Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TikTokShareDialog(
    item: TikTokFeedItem,
    onDismiss: () -> Unit,
    onShareToSocial: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF161823),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "إرسال إلى الأصدقاء",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Social apps share row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data class SocialShareItem(val name: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)
                val socialList = listOf(
                    SocialShareItem("WhatsApp", Icons.Default.Chat, Color(0xFF25D366)),
                    SocialShareItem("Instagram", Icons.Default.PhotoCamera, Color(0xFFE1306C)),
                    SocialShareItem("Snapchat", Icons.Default.ChatBubble, Color(0xFFFFFC00)),
                    SocialShareItem("Twitter / X", Icons.Default.Share, Color(0xFF38BDF8)),
                    SocialShareItem("نسخ الرابط", Icons.Default.Link, Color(0xFF20D5EC))
                )
                socialList.forEach { item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onShareToSocial(item.name) }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = item.color.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, item.color),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name,
                                    tint = item.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.name, color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * TikTok Search Dialog Overlay
 */
@Composable
private fun TikTokSearchDialog(
    onDismiss: () -> Unit,
    onSelectAnimal: (AnimalItem) -> Unit,
    animals: List<AnimalItem>,
    incidents: List<StrayIncident>
) {
    var query by remember { mutableStateOf("") }
    val filteredAnimals = remember(query, animals) {
        if (query.isBlank()) animals else animals.filter {
            it.name.contains(query, true) || it.species.contains(query, true) || it.breed.contains(query, true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF161823)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFF222433),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            BasicTextField(
                                value = query,
                                onValueChange = { query = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (query.isEmpty()) Text("ابحث عن حيوان، سلالة أو ناشر...", color = Color.Gray, fontSize = 13.sp)
                                    inner()
                                }
                            )
                        }
                    }
                    Text(
                        text = "إلغاء",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onDismiss() }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("نتائج البحث والحيوانات المتاحة:", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAnimals) { animal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF222433))
                                .clickable { onSelectAnimal(animal) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = animal.imageUrl,
                                contentDescription = animal.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(animal.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${animal.species} • ${animal.breed}", color = Color.Gray, fontSize = 11.sp)
                            }
                            Surface(
                                color = PrimaryTeal,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        "تبنى",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format numbers for short TikTok style display (e.g. 1.2K, 34.5K, etc.)
 */
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
