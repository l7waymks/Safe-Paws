package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.*
import com.example.ui.components.*
import com.example.ui.theme.PrimaryTeal
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

data class CountryBorder(
    val name: String,
    val iso: String,
    val lines: List<List<GeoPoint>>
)

data class PetPlace(
    val id: String,
    val name: String,
    val category: String,
    val type: String,
    val lat: Double,
    val lng: Double,
    val desc: String,
    val rating: String,
    val reviews: String,
    val phone: String,
    val hours: String,
    val imageUrl: String
)

// Background non-blocking fetcher for live OpenStreetMap Places via Overpass API
fun fetchNearbyPlacesFromOverpass(
    lat: Double,
    lng: Double,
    onSuccess: (List<PetPlace>) -> Unit
) {
    // Return empty list immediately to honor user request and avoid any unsolicited map points
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
        onSuccess(emptyList())
    }
}

// Retrieve approximate user location via IP API as a fast initial fallback while GPS locks
fun fetchIpLocation(onResult: (Double, Double) -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        // 1. Try Free IPAPI.co first
        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipapi.co/json/")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lng = json.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(lat, lng)
                    }
                    return@launch
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipapi.co failed: ${e.message}")
        }

        // 2. Try Free ipinfo.io as fallback
        try {
            val request = okhttp3.Request.Builder()
                .url("https://ipinfo.io/json")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val loc = json.optString("loc", "")
                if (loc.isNotBlank() && loc.contains(",")) {
                    val parts = loc.split(",")
                    val lat = parts[0].toDoubleOrNull()
                    val lng = parts[1].toDoubleOrNull()
                    if (lat != null && lng != null) {
                        withContext(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(lat, lng)
                        }
                        return@launch
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipinfo.io failed: ${e.message}")
        }

        // 3. Try freeipapi.com as additional ultra-fast fallback
        try {
            val request = okhttp3.Request.Builder()
                .url("https://freeipapi.com/api/json")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lng = json.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(lat, lng)
                    }
                    return@launch
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "freeipapi.com failed: ${e.message}")
        }
    }
}

// Clean helper to parse a Firestore Feature JSON object into a PetPlace
fun parseFirestoreFeature(
    featObj: org.json.JSONObject,
    docIndex: Int,
    featIndex: Int
): PetPlace? {
    val geometryObj = featObj.optJSONObject("geometry")?.optJSONObject("mapValue")?.optJSONObject("fields") ?: return null
    val propertiesObj = featObj.optJSONObject("properties")
    
    val coordArray = geometryObj.optJSONObject("coordinates")?.optJSONObject("arrayValue")?.optJSONArray("values") ?: return null
    if (coordArray.length() < 2) return null
    
    val lngValObj = coordArray.optJSONObject(0)
    val latValObj = coordArray.optJSONObject(1)
    
    val lngVal = if (lngValObj != null && lngValObj.has("doubleValue")) {
        lngValObj.optDouble("doubleValue")
    } else if (lngValObj != null && lngValObj.has("integerValue")) {
        lngValObj.optString("integerValue").toDoubleOrNull()
    } else if (lngValObj != null && lngValObj.has("stringValue")) {
        lngValObj.optString("stringValue").toDoubleOrNull()
    } else {
        null
    }

    val latVal = if (latValObj != null && latValObj.has("doubleValue")) {
        latValObj.optDouble("doubleValue")
    } else if (latValObj != null && latValObj.has("integerValue")) {
        latValObj.optString("integerValue").toDoubleOrNull()
    } else if (latValObj != null && latValObj.has("stringValue")) {
        latValObj.optString("stringValue").toDoubleOrNull()
    } else {
        null
    }

    if (lngVal == null || latVal == null || lngVal.isNaN() || latVal.isNaN()) return null
    val lng = lngVal
    val lat = latVal
    
    val propFields = propertiesObj?.optJSONObject("mapValue")?.optJSONObject("fields")
    
    val name = when {
        propFields?.has("الاسم") == true -> propFields.optJSONObject("الاسم")?.optString("stringValue") ?: ""
        propFields?.has("الاسم (عربي)") == true -> propFields.optJSONObject("الاسم (عربي)")?.optString("stringValue") ?: ""
        propFields?.has("name:ar") == true -> propFields.optJSONObject("name:ar")?.optString("stringValue") ?: ""
        propFields?.has("name") == true -> propFields.optJSONObject("name")?.optString("stringValue") ?: ""
        propFields?.has("Name (EN)") == true -> propFields.optJSONObject("Name (EN)")?.optString("stringValue") ?: ""
        propFields?.has("title") == true -> propFields.optJSONObject("title")?.optString("stringValue") ?: ""
        else -> "معلم مضاف"
    }.ifBlank { "معلم مضاف" }
    
    val amenity = propFields?.optJSONObject("amenity")?.optString("stringValue") ?: ""
    val shop = propFields?.optJSONObject("shop")?.optString("stringValue") ?: ""
    val catVal = propFields?.optJSONObject("category")?.optString("stringValue") ?: ""
    
    val lowercaseName = name.lowercase()
    val lowercaseAmenity = amenity.lowercase()
    val lowercaseShop = shop.lowercase()
    val lowercaseCategory = catVal.lowercase()
    
    val street = propFields?.optJSONObject("addr:street")?.optString("stringValue") ?: ""
    val city = propFields?.optJSONObject("addr:city")?.optString("stringValue") ?: ""
    val phone = propFields?.optJSONObject("phone")?.optString("stringValue") ?: ""
    val website = propFields?.optJSONObject("website")?.optString("stringValue") ?: ""
    
    val descBuilder = java.lang.StringBuilder()
    if (city.isNotBlank()) descBuilder.append("المدينة: $city | ")
    if (street.isNotBlank()) descBuilder.append("الشارع: $street | ")
    if (phone.isNotBlank()) descBuilder.append("الهاتف: $phone | ")
    if (website.isNotBlank()) descBuilder.append("الموقع: $website")
    
    val desc = descBuilder.toString().removeSuffix(" | ").trim().ifBlank { 
        "موقع تفاعلي مضاف لخدمة ورعاية الحيوانات الأليفة."
    }
    
    val mappedCategoryAndType = when {
        lowercaseCategory.contains("vet") || lowercaseCategory.contains("clinic") || lowercaseAmenity.contains("vet") || lowercaseName.contains("طبيب") || lowercaseName.contains("عيادة") || desc.contains("طبيب") || desc.contains("عيادة") -> {
            Pair("عيادات بيطرية", "clinic")
        }
        lowercaseCategory.contains("store") || lowercaseCategory.contains("shop") || lowercaseShop.isNotBlank() || lowercaseName.contains("متجر") || lowercaseName.contains("محل") || desc.contains("متجر") || desc.contains("محل") -> {
            Pair("متاجر ومستلزمات", "shop")
        }
        lowercaseCategory.contains("shelter") || lowercaseCategory.contains("adopt") || lowercaseName.contains("ملجأ") || lowercaseName.contains("إيواء") || desc.contains("ملجأ") || desc.contains("إيواء") -> {
            Pair("الملاجئ والتبني", "shelter")
        }
        lowercaseCategory.contains("park") || lowercaseCategory.contains("training") || lowercaseName.contains("حديقة") || lowercaseName.contains("منتزه") || desc.contains("حديقة") || desc.contains("منتزه") -> {
            Pair("مراكز تدريب وحدائق", "park")
        }
        else -> {
            Pair("النقاط المستوردة", "clinic")
        }
    }
    
    val stableId = propFields?.optJSONObject("id")?.optString("stringValue") ?: propFields?.optJSONObject("uid")?.optString("stringValue") ?: "live_firestore_${name.hashCode()}_${lat.toString().hashCode()}_${lng.toString().hashCode()}"
    
    return PetPlace(
        id = stableId,
        name = name,
        category = mappedCategoryAndType.first,
        type = mappedCategoryAndType.second,
        lat = lat,
        lng = lng,
        desc = desc,
        rating = "5.0",
        reviews = "موقع",
        phone = phone.ifBlank { "غير متوفر" },
        hours = "٢٤ ساعة",
        imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
    )
}

// Clean parser tool to fetch and convert geojson points from specified url path
fun getFallbackWebPlaces(): List<PetPlace> {
    return emptyList()
}

