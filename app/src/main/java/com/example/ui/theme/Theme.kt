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
    primary = TacticalCyan,
    secondary = TacticalPurple,
    tertiary = TacticalGold,
    background = TacticalDarkBackground,
    surface = TacticalDarkSurface,
    surfaceVariant = TacticalDarkSurfaceVariant,
    onPrimary = TacticalDarkBackground,
    onSecondary = TacticalDarkBackground,
    onTertiary = TacticalDarkBackground,
    onBackground = TacticalOnBackground,
    onSurface = TacticalOnSurface,
    error = TacticalRed
  )

private val LightColorScheme = DarkColorScheme // Always use dark theme to preserve tactical dashboard aesthetics

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for the tactical interface
  dynamicColor: Boolean = false, // Disable dynamic color to enforce brand consistency
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
