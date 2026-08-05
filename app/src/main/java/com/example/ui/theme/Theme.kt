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
    primary = MedicalTealContainer,
    onPrimary = MedicalTealOnContainer,
    primaryContainer = MedicalTealPrimary,
    secondary = MedicalBlueContainer,
    tertiary = MedicalGreenContainer,
    background = MedicalDarkBackground,
    surface = MedicalDarkSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = MedicalTealOnPrimary,
    primaryContainer = MedicalTealContainer,
    onPrimaryContainer = MedicalTealOnContainer,
    secondary = MedicalBlueSecondary,
    secondaryContainer = MedicalBlueContainer,
    tertiary = MedicalGreenSuccess,
    tertiaryContainer = MedicalGreenContainer,
    background = MedicalLightBackground,
    surface = MedicalLightSurface,
    surfaceVariant = MedicalLightSurfaceVariant
  )

@Composable
fun MedicalKioskTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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
