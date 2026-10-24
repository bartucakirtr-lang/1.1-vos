package com.example.ui.system

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.BootMode
import com.example.model.OSNotification
import com.example.model.SoundProfile
import com.example.ui.theme.LocalIsDarkMode
import com.example.ui.theme.LocalThemeController
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationAndQuickSettingsShade(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    // Intercept back button to close shade
    BackHandler(enabled = true) {
        viewModel.collapseShade()
    }

    val systemTime by viewModel.systemTime.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val activeWifiSsid by viewModel.activeWifiSsid.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val activeBtDevice by viewModel.activeBtDevice.collectAsState()
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val airplaneMode by viewModel.airplaneMode.collectAsState()
    val autoRotate by viewModel.autoRotate.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val locationEnabled by viewModel.locationEnabled.collectAsState()
    val mobileDataEnabled by viewModel.mobileDataEnabled.collectAsState()
    val soundProfile by viewModel.soundProfile.collectAsState()
    val nightLight by viewModel.nightLight.collectAsState()
    val nightLightIntensity by viewModel.nightLightIntensity.collectAsState()
    val nightLightSchedule by viewModel.nightLightSchedule.collectAsState()
    val nightLightStartTime by viewModel.nightLightStartTime.collectAsState()
    val nightLightEndTime by viewModel.nightLightEndTime.collectAsState()
    val hotspotEnabled by viewModel.hotspotEnabled.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val musicTracks by viewModel.musicTracks.collectAsState()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val dailyDataBytes by viewModel.dailyDataUsageBytes.collectAsState()
    val monthlyDataBytes by viewModel.monthlyDataUsageBytes.collectAsState()
    val dataLimitBytes by viewModel.dataLimitBytes.collectAsState()

    var showWifiDialog by remember { mutableStateOf(false) }
    var showBtDialog by remember { mutableStateOf(false) }
    var showPowerMenuDialog by remember { mutableStateOf(false) }
    var showNightLightDialog by remember { mutableStateOf(false) }
    var showDataUsageDialog by remember { mutableStateOf(false) }

    val timeFormatted = remember(systemTime) { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(systemTime)) }
    val dateFormatted = remember(systemTime) { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(systemTime)) }

    val isDark = LocalIsDarkMode.current
    val themeController = LocalThemeController.current
    val shadeBg = if (isDark) Color(0xFF0D1117).copy(alpha = 0.97f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    val headerTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subheaderTextColor = if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
    val shortcutButtonBg = if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    val shortcutButtonTint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(shadeBg)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -20) {
                        viewModel.collapseShade()
                    }
                }
            }
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header: Time, Date, Battery, Power, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.clickable {
                        viewModel.collapseShade()
                        viewModel.openApp(AppId.CLOCK)
                    }
                ) {
                    Text(
                        text = timeFormatted,
                        color = headerTextColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatted,
                        color = subheaderTextColor,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Battery Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(shortcutButtonBg)
                            .clickable { viewModel.toggleCharging() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (isCharging) {
                            Icon(
                                Icons.Filled.Bolt,
                                contentDescription = "Charging",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "$batteryLevel%",
                            color = headerTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Power Menu shortcut
                    IconButton(
                        onClick = { showPowerMenuDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(shortcutButtonBg)
                    ) {
                        Icon(Icons.Outlined.PowerSettingsNew, "Power", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }

                    // Settings shortcut
                    IconButton(
                        onClick = {
                            viewModel.collapseShade()
                            viewModel.openApp(AppId.SETTINGS)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(shortcutButtonBg)
                    ) {
                        Icon(Icons.Outlined.Settings, "Settings", tint = shortcutButtonTint, modifier = Modifier.size(18.dp))
                    }

                    // Lock button shortcut
                    IconButton(
                        onClick = { viewModel.lockPhone() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(shortcutButtonBg)
                    ) {
                        Icon(Icons.Outlined.Lock, "Lock", tint = shortcutButtonTint, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Settings Tiles Grid (2 columns)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: Wi-Fi & Bluetooth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Internet",
                        subtitle = if (airplaneMode) "Airplane Active" else if (wifiEnabled) activeWifiSsid else "Disconnected",
                        icon = if (wifiEnabled && !airplaneMode) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        isActive = wifiEnabled && !airplaneMode,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleWifi() },
                        onLongClick = { showWifiDialog = true },
                        hasDetail = true,
                        onDetailClick = { showWifiDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Bluetooth",
                        subtitle = if (airplaneMode) "Disabled" else if (bluetoothEnabled) activeBtDevice else "Off",
                        icon = if (bluetoothEnabled && !airplaneMode) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
                        isActive = bluetoothEnabled && !airplaneMode,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleBluetooth() },
                        onLongClick = { showBtDialog = true },
                        hasDetail = true,
                        onDetailClick = { showBtDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Airplane Mode & Flashlight
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Airplane Mode",
                        subtitle = if (airplaneMode) "Radios Off" else "Standard",
                        icon = if (airplaneMode) Icons.Filled.AirplanemodeActive else Icons.Filled.AirplanemodeInactive,
                        isActive = airplaneMode,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleAirplane() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Flashlight",
                        subtitle = if (flashlightOn) "Torch Active" else "Off",
                        icon = if (flashlightOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                        isActive = flashlightOn,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleFlashlight() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Do Not Disturb & Night Light
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Do Not Disturb",
                        subtitle = if (dndEnabled) "Priority Only" else "Off",
                        icon = if (dndEnabled) Icons.Filled.DoNotDisturbOn else Icons.Filled.DoNotDisturbOff,
                        isActive = dndEnabled,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleDnd() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Night Light",
                        subtitle = if (nightLight) "Warm On" else "Off",
                        icon = Icons.Filled.Bedtime,
                        isActive = nightLight,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleNightLight() },
                        onLongClick = { showNightLightDialog = true },
                        hasDetail = true,
                        onDetailClick = { showNightLightDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 4: Theme Mode & Sound Profile
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = if (isDark) "Dark Theme" else "Light Theme",
                        subtitle = if (isDark) "Night Mode" else "Day Mode",
                        icon = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        isActive = isDark,
                        currentLang = currentLang,
                        onClick = { themeController.toggleDarkMode() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Sound",
                        subtitle = soundProfile.label,
                        icon = when (soundProfile) {
                            SoundProfile.RING -> Icons.Filled.NotificationsActive
                            SoundProfile.VIBRATE -> Icons.Filled.Vibration
                            SoundProfile.SILENT -> Icons.Filled.VolumeOff
                        },
                        isActive = soundProfile != SoundProfile.SILENT,
                        currentLang = currentLang,
                        onClick = { viewModel.cycleSoundProfile() },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 5: Battery Saver & Auto-Rotate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Battery Saver",
                        subtitle = if (batterySaver) "Optimized" else "Standard",
                        icon = Icons.Filled.BatterySaver,
                        isActive = batterySaver,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleBatterySaver() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Auto-Rotate",
                        subtitle = if (autoRotate) "Auto" else "Locked",
                        icon = if (autoRotate) Icons.Filled.ScreenRotation else Icons.Filled.ScreenLockPortrait,
                        isActive = autoRotate,
                        currentLang = currentLang,
                        onClick = { viewModel.toggleAutoRotate() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Sliders: Brightness & Volume in Compact Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Brightness slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (brightness > 0.6f) Icons.Filled.BrightnessHigh else Icons.Outlined.BrightnessMedium,
                            "Brightness",
                            tint = headerTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Slider(
                            value = brightness,
                            onValueChange = { viewModel.setBrightness(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        Text(
                            text = "${(brightness * 100).toInt()}%",
                            color = headerTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp)
                        )
                    }

                    // Volume slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (volume > 0.5f) Icons.Filled.VolumeUp else if (volume > 0f) Icons.Filled.VolumeDown else Icons.Filled.VolumeMute,
                            "Volume",
                            tint = headerTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Slider(
                            value = volume,
                            onValueChange = { viewModel.setVolume(it) },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            color = headerTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Active Media Player Tile in Quick Settings
            if (isPlayingMusic && musicTracks.isNotEmpty()) {
                val track = musicTracks.getOrElse(currentTrackIndex) { musicTracks.first() }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(track.coverGradientStart),
                                                    Color(track.coverGradientEnd)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = track.title,
                                        color = headerTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = track.artist,
                                        color = subheaderTextColor,
                                        fontSize = 10.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.prevTrack() }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.SkipPrevious, "Prev", tint = headerTextColor, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.togglePlayMusic() },
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        if (isPlayingMusic) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        "Play/Pause",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.nextTrack() }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.SkipNext, "Next", tint = headerTextColor, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        // Progress line
                        LinearProgressIndicator(
                            progress = { (playbackProgress.toFloat() / track.durationSeconds.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Notifications List Header & List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications (${notifications.size})",
                    color = headerTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllNotifications() }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("Clear all", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No new notifications",
                        color = subheaderTextColor,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    when (notif.appId) {
                                                        AppId.MESSAGES -> Icons.Outlined.Chat
                                                        AppId.PHONE -> Icons.Outlined.Phone
                                                        AppId.PHOTOS -> Icons.Outlined.Image
                                                        AppId.CLOCK -> Icons.Outlined.Alarm
                                                        else -> Icons.Outlined.Notifications
                                                    },
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = notif.title,
                                            color = headerTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.dismissNotification(notif.id) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, "Dismiss", tint = subheaderTextColor, modifier = Modifier.size(12.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    color = subheaderTextColor,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(start = 32.dp)
                                )

                                if (notif.actionLabel != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.collapseShade()
                                                if (notif.id == "notif3" || notif.id.startsWith("notif_periodic_update") || notif.title.contains("Güncelleme", ignoreCase = true) || notif.title.contains("Update", ignoreCase = true)) {
                                                    viewModel.openSystemUpdate()
                                                } else {
                                                    viewModel.openApp(notif.appId)
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(notif.actionLabel, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Drag Handle to Close
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clickable { viewModel.collapseShade() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }

    // --- Wi-Fi Network Selector Dialog ---
    if (showWifiDialog) {
        AlertDialog(
            onDismissRequest = { showWifiDialog = false },
            icon = { Icon(Icons.Filled.Wifi, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Wi-Fi Networks", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wi-Fi Active", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = wifiEnabled,
                            onCheckedChange = { viewModel.toggleWifi() }
                        )
                    }

                    HorizontalDivider()

                    val wifiNetworks = listOf(
                        "Nova-WiFi 5G" to "Connected • Excellent signal",
                        "Fiber-Ultra-6G" to "Saved • WPA3 Security",
                        "Office-Guest-WiFi" to "Open network",
                        "Starlink-Mobile" to "Saved • 5GHz",
                        "Metro-Free-Public" to "Login required"
                    )

                    wifiNetworks.forEach { (ssid, status) ->
                        val isConnected = wifiEnabled && activeWifiSsid == ssid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectWifiNetwork(ssid)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.Wifi, null, tint = if (isConnected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(ssid, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(if (isConnected) "Connected" else status, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                if (isConnected) {
                                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWifiDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // --- Bluetooth Device Selector Dialog ---
    if (showBtDialog) {
        AlertDialog(
            onDismissRequest = { showBtDialog = false },
            icon = { Icon(Icons.Filled.Bluetooth, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Bluetooth Devices", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bluetooth Active", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = bluetoothEnabled,
                            onCheckedChange = { viewModel.toggleBluetooth() }
                        )
                    }

                    HorizontalDivider()

                    val btDevices = listOf(
                        "Galaxy Buds Pro" to "Connected • 95% Battery",
                        "Nova Watch 4" to "Paired • Connected",
                        "Sony WH-1000XM5" to "Paired",
                        "Tesla Model 3" to "Saved"
                    )

                    btDevices.forEach { (name, status) ->
                        val isConnected = bluetoothEnabled && activeBtDevice == name
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectBtDevice(name)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.Headphones, null, tint = if (isConnected) MaterialTheme.colorScheme.primary else Color.Gray, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(if (isConnected) "Connected" else status, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                if (isConnected) {
                                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBtDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // --- Power Menu Dialog ---
    if (showPowerMenuDialog) {
        AlertDialog(
            onDismissRequest = { showPowerMenuDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Güç & Boot Seçenekleri", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            showPowerMenuDialog = false
                            viewModel.collapseShade()
                            viewModel.lockPhone()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cihazı Kilitle (Lock)")
                    }

                    Button(
                        onClick = {
                            showPowerMenuDialog = false
                            viewModel.collapseShade()
                            viewModel.rebootSystem(BootMode.NORMAL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.RestartAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yeniden Başlat (Reboot OS)")
                    }

                    Button(
                        onClick = {
                            showPowerMenuDialog = false
                            viewModel.collapseShade()
                            viewModel.rebootSystem(BootMode.FAST)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Bolt, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hızlı Boot (Fast Reboot)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showPowerMenuDialog = false
                            viewModel.collapseShade()
                            viewModel.rebootSystem(BootMode.RECOVERY)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Build, null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kurtarma Modu (Recovery)", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            showPowerMenuDialog = false
                            viewModel.collapseShade()
                            viewModel.shutdownSystem()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PowerSettingsNew, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cihazı Kapat (Power Off)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPowerMenuDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // --- Night Light Dialog ---
    if (showNightLightDialog) {
        AlertDialog(
            onDismissRequest = { showNightLightDialog = false },
            title = { Text("Night Light & Eye Comfort", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Night Light", fontWeight = FontWeight.SemiBold)
                        Switch(checked = nightLight, onCheckedChange = { viewModel.toggleNightLight() })
                    }

                    Text("Warmth Intensity: ${(nightLightIntensity * 100).toInt()}%", fontSize = 12.sp)
                    Slider(
                        value = nightLightIntensity,
                        onValueChange = { viewModel.setNightLightIntensity(it) },
                        valueRange = 0.10f..0.60f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNightLightDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // --- Data Usage Dialog ---
    if (showDataUsageDialog) {
        AlertDialog(
            onDismissRequest = { showDataUsageDialog = false },
            title = { Text("Data Usage Manager", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Monthly Consumption: ${String.format(Locale.getDefault(), "%.2f GB", monthlyDataBytes.toFloat() / (1024*1024*1024))}", fontSize = 13.sp)
                    Text("Daily Consumption: ${String.format(Locale.getDefault(), "%.1f MB", dailyDataBytes.toFloat() / (1024*1024))}", fontSize = 13.sp)
                    Text("Set Monthly Limit:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5L, 10L, 20L, 50L).forEach { gb ->
                            val bytes = gb * 1024 * 1024 * 1024
                            val isSelected = dataLimitBytes == bytes
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setDataLimit(bytes) },
                                label = { Text("$gb GB") }
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.addSimulatedDataUsage(500L * 1024 * 1024) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Simulate +500 MB Usage")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataUsageDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    currentLang: String = "English",
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    hasDetail: Boolean = false,
    onDetailClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkMode.current
    val tileBg = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    }
    val contentTint = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    }
    val subTextTint = if (isActive) {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
    } else {
        if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tileBg,
        modifier = modifier
            .height(54.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = contentTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = com.example.util.TranslationManager.getTranslation(title, currentLang),
                        color = contentTint,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        color = subTextTint,
                        fontSize = 9.5.sp,
                        maxLines = 1
                    )
                }
            }

            if (hasDetail && onDetailClick != null) {
                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = "Details",
                        tint = contentTint.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
