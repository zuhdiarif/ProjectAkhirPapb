package com.zuhdiarifazizi.projectakhirpapb.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AuraColorScheme = lightColorScheme(
    primary = AuraNavy,
    onPrimary = AuraBackground,
    secondary = AuraNavy,
    onSecondary = AuraBackground,
    error = AuraErrorRed,
    onError = AuraBackground,
    background = AuraBackground,
    onBackground = AuraTextPrimary,
    surface = AuraSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraCardSelectedBg,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraCardBorder
)

@Composable
fun ProjectAkhirPapbTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // AURA follows the supplied fixed palette; system/dynamic colors must not replace it.
    MaterialTheme(
        colorScheme = AuraColorScheme,
        typography = Typography,
        content = content
    )
}
