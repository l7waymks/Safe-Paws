package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerTeal,
    secondary = SecondaryTerracotta,
    onSecondary = OnSecondaryWhite,
    background = OnBackgroundNavy,
    surface = OnBackgroundNavy,
    onBackground = BackgroundSky,
    onSurface = BackgroundSky
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerTeal,
    secondary = SecondaryTerracotta,
    onSecondary = OnSecondaryWhite,
    secondaryContainer = SecondaryContainerPeach,
    background = BackgroundSky,
    surface = SurfaceWhite,
    onBackground = OnBackgroundNavy,
    onSurface = OnSurfaceNavy,
    onSurfaceVariant = OnSurfaceVariantSlate,
    outlineVariant = OutlineVariantTeal,
    error = ErrorRed,
    tertiary = GoldTertiary,
    tertiaryContainer = SoftGoldContainer,
    onTertiaryContainer = OnGoldContainer
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default to enforce our premium Safe Paws branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
