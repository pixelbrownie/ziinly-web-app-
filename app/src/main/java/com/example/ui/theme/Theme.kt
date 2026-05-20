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
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = ZiinlyPink,
    secondary = ZiinlyOrange,
    tertiary = ZiinlyBlue,
    background = ZiinlyCharcoal,
    surface = Color(0xFF232338),
    onPrimary = ZiinlyCharcoal,
    onBackground = ZiinlyCream,
    onSurface = ZiinlyCream
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ZiinlyPink,
    secondary = ZiinlyOrange,
    tertiary = ZiinlyBlue,
    background = ZiinlyCream,
    surface = ZiinlyCream,
    onPrimary = ZiinlyCharcoal,
    onSecondary = ZiinlyCharcoal,
    onTertiary = ZiinlyCharcoal,
    onBackground = ZiinlyCharcoal,
    onSurface = ZiinlyCharcoal,
    outline = ZiinlyCharcoal
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For Ziinly, we disable dynamicColors to preserve the Warm Cream branding
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
