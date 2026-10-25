package com.afnuke.halloweenfamilyfeud.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HalloweenColorScheme = darkColorScheme(
    primary = HalloweenOrange,
    onPrimary = CharcoalBg,
    primaryContainer = HalloweenOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = HalloweenPurple,
    onSecondary = Color.White,
    secondaryContainer = HalloweenPurpleDeep,
    onSecondaryContainer = HalloweenPurpleLight,
    background = CharcoalBg,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = TextSecondary,
    outline = HalloweenPurpleBorder
)

@Composable
fun HalloweenFamilyFeudTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HalloweenColorScheme,
        typography = Typography,
        content = content
    )
}
