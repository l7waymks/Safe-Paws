package com.example.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Data structure representing a single border boundary polygon/ring
 */
data class CountryBorderSegment(
    val countryName: String,
    val points: List<GeoPoint>,
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

/**
 * Loader & Cache for World Country Borders (matching the web GIS system)
 */
object WorldBordersRepository {
    private var cachedSegments: List<CountryBorderSegment>? = null

    suspend fun getBorderSegments(context: Context): List<CountryBorderSegment> {
        cachedSegments?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val segments = ArrayList<CountryBorderSegment>(3600)
                context.assets.open("world_borders.json").use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    val jsonArray = JSONArray(content)

                    for (i in 0 until jsonArray.length()) {
                        val countryObj = jsonArray.getJSONObject(i)
                        val name = countryObj.optString("name", "")
                        val linesArray = countryObj.optJSONArray("lines") ?: continue

                        for (j in 0 until linesArray.length()) {
                            val ringArray = linesArray.optJSONArray(j) ?: continue
                            val ringLen = ringArray.length()
                            if (ringLen < 2) continue

                            val ringPoints = ArrayList<GeoPoint>(ringLen)
                            var minLat = 90.0
                            var maxLat = -90.0
                            var minLng = 180.0
                            var maxLng = -180.0

                            for (k in 0 until ringLen) {
                                val pt = ringArray.getJSONArray(k)
                                val lat = pt.getDouble(0)
                                val lng = pt.getDouble(1)
                                ringPoints.add(GeoPoint(lat, lng))

                                if (lat < minLat) minLat = lat
                                if (lat > maxLat) maxLat = lat
                                if (lng < minLng) minLng = lng
                                if (lng > maxLng) maxLng = lng
                            }

                            segments.add(
                                CountryBorderSegment(
                                    countryName = name,
                                    points = ringPoints,
                                    minLat = minLat,
                                    maxLat = maxLat,
                                    minLng = minLng,
                                    maxLng = maxLng
                                )
                            )
                        }
                    }
                }
                cachedSegments = segments
                segments
            } catch (e: Exception) {
                android.util.Log.e("WorldBorders", "Error parsing world_borders.json: ${e.message}")
                emptyList()
            }
        }
    }
}

/**
 * Ultra-fast GPU-accelerated single-pass Overlay for drawing country borders on OSMDroid.
 * Emulates the exact visual style of the web app (e81cff dashed line) with 60-120 FPS smooth panning.
 */
class WorldBordersOverlay(
    var segments: List<CountryBorderSegment> = emptyList(),
    var density: Float = 2.5f,
    var colorHex: String = "#E81CFF",
    var isDashed: Boolean = true
) : Overlay() {

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private var lastColorHex: String = ""
    private var lastIsDashed: Boolean? = null

    fun updateStyle(newColorHex: String, newIsDashed: Boolean) {
        colorHex = newColorHex
        isDashed = newIsDashed
        updatePaints()
    }

    private fun updatePaints() {
        if (lastColorHex == colorHex && lastIsDashed == isDashed) return
        lastColorHex = colorHex
        lastIsDashed = isDashed

        val parsedColor = try {
            Color.parseColor(colorHex)
        } catch (_: Exception) {
            Color.parseColor("#E81CFF")
        }

        borderPaint.color = parsedColor
        borderPaint.strokeWidth = 2.2f * density
        borderPaint.pathEffect = if (isDashed) {
            DashPathEffect(floatArrayOf(12f * density, 8f * density), 0f)
        } else null

        val r = Color.red(parsedColor)
        val g = Color.green(parsedColor)
        val b = Color.blue(parsedColor)
        glowPaint.color = Color.argb(45, r, g, b)
        glowPaint.strokeWidth = 4.0f * density
    }

    private val tempPoint = Point()
    private val drawPath = Path()

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow || segments.isEmpty()) return

        val projection = mapView.projection ?: return
        val bbox: BoundingBox = mapView.boundingBox ?: return

        updatePaints()

        val viewMinLat = bbox.latSouth
        val viewMaxLat = bbox.latNorth
        val viewMinLng = bbox.lonWest
        val viewMaxLng = bbox.lonEast

        drawPath.reset()
        var hasElementsToDraw = false

        val zoom = mapView.zoomLevelDouble
        // Downsample when zoomed far out to guarantee zero lag on any device
        val step = when {
            zoom < 4.0 -> 4
            zoom < 6.0 -> 2
            else -> 1
        }

        for (seg in segments) {
            // High-speed bounding box intersection test
            if (seg.maxLat < viewMinLat || seg.minLat > viewMaxLat) continue
            if (viewMinLng <= viewMaxLng) {
                if (seg.maxLng < viewMinLng || seg.minLng > viewMaxLng) continue
            }

            val pts = seg.points
            val ptsSize = pts.size
            if (ptsSize < 2) continue

            projection.toPixels(pts[0], tempPoint)
            drawPath.moveTo(tempPoint.x.toFloat(), tempPoint.y.toFloat())

            var i = step
            while (i < ptsSize) {
                projection.toPixels(pts[i], tempPoint)
                drawPath.lineTo(tempPoint.x.toFloat(), tempPoint.y.toFloat())
                i += step
            }
            if (i - step != ptsSize - 1) {
                projection.toPixels(pts[ptsSize - 1], tempPoint)
                drawPath.lineTo(tempPoint.x.toFloat(), tempPoint.y.toFloat())
            }

            hasElementsToDraw = true
        }

        if (hasElementsToDraw) {
            canvas.drawPath(drawPath, glowPaint)
            canvas.drawPath(drawPath, borderPaint)
        }
    }
}

