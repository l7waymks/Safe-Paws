package com.example.ui.screens

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint

data class CountryBorderData(
    val id: String,
    val countryName: String,
    val flagEmoji: String,
    val animalName: String,
    val animalEmoji: String,
    val centerLat: Double,
    val centerLng: Double,
    val zoomLevel: Double,
    val strokeColorInt: Int,
    val fillColorInt: Int,
    val boundaryPoints: List<GeoPoint>,
    val description: String
)

object CountryBoundariesProvider {

    val countries: List<CountryBorderData> by lazy {
        val worldMap = WorldBordersProvider.countries.associateBy { it.id }

        listOf(
            // 1. Morocco 🇲🇦 - Lion 🦁
            CountryBorderData(
                id = "ma",
                countryName = "المغرب",
                flagEmoji = "🇲🇦",
                animalName = "أسد الأطلس البربري",
                animalEmoji = "🦁",
                centerLat = 29.5000,
                centerLng = -9.0000,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#E11D48"), // Vivid Crimson Red
                fillColorInt = android.graphics.Color.parseColor("#33E11D48"),   // 20% Alpha Fill
                boundaryPoints = worldMap["ma"]?.boundaryPoints ?: emptyList(),
                description = "المملكة المغربية 🇲🇦 - أسد الأطلس البربري رمز الشجاعة والملوكية التاريخية ولقب أسود الأطلس!"
            ),

            // 2. Algeria 🇩🇿 - Fennec Fox 🦊
            CountryBorderData(
                id = "dz",
                countryName = "الجزائر",
                flagEmoji = "🇩🇿",
                animalName = "فنك الصحراء (الأفناك)",
                animalEmoji = "🦊",
                centerLat = 28.0339,
                centerLng = 1.6596,
                zoomLevel = 5.0,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["dz"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية الجزائرية 🇩🇿 - ثعلب الفنك رمز الذكاء والسرعة ومحاربو الصحراء في قلب الرمال!"
            ),

            // 3. Tunisia 🇹🇳 - Falcon 🦅
            CountryBorderData(
                id = "tn",
                countryName = "تونس",
                flagEmoji = "🇹🇳",
                animalName = "عقاب قرطاج وصقر السهوب",
                animalEmoji = "🦅",
                centerLat = 34.5000,
                centerLng = 9.5375,
                zoomLevel = 6.6,
                strokeColorInt = android.graphics.Color.parseColor("#E11D48"),
                fillColorInt = android.graphics.Color.parseColor("#33E11D48"),
                boundaryPoints = worldMap["tn"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية التونسية 🇹🇳 - نسور قرطاج رمز التاريخ العريق والشجاعة في شمال إفريقيا!"
            ),

            // 4. Libya 🇱🇾 - Desert Eagle 🦅
            CountryBorderData(
                id = "ly",
                countryName = "ليبيا",
                flagEmoji = "🇱🇾",
                animalName = "صقر الصحراء الحر",
                animalEmoji = "🦅",
                centerLat = 26.3351,
                centerLng = 17.2283,
                zoomLevel = 5.0,
                strokeColorInt = android.graphics.Color.parseColor("#059669"),
                fillColorInt = android.graphics.Color.parseColor("#33059669"),
                boundaryPoints = worldMap["ly"]?.boundaryPoints ?: emptyList(),
                description = "دولة ليبيا 🇱🇾 - صقر الصحراء الحر رمز العزة والشهامة وأطول شاطئ متوسطي في إفريقيا!"
            ),

            // 5. Egypt 🇪🇬 - Eagle 🦅
            CountryBorderData(
                id = "eg",
                countryName = "مصر",
                flagEmoji = "🇪🇬",
                animalName = "عقاب السهوب وصقر صلاح الدين",
                animalEmoji = "🦅",
                centerLat = 26.8206,
                centerLng = 30.8025,
                zoomLevel = 5.4,
                strokeColorInt = android.graphics.Color.parseColor("#D97706"),
                fillColorInt = android.graphics.Color.parseColor("#33D97706"),
                boundaryPoints = worldMap["eg"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية مصر العربية 🇪🇬 - نسر صلاح الدين الذهبي رمز القوة والعزة والسيادة التاريخية!"
            ),

            // 6. Sudan 🇸🇩 - Secretary Bird 🦅
            CountryBorderData(
                id = "sd",
                countryName = "السودان",
                flagEmoji = "🇸🇩",
                animalName = "صقر الجديان (Secretarybird)",
                animalEmoji = "🦅",
                centerLat = 15.0000,
                centerLng = 30.0000,
                zoomLevel = 5.0,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["sd"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية السودان 🇸🇩 - صقر الجديان رمز النبالة والشموخ الوطني في ملتقى النيلين!"
            ),

            // 7. Mauritania 🇲🇷 - Desert Gazelle 🦌
            CountryBorderData(
                id = "mr",
                countryName = "موريتانيا",
                flagEmoji = "🇲🇷",
                animalName = "مها الصحراء وغزال المهر",
                animalEmoji = "🦌",
                centerLat = 20.2540,
                centerLng = -10.3333,
                zoomLevel = 5.2,
                strokeColorInt = android.graphics.Color.parseColor("#059669"),
                fillColorInt = android.graphics.Color.parseColor("#33059669"),
                boundaryPoints = worldMap["mr"]?.boundaryPoints ?: emptyList(),
                description = "موريتانيا 🇲🇷 - مها الصحراء رمز الصبر والجمال والوفاق في بلاد المليون شاعر!"
            ),

            // 8. Saudi Arabia 🇸🇦 - Falcon & Camel 🦅🐪
            CountryBorderData(
                id = "sa",
                countryName = "المملكة العربية السعودية",
                flagEmoji = "🇸🇦",
                animalName = "الصقر الحر والجمل العربي",
                animalEmoji = "🦅",
                centerLat = 23.8859,
                centerLng = 45.0792,
                zoomLevel = 5.2,
                strokeColorInt = android.graphics.Color.parseColor("#059669"),
                fillColorInt = android.graphics.Color.parseColor("#33059669"),
                boundaryPoints = worldMap["sa"]?.boundaryPoints ?: emptyList(),
                description = "المملكة العربية السعودية 🇸🇦 - الصقر الحر والجمل رمز الشموخ والأصالة والصبر وعزة النفس!"
            ),

            // 9. UAE 🇦🇪 - Arabian Oryx 🦄
            CountryBorderData(
                id = "ae",
                countryName = "الإمارات",
                flagEmoji = "🇦🇪",
                animalName = "المها العربي والصقر الحر",
                animalEmoji = "🦄",
                centerLat = 24.0000,
                centerLng = 54.0000,
                zoomLevel = 6.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["ae"]?.boundaryPoints ?: emptyList(),
                description = "الإمارات العربية المتحدة 🇦🇪 - المها العربي رمز النقاء والأصالة البيئية والتراث الأصيل!"
            ),

            // 10. Qatar 🇶🇦 - Arabian Oryx 🦄
            CountryBorderData(
                id = "qa",
                countryName = "قطر",
                flagEmoji = "🇶🇦",
                animalName = "المها العربي الوضيحي",
                animalEmoji = "🦄",
                centerLat = 25.3548,
                centerLng = 51.1839,
                zoomLevel = 8.2,
                strokeColorInt = android.graphics.Color.parseColor("#881337"),
                fillColorInt = android.graphics.Color.parseColor("#33881337"),
                boundaryPoints = worldMap["qa"]?.boundaryPoints ?: emptyList(),
                description = "دولة قطر 🇶🇦 - المها الوضيحي ذو القرنين الشامخين رمز الوطنية والأناقة الصحراوية!"
            ),

            // 11. Kuwait 🇰🇼 - Arabian Camel 🐪
            CountryBorderData(
                id = "kw",
                countryName = "الكويت",
                flagEmoji = "🇰🇼",
                animalName = "الصقر الحر والجمل",
                animalEmoji = "🦅",
                centerLat = 29.3117,
                centerLng = 47.4818,
                zoomLevel = 8.0,
                strokeColorInt = android.graphics.Color.parseColor("#059669"),
                fillColorInt = android.graphics.Color.parseColor("#33059669"),
                boundaryPoints = worldMap["kw"]?.boundaryPoints ?: emptyList(),
                description = "دولة الكويت 🇰🇼 - الصقر والجمل رمز الكرم والتجارة التاريخية وعزة النفس!"
            ),

            // 12. Bahrain 🇧🇭 - Arabian Gazelle 🦌
            CountryBorderData(
                id = "bh",
                countryName = "البحرين",
                flagEmoji = "🇧🇭",
                animalName = "غزال الريم والبلبل البحريني",
                animalEmoji = "🦌",
                centerLat = 26.0667,
                centerLng = 50.5577,
                zoomLevel = 9.5,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["bh"]?.boundaryPoints ?: emptyList(),
                description = "مملكة البحرين 🇧🇭 - غزال الريم رمز الرقة والجمال الطبيعي وتاريخ دلمون العريق!"
            ),

            // 13. Oman 🇴🇲 - Arabian Leopard 🐆
            CountryBorderData(
                id = "om",
                countryName = "سلطنة عمان",
                flagEmoji = "🇴🇲",
                animalName = "النمر العربي والمها",
                animalEmoji = "🐆",
                centerLat = 21.4735,
                centerLng = 55.9754,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["om"]?.boundaryPoints ?: emptyList(),
                description = "سلطنة عمان 🇴🇲 - النمر العربي النادر في جبال ظفار رمز الحكمة والشجاعة والهدوء!"
            ),

            // 14. Yemen 🇾🇪 - Arabian Leopard 🐆
            CountryBorderData(
                id = "ye",
                countryName = "اليمن",
                flagEmoji = "🇾🇪",
                animalName = "النمر العربي والنسر",
                animalEmoji = "🐆",
                centerLat = 15.5527,
                centerLng = 48.5164,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["ye"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية اليمنية 🇾🇪 - النمر العربي رمز الهيبة وأصالة جبال اليمن السعيد وسقطرى!"
            ),

            // 15. Jordan 🇯🇴 - Arabian Oryx 🦄
            CountryBorderData(
                id = "jo",
                countryName = "الأردن",
                flagEmoji = "🇯🇴",
                animalName = "المها العربي والعصفور الوردي",
                animalEmoji = "🦄",
                centerLat = 31.0000,
                centerLng = 36.5000,
                zoomLevel = 6.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["jo"]?.boundaryPoints ?: emptyList(),
                description = "المملكة الأردنية الهاشمية 🇯🇴 - المها العربي رمز الحفاظ على البيئة وأصالة البتراء ورم!"
            ),

            // 16. Palestine 🇵🇸 - Sunbird 🐦
            CountryBorderData(
                id = "ps",
                countryName = "فلسطين",
                flagEmoji = "🇵🇸",
                animalName = "تمير فلسطين وغزال الجبل",
                animalEmoji = "🐦",
                centerLat = 31.9522,
                centerLng = 35.2332,
                zoomLevel = 7.8,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["ps"]?.boundaryPoints ?: emptyList(),
                description = "دولة فلسطين 🇵🇸 - عصفور الشمس الفلسطيني رمز الحرية والتحليق فوق أراضي القدس والزيتون!"
            ),

            // 17. Iraq 🇮🇶 - Mesopotamian Lion 🦁
            CountryBorderData(
                id = "iq",
                countryName = "العراق",
                flagEmoji = "🇮🇶",
                animalName = "أسد بابل والشنار",
                animalEmoji = "🦁",
                centerLat = 33.2232,
                centerLng = 43.6793,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["iq"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية العراق 🇮🇶 - أسد بابل التاريخي رمز الحضارة السومرية والبابلية وقوة دجلة والفرات!"
            ),

            // 18. Syria 🇸🇾 - Syrian Bear 🐻
            CountryBorderData(
                id = "sy",
                countryName = "سوريا",
                flagEmoji = "🇸🇾",
                animalName = "الدب البني السوري والصقر",
                animalEmoji = "🐻",
                centerLat = 34.8021,
                centerLng = 38.9968,
                zoomLevel = 6.4,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["sy"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية العربية السورية 🇸🇾 - الصقر السوري رمز العنفوان وتاريخ دمشق العريق وأشجار الياسمين!"
            ),

            // 19. Lebanon 🇱🇧 - Striped Hyena 🐺
            CountryBorderData(
                id = "lb",
                countryName = "لبنان",
                flagEmoji = "🇱🇧",
                animalName = "الضبع المخطط والسنونو",
                animalEmoji = "🐺",
                centerLat = 33.8547,
                centerLng = 35.8623,
                zoomLevel = 8.4,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["lb"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية اللبنانية 🇱🇧 - رمز جبال الأرز الخضراء وجمال الطبيعة المتوسطية الساحرة!"
            ),

            // 20. France 🇫🇷 - Rooster 🐓
            CountryBorderData(
                id = "fr",
                countryName = "فرنسا",
                flagEmoji = "🇫🇷",
                animalName = "الديك الغالي (Le Coq Gaulois)",
                animalEmoji = "🐓",
                centerLat = 46.6033,
                centerLng = 2.2137,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#2563EB"),
                fillColorInt = android.graphics.Color.parseColor("#332563EB"),
                boundaryPoints = worldMap["fr"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية الفرنسية 🇫🇷 - الديك الغالي رمز اليقظة والفخر الوطني والشجاعة عند بزوغ الفجر!"
            ),

            // 21. Spain 🇪🇸 - Bull 🐂
            CountryBorderData(
                id = "es",
                countryName = "إسبانيا",
                flagEmoji = "🇪🇸",
                animalName = "الثور الإسباني (El Toro Bravo)",
                animalEmoji = "🐂",
                centerLat = 40.4637,
                centerLng = -3.7492,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["es"]?.boundaryPoints ?: emptyList(),
                description = "مملكة إسبانيا 🇪🇸 - الثور الإسباني رمز القوة والعنفوان والكرامة في الفلكلور والتراث الوطني!"
            ),

            // 22. Germany 🇩🇪 - Federal Eagle 🦅
            CountryBorderData(
                id = "de",
                countryName = "ألمانيا",
                flagEmoji = "🇩🇪",
                animalName = "النسر الفيدرالي (Bundesadler)",
                animalEmoji = "🦅",
                centerLat = 51.1657,
                centerLng = 10.4515,
                zoomLevel = 6.0,
                strokeColorInt = android.graphics.Color.parseColor("#18181B"),
                fillColorInt = android.graphics.Color.parseColor("#33FBBF24"),
                boundaryPoints = worldMap["de"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية ألمانيا الاتحادية 🇩🇪 - النسر الفيدرالي الشعار الوطني الأقدم للحكم والسيادة والاستقرار!"
            ),

            // 23. Italy 🇮🇹 - Italian Wolf 🐺
            CountryBorderData(
                id = "it",
                countryName = "إيطاليا",
                flagEmoji = "🇮🇹",
                animalName = "الذئب الإيطالي الأبينيني",
                animalEmoji = "🐺",
                centerLat = 42.5000,
                centerLng = 12.5674,
                zoomLevel = 5.8,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["it"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية الإيطالية 🇮🇹 - الذئب الإيطالي رمز أسطورة تأسيس روما وحماية الطبيعة في الألب!"
            ),

            // 24. United Kingdom 🇬🇧 - British Lion 🦁
            CountryBorderData(
                id = "gb",
                countryName = "بريطانيا (المملكة المتحدة)",
                flagEmoji = "🇬🇧",
                animalName = "الأسد البريطاني واليونيكورن",
                animalEmoji = "🦁",
                centerLat = 55.3781,
                centerLng = -3.4360,
                zoomLevel = 6.0,
                strokeColorInt = android.graphics.Color.parseColor("#1E3A8A"),
                fillColorInt = android.graphics.Color.parseColor("#331E3A8A"),
                boundaryPoints = worldMap["gb"]?.boundaryPoints ?: emptyList(),
                description = "المملكة المتحدة 🇬🇧 - الأسد البريطاني رمز الشجاعة والمهابة الملكية على شعار النبالة والعرش!"
            ),

            // 25. Turkey 🇹🇷 - Gray Wolf 🐺
            CountryBorderData(
                id = "tr",
                countryName = "تركيا",
                flagEmoji = "🇹🇷",
                animalName = "الذئب الرمادي (Bozkurt)",
                animalEmoji = "🐺",
                centerLat = 38.9637,
                centerLng = 35.2433,
                zoomLevel = 5.6,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["tr"]?.boundaryPoints ?: emptyList(),
                description = "الجمهورية التركية 🇹🇷 - الذئب الرمادي رمز الحرية والقيادة والوفاء في التاريخ الأناضولي!"
            ),

            // 26. United States 🇺🇸 - Bald Eagle 🦅
            CountryBorderData(
                id = "us",
                countryName = "الولايات المتحدة",
                flagEmoji = "🇺🇸",
                animalName = "النسر الأصلع والبيسون",
                animalEmoji = "🦅",
                centerLat = 37.0902,
                centerLng = -95.7129,
                zoomLevel = 4.2,
                strokeColorInt = android.graphics.Color.parseColor("#1D4ED8"),
                fillColorInt = android.graphics.Color.parseColor("#331D4ED8"),
                boundaryPoints = worldMap["us"]?.boundaryPoints ?: emptyList(),
                description = "الولايات المتحدة الأمريكية 🇺🇸 - النسر الأصلع رمز الحرية والقوة والشجاعة المستقلة منذ 1782!"
            ),

            // 27. Canada 🇨🇦 - Beaver 🦫
            CountryBorderData(
                id = "ca",
                countryName = "كندا",
                flagEmoji = "🇨🇦",
                animalName = "القندس الكندي (Beaver)",
                animalEmoji = "🦫",
                centerLat = 56.1304,
                centerLng = -106.3468,
                zoomLevel = 3.8,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["ca"]?.boundaryPoints ?: emptyList(),
                description = "كندا 🇨🇦 - القندس الكندي رمز العمل الدؤوب والهندسة الطبيعية وبناء السدود والمثابرة!"
            ),

            // 28. Russia 🇷🇺 - Brown Bear 🐻
            CountryBorderData(
                id = "ru",
                countryName = "روسيا",
                flagEmoji = "🇷🇺",
                animalName = "الدب البني الأوراسي",
                animalEmoji = "🐻",
                centerLat = 61.5240,
                centerLng = 105.3188,
                zoomLevel = 3.2,
                strokeColorInt = android.graphics.Color.parseColor("#7C2D12"),
                fillColorInt = android.graphics.Color.parseColor("#337C2D12"),
                boundaryPoints = worldMap["ru"]?.boundaryPoints ?: emptyList(),
                description = "روسيا الاتحادية 🇷🇺 - الدب الروسي رمز الصلابة والقوة والقدرة على الصمود في التايغا وسيبيريا!"
            ),

            // 29. China 🇨🇳 - Panda 🐼
            CountryBorderData(
                id = "cn",
                countryName = "الصين",
                flagEmoji = "🇨🇳",
                animalName = "الباندا العملاقة (Giant Panda)",
                animalEmoji = "🐼",
                centerLat = 35.8617,
                centerLng = 104.1954,
                zoomLevel = 4.0,
                strokeColorInt = android.graphics.Color.parseColor("#B91C1C"),
                fillColorInt = android.graphics.Color.parseColor("#33B91C1C"),
                boundaryPoints = worldMap["cn"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية الصين الشعبية 🇨🇳 - الباندا العملاقة الكنز الوطني وسفير السلام والتعايش العالمي!"
            ),

            // 30. India 🇮🇳 - Bengal Tiger 🐅
            CountryBorderData(
                id = "in",
                countryName = "الهند",
                flagEmoji = "🇮🇳",
                animalName = "النمر البنغالي الملكي",
                animalEmoji = "🐅",
                centerLat = 20.5937,
                centerLng = 78.9629,
                zoomLevel = 4.8,
                strokeColorInt = android.graphics.Color.parseColor("#D97706"),
                fillColorInt = android.graphics.Color.parseColor("#33D97706"),
                boundaryPoints = worldMap["in"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية الهند 🇮🇳 - النمر البنغالي الملكي رمز الهيبة والسرعة والقوة في الغابات الهندية!"
            ),

            // 31. Brazil 🇧🇷 - Jaguar 🐆
            CountryBorderData(
                id = "br",
                countryName = "البرازيل",
                flagEmoji = "🇧🇷",
                animalName = "اليغور المرقط (Jaguar)",
                animalEmoji = "🐆",
                centerLat = -14.2350,
                centerLng = -51.9253,
                zoomLevel = 4.2,
                strokeColorInt = android.graphics.Color.parseColor("#16A34A"),
                fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
                boundaryPoints = worldMap["br"]?.boundaryPoints ?: emptyList(),
                description = "جمهورية البرازيل الاتحادية 🇧🇷 - اليغور المرقط ملك غابات الأمازون وأكبر مفترس قطي في الأمريكتين!"
            ),

            // 32. Australia 🇦🇺 - Kangaroo 🦘
            CountryBorderData(
                id = "au",
                countryName = "أستراليا",
                flagEmoji = "🇦🇺",
                animalName = "الكنغر الأحمر والكوالا",
                animalEmoji = "🦘",
                centerLat = -25.2744,
                centerLng = 133.7751,
                zoomLevel = 4.2,
                strokeColorInt = android.graphics.Color.parseColor("#EA580C"),
                fillColorInt = android.graphics.Color.parseColor("#33EA580C"),
                boundaryPoints = worldMap["au"]?.boundaryPoints ?: emptyList(),
                description = "أستراليا 🇦🇺 - الكنغر الأحمر الذي لا يقفز للوراء دلالة على الأمة التي تمضي دائماً للأمام!"
            ),

            // 33. Japan 🇯🇵 - Japanese Macaque 🐒
            CountryBorderData(
                id = "jp",
                countryName = "اليابان",
                flagEmoji = "🇯🇵",
                animalName = "قرد الثلج والكركي الياباني",
                animalEmoji = "🐒",
                centerLat = 36.2048,
                centerLng = 138.2529,
                zoomLevel = 5.6,
                strokeColorInt = android.graphics.Color.parseColor("#DC2626"),
                fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
                boundaryPoints = worldMap["jp"]?.boundaryPoints ?: emptyList(),
                description = "اليابان 🇯🇵 - طائر الكركي ذو التاج الأحمر وقرد الثلج رمز طول العمر والوفاء والسكينة!"
            )
        )
    }

    // Create custom printed badge overlay for the country directly centered inside the territory
    fun createCountryAnimalStampDrawable(
        context: Context,
        data: CountryBorderData
    ): Drawable {
        val density = context.resources.displayMetrics.density
        val width = (130 * density).toInt()
        val height = (58 * density).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33000000")
            style = Paint.Style.FILL
        }
        val shadowRect = RectF(4 * density, 4 * density, width - (2 * density), height.toFloat())
        canvas.drawRoundRect(shadowRect, 16 * density, 16 * density, shadowPaint)

        // Draw container card background
        paint.color = Color.parseColor("#F8FAFC") // Pure clean off-white card
        paint.style = Paint.Style.FILL
        val cardRect = RectF(2 * density, 2 * density, width - (4 * density), height - (4 * density))
        canvas.drawRoundRect(cardRect, 14 * density, 14 * density, paint)

        // Draw stroke colored border matching country theme
        paint.color = data.strokeColorInt
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.2f * density
        canvas.drawRoundRect(cardRect, 14 * density, 14 * density, paint)

        // Draw Animal Emoji badge circle
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = data.strokeColorInt
            style = Paint.Style.FILL
        }
        val circleRadius = 16 * density
        val circleCenterX = cardRect.left + (20 * density)
        val circleCenterY = cardRect.centerY()
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, circlePaint)

        // Inner white circle
        circlePaint.color = Color.WHITE
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius - (2 * density), circlePaint)

        // Draw Emoji
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.textSize = 18 * density
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val emojiY = circleCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(data.animalEmoji, circleCenterX, emojiY, paint)

        // Draw Country Flag & Name Text
        val textStartX = circleCenterX + (20 * density)
        
        // Country Name & Flag
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A") // Slate 900
            textSize = 11.5f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val titleText = "${data.flagEmoji} ${data.countryName}"
        canvas.drawText(titleText, textStartX, cardRect.centerY() - (3 * density), titlePaint)

        // Animal Name Subtitle
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = data.strokeColorInt
            textSize = 9.5f * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val animalShort = if (data.animalName.length > 15) data.animalName.take(13) + ".." else data.animalName
        canvas.drawText(animalShort, textStartX, cardRect.centerY() + (12 * density), subPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
