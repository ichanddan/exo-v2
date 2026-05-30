package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = FintechPrimary,
    onPrimary = FintechSurface,
    primaryContainer = FintechPrimaryContainer,
    onPrimaryContainer = FintechOnPrimaryContainer,
    secondary = FintechSecondary,
    onSecondary = FintechSurface,
    secondaryContainer = FintechSecondaryContainer,
    onSecondaryContainer = FintechOnSecondaryContainer,
    error = FintechError,
    onError = FintechSurface,
    errorContainer = FintechErrorContainer,
    background = FintechBackground,
    onBackground = FintechOnSurface,
    surface = FintechSurface,
    onSurface = FintechOnSurface,
    surfaceVariant = FintechSurfaceContainerLow,
    onSurfaceVariant = FintechOnSurfaceVariant,
    outline = FintechOutline,
    outlineVariant = FintechOutlineVariant,
    inverseSurface = FintechInverseSurface,
    inverseOnSurface = FintechInverseOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Disable dynamic color to lock in "Modern Fiscal Clarity" color branding
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