fun getFallbackWebPlaces_unused(): List<PetPlace> {
    return listOf(
        PetPlace(
            id = "fallback_web_1",
            name = "عيادة الأليف المتقدمة البيطرية",
            category = "النقاط المستوردة",
            type = "clinic",
            lat = 24.7082,
            lng = 46.6815,
            desc = "عيادة بيطرية متكاملة لتقديم الاستشارات والعلاجات والتطعيمات للحيوانات الأليفة بالرياض.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 11 462 2345",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_2",
            name = "متجر أليفي لملحقات الحيوانات",
            category = "النقاط المستوردة",
            type = "shop",
            lat = 24.7725,
            lng = 46.6391,
            desc = "متجر رائد يوفر الأغذية الفاخرة، الألعاب، والاكسسوارات المخصصة للكلاب والقطط.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 55 123 4567",
            hours = "١٠ صباحاً - ١١ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400"
        ),
        PetPlace(
            id = "fallback_web_3",
            name = "ملجأ الرياض لتبني القطط والكلاب",
            category = "النقاط المستوردة",
            type = "shelter",
            lat = 24.8211,
            lng = 46.7032,
            desc = "ملجأ رعاية إنسانية للحيوانات الأليفة المشردة، يوفر الرعاية الطبية ويبحث عن منازل حنونة للتبني.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 50 222 3333",
            hours = "٩ صباحاً - ٥ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_4",
            name = "صالون وسبا المخالب اللطيفة",
            category = "النقاط المستوردة",
            type = "hotel",
            lat = 24.7431,
            lng = 46.6548,
            desc = "قص، استحمام، العناية بالأظافر والوبر بأيدي متخصصين لتدليل قطتكم وكلبكم اللطيف.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 54 888 9999",
            hours = "١٠ صباحاً - ١٠ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400"
        ),
        PetPlace(
            id = "fallback_web_5",
            name = "فندق أليف للرعاية الفندقية",
            category = "النقاط المستوردة",
            type = "hotel",
            lat = 24.7895,
            lng = 46.6124,
            desc = "فندق وبيت ضيافة آمن للحيوانات الأليفة أثناء سفركم، رعاية تامة ٢٤ ساعة مع كاميرات لمراقبة أليفكم.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 53 111 7777",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_6",
            name = "مستشفى الرياض البيطري الدولي",
            category = "النقاط المستوردة",
            type = "clinic",
            lat = 24.6948,
            lng = 46.7215,
            desc = "رعاية طبية بيطرية وجراحة على مدار الساعة، أشعة، تحاليل مخبرية، وعناية تخصصية متكاملة.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 11 475 8888",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_7",
            name = "حديقة الأليف السعيدة للتدريب",
            category = "النقاط المستوردة",
            type = "park",
            lat = 24.8055,
            lng = 46.6712,
            desc = "منطقة مخصصة للعب الحر لحيواناتكم الأليفة وتدريب الكلاب على الطاعة وتعديل السلوك.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 56 333 4444",
            hours = "٤ مساءً - ١١ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_8",
            name = "نقطة إطعام القطط المشردة - العليا",
            category = "النقاط المستوردة",
            type = "shelter",
            lat = 24.7152,
            lng = 46.6741,
            desc = "مبادرة مجتمعية تطوعية مخصصة لتزويد القطط الضالة بالطعام والماء النظيف يومياً.",
            rating = "5.0",
            reviews = "موقع",
            phone = "عمل تطوعي",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400"
        ),
        PetPlace(
            id = "fallback_web_9",
            name = "متجر واحة الحيوان الشامل",
            category = "النقاط المستوردة",
            type = "shop",
            lat = 24.7312,
            lng = 46.6985,
            desc = "أكبر الفروع لـ لوازم الحيوانات بالرياض، يتضمن قسماً خاصاً للحيوانات الصغيرة والطيور.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 11 222 5555",
            hours = "٩ صباحاً - ١٢ ليلاً",
            imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400"
        ),
        PetPlace(
            id = "fallback_web_10",
            name = "مركز بسمة أليف للعناية الطبية",
            category = "النقاط المستوردة",
            type = "clinic",
            lat = 24.7568,
            lng = 46.6212,
            desc = "فحوصات طبية دورية وصيدلية متكاملة لتقديم الرعاية الوقائية وحماية أليفك.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 59 777 8888",
            hours = "٩ صباحاً - ١٠ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_11",
            name = "نادي ومسبح الكلاب الرياضي",
            category = "النقاط المستوردة",
            type = "park",
            lat = 24.8421,
            lng = 46.6455,
            desc = "مسبح وغرف ترفيهية متكاملة للياقة والتدريب وتخفيف التوتر للكلاب الكبيرة والصغيرة.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 55 444 8888",
            hours = "١٠ صباحاً - ٨ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_12",
            name = "جمعية رفق للرفق بالحيوان",
            category = "النقاط المستوردة",
            type = "shelter",
            lat = 24.6312,
            lng = 46.7118,
            desc = "جمعية خيرية لنشر الوعي بحقوق الحيوان، إنقاذ الحالات المصابة، وتنظيم حملات التعقيم.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 50 999 1111",
            hours = "٨ صباحاً - ٤ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_1",
            name = "مركز شفاء للطب البيطري - الدار البيضاء",
            category = "النقاط المستوردة",
            type = "clinic",
            lat = 33.5731,
            lng = -7.5898,
            desc = "رعاية طبية وعلاج متقدم وجراحة للحيوانات الأليفة في حي المعاريف بالدار البيضاء.",
            rating = "4.9",
            reviews = "موقع",
            phone = "+212 522 25 40 40",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_2",
            name = "ملجأ الأمل لإنقاذ الحيوانات - الرباط",
            category = "النقاط المستوردة",
            type = "shelter",
            lat = 34.0209,
            lng = -6.8416,
            desc = "جمعية خيرية عريقة لإيواء وتربية القطط والكلاب الضالة والبحث عن متبنين حنونين بالرباط ونواحيها.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+212 661 15 22 33",
            hours = "٩ صباحاً - ٦ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_3",
            name = "محل أليفي المدلل - طنجة",
            category = "النقاط المستوردة",
            type = "shop",
            lat = 35.7595,
            lng = -5.8340,
            desc = "أرقى الأغذية والمستلزمات المستوردة لجميع أنواع الحيوانات الأليفة والطيور والأسماك في مدينة طنجة.",
            rating = "4.8",
            reviews = "موقع",
            phone = "+212 539 93 45 45",
            hours = "١٠ صباحاً - ١٠ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_4",
            name = "العيادة البيطرية الكبرى لحي النخيل - مراكش",
            category = "النقاط المستوردة",
            type = "clinic",
            lat = 31.6295,
            lng = -7.9811,
            desc = "رعاية صحية عاجلة، تصوير بالموجات فوق الصوتية، وتحاليل مخبرية، مع قسم خاص للعناية والوقاية بأحياء مراكش.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+212 524 44 88 99",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_5",
            name = "ملجأ سوس لإنقاذ ورعاية القطط - أيت ملول",
            category = "النقاط المستوردة",
            type = "shelter",
            lat = 30.4179,
            lng = -9.5776,
            desc = "مبادرة خيرية من محبي القطط لتوفير بيئة إنقاذ دافئة وتطعيم وتثقيف المجتمع لتبني الهررة الضالة بأكادير ونواحيها.",
            rating = "4.9",
            reviews = "موقع",
            phone = "+212 670 44 55 66",
            hours = "١٠ صباحاً - ٥ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400"
        ),
        PetPlace(
            id = "fallback_web_morocco_6",
            name = "صالون تدليل الأنيق لخدمات التزيين - الدار البيضاء",
            category = "النقاط المستوردة",
            type = "hotel",
            lat = 33.5852,
            lng = -7.6324,
            desc = "استحمام، قص شعر، تنظيف وتقليم أظافر للقطط والكلاب مع تأمين أعلى مستويات الراحة لحيواناتكم بالبيضاء.",
            rating = "4.7",
            reviews = "موقع",
            phone = "+212 522 36 36 36",
            hours = "٩:٣٠ صباحاً - ٨:٣٠ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?w=400"
        )
    )
}

