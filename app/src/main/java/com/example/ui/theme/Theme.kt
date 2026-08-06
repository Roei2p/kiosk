package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme =
  darkColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = HighDensityDarkBlue,
    onSecondary = HighDensityOnDarkBlue,
    tertiary = HighDensitySuccess,
    tertiaryContainer = HighDensitySuccessContainer,
    onTertiaryContainer = HighDensityOnSuccessContainer,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = HighDensityOnPrimary,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondary = HighDensityDarkBlue,
    onSecondary = HighDensityOnDarkBlue,
    secondaryContainer = HighDensityPrimaryContainer,
    tertiary = HighDensitySuccess,
    tertiaryContainer = HighDensitySuccessContainer,
    onTertiaryContainer = HighDensityOnSuccessContainer,
    background = HighDensityBackground,
    surface = HighDensitySurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onSurface = HighDensityOnSurface,
    onSurfaceVariant = HighDensityOnSurfaceVariant,
    outline = HighDensityBorder
  )

@Composable
fun MedicalKioskTheme(
  darkTheme: Boolean = false,
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

  // Mandatory Hebrew RTL Support
  CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
