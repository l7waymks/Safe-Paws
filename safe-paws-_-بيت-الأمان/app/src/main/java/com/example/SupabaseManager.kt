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

data class AnimalItem(
    val id: String,
    val name: String,
    val species: String,
    val breed: String,
    val age: String,
    val gender: String,
    val size: String,
    val description: String,
    val imageUrl: String,
    val likesCount: Int,
    val specialNeeds: Boolean,
    val vaccinated: Boolean,
    val neutered: Boolean,
    val compatibility: String,
    val backstory: String,
    val matchPercentage: Int = 85,
    val priceStatus: String = "مجاني"
)

data class CommentItem(
    val id: String,
    val animalId: String,
    val author: String,
    val text: String,
    val createdAt: String
)

object SupabaseManager {
    private const val TAG = "SupabaseManager"
    private const val BASE_URL = "https://zbybaueqwizpuuykmllh.supabase.co/rest/v1"
    private const val API_KEY = "sb_publishable_Od9QmrQY2erFqGuP5u-JmA_I-dVy8SC"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    // Base fallback static animals
    val fallbackAnimals = listOf(
        AnimalItem(
            id = "1",
            name = "بندق",
            species = "كلب",
            breed = "جولدن ريتريفر",
            age = "سنتان",
            gender = "ذكر",
            size = "متوسط",
            description = "جرو ودود ومحب للعب، ذكي جداً ويبحث عن عائلة ترعاه وتحبه.",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAoDSKCeLAzLLM17fELuvP0tR8ureUg8OBaPcngBQw9v5hdsPZr2NYfASeVyJP1CYV3j0vgtGF8PApwnv_67ijmt3tq1cJDaiNSKU-rvY7WvivV5pNJjcFwm74bj49oL5foWS-yPG4Uo4KMvVY0fyM7OhcgJ9pv7eh3GyNJh4aqDixuJuZkbN4APoqE_nbLJVSkSzemNc2A_w8fWVL949cDMpGFNYv3k6GWJE09CJkSZfMbh2US1BPzExZIpEutGp7szWLmXQLZVEk",
            likesCount = 240,
            specialNeeds = false,
            vaccinated = true,
            neutered = true,
            compatibility = "ممتاز مع الأطفال والحيوانات الأخرى",
            backstory = "تم إنقاذه من حي الياسمين في الرياض بعد أن ضل طريقه وتلقى الرعاية الطبية الكاملة."
        ),
        AnimalItem(
            id = "2",
            name = "لونا",
            species = "قطة",
            breed = "شيرازي هجين",
            age = "٣ أشهر",
            gender = "أنثى",
            size = "صغير جداً",
            description = "قطة صغيرة نشيطة، تحب الجري واللعب بالكرات الملونة، هادئة ليلاً.",
            imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCdp1UGfnHAUK_JXoOnOPxaqIHc9L4POaavlf0dE2wZljvZ9i2dqiz2oy-r_yMY7edj-X4SXaiVTaJ86gv8lVnG9BYmp-p9kQN-ZDHu51w4p9Q9R4Nb9ZfkaUfCqQbGPDLIlonO6wGSlK9-7enVQdYBiZq6qymvru2FyHLm6F63TzZ6l8N-rPy_zgHKrH9JjjVEsVLr_HrSjoy88rAc7bBupYGyLiFNfe4vLIoM-hz_pMs7oqig3zEGJSgqe1Rq9dZUNpPBScufFhE", // kitten in cozy room
            likesCount = 124,
            specialNeeds = false,
            vaccinated = true,
            neutered = false,
            compatibility = "لطيفة جداً ولكن تفضل بيئة هادئة",
            backstory = "وجدت تحت شرفة منزل في حي السليمانية وهي وحيدة، واعتنت بها المتطوعة سارة حتى استقرت صحتها."
        ),
        AnimalItem(
            id = "3",
            name = "شاكي",
            species = "كلب",
            breed = "جيرمان شيبرد هجين",
            age = "سنة ونصف",
            gender = "ذكر",
            size = "كبير",
            description = "كلب حراسة رائع وودود في نفس الوقت مع العائلة. قوي البنية ويحب الجري.",
            imageUrl = "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&q=80&w=600",
            likesCount = 380,
            specialNeeds = true,
            vaccinated = true,
            neutered = true,
            compatibility = "مناسب لأصحاب الفلل والحدائق الواسعة",
            backstory = "أصيب في قدمه الخلفية إثر حادث بسيط وتم علاجه بنجاح بفضل تبرعات المجتمع الشركاء."
        ),
        AnimalItem(
            id = "4",
            name = "مشمش",
            species = "قطة",
            breed = "بلدي برتقالي",
            age = "سنة",
            gender = "ذكر",
            size = "متوسط",
            description = "مشمش قط اجتماعي واثق من نفسه ومحب لضرب كفوف الأيدي والتلاعب بصمت.",
            imageUrl = "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=600",
            likesCount = 95,
            specialNeeds = false,
            vaccinated = true,
            neutered = true,
            compatibility = "ممتاز لجميع أنواع العائلات والشقق",
            backstory = "عُثر عليه نائماً داخل أحد أنابيب التغذية لشبكة المياه وتم إنقاذه بأمان بفضل أبطال Rescue Waves."
        )
    )