fun fetchGeoJsonFromUrl(context: android.content.Context, url: String, onResult: (List<PetPlace>, Boolean) -> Unit) {
    val supabaseUrl = "https://rxclrwcwhbvnldmguxko.supabase.co"
    val supabaseKey = "sb_publishable_7ECYLHs8ZeuE40g2El1KiQ_Ghhg79Uf"
    
    // Listen directly to the active visible website layers (not the raw static database markers!)
    // If a layer/point is turned off on the website, it is immediately excluded.
    fetchLayersFromSupabase(supabaseUrl, supabaseKey) { activePlaces, error ->
        if (error == null) {
            onResult(activePlaces, false)
        } else {
            android.util.Log.e("MapScreen", "Error listening to website layers: $error")
            onResult(emptyList(), false)
        }
    }
}

fun bulkInsertIntoSupabase(
    url: String,
    anonKey: String,
    tableName: String,
    places: List<PetPlace>,
    onResult: (Boolean, String?) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val cleanUrl = url.trim().removeSuffix("/")
        val endpoint = "$cleanUrl/rest/v1/$tableName"
        
        val jsonArray = org.json.JSONArray()
        places.forEach { place ->
            val obj = org.json.JSONObject()
            obj.put("title", place.name)
            obj.put("description", place.desc)
            obj.put("lat", place.lat)
            obj.put("lng", place.lng)
            obj.put("category", place.category)
            obj.put("phone", place.phone)
            obj.put("hours", place.hours)
            obj.put("rating", place.rating)
            obj.put("reviews", place.reviews)
            obj.put("imageUrl", place.imageUrl)
            jsonArray.put(obj)
        }
        
        try {
            val body = okhttp3.RequestBody.create(null, jsonArray.toString())
            
            val request = okhttp3.Request.Builder()
                .url(endpoint)
                .post(body)
                .header("apikey", anonKey.trim())
                .header("Authorization", "Bearer ${anonKey.trim()}")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .build()
                
            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 201) {
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(true, null)
                }
            } else {
                val err = response.body?.string() ?: ""
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(false, "Error ${response.code}: $err")
                }
            }
        } catch (e: Exception) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(false, e.localizedMessage)
            }
        }
    }
}

fun deleteFromSupabase(
    url: String,
    anonKey: String,
    tableName: String,
    onResult: (Boolean, String?) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val cleanUrl = url.trim().removeSuffix("/")
        val endpoint = "$cleanUrl/rest/v1/$tableName?id=gte.0"
        
        try {
            val request = okhttp3.Request.Builder()
                .url(endpoint)
                .delete()
                .header("apikey", anonKey.trim())
                .header("Authorization", "Bearer ${anonKey.trim()}")
                .build()
                
            val response = client.newCall(request).execute()
            if (response.isSuccessful || response.code == 204) {
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(true, null)
                }
            } else {
                val err = response.body?.string() ?: ""
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(false, "Error ${response.code}: $err")
                }
            }
        } catch (e: Exception) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(false, e.localizedMessage)
            }
        }
    }
}

fun fetchFromSupabase(
    url: String,
    anonKey: String,
    tableName: String,
    onResult: (List<PetPlace>, String?) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val cleanUrl = url.trim().removeSuffix("/")
        val endpoint = "$cleanUrl/rest/v1/$tableName?select=*"
        
        try {
            val request = okhttp3.Request.Builder()
                .url(endpoint)
                .get()
                .header("apikey", anonKey.trim())
                .header("Authorization", "Bearer ${anonKey.trim()}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
                
            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                val errMsg = "Supabase API returned error code ${response.code}: $responseStr"
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(emptyList(), errMsg)
                }
                return@launch
            }
            
            val jsonArray = org.json.JSONArray(responseStr)
            val list = mutableListOf<PetPlace>()
            
            // Helper for relaxed double parsing
            fun optDoubleRelaxed(obj: org.json.JSONObject, vararg keys: String): Double {
                for (key in keys) {
                    if (obj.has(key) && !obj.isNull(key)) {
                        val value = obj.opt(key)
                        if (value is Number) {
                            return value.toDouble()
                        } else if (value is String) {
                            val d = value.toDoubleOrNull()
                            if (d != null) return d
                        }
                    }
                }
                return Double.NaN
            }

            // Helper to parse PostGIS/GeoJSON Point geometry column
            fun parseGeometryPoint(obj: org.json.JSONObject, vararg keys: String): Pair<Double, Double>? {
                for (key in keys) {
                    if (obj.has(key) && !obj.isNull(key)) {
                        val geom = obj.optJSONObject(key)
                        if (geom != null) {
                            val type = geom.optString("type", "")
                            if (type.equals("Point", ignoreCase = true)) {
                                val coords = geom.optJSONArray("coordinates")
                                if (coords != null && coords.length() >= 2) {
                                    val lng = coords.optDouble(0, 0.0)
                                    val lat = coords.optDouble(1, 0.0)
                                    return Pair(lat, lng)
                                }
                            }
                        }
                    }
                }
                return null
            }
            
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                
                val id = if (obj.has("id")) obj.optString("id") else (if (obj.has("uuid")) obj.optString("uuid") else i.toString())
                
                // Flexible keys for name
                val name = when {
                    obj.has("name") -> obj.optString("name")
                    obj.has("title") -> obj.optString("title")
                    obj.has("label") -> obj.optString("label")
                    else -> "معلم غير مسمى"
                }
                
                // Flexible keys for category
                val category = when {
                    obj.has("category") -> obj.optString("category")
                    obj.has("type") -> obj.optString("type")
                    obj.has("tag") -> obj.optString("tag")
                    else -> "النقاط المستوردة"
                }
                
                // Parse coordinates with relaxed keys and geom geometry fallback
                var lat = optDoubleRelaxed(obj, "lat", "latitude", "lat_val", "y")
                var lng = optDoubleRelaxed(obj, "lng", "longitude", "lon", "long", "lng_val", "x")
                
                if (lat.isNaN() || lng.isNaN() || (lat == 0.0 && lng == 0.0)) {
                    val geomPair = parseGeometryPoint(obj, "geom", "geometry", "location")
                    if (geomPair != null) {
                        lat = geomPair.first
                        lng = geomPair.second
                    }
                }
                
                val desc = if (obj.has("desc")) obj.optString("desc") else (if (obj.has("description")) obj.optString("description") else "")
                val rating = if (obj.has("rating")) obj.optString("rating") else "4.5"
                val reviews = if (obj.has("reviews")) obj.optString("reviews") else "12"
                val phone = if (obj.has("phone")) obj.optString("phone") else ""
                val hours = if (obj.has("hours")) obj.optString("hours") else (if (obj.has("working_hours")) obj.optString("working_hours") else "")
                val imageUrl = if (obj.has("image_url")) obj.optString("image_url") else (if (obj.has("image")) obj.optString("image") else "")
                
                list.add(
                    PetPlace(
                        id = id ?: i.toString(),
                        name = name ?: "معلم غير مسمى",
                        category = category ?: "النقاط المستوردة",
                        type = category ?: "النقاط المستوردة",
                        lat = if (lat.isNaN()) 30.0 else lat, // fallbacks to safe center coordinate if parsing failed completely
                        lng = if (lng.isNaN()) 31.0 else lng,
                        desc = desc ?: "",
                        rating = rating ?: "4.5",
                        reviews = reviews ?: "12",
                        phone = phone ?: "",
                        hours = hours ?: "",
                        imageUrl = imageUrl ?: ""
                    )
                )
            }
            
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(list, null)
            }
        } catch (e: Exception) {
            val errMsg = "Error connecting to Supabase: ${e.message}"
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(emptyList(), errMsg)
            }
        }
    }
}

