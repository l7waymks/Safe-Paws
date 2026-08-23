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
    private const val PRIMARY_MODEL = "gemini-2.5-flash"
    private val MODELS = listOf(
        "gemini-2.5-flash",
        "gemini-flash-latest",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.1-pro-preview"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        // 1. Direct compile-time access via BuildConfig
        try {
            val key = BuildConfig.GEMINI_API_KEY.trim().replace("\"", "").replace("'", "")
            if (isApiKeyValid(key)) return key
        } catch (_: Throwable) {}

        // 2. Reflection fallback
        try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val key = ((field.get(null) as? String) ?: "").trim().replace("\"", "").replace("'", "")
            if (isApiKeyValid(key)) return key
        } catch (_: Throwable) {}

        return ""
    }

    private fun isApiKeyValid(key: String): Boolean {
        val clean = key.trim().replace("\"", "").replace("'", "")
        return clean.isNotBlank() && 
               clean != "MY_GEMINI_API_KEY" && 
               !clean.contains("API_KEY_DEFAULT") && 
               clean.length >= 10
    }

    /**
     * Sends chat history to Gemini API for expert veterinary replies.
     * Fallbacks to rich clinical local knowledge engine if API is unavailable or unconfigured.
     */
    suspend fun getExpertReply(history: List<Pair<String, Boolean>>): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val lastUserMessage = history.lastOrNull { it.second }?.first ?: "مرحباً"

        if (isApiKeyValid(apiKey)) {
            // Cascade through verified supported models
            for (model in MODELS) {
                val apiReply = callGeminiChatApi(apiKey, model, history)
                if (!apiReply.isNullOrBlank()) {
                    return@withContext apiReply
                }
            }
        }

        // Deploy comprehensive offline medical knowledge engine
        return@withContext getLocalExpertReply(lastUserMessage)
    }

    private fun callGeminiChatApi(apiKey: String, model: String, history: List<Pair<String, Boolean>>): String? {
        try {
            val validHistory = mutableListOf<Pair<String, String>>()
            val firstUserIndex = history.indexOfFirst { it.second }

            if (firstUserIndex != -1) {
                for (i in firstUserIndex until history.size) {
                    val item = history[i]
                    val role = if (item.second) "user" else "model"
                    val text = item.first.trim()
                    if (text.isEmpty()) continue

                    if (validHistory.isNotEmpty() && validHistory.last().first == role) {
                        val prev = validHistory.removeAt(validHistory.lastIndex)
                        validHistory.add(Pair(role, "${prev.second}\n$text"))
                    } else {
                        validHistory.add(Pair(role, text))
                    }
                }
            } else {
                val lastMsg = history.lastOrNull()?.first ?: "مرحباً"
                validHistory.add(Pair("user", lastMsg))
            }

            if (validHistory.isEmpty()) return null

            val contentsArray = JSONArray()
            for (msg in validHistory) {
                val partObj = JSONObject().put("text", msg.second)
                val partsArray = JSONArray().put(partObj)
                val contentObj = JSONObject()
                    .put("role", msg.first)
                    .put("parts", partsArray)
                contentsArray.put(contentObj)
            }

            val systemInstruction = "أنت المستشار الطبي والبيطري الذكي في منصة (بيت الأمان - Safe Paws). " +
                    "مهمتك الإجابة بذكاء ودقة واختصار على سؤال المستخدم المحدد فقط، دون إطالة مفرطة أو سرد نصوص ضخمة لا علاقة لها بالسؤال. " +
                    "اجعل إجابتك مباشرة ومطابقة لما سأل عنه بالضبط باللغة العربية الفصحى:\n" +
                    "- أجب مباشرة عن السؤال المطلوب في نقاط واضحة أو فقرة مركزة.\n" +
                    "- ركز على الحل العملي، الإسعاف المباشر، أو النصيحة المحددة لسؤاله.\n" +
                    "- تجنب الإطالة الزائدة أو الحشو واجعل الرد سهلاً وسريع القراءة ومفيداً للغاية."

            val requestJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemInstruction))))

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
            val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorStr = response.body?.string() ?: ""
                    Log.w(TAG, "Gemini $model call error ${response.code}: $errorStr")
                    return null
                }
                val bodyStr = response.body?.string() ?: return null
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "")

                return if (!text.isNullOrBlank()) text.trim() else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception calling Gemini API with model $model", e)
            return null
        }
    }

    /**
     * Determines matching percentage and details for PetMatch AI.
     */
    suspend fun getPetMatchAnalysis(
        answers: List<String>,
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
                "Evaluate compatibility for adoption of pet '$petName' ($petBreed) described as '$petBio'.\n" +
                "Format EXACTLY as JSON: {\"percentage\": 88, \"reason\": \"Detailed helpful compatibility report in Arabic.\"}"

        if (isApiKeyValid(apiKey)) {
            try {
                val partObj = JSONObject().put("text", prompt)
                val partsArray = JSONArray().put(partObj)
                val contentObj = JSONObject().put("role", "user").put("parts", partsArray)
                val contentsArray = JSONArray().put(contentObj)

                val requestJson = JSONObject()
                    .put("contents", contentsArray)
                    .put("generationConfig", JSONObject().put("responseFormat", JSONObject().put("type", "application/json")))

                val url = "https://generativelanguage.googleapis.com/v1beta/models/$PRIMARY_MODEL:generateContent?key=$apiKey"
                val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
                val request = Request.Builder().url(url).post(requestBody).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val responseJson = JSONObject(bodyStr)
                            val rawText = responseJson.optJSONArray("candidates")
                                ?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text", "{}") ?: "{}"
                            val parsed = JSONObject(rawText)
                            val percentage = parsed.optInt("percentage", (80..95).random())
                            val reason = parsed.optString("reason", "")
                            if (reason.isNotBlank()) {
                                return@withContext Pair(percentage, reason)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in PetMatch Gemini call", e)
            }
        }

        return@withContext getLocalMatchAnalysis(answers, petName, petBreed)
    }

    /**
     * Concise & Direct Veterinary Medical Knowledge Engine
     * Directly answers user questions without overwhelming text.
     */
    fun getLocalExpertReply(question: String): String {
        val q = question.lowercase()

        return when {
            // 0. التحية والترحيب والأسئلة العامة
            q.contains("سلام") || q.contains("مرحب") || q.contains("أهل") || q.contains("اهل") || q.contains("هلا") || q.contains("صباح") || q.contains("مساء") || q.contains("من انت") || q.contains("مين انت") || q.contains("من أنت") || q == "hi" || q == "hello" ->
                "👋 **أهلاً بك في منصة بيت الأمان (Safe Paws)!**\n" +
                "أنا المستشار البيطري الذكي. أنا هنا لمساعدتك في كل ما يخص صحة ورعاية أليفك:\n" +
                "• الاستشارات الطبية والإسعافات الأولية السريعة 🩺\n" +
                "• نصائح التغذية والجداول الوقائية والتطعيمات 💉\n" +
                "• حلول مشاكل السلوك والتدريب والعناية اليومية 🐾\n" +
                "كيف يمكنني مساعدتك اليوم؟ تفضل بطرح سؤالك!"

            // 1. ارتفاع الحرارة والحمى
            q.contains("حرار") || q.contains("سخون") || q.contains("حمى") || (q.contains("ارتفاع") && q.contains("درجة")) ->
                "🌡️ **ارتفاع حرارة الأليف (الحمى):**\n" +
                "• **المعدل الطبيعي:** بين 38.0 إلى 39.2 درجة مئوية.\n" +
                "• **الإسعاف الفوري:** كمادات ماء فاتر (غير بارد) على باطن الكفوف وأسفل البطن، وتوفير ماء شرب نظيف.\n" +
                "• ⚠️ **تحذير حاسم:** لا تعطِ أليفك بنادول أو باراسيتامول نهائياً لأنه سام وقاتل له.\n" +
                "• راجع العيادة إذا تجاوزت 39.5°م أو صاحبها خمول وامتناع عن الأكل."

            // 2. القيء والاستفراغ
            q.contains("قيء") || q.contains("استفراغ") || q.contains("ترجيع") || q.contains("يستفرغ") || q.contains("تستفرغ") || q.contains("يرجع") ->
                "🩺 **علاج القيء والاستفراغ:**\n" +
                "1. **إراحة المعدة:** امنع الطعام لمدة 6 ساعات مع إبقاء رشفات ماء قليلة.\n" +
                "2. **وجبة خفيفة:** قدم دجاج مسلوق مهروس مع أرز أبيض مسلوق بدون ملح أو بهارات.\n" +
                "3. **تنبيه:** إذا تكرر القيء أكثر من مرتين أو احتوى على دم أو رغوة صفراء، توجه للعيادة فوراً."

            // 3. الإسهال ومشاكل الإخراج
            q.contains("إسهال") || q.contains("اسهال") || q.contains("براز مائي") || q.contains("ليونة") ->
                "🩹 **التعامل مع الإسهال:**\n" +
                "• حافظ على شرب الماء لمنع الجفاف.\n" +
                "• قدم طعاماً مسلوقاً (أرز أبيض مع دجاج مسلوق أو يقطين مهروس).\n" +
                "• امنع الحليب ومشتقاته والأطعمة الدسمة تماماً.\n" +
                "• إذا استمر لأكثر من 24 ساعة يفضل فحص عينة براز بالعيادة لطرد الطفيليات أو الديدان."

            // 4. شرب الماء والجفاف ومشاكل البول
            q.contains("ماء") || q.contains("شرب") || q.contains("عطش") || q.contains("جفاف") || q.contains("كلى") || q.contains("مسالك") ->
                "💧 **شرب الماء وصحة المسالك البولية:**\n" +
                "• القطط تفضل المياه الجارية، لذا يوصى باستخدام نافورة مياه خاصة بالحيوانات.\n" +
                "• احرص على زيادة نسبة الطعام الرطب (Wet Food) لترطيب الكلى.\n" +
                "• في حال صعوبة التبول أو حبس البول تماماً، هذه حالة طارئة تستدعي التوجه للعيادة فوراً."

            // 5. فقدان الشهية والخمول
            q.contains("شهي") || q.contains("لا تأكل") || q.contains("لا ياكل") || q.contains("ما ياكل") || q.contains("ما تاكل") || q.contains("خمول") || q.contains("تعبان") || q.contains("مريض") ->
                "🥣 **تحفيز الأليف الفاقد للشهية:**\n" +
                "• قم بتدفئة الطعام الرطب قليلاً لزيادة رائحته وجاذبيته.\n" +
                "• قدم مرق دجاج مسلوق دافئ وخالٍ تماماً من البصل والملح.\n" +
                "• **تنبيه خاص بالقطط:** انقطاع القطة عن الأكل لأكثر من يومين يضر الكبد ويستلزم فحصاً طبياً سريعاً."

            // 6. التطعيمات والتحصينات
            q.contains("تطعيم") || q.contains("لقاح") || q.contains("تلقيح") || q.contains("تحصين") || q.contains("جرع") ->
                "💉 **جدول التطعيمات الأساسي:**\n" +
                "• **القطط:** التطعيم الرباعي في عمر شهرين ثم جرعة منشطة بعد شهر، وتطعيم السعار في عمر 3 أشهر (ويكرر سنوياً).\n" +
                "• **الكلاب:** التطعيم الثماني في عمر شهر ونصف ثم الجرعة التنشيطية والسعار (ويكرر سنوياً).\n" +
                "• **الديدان والحشرات:** حبة ديدان وأمبولة براغيث كل 3 أشهر."

            // 7. مشاكل الجلد، الحكة، الفطريات، تساقط الشعر
            q.contains("حك") || q.contains("شعر") || q.contains("تساقط") || q.contains("فطر") || q.contains("قشر") || q.contains("جرب") || q.contains("صلع") ->
                "🐾 **علاج الحكة والفطريات وتساقط الشعر:**\n" +
                "• طهر البقع الحمراء أو الخالية من الشعر ببيتادين مخفف (1 بيتادين إلى 10 ماء).\n" +
                "• أضف زيت أوميغا 3 أو زيت السلمون لوجباته لتقوية الفرو.\n" +
                "• الفطريات (Ringworm) معدية؛ اعزل الأليف واغسل يديك بعد ملامسته."

            // 8. البراغيث والقراد والحشرات
            q.contains("برغوث") || q.contains("براغيث") || q.contains("قراد") || q.contains("حشرات") || q.contains("دود") || q.contains("طفيلي") ->
                "🐛 **مكافحة البراغيث والحشرات:**\n" +
                "• ضع أمبولة موضعية مخصصة (مثل Revolution أو Advantage) خلف الرقبة حسب وزن أليفك.\n" +
                "• مشط الفرو بمشط براغيث دقيق واغسل المفارش بماء ساخن."

            // 9. العيون والأنف ومشاكل التنفس
            q.contains("عين") || q.contains("عيون") || q.contains("افراز") || q.contains("إفراز") || q.contains("عطس") || q.contains("تنفس") || q.contains("كحة") || q.contains("سعال") ->
                "👀 **تنظيف العيون والعطاس:**\n" +
                "• امسح إفرازات العين والأنف بمحلول ملحي معقم (Saline) وقطنة نظيفة.\n" +
                "• في حال الزكام والعطاس، أدخل الأليف بحمام دافئ يستنشق البخار لمدة 10 دقائق لتسليك المجاري التنفسية."

            // 10. الأسنان ورائحة الفم
            q.contains("سن") || q.contains("أسنان") || q.contains("اسنان") || q.contains("فم") || q.contains("لثة") || q.contains("رائحة") ->
                "🦷 **صحة الأسنان والفم:**\n" +
                "• استخدم معجون أسنان مخصص للحيوانات (لا تستخدم المعجون البشري أبداً).\n" +
                "• احمرار اللثة أو الرائحة الكريهة قد تدل على جير متراكم أو التهاب يستدعي تنظيفاً بيطرياً."

            // 11. التعقيم والخصي والتزاوج
            q.contains("تعقيم") || q.contains("خصي") || q.contains("تزاوج") || q.contains("طلب") || q.contains("شبق") || q.contains("عملية") ->
                "⚕️ **التعقيم والخصي والتزاوج:**\n" +
                "• العمر المثالي للتعقيم هو بين عمر 5 إلى 6 أشهر.\n" +
                "• التعقيم يحمي الإناث من أورام الرحم والثدي ويمنع الذكور من الرش والهروب والعدوانية."

            // 12. الاستحمام وقص الأظافر والنظافة
            q.contains("استحمام") || q.contains("حمام") || q.contains("ترويش") || q.contains("أظافر") || q.contains("قص") || q.contains("أذن") || q.contains("اذن") ->
                "🛁 **العناية والنظافة:**\n" +
                "• القطط تنظف نفسها ولا تحتاج للاستحمام إلا عند الاتساخ الشديد (استخدم شامبو مخصص).\n" +
                "• قص فقط الطرف الأبيض الشفاف من الظفر وتجنب الوصول للجزء الوردي (الشريان المغذي).\n" +
                "• نظف صيوان الأذن بقطنة مبللة بمحلول الأذن وتجنب إدخال الأعواد داخل القناة السمعية."

            // 13. الخوف والقلق والاكتئاب
            q.contains("خوف") || q.contains("خائف") || q.contains("قلق") || q.contains("اكتئاب") || q.contains("اختباء") || q.contains("حزن") || q.contains("صدمة") ->
                "🛋️ **التعامل مع الخوف والتوتر:**\n" +
                "• وفر للأليف مكاناً آمناً ومرتفعاً يختبئ فيه دون إجباره على الخروج.\n" +
                "• استخدم روائح مهدئة طبيعية (مثل Feliway للقطط أو Adaptil للكلاب).\n" +
                "• تحدث معه بنبرة صوت هادئة وقدم له مكافأة لذيذة عند استرخائه."

            // 14. الأطعمة الممنوعة والسامة
            q.contains("ممنوع") || q.contains("سام") || q.contains("شوكولات") || q.contains("بصل") || q.contains("ثوم") || q.contains("عنب") || q.contains("أكل ممنوع") ->
                "🚫 **أخطر الأطعمة السامة للأليف:**\n" +
                "1. **الشوكولاتة والكافيين** (تسمم قلبي وعصبي).\n" +
                "2. **البصل والثوم** (يدمر كريات الدم الحمراء).\n" +
                "3. **العنب والزبيب** (يسبب فشلاً كلوياً فورياً).\n" +
                "4. **العظام المطبوخة والحليب البقري** (انسداد معوي ومغص حاد)."

            // 15. التغذية والرضاعة للجراء والقطط الصغيرة
            q.contains("رضيع") || q.contains("صغير") || q.contains("حليب") || q.contains("ولادة") || q.contains("غذاء") || q.contains("طعام") || q.contains("اكل") || q.contains("وجب") ->
                "🍼 **تغذية الصغار والرضع:**\n" +
                "• استخدم حليب بديل مخصص (KMR للقطط / Esbilac للكلاب) برضاعة خاصة كل 3 ساعات مع تدفئة جيدة.\n" +
                "• بعد عمر 4 أسابيع، ابدأ بإدخال الطعام الرطب المهروس تدريجياً."

            // 16. الطوارئ، الجروح، الكسور والنزيف
            q.contains("جرح") || q.contains("نزيف") || q.contains("كسر") || q.contains("عرج") || q.contains("حادث") || q.contains("طوارئ") || q.contains("دم") || q.contains("بلع") ->
                "🚨 **إسعافات الطوارئ السريعة:**\n" +
                "• **للنزيف:** اضغط بشاش معقم نظيف مباشرة على الجرح لمدة 3 دقائق.\n" +
                "• **للكسور والعرج:** ضع الأليف بصندوق مبطن بهدوء ولا تحاول تحريك العظم المصاب وتوجه للطوارئ البيطرية."

            // 17. الأرانب والطيور والقوارض
            q.contains("أرنب") || q.contains("ارنب") || q.contains("طير") || q.contains("عصفور") || q.contains("ببغاء") || q.contains("هامستر") ->
                "🐰 **رعاية الأرانب والطيور:**\n" +
                "• **الأرانب:** دريس الحشائش (Timothy Hay) يمثل 80% من غذائها اليومي لصحة الأمعاء والأسنان.\n" +
                "• **الطيور:** احذر تماماً من روائح أواني التيفال الساخنة والبخور والمعطرات لأنها سامة وقاتلة لرئتيها."

            // 18. السلوك، التبول، المواء والتربية
            q.contains("تبول") || q.contains("رمل") || q.contains("مواء") || q.contains("عواء") || q.contains("نباح") || q.contains("سلوك") || q.contains("عدوان") || q.contains("عض") || q.contains("تدريب") ->
                "🐾 **حلول السلوك وصندوق الرمل:**\n" +
                "• نظف صندوق الرمل يومياً وتأكد من عدم وجود التهاب مسالك بولية إذا كان يتبول خارجه.\n" +
                "• اعتمد على أسلوب المكافآت والتدريب الإيجابي وتجنب الصراخ أو العقاب البدني."

            // 19. التبني، الإجراءات والشروط
            q.contains("تبني") || q.contains("شروط") || q.contains("خطوات") || q.contains("عقد") || q.contains("استمارة") ->
                "🏡 **شروط وخطوات التبني في بيت الأمان:**\n" +
                "• التبني **مجاني 100%** لضمان منزل آمن ومحب للأليف.\n" +
                "• الخطوات: اختيار الأليف، تعبئة استبيان التوافق، المقابلة البسيطة، وتوقيع العقد الرقمي للاستلام."

            // 20. تحليل مباشر لأي استفسار آخر
            else ->
                "🩺 **إجابة المستشار الطبي:**\n" +
                "أهلاً بك! يمكنك إخباري بالعَرَض أو المشكلة المحددة التي تواجه أليفك (مثل: قيء، سخونة، تطعيم، نوع أكل) وسأعطيك الحل المباشر والإسعاف الدقيق المناسب فوراً."
        }
    }

    private fun getLocalMatchAnalysis(answers: List<String>, petName: String, petBreed: String): Pair<Int, String> {
        val isApartment = answers.getOrNull(2)?.contains("شقة") ?: true
        val isActive = answers.getOrNull(0)?.contains("نشط") ?: true
        val hasKids = answers.getOrNull(3)?.contains("نعم") ?: false

        var score = 85
        val reasonBuilder = StringBuilder()

        if (isApartment && petBreed.contains("ريتريفر")) {
            score -= 5
            reasonBuilder.append("نمط السكن في شقة مناسب، ولكن يوصى بجولة مشي يومية في حديقة عامة لتفريغ طاقة '$petName' كـ '$petBreed' نشيط. ")
        } else {
            score += 6
            reasonBuilder.append("المسكن ونمط الحياة متوافقان تماماً مع احتياجات الأليف '$petName'. ")
        }

        if (isActive) {
            score += 5
            reasonBuilder.append("مستوى نشاطك الممتاز يضمن قضاء أوقات مرحة وتدريبات إيجابية مستمرة. ")
        } else {
            reasonBuilder.append("طبيعتك الهادئة تساعد الأليف على الاسترخاء والأمان المنزلي. ")
        }

        if (hasKids) {
            score += 4
            reasonBuilder.append("وجود الأطفال في المنزل رائع لتنمية الروابط الاجتماعية ومحبة الأليف.")
        }

        val finalScore = score.coerceIn(75, 98)
        return Pair(finalScore, reasonBuilder.toString())
    }
}

