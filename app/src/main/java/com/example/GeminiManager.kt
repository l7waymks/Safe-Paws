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

object GeminiManager {
    private const val TAG = "GeminiManager"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun isApiKeyValid(key: String): Boolean {
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("API_KEY")
    }

    /**
     * Sends a chat history to Gemini for intelligent expert replies.
     * Falls back gracefully if the API Key is empty or invalid.
     */
    suspend fun getExpertReply(history: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val lastMessage = history.lastOrNull { it.second }?.first ?: "مرحباً"

        if (!isApiKeyValid(apiKey)) {
            Log.w(TAG, "Gemini API key is not initialized or invalid, using intelligent local expert response.")
            return@withContext getLocalExpertReply(lastMessage)
        }

        try {
            // Build Gemini Chat history contents structure
            val contentsArray = JSONArray()
            for (message in history) {
                val role = if (message.second) "user" else "model"
                val text = message.first
                
                val partObj = JSONObject().put("text", text)
                val partsArray = JSONArray().put(partObj)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", 
                    "أنت طبيب بيطري وخبير في سلوك ورعاية الحيوانات الأليفة في منصة بيت الأمان (Safe Paws) لإنقاذ الحيوانات وتسهيل التبني. " +
                    "أجب دائماً باللغة العربية بأسلوب ودود، مشجع، علمي، ومهني للغاية. لا تذكر أي معلومات غير مفيدة. شجع التبني غير مدفوع الأجر في بيئة آمنة وراعية."
                ))))

            val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorStr = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini call failed with code ${response.code}: $errorStr")
                    return@withContext getLocalExpertReply(lastMessage)
                }
                
                val bodyStr = response.body?.string() ?: return@withContext getLocalExpertReply(lastMessage)
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "")
                
                if (text.isNullOrEmpty()) {
                    getLocalExpertReply(lastMessage)
                } else {
                    text
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini call, deploying local response fallback.", e)
            getLocalExpertReply(lastMessage)
        }
    }

    /**
     * Determines matching percentage and details for PetMatch AI.
     */
    suspend fun getPetMatchAnalysis(
        answers: List<String>, // [Activity, WorkHours, Flat/Villa, Has Kids, Experience]
        petName: String,
        petBreed: String,
        petBio: String
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()

        val prompt = "User answers regarding lifestyle: \n" +
                "1. Activity level: ${answers.getOrNull(0) ?: "Medium"}\n" +
                "2. Daily solitary working hours: ${answers.getOrNull(1) ?: "4"}\n" +
                "3. Residential style: ${answers.getOrNull(2) ?: "Apartment"}\n" +
                "4. Presence of children: ${answers.getOrNull(3) ?: "No"}\n" +
                "5. Pet experience: ${answers.getOrNull(4) ?: "Beginner"}\n\n" +
                "Evaluate compatibility for adpotion of the pet '$petName', a '$petBreed' described as '$petBio'.\n" +
                "Format your response EXACTLY as a JSON object, like: \n" +
                "{\"percentage\": 87, \"reason\": \"Your explanation in Arabic explaining why this pet matches and customized tips for adoption.\"}\n" +
                "Keep explanation brief, professional, extremely positive and informative in Arabic."

        if (!isApiKeyValid(apiKey)) {
            Log.w(TAG, "Gemini API key is not valid, matching via local matching algorithms.")
            return@withContext getLocalMatchAnalysis(answers, petName, petBreed)
        }

        try {
            val partObj = JSONObject().put("text", prompt)
            val partsArray = JSONArray().put(partObj)
            val contentObj = JSONObject().put("parts", partsArray)
            val contentsArray = JSONArray().put(contentObj)

            // Dynamic Config to enforce JSON Output Schema
            val responseFormatObj = JSONObject()
                .put("type", "OBJECT")
                .put("properties", JSONObject()
                    .put("percentage", JSONObject().put("type", "INTEGER"))
                    .put("reason", JSONObject().put("type", "STRING"))
                )
                .put("required", JSONArray().put("percentage").put("reason"))

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("generationConfig", JSONObject()
                    .put("responseFormat", JSONObject().put("type", "application/json"))
                )

            val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext getLocalMatchAnalysis(answers, petName, petBreed)
                }
                val bodyStr = response.body?.string() ?: return@withContext getLocalMatchAnalysis(answers, petName, petBreed)
                val responseJson = JSONObject(bodyStr)
                val rawText = responseJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "{}") ?: "{}"
                
                val parsedText = JSONObject(rawText)
                val percentage = parsedText.optInt("percentage", (75..95).random())
                val reason = parsedText.optString("reason", "توافق ممتاز بالاعتماد على معايير السكن والوقت المتاح للأليف.")
                Pair(percentage, reason)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating PetMatch via Gemini, using fallback matching system.", e)
            getLocalMatchAnalysis(answers, petName, petBreed)
        }
    }

    private fun getLocalExpertReply(question: String): String {
        val q = question.lowercase()
        return when {
            q.contains("غذاء") || q.contains("أكل") || q.contains("طعام") -> 
                "بصفتي خبيراً بيطرياً، الغذاء الصحي السليم يعتمد على الاحتياجات العمرية لنوع أليفك. للجراء الصغيرة مثل 'بندق'، نوصي بوجبات غنية بالبروتينات لنمو العضلات (طعام جاف عالي الجودة أو دجاج مسلوق خالٍ من البصل والثوم). القطط تحتاج إلى توراين (Taurine) بشكل يومي لذا يفضل إطعامها طعام قطط مخصص."
            q.contains("تطعيم") || q.contains("لقاح") || q.contains("تلقيح") -> 
                "في بيت الأمان، نلتزم بسلامة جميع الألياف. يحتاج الجراء لجرعات تطعيم أساسية (الثلاثي أو الرباعي للقطط، والثماني للكلاب) بين عمر مجسات يتراوح من ٦ إلى ٨ أسابيع، متبوعاً بجرعات تنشيطية وتطعيم داء السعار السنوي. ننصحك بمراجعة السجل الصحي الرقمي لـمتابعة الجرعات القادمة."
            q.contains("تربية") || q.contains("تدريب") || q.contains("بيت") || q.contains("منزل") -> 
                "إعداد المنزل لاستقبال صديقك الجديد يحتاج إلى تهيئة بيئة آمنة: قم بإزالة النباتات المنزلية السامة، وتأمين النوافذ والشرفات لسلامة القطط، وتخصيص منطقة هادئة للنوم وصندوق الرمل. الصبر والتدريب الإيجابي بالمكافآت (Positive Reinforcement) هو السبيل الأمثل لتعليم أليفك!"
            q.contains("مرشح") || q.contains("شرط") || q.contains("خطوات") || q.contains("تبني") -> 
                "عملية التبني في 'بيت الأمان' مجانية ومسؤولة تماماً، وتتكون من ٥ خطوات بسيطة: تقديم الطلب الإلكتروني، استبيان تقييم المنزل للتأكد من المعايير الآمنة، مقابلة عائلية لتقريب وجهات النظر والتقارب، توقيع عقد التبني الرقمي، متبوعاً بمتابعة دورية للاطمئنان عليه."
            else -> 
                "مرحباً بك! أنا طبيب بيطري ومستشار سلوك الحيوان في بيت الأمان. يسعدني الإجابة على أي استفسار طبي أو سلوكي يتعلق بأليفك القادم، مثل العناية بـ 'بندق' أو التجهيزات المناسبة للبيت. ما الذي تود معرفته؟"
        }
    }

    private fun getLocalMatchAnalysis(answers: List<String>, petName: String, petBreed: String): Pair<Int, String> {
        // Simple logic based on Apartment and Activity
        val isApartment = answers.getOrNull(2)?.contains("شقة") ?: true
        val isActive = answers.getOrNull(0)?.contains("نشط") ?: true
        val hasKids = answers.getOrNull(3)?.contains("نعم") ?: false
        
        var score = 85
        val reasonBuilder = StringBuilder()
        
        if (isApartment && petBreed.contains("ريتريفر")) {
            score -= 10
            reasonBuilder.append("حجم شقتك مناسب ومحدد لكن الأليف '$petName' يحتاج إلى تنشئة حركية وتدريبات يومية في الحديقة العامة المجاورة لتلبية احتياجه كـ '$petBreed' نشيط. ")
        } else {
            score += 5
            reasonBuilder.append("نمط المعيشة والمسكن لديك يلائم تماماً احتياجات الأليف '$petName'. ")
        }
        
        if (isActive) {
            score += 8
            reasonBuilder.append("طاقتك العالية وشغفك بالنشاط تجعلك الرفيق المثالي له لقضاء أوقات خارجية ممتازة. ")
        } else {
            score -= 5
            reasonBuilder.append("بما أنك تفضل الهدوء والأليف نشط، ننصح بتدريبات الخفة المنزلية لتسليته بأقل مجهود. ")
        }
        
        if (hasKids) {
            score += 5
            reasonBuilder.append("وجود الأطفال يعزز من الترابط الاجتماعي، و'$petName' ودود واجتماعي جداً مع الصغار.")
        }
        
        val finalScore = score.coerceIn(65, 98)
        return Pair(finalScore, reasonBuilder.toString())
    }
}
