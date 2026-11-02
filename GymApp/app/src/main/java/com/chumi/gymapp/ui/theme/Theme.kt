package com.chumi.gymapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.chumi.gymapp.data.ThemeMode

private val Orange = Color(0xFFFF7A1A)
private val OrangeDark = Color(0xFFC85A00)

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color(0xFF2B1300),
    primaryContainer = Color(0xFF5A2A00),
    onPrimaryContainer = Color(0xFFFFDBC8),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF00283A),
    secondaryContainer = Color(0xFF0E3A50),
    onSecondaryContainer = Color(0xFFCDEBFF),
    tertiary = Color(0xFF7BD88F),
    onTertiary = Color(0xFF00391A),
    tertiaryContainer = Color(0xFF1B4D2A),
    onTertiaryContainer = Color(0xFFC9F5D2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFECE6E1),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFECE6E1),
    surfaceVariant = Color(0xFF2A2522),
    onSurfaceVariant = Color(0xFFC9C0B9),
    surfaceContainerLowest = Color(0xFF0D0D0D),
    surfaceContainerLow = Color(0xFF1A1817),
    surfaceContainer = Color(0xFF1F1C1A),
    surfaceContainerHigh = Color(0xFF292523),
    surfaceContainerHighest = Color(0xFF34302D),
    outline = Color(0xFF8F8780),
    outlineVariant = Color(0xFF4A433E),
    error = Color(0xFFFF6B6B),
)

private val LightColors = lightColorScheme(
    primary = OrangeDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBC8),
    onPrimaryContainer = Color(0xFF331200),
    secondary = Color(0xFF00658E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC7E7FF),
    onSecondaryContainer = Color(0xFF001E2E),
    tertiary = Color(0xFF1E6B33),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB4F1C0),
    onTertiaryContainer = Color(0xFF00210B),
    background = Color(0xFFFCF8F5),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFCF8F5),
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF2E6DE),
    onSurfaceVariant = Color(0xFF52443C),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF8F1EC),
    surfaceContainer = Color(0xFFF3EBE5),
    surfaceContainerHigh = Color(0xFFEDE4DE),
    surfaceContainerHighest = Color(0xFFE7DED8),
    outline = Color(0xFF85736A),
    outlineVariant = Color(0xFFD8C2B8),
)

private val AppTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Black),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = labelSmall.copy(fontSize = 11.sp),
    )
}

@Composable
fun isAppDark(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
}

@Composable
fun GymTheme(mode: ThemeMode, dynamic: Boolean, content: @Composable () -> Unit) {
    val dark = isAppDark(mode)
    val ctx = LocalContext.current
    val colors: ColorScheme = when {
        dynamic && Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
