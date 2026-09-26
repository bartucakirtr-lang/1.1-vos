@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.LocalIsDarkMode
import com.example.ui.theme.LocalThemeController
import com.example.viewmodel.OSViewModel

@Composable
fun SettingsApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val themePalette by viewModel.themePalette.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val navigationMode by viewModel.navigationMode.collectAsState()
    val screenLockType by viewModel.screenLockType.collectAsState()
    val clockStyle by viewModel.clockStyle.collectAsState()
    val currentWallpaper by viewModel.currentWallpaper.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val isDeveloperModeUnlocked by viewModel.isDeveloperModeUnlocked.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeSubpage by remember { mutableStateOf<String?>(null) }
    var devEggTaps by remember { mutableIntStateOf(0) }
    var showEasterEggDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeSubpage ?: "Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeSubpage != null) {
                                activeSubpage = null
                            } else {
                                viewModel.navigateHome()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeSubpage) {
                "Wallpaper & Style" -> {
                    WallpaperAndStyleSubpage(
                        currentPalette = themePalette,
                        themeMode = themeMode,
                        isDark = isDarkMode,
                        currentWallpaper = currentWallpaper,
                        clockStyle = clockStyle,
                        onPaletteSelect = { viewModel.setThemePalette(it) },
                        onThemeModeSelect = { viewModel.setThemeMode(it) },
                        onDarkModeToggle = { viewModel.toggleDarkMode() },
                        onWallpaperSelect = { viewModel.setWallpaper(it) },
                        onClockStyleSelect = { viewModel.setClockStyle(it) }
                    )
                }
                "Display & Navigation" -> {
                    DisplayNavigationSubpage(
                        navMode = navigationMode,
                        brightness = brightness,
                        onNavModeChange = { viewModel.setNavigationMode(it) },
                        onBrightnessChange = { viewModel.setBrightness(it) }
                    )
                }
                "Security & Lock Screen" -> {
                    SecuritySubpage(
                        lockType = screenLockType,
                        onLockTypeChange = { viewModel.setLockType(it) },
                        onLockNow = { viewModel.lockPhone() }
                    )
                }
                "Battery & Performance" -> {
                    BatterySubpage(viewModel = viewModel)
                }
                "Storage & Memory" -> {
                    StorageSubpage()
                }
                "Language & Input" -> {
                    LanguagesSubpage(viewModel = viewModel)
                }
                "About vos" -> {
                    AboutPhoneSubpage(
                        viewModel = viewModel,
                        onBuildNumberClick = {
                            devEggTaps++
                            if (devEggTaps >= 7) {
                                viewModel.unlockDeveloperMode()
                                showEasterEggDialog = true
                                devEggTaps = 0
                            }
                        }
                    )
                }
                "Developer Options" -> {
                    DeveloperOptionsSubpage(viewModel = viewModel)
                }
                else -> {
                    // Main Settings Categories List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Profile Card
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openApp(AppId.ACCOUNT) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(userAccount.avatarColor),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(userAccount.avatarEmoji, fontSize = 26.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${userAccount.firstName} ${userAccount.lastName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "${userAccount.email} • Cloud Active",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = "Open Account",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        item {
                            SettingsCategoryHeader("Personalization & Appearance")
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.Palette,
                                title = "Wallpaper & Style",
                                subtitle = "${if (isDarkMode) "Dark Mode" else "Light Mode"} • ${themePalette.displayName}",
                                onClick = { activeSubpage = "Wallpaper & Style" }
                            )
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.DisplaySettings,
                                title = "Display & Navigation",
                                subtitle = "Gesture vs 3-Button, Brightness & Scaling",
                                onClick = { activeSubpage = "Display & Navigation" }
                            )
                        }

                        item {
                            SettingsCategoryHeader("System & Privacy")
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.Lock,
                                title = "Security & Screen Lock",
                                subtitle = "PIN Lock, Biometrics & Shortcuts",
                                onClick = { activeSubpage = "Security & Lock Screen" }
                            )
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.BatteryChargingFull,
                                title = "Battery & Power",
                                subtitle = "$batteryLevel% • ${if (isCharging) "Charging" else "Normal Usage"}",
                                onClick = { activeSubpage = "Battery & Performance" }
                            )
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.Storage,
                                title = "Storage & Memory",
                                subtitle = "48.2 GB used of 256 GB (18% full)",
                                onClick = { activeSubpage = "Storage & Memory" }
                            )
                        }

                        item {
                            SettingsCategoryHeader("System & Developer")
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.Language,
                                title = "Language & Input",
                                subtitle = "Active: $currentLang (40 Languages Supported)",
                                onClick = { activeSubpage = "Language & Input" }
                            )
                        }

                        item {
                            SettingsRowItem(
                                icon = Icons.Filled.Info,
                                title = "About vos",
                                subtitle = "vos (Baklava) • GitHub Config Check",
                                onClick = { activeSubpage = "About vos" }
                            )
                        }

                        if (isDeveloperModeUnlocked) {
                            item {
                                SettingsRowItem(
                                    icon = Icons.Filled.Code,
                                    title = "{ } Developer Options",
                                    subtitle = "USB Debugging, FPS HUD, Performance Diagnostics",
                                    onClick = { activeSubpage = "Developer Options" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEasterEggDialog) {
        AlertDialog(
            onDismissRequest = { showEasterEggDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD54F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Developer Options Unlocked!")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎉 You found the Android 16 (Baklava) Developer Easter Egg!")
                    Text("NovaOS virtualizer is running in hypervisor mode with zero frame drops.")
                }
            },
            confirmButton = {
                Button(onClick = { showEasterEggDialog = false }) {
                    Text("Awesome")
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 2.dp)
    )
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun WallpaperAndStyleSubpage(
    currentPalette: ThemePalette,
    themeMode: ThemeMode,
    isDark: Boolean,
    currentWallpaper: WallpaperType,
    clockStyle: ClockStyle,
    onPaletteSelect: (ThemePalette) -> Unit,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onDarkModeToggle: () -> Unit,
    onWallpaperSelect: (WallpaperType) -> Unit,
    onClockStyleSelect: (ClockStyle) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Theme Mode (Light / Dark / Follow System)
        item {
            Text("Appearance Theme Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Controls theme across all OS Forge apps, system bars, and surfaces",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = themeMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onThemeModeSelect(mode) },
                        leadingIcon = {
                            Icon(
                                when (mode) {
                                    ThemeMode.LIGHT -> Icons.Filled.LightMode
                                    ThemeMode.DARK -> Icons.Filled.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Filled.SettingsBrightness
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(mode.title, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Dark Theme Quick Switch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dark Theme", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isDark) "Night palette active (battery & eye comfort)" else "Light palette active (high daytime clarity)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    Switch(checked = isDark, onCheckedChange = { onDarkModeToggle() })
                }
            }
        }

        item {
            HorizontalDivider()
        }

        // 3. Material You Dynamic Color
        item {
            Text("Material You Dynamic Color", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ThemePalette.entries.forEach { palette ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onPaletteSelect(palette) }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(palette.primaryHex),
                            border = if (currentPalette == palette) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                            modifier = Modifier.size(46.dp)
                        ) {}
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(palette.displayName.split(" ").first(), fontSize = 10.sp, fontWeight = if (currentPalette == palette) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        item {
            HorizontalDivider()
        }

        // 4. Live Palette Theme Preview
        item {
            Text("Live Theme Color Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ThemeColorSample("Primary", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
                    ThemeColorSample("Secondary", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.onSecondary)
                    ThemeColorSample("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
                    ThemeColorSample("Variant", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            Text("System Wallpapers", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                WallpaperType.entries.take(4).forEach { wp ->
                    Card(
                        onClick = { onWallpaperSelect(wp) },
                        shape = RoundedCornerShape(14.dp),
                        border = if (currentWallpaper == wp) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(wp.title.split(" ").first(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            Text("Lock Screen Clock Style", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ClockStyle.entries.forEach { style ->
                    FilterChip(
                        selected = clockStyle == style,
                        onClick = { onClockStyleSelect(style) },
                        label = { Text(style.name.replace("_", " "), fontSize = 11.sp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorSample(
    label: String,
    bg: Color,
    fg: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = bg,
            modifier = Modifier.size(36.dp),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Aa", color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DisplayNavigationSubpage(
    navMode: NavMode,
    brightness: Float,
    onNavModeChange: (NavMode) -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Navigation Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        Card(
            onClick = { onNavModeChange(NavMode.GESTURE) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (navMode == NavMode.GESTURE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = navMode == NavMode.GESTURE, onClick = { onNavModeChange(NavMode.GESTURE) })
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Gesture Navigation", fontWeight = FontWeight.Bold)
                    Text("Swipe up for home, hold for recents, swipe pill to switch apps", fontSize = 12.sp)
                }
            }
        }

        Card(
            onClick = { onNavModeChange(NavMode.THREE_BUTTON) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (navMode == NavMode.THREE_BUTTON) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = navMode == NavMode.THREE_BUTTON, onClick = { onNavModeChange(NavMode.THREE_BUTTON) })
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("3-Button Navigation", fontWeight = FontWeight.Bold)
                    Text("Back, Home, and Recents buttons at bottom", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Screen Brightness", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Slider(value = brightness, onValueChange = onBrightnessChange)
    }
}

@Composable
private fun SecuritySubpage(
    lockType: LockType,
    onLockTypeChange: (LockType) -> Unit,
    onLockNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Screen Lock Type", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        LockType.entries.forEach { type ->
            Card(
                onClick = { onLockTypeChange(type) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lockType == type) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = lockType == type, onClick = { onLockTypeChange(type) })
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(type.name, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onLockNow,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Filled.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Lock Device Now")
        }
    }
}

@Composable
private fun BatterySubpage(viewModel: OSViewModel) {
    val level by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val statusText by viewModel.batteryStatusText.collectAsState()
    val health by viewModel.batteryHealth.collectAsState()
    val temp by viewModel.batteryTemperature.collectAsState()
    val voltage by viewModel.batteryVoltage.collectAsState()
    val pluggedType by viewModel.batteryPluggedType.collectAsState()
    val tech by viewModel.batteryTechnology.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                            contentDescription = "Battery",
                            tint = if (isCharging) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "$level%",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = statusText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(onClick = { viewModel.toggleCharging() }) {
                        Text(if (isCharging) "Disconnect Charger" else "Connect Charger ⚡")
                    }
                }
            }
        }

        item { SettingsCategoryHeader("Hardware Telemetry") }

        item {
            ListItem(
                headlineContent = { Text("Power Source") },
                supportingContent = { Text(pluggedType) },
                leadingContent = { Icon(Icons.Filled.Power, contentDescription = null) }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Battery Health") },
                supportingContent = { Text(health) },
                leadingContent = { Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color(0xFFFF1744)) }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Thermal Temperature") },
                supportingContent = { Text("${String.format("%.1f", temp)} °C (${if (temp > 40f) "Warm" else "Optimal"})") },
                leadingContent = { Icon(Icons.Filled.Thermostat, contentDescription = null) }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Operating Voltage") },
                supportingContent = { Text("$voltage mV") },
                leadingContent = { Icon(Icons.Filled.FlashOn, contentDescription = null) }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Cell Technology") },
                supportingContent = { Text(tech) },
                leadingContent = { Icon(Icons.Filled.Memory, contentDescription = null) }
            )
        }

        item { SettingsCategoryHeader("Power Optimization") }

        item {
            ListItem(
                headlineContent = { Text("Battery Saver Mode") },
                supportingContent = { Text("Reduce background activity and extend battery life") },
                leadingContent = { Icon(Icons.Filled.BatterySaver, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = batterySaver,
                        onCheckedChange = { viewModel.toggleBatterySaver() }
                    )
                }
            )
        }
    }
}

@Composable
private fun StorageSubpage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("48.2 GB of 256 GB used", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.18f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        ListItem(headlineContent = { Text("System & Kernel") }, supportingContent = { Text("14.2 GB") })
        ListItem(headlineContent = { Text("Apps & Games") }, supportingContent = { Text("22.5 GB") })
        ListItem(headlineContent = { Text("Images & Photos") }, supportingContent = { Text("6.8 GB") })
        ListItem(headlineContent = { Text("Audio & Music") }, supportingContent = { Text("4.7 GB") })
    }
}

@Composable
private fun AboutPhoneSubpage(
    viewModel: OSViewModel,
    onBuildNumberClick: () -> Unit
) {
    val remoteVersion by viewModel.remoteConfigVersion.collectAsState()
    val remoteStatus by viewModel.remoteConfigStatus.collectAsState()
    val isDark = LocalIsDarkMode.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dynamic System Update & Version Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "System Update Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    val isUpToDate = remoteVersion == "1.1" || remoteVersion == "1.1.0"
                    Text(
                        text = if (isUpToDate) "vos is Up to Date (v$remoteVersion)" else "Configuration Available (v$remoteVersion)",
                        fontSize = 13.sp,
                        color = if (isUpToDate) Color(0xFF00C853) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your device continuously monitors GitHub repo config.json for live OTA updates.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Refresh / Fetch Actions
        item {
            Button(
                onClick = { viewModel.fetchRemoteConfigVersion() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Check for Configuration Updates", fontWeight = FontWeight.Bold)
            }
        }

        // 3. GitHub Connection Card Details
        item {
            Text(
                text = "GITHUB CONNECT OVERVIEW",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Hub, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("GitHub Repository", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("bartucakirtr-lang/1.1-vos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, null, tint = Color(0xFF00C853), modifier = Modifier.size(20.dp))
                            Column {
                                Text("Config Sync Status", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(remoteStatus, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // 4. Device Details Specs
        item {
            Text(
                text = "DEVICE SPECIFICATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Device Name") },
                        supportingContent = { Text("vos Pixel Edition") },
                        leadingContent = { Icon(Icons.Default.Smartphone, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    ListItem(
                        headlineContent = { Text("Android Version") },
                        supportingContent = { Text("Android Baklava (16.0)") },
                        leadingContent = { Icon(Icons.Default.Android, null, tint = Color(0xFF3DDC84)) }
                    )
                    ListItem(
                        headlineContent = { Text("Build Version") },
                        supportingContent = { Text("VOS-REL (Tap 7 times for Easter Egg)") },
                        leadingContent = { Icon(Icons.Default.Construction, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable { onBuildNumberClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperOptionsSubpage(viewModel: OSViewModel) {
    val usbDebugging by viewModel.usbDebuggingEnabled.collectAsState()
    val showFps by viewModel.showFpsHud.collectAsState()
    val showTouches by viewModel.showTouchPointer.collectAsState()
    val animScale by viewModel.animatorScale.collectAsState()
    val strictMode by viewModel.strictModeEnabled.collectAsState()
    val isDevUnlocked by viewModel.isDeveloperModeUnlocked.collectAsState()
    val remoteVersion by viewModel.remoteConfigVersion.collectAsState()
    val remoteStatus by viewModel.remoteConfigStatus.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("{ } Developer Options", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(if (isDevUnlocked) "Developer Mode Active" else "Disabled", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isDevUnlocked,
                        onCheckedChange = { viewModel.setDeveloperModeUnlocked(it) }
                    )
                }
            }
        }

        item { SettingsCategoryHeader("Debugging & Bridge") }

        item {
            ListItem(
                headlineContent = { Text("USB Debugging") },
                supportingContent = { Text("Enable ADB command bridge & hypervisor protocol") },
                trailingContent = {
                    Switch(checked = usbDebugging, onCheckedChange = { viewModel.toggleUsbDebugging() })
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Show Performance & FPS Overlay") },
                supportingContent = { Text("Display real-time frame rate (60 FPS) and memory pressure") },
                trailingContent = {
                    Switch(checked = showFps, onCheckedChange = { viewModel.toggleFpsHud() })
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Show Pointer Input Visualizer") },
                supportingContent = { Text("Display touch ripple feedback on screen interactions") },
                trailingContent = {
                    Switch(checked = showTouches, onCheckedChange = { viewModel.toggleTouchPointer() })
                }
            )
        }

        item { SettingsCategoryHeader("Animation & Rendering Scale") }

        item {
            ListItem(
                headlineContent = { Text("Window Animation Scale") },
                supportingContent = { Text("Current scale: ${animScale}x") },
                trailingContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0.5f, 1.0f, 1.5f, 2.0f).forEach { scale ->
                            FilterChip(
                                selected = animScale == scale,
                                onClick = { viewModel.setAnimatorScale(scale) },
                                label = { Text("${scale}x", fontSize = 10.sp) }
                            )
                        }
                    }
                }
            )
        }

        item { SettingsCategoryHeader("System Diagnostics") }

        item {
            ListItem(
                headlineContent = { Text("Strict Mode Diagnostics") },
                supportingContent = { Text("Detect accidental main thread disk or network I/O") },
                trailingContent = {
                    Switch(checked = strictMode, onCheckedChange = { viewModel.toggleStrictMode() })
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("GitHub Config.json Inspector") },
                supportingContent = { Text("Repository: bartucakirtr-lang/1.1-vos\nSync Status: $remoteStatus") }
            )
        }

        item {
            Button(
                onClick = { viewModel.sendTestNotification() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Trigger Test Dev Notification")
            }
        }
    }
}

@Composable
private fun LanguagesSubpage(viewModel: OSViewModel) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val languagesList = remember {
        listOf(
            Pair("English", "English (United States) 🇺🇸"),
            Pair("Türkçe", "Turkish (Türkiye) 🇹🇷"),
            Pair("Español", "Spanish (España / Latinoamerica) 🇪🇸"),
            Pair("Français", "French (France) 🇫🇷"),
            Pair("Deutsch", "German (Deutschland) 🇩🇪"),
            Pair("Italiano", "Italian (Italia) 🇮🇹"),
            Pair("Português", "Portuguese (Brasil / Portugal) 🇧🇷"),
            Pair("Русский", "Russian (Россия) 🇷🇺"),
            Pair("中文 (简体)", "Chinese Simplified (中国) 🇨🇳"),
            Pair("中文 (繁體)", "Chinese Traditional (台灣 / 香港) 🇹🇼"),
            Pair("日本語", "Japanese (日本) 🇯🇵"),
            Pair("한국어", "Korean (대한민국) 🇰🇷"),
            Pair("العربية", "Arabic (العالم العربي) 🇸🇦"),
            Pair("हिन्दी", "Hindi (भारत) 🇮🇳"),
            Pair("বাংলা", "Bengali (বাংলাদেশ / भारत) 🇧🇩"),
            Pair("Bahasa Indonesia", "Indonesian (Indonesia) 🇮🇩"),
            Pair("Tiếng Việt", "Vietnamese (Việt Nam) 🇻🇳"),
            Pair("Polski", "Polish (Polska) 🇵🇱"),
            Pair("Nederlands", "Dutch (Nederland) 🇳🇱"),
            Pair("Українська", "Ukrainian (Україна) 🇺🇦"),
            Pair("Română", "Romanian (România) 🇷🇴"),
            Pair("Ελληνικά", "Greek (Ελλάδα) 🇬🇷"),
            Pair("Čeština", "Czech (Česká republika) 🇨🇿"),
            Pair("Svenska", "Swedish (Sverige) 🇸🇪"),
            Pair("Magyar", "Hungarian (Magyarország) 🇭🇺"),
            Pair("Dansk", "Danish (Danmark) 🇩🇰"),
            Pair("Suomi", "Finnish (Suomi) 🇫🇮"),
            Pair("Norsk", "Norwegian (Norge) 🇳🇴"),
            Pair("ไทย", "Thai (ประเทศไทย) 🇹🇭"),
            Pair("עברית", "Hebrew (ישראל) 🇮🇱"),
            Pair("Bahasa Melayu", "Malay (Malaysia) 🇲🇾"),
            Pair("Tagalog", "Filipino / Tagalog (Pilipinas) 🇵🇭"),
            Pair("Kiswahili", "Swahili (East Africa) 🇰🇪"),
            Pair("فارسی", "Persian (ایران) 🇮🇷"),
            Pair("اردو", "Urdu (پاکستان) 🇵🇰"),
            Pair("தமிழ்", "Tamil (இலங்கை / இந்தியா) 🇮🇳"),
            Pair("తెలుగు", "Telugu (భారతదేశం) 🇮🇳"),
            Pair("मराठी", "Marathi (भारत) 🇮🇳"),
            Pair("Hrvatski", "Croatian (Hrvatska) 🇭🇷"),
            Pair("Català", "Catalan (Catalunya) 🇪🇸")
        )
    }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) languagesList
        else languagesList.filter {
            it.first.contains(searchQuery, ignoreCase = true) ||
                    it.second.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search 40 Languages") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Language", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(currentLang, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "40 Languages Available",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        items(filteredLanguages, key = { it.first }) { (langName, langDetails) ->
            val isSelected = currentLang.equals(langName, ignoreCase = true)
            Card(
                onClick = { viewModel.setLanguage(langName) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(langName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(langDetails, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
