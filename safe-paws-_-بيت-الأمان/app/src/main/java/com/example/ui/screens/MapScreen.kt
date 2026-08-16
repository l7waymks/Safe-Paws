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

// Retrieve approximate user location via IP API as a highly accurate fallback when GPS is sluggish or unavailable
fun fetchIpLocation(onResult: (Double, Double) -> Unit) {
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        // Try Free IPAPI.co first (supports free HTTPS)
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url("https://ipapi.co/json/")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (body.isNotBlank()) {
                val json = org.json.JSONObject(body)
                val lat = json.optDouble("latitude", Double.NaN)
                val lng = json.optDouble("longitude", Double.NaN)
                if (!lat.isNaN() && !lng.isNaN()) {
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        onResult(lat, lng)
                    }
                    return@launch
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipapi.co failed: ${e.message}")
        }

        // Try Free ipinfo.io as fallback (supports free HTTPS)
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val request = okhttp3.Request.Builder()
                .url("https://ipinfo.io/json")
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
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                            onResult(lat, lng)
                        }
                        return@launch
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MapScreen", "ipinfo.io failed: ${e.message}")
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
        else -> "معلم مضاف 📍"
    }.ifBlank { "معلم مضاف 📍" }
    
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
            Pair("عيادات بيطرية 🏥", "clinic")
        }
        lowercaseCategory.contains("store") || lowercaseCategory.contains("shop") || lowercaseShop.isNotBlank() || lowercaseName.contains("متجر") || lowercaseName.contains("محل") || desc.contains("متجر") || desc.contains("محل") -> {
            Pair("متاجر ومستلزمات 🛒", "shop")
        }
        lowercaseCategory.contains("shelter") || lowercaseCategory.contains("adopt") || lowercaseName.contains("ملجأ") || lowercaseName.contains("إيواء") || desc.contains("ملجأ") || desc.contains("إيواء") -> {
            Pair("الملاجئ والتبني 🐶", "shelter")
        }
        lowercaseCategory.contains("park") || lowercaseCategory.contains("training") || lowercaseName.contains("حديقة") || lowercaseName.contains("منتزه") || desc.contains("حديقة") || desc.contains("منتزه") -> {
            Pair("مراكز تدريب وحدائق 🎓", "park")
        }
        else -> {
            Pair("النقاط المستوردة 🌐", "clinic")
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
            name = "عيادة الأليف المتقدمة البيطرية 🏥",
            category = "النقاط المستوردة 🌐",
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
            name = "متجر أليفي لملحقات الحيوانات 🛒",
            category = "النقاط المستوردة 🌐",
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
            name = "ملجأ الرياض لتبني القطط والكلاب 🐶",
            category = "النقاط المستوردة 🌐",
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
            name = "صالون وسبا المخالب اللطيفة ✂️",
            category = "النقاط المستوردة 🌐",
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
            name = "فندق أليف للرعاية الفندقية 🐾",
            category = "النقاط المستوردة 🌐",
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
            name = "مستشفى الرياض البيطري الدولي 🏥",
            category = "النقاط المستوردة 🌐",
            type = "clinic",
            lat = 24.6948,
            lng = 46.7215,
            desc = "طوارئ وجراحة على مدار الساعة، أشعة، تحاليل مخبرية، وعناية مركزة للحالات الحرجة.",
            rating = "5.0",
            reviews = "موقع",
            phone = "+966 11 475 8888",
            hours = "٢٤ ساعة",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400"
        ),
        PetPlace(
            id = "fallback_web_7",
            name = "حديقة الأليف السعيدة للتدريب 🎓",
            category = "النقاط المستوردة 🌐",
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
            name = "نقطة إطعام القطط المشردة - العليا 🐈",
            category = "النقاط المستوردة 🌐",
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
            name = "متجر واحة الحيوان الشامل 🛒",
            category = "النقاط المستوردة 🌐",
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
            name = "مركز بسمة أليف للعناية الطبية 🏥",
            category = "النقاط المستوردة 🌐",
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
            name = "نادي ومسبح الكلاب الرياضي 🐾",
            category = "النقاط المستوردة 🌐",
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
            name = "جمعية رفق للرفق بالحيوان 🐶",
            category = "النقاط المستوردة 🌐",
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
            name = "مركز شفاء للطب البيطري - الدار البيضاء 🏥",
            category = "النقاط المستوردة 🌐",
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
            name = "ملجأ الأمل لإنقاذ الحيوانات - الرباط 🐶",
            category = "النقاط المستوردة 🌐",
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
            name = "محل أليفي المدلل - طنجة 🛒",
            category = "النقاط المستوردة 🌐",
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
            name = "العيادة البيطرية الكبرى لحي النخيل - مراكش 🏥",
            category = "النقاط المستوردة 🌐",
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
            name = "ملجأ سوس لإنقاذ ورعاية القطط - أيت ملول 🐈",
            category = "النقاط المستوردة 🌐",
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
            name = "صالون تدليل الأنيق لخدمات التزيين - الدار البيضاء ✂️",
            category = "النقاط المستوردة 🌐",
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
    val supabaseTable = "markers"
    
    fetchEverythingFromSupabase(supabaseUrl, supabaseKey, supabaseTable) { fetched, error ->
        if (fetched.isNotEmpty() || error == null) {
            onResult(fetched, false)
        } else {
            android.util.Log.e("MapScreen", "Supabase error: $error")
            fetchGeoJsonFromUrlScraper(url, onResult)
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
                    else -> "النقاط المستوردة 🌐"
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
                        category = category ?: "النقاط المستوردة 🌐",
                        type = category ?: "النقاط المستوردة 🌐",
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
            val features = layerObj.optJSONArray("features") ?: continue
            for (j in 0 until features.length()) {
                val featArr = features.optJSONArray(j)
                val featObj = features.optJSONObject(j)
                
                if (featArr != null) {
                    if (featArr.length() >= 3) {
                        val coords = featArr.optJSONArray(1) ?: continue
                        val name = featArr.optString(2, "معلم مستورد").ifBlank { "معلم مستورد" }
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
                    val name = props.optString("name", props.optString("NAME", props.optString("nom", "معلم مستورد"))).ifBlank { "معلم مستورد" }
                    
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
        isVet -> Pair("عيادات بيطرية 🏥", "clinic")
        isShop -> Pair("متاجر ومستلزمات 🛒", "shop")
        isShelter -> Pair("الملاجئ والتبني 🐶", "shelter")
        isPark -> Pair("مراكز تدريب وحدائق 🎓", "park")
        else -> Pair("النقاط المستوردة 🌐", "clinic")
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
    var markersList = emptyList<PetPlace>()
    var layersList = emptyList<PetPlace>()
    var markersFinished = false
    var layersFinished = false
    var markersError: String? = null
    var layersError: String? = null
    
    fun checkAndComplete() {
        if (markersFinished && layersFinished) {
            val combined = (markersList + layersList).distinctBy { it.id }
            val finalError = if (markersError != null && layersError != null) {
                "Markers error: $markersError; Layers error: $layersError"
            } else {
                null
            }
            onResult(combined, finalError)
        }
    }
    
    fetchFromSupabase(url, anonKey, tableName) { fetched, error ->
        markersList = fetched
        markersError = error
        markersFinished = true
        checkAndComplete()
    }
    
    fetchLayersFromSupabase(url, anonKey) { fetchedLayers, error ->
        layersList = fetchedLayers
        layersError = error
        layersFinished = true
        checkAndComplete()
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
                else -> "موقع تفاعلي مضاف 📍"
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
                    Pair("عيادات بيطرية 🏥", "clinic")
                }
                category.contains("store") || category.contains("shop") || desc.contains("متجر") || desc.contains("محل") -> {
                    Pair("متاجر ومستلزمات 🛒", "shop")
                }
                category.contains("shelter") || category.contains("adopt") || desc.contains("ملجأ") || desc.contains("إيواء") -> {
                    Pair("الملاجئ والتبني 🐶", "shelter")
                }
                category.contains("park") || category.contains("training") || desc.contains("حديقة") || desc.contains("منتزه") -> {
                    Pair("مراكز تدريب وحدائق 🎓", "park")
                }
                else -> {
                    Pair("النقاط المستوردة 🌐", "clinic")
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

// Create beautiful, custom map marker pins programmatically utilizing custom color coding of categories and relevant pet/care emojis
fun createMarkerIcon(context: android.content.Context, category: String): android.graphics.drawable.Drawable {
    val density = context.resources.displayMetrics.density
    val size = (48 * density).toInt() // Ensure proportional touch target visual representation (around 48dp)
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    
    // Determine color and emoji based on category
    val (pinColor, emoji) = when (category) {
        "عيادات بيطرية 🏥" -> Pair(android.graphics.Color.parseColor("#EF4444"), "🏥") // Crimson Red for Urgency/Health
        "متاجر ومستلزمات 🛒" -> Pair(android.graphics.Color.parseColor("#F97316"), "🛒") // Bright Orange for Retail
        "الملاجئ والتبني 🐶" -> Pair(android.graphics.Color.parseColor("#3B82F6"), "🐶") // Warm Blue for Shelter care
        "مراكز تدريب وحدائق 🎓" -> Pair(android.graphics.Color.parseColor("#10B981"), "🎓") // Emerald Green for Parks
        "العناية والفنادق ✂️" -> Pair(android.graphics.Color.parseColor("#8B5CF6"), "✂️") // Royal Purple for Grooming
        "بلاغات مفقودة 🚨" -> Pair(android.graphics.Color.parseColor("#EF1253"), "🚨") // Bright Warning Alert
        "النقاط المستوردة 🌐" -> Pair(android.graphics.Color.parseColor("#0D9488"), "🌐") // Teal for imported layers
        "موقعي الحالي" -> Pair(android.graphics.Color.parseColor("#06B6D4"), "🐾") // Cool Cyan with Cute Paw standard
        else -> Pair(android.graphics.Color.parseColor("#6B7280"), "📍") // Sleek Gray fallback
    }
    
    val centerX = size / 2f
    val centerY = size / 2.5f
    val radius = size / 3.2f
    
    // Draw marker shadow (beautiful soft dark blur)
    paint.color = android.graphics.Color.parseColor("#1F000000")
    canvas.drawCircle(centerX, centerY + (4 * density), radius + (2 * density), paint)
    
    // Draw pin pointer triangle pointing down
    val path = android.graphics.Path()
    path.moveTo(centerX - radius * 0.7f, centerY + radius * 0.5f)
    path.lineTo(centerX, size * 0.88f)
    path.lineTo(centerX + radius * 0.7f, centerY + radius * 0.5f)
    path.close()
    
    paint.color = pinColor
    canvas.drawPath(path, paint)
    
    // Draw outer pin circle boundary
    canvas.drawCircle(centerX, centerY, radius, paint)
    
    // Draw white border contour
    paint.color = android.graphics.Color.WHITE
    paint.style = android.graphics.Paint.Style.STROKE
    paint.strokeWidth = 2f * density
    canvas.drawCircle(centerX, centerY, radius, paint)
    
    // Draw inner white circle badge
    paint.style = android.graphics.Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centerX, centerY, radius * 0.75f, paint)
    
    // Draw the emoji icon centered in the badge
    paint.color = android.graphics.Color.BLACK
    paint.textSize = radius * 0.95f
    paint.textAlign = android.graphics.Paint.Align.CENTER
    val fontMetrics = paint.fontMetrics
    val textY = centerY - (fontMetrics.ascent + fontMetrics.descent) / 2f
    canvas.drawText(emoji, centerX, textY, paint)
    
    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}

@Composable
fun MapScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("الكل") }
    var selectedPlace by remember { mutableStateOf<PetPlace?>(null) }
    var isMapLoading by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var isQuotaExceeded by remember { mutableStateOf(false) }

    val incidentsList by viewModel.strayIncidents.collectAsState()

    // Start centered on Rabat, Morocco (34.0181, -6.8358)
    var mapCenter by remember { mutableStateOf(GeoPoint(34.0181, -6.8358)) }
    var userLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var placesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var webPlacesList by remember { mutableStateOf<List<PetPlace>>(emptyList()) }
    var hasAutoCentered by remember { mutableStateOf(false) }

    // Manual GeoJSON Import Dialog States
    var showImportDialog by remember { mutableStateOf(false) }
    var rawGeoJsonText by remember { mutableStateOf("") }
    var geoJsonUrlInput by remember { mutableStateOf("") }



    // Keep direct reference to the MapView to coordinate programmatic shifts cleanly without recomposition feedback-loops
    var mapView by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(mapCenter) {
        mapView?.let { mv ->
            mv.controller.animateTo(mapCenter)
        }
    }

    LaunchedEffect(webPlacesList, mapView) {
        val mv = mapView
        if (mv != null && webPlacesList.isNotEmpty() && !hasAutoCentered) {
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
                
                // Allow a tiny delay for MapView initialization layout before bounds animations
                kotlinx.coroutines.delay(800L)
                mv.zoomToBoundingBox(box, true)
            }
        }
    }

    // Configure continuous GPS and Network provider location tracking to dynamically update real-time user point on map
    val locationListener = remember {
        object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                val gp = GeoPoint(loc.latitude, loc.longitude)
                userLocation = gp
            }
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
    }

    DisposableEffect(context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000L, 2f, locationListener)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000L, 2f, locationListener)
            }
        } catch (e: SecurityException) {
            // Safe fallback
        }
        onDispose {
            try {
                locationManager.removeUpdates(locationListener)
            } catch (e: Exception) {
                // Safe disposal
            }
        }
    }

    // Initialize osmdroid tile cache configuration with correct user-agent on launch
    LaunchedEffect(Unit) {
        val osmConfig = Configuration.getInstance()
        osmConfig.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        osmConfig.userAgentValue = context.packageName
        osmConfig.osmdroidBasePath = context.cacheDir
        osmConfig.osmdroidTileCache = context.cacheDir

        // Load initial live OSM places around current center coordinates via Overpass
        isMapLoading = false
        placesList = emptyList()
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
                                "تمت إضافة معلم جديد على الخريطة: ${latestPlace.name} 🗺️",
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
                    webPlacesList = fetchedWebPlaces
                }
                isQuotaExceeded = quotaErr
                isSyncing = false
            }
            kotlinx.coroutines.delay(3000L) // Poll every 3 seconds for instant real-time sync with the website
        }
    }

    // Dynamic standard location request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                val providers = locationManager.getProviders(true)
                var bestLoc: Location? = null
                for (p in providers) {
                    val l = locationManager.getLastKnownLocation(p) ?: continue
                    if (bestLoc == null || l.accuracy < bestLoc.accuracy) {
                        bestLoc = l
                    }
                }
                bestLoc?.let {
                    val gp = GeoPoint(it.latitude, it.longitude)
                    userLocation = gp
                    mapCenter = gp

                    isMapLoading = false
                    placesList = emptyList()
                }
            } catch (e: SecurityException) {
                // Ignore silent security exceptions
            }
        }
    }

    // Launch location prompt as soon as user opens map, alongside reliable IP API fallback
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        fetchIpLocation { lat, lng ->
            if (userLocation == null) {
                val gp = GeoPoint(lat, lng)
                mapCenter = gp
                isMapLoading = false
                placesList = emptyList()
            }
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
                    isTilesScaledToDpi = true // Scales map tile images cleanly for crisp look and normalized touch density
                    setBuiltInZoomControls(false) // Disable clunky overlay buttons; standard pinch-to-zoom is intuitive and responsive
                    minZoomLevel = 4.0
                    maxZoomLevel = 20.0
                    controller.setZoom(14.0)
                    controller.setCenter(mapCenter)
                    
                    // Prevent parents from intercepting touch gestures when panning/zooming, keeping the map container strictly stable and fixed in place
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                    mapView = this
                }
            },
            update = { mv ->
                mv.overlays.clear()

                // 1. Draw user standard location pin if loaded
                userLocation?.let { loc ->
                    val userM = Marker(mv).apply {
                        position = loc
                        title = "موقعي الحالي 🐾"
                        icon = createMarkerIcon(context, "موقعي الحالي")
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mv.overlays.add(userM)
                }

                // 2. Filter and Draw only dynamically tracked website map pins
                val combinedPlaces = webPlacesList.distinctBy { it.id }
                combinedPlaces.forEach { place ->
                    val matchesQuery = searchQuery.isBlank() || 
                                       place.name.contains(searchQuery, ignoreCase = true) || 
                                       place.desc.contains(searchQuery, ignoreCase = true)
                    
                    val matchesCategory = filterCategory == "الكل" || place.category == filterCategory

                    if (matchesQuery && matchesCategory) {
                        val emoji = when (place.category) {
                            "عيادات بيطرية 🏥" -> "🏥"
                            "متاجر ومستلزمات 🛒" -> "🛒"
                            "الملاجئ والتبني 🐶" -> "🐶"
                            "مراكز تدريب وحدائق 🎓" -> "🎓"
                            "العناية والفنادق ✂️" -> "✂️"
                            "النقاط المستوردة 🌐" -> "🌐"
                            else -> "📍"
                        }
                        val pm = Marker(mv).apply {
                            position = GeoPoint(place.lat, place.lng)
                            title = "$emoji ${place.name}"
                            subDescription = place.desc
                            icon = createMarkerIcon(context, place.category)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            setOnMarkerClickListener { marker, map ->
                                selectedPlace = place
                                mapCenter = marker.position as GeoPoint
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

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable dynamic M3 category filter chips row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chipsList = listOf(
                    "الكل",
                    "عيادات بيطرية 🏥",
                    "متاجر ومستلزمات 🛒",
                    "الملاجئ والتبني 🐶",
                    "العناية والفنادق ✂️",
                    "بلاغات مفقودة 🚨",
                    "النقاط المستوردة 🌐"
                )
                chipsList.forEach { cat ->
                    val isSelected = filterCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
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
                    .padding(bottom = 6.dp)
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
                                Text(text = "🕒 ساعات العمل:", fontSize = 9.sp, color = Color.Gray)
                                Text(text = place.hours, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(text = "📞 هاتف الخدمة:", fontSize = 9.sp, color = Color.Gray)
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