    val fallbackComments = listOf(
        CommentItem("101", "1", "خالد الحربي", "بندق لطيف للغاية! رأيته في الملجأ الأسبوع الماضي وهو يحب اللعب بالكرة.", "منذ ساعتين"),
        CommentItem("102", "1", "منى الدوسري", "أتمنى له عائلة دافئة تستحق هذه العيون البريئة ❤️🐾", "منذ ٥ ساعات"),
        CommentItem("103", "2", "أمل السديري", "لونا تجنن! يا ريتني أقدر أتبناها عندي ٣ قطط بالبيت هالحين.", "منذ يومين")
    )

    suspend fun getAnimals(): List<AnimalItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/animals?select=*")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code ${response.code}: ${response.body?.string()}")
                    return@withContext fallbackAnimals
                }
                val bodyStr = response.body?.string() ?: return@withContext fallbackAnimals
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<AnimalItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(parseAnimal(obj))
                }
                if (list.isEmpty()) fallbackAnimals else list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching animals from Supabase, reverting to gorgeous offline database.", e)
            fallbackAnimals
        }
    }

    private fun parseAnimal(obj: JSONObject): AnimalItem {
        val id = obj.optString("id", "")
        val name = obj.optString("name", "أليف غامض")
        val species = obj.optString("species", obj.optString("type", "أليف"))
        val breed = obj.optString("breed", "فصيل منوع")
        val age = obj.optString("age", "غير معروف")
        val gender = obj.optString("gender", "غير محدد")
        val size = obj.optString("size", "متوسط")
        val desc = obj.optString("description", obj.optString("bio", "يبحث عن عائلة دافئة."))
        val likes = obj.optInt("likes_count", obj.optInt("likes", 0))
        val spNeeds = obj.optBoolean("special_needs", obj.optBoolean("specialNeeds", false))
        val vacc = obj.optBoolean("vaccinated", true)
        val neut = obj.optBoolean("neutered", true)
        val comp = obj.optString("compatibility", "متوافق مع محبي الحيوانات")
        val back = obj.optString("backstory", obj.optString("story", "تم إنقاذه وتقدم له الرعاية الطبية."))
        
        val priceStatus = obj.optString("price_status", "مجاني")
        
        // Find default local image matching the ID or fall back to high quality placeholders
        val img = obj.optString("image_url", obj.optString("imageUrl", ""))
        val finalImg = if (img.isEmpty() || img == "null") {
            when (id) {
                "1" -> fallbackAnimals[0].imageUrl
                "2" -> fallbackAnimals[1].imageUrl
                "3" -> fallbackAnimals[2].imageUrl
                "4" -> fallbackAnimals[3].imageUrl
                else -> "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=600"
            }
        } else {
            img
        }

        return AnimalItem(
            id = id,
            name = name,
            species = species,
            breed = breed,
            age = age,
            gender = gender,
            size = size,
            description = desc,
            imageUrl = finalImg,
            likesCount = likes,
            specialNeeds = spNeeds,
            vaccinated = vacc,
            neutered = neut,
            compatibility = comp,
            backstory = back,
            matchPercentage = (75..98).random(), // Realistic interactive PetMatch %
            priceStatus = priceStatus
        )
    }

    suspend fun postAnimal(
        name: String,
        species: String,
        breed: String,
        age: String,
        gender: String,
        description: String,
        backstory: String,
        imageUrl: String,
        priceStatus: String
    ): AnimalItem? = withContext(Dispatchers.IO) {
        val jsonObj = JSONObject()
            .put("name", name)
            .put("species", species)
            .put("breed", breed)
            .put("age", age)
            .put("gender", gender)
            .put("size", "متوسط")
            .put("description", description)
            .put("image_url", imageUrl)
            .put("likes_count", 0)
            .put("special_needs", false)
            .put("vaccinated", true)
            .put("neutered", false)
            .put("compatibility", "متوافق مع الجميع")
            .put("backstory", backstory)
            .put("price_status", priceStatus)
        
        val requestBody = jsonObj.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url("$BASE_URL/animals")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Prefer", "return=representation")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "POST animal failed: ${response.code}")
                    AnimalItem(
                        id = (1000..9999).random().toString(),
                        name = name,
                        species = species,
                        breed = breed,
                        age = age,
                        gender = gender,
                        size = "متوسط",
                        description = description,
                        imageUrl = imageUrl,
                        likesCount = 0,
                        specialNeeds = false,
                        vaccinated = true,
                        neutered = false,
                        compatibility = "متوافق مع الجميع",
                        backstory = backstory,
                        priceStatus = priceStatus
                    )
                } else {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "POST animal success! Response: $responseBody")
                    AnimalItem(
                        id = (1000..9999).random().toString(),
                        name = name,
                        species = species,
                        breed = breed,
                        age = age,
                        gender = gender,
                        size = "متوسط",
                        description = description,
                        imageUrl = imageUrl,
                        likesCount = 0,
                        specialNeeds = false,
                        vaccinated = true,
                        neutered = false,
                        compatibility = "متوافق مع الجميع",
                        backstory = backstory,
                        priceStatus = priceStatus
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "POST error, returning locally simulated animal", e)
            AnimalItem(
                id = (1000..9999).random().toString(),
                name = name,
                species = species,
                breed = breed,
                age = age,
                gender = gender,
                size = "متوسط",
                description = description,
                imageUrl = imageUrl,
                likesCount = 0,
                specialNeeds = false,
                vaccinated = true,
                neutered = false,
                compatibility = "متوافق مع الجميع",
                backstory = backstory,
                priceStatus = priceStatus
            )
        }
    }

    suspend fun incrementLike(animalId: String, currentLikes: Int): Int = withContext(Dispatchers.IO) {
        val newLikes = currentLikes + 1
        val jsonPayload = JSONObject().put("likes_count", newLikes).toString()
        val requestBody = jsonPayload.toRequestBody(mediaTypeJson)

        val request = Request.Builder()
            .url("$BASE_URL/animals?id=eq.$animalId")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Prefer", "resolution=merge-duplicates")
            .patch(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "PATCH like failed: ${response.code}")
                    newLikes
                } else {
                    Log.d(TAG, "PATCH like success for id $animalId")
                    newLikes
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "PATCH error, returning locally simulated increment", e)
            newLikes
        }
    }

    suspend fun getComments(animalId: String): List<CommentItem> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$BASE_URL/animal_comments?animal_id=eq.$animalId&order=created_at.desc")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Fetch comments error: ${response.code}")
                    return@withContext fallbackComments.filter { it.animalId == animalId }
                }
                val bodyStr = response.body?.string() ?: return@withContext emptyList<CommentItem>()
                val jsonArray = JSONArray(bodyStr)
                val list = mutableListOf<CommentItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        CommentItem(
                            id = obj.optString("id", i.toString()),
                            animalId = obj.optString("animal_id", animalId),
                            author = obj.optString("author_name", obj.optString("author", "متبرع مجهول")),
                            text = obj.optString("comment_text", obj.optString("content", "")),
                            createdAt = "الآن"
                        )
                    )
                }
                if (list.isEmpty()) fallbackComments.filter { it.animalId == animalId } else list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Comments HTTP Error, using fallback comments.", e)
            fallbackComments.filter { it.animalId == animalId }
        }
    }

    suspend fun postComment(animalId: String, authorName: String, text: String): CommentItem? = withContext(Dispatchers.IO) {
        val jsonObj = JSONObject()
            .put("animal_id", animalId)
            .put("author_name", authorName)
            .put("comment_text", text)
        
        val requestBody = jsonObj.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url("$BASE_URL/animal_comments")
            .header("apikey", API_KEY)
            .header("Authorization", "Bearer $API_KEY")
            .header("Prefer", "return=representation")
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "POST comment failed: ${response.code}")
                    null
                } else {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "POST comment success! Response: $responseBody")
                    CommentItem(
                        id = (1000..9999).random().toString(),
                        animalId = animalId,
                        author = authorName,
                        text = text,
                        createdAt = "الآن"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "POST error, returning locally simulated comment", e)
            CommentItem(
                id = (1000..9999).random().toString(),
                animalId = animalId,
                author = authorName,
                text = text,
                createdAt = "الآن"
            )
        }
    }

    // Server-Driven UI mock fetcher (can be hooked to a real Supabase endpoint later)
    suspend fun fetchDynamicUI(): String = withContext(Dispatchers.IO) {
        // Mock JSON representing a dynamic promotional banner
        """
        {
          "type": "Card",
          "elevationDp": 4.0,
          "cornerRadiusDp": 16.0,
          "backgroundColorHex": "#FFF3E0",
          "child": {
            "type": "Column",
            "paddingDp": 16.0,
            "spacingDp": 12.0,
            "children": [
              {
                "type": "Image",
                "url": "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?w=600",
                "heightDp": 140.0,
                "cornerRadiusDp": 12.0
              },
              {
                "type": "Text",
                "text": "حملة التبرع العاجلة 🐾",
                "sizeSp": 18.0,
                "colorHex": "#E65100",
                "isBold": true
              },
              {
                "type": "Text",
                "text": "ساعدنا في إنقاذ أكثر من 50 حيواناً بلا مأوى هذا الشتاء. تبرعك يصنع الفرق!",
                "sizeSp": 14.0,
                "colorHex": "#546E7A",
                "isBold": false
              },
              {
                "type": "Button",
                "text": "تبرع الآن",
                "colorHex": "#00897B",
                "actionUrl": "donate_campaign_1"
              }
            ]
          }
        }
        """.trimIndent()
    }
}
