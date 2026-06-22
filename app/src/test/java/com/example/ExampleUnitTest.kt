package com.example

import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFetchWebsite() {
    val client = OkHttpClient()
    val apiKey = "AIzaSyAu2iWAA8WmMdkMRAC95FneRF-oV8Hafh4"
    
    val layers = listOf("custom-layer-1781872977059", "custom-layer-1781794015901")
    val report = StringBuilder()
    
    for (layerId in layers) {
        val runQueryUrl = "https://firestore.googleapis.com/v1/projects/corded-principle-5dzmz/databases/ai-studio-ca385af0-d7d1-4e5a-90f0-6ed01800913a/documents/layers/$layerId:runQuery?key=$apiKey"
        
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
        
        val body = RequestBody.create(null, queryBodyJson)
        val request = Request.Builder().url(runQueryUrl).post(body).build()
        val response = client.newCall(request).execute()
        val resultStr = response.body?.string() ?: ""
        
        report.append("=== LAYER: $layerId ===\n")
        try {
            val rootArr = org.json.JSONArray(resultStr)
            report.append("Docs found: ${rootArr.length()}\n")
            for (i in 0 until rootArr.length()) {
                val obj = rootArr.optJSONObject(i) ?: continue
                val doc = obj.optJSONObject("document") ?: continue
                val name = doc.optString("name")
                val fields = doc.optJSONObject("fields") ?: continue
                
                val idx = fields.optJSONObject("index")?.optString("integerValue") ?: fields.optJSONObject("index")?.optDouble("doubleValue")?.toString() ?: ""
                val featuresArr = fields.optJSONObject("features")?.optJSONObject("arrayValue")?.optJSONArray("values")
                val featureCount = featuresArr?.length() ?: 0
                
                report.append("  Doc $i: ID='${name.substringAfterLast("/")}', index=$idx, features=$featureCount\n")
            }
        } catch (e: Exception) {
            report.append("Error parsing: ${e.message}\n$resultStr\n")
        }
        report.append("\n")
    }
    
    java.io.File("src/test/java/com/example/fragment_out.txt").writeText(report.toString())
    fail("Done")
  }
}
