package com.example.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.system.getAppIcon
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.launch

data class ThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val palette: ThemePalette,
    val wallpaper: WallpaperType,
    val isDark: Boolean,
    val iconStyle: IconStyle,
    val clockStyle: ClockStyle,
    val gradientColors: List<Color>,
    val badge: String = "POPÜLER"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateHome()
    }

    val isDark = LocalIsDarkMode.current
    val currentPalette by viewModel.themePalette.collectAsState()
    val currentWallpaper by viewModel.currentWallpaper.collectAsState()
    val currentIconStyle by viewModel.iconStyle.collectAsState()
    val currentClockStyle by viewModel.clockStyle.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        "✨ Hazır Temalar",
        "🎨 Renk Paleti",
        "🖼️ Duvar Kağıdı",
        "🔲 Simge Stili",
        "🕒 Kilit Saati"
    )

    val themePresets = remember {
        listOf(
            ThemePreset(
                id = "cyberpunk",
                title = "Siberpunk Neon 2077",
                subtitle = "Fütüristik Gece & Canlı Neon",
                description = "Yüksek kontrastlı siber neon renkler, altıgen ikonik form ve gece manzarası.",
                palette = ThemePalette.CYBERPUNK_NEON,
                wallpaper = WallpaperType.CYBERPUNK,
                isDark = true,
                iconStyle = IconStyle.HEXAGON,
                clockStyle = ClockStyle.DUAL_COLOR,
                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFFFF007F)),
                badge = "ÖNE ÇIKAN"
            ),
            ThemePreset(
                id = "vanilla_android",
                title = "Saf Android 16 (Vanilla)",
                subtitle = "Material You Dynamic Core",
                description = "Google Pixel esintili doğal yeşil ve mavi tonlar, yumuşak squircle simgeler.",
                palette = ThemePalette.ANDROID_GREEN,
                wallpaper = WallpaperType.ABSTRACT,
                isDark = false,
                iconStyle = IconStyle.SQUIRCLE,
                clockStyle = ClockStyle.PIXEL_BOLD,
                gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF00B0FF)),
                badge = "RESMİ"
            ),
            ThemePreset(
                id = "deep_space",
                title = "OLED Derin Uzay",
                subtitle = "Sonsuz Siyah & Kozmik Nebula",
                description = "Tam siyah AMOLED tasarruflu arka plan, minimal daire simgeler ve saf sadelik.",
                palette = ThemePalette.MONOCHROME,
                wallpaper = WallpaperType.DEEP_SPACE,
                isDark = true,
                iconStyle = IconStyle.CIRCLE,
                clockStyle = ClockStyle.MINIMAL,
                gradientColors = listOf(Color(0xFF37474F), Color(0xFF607D8B)),
                badge = "AMOLED"
            ),
            ThemePreset(
                id = "sunset_breeze",
                title = "Gün Batımı Esintisi",
                subtitle = "Sıcak Amber & Altın Işıltı",
                description = "Göz yormayan sıcak turuncu degrade ve modern buzlu cam efektli simgeler.",
                palette = ThemePalette.SUNSET_ORANGE,
                wallpaper = WallpaperType.OCEAN_SUNRISE,
                isDark = false,
                iconStyle = IconStyle.GLASSMORPHISM,
                clockStyle = ClockStyle.ANALOG,
                gradientColors = listOf(Color(0xFFE65100), Color(0xFFFFB74D)),
                badge = "YENİ"
            ),
            ThemePreset(
                id = "lavender_dream",
                title = "Lavanta Rüyası",
                subtitle = "Zarif Mor & Pastel Işık",
                description = "Pastel lila vurgular, damla biçimli simgeler ve ferahlatıcı aydınlık tasarım.",
                palette = ThemePalette.LAVENDER_PURPLE,
                wallpaper = WallpaperType.AURORA,
                isDark = false,
                iconStyle = IconStyle.TEARDROP,
                clockStyle = ClockStyle.PIXEL_BOLD,
                gradientColors = listOf(Color(0xFF7B1FA2), Color(0xFFE1BEE7)),
                badge = "POPÜLER"
            ),
            ThemePreset(
                id = "ocean_breeze",
                title = "Okyanus Derinliği",
                subtitle = "Turkuaz & Saf Mavi Akış",
                description = "Kristal mavi tonlar, neumorphic 3D simgeler ve dingin arayüz deneyimi.",
                palette = ThemePalette.OCEAN_BLUE,
                wallpaper = WallpaperType.FOREST_MIST,
                isDark = true,
                iconStyle = IconStyle.NEUMORPHIC,
                clockStyle = ClockStyle.DUAL_COLOR,
                gradientColors = listOf(Color(0xFF0288D1), Color(0xFF00E5FF)),
                badge = "ÖZEL"
            )
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Arayüz Temaları",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Vroxen UI Kişiselleştirme & Tema Stüdyosu",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleDarkMode()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (!isDarkMode) "Karanlık Tema Aktif" else "Aydınlık Tema Aktif",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Tema Modu",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.setThemePalette(ThemePalette.OCEAN_BLUE)
                            viewModel.setWallpaper(WallpaperType.AURORA)
                            viewModel.setIconStyle(IconStyle.SQUIRCLE)
                            viewModel.setClockStyle(ClockStyle.PIXEL_BOLD)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Varsayılan Vroxen UI temasına sıfırlandı")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Sıfırla"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live Preview Card (Miniature Phone Preview)
            MiniThemePreviewCard(
                palette = currentPalette,
                wallpaper = currentWallpaper,
                iconStyle = currentIconStyle,
                clockStyle = currentClockStyle,
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                divider = {},
                containerColor = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Presets Tab
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(themePresets) { preset ->
                                val isCurrentActive = currentPalette == preset.palette &&
                                        currentWallpaper == preset.wallpaper &&
                                        currentIconStyle == preset.iconStyle

                                ThemePresetCard(
                                    preset = preset,
                                    isActive = isCurrentActive,
                                    onApply = {
                                        viewModel.setThemePalette(preset.palette)
                                        viewModel.setWallpaper(preset.wallpaper)
                                        viewModel.setIconStyle(preset.iconStyle)
                                        viewModel.setClockStyle(preset.clockStyle)
                                        viewModel.setDarkMode(preset.isDark)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "${preset.title} teması başarıyla uygulandı!",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    1 -> {
                        // Color Palette Tab
                        ColorPaletteSelectorTab(
                            currentPalette = currentPalette,
                            onSelectPalette = { palette ->
                                viewModel.setThemePalette(palette)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${palette.displayName} paleti uygulandı",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                    2 -> {
                        // Wallpaper Tab
                        WallpaperSelectorTab(
                            currentWallpaper = currentWallpaper,
                            onSelectWallpaper = { wp ->
                                viewModel.setWallpaper(wp)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${wp.title} duvar kağıdı ayarlandı",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                    3 -> {
                        // Icon Style Tab
                        IconStyleSelectorTab(
                            currentIconStyle = currentIconStyle,
                            onSelectIconStyle = { style ->
                                viewModel.setIconStyle(style)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${style.title} simge paketi uygulandı",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                    4 -> {
                        // Lock Clock Style Tab
                        ClockStyleSelectorTab(
                            currentClockStyle = currentClockStyle,
                            onSelectClockStyle = { style ->
                                viewModel.setClockStyle(style)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Kilit saati stili güncellendi",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniThemePreviewCard(
    palette: ThemePalette,
    wallpaper: WallpaperType,
    iconStyle: IconStyle,
    clockStyle: ClockStyle,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Screen Simulator
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(palette.primaryHex),
                                Color(palette.secondaryHex)
                            )
                        )
                    )
                    .padding(6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mini Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("12:00", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(Color.White, CircleShape))
                        }
                    }

                    // Mini Clock
                    Text(
                        text = "12:00",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    // Mini App Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(
                                        when (iconStyle) {
                                            IconStyle.CIRCLE -> CircleShape
                                            IconStyle.ROUNDED_SQUARE -> RoundedCornerShape(4.dp)
                                            else -> RoundedCornerShape(6.dp)
                                        }
                                    )
                                    .background(Color.White.copy(alpha = 0.85f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Current Applied Specs
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(palette.primaryHex))
                    )
                    Text(
                        text = "Aktif Arayüz Yapılandırması",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${palette.displayName} • ${if (isDark) "Koyu Mod" else "Açık Mod"}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Duvar Kağıdı: ${wallpaper.title}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                Text(
                    text = "Simgeler: ${iconStyle.title} • Saat: ${clockStyle.name}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun ThemePresetCard(
    preset: ThemePreset,
    isActive: Boolean,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Brush.horizontalGradient(preset.gradientColors))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.35f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = preset.badge,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = preset.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = preset.subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }

                    // Sample Mini Icon Stack
                    Row(
                        horizontalArrangement = Arrangement.spacedBy((-6).dp)
                    ) {
                        repeat(3) { idx ->
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f - (idx * 0.2f)),
                                border = BorderStroke(1.5.dp, Color.White),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (idx) {
                                            0 -> Icons.Default.Palette
                                            1 -> Icons.Default.PhoneAndroid
                                            else -> Icons.Default.AutoAwesome
                                        },
                                        contentDescription = null,
                                        tint = Color(preset.palette.primaryHex),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Body Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = preset.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feature tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(preset.palette.displayName, fontSize = 11.sp) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(preset.palette.primaryHex))
                            )
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(if (preset.isDark) "Koyu Mod" else "Açık Mod", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(
                                if (preset.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                null,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(preset.iconStyle.title.take(10), fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Apply Button
                Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(14.dp),
                    colors = if (isActive) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.DownloadDone,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive) "Şu Anda Aktif" else "Temayı Uygula",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPaletteSelectorTab(
    currentPalette: ThemePalette,
    onSelectPalette: (ThemePalette) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Material You Renk Paletleri",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tüm sistem düğmeleri, rozetleri, bildirimleri ve arayüz ögelerini anında renklendirin.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        }

        items(ThemePalette.entries) { palette ->
            val isSelected = currentPalette == palette
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectPalette(palette) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Double Color Chip
                        Row(
                            horizontalArrangement = Arrangement.spacedBy((-8).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(palette.primaryHex))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(palette.secondaryHex))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }

                        Column {
                            Text(
                                text = palette.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "HEX: #${java.lang.Long.toHexString(palette.primaryHex).takeLast(6).uppercase()} • #${java.lang.Long.toHexString(palette.secondaryHex).takeLast(6).uppercase()}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectPalette(palette) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperSelectorTab(
    currentWallpaper: WallpaperType,
    onSelectWallpaper: (WallpaperType) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(WallpaperType.entries) { wp ->
            val isSelected = currentWallpaper == wp
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clickable { onSelectWallpaper(wp) }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Wallpaper Gradient Art
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                when (wp) {
                                    WallpaperType.DEVICE_SYSTEM -> Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF4CAF50)))
                                    WallpaperType.AURORA -> Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
                                    WallpaperType.CYBERPUNK -> Brush.verticalGradient(listOf(Color(0xFF1F0036), Color(0xFF990066), Color(0xFFFF007F)))
                                    WallpaperType.ABSTRACT -> Brush.verticalGradient(listOf(Color(0xFF134E5E), Color(0xFF71B280)))
                                    WallpaperType.DEEP_SPACE -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF141E30), Color(0xFF243B55)))
                                    WallpaperType.FOREST_MIST -> Brush.verticalGradient(listOf(Color(0xFF1D2B64), Color(0xFFF8CDDA)))
                                    WallpaperType.OCEAN_SUNRISE -> Brush.verticalGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476)))
                                    WallpaperType.MINIMAL_GRADIENT -> Brush.verticalGradient(listOf(Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121)))
                                    else -> Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)))
                                }
                            )
                    )

                    // Overlay Title & Badge
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = wp.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isSelected) "Şu Anda Aktif" else "Uygulamak İçin Dokun",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconStyleSelectorTab(
    currentIconStyle: IconStyle,
    onSelectIconStyle: (IconStyle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Simge Paketleri ve Şekilleri",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Ana ekran ve menüdeki tüm uygulama simgelerinin geometri ve ışıklandırmasını seçin.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        }

        items(IconStyle.entries) { style ->
            val isSelected = currentIconStyle == style
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectIconStyle(style) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Live Icon Sample
                        Surface(
                            shape = when (style) {
                                IconStyle.CIRCLE -> CircleShape
                                IconStyle.ROUNDED_SQUARE -> RoundedCornerShape(8.dp)
                                IconStyle.TEARDROP -> RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                IconStyle.HEXAGON -> RoundedCornerShape(6.dp)
                                else -> RoundedCornerShape(12.dp)
                            },
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = style.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = style.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectIconStyle(style) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClockStyleSelectorTab(
    currentClockStyle: ClockStyle,
    onSelectClockStyle: (ClockStyle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Kilit Ekranı Saat Tasarımı",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Kilit ekranında gösterilecek saat ve tarih tipografisini kişiselleştirin.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )
        }

        items(ClockStyle.entries) { style ->
            val isSelected = currentClockStyle == style
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectClockStyle(style) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (style) {
                                ClockStyle.PIXEL_BOLD -> "Pixel Kalın Dijital Saat"
                                ClockStyle.MINIMAL -> "Minimalist Dijital Saat"
                                ClockStyle.ANALOG -> "Klasik Analog Saat"
                                ClockStyle.DUAL_COLOR -> "Çift Renkli Fütüristik Saat"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stil Kodu: ${style.name}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelectClockStyle(style) }
                    )
                }
            }
        }
    }
}
