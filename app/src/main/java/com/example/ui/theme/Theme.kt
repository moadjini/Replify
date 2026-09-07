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
    primary = ElectricPurple,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = TrueDark,
    surface = SurfaceDark,
    onPrimary = SoftWhite,
    onSecondary = SoftWhite,
    onTertiary = SoftWhite,
    onBackground = SoftWhite,
    onSurface = SoftWhite
  )

private val LightColorScheme = DarkColorScheme // Enforce dark

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
