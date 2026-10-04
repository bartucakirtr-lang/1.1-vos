package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.model.OSNotification
import com.example.ui.theme.LocalIsDarkMode
import com.example.ui.theme.LocalThemeController
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationAndQuickSettingsShade(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val airplaneMode by viewModel.airplaneMode.collectAsState()
    val autoRotate by viewModel.autoRotate.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
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
    val isDexModeActive by viewModel.isDexModeActive.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val dailyDataBytes by viewModel.dailyDataUsageBytes.collectAsState()
    val monthlyDataBytes by viewModel.monthlyDataUsageBytes.collectAsState()
    val dataLimitBytes by viewModel.dataLimitBytes.collectAsState()

    var showNightLightDialog by remember { mutableStateOf(false) }
    var showDataUsageDialog by remember { mutableStateOf(false) }

    val timeFormatted = remember(systemTime) { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(systemTime)) }
    val dateFormatted = remember(systemTime) { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date(systemTime)) }

    val isDark = LocalIsDarkMode.current
    val themeController = LocalThemeController.current
    val shadeBg = if (isDark) Color(0xFF0C1017).copy(alpha = 0.96f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    val headerTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subheaderTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(top = 10.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Header: Time, Date, Battery & Shortcuts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeFormatted,
                        color = headerTextColor,
                        fontSize = 22.sp,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Settings Tiles Grid (2 columns)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Internet",
                        subtitle = if (airplaneMode) "Airplane Active" else if (wifiEnabled) "Nova-WiFi 5G" else "Disconnected",
                        icon = if (wifiEnabled && !airplaneMode) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                        isActive = wifiEnabled && !airplaneMode,
                        onClick = { viewModel.toggleWifi() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Bluetooth",
                        subtitle = if (airplaneMode) "Disabled" else if (bluetoothEnabled) "Galaxy Buds Pro" else "Off",
                        icon = Icons.Filled.Bluetooth,
                        isActive = bluetoothEnabled && !airplaneMode,
                        onClick = { viewModel.toggleBluetooth() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Night Light",
                        subtitle = if (nightLight) "Warm On" else when (nightLightSchedule) {
                            com.example.model.NightLightSchedule.SUNSET_TO_SUNRISE -> "Sunset Schedule"
                            com.example.model.NightLightSchedule.CUSTOM -> "Scheduled ($nightLightStartTime)"
                            else -> "Off"
                        },
                        icon = Icons.Filled.Bedtime,
                        isActive = nightLight,
                        onClick = { viewModel.toggleNightLight() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Flashlight",
                        subtitle = if (flashlightOn) "On" else "Off",
                        icon = Icons.Filled.FlashlightOn,
                        isActive = flashlightOn,
                        onClick = { viewModel.toggleFlashlight() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Airplane Mode",
                        subtitle = if (airplaneMode) "Radios Off" else "Off",
                        icon = if (airplaneMode) Icons.Filled.AirplanemodeActive else Icons.Filled.AirplanemodeInactive,
                        isActive = airplaneMode,
                        onClick = { viewModel.toggleAirplane() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Do Not Disturb",
                        subtitle = if (dndEnabled) "Priority Only" else "Off",
                        icon = Icons.Filled.DoNotDisturbOn,
                        isActive = dndEnabled,
                        onClick = { viewModel.toggleDnd() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = if (isDark) "Dark Theme" else "Light Theme",
                        subtitle = if (isDark) "Night Mode" else "Day Mode",
                        icon = if (isDark) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                        isActive = isDark,
                        onClick = { themeController.toggleDarkMode() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Battery Saver",
                        subtitle = if (batterySaver) "Optimized" else "Off",
                        icon = Icons.Filled.BatterySaver,
                        isActive = batterySaver,
                        onClick = { viewModel.toggleBatterySaver() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickTile(
                        title = "Auto-Rotate",
                        subtitle = if (autoRotate) "Auto" else "Locked",
                        icon = Icons.Filled.ScreenRotation,
                        isActive = autoRotate,
                        onClick = { viewModel.toggleAutoRotate() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickTile(
                        title = "Hotspot",
                        subtitle = if (hotspotEnabled) "Nova-Hotspot 5G" else "Off",
                        icon = Icons.Filled.WifiTethering,
                        isActive = hotspotEnabled,
                        onClick = { viewModel.toggleHotspot() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brightness Adjustment Card with percentage readout
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.BrightnessLow, "Min Brightness", tint = headerTextColor, modifier = Modifier.size(18.dp))
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
                    Icon(Icons.Filled.BrightnessHigh, "Max Brightness", tint = headerTextColor, modifier = Modifier.size(18.dp))
                    Text(
                        text = "${(brightness * 100).toInt()}%",
                        color = headerTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Data Usage Monitor Card
            val monthlyGb = monthlyDataBytes.toFloat() / (1024 * 1024 * 1024)
            val limitGb = dataLimitBytes.toFloat() / (1024 * 1024 * 1024)
            val usageRatio = (monthlyGb / limitGb).coerceIn(0f, 1f)
            val dataColor = when {
                usageRatio >= 0.85f -> Color(0xFFFF1744)
                usageRatio >= 0.70f -> Color(0xFFFFB300)
                else -> MaterialTheme.colorScheme.primary
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDataUsageDialog = true }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Icon(Icons.Default.DataUsage, contentDescription = "Data Usage", tint = dataColor, modifier = Modifier.size(20.dp))
                            Text("Mobile Data Usage", color = headerTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f GB / %.0f GB", monthlyGb, limitGb),
                            color = headerTextColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    LinearProgressIndicator(
                        progress = { usageRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = dataColor,
                        trackColor = if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Daily: ${String.format(Locale.getDefault(), "%.1f MB", dailyDataBytes.toFloat() / (1024 * 1024))}",
                            color = subheaderTextColor,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "${(usageRatio * 100).toInt()}% used • Tap to configure",
                            color = subheaderTextColor,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Active Media Player Tile in Quick Settings
            if (isPlayingMusic) {
                val track = musicTracks[currentTrackIndex]
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
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
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                    Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = track.title,
                                        color = headerTextColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = track.artist,
                                        color = subheaderTextColor,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.prevTrack() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.SkipPrevious, "Prev", tint = headerTextColor, modifier = Modifier.size(20.dp))
                                }
                                IconButton(
                                    onClick = { viewModel.togglePlayMusic() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        if (isPlayingMusic) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        "Play/Pause",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { viewModel.nextTrack() }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.SkipNext, "Next", tint = headerTextColor, modifier = Modifier.size(20.dp))
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
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Notifications List
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications (${notifications.size})",
                    color = headerTextColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllNotifications() }) {
                        Text("Clear all", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
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
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
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
                                            modifier = Modifier.size(28.dp)
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
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = notif.title,
                                            color = headerTextColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.dismissNotification(notif.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, "Dismiss", tint = subheaderTextColor, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    color = subheaderTextColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 36.dp)
                                )

                                if (notif.actionLabel != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.collapseShade()
                                                if (notif.id == "notif3") {
                                                    viewModel.openSystemUpdate()
                                                } else {
                                                    viewModel.openApp(notif.appId)
                                                }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text(notif.actionLabel, fontSize = 11.sp)
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
                    .padding(top = 8.dp)
                    .clickable { viewModel.collapseShade() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }

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

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    currentLang: String = "English",
    onClick: () -> Unit,
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
            .height(58.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = contentTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = com.example.util.TranslationManager.getTranslation(title, currentLang),
                    color = contentTint,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    color = subTextTint,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
    }
}
