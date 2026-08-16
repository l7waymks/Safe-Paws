package com.example.ui.sdui

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object SduiParser {
    private const val TAG = "SduiParser"

    fun parse(jsonString: String): SduiComponent? {
        return try {
            val jsonObject = JSONObject(jsonString)
            parseComponent(jsonObject)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing SDUI JSON", e)
            null
        }
    }

    private fun parseComponent(obj: JSONObject): SduiComponent? {
        val type = obj.optString("type")
        return when (type) {
            "Text" -> parseText(obj)
            "Image" -> parseImage(obj)
            "Button" -> parseButton(obj)
            "Column" -> parseColumn(obj)
            "Row" -> parseRow(obj)
            "Card" -> parseCard(obj)
            else -> {
                Log.w(TAG, "Unknown SDUI component type: $type")
                null
            }
        }
    }

    private fun parseText(obj: JSONObject): SduiText {
        return SduiText(
            text = obj.optString("text", ""),
            sizeSp = if (obj.has("sizeSp")) obj.getDouble("sizeSp").toFloat() else null,
            colorHex = obj.optString("colorHex", null),
            isBold = obj.optBoolean("isBold", false)
        )
    }

    private fun parseImage(obj: JSONObject): SduiImage {
        return SduiImage(
            url = obj.optString("url", ""),
            heightDp = if (obj.has("heightDp")) obj.getDouble("heightDp").toFloat() else null,
            cornerRadiusDp = if (obj.has("cornerRadiusDp")) obj.getDouble("cornerRadiusDp").toFloat() else null
        )
    }

    private fun parseButton(obj: JSONObject): SduiButton {
        return SduiButton(
            text = obj.optString("text", ""),
            actionUrl = obj.optString("actionUrl", null),
            colorHex = obj.optString("colorHex", null)
        )
    }

    private fun parseColumn(obj: JSONObject): SduiColumn {
        val childrenJson = obj.optJSONArray("children")
        return SduiColumn(
            children = parseChildren(childrenJson),
            spacingDp = obj.optDouble("spacingDp", 0.0).toFloat(),
            paddingDp = obj.optDouble("paddingDp", 0.0).toFloat(),
            backgroundColorHex = obj.optString("backgroundColorHex", null)
        )
    }

    private fun parseRow(obj: JSONObject): SduiRow {
        val childrenJson = obj.optJSONArray("children")
        return SduiRow(
            children = parseChildren(childrenJson),
            spacingDp = obj.optDouble("spacingDp", 0.0).toFloat(),
            paddingDp = obj.optDouble("paddingDp", 0.0).toFloat()
        )
    }

    private fun parseCard(obj: JSONObject): SduiCard {
        val childJson = obj.optJSONObject("child")
        val child = childJson?.let { parseComponent(it) } ?: SduiText("")
        return SduiCard(
            child = child,
            elevationDp = obj.optDouble("elevationDp", 0.0).toFloat(),
            cornerRadiusDp = obj.optDouble("cornerRadiusDp", 8.0).toFloat(),
            backgroundColorHex = obj.optString("backgroundColorHex", null)
        )
    }

    private fun parseChildren(array: JSONArray?): List<SduiComponent> {
        val list = mutableListOf<SduiComponent>()
        if (array == null) return list
        for (i in 0 until array.length()) {
            val childObj = array.optJSONObject(i)
            if (childObj != null) {
                parseComponent(childObj)?.let { list.add(it) }
            }
        }
        return list
    }
}
