package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.model.ExtractedPaletteSwatch
import com.example.model.ThemePalette
import com.example.model.WallpaperType

/**
 * Material You Dynamic Theming & Wallpaper Palette Extraction Engine.
 * Extracts harmonious tonal palettes from active wallpapers and generates
 * complete Material 3 ColorSchemes for the NovaOS interface.
 */
object MaterialYouThemeEngine {

    /**
     * Extracts 4 distinct dynamic color palette options derived from the provided wallpaper.
     */
    fun extractWallpaperPalettes(wallpaper: WallpaperType): List<ExtractedPaletteSwatch> {
        return when (wallpaper) {
            WallpaperType.DEVICE_SYSTEM -> listOf(
                ExtractedPaletteSwatch(
                    id = "system_tonal",
                    title = "Telefon Dinamik Tonal",
                    primaryColor = 0xFF00E676,
                    secondaryColor = 0xFF00B0FF,
                    tertiaryColor = 0xFF004D40,
                    surfaceColor = 0xFF0B1F16,
                    containerColor = 0xFF005A32,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "system_vibrant",
                    title = "Telefon Dinamik Canlı",
                    primaryColor = 0xFF00E5FF,
                    secondaryColor = 0xFF76FF03,
                    tertiaryColor = 0xFF00B8D4,
                    surfaceColor = 0xFF0A1C24,
                    containerColor = 0xFF006064,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "system_pastel",
                    title = "Telefon Dinamik Pastel",
                    primaryColor = 0xFF81C784,
                    secondaryColor = 0xFF80DEEA,
                    tertiaryColor = 0xFF26A69A,
                    surfaceColor = 0xFF121F18,
                    containerColor = 0xFF2E4C38,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "system_dualtone",
                    title = "Telefon Dinamik Çift Ton",
                    primaryColor = 0xFFFFB74D,
                    secondaryColor = 0xFF40C4FF,
                    tertiaryColor = 0xFF18FFFF,
                    surfaceColor = 0xFF0F1A24,
                    containerColor = 0xFF1A384D,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )
            WallpaperType.AURORA -> listOf(
                ExtractedPaletteSwatch(
                    id = "aurora_tonal",
                    title = "Aurora Emerald",
                    primaryColor = 0xFF00E676,
                    secondaryColor = 0xFF00B0FF,
                    tertiaryColor = 0xFF004D40,
                    surfaceColor = 0xFF0B1F16,
                    containerColor = 0xFF005A32,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "aurora_vibrant",
                    title = "Electric Cyan",
                    primaryColor = 0xFF00E5FF,
                    secondaryColor = 0xFF76FF03,
                    tertiaryColor = 0xFF00B8D4,
                    surfaceColor = 0xFF0A1C24,
                    containerColor = 0xFF006064,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "aurora_muted",
                    title = "Arctic Mint",
                    primaryColor = 0xFF81C784,
                    secondaryColor = 0xFF80DEEA,
                    tertiaryColor = 0xFF26A69A,
                    surfaceColor = 0xFF121F18,
                    containerColor = 0xFF2E4C38,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "aurora_dualtone",
                    title = "Borealis Dusk",
                    primaryColor = 0xFF69F0AE,
                    secondaryColor = 0xFF40C4FF,
                    tertiaryColor = 0xFF18FFFF,
                    surfaceColor = 0xFF0F1A24,
                    containerColor = 0xFF1A384D,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.CYBERPUNK -> listOf(
                ExtractedPaletteSwatch(
                    id = "cyber_tonal",
                    title = "Neon Magenta",
                    primaryColor = 0xFFFF007F,
                    secondaryColor = 0xFF00F5FF,
                    tertiaryColor = 0xFF7928CA,
                    surfaceColor = 0xFF1F0B18,
                    containerColor = 0xFF5C0030,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "cyber_vibrant",
                    title = "Cyber Cyan",
                    primaryColor = 0xFF00F5FF,
                    secondaryColor = 0xFFFF007F,
                    tertiaryColor = 0xFFFFD600,
                    surfaceColor = 0xFF071924,
                    containerColor = 0xFF004D5C,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "cyber_muted",
                    title = "Synthwave Violet",
                    primaryColor = 0xFFB388FF,
                    secondaryColor = 0xFFFF80AB,
                    tertiaryColor = 0xFF80D8FF,
                    surfaceColor = 0xFF151020,
                    containerColor = 0xFF352055,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "cyber_dualtone",
                    title = "Night City Contrast",
                    primaryColor = 0xFFFF4081,
                    secondaryColor = 0xFF00E5FF,
                    tertiaryColor = 0xFFFFEA00,
                    surfaceColor = 0xFF14081E,
                    containerColor = 0xFF421554,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.ABSTRACT -> listOf(
                ExtractedPaletteSwatch(
                    id = "abstract_tonal",
                    title = "Sunset Ochre",
                    primaryColor = 0xFFFF9100,
                    secondaryColor = 0xFFFF5722,
                    tertiaryColor = 0xFFFFA000,
                    surfaceColor = 0xFF24140B,
                    containerColor = 0xFF5C2D00,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "abstract_vibrant",
                    title = "Warm Coral",
                    primaryColor = 0xFFFF5722,
                    secondaryColor = 0xFFFFD54F,
                    tertiaryColor = 0xFFE64A19,
                    surfaceColor = 0xFF240E0A,
                    containerColor = 0xFF631B08,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "abstract_muted",
                    title = "Earth Sand",
                    primaryColor = 0xFFFFB74D,
                    secondaryColor = 0xFFFFCC80,
                    tertiaryColor = 0xFFD7CCC8,
                    surfaceColor = 0xFF1F1813,
                    containerColor = 0xFF4D3826,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "abstract_dualtone",
                    title = "Desert Bloom",
                    primaryColor = 0xFFFF7043,
                    secondaryColor = 0xFFFFCA28,
                    tertiaryColor = 0xFFFF8A65,
                    surfaceColor = 0xFF201318,
                    containerColor = 0xFF4A1F2C,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.DEEP_SPACE -> listOf(
                ExtractedPaletteSwatch(
                    id = "space_tonal",
                    title = "Cosmic Purple",
                    primaryColor = 0xFFAB47BC,
                    secondaryColor = 0xFF3D5AFE,
                    tertiaryColor = 0xFF7C4DFF,
                    surfaceColor = 0xFF150B20,
                    containerColor = 0xFF3E1259,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "space_vibrant",
                    title = "Nebula Indigo",
                    primaryColor = 0xFF536DFE,
                    secondaryColor = 0xFFE040FB,
                    tertiaryColor = 0xFF00E5FF,
                    surfaceColor = 0xFF0C1024,
                    containerColor = 0xFF1B286E,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "space_muted",
                    title = "Starlight Lavender",
                    primaryColor = 0xFFCE93D8,
                    secondaryColor = 0xFF9FA8DA,
                    tertiaryColor = 0xFFB39DDB,
                    surfaceColor = 0xFF14111C,
                    containerColor = 0xFF362C47,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "space_dualtone",
                    title = "Supernova Flare",
                    primaryColor = 0xFF7C4DFF,
                    secondaryColor = 0xFF00B0FF,
                    tertiaryColor = 0xFFFF4081,
                    surfaceColor = 0xFF0F0B1E,
                    containerColor = 0xFF2B1C57,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.FOREST_MIST -> listOf(
                ExtractedPaletteSwatch(
                    id = "forest_tonal",
                    title = "Pine Sage",
                    primaryColor = 0xFF66BB6A,
                    secondaryColor = 0xFF2E7D32,
                    tertiaryColor = 0xFF81C784,
                    surfaceColor = 0xFF0D1C10,
                    containerColor = 0xFF1B4D22,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "forest_vibrant",
                    title = "Emerald Moss",
                    primaryColor = 0xFF00E676,
                    secondaryColor = 0xFFAEEA00,
                    tertiaryColor = 0xFF00BFA5,
                    surfaceColor = 0xFF091F14,
                    containerColor = 0xFF00572B,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "forest_muted",
                    title = "Earthy Meadow",
                    primaryColor = 0xFFA5D6A7,
                    secondaryColor = 0xFFC8E6C9,
                    tertiaryColor = 0xFF80CBC4,
                    surfaceColor = 0xFF131E16,
                    containerColor = 0xFF2A3D2F,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "forest_dualtone",
                    title = "Mist & Sunbeam",
                    primaryColor = 0xFF4CAF50,
                    secondaryColor = 0xFFFFD54F,
                    tertiaryColor = 0xFF26A69A,
                    surfaceColor = 0xFF141B12,
                    containerColor = 0xFF334526,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.OCEAN_SUNRISE -> listOf(
                ExtractedPaletteSwatch(
                    id = "ocean_tonal",
                    title = "Ocean Sapphire",
                    primaryColor = 0xFF0288D1,
                    secondaryColor = 0xFFFF7043,
                    tertiaryColor = 0xFF00ACC1,
                    surfaceColor = 0xFF0A1724,
                    containerColor = 0xFF003E6B,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "ocean_vibrant",
                    title = "Sunrise Coral",
                    primaryColor = 0xFFFF7043,
                    secondaryColor = 0xFF40C4FF,
                    tertiaryColor = 0xFFFFD54F,
                    surfaceColor = 0xFF1F120E,
                    containerColor = 0xFF5C2414,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "ocean_muted",
                    title = "Seaside Mist",
                    primaryColor = 0xFF81D4FA,
                    secondaryColor = 0xFFFFAB91,
                    tertiaryColor = 0xFFB2EBF2,
                    surfaceColor = 0xFF121B22,
                    containerColor = 0xFF253947,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "ocean_dualtone",
                    title = "Tidal Glow",
                    primaryColor = 0xFF00B0FF,
                    secondaryColor = 0xFFFF8A65,
                    tertiaryColor = 0xFF26C6DA,
                    surfaceColor = 0xFF0D1B28,
                    containerColor = 0xFF144161,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )

            WallpaperType.MINIMAL_GRADIENT -> listOf(
                ExtractedPaletteSwatch(
                    id = "velvet_tonal",
                    title = "Velvet Rose",
                    primaryColor = 0xFFE91E63,
                    secondaryColor = 0xFFB388FF,
                    tertiaryColor = 0xFFFF80AB,
                    surfaceColor = 0xFF1C0A14,
                    containerColor = 0xFF540026,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_1
                ),
                ExtractedPaletteSwatch(
                    id = "velvet_vibrant",
                    title = "Peach Gold",
                    primaryColor = 0xFFFF4081,
                    secondaryColor = 0xFFFFD700,
                    tertiaryColor = 0xFFFF80AB,
                    surfaceColor = 0xFF1F0D16,
                    containerColor = 0xFF5E1033,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_2
                ),
                ExtractedPaletteSwatch(
                    id = "velvet_muted",
                    title = "Soft Lilac",
                    primaryColor = 0xFFF48FB1,
                    secondaryColor = 0xFFD1C4E9,
                    tertiaryColor = 0xFFFFCDD2,
                    surfaceColor = 0xFF1D1219,
                    containerColor = 0xFF452B39,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_3
                ),
                ExtractedPaletteSwatch(
                    id = "velvet_dualtone",
                    title = "Twilight Velvet",
                    primaryColor = 0xFFEC407A,
                    secondaryColor = 0xFF7C4DFF,
                    tertiaryColor = 0xFFFFAB91,
                    surfaceColor = 0xFF160E21,
                    containerColor = 0xFF3C1F5C,
                    paletteEnum = ThemePalette.WALLPAPER_DYNAMIC_4
                )
            )
        }
    }

    /**
     * Resolves dynamic Material You ColorScheme for a specific wallpaper & selected extracted palette.
     */
    fun getDynamicColorSchemeForWallpaper(
        wallpaper: WallpaperType,
        palette: ThemePalette,
        isDark: Boolean
    ): ColorScheme {
        val extractedPalettes = extractWallpaperPalettes(wallpaper)
        val swatch = when (palette) {
            ThemePalette.WALLPAPER_DYNAMIC_1 -> extractedPalettes.getOrNull(0) ?: extractedPalettes.first()
            ThemePalette.WALLPAPER_DYNAMIC_2 -> extractedPalettes.getOrNull(1) ?: extractedPalettes.first()
            ThemePalette.WALLPAPER_DYNAMIC_3 -> extractedPalettes.getOrNull(2) ?: extractedPalettes.first()
            ThemePalette.WALLPAPER_DYNAMIC_4 -> extractedPalettes.getOrNull(3) ?: extractedPalettes.first()
            else -> extractedPalettes.first()
        }

        val primary = Color(swatch.primaryColor)
        val secondary = Color(swatch.secondaryColor)
        val tertiary = Color(swatch.tertiaryColor)

        return if (isDark) {
            darkColorScheme(
                primary = primary,
                onPrimary = Color(0xFF001F12),
                primaryContainer = Color(swatch.containerColor),
                onPrimaryContainer = Color(0xFFE8F5E9),
                secondary = secondary,
                onSecondary = Color(0xFF001F28),
                secondaryContainer = secondary.copy(alpha = 0.35f),
                onSecondaryContainer = Color(0xFFE0F7FA),
                tertiary = tertiary,
                onTertiary = Color(0xFF1E0028),
                tertiaryContainer = tertiary.copy(alpha = 0.35f),
                onTertiaryContainer = Color(0xFFF3E5F5),
                background = Color(0xFF0C1014),
                onBackground = Color(0xFFE2E6EA),
                surface = Color(swatch.surfaceColor),
                onSurface = Color(0xFFE8ECEF),
                surfaceVariant = Color(swatch.surfaceColor).copy(alpha = 0.85f),
                onSurfaceVariant = Color(0xFFB0BEC5),
                outline = Color(0xFF78909C),
                outlineVariant = Color(0xFF37474F)
            )
        } else {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.2f),
                onPrimaryContainer = Color(0xFF002204),
                secondary = secondary,
                onSecondary = Color.White,
                secondaryContainer = secondary.copy(alpha = 0.2f),
                onSecondaryContainer = Color(0xFF001E2B),
                tertiary = tertiary,
                onTertiary = Color.White,
                tertiaryContainer = tertiary.copy(alpha = 0.2f),
                onTertiaryContainer = Color(0xFF2E0038),
                background = Color(0xFFF8FAF9),
                onBackground = Color(0xFF191C1E),
                surface = Color.White,
                onSurface = Color(0xFF191C1E),
                surfaceVariant = Color(0xFFE6EAE8),
                onSurfaceVariant = Color(0xFF424940),
                outline = Color(0xFF72796F),
                outlineVariant = Color(0xFFC2C9BD)
            )
        }
    }
}
