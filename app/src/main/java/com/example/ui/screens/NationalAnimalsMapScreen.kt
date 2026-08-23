package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.PrimaryTeal
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

data class IllustratedCountrySpot(
    val id: String,
    val countryName: String,
    val flagEmoji: String,
    val animalName: String,
    val animalEmoji: String,
    val mapXPercent: Float, // Relative X on the illustrated world map (0.0 to 1.0)
    val mapYPercent: Float, // Relative Y on the illustrated world map (0.0 to 1.0)
    val region: String,
    val colorHex: String,
    val description: String
)

object IllustratedMapSpotsProvider {
    val spots = listOf(
        IllustratedCountrySpot(
            id = "ma",
            countryName = "المغرب",
            flagEmoji = "🇲🇦",
            animalName = "أسد الأطلس (Barbary Lion)",
            animalEmoji = "🦁",
            mapXPercent = 0.44f,
            mapYPercent = 0.42f,
            region = "شمال إفريقيا",
            colorHex = "#E11D48",
            description = "المملكة المغربية 🇲🇦 - أسد الأطلس البربري رمز الشجاعة والمهابة ولقب المنتخب الوطني 'أسود الأطلس'. رسم الأسد يغطي جبال الأطلس والمملكة من الشمال حتى الجنوب الصحراوي!"
        ),
        IllustratedCountrySpot(
            id = "fr",
            countryName = "فرنسا",
            flagEmoji = "🇫🇷",
            animalName = "الديك الغالي (Le Coq Gaulois)",
            animalEmoji = "🐓",
            mapXPercent = 0.48f,
            mapYPercent = 0.31f,
            region = "أوروبا الغربية",
            colorHex = "#2563EB",
            description = "فرنسا 🇫🇷 - الديك الغالي رمز اليقظة والفخر الوطني والشجاعة عند بزوغ الفجر، مطبوع داخل حدود الأراضي الفرنسية!"
        ),
        IllustratedCountrySpot(
            id = "es",
            countryName = "إسبانيا",
            flagEmoji = "🇪🇸",
            animalName = "الثور الإسباني (El Toro Bravo)",
            animalEmoji = "🐂",
            mapXPercent = 0.45f,
            mapYPercent = 0.36f,
            region = "أوروبا / شبه الجزيرة الإيبيرية",
            colorHex = "#DC2626",
            description = "إسبانيا 🇪🇸 - الثور الإسباني رمز العنفوان والشجاعة والقوة في الفلكلور والتراث الإسباني التاريخي!"
        ),
        IllustratedCountrySpot(
            id = "sa",
            countryName = "المملكة العربية السعودية",
            flagEmoji = "🇸🇦",
            animalName = "الصقر الحر والجمل العربي",
            animalEmoji = "🦅",
            mapXPercent = 0.58f,
            mapYPercent = 0.44f,
            region = "شبه الجزيرة العربية",
            colorHex = "#059669",
            description = "السعودية 🇸🇦 - الصقر الحر رمز الشموخ والأصالة وعزة النفس، والجمل سفينة الصحراء ورمز الصبر والكرم في الجزيرة العربية!"
        ),
        IllustratedCountrySpot(
            id = "dz",
            countryName = "الجزائر",
            flagEmoji = "🇩🇿",
            animalName = "فنك الصحراء (Fennec Fox)",
            animalEmoji = "🦊",
            mapXPercent = 0.48f,
            mapYPercent = 0.46f,
            region = "شمال إفريقيا",
            colorHex = "#16A34A",
            description = "الجزائر 🇩🇿 - ثعلب الفنك ذو الأذنين الكبيرتين، سريع الحركة وشديد الذكاء ولقب المنتخب 'محاربو الصحراء / الأفناك'!"
        ),
        IllustratedCountrySpot(
            id = "eg",
            countryName = "مصر",
            flagEmoji = "🇪🇬",
            animalName = "عقاب السهوب وصقر صلاح الدين",
            animalEmoji = "🦅",
            mapXPercent = 0.54f,
            mapYPercent = 0.43f,
            region = "شمال إفريقيا والشرق الأوسط",
            colorHex = "#D97706",
            description = "مصر 🇪🇬 - نسر صلاح الدين والعقاب الذهبي رمز العزة والسيادة والقوة في وادي النيل وتاريخ الحضارة الفرعونية والإسلامية!"
        ),
        IllustratedCountrySpot(
            id = "us",
            countryName = "الولايات المتحدة",
            flagEmoji = "🇺🇸",
            animalName = "النسر الأصلع والبيسون الأمريكي",
            animalEmoji = "🦅",
            mapXPercent = 0.19f,
            mapYPercent = 0.34f,
            region = "أمريكا الشمالية",
            colorHex = "#1D4ED8",
            description = "الولايات المتحدة 🇺🇸 - النسر الأصلع الشامخ والبيسون الضخم يشكلان كامل جغرافيا السهول الأمريكية من المحيط إلى المحيط!"
        ),
        IllustratedCountrySpot(
            id = "ca",
            countryName = "كندا",
            flagEmoji = "🇨🇦",
            animalName = "القندس الكندي والموظ",
            animalEmoji = "🦫",
            mapXPercent = 0.22f,
            mapYPercent = 0.22f,
            region = "أمريكا الشمالية",
            colorHex = "#DC2626",
            description = "كندا 🇨🇦 - القندس الكندي رمز الهندسة الطبيعية والعمل الدؤوب، وغزال الموظ العملاق في الغابات والبحيرات الكندية!"
        ),
        IllustratedCountrySpot(
            id = "br",
            countryName = "البرازيل",
            flagEmoji = "🇧🇷",
            animalName = "اليغور المرقط (Jaguar) والببغاء",
            animalEmoji = "🐆",
            mapXPercent = 0.31f,
            mapYPercent = 0.65f,
            region = "أمريكا الجنوبية",
            colorHex = "#15803D",
            description = "البرازيل 🇧🇷 - اليغور المرقط ملك غابات الأمازون وأكبر مفترس قطي في القارة مع طيور الطوقان والببغاوات الاستوائية!"
        ),
        IllustratedCountrySpot(
            id = "ru",
            countryName = "روسيا",
            flagEmoji = "🇷🇺",
            animalName = "الدب البني الأوراسي",
            animalEmoji = "🐻",
            mapXPercent = 0.72f,
            mapYPercent = 0.22f,
            region = "أوراسيا / سيبيريا",
            colorHex = "#7C2D12",
            description = "روسيا 🇷🇺 - الدب البني الضخم يغطي غابات التايغا وسيبيريا ممتداً عبر القارة رمزاً للقوة والتحمل والصلابة!"
        ),
        IllustratedCountrySpot(
            id = "cn",
            countryName = "الصين",
            flagEmoji = "🇨🇳",
            animalName = "الباندا العملاقة (Giant Panda)",
            animalEmoji = "🐼",
            mapXPercent = 0.81f,
            mapYPercent = 0.36f,
            region = "شرق آسيا",
            colorHex = "#B91C1C",
            description = "الصين 🇨🇳 - الباندا العملاقة الكنز الوطني الصيني وسفير السلام والصداقة، مطبوع في قلب غابات الخيزران الصينية!"
        ),
        IllustratedCountrySpot(
            id = "in",
            countryName = "الهند",
            flagEmoji = "🇮🇳",
            animalName = "النمر البنغالي الملكي والطاووس",
            animalEmoji = "🐅",
            mapXPercent = 0.69f,
            mapYPercent = 0.45f,
            region = "جنوب آسيا",
            colorHex = "#D97706",
            description = "الهند 🇮🇳 - النمر البنغالي الملكي رمز الرشاقة والهيبة والجمال الأسطوري في شبه القارة الهندية!"
        ),
        IllustratedCountrySpot(
            id = "au",
            countryName = "أستراليا",
            flagEmoji = "🇦🇺",
            animalName = "الكنغر الأحمر ودب الكوالا",
            animalEmoji = "🦘",
            mapXPercent = 0.86f,
            mapYPercent = 0.72f,
            region = "أوقيانوسيا",
            colorHex = "#EA580C",
            description = "أستراليا 🇦🇺 - الكنغر الأحمر الذي لا يقفز للوراء ودب الكوالا اللطيف يشكلان القارة الأسترالية بأكملها!"
        ),
        IllustratedCountrySpot(
            id = "gb",
            countryName = "بريطانيا (المملكة المتحدة)",
            flagEmoji = "🇬🇧",
            animalName = "الأسد البريطاني الملكي",
            animalEmoji = "🦁",
            mapXPercent = 0.46f,
            mapYPercent = 0.27f,
            region = "أوروبا",
            colorHex = "#1E3A8A",
            description = "بريطانيا 🇬🇧 - الأسد البريطاني رمز الشجاعة والمهابة الملكية على التاج والراية البريطانية!"
        ),
        IllustratedCountrySpot(
            id = "de",
            countryName = "ألمانيا",
            flagEmoji = "🇩🇪",
            animalName = "النسر الفيدرالي (Bundesadler)",
            animalEmoji = "🦅",
            mapXPercent = 0.50f,
            mapYPercent = 0.28f,
            region = "أوروبا الوسطى",
            colorHex = "#18181B",
            description = "ألمانيا 🇩🇪 - النسر الفيدرالي الشعار الوطني الأقدم للحكم والسيادة والاستقرار!"
        ),
        IllustratedCountrySpot(
            id = "it",
            countryName = "إيطاليا",
            flagEmoji = "🇮🇹",
            animalName = "الذئب الإيطالي (Italian Wolf)",
            animalEmoji = "🐺",
            mapXPercent = 0.51f,
            mapYPercent = 0.34f,
            region = "جنوب أوروبا",
            colorHex = "#047857",
            description = "إيطاليا 🇮🇹 - الذئب الإيطالي المرتبط بأسطورة تأسيس روما القديمة والشجاعة والولاء!"
        ),
        IllustratedCountrySpot(
            id = "ae",
            countryName = "الإمارات العربية المتحدة",
            flagEmoji = "🇦🇪",
            animalName = "المها العربي والصقر الحر",
            animalEmoji = "🦌",
            mapXPercent = 0.61f,
            mapYPercent = 0.44f,
            region = "الخليج العربي",
            colorHex = "#047857",
            description = "الإمارات 🇦🇪 - المها العربي ذو القرون الطويلة الرشيقة رمز الأصالة والجمال البيئي في الكثبان الرملية!"
        ),
        IllustratedCountrySpot(
            id = "tn",
            countryName = "تونس",
            flagEmoji = "🇹🇳",
            animalName = "عقاب قرطاج وصقر السهوب",
            animalEmoji = "🦅",
            mapXPercent = 0.50f,
            mapYPercent = 0.38f,
            region = "شمال إفريقيا",
            colorHex = "#E11D48",
            description = "تونس 🇹🇳 - نسور قرطاج رمز العزة والتاريخ العريق لمدينة قرطاج وشمال إفريقيا الخضراء!"
        ),
        IllustratedCountrySpot(
            id = "ly",
            countryName = "ليبيا",
            flagEmoji = "🇱🇾",
            animalName = "صقر الصحراء الحر",
            animalEmoji = "🦅",
            mapXPercent = 0.52f,
            mapYPercent = 0.44f,
            region = "شمال إفريقيا",
            colorHex = "#059669",
            description = "ليبيا 🇱🇾 - صقر الصحراء الحر رمز الشموخ والأصالة وأطول ساحل متوسطي في القارة الإفريقية!"
        ),
        IllustratedCountrySpot(
            id = "sd",
            countryName = "السودان",
            flagEmoji = "🇸🇩",
            animalName = "صقر الجديان (Secretarybird)",
            animalEmoji = "🦅",
            mapXPercent = 0.55f,
            mapYPercent = 0.51f,
            region = "شرق ووسط إفريقيا",
            colorHex = "#16A34A",
            description = "السودان 🇸🇩 - صقر الجديان رمز النبالة والقوة عند ملتقى النيلين الأبيض والأزرق!"
        ),
        IllustratedCountrySpot(
            id = "mr",
            countryName = "موريتانيا",
            flagEmoji = "🇲🇷",
            animalName = "مها الصحراء وغزال المهر",
            animalEmoji = "🦌",
            mapXPercent = 0.42f,
            mapYPercent = 0.47f,
            region = "غرب إفريقيا",
            colorHex = "#059669",
            description = "موريتانيا 🇲🇷 - غزال المهر ومها الصحراء رمز الصبر والجمال والوفاق في بلاد شنقيط!"
        ),
        IllustratedCountrySpot(
            id = "qa",
            countryName = "قطر",
            flagEmoji = "🇶🇦",
            animalName = "المها العربي الوضيحي",
            animalEmoji = "🦄",
            mapXPercent = 0.60f,
            mapYPercent = 0.44f,
            region = "الخليج العربي",
            colorHex = "#881337",
            description = "قطر 🇶🇦 - المها العربي الوضيحي رمز الوطنية والأناقة الصحراوية في شبه جزيرة قطر!"
        ),
        IllustratedCountrySpot(
            id = "kw",
            countryName = "الكويت",
            flagEmoji = "🇰🇼",
            animalName = "الصقر الحر والجمل",
            animalEmoji = "🦅",
            mapXPercent = 0.59f,
            mapYPercent = 0.42f,
            region = "الخليج العربي",
            colorHex = "#059669",
            description = "الكويت 🇰🇼 - الصقر الحر والجمل رمز الكرم والتجارة البحرية والتراث الخليجي الأصيل!"
        ),
        IllustratedCountrySpot(
            id = "bh",
            countryName = "البحرين",
            flagEmoji = "🇧🇭",
            animalName = "غزال الريم والبلبل البحريني",
            animalEmoji = "🦌",
            mapXPercent = 0.60f,
            mapYPercent = 0.43f,
            region = "الخليج العربي",
            colorHex = "#DC2626",
            description = "البحرين 🇧🇭 - غزال الريم رمز الرقة والجمال الطبيعي وتاريخ دلمون العريق!"
        ),
        IllustratedCountrySpot(
            id = "om",
            countryName = "سلطنة عمان",
            flagEmoji = "🇴🇲",
            animalName = "النمر العربي والمها",
            animalEmoji = "🐆",
            mapXPercent = 0.62f,
            mapYPercent = 0.46f,
            region = "شبه الجزيرة العربية",
            colorHex = "#16A34A",
            description = "سلطنة عمان 🇴🇲 - النمر العربي النادر في جبال ظفار رمز الحكمة والهدوء والشجاعة!"
        ),
        IllustratedCountrySpot(
            id = "ye",
            countryName = "اليمن",
            flagEmoji = "🇾🇪",
            animalName = "النمر العربي والنسر",
            animalEmoji = "🐆",
            mapXPercent = 0.59f,
            mapYPercent = 0.49f,
            region = "شبه الجزيرة العربية",
            colorHex = "#DC2626",
            description = "اليمن 🇾🇪 - النمر العربي رمز الهيبة وأصالة جبال اليمن السعيد وجزيرة سقطرى الفريدة!"
        ),
        IllustratedCountrySpot(
            id = "jo",
            countryName = "الأردن",
            flagEmoji = "🇯🇴",
            animalName = "المها العربي والعصفور الوردي",
            animalEmoji = "🦄",
            mapXPercent = 0.56f,
            mapYPercent = 0.41f,
            region = "بلاد الشام",
            colorHex = "#DC2626",
            description = "الأردن 🇯🇴 - المها العربي رمز الحفاظ على الحياة الفطرية والبتراء الوردية ووادي رم!"
        ),
        IllustratedCountrySpot(
            id = "ps",
            countryName = "فلسطين",
            flagEmoji = "🇵🇸",
            animalName = "عصفور الشمس وغزال الجبل",
            animalEmoji = "🐦",
            mapXPercent = 0.55f,
            mapYPercent = 0.40f,
            region = "بلاد الشام",
            colorHex = "#16A34A",
            description = "فلسطين 🇵🇸 - عصفور الشمس الفلسطيني رمز الحرية والتحليق فوق أراضي القدس والزيتون!"
        ),
        IllustratedCountrySpot(
            id = "iq",
            countryName = "العراق",
            flagEmoji = "🇮🇶",
            animalName = "أسد بابل والشنار",
            animalEmoji = "🦁",
            mapXPercent = 0.58f,
            mapYPercent = 0.39f,
            region = "بلاد الرافدين",
            colorHex = "#DC2626",
            description = "العراق 🇮🇶 - أسد بابل التاريخي رمز الحضارة السومرية والبابلية وشموخ دجلة والفرات!"
        ),
        IllustratedCountrySpot(
            id = "sy",
            countryName = "سوريا",
            flagEmoji = "🇸🇾",
            animalName = "الدب السوري والصقر",
            animalEmoji = "🐻",
            mapXPercent = 0.57f,
            mapYPercent = 0.39f,
            region = "بلاد الشام",
            colorHex = "#16A34A",
            description = "سوريا 🇸🇾 - الصقر السوري رمز العنفوان وتاريخ دمشق وأشجار الياسمين العريقة!"
        ),
        IllustratedCountrySpot(
            id = "lb",
            countryName = "لبنان",
            flagEmoji = "🇱🇧",
            animalName = "الضبع المخطط والسنونو",
            animalEmoji = "🐺",
            mapXPercent = 0.56f,
            mapYPercent = 0.39f,
            region = "بلاد الشام",
            colorHex = "#16A34A",
            description = "لبنان 🇱🇧 - رمز جبال الأرز الخضراء والشواطئ المتوسطية الخلابة!"
        ),
        IllustratedCountrySpot(
            id = "tr",
            countryName = "تركيا",
            flagEmoji = "🇹🇷",
            animalName = "الذئب الرمادي (Bozkurt)",
            animalEmoji = "🐺",
            mapXPercent = 0.56f,
            mapYPercent = 0.35f,
            region = "أوراسيا / الأناضول",
            colorHex = "#DC2626",
            description = "تركيا 🇹🇷 - الذئب الرمادي رمز الحرية والقيادة في الفلكلور والتاريخ الأناضولي!"
        ),
        IllustratedCountrySpot(
            id = "jp",
            countryName = "اليابان",
            flagEmoji = "🇯🇵",
            animalName = "طائر الدراج الأخضر وقرد الثلج",
            animalEmoji = "🦚",
            mapXPercent = 0.89f,
            mapYPercent = 0.35f,
            region = "شرق آسيا",
            colorHex = "#BE123C",
            description = "اليابان 🇯🇵 - طائر الدراج الأخضر الزمردي وقرود المكاك في جبال الثلوج والينابيع الدافئة!"
        ),
        IllustratedCountrySpot(
            id = "nz",
            countryName = "نيوزيلندا",
            flagEmoji = "🇳🇿",
            animalName = "طائر الكيوي (Kiwi Bird)",
            animalEmoji = "🥝",
            mapXPercent = 0.95f,
            mapYPercent = 0.82f,
            region = "أوقيانوسيا",
            colorHex = "#0369A1",
            description = "نيوزيلندا 🇳🇿 - طائر الكيوي الفريد غير القادر على الطيران وأيقونة ولقب شعب نيوزيلندا!"
        ),
        IllustratedCountrySpot(
            id = "za",
            countryName = "جنوب إفريقيا",
            flagEmoji = "🇿🇦",
            animalName = "غزال السبرينغبوك (القفاز)",
            animalEmoji = "🦌",
            mapXPercent = 0.53f,
            mapYPercent = 0.75f,
            region = "جنوب القارة الإفريقية",
            colorHex = "#15803D",
            description = "جنوب إفريقيا 🇿🇦 - غزال السبرينغبوك فائق السرعة والرشاقة والقفز العالي وشعار منتخب الرغبي الوطني!"
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NationalAnimalsMapScreen(
    initialCountryId: String? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var mapDisplayMode by remember { mutableStateOf("illustrated") } // "illustrated" or "satellite_borders"

    var selectedSpot by remember {
        mutableStateOf(
            IllustratedMapSpotsProvider.spots.find { it.id == initialCountryId }
                ?: IllustratedMapSpotsProvider.spots.first()
        )
    }

    var showSpotDetailCard by remember { mutableStateOf(true) }

    // Illustrated Pan & Zoom Transform State
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Helper to focus on a country in the illustrated map
    fun focusCountryOnIllustratedMap(spot: IllustratedCountrySpot) {
        selectedSpot = spot
        showSpotDetailCard = true
        // Center the view on this country's coordinate with zoom
        val targetScale = 2.2f
        scale = targetScale
        // Calculate offset so the spot is in the center
        val targetOffsetX = (0.5f - spot.mapXPercent) * 800f * targetScale
        val targetOffsetY = (0.5f - spot.mapYPercent) * 500f * targetScale
        offset = Offset(targetOffsetX.coerceIn(-1200f, 1200f), targetOffsetY.coerceIn(-800f, 800f))
    }

    var osmMapView by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🗺️ خريطة العالم: الحيوانات مطبوعة في البلاد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "كل بلد مطبوع فيها حيوانها الوطني من وسطها إلى حدودها 🌍",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "عودة",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Reset View Button
                    IconButton(onClick = {
                        scale = 1f
                        offset = Offset.Zero
                        osmMapView?.let { mv ->
                            mv.controller.animateTo(GeoPoint(20.0, 10.0))
                            mv.controller.setZoom(2.8)
                        }
                    }) {
                        Icon(
                            Icons.Default.FitScreen,
                            contentDescription = "إعادة ضبط الخريطة",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryTeal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFE2F1F8)) // Soft ocean blue canvas
        ) {
            if (mapDisplayMode == "illustrated") {
                // ==========================================
                // 1. ILLUSTRATED WORLD ANIMAL MAP CANVAS
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(0.9f, 4.5f)
                                val maxOffsetX = 600f * scale
                                val maxOffsetY = 450f * scale
                                val newX = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                val newY = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                offset = Offset(newX, newY)
                            }
                        }
                ) {
                    // World Illustrated Animal Canvas Container
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // World Animal Art Image (Animals forming continents & filling borders)
                        Image(
                            painter = painterResource(id = R.drawable.world_map_animals_art_1787510619646),
                            contentDescription = "خريطة العالم المصورة للحيوانات والحدود",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )

                        // Interactive Country Badges & Pins Printed across the Map Canvas
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        ) {
                            val mapWidth = maxWidth
                            val mapHeight = maxHeight

                            IllustratedMapSpotsProvider.spots.forEach { spot ->
                                val isSelected = spot.id == selectedSpot.id

                                val pinX = mapWidth * spot.mapXPercent
                                val pinY = mapHeight * spot.mapYPercent

                                Box(
                                    modifier = Modifier
                                        .offset(x = pinX - 36.dp, y = pinY - 24.dp)
                                        .clickable {
                                            selectedSpot = spot
                                            showSpotDetailCard = true
                                        }
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(android.graphics.Color.parseColor(spot.colorHex)) else Color.White.copy(alpha = 0.95f),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.5.dp else 1.5.dp,
                                            color = Color(android.graphics.Color.parseColor(spot.colorHex))
                                        ),
                                        shadowElevation = if (isSelected) 8.dp else 4.dp
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = spot.animalEmoji,
                                                fontSize = if (isSelected) 16.sp else 13.sp
                                            )
                                            Column {
                                                Text(
                                                    text = "${spot.flagEmoji} ${spot.countryName}",
                                                    fontSize = if (isSelected) 10.5.sp else 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else Color(0xFF0F172A),
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = spot.animalName.take(12),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(android.graphics.Color.parseColor(spot.colorHex)),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Watermark hint on top
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = "يمكنك التكبير والسحب والتنقل بين بلدان العالم 🔍",
                                fontSize = 10.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. GEOGRAPHIC OSM BORDERS MAP CANVAS
                // ==========================================
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setMultiTouchControls(true)
                            isTilesScaledToDpi = true
                            setBuiltInZoomControls(false)
                            minZoomLevel = 2.5
                            maxZoomLevel = 18.0
                            val geoData = CountryBoundariesProvider.countries.find { it.id == selectedSpot.id }
                                ?: CountryBoundariesProvider.countries.first()
                            controller.setZoom(geoData.zoomLevel)
                            controller.setCenter(GeoPoint(geoData.centerLat, geoData.centerLng))
                            osmMapView = this
                        }
                    },
                    update = { mv ->
                        mv.overlays.clear()

                        CountryBoundariesProvider.countries.forEach { country ->
                            val isSelected = country.id == selectedSpot.id

                            if (country.boundaryPoints.size >= 3) {
                                val polygon = Polygon(mv).apply {
                                    points = country.boundaryPoints
                                    title = "${country.flagEmoji} ${country.countryName}"
                                    snippet = "${country.animalEmoji} ${country.animalName}"

                                    outlinePaint.color = country.strokeColorInt
                                    outlinePaint.strokeWidth = if (isSelected) 8.5f else 4.5f
                                    outlinePaint.style = android.graphics.Paint.Style.STROKE
                                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                                    outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                                    outlinePaint.isAntiAlias = true

                                    fillPaint.color = if (isSelected) {
                                        (country.fillColorInt and 0x00FFFFFF) or 0x55000000.toInt()
                                    } else {
                                        country.fillColorInt
                                    }
                                    fillPaint.style = android.graphics.Paint.Style.FILL
                                    fillPaint.isAntiAlias = true

                                    setOnClickListener { _, _, _ ->
                                        val matchSpot = IllustratedMapSpotsProvider.spots.find { it.id == country.id }
                                        if (matchSpot != null) {
                                            selectedSpot = matchSpot
                                        }
                                        showSpotDetailCard = true
                                        val worldCountry = WorldBordersProvider.countries.find { it.id == country.id }
                                        if (worldCountry != null) {
                                            val bbox = WorldBordersProvider.getBoundingBox(worldCountry)
                                            try {
                                                mv.zoomToBoundingBox(bbox, true, 80)
                                            } catch (e: Exception) {
                                                mv.controller.animateTo(GeoPoint(country.centerLat, country.centerLng))
                                                mv.controller.setZoom(country.zoomLevel)
                                            }
                                        } else {
                                            mv.controller.animateTo(GeoPoint(country.centerLat, country.centerLng))
                                            mv.controller.setZoom(country.zoomLevel)
                                        }
                                        true
                                    }
                                }
                                mv.overlays.add(polygon)
                            }

                            val stampMarker = Marker(mv).apply {
                                position = GeoPoint(country.centerLat, country.centerLng)
                                title = "${country.flagEmoji} ${country.countryName}"
                                snippet = "${country.animalEmoji} ${country.animalName}"
                                icon = CountryBoundariesProvider.createCountryAnimalStampDrawable(context, country)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                                setOnMarkerClickListener { _, _ ->
                                    val matchSpot = IllustratedMapSpotsProvider.spots.find { it.id == country.id }
                                    if (matchSpot != null) {
                                        selectedSpot = matchSpot
                                    }
                                    showSpotDetailCard = true
                                    val worldCountry = WorldBordersProvider.countries.find { it.id == country.id }
                                    if (worldCountry != null) {
                                        val bbox = WorldBordersProvider.getBoundingBox(worldCountry)
                                        try {
                                            mv.zoomToBoundingBox(bbox, true, 80)
                                        } catch (e: Exception) {
                                            mv.controller.animateTo(GeoPoint(country.centerLat, country.centerLng))
                                            mv.controller.setZoom(country.zoomLevel)
                                        }
                                    } else {
                                        mv.controller.animateTo(GeoPoint(country.centerLat, country.centerLng))
                                        mv.controller.setZoom(country.zoomLevel)
                                    }
                                    true
                                }
                            }
                            mv.overlays.add(stampMarker)
                        }

                        mv.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Top Mode Toggle & Horizontal Quick Country Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // Map Mode Switcher (Illustrated Art Map vs Geographic Satellite Borders)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Illustrated Animal Map Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { mapDisplayMode = "illustrated" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (mapDisplayMode == "illustrated") PrimaryTeal else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎨 الخريطة المصورة الفنية",
                                    color = if (mapDisplayMode == "illustrated") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Satellite & Geographic Borders Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    mapDisplayMode = "satellite_borders"
                                    val geoData = CountryBoundariesProvider.countries.find { it.id == selectedSpot.id }
                                    if (geoData != null && osmMapView != null) {
                                        osmMapView?.controller?.animateTo(GeoPoint(geoData.centerLat, geoData.centerLng))
                                        osmMapView?.controller?.setZoom(geoData.zoomLevel)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (mapDisplayMode == "satellite_borders") PrimaryTeal else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🌐 خريطة الحدود الجغرافية",
                                    color = if (mapDisplayMode == "satellite_borders") Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Country Filter Slider
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(IllustratedMapSpotsProvider.spots) { spot ->
                            val isSelected = spot.id == selectedSpot.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSpot = spot
                                    showSpotDetailCard = true
                                    if (mapDisplayMode == "illustrated") {
                                        focusCountryOnIllustratedMap(spot)
                                    } else {
                                        val geoData = CountryBoundariesProvider.countries.find { it.id == spot.id }
                                        if (geoData != null) {
                                            osmMapView?.controller?.animateTo(GeoPoint(geoData.centerLat, geoData.centerLng))
                                            osmMapView?.controller?.setZoom(geoData.zoomLevel)
                                        }
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${spot.flagEmoji} ${spot.countryName} ${spot.animalEmoji}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(android.graphics.Color.parseColor(spot.colorHex)),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Floating Card Displaying Details of Selected Country & Printed Animal
            AnimatedVisibility(
                visible = showSpotDetailCard,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(android.graphics.Color.parseColor(selectedSpot.colorHex)).copy(alpha = 0.15f),
                                modifier = Modifier.size(54.dp),
                                border = BorderStroke(2.dp, Color(android.graphics.Color.parseColor(selectedSpot.colorHex)))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = selectedSpot.animalEmoji, fontSize = 28.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${selectedSpot.flagEmoji} ${selectedSpot.countryName}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(android.graphics.Color.parseColor(selectedSpot.colorHex)).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = selectedSpot.region,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(android.graphics.Color.parseColor(selectedSpot.colorHex)),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "الحيوان الوطني: ${selectedSpot.animalName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(android.graphics.Color.parseColor(selectedSpot.colorHex))
                                )
                            }

                            IconButton(
                                onClick = { showSpotDetailCard = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = selectedSpot.description,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (mapDisplayMode == "illustrated") {
                                        focusCountryOnIllustratedMap(selectedSpot)
                                    } else {
                                        val geoData = CountryBoundariesProvider.countries.find { it.id == selectedSpot.id }
                                        if (geoData != null) {
                                            osmMapView?.controller?.animateTo(GeoPoint(geoData.centerLat, geoData.centerLng))
                                            osmMapView?.controller?.setZoom(geoData.zoomLevel + 1.2)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تكبير ورؤية حدود البلد 🔍", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val currentIdx = IllustratedMapSpotsProvider.spots.indexOfFirst { it.id == selectedSpot.id }
                                    val nextIdx = (currentIdx + 1) % IllustratedMapSpotsProvider.spots.size
                                    val nextSpot = IllustratedMapSpotsProvider.spots[nextIdx]
                                    selectedSpot = nextSpot
                                    if (mapDisplayMode == "illustrated") {
                                        focusCountryOnIllustratedMap(nextSpot)
                                    } else {
                                        val geoData = CountryBoundariesProvider.countries.find { it.id == nextSpot.id }
                                        if (geoData != null) {
                                            osmMapView?.controller?.animateTo(GeoPoint(geoData.centerLat, geoData.centerLng))
                                            osmMapView?.controller?.setZoom(geoData.zoomLevel)
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("الدولة التالية ➡️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Zoom In / Zoom Out Floating Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (mapDisplayMode == "illustrated") {
                            scale = (scale + 0.4f).coerceAtMost(4.5f)
                        } else {
                            osmMapView?.controller?.zoomIn()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = PrimaryTeal
                ) {
                    Icon(Icons.Default.Add, contentDescription = "تكبير")
                }

                SmallFloatingActionButton(
                    onClick = {
                        if (mapDisplayMode == "illustrated") {
                            scale = (scale - 0.4f).coerceAtLeast(0.9f)
                        } else {
                            osmMapView?.controller?.zoomOut()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    contentColor = PrimaryTeal
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "تصغير")
                }
            }
        }
    }
}
