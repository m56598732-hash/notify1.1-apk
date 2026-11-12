package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BlackColorScheme = darkColorScheme(
  primary = PureWhite,
  onPrimary = PureBlack,
  primaryContainer = DarkElevated,
  onPrimaryContainer = PureWhite,
  secondary = OffWhite,
  onSecondary = PureBlack,
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = PureWhite,
  tertiary = PureWhite,
  onTertiary = PureBlack,
  background = PureBlack,
  onBackground = PureWhite,
  surface = PureBlack,
  onSurface = PureWhite,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorderLight,
  error = AccentRed,
  onError = PureWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  // Always use the tailored minimalist pure black scheme
  MaterialTheme(
    colorScheme = BlackColorScheme,
    typography = Typography,
    content = content
  )
}
