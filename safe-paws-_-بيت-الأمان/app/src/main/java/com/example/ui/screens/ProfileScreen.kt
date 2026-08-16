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

data class ProfileDetailItem(
    val id: String,
    val title: String,
    val category: String,
    val type: String, // "clinic", "shelter", "shop", "stray", "adoption", "discussion"
    val dateOrLikes: String,
    val details: String,
    val imageUrl: String,
    val location: String = "",
    val phone: String = "",
    val rating: String = "",
    val reporter: String = ""
)

data class ProfileVideoItem(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val views: String,
    val videoThumbnailUrl: String,
    val category: String = "فيديو قصير"
)

// ---------------------- 6. SMART PROFILE SCREEN (SCREENSHOT 1) ----------------------
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val trustScore by viewModel.trustScore.collectAsState()
    val rescuesCount by viewModel.rescuesCount.collectAsState()
    val profileImageUrl by viewModel.profileImageUrl.collectAsState()

    val scrollState = rememberScrollState()

    // State to toggle the profile options menu dialog
    var showProfileMenu by remember { mutableStateOf(false) }
    // State to track which detailed view is open in the menu ("options", "info", "saved", "liked")
    var menuDetailSection by remember { mutableStateOf("options") }
    // State to track selected post detail
    var selectedProfileDetailItem by remember { mutableStateOf<ProfileDetailItem?>(null) }
    
    // Custom dialogs triggers
    var showChangePhotoDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }

    var selectedProfileTab by remember { mutableStateOf("photos") }
    var selectedPhotoDetail by remember { mutableStateOf<ProfileDetailItem?>(null) }
    var selectedVideoDetail by remember { mutableStateOf<ProfileVideoItem?>(null) }

    val profilePhotos = remember {
        listOf(
            ProfileDetailItem(
                id = "p1",
                title = "لونا اللطيفة 🐱",
                category = "إنقاذ ناجح",
                type = "adoption",
                dateOrLikes = "١٥ يونيو ٢٠٢٤",
                details = "وجدتها في ليلة باردة تحت المطر تختبئ خلف صندوق القمامة. كانت جائعة وخائفة جداً. قمت بإحضارها للبيت وتدفئتها، والآن هي أميرة المنزل تنام بجانبي دائماً وتملأ حياتي بالبهجة واللعب.",
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
                location = "حي الشرق، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p2",
                title = "بوبي الشجاع 🐶",
                category = "حالة تعافي",
                type = "stray",
                dateOrLikes = "٢٢ مايو ٢٠٢٤",
                details = "عثرت عليه مصاباً في قدمه الخلفية بسبب حادث سيارة. بمساعدة عيادة شريكة، تم إجراء عملية جراحية له وتركيب شريحة. اليوم بوبي يركض ويمرح بكل حرية وصار صديق العائلة الوفي.",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500",
                location = "حي السليمانية، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p3",
                title = "ميلو الصغير 🐾",
                category = "رعاية صغار",
                type = "adoption",
                dateOrLikes = "١٠ أبريل ٢٠٢٤",
                details = "هر صغير يبلغ من العمر أسابيع قليلة فقط، فقد أمه في الشارع. قمت بإرضاعه بالحليب الصناعي المخصص للقطط كل ٣ ساعات حتى كبُر وتخطى مرحلة الخطر. هو الآن مشاكس ومحب للمرح للغاية.",
                imageUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=500",
                location = "حي الصحافة، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p4",
                title = "ماكس البطل 🐕",
                category = "صديق دائم",
                type = "stray",
                dateOrLikes = "٠٥ مارس ٢٠٢٤",
                details = "كلب ضائع تعرض للجفاف الشديد في الصيف. قمت بتوفير الماء والطعام الرطب له بشكل مستمر حتى استعاد طاقته، وتعلّق بي تماماً ليصبح حارس المنزل والرفيق المخلص في نزهاتي الصباحية.",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=500",
                location = "طريق الملك فهد، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p5",
                title = "ليو الرمادي 🐈",
                category = "إنقاذ طارئ",
                type = "stray",
                dateOrLikes = "١٨ فبراير ٢٠٢٤",
                details = "عالق داخل محرك سيارة جاري! سمعنا مواءه الصعب وقضينا ساعتين كاملتين لفك الأجزاء وإنقاذه بسلام دون أي خدش. قمنا بتنظيفه وعلاجه من الفطريات والآن هو أهدأ قط في الكون.",
                imageUrl = "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=500",
                location = "حي النرجس، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p6",
                title = "كيكي المتكلم 🦜",
                category = "رعاية طيور",
                type = "discussion",
                dateOrLikes = "٢٩ يناير ٢٠٢٤",
                details = "ببغاء صغير سقط من عشه وكان معرضاً لهجوم القطط. اعتنيت به وغذيته بالحبوب اللينة حتى نبت ريشه وتعلّم الطيران. يطلق الآن صفارات ترحيبية جميلة كلما دخلت الغرفة.",
                imageUrl = "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=500",
                location = "المنتدى التعليمي، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p7",
                title = "أوسكار المرح 🐕‍🦺",
                category = "صداقة جديدة",
                type = "adoption",
                dateOrLikes = "١٤ ديسمبر ٢٠٢٣",
                details = "جرو ذو عينين حزينتين وجدته وحيداً في الشارع. بعد تنظيفه وتقديم التطعيمات اللازمة، أظهر حباً غير مشروط للجميع، ولديه طاقة مذهلة للعب بالكرة وتعديل مزاجي اليومي.",
                imageUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=500",
                location = "حي العقيق، الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p8",
                title = "بيلا الأنيقة 😻",
                category = "إنقاذ وتأهيل",
                type = "adoption",
                dateOrLikes = "٣٠ نوفمبر ٢٠٢٣",
                details = "قطة شيرازية بيضاء تخلى عنها أصحابها في الشارع وكانت تعاني من مشاكل هضمية وخوف شديد. مع نظام غذائي متكامل وصبر طويل، استعادت ثقتها بالبشر وصارت فرواً ناعماً من الحب.",
                imageUrl = "https://images.unsplash.com/photo-1533743983669-94fa5c4338ec?w=500",
                location = "حي الملقا, الرياض",
                reporter = "أحمد محمد"
            ),
            ProfileDetailItem(
                id = "p9",
                title = "روكي الوفي 🐕",
                category = "حراسة وألفة",
                type = "stray",
                dateOrLikes = "١٢ أكتوبر ٢٠٢٣",
                details = "كلب بلدي ذكي جداً، كان يحمي زقاق الشارع. بعد أن ألِفني، صار ينتظرني يومياً عند عودتي من العمل ليرافقني حتى باب المنزل. قصة وفاء عظيمة تظهر مدى رحمة الحيوانات الضالة.",
                imageUrl = "https://images.unsplash.com/photo-1561037404-61cd46aa615b?w=500",
                location = "طريق أنس بن مالك، الرياض",
                reporter = "أحمد محمد"
            )
        )
    }

    val profileVideos = remember {
        listOf(
            ProfileVideoItem(
                id = "v1",
                title = "توزيع طعام الشتاء 🌧️",
                description = "جولة ميدانية لتوزيع الوجبات الدافئة والمياه لقطط الشارع في ليلة ماطرة بالرياض.",
                duration = "٠١:٤٥",
                views = "٢.٥ ألف",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500"
            ),
            ProfileVideoItem(
                id = "v2",
                title = "إنقاذ بوبي من الخطر 🚗",
                description = "توثيق لقصة لحظة استدراج الكلب المصاب بوبي وتهدئته حتى نقله للسيارة الطبية بأمان وعلاجه.",
                duration = "٠٣:١٢",
                views = "٥.١ ألف",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500"
            ),
            ProfileVideoItem(
                id = "v3",
                title = "ألعاب ميلو المشاكسة 🎾",
                description = "لقطات مرحة لطريقة تفاعل ميلو مع ألعابه الجديدة والقفزات البهلوانية اللطيفة في منزله الجديد.",
                duration = "٠٠:٥٨",
                views = "١.٨ ألف",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1533738363-b7f9aef128ce?w=500"
            ),
            ProfileVideoItem(
                id = "v4",
                title = "يوم التعقيم المجاني 🏥",
                description = "مقتطفات من المبادرة التطوعية لتطعيم وتعقيم حيوانات الشارع بالتعاون مع العيادات البيطرية.",
                duration = "٢:٣٠",
                views = "٩٥٠",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=500"
            ),
            ProfileVideoItem(
                id = "v5",
                title = "جولة في مأوى الأمان 🏡",
                description = "جولة تعريفية بالملجأ والمساحات المخصصة لحركة ولعب الكلاب والقطط المنقذة في الرياض.",
                duration = "٠٤:١٥",
                views = "٣.٢ ألف",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=500"
            ),
            ProfileVideoItem(
                id = "v6",
                title = "نصائح الإسعافات الأولية 💉",
                description = "فيديو تعليمي سريع يوضح كيفية التعامل مع جروح الحيوانات الضالة وتطهيرها بأمان عند العثور عليها.",
                duration = "٠٢:٠٥",
                views = "٤.٤ ألف",
                videoThumbnailUrl = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=500"
            )
        )
    }

    val savedItems = remember {
        listOf(
            ProfileDetailItem(
                id = "saved_1",
                title = "عيادة الرياض البيطرية",
                category = "عيادة بيطرية",
                type = "clinic",
                dateOrLikes = "قبل ٣ أيام",
                details = "عيادة بيطرية متكاملة مجهزة بأحدث التقنيات لتقديم الرعاية الطبية الفائقة والجراحات الطارئة والتحاليل المخبرية للحيوانات الأليفة مع توفير طوارئ ٢٤ ساعة.",
                imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=500",
                location = "الرياض، حي السليمانية، طريق الملك عبد العزيز",
                phone = "+966 11 462 2345",
                rating = "4.9"
            ),
            ProfileDetailItem(
                id = "saved_2",
                title = "ملجأ أليف لرعاية الكلاب",
                category = "ملجأ حيوانات",
                type = "shelter",
                dateOrLikes = "قبل أسبوع",
                details = "ملجأ غير ربحي يهدف لإنقاذ وإإيواء الكلاب الضالة والمصابة بالرياض، وتأهيلها طبياً وسلوكياً قبل عرضها لبرامج التبني المجتمعية الموثقة.",
                imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=500",
                location = "الرياض، حي Nargis، طريق أبي بكر الصديق",
                phone = "+966 50 123 4567",
                rating = "4.8"
            ),
            ProfileDetailItem(
                id = "saved_3",
                title = "متجر طعام ومستلزمات الأليف",
                category = "متجر ومستلزمات",
                type = "shop",
                dateOrLikes = "قبل أسبوعين",
                details = "يوفر المتجر تشكيلة واسعة من الأغذية العضوية الرطبة والجافة، الألعاب التعليمية، أدوات التدريب والتعقيم الفاخرة للحيوانات الأليفة.",
                imageUrl = "https://images.unsplash.com/photo-1583337130417-3346a1be7dee?w=500",
                location = "الرياض، حي الصحافة، طريق الملك فهد",
                phone = "+966 11 234 5678",
                rating = "4.7"
            )
        )
    }

    val likedItems = remember {
        listOf(
            ProfileDetailItem(
                id = "liked_1",
                title = "إنقاذ جرو صغير مصاب في حي الصحافة",
                category = "حالات عاجلة",
                type = "stray",
                dateOrLikes = "٢٤ إعجاب",
                details = "عثرنا على جرو صغير مصاب في ساقه الخلفية في حي الصحافة. بفضل دعم متطوعي Safe Paws، تم نقله فوراً لعيادة شريكة، وهو الآن بصحة ممتازة ويبحث عن منزل دائم.",
                imageUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=500",
                reporter = "أحمد محمد",
                location = "حي الصحافة، الرياض"
            ),
            ProfileDetailItem(
                id = "liked_2",
                title = "تبني قطة هملايا لطيفة وودودة",
                category = "تبني الأليفة",
                type = "adoption",
                dateOrLikes = "٥٦ إعجاب",
                details = "لونا قطة هملايا نقية تبلغ من العمر سنة واحدة. هادئة جداً، مطعمة ومعقمة بالكامل، تحب التفاعل اللطيف مع الأطفال وتبحث عن عائلة محبة ترعاها.",
                imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=500",
                reporter = "سارة العتيبي",
                location = "حي السليمانية، الرياض"
            ),
            ProfileDetailItem(
                id = "liked_3",
                title = "منشور: نصائح تغذية ورعاية الطيور المنزلية",
                category = "مناقشات",
                type = "discussion",
                dateOrLikes = "١٨ إعجاب",
                details = "دليل علمي مبسط وشامل للعناية بالطيور المنزلية (البادجي والكوكاتيل) يشمل الحميات الغذائية الصحيحة، أهمية الضوء الطبيعي، وتجنب برودة الغرفة.",
                imageUrl = "https://images.unsplash.com/photo-1522850949506-585e5e2298f7?w=500",
                reporter = "د. فهد المطيري",
                location = "المنتدى التعليمي"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Very Top Row: Screen Title and Hamburger Menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "الملف الشخصي",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(
                onClick = {
                    menuDetailSection = "options"
                    showProfileMenu = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "خيارات الملف الشخصي",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Profile Card Row below the header: picture on the left, Name & Location on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile picture box with clickable trigger and camera overlay
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clickable { showChangePhotoDialog = true }
                    .testTag("change_profile_picture_trigger")
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "الملف الشخصي لأحمد محمد",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Verified ID check aligned to TopEnd
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32)) // Green for verification
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "حساب موثق الهوية",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
                // Edit image icon aligned to BottomEnd
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomEnd)
                        .border(1.5.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تغيير الصورة",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }

            // Name & Location on the right of the profile image (in RTL starting side)
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "أحمد محمد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "الموقع",
                        tint = Color.LightGray,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "الرياض، المملكة العربية السعودية",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GAMIFICATIONS BADGES ROW (نقاط الثقة، عمليات الإنقاذ، منشورات)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GamificationBadge(label = "المنشورات", count = "١٢")
            GamificationBadge(label = "الإنقاذ 🐾", count = rescuesCount.toString())
            GamificationBadge(label = "نقاط الثقة 🛡️", count = trustScore.toString(), isHighlight = true)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Two actions row: Edit Profile & QR Code Accounts
        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1.3f)
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(16.dp))
                    Text("تعديل ملف الرعاية", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Button(
                onClick = { showQrDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("show_profile_qr_trigger"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode, contentDescription = "رمز QR", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text("رمز QR للحساب", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instagram-style Tab Bar (خانتين مثل انستغرام)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color.White.copy(alpha = 0.12f)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Photos Tab Button
            val photosActive = selectedProfileTab == "photos"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedProfileTab = "photos" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "الصور",
                        tint = if (photosActive) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الصور (${profilePhotos.size})",
                        fontSize = 12.sp,
                        fontWeight = if (photosActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (photosActive) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }

            // Videos Tab Button
            val videosActive = selectedProfileTab == "videos"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { selectedProfileTab = "videos" }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "الفيديوهات",
                        tint = if (videosActive) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "الفيديوهات (${profileVideos.size})",
                        fontSize = 12.sp,
                        fontWeight = if (videosActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (videosActive) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        if (selectedProfileTab == "photos") {
            // 3-column square grid of posts
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                profilePhotos.chunked(3).forEach { rowPhotos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowPhotos.forEach { photo ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPhotoDetail = photo }
                            ) {
                                AsyncImage(
                                    model = photo.imageUrl,
                                    contentDescription = photo.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        // Fill empty spaces if row is incomplete
                        repeat(3 - rowPhotos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            // 3-column square grid of video thumbnails
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                profileVideos.chunked(3).forEach { rowVideos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowVideos.forEach { video ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedVideoDetail = video }
                            ) {
                                AsyncImage(
                                    model = video.videoThumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dark overlay with video duration & play play icon
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "تشغيل",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center)
                                    )
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = video.views,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = video.duration,
                                            color = Color.White,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                        repeat(3 - rowVideos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modern Profile Options Page - full screen overlay
    if (showProfileMenu) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(enabled = false) {} // block click propagation
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Area of Full Screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (menuDetailSection) {
                            "info" -> "معلومات الحساب"
                            "saved" -> "المحفوظات"
                            "liked" -> "القلوب والمفضلات"
                            else -> "خيارات الحساب ⚙️"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    )

                    IconButton(
                        onClick = {
                            if (menuDetailSection != "options") {
                                menuDetailSection = "options"
                            } else {
                                showProfileMenu = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Page Content switcher
                when (menuDetailSection) {
                    "options" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. Account Info Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuDetailSection = "info" },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "معلومات الحساب",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                     )
                                    Text(
                                        text = "معلومات الحساب",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "عرض التفاصيل",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // 2. Saved Items Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuDetailSection = "saved" },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "المحفوظات",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "المحفوظات",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "عرض التفاصيل",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // 3. Liked Items Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { menuDetailSection = "liked" },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "القلوب والمفضلات",
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "الأشياء التي نالت إعجابك (القلوب)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "عرض التفاصيل",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    "info" -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                InfoRow(label = "الاسم الكامل", value = "أحمد محمد")
                                InfoRow(label = "البريد الإلكتروني", value = "ahmed.mohammed@example.com")
                                InfoRow(label = "رقم الهاتف", value = "+966 50 123 4567")
                                InfoRow(label = "نوع الحساب", value = "متطوع معتمد")
                                InfoRow(label = "تاريخ الانضمام", value = "١٢ سبتمبر ٢٠٢٣")
                            }
                        }
                    }

                    "saved" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            savedItems.forEach { item ->
                                ProfilePostCard(item = item, onClick = { selectedProfileDetailItem = item })
                            }
                        }
                    }

                    "liked" -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            likedItems.forEach { item ->
                                ProfilePostCard(item = item, onClick = { selectedProfileDetailItem = item })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (selectedProfileDetailItem != null) {
            ProfileDetailDialog(item = selectedProfileDetailItem!!, onDismiss = { selectedProfileDetailItem = null })
        }

        if (selectedPhotoDetail != null) {
            ProfileDetailDialog(item = selectedPhotoDetail!!, onDismiss = { selectedPhotoDetail = null })
        }

        if (selectedVideoDetail != null) {
            ProfileVideoDetailDialog(item = selectedVideoDetail!!, onDismiss = { selectedVideoDetail = null })
        }

        if (showChangePhotoDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showChangePhotoDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("change_profile_picture_dialog"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "تغيير صورة الملف الشخصي 📸",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Text(
                            text = "اختر رمزاً تعبيرياً كأفاتار أو أدخل رابط صورتك المخصصة أدناه:",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // Avatar List
                        val avatars = listOf(
                            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200" to "شاب مبادر",
                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200" to "فتاة منقذة",
                            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=200" to "صديق الكلاب",
                            "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=200" to "محب القطط",
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200" to "طبيبة أليفة",
                            "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=200" to "طبيب رعاية"
                        )

                        var customUrlInput by remember { mutableStateOf("") }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(avatars) { (url, label) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.updateProfileImageUrl(url)
                                            showChangePhotoDialog = false
                                        }
                                        .padding(4.dp)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = label,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (profileImageUrl == url) 2.dp else 1.dp,
                                                color = if (profileImageUrl == url) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = if (profileImageUrl == url) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
                                }
                            }
                        }

                        // Custom URL Field
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "أو ضع رابط صورة خارجي (URL):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            BasicTextField(
                                value = customUrlInput,
                                onValueChange = { customUrlInput = it },
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("custom_profile_picture_url_input"),
                                decorationBox = @Composable { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1E1E1E), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (customUrlInput.isEmpty()) {
                                            Text(
                                                text = "https://example.com/photo.jpg",
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        // Dialog Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (customUrlInput.isNotBlank()) {
                                        viewModel.updateProfileImageUrl(customUrlInput.trim())
                                        showChangePhotoDialog = false
                                    }
                                },
                                enabled = customUrlInput.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("save_custom_profile_picture_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("تطبيق الرابط", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = { showChangePhotoDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                Text("إلغاء", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        if (showQrDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showQrDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("profile_qr_dialog"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "رمز QR التعريفي 🪪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Text(
                            text = "امسح الرمز ضوئياً لمشاركة ملف رعاية المتطوع أحمد محمد والتحقق من مصداقيته ونقاطه ونشاطه.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        // QR Code Container with dynamic api.qrserver.com
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val qrCodeData = "بيت الأمان - ملف المتطوع البطل: أحمد محمد\nالنقاط التراكمية: $trustScore نقطة\nعمليات الإنقاذ: $rescuesCount حالة"
                            val encodedData = try {
                                java.net.URLEncoder.encode(qrCodeData, "UTF-8")
                            } catch (e: Exception) {
                                "Ahmed"
                            }
                            val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=$encodedData"

                            AsyncImage(
                                model = qrCodeUrl,
                                contentDescription = "رمز QR الحساب",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Stats summary inside QR card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الاسم", fontSize = 10.sp, color = Color.Gray)
                                Text("أحمد محمد", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("الإنقاذ 🐾", fontSize = 10.sp, color = Color.Gray)
                                Text("$rescuesCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha = 0.1f)))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("نقاط الثقة 🛡️", fontSize = 10.sp, color = Color.Gray)
                                Text("$trustScore", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                            }
                        }

                        // Share / Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    android.widget.Toast.makeText(context, "تم نسخ رابط الملف الشخصي لمشاركته!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("نسخ الرابط", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showQrDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E))
                            ) {
                                Text("إغلاق", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp)
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)
}

@Composable
fun SavedItemRow(title: String, type: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = type, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                    Text(text = "•", color = Color.Gray, fontSize = 9.sp)
                    Text(text = date, color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun LikedItemRow(title: String, category: String, likes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFFEBEE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(14.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                    Text(text = "•", color = Color.Gray, fontSize = 9.sp)
                    Text(text = likes, color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun GamificationBadge(label: String, count: String, isHighlight: Boolean = false) {
    Column(
        modifier = Modifier
            .width(70.dp)
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}


// ---------------------- 7. INTERACTIVE CHATBOT (GEMINI EXPERT CALLS) ----------------------
@Composable


fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(text, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun ProfilePostCard(item: ProfileDetailItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.type == "stray" || item.type == "adoption" || item.type == "discussion") 
                                    Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = if (item.type == "stray" || item.type == "adoption" || item.type == "discussion") 
                                Color(0xFFE91E63) else MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = item.dateOrLikes,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.details,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun ProfileDetailDialog(item: ProfileDetailItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "تفاصيل المنشور 📖",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .background(
                            color = if (item.type == "stray" || item.type == "adoption" || item.type == "discussion") 
                                Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category,
                        color = if (item.type == "stray" || item.type == "adoption" || item.type == "discussion") 
                            Color(0xFFE91E63) else MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.reporter.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الناشر:", fontSize = 12.sp, color = Color.Gray)
                                Text(item.reporter, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (item.location.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الموقع:", fontSize = 12.sp, color = Color.Gray)
                                Text(item.location, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (item.phone.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("رقم الهاتف:", fontSize = 12.sp, color = Color.Gray)
                                Text(item.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (item.rating.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("التقييم:", fontSize = 12.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(item.rating, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الحالة:", fontSize = 12.sp, color = Color.Gray)
                            Text(item.dateOrLikes, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "تفاصيل وتوضيح:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.details,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}

@Composable
fun ProfileVideoDetailDialog(item: ProfileVideoItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "قصة وتفاصيل الإنقاذ 🎥",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = item.videoThumbnailUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Play Button Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "تشغيل الفيديو",
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المدة:", fontSize = 12.sp, color = Color.Gray)
                            Text(item.duration, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المشاهدات:", fontSize = 12.sp, color = Color.Gray)
                            Text(item.views, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "شرح وقصة هذا المقطع مع الحيوان الأليف:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Justify
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}


