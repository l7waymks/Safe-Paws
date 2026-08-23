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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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

// Data Models
data class ProfilePostItem(
    val id: String,
    val title: String,
    val category: String,
    val type: String, // "post", "rescue", "video", "saved"
    val dateOrLikes: String,
    val details: String,
    val imageUrl: String,
    val location: String = "الرياض، المملكة العربية السعودية",
    val likesCount: Int = 124,
    val commentsCount: Int = 18,
    val reporter: String = "أحمد محمد"
)

data class StoryHighlight(
    val id: String,
    val title: String,
    val coverUrl: String,
    val emoji: String,
    val storyImages: List<String> = emptyList()
)

enum class OldProfileTab(val title: String, val icon: ImageVector) {
    GRID("الشبكة", Icons.Default.GridOn),
    RESCUES("الإنقاذ 🐾", Icons.Default.Pets),
    REELS("ريلز 🎥", Icons.Default.PlayCircleOutline),
    SAVED("المحفوظات 🔖", Icons.Default.BookmarkBorder),
    TAGGED("الإشارات 🏷️", Icons.Default.AccountBox)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val trustScore by viewModel.trustScore.collectAsState()
    val rescuesCount by viewModel.rescuesCount.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()
    val incidents by viewModel.strayIncidents.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf(OldProfileTab.GRID) }

    // Dialog States
    var showChangePhotoDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showTrustScoreDialog by remember { mutableStateOf(false) }
    var activeStoryHighlight by remember { mutableStateOf<StoryHighlight?>(null) }
    var selectedPostDetail by remember { mutableStateOf<ProfilePostItem?>(null) }

    // User Profile Editable Data
    var userName by rememberSaveable { mutableStateOf("أحمد محمد 🐾") }
    var userHandle by rememberSaveable { mutableStateOf("ahmed_rescues") }
    var userCategory by rememberSaveable { mutableStateOf("منقذ ومتطوع معتمد | رعاية وإنقاذ 🌿") }
    var userBio by rememberSaveable { mutableStateOf("🐾 شغوف بإنقاذ ورعاية الحيوانات الأليفة والحالات الطارئة بالرياض 🌧️\nمؤسس مبادرة 'طعام الشتاء' 🥣 | معاً لبيئة أكثر رحمة وأماناً 💚") }
    var userLink by rememberSaveable { mutableStateOf("safepaws.app/ahmed") }
    var userLocation by rememberSaveable { mutableStateOf("الرياض، المملكة العربية السعودية") }
    var userPhone by rememberSaveable { mutableStateOf("+966 50 123 4567") }

    // Stats
    val followersCount = 1420
    val followingCount = 286

    // Highlights List
    val highlights = remember {
        listOf(
            StoryHighlight(
                id = "h1",
                title = "إنقاذ لونا 🐱",
                coverUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
                emoji = "🐱",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800",
                    "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=800"
                )
            ),
            StoryHighlight(
                id = "h2",
                title = "طعام الشتاء 🌧️",
                coverUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=500",
                emoji = "🌧️",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=800",
                    "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=800"
                )
            ),
            StoryHighlight(
                id = "h3",
                title = "بوبي البطل 🐶",
                coverUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500",
                emoji = "🐶",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800",
                    "https://images.unsplash.com/photo-1552053831-71594a27632d?w=800"
                )
            ),
            StoryHighlight(
                id = "h4",
                title = "يوم التطوع 🏥",
                coverUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=500",
                emoji = "🏥",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=800"
                )
            ),
            StoryHighlight(
                id = "h5",
                title = "أوسمة وتكريم 🏆",
                coverUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=500",
                emoji = "🏆",
                storyImages = listOf(
                    "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=800"
                )
            )
        )
    }

    // Posts Grid & Details
    val allPosts = remember {
        listOf(
            ProfilePostItem(
                id = "post_1",
                title = "لونا بعد ٣ أشهر من التعافي الكامل 🐱",
                category = "إنقاذ وتأهيل",
                type = "rescue",
                dateOrLikes = "منذ يومين",
                details = "وجدتها في ليلة باردة تحت المطر تختبئ خلف صندوق القمامة. كانت جائعة وخائفة جداً. قمت بإحضارها للبيت وتدفئتها، والآن هي أميرة المنزل تنام بجانبي دائماً وتملأ حياتي بالبهجة واللعب.",
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=600",
                location = "حي الشرق، الرياض",
                likesCount = 342,
                commentsCount = 45
            ),
            ProfilePostItem(
                id = "post_2",
                title = "بوبي الشجاع يركض لأول مرة بعد العملية 🐶",
                category = "حالة تعافي",
                type = "rescue",
                dateOrLikes = "منذ أسبوع",
                details = "عثرت عليه مصاباً في قدمه الخلفية بسبب حادث سيارة. بمساعدة عيادة شريكة، تم إجراء عملية جراحية له وتركيب شريحة. اليوم بوبي يركض ويمرح بكل حرية وصار صديق العائلة الوفي.",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                location = "حي السليمانية، الرياض",
                likesCount = 512,
                commentsCount = 89
            ),
            ProfilePostItem(
                id = "post_3",
                title = "ميلو الصغير في لحظات اللعب الأولى 🐾",
                category = "رعاية صغار",
                type = "post",
                dateOrLikes = "منذ أسبوعين",
                details = "هر صغير يبلغ من العمر أسابيع قليلة فقط، فقد أمه في الشارع. قمت بإرضاعه بالحليب الصناعي المخصص للقطط كل ٣ ساعات حتى كبُر وتخطى مرحلة الخطر. هو الآن مشاكس ومحب للمرح للغاية.",
                imageUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600",
                location = "حي الصحافة، الرياض",
                likesCount = 280,
                commentsCount = 32
            ),
            ProfilePostItem(
                id = "post_4",
                title = "ماكس البطل في نزهته الصباحية 🐕",
                category = "صديق دائم",
                type = "post",
                dateOrLikes = "منذ شهر",
                details = "كلب ضائع تعرض للجفاف الشديد في الصيف. قمت بتوفير الماء والطعام الرطب له بشكل مستمر حتى استعاد طاقته، وتعلّق بي تماماً ليصبح حارس المنزل والرفيق المخلص في نزهاتي الصباحية.",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=600",
                location = "طريق الملك فهد، الرياض",
                likesCount = 415,
                commentsCount = 56
            ),
            ProfilePostItem(
                id = "post_5",
                title = "ليو الرمادي بعد إنقاذه من محرك السيارة 🐈",
                category = "إنقاذ طارئ",
                type = "rescue",
                dateOrLikes = "منذ شهرين",
                details = "عالق داخل محرك سيارة جاري! سمعنا مواءه الصعب وقضينا ساعتين كاملتين لفك الأجزاء وإنقاذه بسلام دون أي خدش. قمنا بتنظيفه وعلاجه من الفطريات والآن هو أهدأ قط في الكون.",
                imageUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=600",
                location = "حي النرجس، الرياض",
                likesCount = 620,
                commentsCount = 94
            ),
            ProfilePostItem(
                id = "post_6",
                title = "كيكي المغرد بعد اكتمال نمو ريشه 🦜",
                category = "رعاية طيور",
                type = "post",
                dateOrLikes = "منذ شهرين",
                details = "ببغاء صغير سقط من عشه وكان معرضاً لهجوم القطط. اعتنيت به وغذيته بالحبوب اللينة حتى نبت ريشه وتعلّم الطيران. يطلق الآن صفارات ترحيبية جميلة كلما دخلت الغرفة.",
                imageUrl = "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=600",
                location = "حي الياسمين، الرياض",
                likesCount = 195,
                commentsCount = 21
            ),
            ProfilePostItem(
                id = "post_7",
                title = "أوسكار الجرو المرح مع لعبته المفضلة 🐕‍🦺",
                category = "صداقة جديدة",
                type = "post",
                dateOrLikes = "منذ ٣ أشهر",
                details = "جرو ذو عينين حزينتين وجدته وحيداً في الشارع. بعد تنظيفه وتقديم التطعيمات اللازمة، أظهر حباً غير مشروط للجميع، ولديه طاقة مذهلة للعب بالكرة وتعديل مزاجي اليومي.",
                imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600",
                location = "حي العقيق، الرياض",
                likesCount = 380,
                commentsCount = 48
            ),
            ProfilePostItem(
                id = "post_8",
                title = "بيلا الشيرازية الجميلة تبحث عن أسرة دافئة 😻",
                category = "تبني معتمد",
                type = "rescue",
                dateOrLikes = "منذ ٤ أشهر",
                details = "قطة شيرازية بيضاء تخلى عنها أصحابها في الشارع وكانت تعاني من مشاكل هضمية وخوف شديد. مع نظام غذائي متكامل وصبر طويل، استعادت ثقتها بالبشر وصارت فرواً ناعماً من الحب.",
                imageUrl = "https://images.unsplash.com/photo-1533743983669-94fa5c4338ec?w=600",
                location = "حي الملقا، الرياض",
                likesCount = 490,
                commentsCount = 67
            ),
            ProfilePostItem(
                id = "post_9",
                title = "روكي حارس الحي وصديقي الوفي 🐕",
                category = "حراسة وألفة",
                type = "post",
                dateOrLikes = "منذ ٥ أشهر",
                details = "كلب بلدي ذكي جداً، كان يحمي زقاق الشارع. بعد أن ألِفني، صار ينتظرني يومياً عند عودتي من العمل ليرافقني حتى باب المنزل. قصة وفاء عظيمة تظهر مدى رحمة الحيوانات الضالة.",
                imageUrl = "https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=600",
                location = "طريق أنس بن مالك، الرياض",
                likesCount = 530,
                commentsCount = 72
            )
        )
    }

    val reelsPosts = remember {
        listOf(
            ProfilePostItem(
                id = "reel_1",
                title = "توزيع وجبات طعام الشتاء في ليلة ماطرة بالرياض 🌧️",
                category = "مقطع ريلز",
                type = "video",
                dateOrLikes = "٣.٢ ألف مشاهدة",
                details = "فيديو توثيقي لمبادرة طعام الشتاء وتوزيع أكثر من ٥٠ وجبة دافئة ومياه نظيفة لقطط وكلاب الشوارع في ليلة شتوية باردة.",
                imageUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=600",
                likesCount = 980,
                commentsCount = 142
            ),
            ProfilePostItem(
                id = "reel_2",
                title = "لحظة إنقاذ بوبي ونقله لسيارة الإسعاف 🚑",
                category = "مقطع ريلز",
                type = "video",
                dateOrLikes = "٥.٨ ألف مشاهدة",
                details = "توثيق للحظات الاستجابة السريعة للبلاغ الطارئ وتهدئة الكلب المصاب ونقله بحذر لتلقي الإسعافات الأولية.",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                likesCount = 1420,
                commentsCount = 210
            ),
            ProfilePostItem(
                id = "reel_3",
                title = "ميلو يتسلق الشجرة لأول مرة 🌳🐾",
                category = "مقطع ريلز",
                type = "video",
                dateOrLikes = "٢.١ ألف مشاهدة",
                details = "لقطات طريفة لميلو وهو يكتشف الحديقة ويتدرب على القفز واللعب بين الأشجار.",
                imageUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=600",
                likesCount = 640,
                commentsCount = 88
            )
        )
    }

    val savedPlaces = remember {
        listOf(
            ProfilePostItem(
                id = "saved_1",
                title = "عيادة الرياض البيطرية المتقدمة (طوارئ ٢٤/٧)",
                category = "عيادة بيطرية",
                type = "saved",
                dateOrLikes = "تقييم ٤.٩ ⭐",
                details = "عيادة بيطرية متكاملة مجهزة بأحدث التقنيات لتقديم الرعاية الطبية الفائقة والجراحات الطارئة والتحاليل المخبرية للحيوانات الأليفة.",
                imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=600",
                location = "حي السليمانية، الرياض",
                likesCount = 120,
                commentsCount = 14
            ),
            ProfilePostItem(
                id = "saved_2",
                title = "ملجأ أليف لرعاية وتأهيل الكلاب الضالة",
                category = "ملجأ معتمد",
                type = "saved",
                dateOrLikes = "تقييم ٤.٨ ⭐",
                details = "ملجأ غير ربحي يهدف لإنقاذ وإيواء الكلاب الضالة والمصابة بالرياض، وتأهيلها طبياً وسلوكياً قبل عرضها للتبني.",
                imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=600",
                location = "حي النرجس، الرياض",
                likesCount = 95,
                commentsCount = 8
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("old_profile_screen_scroll"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Classic Instagram Top Header App Bar
            item {
                ClassicProfileTopBar(
                    handle = userHandle,
                    trustScore = trustScore,
                    onQrClick = { showQrDialog = true },
                    onMenuClick = { showSettingsSheet = true }
                )
            }

            // 2. Profile Header Info (Avatar + 3 Stats Columns)
            item {
                ClassicProfileHeaderInfo(
                    avatarUrl = profileImageUrl,
                    postsCount = allPosts.size,
                    followersCount = followersCount,
                    followingCount = followingCount,
                    trustScore = trustScore,
                    onAvatarClick = { showChangePhotoDialog = true },
                    onTrustScoreClick = { showTrustScoreDialog = true }
                )
            }

            // 3. User Bio, Category & Link
            item {
                ClassicProfileBioSection(
                    name = userName,
                    category = userCategory,
                    bio = userBio,
                    link = userLink,
                    onEditClick = { showEditProfileDialog = true },
                    onShareClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "تابع ملف $userName في بيت الأمان:\n🛡️ نقاط الثقة: $trustScore\n🐾 عدد الإنقاذات: $rescuesCount\nhttps://$userLink"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الملف الشخصي"))
                    }
                )
            }

            // 4. Story Highlights Bar
            item {
                ClassicStoryHighlightsBar(
                    highlights = highlights,
                    onHighlightClick = { activeStoryHighlight = it },
                    onAddClick = {
                        Toast.makeText(context, "إضافة قصة مميزة جديدة 📸", Toast.LENGTH_SHORT).show()
                    }
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            // 5. Classic Instagram Tabs (Grid, Rescues, Reels, Saved)
            item {
                ClassicInstagramTabsBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            // 6. Dynamic Tab Content
            when (selectedTab) {
                OldProfileTab.GRID -> {
                    // Instagram 3-column Grid
                    item {
                        ClassicPostsGrid3x3(
                            posts = allPosts,
                            onPostClick = { selectedPostDetail = it }
                        )
                    }
                }

                OldProfileTab.RESCUES -> {
                    // Detailed Rescue Cards
                    items(allPosts.filter { it.type == "rescue" || it.id.startsWith("post_") }) { rescueItem ->
                        ClassicRescueFeedCard(
                            post = rescueItem,
                            onClick = { selectedPostDetail = rescueItem }
                        )
                    }
                }

                OldProfileTab.REELS -> {
                    // Reels 3-column vertical Grid
                    item {
                        ClassicReelsGrid(
                            reels = reelsPosts,
                            onReelClick = { selectedPostDetail = it }
                        )
                    }
                }

                OldProfileTab.SAVED -> {
                    // Saved Items List
                    items(savedPlaces) { savedItem ->
                        ClassicSavedPlaceCard(
                            post = savedItem,
                            onClick = { selectedPostDetail = savedItem }
                        )
                    }
                }

                OldProfileTab.TAGGED -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Text(
                                    text = "الصور والإشارات المشتركة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "عندما يذكرك أحد المنقذين أو الملاجئ في منشوراتهم ستظهر هنا",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= DIALOGS =================

        // 1. Story Highlight Viewer
        activeStoryHighlight?.let { highlight ->
            ClassicStoryViewerDialog(
                highlight = highlight,
                onDismiss = { activeStoryHighlight = null }
            )
        }

        // 2. Post Detail Dialog
        selectedPostDetail?.let { post ->
            ClassicPostDetailDialog(
                post = post,
                userAvatarUrl = profileImageUrl,
                onDismiss = { selectedPostDetail = null }
            )
        }

        // 3. Edit Profile Dialog
        if (showEditProfileDialog) {
            ClassicEditProfileDialog(
                initialName = userName,
                initialHandle = userHandle,
                initialBio = userBio,
                initialLink = userLink,
                initialLocation = userLocation,
                initialPhone = userPhone,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newName, newHandle, newBio, newLink, newLoc, newPhone ->
                    userName = newName
                    userHandle = newHandle
                    userBio = newBio
                    userLink = newLink
                    userLocation = newLoc
                    userPhone = newPhone
                    showEditProfileDialog = false
                    Toast.makeText(context, "تم تحديث الملف الشخصي بنجاح 💾", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 4. Change Photo Dialog
        if (showChangePhotoDialog) {
            ClassicChangePhotoDialog(
                currentUrl = profileImageUrl,
                onDismiss = { showChangePhotoDialog = false },
                onSelectUrl = { newUrl ->
                    viewModel.updateProfileImageUrl(newUrl)
                    showChangePhotoDialog = false
                    Toast.makeText(context, "تم تغيير الصورة الشخصية بنجاح 📸", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 5. QR Code Card Dialog
        if (showQrDialog) {
            ClassicQrDialog(
                name = userName,
                handle = userHandle,
                trustScore = trustScore,
                rescuesCount = rescuesCount,
                onDismiss = { showQrDialog = false }
            )
        }

        // 6. Trust Score Info Dialog
        if (showTrustScoreDialog) {
            ClassicTrustScoreInfoDialog(
                trustScore = trustScore,
                rescuesCount = rescuesCount,
                onDismiss = { showTrustScoreDialog = false }
            )
        }

        // 7. Settings Bottom Sheet
        if (showSettingsSheet) {
            ClassicSettingsSheet(
                userName = userName,
                userHandle = userHandle,
                onDismiss = { showSettingsSheet = false },
                onEditProfile = {
                    showSettingsSheet = false
                    showEditProfileDialog = true
                },
                onChangePhoto = {
                    showSettingsSheet = false
                    showChangePhotoDialog = true
                },
                onShowQr = {
                    showSettingsSheet = false
                    showQrDialog = true
                },
                onOpenAiChat = {
                    showSettingsSheet = false
                    viewModel.showExpertChat.value = true
                }
            )
        }
    }
}

// ----------------------------------------------------
// 1. Classic Top Header App Bar
// ----------------------------------------------------
@Composable
private fun ClassicProfileTopBar(
    handle: String,
    trustScore: Int,
    onQrClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
            // Username with Lock/Verified icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = handle,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "موثق",
                    tint = Color(0xFF1DA1F2),
                    modifier = Modifier.size(17.dp)
                )
            }

            // Right Action Icons (Trust Score badge, QR, Menu)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. Profile Header Info (Avatar + 3 Stat Columns)
// ----------------------------------------------------
@Composable
private fun ClassicProfileHeaderInfo(
    avatarUrl: String,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    trustScore: Int,
    onAvatarClick: () -> Unit,
    onTrustScoreClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Story Gradient Ring + Avatar
        Box(
            modifier = Modifier
                .size(86.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFBAA47),
                            Color(0xFFD91A46),
                            Color(0xFFA60F93),
                            MaterialTheme.colorScheme.primary,
                            Color(0xFFFBAA47)
                        )
                    )
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .padding(2.5.dp)
                .clickable { onAvatarClick() }
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "الصورة الشخصية",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )

            // Plus icon for story / photo change
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "تغيير",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // Stats Row (Posts, Followers, Following)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClassicStatColumn(count = "$postsCount", label = "المنشورات")
            ClassicStatColumn(count = "$followersCount", label = "المتابعون")
            ClassicStatColumn(count = "$followingCount", label = "أتابعه")
            ClassicStatColumn(
                count = "$trustScore",
                label = "الثقة 🛡️",
                highlight = true,
                onClick = onTrustScoreClick
            )
        }
    }
}

@Composable
private fun ClassicStatColumn(
    count: String,
    label: String,
    highlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.Black,
            fontSize = 17.sp,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ----------------------------------------------------
// 3. User Bio & Action Buttons
// ----------------------------------------------------
@Composable
private fun ClassicProfileBioSection(
    name: String,
    category: String,
    bio: String,
    link: String,
    onEditClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Name
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Category
        Text(
            text = category,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 1.dp)
        )

        // Bio
        Text(
            text = bio,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )

        // Link
        if (link.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = link,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row (Edit Profile, Share Profile)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "تعديل الملف الشخصي",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onShareClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "مشاركة الملف",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ----------------------------------------------------
// 4. Story Highlights Bar
// ----------------------------------------------------
@Composable
private fun ClassicStoryHighlightsBar(
    highlights: List<StoryHighlight>,
    onHighlightClick: (StoryHighlight) -> Unit,
    onAddClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Add new Highlight circle
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onAddClick() }
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "جديد",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "جديد",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Highlight items
        items(highlights, key = { it.id }) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { onHighlightClick(item) }
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(1.2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                ) {
                    AsyncImage(
                        model = item.coverUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Text(
                    text = item.title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ----------------------------------------------------
// 5. Classic Instagram Tabs Bar (Grid, Rescues, Reels, Saved)
// ----------------------------------------------------
@Composable
private fun ClassicInstagramTabsBar(
    selectedTab: OldProfileTab,
    onTabSelected: (OldProfileTab) -> Unit
) {
    val tabs = OldProfileTab.values()
    val selectedIndex = tabs.indexOf(selectedTab)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedIndex])
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                    thickness = 0.8.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.height(44.dp),
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                )
            }
        }
    }
}

// ----------------------------------------------------
// 6. Classic 3x3 Photo Grid
// ----------------------------------------------------
@Composable
private fun ClassicPostsGrid3x3(
    posts: List<ProfilePostItem>,
    onPostClick: (ProfilePostItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
    ) {
        val rows = posts.chunked(3)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp)
            ) {
                rowItems.forEach { post ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable { onPostClick(post) }
                    ) {
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = post.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Multi-photo or Rescue Icon overlay
                        if (post.type == "rescue") {
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                // Fill empty slots if last row has less than 3
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(1.5.dp))
        }
    }
}

// ----------------------------------------------------
// 7. Classic Rescue Feed Card
// ----------------------------------------------------
@Composable
private fun ClassicRescueFeedCard(
    post: ProfilePostItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = post.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = post.location,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = post.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
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

            Text(
                text = post.details,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE91E63),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${post.likesCount}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${post.commentsCount}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = post.dateOrLikes,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ----------------------------------------------------
// 8. Classic Reels 3-Column Grid
// ----------------------------------------------------
@Composable
private fun ClassicReelsGrid(
    reels: List<ProfilePostItem>,
    onReelClick: (ProfilePostItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
    ) {
        val rows = reels.chunked(3)
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp)
            ) {
                rowItems.forEach { reel ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.65f)
                            .clickable { onReelClick(reel) }
                    ) {
                        AsyncImage(
                            model = reel.imageUrl,
                            contentDescription = reel.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark gradient overlay on bottom
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                        startY = 100f
                                    )
                                )
                        )

                        // Views count
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
                                text = reel.dateOrLikes,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (rowItems.size < 3) {
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(1.5.dp))
        }
    }
}

// ----------------------------------------------------
// 9. Classic Saved Place Card
// ----------------------------------------------------
@Composable
private fun ClassicSavedPlaceCard(
    post: ProfilePostItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = post.location,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = post.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = post.dateOrLikes,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "محفوظ",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ----------------------------------------------------
// 10. Classic Story Viewer Dialog
// ----------------------------------------------------
@Composable
private fun ClassicStoryViewerDialog(
    highlight: StoryHighlight,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var currentStoryIndex by remember { mutableStateOf(0) }
        val stories = if (highlight.storyImages.isNotEmpty()) highlight.storyImages else listOf(highlight.coverUrl)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Story Image
            AsyncImage(
                model = stories[currentStoryIndex],
                contentDescription = highlight.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (currentStoryIndex < stories.size - 1) {
                            currentStoryIndex++
                        } else {
                            onDismiss()
                        }
                    }
            )

            // Top Header (Progress bars + Story title + Close)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                // Segmented Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    stories.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (index <= currentStoryIndex) Color.White else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = highlight.coverUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            text = highlight.title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 11. Post Detail Dialog
// ----------------------------------------------------
@Composable
private fun ClassicPostDetailDialog(
    post: ProfilePostItem,
    userAvatarUrl: String,
    onDismiss: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(post.likesCount) }
    var comments by remember {
        mutableStateOf(
            listOf(
                "ما شاء الله تبارك الله، عمل إنساني عظيم 👏💚",
                "الله يجزاك كل خير ويكتب أجرك في إنقاذ هذه الأرواح 🐾",
                "لونا صارت تجنن وصحتها ممتازة جداً ✨"
            )
        )
    }
    var commentInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                        ) {
                            AsyncImage(
                                model = userAvatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text(
                                text = post.reporter,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = post.location,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            AsyncImage(
                                model = post.imageUrl,
                                contentDescription = post.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Action Icons
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        isLiked = !isLiked
                                        likesCount += if (isLiked) 1 else -1
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "إعجاب",
                                        tint = if (isLiked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "تعليق",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "مشاركة",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = "حفظ",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Likes count
                    item {
                        Text(
                            text = "$likesCount إعجاب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Post details
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text(
                                text = post.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.details,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Comments List Header
                    item {
                        Text(
                            text = "التعليقات (${comments.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    // Comments items
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🐾", fontSize = 11.sp)
                            }
                            Text(
                                text = comment,
                                fontSize = 12.5.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Add Comment Input Row
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BasicTextField(
                            value = commentInput,
                            onValueChange = { commentInput = it },
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            decorationBox = { innerTextField ->
                                if (commentInput.isEmpty()) {
                                    Text(
                                        text = "أضف تعليقاً لطيفاً...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        TextButton(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    comments = comments + commentInput.trim()
                                    commentInput = ""
                                }
                            },
                            enabled = commentInput.isNotBlank()
                        ) {
                            Text(
                                text = "نشر",
                                fontWeight = FontWeight.Bold,
                                color = if (commentInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 12. Edit Profile Dialog
// ----------------------------------------------------
@Composable
private fun ClassicEditProfileDialog(
    initialName: String,
    initialHandle: String,
    initialBio: String,
    initialLink: String,
    initialLocation: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var handle by remember { mutableStateOf(initialHandle) }
    var bio by remember { mutableStateOf(initialBio) }
    var link by remember { mutableStateOf(initialLink) }
    var location by remember { mutableStateOf(initialLocation) }
    var phone by remember { mutableStateOf(initialPhone) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "تعديل الملف الشخصي",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("الاسم الكامل") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = handle,
                            onValueChange = { handle = it },
                            label = { Text("اسم المستخدم / المعرف") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("النبذة التعريفية (Bio)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = link,
                            onValueChange = { link = it },
                            label = { Text("الرابط أو الموقع") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("الموقع والمدينة") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم التواصل للطوارئ") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        onSave(name, handle, bio, link, location, phone)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "حفظ التغييرات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 13. Change Photo Dialog
// ----------------------------------------------------
@Composable
private fun ClassicChangePhotoDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSelectUrl: (String) -> Unit
) {
    var customUrl by remember { mutableStateOf("") }
    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "تغيير الصورة الشخصية",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "اختر من النماذج المقترحة أو أدخل رابط صورة مباشرة:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    avatarPresets.forEach { presetUrl ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    if (presetUrl == currentUrl) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { onSelectUrl(presetUrl) }
                        ) {
                            AsyncImage(
                                model = presetUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("رابط صورة مخصص (URL)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            if (customUrl.isNotBlank()) {
                                onSelectUrl(customUrl.trim())
                            }
                        },
                        enabled = customUrl.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("تطبيق", color = Color.White)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 14. Classic QR Card Dialog
// ----------------------------------------------------
@Composable
private fun ClassicQrDialog(
    name: String,
    handle: String,
    trustScore: Int,
    rescuesCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "بطاقة المتطوع الرقمية 🪪",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Simulated QR Graphic
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF0FDF4))
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(110.dp)
                        )
                        Text(
                            text = "@$handle",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "نقاط الثقة: $trustScore/100 🛡️ | حالات الإنقاذ: $rescuesCount 🐾",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("تم", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 15. Trust Score Explainer Dialog
// ----------------------------------------------------
@Composable
private fun ClassicTrustScoreInfoDialog(
    trustScore: Int,
    rescuesCount: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "نقاط الثقة المجتمعية 🛡️",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = "رصيدك الحالي هو $trustScore نقطة، وهو يمثل موثوقية نشاطك في مجتمع بيت الأمان.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "• +5 نقاط عند نشر بلاغ إنقاذ مؤكد مع صورة وموقع.\n• +10 نقاط عند إتمام وإنقاذ حالة طارئة.\n• +15 نقطة عند نجاح تبني بإشراف ملجأ معتمد.\n• توثيق رسمي للحسابات التي تتجاوز 80 نقطة.",
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("فهمت ذلك", color = Color.White)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 16. Settings Bottom Sheet
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClassicSettingsSheet(
    userName: String,
    userHandle: String,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePhoto: () -> Unit,
    onShowQr: () -> Unit,
    onOpenAiChat: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "إعدادات الحساب ⚙️",
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            SettingsItemRow(
                icon = Icons.Default.MedicalServices,
                title = "المستشار البيطري 🐾",
                onClick = onOpenAiChat
            )
            SettingsItemRow(
                icon = Icons.Default.Edit,
                title = "تعديل الملف الشخصي",
                onClick = onEditProfile
            )
            SettingsItemRow(
                icon = Icons.Default.CameraAlt,
                title = "تغيير الصورة الشخصية",
                onClick = onChangePhoto
            )
            SettingsItemRow(
                icon = Icons.Default.QrCode,
                title = "بطاقة ورمز QR",
                onClick = onShowQr
            )
            SettingsItemRow(
                icon = Icons.Default.Notifications,
                title = "إشعارات الطوارئ والبلاغات",
                onClick = onDismiss
            )
            SettingsItemRow(
                icon = Icons.Default.Security,
                title = "الخصوصية والأمان",
                onClick = onDismiss
            )
            SettingsItemRow(
                icon = Icons.Default.HelpOutline,
                title = "المساعدة والدعم الفني",
                onClick = onDismiss
            )
            SettingsItemRow(
                icon = Icons.Default.Logout,
                title = "تسجيل الخروج",
                isDestructive = true,
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}
