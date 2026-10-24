package com.example.data

import com.example.model.*

object WallpaperCatalog {

    val ALL_WALLPAPERS: List<WallpaperDefinition> = listOf(
        // -------------------------------------------------------------
        // 1. LIVE ANIMATED WALLPAPERS
        // -------------------------------------------------------------
        WallpaperDefinition(
            id = "live_aurora",
            title = "Aurora Kozmik Enerji",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.AURORA_GLOW,
            gradientColors = listOf(0xFF030B1E, 0xFF0A192F, 0xFF00E5FF, 0xFF010A14),
            badge = "Canlı 60FPS",
            description = "Sürekli dalgalanan armonik kuzey ışıkları, hareketli ışık hüzmeleri ve parıldayan mikro parçacıklar."
        ),
        WallpaperDefinition(
            id = "live_matrix",
            title = "Siber Matris & Neon",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.CYBER_MATRIX,
            gradientColors = listOf(0xFF030708, 0xFF00FF66, 0xFF08120E),
            badge = "İnteraktif",
            description = "Matrix tarzı düşen neon yeşil/mor dijital veri akışları, terminal kodları ve ışıltılı siber grid."
        ),
        WallpaperDefinition(
            id = "live_nebula",
            title = "Derin Nebula & Galaksi",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.DEEP_NEBULA,
            gradientColors = listOf(0xFF040209, 0xFF7B1FA2, 0xFF00B0FF, 0xFF010005),
            badge = "Canlı 60FPS",
            description = "Derin uzay spiral galaksi bulutları, göz kırpan yıldızlar ve arada geçen kayan meteorlar."
        ),
        WallpaperDefinition(
            id = "live_ocean",
            title = "Biyolüminesan Okyanus",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.OCEAN_WAVES,
            gradientColors = listOf(0xFF021024, 0xFF05264E, 0xFF00E5FF, 0xFF031B33),
            badge = "Canlı 60FPS",
            description = "Sakinleştirici çok katmanlı okyanus dalgaları, ışıldayan biyolüminesans planktonlar ve yumuşak köpükler."
        ),
        WallpaperDefinition(
            id = "live_swarm",
            title = "Kuantum Parçacık Sürüsü",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.PARTICLE_SWARM,
            gradientColors = listOf(0xFF140727, 0xFFFF4081, 0xFF00E5FF, 0xFF020005),
            badge = "Dokunmatik",
            description = "Ekrandaki dokunma ve kaydırmalarınıza göre bir araya toplanan ve dağılan interaktif fizik parçacıkları."
        ),
        WallpaperDefinition(
            id = "live_bokeh",
            title = "Kromatik Işık Küreleri",
            category = WallpaperCategory.LIVE,
            sourceType = WallpaperSourceType.LIVE_ANIMATED,
            liveType = LiveWallpaperType.DYNAMIC_BOKEH,
            gradientColors = listOf(0xFF140812, 0xFFFF4081, 0xFFFF9800, 0xFF0A0208),
            badge = "Canlı 60FPS",
            description = "Arka planda süzülen yumuşak odak dışı renkli bokeh küreleri ve atmosferik ışık parlamaları."
        ),

        // -------------------------------------------------------------
        // 2. SYSTEM NATIVE ANDROID WALLPAPER
        // -------------------------------------------------------------
        WallpaperDefinition(
            id = "system_default",
            title = "Telefon Arka Planı (Sistem)",
            category = WallpaperCategory.SYSTEM,
            sourceType = WallpaperSourceType.SYSTEM_NATIVE,
            staticType = WallpaperType.DEVICE_SYSTEM,
            gradientColors = listOf(0xFF1E88E5, 0xFF43A047),
            badge = "Varsayılan",
            description = "Android cihazınızın kendi orijinal sistem duvar kağıdını doğrudan yansıtır."
        ),

        // -------------------------------------------------------------
        // 3. STATIC LANDSCAPE & ART PRESETS
        // -------------------------------------------------------------
        WallpaperDefinition(
            id = "static_aurora",
            title = "Aurora Borealis",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.AURORA,
            gradientColors = listOf(0xFF0F2027, 0xFF203A43, 0xFF2C5364),
            badge = "Ultra HD",
            description = "Muhteşem İskandinav gökyüzü kuzey ışıkları ve dağ silüeti."
        ),
        WallpaperDefinition(
            id = "static_cyber",
            title = "Cyber City Neon",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.CYBERPUNK,
            gradientColors = listOf(0xFF1F0036, 0xFF990066, 0xFFFF007F),
            badge = "HDR",
            description = "Fütüristik neon ışıklı gökdelenler ve gece şehri estetiği."
        ),
        WallpaperDefinition(
            id = "static_abstract",
            title = "Material Wave",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.ABSTRACT,
            gradientColors = listOf(0xFF134E5E, 0xFF71B280),
            badge = "Material You",
            description = "Akıcı organik formlar ve harmonik Material Design dalgaları."
        ),
        WallpaperDefinition(
            id = "static_space",
            title = "Deep Nebula",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.DEEP_SPACE,
            gradientColors = listOf(0xFF070414, 0xFF1B0F38, 0xFF2E1065, 0xFF030014),
            badge = "AMOLED",
            description = "Derin kozmik nebula bulutları ve kristal yıldız kümeleri."
        ),
        WallpaperDefinition(
            id = "static_forest",
            title = "Forest Mist",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.FOREST_MIST,
            gradientColors = listOf(0xFF0A1F12, 0xFF143820, 0xFF1E5230, 0xFF05120A),
            badge = "Doğa",
            description = "Huzurlu sisli çam ormanı ve zümrüt yeşili tonlar."
        ),
        WallpaperDefinition(
            id = "static_ocean",
            title = "Ocean Sunrise",
            category = WallpaperCategory.STATIC,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.OCEAN_SUNRISE,
            gradientColors = listOf(0xFF08182B, 0xFF0F3254, 0xFF9E4822, 0xFFD97736),
            badge = "Gün Doğumu",
            description = "Sakin deniz üzerinde sıcak altın ve mercan gün doğumu ufku."
        ),

        // -------------------------------------------------------------
        // 4. MINIMAL GRADIENT & AMOLED PRESETS
        // -------------------------------------------------------------
        WallpaperDefinition(
            id = "grad_velvet",
            title = "Velvet Gradient",
            category = WallpaperCategory.GRADIENTS,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.MINIMAL_GRADIENT,
            gradientColors = listOf(0xFF1C0A18, 0xFF4A1035, 0xFF7A184D, 0xFF0D030B),
            badge = "Minimal",
            description = "Lüks kadife moru ve sıcak fuşya geçişli minimalist gradyan."
        ),
        WallpaperDefinition(
            id = "grad_obsidian",
            title = "Karanlık Obsidyen",
            category = WallpaperCategory.GRADIENTS,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.DARK_OBSIDIAN,
            gradientColors = listOf(0xFF000000, 0xFF121212, 0xFF1E1E1E, 0xFF0A0A0A),
            badge = "AMOLED Saf Siyah",
            description = "Pil tasarrufu sağlayan saf siyah ve füme tonlu zarif arka plan."
        ),
        WallpaperDefinition(
            id = "grad_sunset",
            title = "Sunset Boulevard",
            category = WallpaperCategory.GRADIENTS,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.SUNSET_BLVD,
            gradientColors = listOf(0xFF2C0B3B, 0xFF8B2635, 0xFFE0533C, 0xFFF99F44),
            badge = "Sıcak",
            description = "Alacakaranlık gökyüzünün büyüleyici turuncu ve mor renk paleti."
        ),
        WallpaperDefinition(
            id = "grad_neon",
            title = "Neon Geometri",
            category = WallpaperCategory.GRADIENTS,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.NEON_GEOMETRY,
            gradientColors = listOf(0xFF0B132B, 0xFF1C2541, 0xFF3A506B, 0xFF5BC0BE),
            badge = "Modern",
            description = "Fütüristik koyu lacivert ve parlayan akuamarin minimal hatlar."
        ),
        WallpaperDefinition(
            id = "grad_pastel",
            title = "Kozmik Pastel",
            category = WallpaperCategory.GRADIENTS,
            sourceType = WallpaperSourceType.STATIC_PRESET,
            staticType = WallpaperType.COSMIC_PASTEL,
            gradientColors = listOf(0xFF2D1B4E, 0xFF563D7C, 0xFF8E6FB8, 0xFFD8B4E2),
            badge = "Pastel",
            description = "Yumuşak lavanta ve leylak tonlarında rüya gibi degrade geçiş."
        )
    )

    fun findById(id: String): WallpaperDefinition {
        return ALL_WALLPAPERS.firstOrNull { it.id == id } ?: ALL_WALLPAPERS.first()
    }

    fun findByWallpaperType(type: WallpaperType): WallpaperDefinition {
        return ALL_WALLPAPERS.firstOrNull { it.staticType == type && it.sourceType == WallpaperSourceType.STATIC_PRESET }
            ?: ALL_WALLPAPERS.firstOrNull { it.staticType == type }
            ?: ALL_WALLPAPERS.first()
    }
}
