package com.example.ui.screens

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Overlay

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

    // Helper to generate polygon GeoPoints safely
    private fun points(vararg coords: Pair<Double, Double>): List<GeoPoint> {
        return coords.map { GeoPoint(it.first, it.second) }
    }

    val countries: List<CountryBorderData> = listOf(
        // 1. Morocco 🇲🇦 - Lion 🦁
        CountryBorderData(
            id = "ma",
            countryName = "المغرب",
            flagEmoji = "🇲🇦",
            animalName = "أسد الأطلس البربري",
            animalEmoji = "🦁",
            centerLat = 31.7917,
            centerLng = -7.0926,
            zoomLevel = 6.0,
            strokeColorInt = android.graphics.Color.parseColor("#E11D48"), // Vivid Crimson Red
            fillColorInt = android.graphics.Color.parseColor("#33E11D48"),   // 20% Alpha Fill
            boundaryPoints = points(
                35.92 to -5.35,  // Tangier / Strait of Gibraltar
                35.18 to -3.00,  // Nador Mediterranean
                34.80 to -1.85,  // Saidia / Berkane
                34.20 to -2.00,  // Oujda
                32.30 to -1.25,  // Figuig
                30.50 to -4.50,  // Errachidia / Zagora
                28.00 to -8.65,  // Smara
                27.15 to -13.20, // Western Sahara border
                21.40 to -16.90, // Lagouira / Dakhla southernmost
                23.70 to -15.95, // Atlantic Coast South
                27.15 to -13.20, // Tarfaya
                30.42 to -9.60,  // Agadir
                31.50 to -9.77,  // Essaouira
                33.57 to -7.58,  // Casablanca
                34.02 to -6.83,  // Rabat
                35.78 to -5.81,  // Asilah
                35.92 to -5.35   // Closed polygon
            ),
            description = "المملكة المغربية 🇲🇦 - أسد الأطلس البربري رمز الشجاعة والملوكية التاريخية ولقب أسود الأطلس!"
        ),

        // 2. France 🇫🇷 - Rooster 🐓
        CountryBorderData(
            id = "fr",
            countryName = "فرنسا",
            flagEmoji = "🇫🇷",
            animalName = "الديك الغالي (Le Coq Gaulois)",
            animalEmoji = "🐓",
            centerLat = 46.6033,
            centerLng = 2.2137,
            zoomLevel = 5.8,
            strokeColorInt = android.graphics.Color.parseColor("#2563EB"), // Royal Blue
            fillColorInt = android.graphics.Color.parseColor("#332563EB"),
            boundaryPoints = points(
                51.05 to 2.53,   // Dunkirk / North Sea
                50.00 to 4.25,   // Belgian border
                49.45 to 6.36,   // Luxembourg border
                48.97 to 8.23,   // Strasbourg / Rhine River
                47.58 to 7.58,   // Basel / Swiss border
                46.40 to 6.80,   // Lake Geneva / Alps
                44.00 to 7.50,   // Nice / Italian border
                43.10 to 6.00,   // Toulon Mediterranean
                43.30 to 3.50,   // Montpellier
                42.45 to 3.15,   // Perpignan / Spanish Pyrenees
                42.75 to 0.00,   // Pyrenees mountains
                43.35 to -1.78,  // Biarritz / Bay of Biscay
                46.15 to -1.15,  // La Rochelle
                48.30 to -4.70,  // Brest / Brittany
                49.70 to -1.90,  // Cherbourg / Normandy
                50.10 to 1.60,   // Calais
                51.05 to 2.53    // Dunkirk closed
            ),
            description = "الجمهورية الفرنسية 🇫🇷 - الديك الغالي رمز اليقظة والفخر الوطني والشجاعة عند بزوغ الفجر!"
        ),

        // 3. Spain 🇪🇸 - Bull 🐂
        CountryBorderData(
            id = "es",
            countryName = "إسبانيا",
            flagEmoji = "🇪🇸",
            animalName = "الثور الإسباني (El Toro Bravo)",
            animalEmoji = "🐂",
            centerLat = 40.4637,
            centerLng = -3.7492,
            zoomLevel = 5.8,
            strokeColorInt = android.graphics.Color.parseColor("#DC2626"), // Spanish Red
            fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
            boundaryPoints = points(
                43.78 to -7.70,  // Galicia north
                43.50 to -4.00,  // Cantabria
                43.35 to -1.78,  // Basque Country / French border
                42.75 to 0.00,   // Pyrenees
                42.45 to 3.15,   // Costa Brava
                41.38 to 2.17,   // Barcelona
                39.46 to -0.37,  // Valencia
                37.60 to -0.98,  // Cartagena
                36.72 to -4.42,  // Malaga
                36.01 to -5.60,  // Tarifa / Strait of Gibraltar
                36.50 to -6.28,  // Cadiz
                37.18 to -7.42,  // Huelva / Portugal border
                39.70 to -7.10,  // Extremadura
                41.80 to -6.75,  // Zamora
                42.15 to -8.70,  // Vigo Atlantic
                43.78 to -7.70   // Galicia closed
            ),
            description = "مملكة إسبانيا 🇪🇸 - الثور الإسباني رمز القوة والعنفوان والكرامة في الفلكلور والتراث الوطني!"
        ),

        // 4. Saudi Arabia 🇸🇦 - Falcon & Camel 🦅🐪
        CountryBorderData(
            id = "sa",
            countryName = "المملكة العربية السعودية",
            flagEmoji = "🇸🇦",
            animalName = "الصقر الحر والجمل العربي",
            animalEmoji = "🦅",
            centerLat = 23.8859,
            centerLng = 45.0792,
            zoomLevel = 5.2,
            strokeColorInt = android.graphics.Color.parseColor("#059669"), // Emerald Green
            fillColorInt = android.graphics.Color.parseColor("#33059669"),
            boundaryPoints = points(
                31.50 to 37.00,  // Tabuk / Jordan border
                32.15 to 39.30,  // Arar / Iraq border
                30.00 to 47.50,  // Kuwait border
                28.00 to 48.80,  // Arabian Gulf
                26.00 to 50.00,  // Dammam / Bahrain
                24.50 to 51.50,  // Qatar / UAE border
                22.50 to 55.00,  // Empty Quarter (Rub' al Khali)
                18.00 to 52.00,  // Oman border
                16.50 to 43.50,  // Yemen border
                16.90 to 42.50,  // Jazan Red Sea
                21.48 to 39.18,  // Jeddah / Makkah
                24.50 to 37.30,  // Yanbu Red Sea
                28.00 to 35.00,  // Duba
                29.30 to 34.90,  // Gulf of Aqaba
                31.50 to 37.00   // Closed Tabuk
            ),
            description = "المملكة العربية السعودية 🇸🇦 - الصقر الحر والجمل رمز الشموخ والأصالة والصبر وعزة النفس!"
        ),

        // 5. Algeria 🇩🇿 - Fennec Fox 🦊
        CountryBorderData(
            id = "dz",
            countryName = "الجزائر",
            flagEmoji = "🇩🇿",
            animalName = "فنك الصحراء (الأفناك)",
            animalEmoji = "🦊",
            centerLat = 28.0339,
            centerLng = 1.6596,
            zoomLevel = 5.0,
            strokeColorInt = android.graphics.Color.parseColor("#16A34A"), // Algerian Green
            fillColorInt = android.graphics.Color.parseColor("#3316A34A"),
            boundaryPoints = points(
                36.90 to 7.75,   // Annaba Mediterranean
                36.75 to 3.05,   // Algiers
                35.70 to -0.65,  // Oran
                34.80 to -1.85,  // Moroccan border
                32.30 to -1.25,  // Bechar border
                28.00 to -8.65,  // Tindouf
                25.00 to -4.80,  // Mali border
                20.00 to 3.50,   // Niger border South
                23.50 to 11.90,  // Djanet / Libya
                30.20 to 9.50,   // Tunisia border
                36.90 to 7.75    // Annaba closed
            ),
            description = "الجمهورية الجزائرية 🇩🇿 - ثعلب الفنك رمز الذكاء والسرعة ومحاربو الصحراء في قلب الرمال!"
        ),

        // 6. Egypt 🇪🇬 - Eagle 🦅
        CountryBorderData(
            id = "eg",
            countryName = "مصر",
            flagEmoji = "🇪🇬",
            animalName = "عقاب السهوب وصقر صلاح الدين",
            animalEmoji = "🦅",
            centerLat = 26.8206,
            centerLng = 30.8025,
            zoomLevel = 5.4,
            strokeColorInt = android.graphics.Color.parseColor("#D97706"), // Golden Amber
            fillColorInt = android.graphics.Color.parseColor("#33D97706"),
            boundaryPoints = points(
                31.60 to 25.10,  // Sallum / Libya
                31.20 to 29.90,  // Alexandria
                31.40 to 32.30,  // Port Said
                31.30 to 34.20,  // Rafah / Gaza border
                29.55 to 34.95,  // Taba / Gulf of Aqaba
                27.85 to 34.30,  // Sharm El Sheikh / Red Sea
                22.00 to 36.90,  // Halayeb / Sudan border
                22.00 to 25.00,  // Western border South
                31.60 to 25.10   // Sallum closed
            ),
            description = "جمهورية مصر العربية 🇪🇬 - نسر صلاح الدين الذهبي رمز القوة والعزة والسيادة التاريخية!"
        ),

        // 7. Australia 🇦🇺 - Kangaroo 🦘
        CountryBorderData(
            id = "au",
            countryName = "أستراليا",
            flagEmoji = "🇦🇺",
            animalName = "الكنغر الأحمر والكوالا",
            animalEmoji = "🦘",
            centerLat = -25.2744,
            centerLng = 133.7751,
            zoomLevel = 4.2,
            strokeColorInt = android.graphics.Color.parseColor("#EA580C"), // Ochre Orange
            fillColorInt = android.graphics.Color.parseColor("#33EA580C"),
            boundaryPoints = points(
                -11.00 to 142.50, // Cape York
                -19.25 to 146.80, // Townsville
                -27.50 to 153.00, // Brisbane
                -33.86 to 151.20, // Sydney
                -38.00 to 145.00, // Melbourne
                -35.00 to 136.00, // Adelaide
                -31.50 to 115.80, // Perth / Indian Ocean
                -21.80 to 114.10, // Exmouth
                -12.45 to 130.80, // Darwin
                -11.00 to 142.50  // Cape York closed
            ),
            description = "أستراليا 🇦🇺 - الكنغر الأحمر الذي لا يقفز للوراء دلالة على الأمة التي تمضي دائماً للأمام!"
        ),

        // 8. China 🇨🇳 - Panda 🐼
        CountryBorderData(
            id = "cn",
            countryName = "الصين",
            flagEmoji = "🇨🇳",
            animalName = "الباندا العملاقة (Giant Panda)",
            animalEmoji = "🐼",
            centerLat = 35.8617,
            centerLng = 104.1954,
            zoomLevel = 4.2,
            strokeColorInt = android.graphics.Color.parseColor("#B91C1C"), // China Red
            fillColorInt = android.graphics.Color.parseColor("#33B91C1C"),
            boundaryPoints = points(
                53.50 to 123.00, // Mohe North
                48.00 to 134.50, // Heilongjiang East
                40.00 to 124.00, // Liaoning / Yellow Sea
                31.20 to 121.50, // Shanghai
                22.50 to 114.00, // Shenzhen / Hong Kong
                21.50 to 108.00, // Guangxi Coast
                28.00 to 97.00,  // Tibet / Himalayas
                35.00 to 74.00,  // Xinjiang West
                48.00 to 86.00,  // Altay North
                53.50 to 123.00  // Mohe closed
            ),
            description = "جمهورية الصين الشعبية 🇨🇳 - الباندا العملاقة الكنز الوطني وسفير السلام والتعايش العالمي!"
        ),

        // 9. United States 🇺🇸 - Bald Eagle 🦅
        CountryBorderData(
            id = "us",
            countryName = "الولايات المتحدة",
            flagEmoji = "🇺🇸",
            animalName = "النسر الأصلع والبيسون",
            animalEmoji = "🦅",
            centerLat = 37.0902,
            centerLng = -95.7129,
            zoomLevel = 4.2,
            strokeColorInt = android.graphics.Color.parseColor("#1D4ED8"), // Navy Blue
            fillColorInt = android.graphics.Color.parseColor("#331D4ED8"),
            boundaryPoints = points(
                49.00 to -123.00, // Seattle / Canada border
                49.00 to -95.00,  // Lake of the Woods
                45.00 to -71.00,  // Maine / East Canada border
                41.00 to -72.00,  // New York Atlantic
                30.00 to -81.00,  // Florida East
                25.00 to -80.50,  // Miami
                29.50 to -85.00,  // Gulf Coast
                26.00 to -97.00,  // Texas / Mexico border
                32.50 to -117.00, // San Diego Pacific
                38.00 to -123.00, // San Francisco
                48.00 to -124.50, // Olympic Peninsula
                49.00 to -123.00  // Closed
            ),
            description = "الولايات المتحدة الأمريكية 🇺🇸 - النسر الأصلع رمز الحرية والقوة والشجاعة المستقلة منذ 1782!"
        ),

        // 10. India 🇮🇳 - Bengal Tiger 🐅
        CountryBorderData(
            id = "in",
            countryName = "الهند",
            flagEmoji = "🇮🇳",
            animalName = "النمر البنغالي الملكي",
            animalEmoji = "🐅",
            centerLat = 20.5937,
            centerLng = 78.9629,
            zoomLevel = 4.8,
            strokeColorInt = android.graphics.Color.parseColor("#D97706"), // Saffron Gold
            fillColorInt = android.graphics.Color.parseColor("#33D97706"),
            boundaryPoints = points(
                35.00 to 76.00,  // Kashmir North
                28.00 to 88.00,  // Sikkim / Nepal
                27.00 to 96.00,  // Arunachal Pradesh
                22.00 to 89.00,  // Sundarbans Bengal
                17.50 to 83.00,  // Visakhapatnam Bay of Bengal
                13.00 to 80.20,  // Chennai
                8.10 to 77.50,   // Kanyakumari South
                15.40 to 73.80,  // Goa
                19.00 to 72.80,  // Mumbai Arabian Sea
                23.50 to 68.50,  // Gujarat West
                31.00 to 74.50,  // Punjab
                35.00 to 76.00   // Closed Kashmir
            ),
            description = "جمهورية الهند 🇮🇳 - النمر البنغالي الملكي رمز الهيبة والسرعة والقوة في الغابات الهندية!"
        ),

        // 11. Russia 🇷🇺 - Brown Bear 🐻
        CountryBorderData(
            id = "ru",
            countryName = "روسيا",
            flagEmoji = "🇷🇺",
            animalName = "الدب البني الأوراسي",
            animalEmoji = "🐻",
            centerLat = 61.5240,
            centerLng = 105.3188,
            zoomLevel = 3.5,
            strokeColorInt = android.graphics.Color.parseColor("#7C2D12"), // Deep Brown / Maroon
            fillColorInt = android.graphics.Color.parseColor("#337C2D12"),
            boundaryPoints = points(
                69.50 to 31.00,  // Murmansk Arctic
                68.00 to 60.00,  // Ural North
                73.00 to 80.00,  // Taymyr
                70.00 to 140.00, // Yakutia Arctic
                65.00 to 180.00, // Chukotka Bering Strait
                53.00 to 158.00, // Kamchatka
                43.00 to 132.00, // Vladivostok
                50.00 to 87.00,  // Altai
                52.00 to 50.00,  // Samara Volga
                55.00 to 37.00,  // Moscow
                60.00 to 30.00,  // St. Petersburg
                69.50 to 31.00   // Closed
            ),
            description = "روسيا الاتحادية 🇷🇺 - الدب الروسي رمز الصلابة والقوة والقدرة على الصمود في التايغا وسيبيريا!"
        ),

        // 12. Canada 🇨🇦 - Beaver 🦫
        CountryBorderData(
            id = "ca",
            countryName = "كندا",
            flagEmoji = "🇨🇦",
            animalName = "القندس الكندي (Beaver)",
            animalEmoji = "🦫",
            centerLat = 56.1304,
            centerLng = -106.3468,
            zoomLevel = 3.8,
            strokeColorInt = android.graphics.Color.parseColor("#DC2626"), // Maple Red
            fillColorInt = android.graphics.Color.parseColor("#33DC2626"),
            boundaryPoints = points(
                70.00 to -141.00, // Yukon / Alaska border
                70.00 to -70.00,  // Baffin Island
                52.00 to -56.00,  // Newfoundland
                45.00 to -66.00,  // New Brunswick Atlantic
                45.00 to -74.00,  // Montreal
                43.00 to -80.00,  // Great Lakes / Ontario
                49.00 to -95.00,  // Manitoba South
                49.00 to -123.00, // Vancouver Pacific
                54.00 to -130.00, // Prince Rupert
                60.00 to -140.00, // Alaska Panhandle
                70.00 to -141.00  // Closed
            ),
            description = "كندا 🇨🇦 - القندس الكندي رمز العمل الدؤوب والهندسة الطبيعية وبناء السدود والمثابرة!"
        ),

        // 13. United Kingdom 🇬🇧 - British Lion 🦁
        CountryBorderData(
            id = "gb",
            countryName = "بريطانيا (المملكة المتحدة)",
            flagEmoji = "🇬🇧",
            animalName = "الأسد البريطاني واليونيكورن",
            animalEmoji = "🦁",
            centerLat = 55.3781,
            centerLng = -3.4360,
            zoomLevel = 6.0,
            strokeColorInt = android.graphics.Color.parseColor("#1E3A8A"), // British Navy Blue
            fillColorInt = android.graphics.Color.parseColor("#331E3A8A"),
            boundaryPoints = points(
                58.60 to -3.00,  // John o' Groats Scotland
                57.50 to -1.80,  // Aberdeen North Sea
                55.00 to -1.40,  // Newcastle
                52.95 to 1.30,   // Norfolk
                51.35 to 1.40,   // Dover English Channel
                50.10 to -5.70,  // Land's End Cornwall
                51.50 to -4.00,  // Bristol Channel
                53.40 to -4.30,  // Anglesey Wales
                54.60 to -3.50,  // Lake District
                56.00 to -5.00,  // Glasgow / Highlands
                58.60 to -3.00   // Closed
            ),
            description = "المملكة المتحدة 🇬🇧 - الأسد البريطاني رمز الشجاعة والمهابة الملكية على شعار النبالة والعرش!"
        ),

        // 14. Germany 🇩🇪 - Federal Eagle 🦅
        CountryBorderData(
            id = "de",
            countryName = "ألمانيا",
            flagEmoji = "🇩🇪",
            animalName = "النسر الفيدرالي (Bundesadler)",
            animalEmoji = "🦅",
            centerLat = 51.1657,
            centerLng = 10.4515,
            zoomLevel = 6.0,
            strokeColorInt = android.graphics.Color.parseColor("#18181B"), // Black/Gold
            fillColorInt = android.graphics.Color.parseColor("#33FBBF24"),  // Amber Golden Tint
            boundaryPoints = points(
                54.80 to 9.00,   // Denmark border / North Sea
                54.40 to 13.50,  // Baltic Sea Coast
                52.50 to 14.70,  // Polish border / Oder River
                50.80 to 15.00,  // Czech border
                48.60 to 13.50,  // Passau / Austria border
                47.50 to 10.00,  // Bavarian Alps
                47.60 to 7.60,   // Lake Constance / Swiss border
                49.00 to 8.20,   // Rhine / French border
                50.30 to 6.40,   // Belgium border
                53.30 to 7.20,   // Netherlands border
                54.80 to 9.00    // Closed
            ),
            description = "جمهورية ألمانيا الاتحادية 🇩🇪 - النسر الفيدرالي الشعار الوطني الأقدم للحكم والسيادة والاستقرار!"
        ),

        // 15. Italy 🇮🇹 - Italian Wolf 🐺
        CountryBorderData(
            id = "it",
            countryName = "إيطاليا",
            flagEmoji = "🇮🇹",
            animalName = "الذئب الإيطالي (Lupo Appenninico)",
            animalEmoji = "🐺",
            centerLat = 41.8719,
            centerLng = 12.5674,
            zoomLevel = 5.8,
            strokeColorInt = android.graphics.Color.parseColor("#047857"), // Italian Green
            fillColorInt = android.graphics.Color.parseColor("#33047857"),
            boundaryPoints = points(
                46.50 to 11.50,  // Alps / Austria border
                45.70 to 13.60,  // Trieste Adriatic
                44.00 to 12.60,  // Rimini
                41.90 to 16.00,  // Gargano Peninsula
                40.20 to 18.50,  // Puglia (Boot Heel)
                38.00 to 15.60,  // Calabria (Boot Toe)
                39.50 to 16.00,  // Tyrrhenian Coast
                41.80 to 12.30,  // Rome Coast
                44.40 to 8.90,   // Genoa Ligurian Sea
                45.80 to 7.00,   // Mont Blanc / France border
                46.50 to 11.50   // Closed Alps
            ),
            description = "الجمهورية الإيطالية 🇮🇹 - الذئب الإيطالي المرتبط بأسطورة تأسيس مدينة روما الخالدة والولاء والشجاعة!"
        ),

        // 16. United Arab Emirates 🇦🇪 - Arabian Oryx 🦌
        CountryBorderData(
            id = "ae",
            countryName = "الإمارات العربية المتحدة",
            flagEmoji = "🇦🇪",
            animalName = "المها العربي والصقر الحر",
            animalEmoji = "🦌",
            centerLat = 23.4241,
            centerLng = 53.8478,
            zoomLevel = 6.4,
            strokeColorInt = android.graphics.Color.parseColor("#047857"), // Green / Red
            fillColorInt = android.graphics.Color.parseColor("#33047857"),
            boundaryPoints = points(
                26.00 to 56.00,  // Ras Al Khaimah
                25.60 to 56.35,  // Fujairah / Gulf of Oman
                24.50 to 56.00,  // Hatta / Oman border
                23.00 to 55.50,  // Al Ain / Empty Quarter
                22.60 to 52.00,  // Saudi border South
                24.10 to 51.60,  // Sila West
                24.50 to 54.40,  // Abu Dhabi Coast
                25.20 to 55.30,  // Dubai Coast
                26.00 to 56.00   // Closed
            ),
            description = "دولة الإمارات العربية المتحدة 🇦🇪 - المها العربي رمز الجمال والأصالة والصقر رمز العزة والرؤية الثاقبة!"
        ),

        // 17. Brazil 🇧🇷 - Jaguar 🐆
        CountryBorderData(
            id = "br",
            countryName = "البرازيل",
            flagEmoji = "🇧🇷",
            animalName = "اليغور المرقط (Jaguar)",
            animalEmoji = "🐆",
            centerLat = -14.2350,
            centerLng = -51.9253,
            zoomLevel = 4.2,
            strokeColorInt = android.graphics.Color.parseColor("#15803D"), // Amazon Green
            fillColorInt = android.graphics.Color.parseColor("#3315803D"),
            boundaryPoints = points(
                4.00 to -51.00,   // Amapa North
                -0.50 to -48.00,  // Amazon River mouth
                -5.50 to -35.00,  // Natal / Atlantic tip
                -13.00 to -38.50, // Salvador Bahia
                -23.00 to -43.00, // Rio de Janeiro
                -25.50 to -48.50, // Parana Coast
                -33.50 to -53.00, // Uruguay border South
                -22.00 to -58.00, // Pantanal / Paraguay
                -10.00 to -69.00, // Acre / Peru border
                1.00 to -67.00,   // Colombia border
                4.00 to -60.00,   // Venezuela border
                4.00 to -51.00    // Closed
            ),
            description = "جمهورية البرازيل الاتحادية 🇧🇷 - اليغور المرقط ملك غابات الأمازون وأكبر مفترس قطي في الأمريكتين!"
        ),

        // 18. Japan 🇯🇵 - Green Pheasant & Macaque 🦚🐒
        CountryBorderData(
            id = "jp",
            countryName = "اليابان",
            flagEmoji = "🇯🇵",
            animalName = "طائر الدراج الأخضر وقرد المكاك",
            animalEmoji = "🦚",
            centerLat = 36.2048,
            centerLng = 138.2529,
            zoomLevel = 5.2,
            strokeColorInt = android.graphics.Color.parseColor("#BE123C"), // Japan Sun Crimson
            fillColorInt = android.graphics.Color.parseColor("#33BE123C"),
            boundaryPoints = points(
                45.50 to 142.00, // Hokkaido North
                43.00 to 145.50, // Nemuro East
                41.50 to 141.00, // Hakodate
                38.00 to 141.00, // Sendai Honshu
                35.68 to 139.75, // Tokyo Bay
                33.50 to 135.50, // Wakayama
                31.00 to 130.50, // Kagoshima Kyushu South
                33.50 to 130.00, // Fukuoka
                36.50 to 136.50, // Kanazawa Sea of Japan
                40.50 to 140.00, // Akita
                45.50 to 142.00  // Closed
            ),
            description = "اليابان 🇯🇵 - طائر الدراج الأخضر الزمردي وقرود الثلوج في الينابيع الساخنة في الجبال!"
        ),

        // 19. New Zealand 🇳🇿 - Kiwi Bird 🥝
        CountryBorderData(
            id = "nz",
            countryName = "نيوزيلندا",
            flagEmoji = "🇳🇿",
            animalName = "طائر الكيوي (Kiwi Bird)",
            animalEmoji = "🥝",
            centerLat = -40.9006,
            centerLng = 174.8860,
            zoomLevel = 5.4,
            strokeColorInt = android.graphics.Color.parseColor("#0369A1"), // Kiwi Ocean Blue
            fillColorInt = android.graphics.Color.parseColor("#330369A1"),
            boundaryPoints = points(
                -34.50 to 173.00, // North Cape
                -37.00 to 176.00, // Bay of Plenty
                -39.50 to 177.00, // Hawke's Bay
                -41.30 to 174.80, // Wellington / Cook Strait
                -43.50 to 172.70, // Christchurch South Island
                -46.60 to 168.30, // Invercargill / Foveaux Strait
                -44.50 to 167.80, // Milford Sound West
                -41.00 to 172.50, // Nelson
                -38.00 to 174.80, // Waikato
                -34.50 to 173.00  // Closed
            ),
            description = "نيوزيلندا 🇳🇿 - طائر الكيوي الفريد غير القادر على الطيران وأيقونة ولقب شعب نيوزيلندا!"
        ),

        // 20. South Africa 🇿🇦 - Springbok 🦌
        CountryBorderData(
            id = "za",
            countryName = "جنوب إفريقيا",
            flagEmoji = "🇿🇦",
            animalName = "غزال السبرينغبوك (القفاز)",
            animalEmoji = "🦌",
            centerLat = -30.5595,
            centerLng = 22.9375,
            zoomLevel = 5.2,
            strokeColorInt = android.graphics.Color.parseColor("#15803D"), // Springbok Green & Gold
            fillColorInt = android.graphics.Color.parseColor("#3315803D"),
            boundaryPoints = points(
                -22.15 to 29.80, // Limpopo / Zimbabwe border
                -24.00 to 31.80, // Kruger Park / Mozambique
                -27.00 to 32.80, // KwaZulu-Natal coast
                -29.85 to 31.00, // Durban Indian Ocean
                -34.00 to 25.60, // Port Elizabeth
                -34.35 to 18.50, // Cape of Good Hope / Cape Town
                -30.00 to 17.10, // Atlantic Coast North
                -28.60 to 16.50, // Orange River / Namibia border
                -25.00 to 20.00, // Kalahari / Botswana border
                -22.15 to 29.80  // Closed Limpopo
            ),
            description = "جمهورية جنوب إفريقيا 🇿🇦 - غزال السبرينغبوك رمز الرشاقة والسرعة الفائقة والقفز العالي وشعار الرغبي الوطني!"
        )
    )

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
