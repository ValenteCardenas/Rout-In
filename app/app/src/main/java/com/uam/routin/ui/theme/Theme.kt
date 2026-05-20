package com.uam.routin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme

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

val RoutInLightColorScheme = lightColorScheme(
    primary = RoutInColors.VibrantGreenEmphasis,
    onPrimary = RoutInColors.DeepPurpleNavy,
    background = RoutInColors.OffWhiteSerenity,
    onBackground = RoutInColors.DeepPurpleNavy,
    surface = RoutInColors.OffWhiteSerenity,
    onSurface = RoutInColors.DeepPurpleNavy,
    surfaceVariant = RoutInColors.SoftMutedLavender,
    onSurfaceVariant = RoutInColors.SlateGray
)

@Composable
fun RoutInTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RoutInDarkColorScheme else RoutInLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RoutInTypography,
        content = content
    )
}