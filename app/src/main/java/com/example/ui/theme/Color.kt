package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.ThemePalette

// Primary dynamic palettes
fun getDynamicColorScheme(
    palette: ThemePalette,
    isDark: Boolean,
    wallpaper: com.example.model.WallpaperType = com.example.model.WallpaperType.AURORA
): androidx.compose.material3.ColorScheme {
    return when (palette) {
        ThemePalette.WALLPAPER_DYNAMIC_1,
        ThemePalette.WALLPAPER_DYNAMIC_2,
        ThemePalette.WALLPAPER_DYNAMIC_3,
        ThemePalette.WALLPAPER_DYNAMIC_4 -> {
            MaterialYouThemeEngine.getDynamicColorSchemeForWallpaper(wallpaper, palette, isDark)
        }
        ThemePalette.OCEAN_BLUE -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFF90CAF9),
                onPrimary = Color(0xFF0D47A1),
                primaryContainer = Color(0xFF1976D2),
                onPrimaryContainer = Color(0xFFE3F2FD),
                secondary = Color(0xFF81D4FA),
                onSecondary = Color(0xFF01579B),
                background = Color(0xFF0A1118),
                surface = Color(0xFF111D29),
                onSurface = Color(0xFFE1E8ED),
                surfaceVariant = Color(0xFF1A2B3C),
                onSurfaceVariant = Color(0xFFB0BEC5)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF1565C0),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD1E4FF),
                onPrimaryContainer = Color(0xFF001D36),
                secondary = Color(0xFF0288D1),
                onSecondary = Color.White,
                background = Color(0xFFF6F9FD),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF191C1E),
                surfaceVariant = Color(0xFFE0E8F0),
                onSurfaceVariant = Color(0xFF43474E)
            )
        }
        ThemePalette.ANDROID_GREEN -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFFA5D6A7),
                onPrimary = Color(0xFF1B5E20),
                primaryContainer = Color(0xFF2E7D32),
                onPrimaryContainer = Color(0xFFE8F5E9),
                secondary = Color(0xFF81C784),
                onSecondary = Color(0xFF1B5E20),
                background = Color(0xFF0D140E),
                surface = Color(0xFF152217),
                onSurface = Color(0xFFE2EBE3),
                surfaceVariant = Color(0xFF1E3221),
                onSurfaceVariant = Color(0xFFA5B8A7)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF2E7D32),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFC8E6C9),
                onPrimaryContainer = Color(0xFF002204),
                secondary = Color(0xFF388E3C),
                onSecondary = Color.White,
                background = Color(0xFFF5FAF5),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF191C19),
                surfaceVariant = Color(0xFFDEE5DD),
                onSurfaceVariant = Color(0xFF424940)
            )
        }
        ThemePalette.SUNSET_ORANGE -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFFFFB74D),
                onPrimary = Color(0xFFE65100),
                primaryContainer = Color(0xFFF57C00),
                onPrimaryContainer = Color(0xFFFFF3E0),
                secondary = Color(0xFFFFCC80),
                onSecondary = Color(0xFFE65100),
                background = Color(0xFF140F0A),
                surface = Color(0xFF241A12),
                onSurface = Color(0xFFF5EDE6),
                surfaceVariant = Color(0xFF35261B),
                onSurfaceVariant = Color(0xFFD7CCC8)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFFE65100),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFFFE0B2),
                onPrimaryContainer = Color(0xFF3E1200),
                secondary = Color(0xFFF57C00),
                onSecondary = Color.White,
                background = Color(0xFFFDF7F4),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF201A17),
                surfaceVariant = Color(0xFFF0DFD8),
                onSurfaceVariant = Color(0xFF50443E)
            )
        }
        ThemePalette.LAVENDER_PURPLE -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFFCE93D8),
                onPrimary = Color(0xFF4A148C),
                primaryContainer = Color(0xFF7B1FA2),
                onPrimaryContainer = Color(0xFFF3E5F5),
                secondary = Color(0xFFE1BEE7),
                onSecondary = Color(0xFF4A148C),
                background = Color(0xFF120C17),
                surface = Color(0xFF1E1426),
                onSurface = Color(0xFFEDE4F2),
                surfaceVariant = Color(0xFF2E1F3B),
                onSurfaceVariant = Color(0xFFD1C4E9)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF7B1FA2),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFE1BEE7),
                onPrimaryContainer = Color(0xFF2A003D),
                secondary = Color(0xFF9C27B0),
                onSecondary = Color.White,
                background = Color(0xFFFAF6FD),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1D1A20),
                surfaceVariant = Color(0xFFE7E0EB),
                onSurfaceVariant = Color(0xFF49454E)
            )
        }
        ThemePalette.CYBERPUNK_NEON -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFF00E5FF),
                onPrimary = Color(0xFF001017),
                primaryContainer = Color(0xFF006064),
                onPrimaryContainer = Color(0xFF84FFFF),
                secondary = Color(0xFFFF4081),
                onSecondary = Color(0xFF20000D),
                background = Color(0xFF050811),
                surface = Color(0xFF0D1322),
                onSurface = Color(0xFFE0F7FA),
                surfaceVariant = Color(0xFF152238),
                onSurfaceVariant = Color(0xFF80DEEA)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF0097A7),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFB2EBF2),
                onPrimaryContainer = Color(0xFF002024),
                secondary = Color(0xFFE91E63),
                onSecondary = Color.White,
                background = Color(0xFFF4FBFC),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF161D1E),
                surfaceVariant = Color(0xFFCCE8EB),
                onSurfaceVariant = Color(0xFF3D494A)
            )
        }
        ThemePalette.MONOCHROME -> if (isDark) {
            androidx.compose.material3.darkColorScheme(
                primary = Color(0xFFCFD8DC),
                onPrimary = Color(0xFF263238),
                primaryContainer = Color(0xFF37474F),
                onPrimaryContainer = Color(0xFFECEFF1),
                secondary = Color(0xFFB0BEC5),
                onSecondary = Color(0xFF263238),
                background = Color(0xFF0F1214),
                surface = Color(0xFF191E22),
                onSurface = Color(0xFFECEFF1),
                surfaceVariant = Color(0xFF263238),
                onSurfaceVariant = Color(0xFF90A4AE)
            )
        } else {
            androidx.compose.material3.lightColorScheme(
                primary = Color(0xFF37474F),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFECEFF1),
                onPrimaryContainer = Color(0xFF101416),
                secondary = Color(0xFF546E7A),
                onSecondary = Color.White,
                background = Color(0xFFF7F8F9),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF1A1C1E),
                surfaceVariant = Color(0xFFDFE2E6),
                onSurfaceVariant = Color(0xFF44474B)
            )
        }
    }
}