fun parseSupabaseLayersToPlaces(jsonStr: String): List<PetPlace> {
    val list = mutableListOf<PetPlace>()
    try {
        val array = org.json.JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val layerObj = array.optJSONObject(i) ?: continue
            
            // Listen to the website visibility state:
            // When a user turns off (يطفئ) a layer/point on the website, visible is false!
            val isVisible = if (layerObj.has("visible")) layerObj.optBoolean("visible", true) else true
            if (!isVisible) {
                // Layer is disabled/turned off on the website, so do NOT show any of its points!
                continue
            }
            
            val layerName = layerObj.optString("name", "معلم مستورد")
            val features = layerObj.optJSONArray("features") ?: continue
            for (j in 0 until features.length()) {
                val featArr = features.optJSONArray(j)
                val featObj = features.optJSONObject(j)
                
                if (featArr != null) {
                    if (featArr.length() >= 3) {
                        val coords = featArr.optJSONArray(1) ?: continue
                        val name = featArr.optString(2, layerName).ifBlank { layerName }
                        val props = if (featArr.length() >= 4) featArr.optJSONObject(3) else org.json.JSONObject()
                        
                        if (coords.length() >= 2) {
                            val lng = coords.optDouble(0)
                            val lat = coords.optDouble(1)
                            if (lat.isNaN() || lng.isNaN()) continue
                            addParsedFeature(list, name, lat, lng, props)
                        }
                    }
                } else if (featObj != null) {
                    val geom = featObj.optJSONObject("geometry") ?: continue
                    val geomType = geom.optString("type", "")
                    val props = featObj.optJSONObject("properties") ?: org.json.JSONObject()
                    val name = props.optString("name", props.optString("NAME", props.optString("nom", layerName))).ifBlank { layerName }
                    
                    if (geomType.equals("Point", ignoreCase = true)) {
                        val coords = geom.optJSONArray("coordinates") ?: continue
                        if (coords.length() >= 2) {
                            val lng = coords.optDouble(0)
                            val lat = coords.optDouble(1)
                            if (lat.isNaN() || lng.isNaN()) continue
                            addParsedFeature(list, name, lat, lng, props)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("MapScreen", "Error parsing layers to places: ${e.message}")
    }
    return list
}

private fun addParsedFeature(list: MutableList<PetPlace>, name: String, lat: Double, lng: Double, props: org.json.JSONObject) {
    val amenity = props.optString("amenity", "").lowercase()
    val shop = props.optString("shop", "").lowercase()
    val tourism = props.optString("tourism", "").lowercase()
    val leisure = props.optString("leisure", "").lowercase()
    
    val lowerName = name.lowercase()
    val desc = when {
        props.has("الوصف") -> props.optString("الوصف")
        props.has("description") -> props.optString("description")
        props.has("desc") -> props.optString("desc")
        props.has("note") -> props.optString("note")
        else -> "نقطة مستوردة من الخريطة الخارجية التفاعلية."
    }.ifBlank { "نقطة مستوردة من الخريطة الخارجية التفاعلية." }
    
    val isVet = amenity.contains("vet") || lowerName.contains("vet") || lowerName.contains("عيادة") || lowerName.contains("بيطر") || desc.contains("عيادة") || desc.contains("بيطر")
    val isShop = shop.contains("pet") || shop.isNotBlank() || lowerName.contains("متجر") || lowerName.contains("محل") || desc.contains("متجر") || desc.contains("محل")
    val isShelter = amenity.contains("shelter") || lowerName.contains("ملجأ") || lowerName.contains("إيواء") || desc.contains("ملجأ") || desc.contains("إيواء") || props.has("animal_shelter")
    val isPark = leisure.contains("park") || leisure.contains("garden") || tourism.contains("zoo") || lowerName.contains("حديقة") || lowerName.contains("منتزه") || desc.contains("حديقة") || desc.contains("منتزه") || tourism.contains("zoo")
    
    val (categoryMapped, typeMapped) = when {
        isVet -> Pair("عيادات بيطرية", "clinic")
        isShop -> Pair("متاجر ومستلزمات", "shop")
        isShelter -> Pair("الملاجئ والتبني", "shelter")
        isPark -> Pair("مراكز تدريب وحدائق", "park")
        else -> Pair("النقاط المستوردة", "clinic")
    }
    
    val id = if (props.has("id")) {
        props.optString("id")
    } else if (props.has("@id")) {
        props.optString("@id")
    } else {
        "layer_feat_${name.hashCode()}_${lat.hashCode()}_${lng.hashCode()}"
    }
    
    list.add(
        PetPlace(
            id = id,
            name = name,
            category = categoryMapped,
            type = typeMapped,
            lat = lat,
            lng = lng,
            desc = desc,
            rating = "5.0",
            reviews = "موقع",
            phone = if (props.has("phone")) props.optString("phone") else (if (props.has("contact:phone")) props.optString("contact:phone") else "-"),
            hours = if (props.has("opening_hours")) props.optString("opening_hours") else "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
        )
    )
}

fun fetchLayersFromSupabase(
    url: String,
    anonKey: String,
    onResult: (List<PetPlace>, String?) -> Unit
) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val cleanUrl = url.trim().removeSuffix("/")
        val endpoint = "$cleanUrl/rest/v1/layers?select=*"
        
        try {
            val request = okhttp3.Request.Builder()
                .url(endpoint)
                .get()
                .header("apikey", anonKey.trim())
                .header("Authorization", "Bearer ${anonKey.trim()}")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .build()
                
            val response = client.newCall(request).execute()
            val responseStr = response.body?.string() ?: ""
            
            if (!response.isSuccessful) {
                val errMsg = "Supabase layers API returned error code ${response.code}: $responseStr"
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(emptyList(), errMsg)
                }
                return@launch
            }
            
            val parsedPlaces = parseSupabaseLayersToPlaces(responseStr)
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(parsedPlaces, null)
            }
        } catch (e: Exception) {
            val errMsg = "Error loading layers: ${e.message}"
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(emptyList(), errMsg)
            }
        }
    }
}

fun fetchEverythingFromSupabase(
    url: String,
    anonKey: String,
    tableName: String,
    onResult: (List<PetPlace>, String?) -> Unit
) {
    // Exclusively fetch active visible website layers; do not listen to static database markers
    fetchLayersFromSupabase(url, anonKey) { activeLayers, error ->
        onResult(activeLayers, error)
    }
}

fun fetchGeoJsonFromUrlScraper(url: String, onResult: (List<PetPlace>, Boolean) -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        val client = okhttp3.OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        val list = java.util.Collections.synchronizedList(mutableListOf<PetPlace>())
        
        val siteHost = "https://mapsafe-paws.netlify.app"
        val mainUrl = "https://mapsafe-paws.netlify.app/viewer.html"
        
        // 1. Fetch main page HTML
        var mainHtml = ""
        try {
            val request = okhttp3.Request.Builder()
                .url(mainUrl)
                .get()
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                mainHtml = response.body?.string() ?: ""
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Failed to fetch main page HTML: ${e.message}")
        }
        
        // 2. Discover files ending with .json or .geojson inside HTML
        val urlsToFetch = mutableSetOf<String>()
        // Always include these standard fallbacks
        urlsToFetch.add(mainUrl)
        urlsToFetch.add("$siteHost/map.geojson")
        urlsToFetch.add("$siteHost/data.geojson")
        urlsToFetch.add("$siteHost/data.json")
        urlsToFetch.add("$siteHost/live.geojson")
        urlsToFetch.add("$siteHost/places.geojson")
        urlsToFetch.add("$siteHost/places.json")
        urlsToFetch.add("$siteHost/points.geojson")
        urlsToFetch.add("$siteHost/points.json")
        urlsToFetch.add("$siteHost/map.json")
        
        if (mainHtml.isNotBlank()) {
            val pathRegex = """['"]([^'"]+\.geo?json|[^'"]+\.json)['"]""".toRegex()
            pathRegex.findAll(mainHtml).forEach { match ->
                val path = match.groupValues[1]
                if (!path.contains("package.json") && !path.contains("manifest.json")) {
                    val resolved = if (path.startsWith("http://") || path.startsWith("https://")) {
                        path
                    } else {
                        val cleanPath = if (path.startsWith("/")) path else "/$path"
                        "$siteHost$cleanPath"
                    }
                    urlsToFetch.add(resolved)
                }
            }
        }
        
        // 3. Fetch all URLs in parallel
        val jobs = urlsToFetch.map { targetUrl ->
            launch {
                try {
                    val request = okhttp3.Request.Builder()
                        .url(targetUrl)
                        .get()
                        .header("User-Agent", "Mozilla/5.0")
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string() ?: ""
                        if (bodyStr.isNotBlank()) {
                            val parsed = parseGeoJsonStringToPlaces(bodyStr)
                            if (parsed.isNotEmpty()) {
                                list.addAll(parsed)
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MapScreen", "Failed to fetch map data from $targetUrl: ${e.message}")
                }
            }
        }
        jobs.forEach { it.join() }
        
        var consolidatedList = list.distinctBy { it.id }
        if (consolidatedList.isEmpty()) {
            consolidatedList = getFallbackWebPlaces()
        }
        
        // Return results to Callback on Main Thread
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            onResult(consolidatedList, false)
        }
    }
}

