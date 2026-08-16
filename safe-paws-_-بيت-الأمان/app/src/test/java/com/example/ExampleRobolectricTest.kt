package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import okhttp3.OkHttpClient
import okhttp3.Request

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Safe Paws / بيت الأمان", appName)
  }

  @Test
  fun importWebPointsToSupabase() {
    val client = okhttp3.OkHttpClient()
    val supabaseUrl = "https://rxclrwcwhbvnldmguxko.supabase.co"
    val apiKey = "sb_publishable_7ECYLHs8ZeuE40g2El1KiQ_Ghhg79Uf"
    
    // 1. Delete existing rows first to start clean
    val deleteUrl = "$supabaseUrl/rest/v1/markers?id=gte.0"
    val deleteRequest = okhttp3.Request.Builder()
        .url(deleteUrl)
        .header("apikey", apiKey)
        .header("Authorization", "Bearer $apiKey")
        .delete()
        .build()
        
    try {
        client.newCall(deleteRequest).execute().use { resp ->
            println("Delete previous response: ${resp.code}")
        }
    } catch (e: Exception) {
        println("Error deleting: ${e.message}")
    }
    
    // 2. Prepare the 18 points from mapsafe-paws.netlify.app/viewer.html
    val bulkMarkersJson = """
    [
      {
        "id": 1,
        "title": "عيادة الأليف المتقدمة البيطرية 🏥",
        "description": "عيادة بيطرية متكاملة لتقديم الاستشارات والعلاجات والتطعيمات للحيوانات الأليفة بالرياض.",
        "lat": 24.7082,
        "lng": 46.6815
      },
      {
        "id": 2,
        "title": "متجر أليفي لملحقات الحيوانات 🛒",
        "description": "متجر رائد يوفر الأغذية الفاخرة، الألعاب، والاكسسوارات المخصصة للكلاب والقطط.",
        "lat": 24.7725,
        "lng": 46.6391
      },
      {
        "id": 3,
        "title": "ملجأ الرياض لتبني القطط والكلاب 🐶",
        "description": "ملجأ رعاية إنسانية للحيوانات الأليفة المشردة، يوفر الرعاية الطبية ويبحث عن منازل حنونة للتبني.",
        "lat": 24.8211,
        "lng": 46.7032
      },
      {
        "id": 4,
        "title": "صالون وسبا المخالب اللطيفة ✂️",
        "description": "قص، استحمام، العناية بالأظافر والوبر بأيدي متخصصين لتدليل قطتكم وكلبكم اللطيف.",
        "lat": 24.7431,
        "lng": 46.6548
      },
      {
        "id": 5,
        "title": "فندق أليف للرعاية الفندقية 🐾",
        "description": "فندق وبيت ضيافة آمن للحيوانات الأليفة أثناء سفركم، رعاية تامة ٢٤ ساعة مع كاميرات لمراقبة أليفكم.",
        "lat": 24.7895,
        "lng": 46.6124
      },
      {
        "id": 6,
        "title": "مستشفى الرياض البيطري الدولي 🏥",
        "description": "طوارئ وجراحة على مدار الساعة، أشعة، تحاليل مخبرية، وعناية مركزة للحالات الحرجة.",
        "lat": 24.6948,
        "lng": 46.7215
      },
      {
        "id": 7,
        "title": "حديقة الأليف السعيدة للتدريب 🎓",
        "description": "منطقة مخصصة للعب الحر لحيواناتكم الأليفة وتدريب الكلاب على الطاعة وتعديل السلوك.",
        "lat": 24.8055,
        "lng": 46.6712
      },
      {
        "id": 8,
        "title": "نقطة إطعام القطط المشردة - العليا 🐈",
        "description": "مبادرة مجتمعية تطوعية مخصصة لتزويد القطط الضالة بالطعام والماء النظيف يومياً.",
        "lat": 24.7152,
        "lng": 46.6741
      },
      {
        "id": 9,
        "title": "متجر واحة الحيوان الشامل 🛒",
        "description": "أكبر الفروع لـ لوازم الحيوانات بالرياض، يتضمن قسماً خاصاً للحيوانات الصغيرة والطيور.",
        "lat": 24.7312,
        "lng": 46.6985
      },
      {
        "id": 10,
        "title": "مركز بسمة أليف للعناية الطبية 🏥",
        "description": "فحوصات طبية دورية وصيدلية متكاملة لتقديم الرعاية الوقائية وحماية أليفك.",
        "lat": 24.7568,
        "lng": 46.6212
      },
      {
        "id": 11,
        "title": "نادي ومسبح الكلاب الرياضي 🐾",
        "description": "مسبح وغرف ترفيهية متكاملة للياقة والتدريب وتخفيف التوتر للكلاب الكبيرة والصغيرة.",
        "lat": 24.8421,
        "lng": 46.6455
      },
      {
        "id": 12,
        "title": "جمعية رفق للرفق بالحيوان 🐶",
        "description": "جمعية خيرية لنشر الوعي بحقوق الحيوان، إنقاذ الحالات المصابة، وتنظيم حملات التعقيم.",
        "lat": 24.6312,
        "lng": 46.7118
      },
      {
        "id": 13,
        "title": "مركز شفاء للطب البيطري - الدار البيضاء 🏥",
        "description": "رعاية طبية وعلاج متقدم وجراحة للحيوانات الأليفة في حي المعاريف بالدار البيضاء.",
        "lat": 33.5731,
        "lng": -7.5898
      },
      {
        "id": 14,
        "title": "ملجأ الأمل لإنقاذ الحيوانات - الرباط 🐶",
        "description": "جمعية خيرية عريقة لإيواء وتربية القطط والكلاب الضالة والبحث عن متبنين حنونين بالرباط ونواحيها.",
        "lat": 34.0209,
        "lng": -6.8416
      },
      {
        "id": 15,
        "title": "محل أليفي المدلل - طنجة 🛒",
        "description": "أرقى الأغذية والمستلزمات المستوردة لجميع أنواع الحيوانات الأليفة والطيور والأسماك في مدينة طنجة.",
        "lat": 35.7595,
        "lng": -5.8340
      },
      {
        "id": 16,
        "title": "العيادة البيطرية الكبرى لحي النخيل - مراكش 🏥",
        "description": "رعاية صحية عاجلة، تصوير بالموجات فوق الصوتية، وتحاليل مخبرية، مع قسم خاص للعناية والوقاية بأحياء مراكش.",
        "lat": 31.6295,
        "lng": -7.9811
      },
      {
        "id": 17,
        "title": "ملجأ سوس لإنقاذ ورعاية القطط - أيت ملول 🐈",
        "description": "مبادرة خيرية من محبي القطط لتوفير بيئة إنقاذ دافئة وتطعيم وتثقيف المجتمع لتبني الهررة الضالة بأكادير ونواحيها.",
        "lat": 30.4179,
        "lng": -9.5776
      },
      {
        "id": 18,
        "title": "صالون تدليل الأنيق لخدمات التزيين - الدار البيضاء ✂️",
        "description": "استحمام، قص شعر، تنظيف وتقليم أظافر للقطط والكلاب مع تأمين أعلى مستويات الراحة لحيواناتكم بالبيضاء.",
        "lat": 33.5852,
        "lng": -7.6324
      }
    ]
    """.trimIndent()
    
    val url = "$supabaseUrl/rest/v1/markers"
    val body = okhttp3.RequestBody.create(null, bulkMarkersJson)
    val request = okhttp3.Request.Builder()
        .url(url)
        .header("apikey", apiKey)
        .header("Authorization", "Bearer $apiKey")
        .header("Content-Type", "application/json")
        .header("Prefer", "return=representation")
        .post(body)
        .build()
        
    try {
        client.newCall(request).execute().use { resp ->
            val bodyStr = resp.body?.string() ?: ""
            println("Bulk Insert code: ${resp.code}")
            println("Bulk Insert body length: ${bodyStr.length}")
        }
    } catch (e: Exception) {
        println("Error during bulk insertion: ${e.message}")
    }
  }

  @Test
  fun clearSupabaseTable() {
    val client = okhttp3.OkHttpClient()
    val supabaseUrl = "https://rxclrwcwhbvnldmguxko.supabase.co"
    val apiKey = "sb_publishable_7ECYLHs8ZeuE40g2El1KiQ_Ghhg79Uf"
    val deleteUrl = "$supabaseUrl/rest/v1/markers?id=gte.0"
    
    val deleteRequest = okhttp3.Request.Builder()
        .url(deleteUrl)
        .header("apikey", apiKey)
        .header("Authorization", "Bearer $apiKey")
        .delete()
        .build()
        
    try {
        client.newCall(deleteRequest).execute().use { resp ->
            println("Delete previous markers response code: ${resp.code}")
            println("Delete body: ${resp.body?.string()}")
        }
    } catch (e: Exception) {
        println("Error deleting: ${e.message}")
    }
  }

  @Test
  fun testFetchSupabaseLayers() {
    val client = okhttp3.OkHttpClient()
    val url = "https://rxclrwcwhbvnldmguxko.supabase.co/rest/v1/layers"
    val apiKey = "sb_publishable_7ECYLHs8ZeuE40g2El1KiQ_Ghhg79Uf"
    val request = okhttp3.Request.Builder()
        .url(url)
        .header("apikey", apiKey)
        .header("Authorization", "Bearer $apiKey")
        .build()
    try {
        client.newCall(request).execute().use { resp ->
            val body = resp.body?.string() ?: ""
            println("Layers status: ${resp.code}, length: ${body.length}")
            java.io.File("src/test/java/com/example/supabase_layers.json").writeText(body)
            
            // Try parsing using the MainActivity's parsing function
            val parsed = parseSupabaseLayersToPlaces(body)
            println("Parsed ${parsed.size} places from layers!")
            parsed.groupBy { it.category }.forEach { (cat, list) ->
                println("Category '$cat': ${list.size} places")
            }
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
  }

  @Test
  fun testParseChunksOfChunkedLayer() {
    val client = OkHttpClient()
    val apiKey = "AIzaSyAu2iWAA8WmMdkMRAC95FneRF-oV8Hafh4"
    val dbId = "ai-studio-ca385af0-d7d1-4e5a-90f0-6ed01800913a"
    val layerId = "custom-layer-1782132225299"
    
    val url = "https://firestore.googleapis.com/v1/projects/corded-principle-5dzmz/databases/$dbId/documents/layers/$layerId:runQuery?key=$apiKey"
    val queryBodyJson = """
    {
      "structuredQuery": {
        "from": [
          {
            "collectionId": "chunks",
            "allDescendants": false
          }
        ]
      }
    }
    """.trimIndent()
    try {
        val body = okhttp3.RequestBody.create(null, queryBodyJson)
        val req = Request.Builder().url(url).post(body).build()
        client.newCall(req).execute().use { resp ->
            val responseStr = resp.body?.string() ?: ""
            println("Response code for chunks runQuery: ${resp.code}")
            println("Response body length: ${responseStr.length}")
            
            if (responseStr.trim().startsWith("[")) {
                val chunkArr = org.json.JSONArray(responseStr)
                var totalParsed = 0
                println("Found ${chunkArr.length()} documents in chunks query")
                for (chunkIndex in 0 until chunkArr.length()) {
                    val chunkWrapObj = chunkArr.optJSONObject(chunkIndex) ?: continue
                    val chunkDocObj = chunkWrapObj.optJSONObject("document") ?: continue
                    val chunkName = chunkDocObj.optString("name")
                    val chunkFieldsObj = chunkDocObj.optJSONObject("fields") ?: continue
                    
                    val chunkFeaturesField = chunkFieldsObj.optJSONObject("features")
                    if (chunkFeaturesField == null) {
                        println("  Chunk #$chunkIndex ($chunkName) has no features field!")
                        continue
                    }
                    val chunkFeaturesArr = chunkFeaturesField.optJSONObject("arrayValue")?.optJSONArray("values")
                    if (chunkFeaturesArr == null) {
                        println("  Chunk #$chunkIndex ($chunkName) features array is null!")
                        continue
                    }
                    
                    println("  Chunk #$chunkIndex ($chunkName) features count: ${chunkFeaturesArr.length()}")
                    var chunkParsedCount = 0
                    for (featIndex in 0 until chunkFeaturesArr.length()) {
                        val featObj = chunkFeaturesArr.optJSONObject(featIndex)?.optJSONObject("mapValue")?.optJSONObject("fields") ?: continue
                        val place = parseFirestoreFeature(featObj, chunkIndex * 1000, featIndex)
                        if (place != null) {
                            totalParsed++
                            chunkParsedCount++
                            if (totalParsed <= 10) {
                                println("    Parsed Place #$totalParsed: Name='${place.name}', Lat=${place.lat}, Lng=${place.lng}, Category='${place.category}'")
                            }
                        }
                    }
                    println("  Successfully parsed $chunkParsedCount / ${chunkFeaturesArr.length()} in this chunk")
                }
                println("Grand total parsed from chunks: $totalParsed")
            } else {
                println("Response is not an array!")
            }
        }
    } catch (e: Exception) {
        println("Exception: ${e.message}")
        e.printStackTrace()
    }
  }
}

