package com.example

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebMapManager {
    private const val TAG = "WebMapManager"
    
    // ملاحظة: يمكنك تغيير هذا الرابط ليكون رابط موقعك على استضافة InfinityFree
    // مثال: http://safepawslive.infinityfreeapp.com/api.php
    // للـ Emulator المحلي بالأندرويد، الرابط 10.0.2.2 يشير للـ localhost لجهاز الكمبيوتر المضيف
    private const val BASE_URL = "http://10.0.2.2/web-map/api.php"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    // البلاغات التجريبية الافتراضية للرجوع إليها في حال فشل الشبكة
    val fallbackIncidents = listOf(
        StrayIncident("s1", "أربعة جراء ضائعة بحاجة لرعاية عاجلة", "تم العثور عليها وحالتها مستقرة بانتظار تبنيها.", "حي الياسمين، الرياض", "أحمد محمد", 41, 240, true, "منذ يومين", 24.8251, 46.6373),
        StrayIncident("s2", "كلب جولدن - حالة طارئة", "يحتاج لعملية جراحية عاجلة. شارك للمساعدة!", "حي الملز، الرياض", "أحمد محمد", 18, 1200, true, "منذ ٥ ساعات", 24.6644, 46.7311),
        StrayIncident("s3", "جمال العيون في الطبيعة", "لقطة فنية لإحدى حالات الإنقاذ السابقة.", "حي السليمانية، الرياض", "سارة أحمد", 124, 182, false, "منذ ٣ أيام", 24.7031, 46.6961)
    )

    // الأماكن التجريبية الافتراضية
    val fallbackPlaces = listOf(
        PetPlace(
            id = "p1",
            name = "صندوق مياه سبيل - حي السليمانية",
            category = "صناديق تغذية ومياه 🍲",
            type = "feeding",
            lat = 24.7081,
            lng = 46.6911,
            desc = "صندوق بلاستيكي نظيف يتم تعبئته يومياً بالماء والطعام الجاف للقطط والكلاب الضالة.",
            rating = "5.0",
            reviews = "12",
            phone = "غير متوفر",
            hours = "على مدار الساعة",
            imageUrl = "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=400&auto=format&fit=crop&q=60"
        ),
        PetPlace(
            id = "p2",
            name = "عيادة النخبة البيطرية",
            category = "عيادات بيطرية 🏥",
            type = "clinic",
            lat = 24.7156,
            lng = 46.6782,
            desc = "عيادة متميزة لتقديم الرعاية الطبية، الجراحات، والتطعيمات بأسعار مخفضة للحيوانات المنقذة.",
            rating = "4.8",
            reviews = "54",
            phone = "+966 11 456 7890",
            hours = "من ٩ صباحاً إلى ١٠ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1584132967334-10e028bd69f7?w=400&auto=format&fit=crop&q=60"
        ),
        PetPlace(
            id = "p3",
            name = "ملجأ بيت الأمان الرئيسي",
            category = "الملاجئ والتبني 🐶",
            type = "shelter",
            lat = 24.7350,
            lng = 46.7120,
            desc = "الملجأ المركزي لجمعية بيت الأمان، يحتوي على ساحات لعب للجراء والقطط ومكاتب المقابلات الشخصية للتبني.",
            rating = "4.9",
            reviews = "98",
            phone = "+966 50 123 4567",
            hours = "من ٤ مساءً إلى ٩ مساءً",
            imageUrl = "https://images.unsplash.com/photo-1548767797-d8c844163c4c?w=400&auto=format&fit=crop&q=60"
        )
    )

    // جلب قائمة البلاغات
    suspend fun getIncidents(): List<StrayIncident> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL?action=get_incidents")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fallbackIncidents
                val bodyStr = response.body?.string() ?: return@withContext fallbackIncidents
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<StrayIncident>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        StrayIncident(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.getString("description"),
                            location = obj.getString("location"),
                            reporter = obj.getString("reporter"),
                            commentsCount = obj.optInt("comments_count", 0),
                            likesCount = obj.optInt("likes_count", 0),
                            isEmergency = obj.optBoolean("is_emergency", false) || obj.optInt("is_emergency", 0) == 1,
                            timestamp = obj.getString("timestamp"),
                            latitude = if (obj.isNull("latitude")) null else obj.getDouble("latitude"),
                            longitude = if (obj.isNull("longitude")) null else obj.getDouble("longitude")
                        )
                    )
                }
                if (list.isEmpty()) fallbackIncidents else list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching incidents from API, using fallback data", e)
            fallbackIncidents
        }
    }

    // إضافة بلاغ جديد
    suspend fun addIncident(incident: StrayIncident): Boolean = withContext(Dispatchers.IO) {
        val jsonObj = JSONObject()
            .put("id", incident.id)
            .put("title", incident.title)
            .put("description", incident.description)
            .put("location", incident.location)
            .put("reporter", incident.reporter)
            .put("comments_count", incident.commentsCount)
            .put("likes_count", incident.likesCount)
            .put("is_emergency", incident.isEmergency)
            .put("timestamp", incident.timestamp)
            .put("latitude", incident.latitude ?: JSONObject.NULL)
            .put("longitude", incident.longitude ?: JSONObject.NULL)

        val requestBody = jsonObj.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url("$BASE_URL?action=add_incident")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding incident to API", e)
            false
        }
    }

    // جلب الأماكن
    suspend fun getPlaces(): List<PetPlace> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL?action=get_places")
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext fallbackPlaces
                val bodyStr = response.body?.string() ?: return@withContext fallbackPlaces
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<PetPlace>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        PetPlace(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            category = obj.getString("category"),
                            type = obj.getString("type"),
                            lat = obj.getDouble("lat"),
                            lng = obj.getDouble("lng"),
                            desc = obj.getString("description"),
                            rating = obj.optString("rating", "5.0"),
                            reviews = obj.optString("reviews", "1"),
                            phone = obj.optString("phone", "غير متوفر"),
                            hours = obj.optString("hours", "على مدار الساعة"),
                            imageUrl = obj.optString("image_url", "")
                        )
                    )
                }
                if (list.isEmpty()) fallbackPlaces else list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching places from API, using fallback data", e)
            fallbackPlaces
        }
    }

    // إضافة موقع مخصص جديد
    suspend fun addPlace(place: PetPlace): Boolean = withContext(Dispatchers.IO) {
        val jsonObj = JSONObject()
            .put("id", place.id)
            .put("name", place.name)
            .put("category", place.category)
            .put("type", place.type)
            .put("lat", place.lat)
            .put("lng", place.lng)
            .put("description", place.desc)
            .put("rating", place.rating)
            .put("reviews", place.reviews)
            .put("phone", place.phone)
            .put("hours", place.hours)
            .put("image_url", place.imageUrl)

        val requestBody = jsonObj.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url("$BASE_URL?action=add_place")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding place to API", e)
            false
        }
    }
}