fun extractGeoJsonFromHtml(html: String): String? {
    var index = 0
    while (true) {
        val fIndex = html.indexOf("\"FeatureCollection\"", index)
        if (fIndex == -1) break
        
        var startBrace = -1
        for (i in fIndex downTo 0) {
            if (html[i] == '{') {
                startBrace = i
                break
            }
        }
        
        if (startBrace != -1) {
            var braceCount = 0
            var inString = false
            var escape = false
            var endBrace = -1
            for (i in startBrace until html.length) {
                val c = html[i]
                if (escape) {
                    escape = false
                    continue
                }
                if (c == '\\') {
                    escape = true
                    continue
                }
                if (c == '"') {
                    inString = !inString
                    continue
                }
                if (!inString) {
                    if (c == '{') {
                        braceCount++
                    } else if (c == '}') {
                        braceCount--
                        if (braceCount == 0) {
                            endBrace = i
                            break
                        }
                    }
                }
            }
            if (endBrace != -1) {
                val candidateJson = html.substring(startBrace, endBrace + 1)
                try {
                    org.json.JSONObject(candidateJson)
                    return candidateJson
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        index = fIndex + 19
    }
    
    index = 0
    while (true) {
        val fIndex = html.indexOf("\"Feature\"", index)
        if (fIndex == -1) break
        
        var startBrace = -1
        for (i in fIndex downTo 0) {
            if (html[i] == '{') {
                startBrace = i
                break
            }
        }
        
        if (startBrace != -1) {
            var braceCount = 0
            var inString = false
            var escape = false
            var endBrace = -1
            for (i in startBrace until html.length) {
                val c = html[i]
                if (escape) {
                    escape = false
                    continue
                }
                if (c == '\\') {
                    escape = true
                    continue
                }
                if (c == '"') {
                    inString = !inString
                    continue
                }
                if (!inString) {
                    if (c == '{') {
                        braceCount++
                    } else if (c == '}') {
                        braceCount--
                        if (braceCount == 0) {
                            endBrace = i
                            break
                        }
                    }
                }
            }
            if (endBrace != -1) {
                val candidateJson = html.substring(startBrace, endBrace + 1)
                try {
                    org.json.JSONObject(candidateJson)
                    return candidateJson
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
        index = fIndex + 9
    }
    return null
}

fun parseGeoJsonStringToPlaces(jsonStr: String): List<PetPlace> {
    if (jsonStr.isBlank()) return emptyList()
    var targetJson = jsonStr.trim()
    if (targetJson.startsWith("<") || targetJson.contains("<!DOCTYPE") || targetJson.contains("<html")) {
        val extracted = extractGeoJsonFromHtml(targetJson)
        if (extracted != null) {
            targetJson = extracted
        } else {
            return emptyList()
        }
    }
    val list = mutableListOf<PetPlace>()
    try {
        val obj = org.json.JSONObject(targetJson)
        val features = if (obj.has("features")) {
            obj.getJSONArray("features")
        } else if (obj.has("type") && obj.getString("type") == "Feature") {
            org.json.JSONArray().put(obj)
        } else {
            return emptyList()
        }
        
        for (i in 0 until features.length()) {
            val f = features.getJSONObject(i)
            val geometry = f.optJSONObject("geometry") ?: continue
            val properties = f.optJSONObject("properties") ?: org.json.JSONObject()
            val coordinates = geometry.optJSONArray("coordinates") ?: continue
            
            val lng = coordinates.getDouble(0)
            val lat = coordinates.getDouble(1)
            
            val name = when {
                properties.has("الاسم (عربي)") -> properties.getString("الاسم (عربي)")
                properties.has("Name (EN)") -> properties.getString("Name (EN)")
                properties.has("name") -> properties.getString("name")
                properties.has("title") -> properties.getString("title")
                properties.has("الاسم") -> properties.getString("الاسم")
                properties.has("النهر") -> properties.getString("النهر")
                properties.has("الإقليم") -> properties.getString("الإقليم")
                else -> "موقع تفاعلي مضاف"
            }
            
            val desc = when {
                properties.has("الوصف") -> properties.getString("الوصف")
                properties.has("description") -> properties.getString("description")
                properties.has("desc") -> properties.getString("desc")
                else -> "نقطة مستوردة من الموقع التفاعلي الخارجي."
            }
            
            val category = properties.optString("category", "religion")
            val type = properties.optString("type", "shelter")
            
            val mappedCategoryAndType = when {
                category.contains("vet") || category.contains("clinic") || desc.contains("طبيب") || desc.contains("عيادة") -> {
                    Pair("عيادات بيطرية", "clinic")
                }
                category.contains("store") || category.contains("shop") || desc.contains("متجر") || desc.contains("محل") -> {
                    Pair("متاجر ومستلزمات", "shop")
                }
                category.contains("shelter") || category.contains("adopt") || desc.contains("ملجأ") || desc.contains("إيواء") -> {
                    Pair("الملاجئ والتبني", "shelter")
                }
                category.contains("park") || category.contains("training") || desc.contains("حديقة") || desc.contains("منتزه") -> {
                    Pair("مراكز تدريب وحدائق", "park")
                }
                else -> {
                    Pair("النقاط المستوردة", "clinic")
                }
            }
            
            val stableId = if (properties.has("id")) {
                properties.optString("id")
            } else if (properties.has("uid")) {
                properties.optString("uid")
            } else {
                "live_web_${name.hashCode()}_${lat.toString().hashCode()}_${lng.toString().hashCode()}"
            }
            
            list.add(
                PetPlace(
                    id = stableId,
                    name = name,
                    category = mappedCategoryAndType.first,
                    type = mappedCategoryAndType.second,
                    lat = lat,
                    lng = lng,
                    desc = desc,
                    rating = "5.0",
                    reviews = "موقع",
                    phone = properties.optString("phone", "-"),
                    hours = "٢٤ ساعة",
                    imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
                )
            )
        }
    } catch (e: Exception) {
        android.util.Log.e("MapScreen", "Error parsing live GeoJSON string: ${e.message}")
    }
    return list
}

// Keyless OSM-based Geocoding to translate user text queries into geo coordinates
fun geocodeAddress(query: String, onResult: (Double?, Double?) -> Unit) {
    if (query.isBlank()) {
        onResult(null, null)
        return
    }
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(6, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val request = okhttp3.Request.Builder()
                .url("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
                .header("User-Agent", "SafePawsApp/1.0 (Android Emulator; yassineebouchra@gmail.com)")
                .build()
            
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val jsonArray = org.json.JSONArray(body)
                if (jsonArray.length() > 0) {
                    val firstItem = jsonArray.getJSONObject(0)
                    val latStr = firstItem.optString("lat", "NaN")
                    val lngStr = firstItem.optString("lon", "NaN")
                    val lat = latStr.toDoubleOrNull() ?: Double.NaN
                    val lng = lngStr.toDoubleOrNull() ?: Double.NaN
                    if (!lat.isNaN() && !lng.isNaN()) {
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(lat, lng)
                        }
                        return@launch
                    }
                }
            }
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(null, null)
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "Geocoding failed for '$query': ${e.message}")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                onResult(null, null)
            }
        }
    }
}

// Create modern, sleek, and compact map marker pins (substantially smaller, crisp vector symbols)
fun createMarkerIcon(context: android.content.Context, category: String): android.graphics.drawable.Drawable {
    val density = context.resources.displayMetrics.density
    
    // 1. Sleek GPS pulsating location dot for user's current location (20dp x 20dp)
    if (category == "موقعي الحالي") {
        val dotSize = (20 * density).toInt().coerceAtLeast(1)
        val bitmap = android.graphics.Bitmap.createBitmap(dotSize, dotSize, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val center = dotSize / 2f
        
        // Outer soft cyan glow
        paint.color = android.graphics.Color.parseColor("#3306B6D4")
        paint.style = android.graphics.Paint.Style.FILL
        canvas.drawCircle(center, center, center - 1f, paint)
        
        // Crisp white ring
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(center, center, center - (2.5f * density), paint)
        
        // Vibrant cyan core dot
        paint.color = android.graphics.Color.parseColor("#0EA5E9")
        canvas.drawCircle(center, center, center - (4.5f * density), paint)
        
        return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    }
    
    // 2. Compact modern teardrop map pin (24dp width x 30dp height - ~50% smaller than previous bulky 48dp)
    val width = (24 * density).toInt().coerceAtLeast(1)
    val height = (30 * density).toInt().coerceAtLeast(1)
    val widthF = width.toFloat()
    val heightF = height.toFloat()
    val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    
    // Modern color palette and high-contrast clean symbols
    val (pinColor, symbol) = when (category) {
        "عيادات بيطرية" -> Pair(android.graphics.Color.parseColor("#E11D48"), "+") // Medical cross on Rose Red
        "متاجر ومستلزمات" -> Pair(android.graphics.Color.parseColor("#EA580C"), "S") // Store on Vibrant Orange
        "الملاجئ والتبني" -> Pair(android.graphics.Color.parseColor("#0284C7"), "A") // Adoption on Sky Blue
        "مراكز تدريب وحدائق" -> Pair(android.graphics.Color.parseColor("#059669"), "P") // Park on Emerald
        "العناية والفنادق" -> Pair(android.graphics.Color.parseColor("#7C3AED"), "G") // Grooming on Violet
        "بلاغات مفقودة" -> Pair(android.graphics.Color.parseColor("#DC2626"), "!") // Warning Alert on Crimson
        "النقاط المستوردة" -> Pair(android.graphics.Color.parseColor("#0D9488"), "*") // Star marker on Teal
        else -> Pair(android.graphics.Color.parseColor("#475569"), "•") // Sleek Slate Dot
    }
    
    val centerX = widthF / 2f
    val headRadius = (widthF / 2f) - (2.2f * density)
    val headCenterY = headRadius + (2f * density)
    val tipY = heightF - (2f * density)
    
    // A. Soft drop shadow under the tip
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#26000000")
    val shadowW = 3.5f * density
    val shadowH = 3.0f * density
    canvas.drawOval(
        centerX - shadowW,
        heightF - shadowH,
        centerX + shadowW,
        heightF,
        paint
    )
    
    // B. Draw streamlined pin teardrop path
    val pinPath = android.graphics.Path().apply {
        arcTo(
            centerX - headRadius,
            headCenterY - headRadius,
            centerX + headRadius,
            headCenterY + headRadius,
            145f,
            250f,
            false
        )
        lineTo(centerX, tipY)
        close()
    }
    
    // Fill pin body with category vibrant color
    paint.color = pinColor
    paint.style = android.graphics.Paint.Style.FILL
    canvas.drawPath(pinPath, paint)
    
    // Draw clean crisp white border contour
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 1.2f * density
    canvas.drawPath(pinPath, paint)
    
    // C. Inner white circular badge
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    val innerBadgeRadius = headRadius * 0.65f
    canvas.drawCircle(centerX, headCenterY, innerBadgeRadius, paint)
    
    // D. Crisp, sharp bold icon symbol centered inside the badge
    paint.color = pinColor
    paint.textSize = innerBadgeRadius * 1.35f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
    val fontMetrics = paint.fontMetrics
    val textY = headCenterY - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(symbol, centerX, textY, paint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("الكل") }
    var selectedPlace by remember { mutableStateOf<PetPlace?>(null) }
    var isMapLoading by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var isQuotaExceeded by remember { mutableStateOf(false) }

    val incidentsList by viewModel.strayIncidents.collectAsState()

    // Start centered on initial default coordinates
    var mapCenter by remember { mutableStateOf(GeoPoint(34.0181, -6.8358)) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var userAccuracy by remember { mutableStateOf<Float?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var hasCenteredOnRealLocation by remember { mutableStateOf(false) }
    var placesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var webPlacesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var hasAutoCentered by remember { mutableStateOf(false) }

    // Google Play Services Fused Location Client for precise, real hardware GPS & Network positioning
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Keep direct reference to the MapView to coordinate programmatic shifts cleanly without recomposition feedback-loops
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // World Borders State (matching mapsafe-paws.netlify.app's tool-borders)
    var showBorders by rememberSaveable { mutableStateOf(true) }
    var borderColorHex by rememberSaveable { mutableStateOf("#E81CFF") }
    var isBorderDashed by rememberSaveable { mutableStateOf(true) }
    var currentMapTileType by rememberSaveable { mutableStateOf("street") }
    var isBordersLoading by remember { mutableStateOf(false) }

    val displayDensity = context.resources.displayMetrics.density
    val worldBordersOverlay = remember {
        WorldBordersOverlay(
            segments = emptyList(),
            density = displayDensity,
            colorHex = borderColorHex,
            isDashed = isBorderDashed
        )
    }

    // High-performance async loader for world borders
    LaunchedEffect(Unit) {
        isBordersLoading = true
        val segments = WorldBordersRepository.getBorderSegments(context)
        worldBordersOverlay.segments = segments
        isBordersLoading = false
        mapView?.invalidate()
    }

    // Dynamic style update without re-allocating or re-parsing
    LaunchedEffect(borderColorHex, isBorderDashed) {
        worldBordersOverlay.updateStyle(borderColorHex, isBorderDashed)
        mapView?.invalidate()
    }

    // Fast in-memory cache for map marker icons to eliminate runtime bitmap allocation during pan & zoom
    val markerIconCache = remember { HashMap<String, android.graphics.drawable.Drawable>() }
    fun getCachedMarkerIcon(category: String): android.graphics.drawable.Drawable {
        return markerIconCache.getOrPut(category) {
            createMarkerIcon(context, category)
        }
    }

    val esriSatelliteSource = remember {
        XYTileSource(
            "EsriSatellite",
            0, 19, 256, ".jpg",
            arrayOf(
                "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"
            )
        )
    }

    // Night Mode State and Filter for Map
    var isNightMode by rememberSaveable { mutableStateOf(false) }
    val nightModeFilter = remember {
        val nightMatrix = ColorMatrix().apply {
            val src = floatArrayOf(
                -0.80f, 0.00f, 0.00f, 0.00f, 230f,
                0.00f, -0.80f, 0.00f, 0.00f, 230f,
                0.00f, 0.00f, -0.75f, 0.00f, 240f,
                0.00f, 0.00f, 0.00f, 1.00f, 0f
            )
            set(src)
        }
        ColorMatrixColorFilter(nightMatrix)
    }

    // Manual GeoJSON Import Dialog States
    var showImportDialog by remember { mutableStateOf(false) }
    var rawGeoJsonText by remember { mutableStateOf("") }
    var geoJsonUrlInput by remember { mutableStateOf("") }

    LaunchedEffect(mapCenter) {
        mapView?.let { mv ->
            mv.controller.animateTo(mapCenter)
        }
    }

    // Only auto-center on web places if user location hasn't been acquired yet
    LaunchedEffect(webPlacesList, mapView) {
        val mv = mapView
        if (mv != null && webPlacesList.isNotEmpty() && !hasAutoCentered && userLocation == null && !hasCenteredOnRealLocation) {
            hasAutoCentered = true
            val lats = webPlacesList.map { it.lat }
            val lngs = webPlacesList.map { it.lng }
            val maxLat = lats.maxOrNull() ?: 24.7136
            val minLat = lats.minOrNull() ?: 24.7136
            val maxLng = lngs.maxOrNull() ?: 46.6753
            val minLng = lngs.minOrNull() ?: 46.6753
            
            if (maxLat == minLat && maxLng == minLng) {
                mapCenter = GeoPoint(maxLat, maxLng)
                mv.controller.setZoom(14.0)
            } else {
                val latPadding = (maxLat - minLat) * 0.15
                val lngPadding = (maxLng - minLng) * 0.15
                val paddedMaxLat = (maxLat + latPadding).coerceAtMost(90.0)
                val paddedMinLat = (minLat - latPadding).coerceAtLeast(-90.0)
                val paddedMaxLng = (maxLng + lngPadding).coerceAtMost(180.0)
                val paddedMinLng = (minLng - lngPadding).coerceAtLeast(-180.0)
                val box = org.osmdroid.util.BoundingBox(paddedMaxLat, paddedMaxLng, paddedMinLat, paddedMinLng)
                
                kotlinx.coroutines.delay(800L)
                mv.zoomToBoundingBox(box, true)
            }
        }
    }

    // Direct helper to update state and smoothly focus map on user's real physical coordinates
    val updateWithRealLocation: (Location, Boolean) -> Unit = { loc, shouldAnimate ->
        val gp = GeoPoint(loc.latitude, loc.longitude)
        userLocation = gp
        if (loc.hasAccuracy()) {
            userAccuracy = loc.accuracy
        }
        if (!hasCenteredOnRealLocation || shouldAnimate) {
            hasCenteredOnRealLocation = true
            mapCenter = gp
            mapView?.let { mv ->
                mv.controller.animateTo(gp)
                mv.controller.setZoom(16.5)
                mv.invalidate()
            }
        }
    }

    // High-precision location request coordinator
    val requestRealLocation: (Boolean) -> Unit = { isUserInitiated ->
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            if (isUserInitiated) isLocating = true

            // 1. Fast warm-cache check for instantaneous zero-wait response
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                    if (lastLoc != null) {
                        updateWithRealLocation(lastLoc, isUserInitiated && userLocation == null)
                    }
                }
            } catch (_: SecurityException) {}

            // 2. High Accuracy hardware location fix via Fused Location Provider
            try {
                val cts = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        isLocating = false
                        if (loc != null) {
                            updateWithRealLocation(loc, isUserInitiated)
                            if (isUserInitiated) {
                                val accText = if (loc.hasAccuracy()) " (دقة ±${loc.accuracy.toInt()}م)" else ""
                                android.widget.Toast.makeText(context, "تم تحديد موقعك الحقيقي بنجاح$accText", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Fallback to Native LocationManager providers (GPS & Network)
                            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            val providers = lm.getProviders(true)
                            var bestLoc: Location? = null
                            for (p in providers) {
                                val l = try { lm.getLastKnownLocation(p) } catch (_: SecurityException) { null } ?: continue
                                if (bestLoc == null || (l.hasAccuracy() && l.accuracy < (bestLoc?.accuracy ?: Float.MAX_VALUE))) {
                                    bestLoc = l
                                }
                            }
                            if (bestLoc != null) {
                                updateWithRealLocation(bestLoc, isUserInitiated)
                                if (isUserInitiated) {
                                    android.widget.Toast.makeText(context, "تم تحديد موقعك الحقيقي بدقة", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else if (isUserInitiated) {
                                val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                                if (!isGpsEnabled) {
                                    android.widget.Toast.makeText(context, "يرجى تشغيل الـ GPS في جهازك للحصول على موقعك الحقيقي بدقة", android.widget.Toast.LENGTH_LONG).show()
                                } else {
                                    android.widget.Toast.makeText(context, "جاري انتظار إشارة الـ GPS الفعلي...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        isLocating = false
                        if (isUserInitiated) {
                            android.widget.Toast.makeText(context, "تعذر تحديد الموقع، يرجى التأكد من تشغيل الـ GPS", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: SecurityException) {
                isLocating = false
            }
        }
    }

    // Continuous Real-Time High Accuracy Location Tracking (GPS + Network)
    DisposableEffect(context) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        var locationCallback: LocationCallback? = null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val nativeListener = LocationListener { loc ->
            updateWithRealLocation(loc, false)
        }

        if (hasFine || hasCoarse) {
            // A. Google Play Services high-accuracy updates
            try {
                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L)
                    .setMinUpdateDistanceMeters(1.5f)
                    .setMinUpdateIntervalMillis(1500L)
                    .build()

                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(res: LocationResult) {
                        res.lastLocation?.let { loc ->
                            updateWithRealLocation(loc, false)
                        }
                    }
                }
                fusedLocationClient.requestLocationUpdates(req, locationCallback, android.os.Looper.getMainLooper())
            } catch (_: SecurityException) {}

            // B. Native provider backup updates
            try {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 1.5f, nativeListener)
                }
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000L, 1.5f, nativeListener)
                }
            } catch (_: SecurityException) {}
        }

        onDispose {
            locationCallback?.let {
                try { fusedLocationClient.removeLocationUpdates(it) } catch (_: Exception) {}
            }
            try { lm.removeUpdates(nativeListener) } catch (_: Exception) {}
        }
    }

    // Dynamic standard location request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            requestRealLocation(true)
        }
    }

    // Initialize osmdroid tile cache configuration with correct user-agent and generous cache for smooth scrolling
    LaunchedEffect(Unit) {
        val osmConfig = Configuration.getInstance()
        osmConfig.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        osmConfig.userAgentValue = context.packageName
        osmConfig.osmdroidBasePath = context.cacheDir
        osmConfig.osmdroidTileCache = context.cacheDir
        osmConfig.cacheMapTileCount = 48
        osmConfig.cacheMapTileOvershoot = 24
        osmConfig.tileDownloadThreads = 4
        osmConfig.tileFileSystemThreads = 4

        // Load initial live OSM places around current center coordinates via Overpass
        isMapLoading = false
        placesList = emptyList()
    }

    // Launch location prompt as soon as user opens map, alongside reliable IP API fallback
    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            requestRealLocation(false)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        // Fast IP location as initial temporary preview only while GPS locks, NEVER override real GPS
        fetchIpLocation { lat, lng ->
            if (userLocation == null && !hasCenteredOnRealLocation) {
                val gp = GeoPoint(lat, lng)
                mapCenter = gp
                userLocation = gp
                userAccuracy = 400f
                mapView?.let { mv ->
                    mv.controller.animateTo(gp)
                    mv.controller.setZoom(15.0)
                }
            }
        }
    }

    // Real-time periodic website sync poller: polls periodically to grab any dashboard changes sustainably!
    LaunchedEffect(Unit) {
        while (true) {
            isSyncing = true
            fetchGeoJsonFromUrl(context, "https://mapsafe-paws.netlify.app/viewer.html") { fetchedWebPlaces, quotaErr ->
                // If the dynamic load was successful (not restricted by quota exhaustion), we accept the final list (even if empty) to support deletions.
                if (!quotaErr) {
                    if (webPlacesList.isNotEmpty() && fetchedWebPlaces.isNotEmpty()) {
                        val existingIds = webPlacesList.map { it.id }.toSet()
                        val newPlaces = fetchedWebPlaces.filter { it.id !in existingIds }
                        if (newPlaces.isNotEmpty()) {
                            val latestPlace = newPlaces.first()
                            // Show standard visual toast for the user
                            android.widget.Toast.makeText(
                                context,
                                "تمت إضافة معلم جديد على الخريطة: ${latestPlace.name}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                            // Automatically animate map center directly to focus on the newly added spot!
                            mapCenter = GeoPoint(latestPlace.lat, latestPlace.lng)
                            mapView?.let { mv ->
                                mv.controller.animateTo(mapCenter)
                                mv.controller.setZoom(16.0)
                                mv.invalidate()
                            }
                        }
                    }
                    if (webPlacesList != fetchedWebPlaces) {
                        webPlacesList = fetchedWebPlaces
                    }
                }
                isQuotaExceeded = quotaErr
                isSyncing = false
            }
            kotlinx.coroutines.delay(3000L) // Fast periodic sync without excessive CPU wakes
        }
    }

    val locateUser: () -> Unit = {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            requestRealLocation(true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = false // Disable software tile rescaling for ultra-smooth 60-120fps panning
                    setBuiltInZoomControls(false)
                    minZoomLevel = 3.0
                    maxZoomLevel = 20.0
                    controller.setZoom(14.0)
                    controller.setCenter(mapCenter)
                    
                    // Hardware accelerated rendering & smooth fling momentum
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    isFlingEnabled = true
                    
                    // Prevent parents from intercepting touch gestures when panning/zooming
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    mapView = this
                }
            },
            update = { mv ->
                // Apply base map tile source (Street, Satellite, Dark, Terrain)
                when (currentMapTileType) {
                    "satellite" -> {
                        mv.setTileSource(esriSatelliteSource)
                        mv.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                    "dark" -> {
                        mv.setTileSource(TileSourceFactory.MAPNIK)
                        mv.overlayManager.tilesOverlay.setColorFilter(nightModeFilter)
                    }
                    "terrain" -> {
                        mv.setTileSource(TileSourceFactory.OpenTopo)
                        mv.overlayManager.tilesOverlay.setColorFilter(null)
                    }
                    else -> {
                        mv.setTileSource(TileSourceFactory.MAPNIK)
                        if (isNightMode) {
                            mv.overlayManager.tilesOverlay.setColorFilter(nightModeFilter)
                        } else {
                            mv.overlayManager.tilesOverlay.setColorFilter(null)
                        }
                    }
                }

                mv.overlays.clear()

                // 1. Draw Country Borders Layer with single-pass GPU-accelerated Overlay
                if (showBorders) {
                    mv.overlays.add(worldBordersOverlay)
                }

                // 2. Draw user real location with accuracy halo if available
                userLocation?.let { loc ->
                    userAccuracy?.let { acc ->
                        if (acc in 5f..1500f) {
                            val circlePoints = Polygon.pointsAsCircle(loc, acc.toDouble())
                            val accuracyCircle = Polygon(mv).apply {
                                points = circlePoints
                                fillPaint.color = android.graphics.Color.parseColor("#180EA5E9")
                                outlinePaint.color = android.graphics.Color.parseColor("#440EA5E9")
                                outlinePaint.strokeWidth = 1.5f * context.resources.displayMetrics.density
                            }
                            mv.overlays.add(accuracyCircle)
                        }
                    }
                    val userM = Marker(mv).apply {
                        position = loc
                        title = "موقعي الحقيقي"
                        subDescription = if (userAccuracy != null) "دقة الموقع: ±${userAccuracy?.toInt()} متر" else "موقع محدد عبر الـ GPS"
                        icon = getCachedMarkerIcon("موقعي الحالي")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    mv.overlays.add(userM)
                }

                // 3. Filter and Draw only dynamically tracked website map pins with cached icons
                val combinedPlaces = webPlacesList.distinctBy { it.id }
                combinedPlaces.forEach { place ->
                    val matchesQuery = searchQuery.isBlank() || 
                                       place.name.contains(searchQuery, ignoreCase = true) || 
                                       place.desc.contains(searchQuery, ignoreCase = true)
                    
                    val matchesCategory = filterCategory == "الكل" || place.category == filterCategory

                    if (matchesQuery && matchesCategory) {
                        val pm = Marker(mv).apply {
                            position = GeoPoint(place.lat, place.lng)
                            title = place.name
                            subDescription = place.desc
                            icon = getCachedMarkerIcon(place.category)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { marker, map ->
                                selectedPlace = place
                                true
                            }
                        }
                        mv.overlays.add(pm)
                    }
                }

                mv.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 1. Top Controls: Search Bar + Category Filters
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 28.dp) // Leave clean space for edge-to-edge status bar
        ) {
            // Elegant real-time search & live sync header card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_field"),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "البحث عن عيادات، متاجر، بلاغات مفقودة...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable dynamic M3 category filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipsList = listOf(
                    Triple("الكل", Icons.Default.AllInclusive, "الكل"),
                    Triple("عيادات بيطرية", Icons.Default.LocalHospital, "عيادات بيطرية"),
                    Triple("متاجر ومستلزمات", Icons.Default.ShoppingCart, "متاجر ومستلزمات"),
                    Triple("الملاجئ والتبني", Icons.Default.Pets, "الملاجئ والتبني"),
                    Triple("العناية والفنادق", Icons.Default.ContentCut, "العناية والفنادق"),
                    Triple("بلاغات مفقودة", Icons.Default.Warning, "بلاغات مفقودة"),
                    Triple("النقاط المستوردة", Icons.Default.Language, "النقاط المستوردة")
                )
                chipsList.forEach { (catLabel, catIcon, catKey) ->
                    val isSelected = filterCategory == catKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = catKey },
                        leadingIcon = {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(catLabel, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            
            // Removed external map banner overlay as requested

        }

        // 2. Floating Map Action Buttons (Night Mode, Locate User, Zoom controls)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // A. Night Mode Toggle Button (الوضع الليلي)
            FilledTonalIconButton(
                onClick = {
                    isNightMode = !isNightMode
                    val msg = if (isNightMode) "تم تفعيل الوضع الليلي للخريطة" else "تم تفعيل الوضع النهاري للخريطة"
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(6.dp, CircleShape)
                    .testTag("map_night_mode_btn"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isNightMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
                    contentColor = if (isNightMode) Color(0xFFFFD166) else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isNightMode) "الوضع النهاري" else "الوضع الليلي",
                    modifier = Modifier.size(22.dp)
                )
            }

            // C. Locate My Location Button (تحديد موقعي الحقيقي)
            FilledIconButton(
                onClick = {
                    locateUser()
                },
                modifier = Modifier
                    .size(48.dp)
                    .shadow(6.dp, CircleShape)
                    .testTag("map_my_location_btn"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "تحديد موقعي الحقيقي",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // C. Zoom In Button
            FilledTonalIconButton(
                onClick = {
                    mapView?.let { mv ->
                        mv.controller.zoomIn()
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("map_zoom_in_btn"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "تكبير الخريطة",
                    modifier = Modifier.size(20.dp)
                )
            }

            // D. Zoom Out Button
            FilledTonalIconButton(
                onClick = {
                    mapView?.let { mv ->
                        mv.controller.zoomOut()
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .shadow(4.dp, CircleShape)
                    .testTag("map_zoom_out_btn"),
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "تصغير الخريطة",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Loading cover
        if (isMapLoading) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
            }
        }

        // Expanded Place Detail Floating card
        selectedPlace?.let { place ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp)
                    .padding(bottom = 100.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = place.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("•", color = Color.Gray)
                                    Text(
                                        text = "⭐ ${place.rating} (${place.reviews} مراجعة)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedPlace = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = place.desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(text = "ساعات العمل:", fontSize = 9.sp, color = Color.Gray)
                                }
                                Text(text = place.hours, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(text = "هاتف الخدمة:", fontSize = 9.sp, color = Color.Gray)
                                }
                                Text(text = place.phone, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("phone", place.phone)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "تم نسخ الرقم: ${place.phone}", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "اتصال", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val gmmIntentUri = android.net.Uri.parse("geo:0,0?q=${place.lat},${place.lng}(${place.name})")
                                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(mapIntent)
                                    } else {
                                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${place.lat},${place.lng}"))
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = "الاتجاهات", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الاتجاهات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "استيراد",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "استيراد نقاط خريطة تفاعلية",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "أدخل رابط GeoJSON مباشر، أو الصق الكود البرمجي لنقاطك مباشرة أدناه لعرضها فوراً على الخريطة.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        TextField(
                            value = geoJsonUrlInput,
                            onValueChange = { geoJsonUrlInput = it },
                            placeholder = { Text("أدخل رابط GeoJSON (مثال: رابط raw من GitHub)", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "أو الصق الكود البرمجي مباشرة (Raw GeoJSON Text):",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = rawGeoJsonText,
                            onValueChange = { rawGeoJsonText = it },
                            placeholder = { Text("""{"type": "FeatureCollection", "features": [...]}""", fontSize = 10.sp) },
                            maxLines = 6,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        
                        if (isSyncing) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جاري جلب البيانات...", fontSize = 11.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(
                            onClick = {
                                if (geoJsonUrlInput.isNotBlank()) {
                                    isSyncing = true
                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val client = okhttp3.OkHttpClient()
                                            val request = okhttp3.Request.Builder().url(geoJsonUrlInput).get().build()
                                            val response = client.newCall(request).execute()
                                            val bodyStr = response.body?.string() ?: ""
                                            if (bodyStr.isNotBlank()) {
                                                val parsed = parseGeoJsonStringToPlaces(bodyStr)
                                                if (parsed.isNotEmpty()) {
                                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        webPlacesList = parsed
                                                        showImportDialog = false
                                                        isSyncing = false
                                                    }
                                                } else {
                                                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                        android.widget.Toast.makeText(context, "الرابط لا يحتوي على نقاط GeoJSON صالحة", android.widget.Toast.LENGTH_LONG).show()
                                                        isSyncing = false
                                                    }
                                                }
                                            } else {
                                                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    android.widget.Toast.makeText(context, "الاستجابة فارغة", android.widget.Toast.LENGTH_SHORT).show()
                                                    isSyncing = false
                                                }
                                            }
                                        } catch (e: Exception) {
                                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                android.widget.Toast.makeText(context, "فشل جلب الرابط: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                                isSyncing = false
                                            }
                                        }
                                    }
                                } else if (rawGeoJsonText.isNotBlank()) {
                                    val parsed = parseGeoJsonStringToPlaces(rawGeoJsonText)
                                    if (parsed.isNotEmpty()) {
                                        webPlacesList = parsed
                                        showImportDialog = false
                                        android.widget.Toast.makeText(context, "تم استيراد ${parsed.size} نقاط بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "خطأ في قراءة كود GeoJSON المدخل", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "يرجى ملء الحقول أولاً", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("استيراد وتطبيق", fontWeight = FontWeight.Bold)
                        }
                        
                        TextButton(onClick = { showImportDialog = false }) {
                            Text("إلغاء", color = Color.Gray)
                        }
                    }
                }
            )
        }

    }
}




