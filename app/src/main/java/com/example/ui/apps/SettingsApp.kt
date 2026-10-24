@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import com.example.R
import com.example.model.*
import com.example.util.toBitmapOrNull
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.theme.LocalIsDarkMode
import com.example.ui.theme.LocalThemeController
import com.example.ui.theme.MaterialYouThemeEngine
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
    val showSystemBars by viewModel.showSystemBars.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val initialSubpage by viewModel.settingsInitialSubpage.collectAsState()
    val context = LocalContext.current

    var activeSubpage by remember { mutableStateOf<String?>(null) }
    var devEggTaps by remember { mutableIntStateOf(0) }
    var showEasterEggDialog by remember { mutableStateOf(false) }
    var showIconStyleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(initialSubpage) {
        if (initialSubpage != null) {
            activeSubpage = initialSubpage
            viewModel.clearSettingsInitialSubpage()
        }
    }

    Scaffold(
        topBar = {
            if (activeSubpage != "System Update") {
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
                            Icon(
                                if (activeSubpage != null) Icons.AutoMirrored.Filled.ArrowBack else Icons.Filled.Home,
                                contentDescription = if (activeSubpage != null) "Back" else "Home"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        modifier = modifier
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (activeSubpage == "System Update") PaddingValues(0.dp) else padding)
        ) {
            val isTablet = maxWidth >= 700.dp

            if (isTablet) {
                // ================= TABLET LIST-DETAIL TWO-PANE LAYOUT =================
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: Navigation Sidebar (340dp)
                    Box(
                        modifier = Modifier
                            .width(340.dp)
                            .fillMaxHeight()
                    ) {
                        SettingsMainCategoriesList(
                            viewModel = viewModel,
                            activeSubpage = activeSubpage,
                            onSelectSubpage = { activeSubpage = it },
                            dndEnabled = dndEnabled,
                            iconStyle = iconStyle,
                            themePalette = themePalette,
                            isDarkMode = isDarkMode,
                            batteryLevel = batteryLevel,
                            isCharging = isCharging,
                            currentLang = currentLang,
                            isDeveloperModeUnlocked = isDeveloperModeUnlocked,
                            userAccount = userAccount,
                            onOpenIconStyleDialog = { showIconStyleDialog = true }
                        )
                    }

                    VerticalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxHeight()
                    )

                    // Right Pane: Active Detail Subpage or Tablet Settings Hub
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        if (activeSubpage != null) {
                            SettingsSubpageContent(
                                subpage = activeSubpage!!,
                                viewModel = viewModel,
                                themePalette = themePalette,
                                themeMode = themeMode,
                                isDarkMode = isDarkMode,
                                currentWallpaper = currentWallpaper,
                                clockStyle = clockStyle,
                                navigationMode = navigationMode,
                                brightness = brightness,
                                showSystemBars = showSystemBars,
                                screenLockType = screenLockType,
                                onBack = { activeSubpage = null },
                                onEasterEgg = {
                                    devEggTaps++
                                    if (devEggTaps >= 7) {
                                        viewModel.unlockDeveloperMode()
                                        showEasterEggDialog = true
                                        devEggTaps = 0
                                    }
                                }
                            )
                        } else {
                            TabletSettingsHub(
                                viewModel = viewModel,
                                onSelectSubpage = { activeSubpage = it }
                            )
                        }
                    }
                }
            } else {
                // ================= PHONE SINGLE-PANE LAYOUT =================
                if (activeSubpage != null) {
                    SettingsSubpageContent(
                        subpage = activeSubpage!!,
                        viewModel = viewModel,
                        themePalette = themePalette,
                        themeMode = themeMode,
                        isDarkMode = isDarkMode,
                        currentWallpaper = currentWallpaper,
                        clockStyle = clockStyle,
                        navigationMode = navigationMode,
                        brightness = brightness,
                        showSystemBars = showSystemBars,
                        screenLockType = screenLockType,
                        onBack = { activeSubpage = null },
                        onEasterEgg = {
                            devEggTaps++
                            if (devEggTaps >= 7) {
                                viewModel.unlockDeveloperMode()
                                showEasterEggDialog = true
                                devEggTaps = 0
                            }
                        }
                    )
                } else {
                    SettingsMainCategoriesList(
                        viewModel = viewModel,
                        activeSubpage = null,
                        onSelectSubpage = { activeSubpage = it },
                        dndEnabled = dndEnabled,
                        iconStyle = iconStyle,
                        themePalette = themePalette,
                        isDarkMode = isDarkMode,
                        batteryLevel = batteryLevel,
                        isCharging = isCharging,
                        currentLang = currentLang,
                        isDeveloperModeUnlocked = isDeveloperModeUnlocked,
                        userAccount = userAccount,
                        onOpenIconStyleDialog = { showIconStyleDialog = true }
                    )
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

    if (showIconStyleDialog) {
        com.example.ui.system.IconStyleDialog(
            viewModel = viewModel,
            onDismiss = { showIconStyleDialog = false }
        )
    }
}

@Composable
private fun SettingsSubpageContent(
    subpage: String,
    viewModel: OSViewModel,
    themePalette: ThemePalette,
    themeMode: ThemeMode,
    isDarkMode: Boolean,
    currentWallpaper: WallpaperType,
    clockStyle: ClockStyle,
    navigationMode: NavMode,
    brightness: Float,
    showSystemBars: Boolean,
    screenLockType: LockType,
    onBack: () -> Unit,
    onEasterEgg: () -> Unit
) {
    when (subpage) {
        "Do Not Disturb", "Rahatsız Etmeyin" -> {
            DoNotDisturbSubpage(viewModel = viewModel)
        }
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
                onClockStyleSelect = { viewModel.setClockStyle(it) },
                onOpenWallpaperManager = { viewModel.openApp(AppId.WALLPAPERS) }
            )
        }
        "Display & Navigation" -> {
            DisplayNavigationSubpage(
                navMode = navigationMode,
                brightness = brightness,
                showSystemBars = showSystemBars,
                onToggleSystemBars = { viewModel.toggleSystemBars() },
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
        "System Update" -> {
            com.example.ui.system.SystemUpdateScreen(
                viewModel = viewModel,
                onBack = onBack
            )
        }
        "About Vroxen UI", "About vos", "About Phone" -> {
            AboutPhoneSubpage(
                viewModel = viewModel,
                onOpenSystemUpdate = { },
                onBuildNumberClick = onEasterEgg
            )
        }
        "Developer Options" -> {
            DeveloperOptionsSubpage(viewModel = viewModel)
        }
        "Boot & Güç Seçenekleri", "Boot & Power" -> {
            BootAndPowerSubpage(viewModel = viewModel)
        }
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(subpage, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsMainCategoriesList(
    viewModel: OSViewModel,
    activeSubpage: String?,
    onSelectSubpage: (String) -> Unit,
    dndEnabled: Boolean,
    iconStyle: IconStyle,
    themePalette: ThemePalette,
    isDarkMode: Boolean,
    batteryLevel: Int,
    isCharging: Boolean,
    currentLang: String,
    isDeveloperModeUnlocked: Boolean,
    userAccount: UserAccount,
    onOpenIconStyleDialog: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Android Device System Settings Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Android, null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Android Cihaz Ayarları",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Resmi Android sistem ayarlarına git",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open Android Settings",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

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
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(userAccount.avatarColor),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(userAccount.avatarEmoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${userAccount.firstName} ${userAccount.lastName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${userAccount.email} • Cloud Active",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Open Account",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        item {
            SettingsCategoryHeader("Notifications & Alerts (Ses & Bildirimler)")
        }

        item {
            SettingsRowItem(
                icon = if (dndEnabled) Icons.Filled.DoNotDisturbOn else Icons.Filled.DoNotDisturbOff,
                title = "Do Not Disturb (Rahatsız Etmeyin)",
                subtitle = if (dndEnabled) "Açık • Bildirimler ve aramalar susturuldu" else "Kapalı • Tüm uyarılar ve sesler devrede",
                isSelected = activeSubpage == "Do Not Disturb",
                onClick = { onSelectSubpage("Do Not Disturb") }
            )
        }

        item {
            SettingsCategoryHeader("Personalization & Appearance")
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.Palette,
                title = "Wallpaper & Style",
                subtitle = "${if (isDarkMode) "Dark Mode" else "Light Mode"} • ${themePalette.displayName}",
                isSelected = activeSubpage == "Wallpaper & Style",
                onClick = { onSelectSubpage("Wallpaper & Style") }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.AutoFixHigh,
                title = "Simge Tarzı & Biçimi (Icon Style)",
                subtitle = "${iconStyle.title} • ${iconStyle.description}",
                isSelected = false,
                onClick = onOpenIconStyleDialog
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.DisplaySettings,
                title = "Display & Navigation",
                subtitle = "Gesture vs 3-Button, Brightness & Scaling",
                isSelected = activeSubpage == "Display & Navigation",
                onClick = { onSelectSubpage("Display & Navigation") }
            )
        }

        item {
            SettingsCategoryHeader("System & Privacy")
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.Security,
                title = "Vrox Security",
                subtitle = "Device protection status and scan history",
                isSelected = false,
                onClick = { viewModel.openApp(AppId.VROX) }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.Lock,
                title = "Security & Screen Lock",
                subtitle = "PIN Lock, Biometrics & Shortcuts",
                isSelected = activeSubpage == "Security & Lock Screen",
                onClick = { onSelectSubpage("Security & Lock Screen") }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.BatteryChargingFull,
                title = "Battery & Power",
                subtitle = "$batteryLevel% • ${if (isCharging) "Charging" else "Normal Usage"}",
                isSelected = activeSubpage == "Battery & Performance",
                onClick = { onSelectSubpage("Battery & Performance") }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.Storage,
                title = "Storage & Memory",
                subtitle = "48.2 GB used of 256 GB (18% full)",
                isSelected = activeSubpage == "Storage & Memory",
                onClick = { onSelectSubpage("Storage & Memory") }
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
                isSelected = activeSubpage == "Language & Input",
                onClick = { onSelectSubpage("Language & Input") }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.PowerSettingsNew,
                title = "Boot & Güç Seçenekleri",
                subtitle = "Yeniden Başlat, Hızlı Boot, Kurtarma Modu",
                isSelected = activeSubpage == "Boot & Güç Seçenekleri",
                onClick = { onSelectSubpage("Boot & Güç Seçenekleri") }
            )
        }

        item {
            SettingsRowItem(
                icon = Icons.Filled.Info,
                title = "About Vroxen UI",
                subtitle = "Vroxen UI 3 (Baklava) • Sistem Bilgisi",
                isSelected = activeSubpage == "About Vroxen UI",
                onClick = { onSelectSubpage("About Vroxen UI") }
            )
        }

        if (isDeveloperModeUnlocked) {
            item {
                SettingsRowItem(
                    icon = Icons.Filled.Code,
                    title = "{ } Developer Options",
                    subtitle = "USB Debugging, FPS HUD, Diagnostics",
                    isSelected = activeSubpage == "Developer Options",
                    onClick = { onSelectSubpage("Developer Options") }
                )
            }
        }
    }
}

@Composable
private fun TabletSettingsHub(viewModel: OSViewModel, onSelectSubpage: (String) -> Unit) {
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val userAccount by viewModel.userAccount.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(userAccount.avatarColor),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(userAccount.avatarEmoji, fontSize = 30.sp)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vroxen UI Tablet Kontrol Merkezi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${userAccount.firstName} ${userAccount.lastName} • ${userAccount.email}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Hızlı Tablet Denetimleri & Durum",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // DND Quick Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dndEnabled) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubpage("Do Not Disturb") }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (dndEnabled) Icons.Filled.DoNotDisturbOn else Icons.Filled.DoNotDisturbOff, null, tint = MaterialTheme.colorScheme.primary)
                            Switch(checked = dndEnabled, onCheckedChange = { viewModel.setDndEnabled(it) })
                        }
                        Text("Rahatsız Etmeyin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (dndEnabled) "Aktif (Uyarılar Sessiz)" else "Kapalı", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Battery Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubpage("Battery & Performance") }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.BatteryChargingFull, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Pil & Enerji", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("%$batteryLevel • ${if (isCharging) "Şarj Ediliyor" else "Normal Kullanım"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Wallpaper Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubpage("Wallpaper & Style") }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Palette, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Duvar Kağıdı & Stil", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${if (isDarkMode) "Karanlık Tema" else "Aydınlık Tema"} devrede", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Security Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSubpage("Security & Lock Screen") }
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Güvenlik & Kilit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Cihaz Koruması Aktif", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun DoNotDisturbSubpage(viewModel: OSViewModel) {
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val silenceNotifs by viewModel.dndSilenceNotifications.collectAsState()
    val restrictCalls by viewModel.dndRestrictCalls.collectAsState()
    val allowAlarms by viewModel.dndAllowAlarms.collectAsState()
    val allowRepeatCallers by viewModel.dndAllowRepeatCallers.collectAsState()
    val hideBadges by viewModel.dndHideBadges.collectAsState()
    val muteMedia by viewModel.dndMuteMedia.collectAsState()
    val scheduleEnabled by viewModel.dndScheduleEnabled.collectAsState()
    val durationMode by viewModel.dndDurationMode.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Master Switch Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dndEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (dndEnabled) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (dndEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (dndEnabled) Icons.Filled.DoNotDisturbOn else Icons.Filled.DoNotDisturbOff,
                                        contentDescription = null,
                                        tint = if (dndEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Rahatsız Etmeyin (Do Not Disturb)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dndEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (dndEnabled) "Aktif • Bildirimler ve çağrılar sessize alındı" else "Kapalı • Tüm uyarılar ve sesler devrede",
                                    fontSize = 12.sp,
                                    color = if (dndEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = dndEnabled,
                            onCheckedChange = { viewModel.setDndEnabled(it) }
                        )
                    }

                    if (dndEnabled) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Tüm gelen bildirim sesleri ve titreşimler engelleniyor. Kritik alarmlar izin verildiği sürece çalmaya devam eder.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Duration / Turn-off behavior
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Etkinlik Süresi (Duration)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    val durations = listOf("Kapatılana Kadar", "1 Saat Boyunca", "2 Saat Boyunca")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durations.forEachIndexed { index, label ->
                            val isSelected = durationMode == index
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setDndDurationMode(index) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Restrictions section
        item {
            Text("Bildirim & Uyarı Kısıtlamaları", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DndToggleRow(
                        icon = Icons.Filled.NotificationsOff,
                        title = "Bildirim Seslerini ve Titreşimleri Sustur",
                        subtitle = "Gelen bildirimler ses çıkarmadan arka planda saklanır",
                        checked = silenceNotifs,
                        onCheckedChange = { viewModel.setDndSilenceNotifications(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DndToggleRow(
                        icon = Icons.Filled.PhoneDisabled,
                        title = "Gelen Çağrıları ve Mesaj Uyarılarını Kısıtla",
                        subtitle = "Gelen aramalar ve SMS zil sesleri çalmaz",
                        checked = restrictCalls,
                        onCheckedChange = { viewModel.setDndRestrictCalls(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DndToggleRow(
                        icon = Icons.Filled.AccessTime,
                        title = "Alarmlara ve Zamanlayıcılara İzin Ver",
                        subtitle = "Kurulmuş olan saat alarmları ve sayaçlar yüksek sesle çalar",
                        checked = allowAlarms,
                        onCheckedChange = { viewModel.setDndAllowAlarms(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DndToggleRow(
                        icon = Icons.Filled.Repeat,
                        title = "İkinci Aramalara İzin Ver (Acil Durum)",
                        subtitle = "Aynı kişi 15 dakika içinde tekrar ararsa zil sesi çalar",
                        checked = allowRepeatCallers,
                        onCheckedChange = { viewModel.setDndAllowRepeatCallers(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DndToggleRow(
                        icon = Icons.Filled.VisibilityOff,
                        title = "Uygulama Bildirim Rozetlerini Gizle",
                        subtitle = "Ana ekrandaki ve çekmecedeki bildirim noktalarını gizler",
                        checked = hideBadges,
                        onCheckedChange = { viewModel.setDndHideBadges(it) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    DndToggleRow(
                        icon = Icons.Filled.VolumeOff,
                        title = "Medya ve Oyun Seslerini Kıs",
                        subtitle = "Müzik, video ve oyun seslerini de sessize alır",
                        checked = muteMedia,
                        onCheckedChange = { viewModel.setDndMuteMedia(it) }
                    )
                }
            }
        }

        // Automatic Schedules
        item {
            Text("Otomatik Zamanlama Kuralları", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DndToggleRow(
                        icon = Icons.Filled.Bedtime,
                        title = "Gece Uyku Takvimi (22:00 - 07:00)",
                        subtitle = "Her gece otomatik olarak Rahatsız Etmeyin moduna geçer",
                        checked = scheduleEnabled,
                        onCheckedChange = { viewModel.setDndScheduleEnabled(it) }
                    )
                }
            }
        }

        // Live Simulation & Test Alert
        item {
            Text("Canlı Uyarı Testi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Gerçek Zamanlı Bildirim Testi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Aşağıdaki butonları kullanarak DND aktifken ve kapalıyken bildirim ile alarmların nasıl davrandığını test edebilirsiniz.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.sendTestDndNotification(isAlarm = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Normal Bildirim Gönder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.sendTestDndNotification(isAlarm = true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kritik Alarm Gönder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DndToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
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
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
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
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
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
    onClockStyleSelect: (ClockStyle) -> Unit,
    onOpenWallpaperManager: () -> Unit = {}
) {
    var selectedColorTab by remember { mutableIntStateOf(0) } // 0: Wallpaper Colors (Material You), 1: Basic Colors
    val extractedPalettes = remember(currentWallpaper) {
        MaterialYouThemeEngine.extractWallpaperPalettes(currentWallpaper)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Live OS Phone Mockup Previews (Home Screen & Lock Screen)
        item {
            Text("Live Wallpaper & Color Preview", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Lock Screen Preview Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        WallpaperPreviewBackdrop(currentWallpaper)
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Lock Status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Nova 5G", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }

                            // Dynamic Material You Clock
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "09:41",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tuesday, Sep 29",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            // Bottom Lock Shortcuts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.FlashlightOn, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(24.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Home Screen Preview Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        WallpaperPreviewBackdrop(currentWallpaper)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Search Pill Widget
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth().height(22.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("G Search...", fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                    Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(10.dp))
                                }
                            }

                            // Dynamic Themed App Icons Grid Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(3) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Widgets, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }

                            // Dock Bar
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                                modifier = Modifier.fillMaxWidth().height(28.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) {}
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp)) {}
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp)) {}
                                }
                            }
                        }
                    }
                }
            }
        }

        item { HorizontalDivider() }

        // 2. Wallpaper Selection Gallery Carousel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duvar Kağıtları", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                FilledTonalButton(
                    onClick = onOpenWallpaperManager,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Canlı & Statik Yönetici", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(WallpaperType.entries) { wp ->
                    val isSelected = currentWallpaper == wp
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onWallpaperSelect(wp) }
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .width(90.dp)
                                .height(130.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                WallpaperPreviewBackdrop(wp)
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(20.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(13.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (wp == WallpaperType.DEVICE_SYSTEM) "Telefon" else wp.title.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item { HorizontalDivider() }

        // 3. Material You Dynamic Color Extraction Engine (Wallpaper vs Basic Colors)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Material You Color System", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Dynamically extracted color palettes from your wallpaper",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AssistChip(
                        onClick = {},
                        label = { Text("Android 16 M3", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }

                // Dual Tab Switcher: "Wallpaper Colors" vs "Basic Colors"
                TabRow(
                    selectedTabIndex = selectedColorTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedColorTab == 0,
                        onClick = { selectedColorTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.ColorLens, null, modifier = Modifier.size(16.dp))
                                Text("Wallpaper Colors", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedColorTab == 1,
                        onClick = { selectedColorTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Palette, null, modifier = Modifier.size(16.dp))
                                Text("Basic Colors", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                if (selectedColorTab == 0) {
                    // WALLPAPER EXTRACTED PALETTES (4 dynamic choices)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        extractedPalettes.forEach { swatch ->
                            val isSelected = currentPalette == swatch.paletteEnum
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { onPaletteSelect(swatch.paletteEnum) }
                                    .padding(4.dp)
                            ) {
                                // Multi-Tone Concentric Circle Swatch Preview
                                Surface(
                                    shape = CircleShape,
                                    color = Color(swatch.primaryColor),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.size(62.dp),
                                    shadowElevation = if (isSelected) 4.dp else 1.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(swatch.secondaryColor),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(swatch.tertiaryColor),
                                                    modifier = Modifier.size(18.dp)
                                                ) {}
                                            }
                                        }
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = swatch.title.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                } else {
                    // BASIC PRESET PALETTES
                    val basicPalettes = listOf(
                        ThemePalette.OCEAN_BLUE,
                        ThemePalette.ANDROID_GREEN,
                        ThemePalette.SUNSET_ORANGE,
                        ThemePalette.LAVENDER_PURPLE,
                        ThemePalette.CYBERPUNK_NEON,
                        ThemePalette.MONOCHROME
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        basicPalettes.forEach { palette ->
                            val isSelected = currentPalette == palette
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable { onPaletteSelect(palette) }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(palette.primaryHex),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    if (isSelected) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = palette.displayName.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        item { HorizontalDivider() }

        // 4. Live Material You Color Tokens Matrix
        item {
            Text("Active Material You Color Tokens", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    ThemeColorSample("Tertiary", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.onTertiary)
                    ThemeColorSample("Surface", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onSurface)
                    ThemeColorSample("Container", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        item { HorizontalDivider() }

        // 5. Appearance Theme Mode (Light / Dark / Follow System)
        item {
            Text("Appearance Theme Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Controls theme across all NovaOS apps, system bars, and surfaces",
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

        // 6. Dark Theme Quick Switch
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
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

        item { HorizontalDivider() }

        // 7. Lock Screen Clock Style
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
private fun WallpaperPreviewBackdrop(wallpaper: WallpaperType) {
    val context = LocalContext.current
    if (wallpaper == WallpaperType.DEVICE_SYSTEM) {
        var bmp by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val wm = android.app.WallpaperManager.getInstance(context)
                    val d = try { wm.drawable } catch (e: Throwable) { null }
                        ?: try { wm.peekDrawable() } catch (e: Throwable) { null }
                        ?: try { wm.fastDrawable } catch (e: Throwable) { null }
                        ?: try { wm.peekFastDrawable() } catch (e: Throwable) { null }
                    val b = d?.toBitmapOrNull()
                    if (b != null) bmp = b.asImageBitmap()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }

        if (bmp != null) {
            androidx.compose.foundation.Image(
                bitmap = bmp!!,
                contentDescription = "Telefon Arka Planı",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Telefon",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Sistem",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    } else {
        val resId = when (wallpaper) {
            WallpaperType.AURORA -> com.example.R.drawable.wp_aurora
            WallpaperType.CYBERPUNK -> com.example.R.drawable.wp_cyber
            WallpaperType.ABSTRACT -> com.example.R.drawable.wp_abstract
            else -> 0
        }

        if (resId != 0) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = resId),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val gradientColors = when (wallpaper) {
                WallpaperType.DEEP_SPACE -> listOf(Color(0xFF070414), Color(0xFF1B0F38), Color(0xFF2E1065))
                WallpaperType.FOREST_MIST -> listOf(Color(0xFF0A1F12), Color(0xFF143820), Color(0xFF1E5230))
                WallpaperType.OCEAN_SUNRISE -> listOf(Color(0xFF08182B), Color(0xFF0F3254), Color(0xFFD97736))
                WallpaperType.MINIMAL_GRADIENT -> listOf(Color(0xFF1C0A18), Color(0xFF4A1035), Color(0xFF7A184D))
                else -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(gradientColors))
            )
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
            modifier = Modifier.size(38.dp),
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
    showSystemBars: Boolean,
    onToggleSystemBars: () -> Unit,
    onNavModeChange: (NavMode) -> Unit,
    onBrightnessChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            ListItem(
                headlineContent = { Text("Sistem Çubukları (Üst & Alt Çubuk)", fontWeight = FontWeight.Bold) },
                supportingContent = { Text(if (showSystemBars) "Üst durum çubuğu ve alt gezinti çubuğu görünür" else "Üst ve alt çubuklar gizlendi (Tam Ekran Moda Geçildi)") },
                trailingContent = {
                    Switch(checked = showSystemBars, onCheckedChange = { onToggleSystemBars() })
                }
            )
        }

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
    onOpenSystemUpdate: () -> Unit = {},
    onBuildNumberClick: () -> Unit
) {
    val remoteVersion by viewModel.remoteConfigVersion.collectAsState()
    val remoteStatus by viewModel.remoteConfigStatus.collectAsState()
    val systemVersion by viewModel.systemVersion.collectAsState()
    val isDark = LocalIsDarkMode.current
    val currentRelease = remember(systemVersion) {
        com.example.model.VOSVersionCatalog.getRelease(systemVersion)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dynamic System Update & Version Banner
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF131722) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // System Update Logo (Vroxen UI 2 Classic vs Vroxen UI 3 Modern)
                    val isLegacyVOS2 = currentRelease.version.startsWith("Vroxen UI 2.", ignoreCase = true) || currentRelease.version.startsWith("vos 2.", ignoreCase = true)
                    if (isLegacyVOS2) {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFF00332C),
                            shadowElevation = 14.dp,
                            border = BorderStroke(
                                2.dp,
                                Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFF00B0FF)))
                            ),
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Vroxen UI 2",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF00E676)
                                    )
                                    Text(
                                        text = "AURORA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00B0FF),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(26.dp),
                            color = Color(0xFF0C0D12),
                            shadowElevation = 14.dp,
                            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_system_update_v3),
                                    contentDescription = "Vroxen UI 3 System Update Logo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(26.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${currentRelease.version} (${currentRelease.codename})",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isLegacyVOS2) "Android 15 (Vanilla Ice Cream) • ${currentRelease.badge}" else "Android 16 (Baklava) • ${currentRelease.badge}",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isLegacyVOS2) Color(0xFF00B0FF) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Sistem Durumu: ${currentRelease.version} Güncel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853)
                            )
                        }
                    }
                }
            }
        }

        // 2. What's new in Version 3 Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.NewReleases, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("Vroxen UI 3 Sürüm Notları & Yenilikler", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.08f))

                    Text("• Sabit Kalıcı Alt Dock (Persistent Dock)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Sayfalı Uygulama Çekmecesi & Üst Arama Çubuğu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• 8 Farklı Canlı Simge Tarzı & Şekil Motoru", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Hey Vroxen Sesli Yapay Zeka (Gemini AI)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Gerçek Zamanlı Sistem Duvar Kağıdı Entegrasyonu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 3. Refresh / Fetch Actions
        item {
            Button(
                onClick = { viewModel.fetchRemoteConfigVersion() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Güncellemeleri Denetle", fontWeight = FontWeight.Bold)
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
                        supportingContent = { Text("Vroxen UI Pixel Edition") },
                        leadingContent = { Icon(Icons.Default.Smartphone, null, tint = MaterialTheme.colorScheme.primary) }
                    )
                    ListItem(
                        headlineContent = { Text("Android Version") },
                        supportingContent = { Text("Android Baklava (16.0)") },
                        leadingContent = { Icon(Icons.Default.Android, null, tint = Color(0xFF3DDC84)) }
                    )
                    ListItem(
                        headlineContent = { Text("Build Version") },
                        supportingContent = { Text("VROXEN-UI-REL (Tap 7 times for Easter Egg)") },
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

        item { SettingsCategoryHeader("200 Uygulama Performans & Kapasite") }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("200 Uygulama Stres & Çökme Testi", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Tüm 200 uygulamayı tek dokunuşla ana ekrana ve menüye yükleyerek 60/120 FPS akıcılık ve sıfır çökme kararlılığını test edin.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.installAll200Apps() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DownloadForOffline, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("200 Uygulamayı Yükle", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.uninstallAllUserApps() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Sıfırla", fontSize = 11.sp)
                        }
                    }
                }
            }
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

@Composable
fun BootAndPowerSubpage(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val systemVersion by viewModel.systemVersion.collectAsState()
    val batteryPct by viewModel.batteryLevel.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Vroxen Secure Boot v4.2",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color(0xFF00E676).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "GÜVENLİ",
                                color = Color(0xFF00E676),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Sürüm: $systemVersion • Linux Kernel 6.6-android15 • Batarya: %$batteryPct",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "BAŞLATMA VE YENİDEN BAŞLATMA SEÇENEKLERİ",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Reboot Normal
        item {
            Card(
                onClick = { viewModel.rebootSystem(BootMode.NORMAL) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.RestartAlt, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sistemi Yeniden Başlat (Normal Reboot)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Vroxen UI tam animasyon ve servisleriyle yeniden başlar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Fast Boot
        item {
            Card(
                onClick = { viewModel.rebootSystem(BootMode.FAST) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00B0FF).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bolt, null, tint = Color(0xFF00B0FF))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hızlı Boot (Fast Reboot)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("1 saniyede anında hızlı başlatma döngüsü", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Recovery Mode
        item {
            Card(
                onClick = { viewModel.rebootSystem(BootMode.RECOVERY) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9100).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Build, null, tint = Color(0xFFFF9100))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kurtarma Modu (Recovery Mode)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Android kurtarma konsolunu açar, önbellek temizler", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Safe Mode
        item {
            Card(
                onClick = { viewModel.rebootSystem(BootMode.SAFE_MODE) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Security, null, tint = Color(0xFF7C4DFF))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Güvenli Mod (Safe Mode)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Temel sistem sürücüleri ile güvenli başlatma", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Power Off
        item {
            Card(
                onClick = { viewModel.shutdownSystem() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF5252).copy(alpha = 0.2f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PowerSettingsNew, null, tint = Color(0xFFFF5252))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cihazı Kapat (Power Off)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFFF5252))
                        Text("Cihazı kapatır; açılış güç tuşu ile boot edilebilir", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
