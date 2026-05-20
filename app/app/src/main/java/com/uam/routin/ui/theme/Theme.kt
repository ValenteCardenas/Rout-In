package com.uam.routin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

val RoutInDarkColorScheme = darkColorScheme(
    primary = RoutInColors.VibrantGreenEmphasis,
    onPrimary = RoutInColors.DeepPurpleNavy,
    background = RoutInColors.DeepPurpleNavy,
    onBackground = RoutInColors.OffWhiteSerenity,
    surface = RoutInColors.DarkSurface,
    onSurface = RoutInColors.OffWhiteSerenity,
    surfaceVariant = RoutInColors.SlateGray,
    onSurfaceVariant = RoutInColors.SoftMutedLavender
)

@Composable
fun RoutInTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RoutInDarkColorScheme,
        typography = RoutInTypography,
        content = content
    )
}