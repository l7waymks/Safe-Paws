package com.example.ui.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun RenderSduiComponent(component: SduiComponent) {
    when (component) {
        is SduiText -> {
            val color = parseColor(component.colorHex) ?: MaterialTheme.colorScheme.onSurface
            Text(
                text = component.text,
                fontSize = component.sizeSp?.sp ?: 14.sp,
                color = color,
                fontWeight = if (component.isBold) FontWeight.Bold else FontWeight.Normal
            )
        }
        is SduiImage -> {
            AsyncImage(
                model = component.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (component.heightDp != null) Modifier.height(component.heightDp.dp) else Modifier)
                    .then(if (component.cornerRadiusDp != null) Modifier.clip(RoundedCornerShape(component.cornerRadiusDp.dp)) else Modifier)
            )
        }
        is SduiButton -> {
            val containerColor = parseColor(component.colorHex) ?: MaterialTheme.colorScheme.primary
            Button(
                onClick = {
                    // In a real app, handle navigation or deep links via actionUrl
                },
                colors = ButtonDefaults.buttonColors(containerColor = containerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = component.text, color = Color.White)
            }
        }
        is SduiColumn -> {
            val bgColor = parseColor(component.backgroundColorHex) ?: Color.Transparent
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(component.paddingDp.dp),
                verticalArrangement = Arrangement.spacedBy(component.spacingDp.dp)
            ) {
                component.children.forEach { child ->
                    RenderSduiComponent(child)
                }
            }
        }
        is SduiRow -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(component.paddingDp.dp),
                horizontalArrangement = Arrangement.spacedBy(component.spacingDp.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                component.children.forEach { child ->
                    RenderSduiComponent(child)
                }
            }
        }
        is SduiCard -> {
            val bgColor = parseColor(component.backgroundColorHex) ?: MaterialTheme.colorScheme.surface
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(component.cornerRadiusDp.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = component.elevationDp.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                RenderSduiComponent(component.child)
            }
        }
    }
}

private fun parseColor(hex: String?): Color? {
    if (hex.isNullOrEmpty()) return null
    return try {
        Color(android.graphics.Color.parseColor(if (hex.startsWith("#")) hex else "#$hex"))
    } catch (e: Exception) {
        null
    }
}
