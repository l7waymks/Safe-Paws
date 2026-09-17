package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.*
import com.example.ui.theme.PrimaryTeal

enum class UserProfileTab(val icon: androidx.compose.ui.graphics.vector.ImageVector, val description: String) {
    GRID(Icons.Default.GridOn, "الشبكة"),
    RESCUES(Icons.Default.Pets, "حالات الإنقاذ"),
    REELS(Icons.Default.PlayCircleOutline, "ريلز")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    user: UserProfileData,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(UserProfileTab.GRID) }
    var selectedPostDetail by remember { mutableStateOf<ProfilePostItem?>(null) }
    var showDirectMessageDialog by remember { mutableStateOf(false) }
    var activeStoryHighlight by remember { mutableStateOf<StoryHighlight?>(null) }

    // User highlights
    val highlights = remember(user.id) {
        listOf(
            StoryHighlight(
                id = "uh_1",
                title = "عمليات الإنقاذ",
                coverUrl = user.posts.firstOrNull()?.imageUrl ?: user.avatarUrl,
                emoji = "",
                storyImages = listOf(
                    user.posts.firstOrNull()?.imageUrl ?: user.avatarUrl,
                    "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=800"
                )
            ),
            StoryHighlight(
                id = "uh_2",
                title = if (user.isShelter) "يوميات الملجأ" else "رعاية وتغذية",
                coverUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
                emoji = "",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800",
                    "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=800"
                )
            ),
            StoryHighlight(
                id = "uh_3",
                title = "شكر وتقدير",
                coverUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=500",
                emoji = "",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=800"
                )
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .testTag("other_user_profile_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Top App Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(user.handle))
                                Toast.makeText(context, "تم نسخ اسم الحساب: @${user.handle}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "@${user.handle}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (user.isShelter) {
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
                                        contentDescription = "ملجأ",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("ملجأ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                        } else if (user.isVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "موثق",
                                tint = Color(0xFF1DA1F2),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "شاهد الملف الشخصي لـ ${user.name} (@${user.handle}) في تطبيق بيت الأمان:\n${user.bio}\nhttps://safepaws.app/user/${user.handle}"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الملف الشخصي"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. Profile Header Info (Followers on top, Avatar centered, Posts on right, Following on left)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. المتابعون فوق الصورة
                    ProfileStatItem(
                        count = if (user.followersCount >= 1000) String.format(java.util.Locale.US, "%.1fk", user.followersCount / 1000f) else String.format(java.util.Locale.US, "%,d", user.followersCount),
                        label = "المتابعون"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. الصف الأوسط: المنشورات في اليمين | الصورة في الوسط | أتابعه في اليسار
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // اليمين: المنشورات
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileStatItem(
                                count = "${user.posts.size}",
                                label = "المنشورات"
                            )
                        }

                        // الوسط: صورة الملف الشخصي
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFFFBAA47),
                                            Color(0xFFD91A46),
                                            Color(0xFFA60F93),
                                            PrimaryTeal,
                                            Color(0xFFFBAA47)
                                        )
                                    )
                                )
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(2.5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = user.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        // اليسار: أتابعه
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileStatItem(
                                count = "${user.followingCount}",
                                label = "أتابعه"
                            )
                        }
                    }
                }
            }

            // 3. User Name, Category, Bio & Location (Centered & Clean)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Display Name + Badges
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(user.handle))
                                Toast.makeText(context, "تم نسخ اسم الحساب: @${user.handle}", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (user.isShelter) {
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
                                        contentDescription = "ملجأ",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text("ملجأ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                }
                            }
                        } else if (user.isVerified) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "موثق",
                                tint = Color(0xFF1DA1F2),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Role / Category
                    Text(
                        text = user.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 1.dp)
                    )

                    // Bio Text
                    Text(
                        text = user.bio,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Location Tag
                    if (user.location.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "الموقع",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = user.location,
                                fontSize = 12.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 4. Action Buttons (Follow / Following, Message, Share)
            item {
                val isMyProfile = viewModel.isCurrentLoggedInUser(user.handle) || viewModel.isCurrentLoggedInUser(user.name)

                if (isMyProfile) {
                    // Own profile action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.closeUserProfile()
                                viewModel.selectTab(AppTab.Profile)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("edit_my_profile_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "تعديل حسابي الشخصي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Share Icon Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "شاهد حسابي على بيت الأمان:\nhttps://safepaws.app/user/${user.handle}"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "مشاركة",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Follow / Following Button
                        Button(
                            onClick = {
                                viewModel.toggleFollowSelectedUser()
                                val msg = if (!user.isFollowing) "تمت متابعة ${user.name}" else "تم إلغاء المتابعة"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("profile_follow_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isFollowing) MaterialTheme.colorScheme.surfaceVariant else PrimaryTeal,
                                contentColor = if (user.isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            border = if (user.isFollowing) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (user.isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (user.isFollowing) "أتابعه" else "متابعة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Direct Message Button
                        Button(
                            onClick = { showDirectMessageDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("profile_message_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "مراسلة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                        }

                        // Share Icon Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable {
                                    val shareIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "تابع ملف ${user.name} على بيت الأمان:\nhttps://safepaws.app/user/${user.handle}"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة"))
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "مشاركة",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 5. Story Highlights
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(highlights) { highlight ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clickable { activeStoryHighlight = highlight }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, PrimaryTeal, CircleShape)
                                        .padding(3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = highlight.coverUrl,
                                        contentDescription = highlight.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                    )
                                }
                                Text(
                                    text = highlight.title,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 6. Tabs Row (Grid, Rescues, Reels - Icons Only without text)
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = PrimaryTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = PrimaryTeal,
                            height = 2.5.dp
                        )
                    }
                ) {
                    UserProfileTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.height(46.dp),
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.description,
                                    tint = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        )
                    }
                }
            }

            // 7. Tab Content
            when (selectedTab) {
                UserProfileTab.GRID -> {
                    item {
                        if (user.posts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد منشورات حتى الآن", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            val rows = user.posts.chunked(3)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(1.dp),
                                verticalArrangement = Arrangement.spacedBy(1.5.dp)
                            ) {
                                rows.forEach { rowPosts ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
                                    ) {
                                        rowPosts.forEach { post ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clickable { selectedPostDetail = post }
                                            ) {
                                                AsyncImage(
                                                    model = post.imageUrl,
                                                    contentDescription = post.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (post.type == "rescue") {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = Color.Black.copy(alpha = 0.6f),
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(4.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Pets,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(10.dp)
                                                            )
                                                            Text(
                                                                text = "إنقاذ",
                                                                color = Color.White,
                                                                fontSize = 9.sp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        // Fill remaining slots in row to keep grid aspect ratio
                                        for (i in 0 until (3 - rowPosts.size)) {
                                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                UserProfileTab.RESCUES -> {
                    val rescuePosts = user.posts.filter { it.type == "rescue" || it.id.startsWith("s") || it.id.startsWith("p_") }
                    if (rescuePosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد حالات إنقاذ مسجلة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(rescuePosts) { post ->
                            UserRescueCard(
                                post = post,
                                onClick = { selectedPostDetail = post }
                            )
                        }
                    }
                }

                UserProfileTab.REELS -> {
                    item {
                        val reels = user.posts.filter { it.type == "video" || true }
                        val rows = reels.chunked(3)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(1.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            rows.forEach { rowPosts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    rowPosts.forEach { post ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(0.65f)
                                                .clickable { selectedPostDetail = post }
                                        ) {
                                            AsyncImage(
                                                model = post.imageUrl,
                                                contentDescription = post.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                        )
                                                    )
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "${post.likesCount * 3}",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    for (i in 0 until (3 - rowPosts.size)) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(0.65f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        // 1. Post Detail Dialog
        selectedPostDetail?.let { post ->
            UserPostDetailDialog(
                post = post,
                userAvatarUrl = user.avatarUrl,
                onDismiss = { selectedPostDetail = null }
            )
        }

        // 2. Direct Message Dialog
        if (showDirectMessageDialog) {
            UserDirectMessageDialog(
                user = user,
                viewModel = viewModel,
                onDismiss = { showDirectMessageDialog = false }
            )
        }

        // 3. Story Viewer Dialog
        activeStoryHighlight?.let { highlight ->
            ClassicStoryViewerDialog(
                highlight = highlight,
                onDismiss = { activeStoryHighlight = null }
            )
        }
    }
}

@Composable
private fun ProfileStatItem(count: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserRescueCard(
    post: ProfilePostItem,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PrimaryTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = post.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = post.dateOrLikes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = post.details,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = post.location,
                        fontSize = 11.sp,
                        color = PrimaryTeal
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFFE2C55),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(text = "${post.likesCount}", fontSize = 11.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(text = "${post.commentsCount}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserPostDetailDialog(
    post: ProfilePostItem,
    userAvatarUrl: String,
    onDismiss: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = post.category,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Post Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Details Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = post.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = post.details,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = post.location,
                                fontSize = 12.sp,
                                color = PrimaryTeal
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Like & Share row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable {
                                    isLiked = !isLiked
                                    likesCount = if (isLiked) likesCount + 1 else likesCount - 1
                                }
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "إعجاب",
                                    tint = if (isLiked) Color(0xFFFE2C55) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$likesCount إعجاب",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = post.dateOrLikes,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserDirectMessageDialog(
    user: UserProfileData,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var typedMessage by remember { mutableStateOf("") }
    val selectedUser by viewModel.selectedUserProfile.collectAsState()
    val messages = selectedUser?.directMessages ?: user.directMessages

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
            ) {
                // Chat Header
                Surface(
                    shadowElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع"
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column {
                                Text(
                                    text = user.name,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                    Text(
                                        text = "متصل الآن",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E676)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }

                // Messages list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (msg.isUser) 14.dp else 2.dp,
                                    bottomEnd = if (msg.isUser) 2.dp else 14.dp
                                ),
                                color = if (msg.isUser) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.5.sp,
                                        lineHeight = 19.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.timestamp,
                                        color = if (msg.isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.align(if (msg.isUser) Alignment.End else Alignment.Start)
                                    )
                                }
                            }
                        }
                    }
                }

                // Input Bar
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.weight(1f)
                        ) {
                            BasicTextField(
                                value = typedMessage,
                                onValueChange = { typedMessage = it },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                decorationBox = { innerTextField ->
                                    if (typedMessage.isBlank()) {
                                        Text(
                                            text = "اكتب رسالتك لـ ${user.name}...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        IconButton(
                            onClick = {
                                if (typedMessage.isNotBlank()) {
                                    viewModel.sendDirectMessageToSelectedUser(typedMessage)
                                    typedMessage = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "إرسال",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
