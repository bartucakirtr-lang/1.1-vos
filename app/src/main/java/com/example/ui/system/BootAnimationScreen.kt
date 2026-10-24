package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BootMode
import com.example.model.HomeLayoutDesign
import com.example.util.DeviceInstalledApp
import com.example.util.PackageManagerUtil
import com.example.util.toBitmapOrNull
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BootAnimationScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemVersion by viewModel.systemVersion.collectAsState()
    val bootMode by viewModel.bootMode.collectAsState()
    val currentLauncherDesign by viewModel.homeLayoutDesign.collectAsState()
    val selectedExternalLauncherPackage by viewModel.selectedExternalLauncherPackage.collectAsState()
    val selectedExternalLauncherName by viewModel.selectedExternalLauncherName.collectAsState()

    var inRecoveryScreen by remember { mutableStateOf(bootMode == BootMode.RECOVERY) }
    var recoveryLogMessage by remember { mutableStateOf<String?>(null) }
    var showLauncherSelectorDialog by remember { mutableStateOf(false) }

    // Dynamic boot sequence simulation timeout before finishing boot
    LaunchedEffect(bootMode, inRecoveryScreen) {
        if (inRecoveryScreen) return@LaunchedEffect

        val totalDurationMs = when (bootMode) {
            BootMode.FAST -> 1000L
            else -> 60000L
        }

        delay(totalDurationMs)
        viewModel.completeBoot()
        val targetExternal = viewModel.selectedExternalLauncherPackage.value
        if (targetExternal != null) {
            PackageManagerUtil.launchPackage(context, targetExternal)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (inRecoveryScreen) {
            // ================= RECOVERY CONSOLE UI =================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Android / Vroxen UI Recovery Mode",
                            color = Color(0xFFFF9100),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Build: $systemVersion / 2026-android15-rel",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Default Launcher: ${selectedExternalLauncherName ?: currentLauncherDesign.title}",
                        color = Color(0xFF00E5FF).copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Middle Options
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B241D))
                    ) {
                        Icon(
                            Icons.Filled.Android,
                            contentDescription = null,
                            tint = Color(0xFF3DDC84),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    if (recoveryLogMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0D1B2A),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = recoveryLogMessage!!,
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                inRecoveryScreen = false
                                viewModel.rebootSystem(BootMode.NORMAL)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_reboot_system")
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reboot system now", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showLauncherSelectorDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_change_launcher")
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Change default launcher (Başlatıcı Değiştir)", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.wipeCachePartition()
                                recoveryLogMessage = "-- Wiping cache partition...\nFormatting /cache...\nCache wipe complete."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263238)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("recovery_wipe_cache")
                        ) {
                            Icon(Icons.Filled.DeleteSweep, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Wipe cache partition", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.shutdownSystem()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.PowerSettingsNew, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Power off")
                        }
                    }
                }

                Text(
                    text = "Vroxen UI Recovery Environment",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // ================= NORMAL BOOT SCREEN (ONLY VROXEN UI TEXT) =================
            // Pulsing text animation
            val alpha = remember { Animatable(0f) }
            val scale = remember { Animatable(0.9f) }
            val pulseAlpha = remember { Animatable(1f) }

            LaunchedEffect(Unit) {
                launch { alpha.animateTo(1f, animationSpec = tween(2000, easing = LinearOutSlowInEasing)) }
                launch { scale.animateTo(1f, animationSpec = tween(2000, easing = EaseOutBack)) }

                delay(2000)
                while (true) {
                    pulseAlpha.animateTo(0.5f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
                    pulseAlpha.animateTo(1f, animationSpec = tween(1000, easing = FastOutSlowInEasing))
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing Center Logo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.pointerInput(Unit) { detectTapGestures { } }
                ) {
                    Text(
                        text = "Vroxen UI",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = alpha.value * pulseAlpha.value),
                        letterSpacing = 2.sp,
                        modifier = Modifier.scale(scale.value)
                    )
                }

                // Small Recovery Menu Button at Bottom
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.65f)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                        .clickable {
                            viewModel.setBootMode(BootMode.RECOVERY)
                            inRecoveryScreen = true
                        }
                        .testTag("boot_open_recovery_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Build,
                            contentDescription = null,
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Kurtarma Menüsü (Recovery)",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ================= RECOVERY LAUNCHER SELECTOR DIALOG =================
        if (showLauncherSelectorDialog) {
            var selectedTab by remember { mutableIntStateOf(0) }
            val installedHomeApps = remember {
                val homeList = PackageManagerUtil.getInstalledHomeApps(context)
                val fallbackList = PackageManagerUtil.getInstalledDeviceApps(context).filter { app ->
                    val nameLower = app.appName.lowercase()
                    val pkgLower = app.packageName.lowercase()
                    (nameLower.contains("launcher") || nameLower.contains("home") ||
                     pkgLower.contains("launcher") || pkgLower.contains("home")) &&
                    homeList.none { it.packageName == app.packageName }
                }
                (homeList + fallbackList).distinctBy { it.packageName }
            }

            AlertDialog(
                onDismissRequest = { showLauncherSelectorDialog = false },
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Home, contentDescription = null, tint = Color(0xFF00E5FF))
                            Text(
                                "Varsayılan Başlatıcı Seçici",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            "Kurtarma Modu (Recovery Mode) - Başlatıcılar Arası Geçiş",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 440.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tab Selector
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0xFF141E17),
                            contentColor = Color(0xFF00E5FF),
                            indicator = {},
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        "Cihazda Yüklü (${installedHomeApps.size})",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (selectedTab == 0) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        "Vroxen UI (${HomeLayoutDesign.entries.size})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (selectedTab == 1) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            )
                        }

                        if (selectedTab == 0) {
                            // Section: Device-installed Android Launchers
                            if (installedHomeApps.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF263238),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        "Cihazda başka harici Android başlatıcı paketi bulunamadı. Vroxen UI sistem başlatıcısı aktiftir.",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                                ) {
                                    items(installedHomeApps, key = { it.packageName }) { app ->
                                        val isThisApp = app.packageName == context.packageName
                                        val isSelected = if (selectedExternalLauncherPackage == null) {
                                            isThisApp
                                        } else {
                                            selectedExternalLauncherPackage == app.packageName
                                        }

                                        DeviceLauncherItemCard(
                                            app = app,
                                            isThisApp = isThisApp,
                                            isSelected = isSelected,
                                            onClick = {
                                                if (isThisApp) {
                                                    viewModel.setSelectedExternalLauncher(null, null)
                                                    recoveryLogMessage = "-- Seçilen Başlatıcı: Vroxen UI (${app.appName})\n/system/etc/default_launcher.cfg güncellendi.\nYeniden başlatmaya hazır (Ready to reboot)."
                                                } else {
                                                    viewModel.setExternalLauncherFromRecovery(app.packageName, app.appName)
                                                    recoveryLogMessage = "-- Seçilen Başlatıcı: ${app.appName} (${app.packageName})\n/system/etc/default_launcher.cfg güncellendi.\nVarsayılan olarak ayarlandı.\nYeniden başlatmaya hazır (Ready to reboot)."
                                                }
                                                showLauncherSelectorDialog = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            // Section: Vroxen UI Built-in Designs
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                            ) {
                                items(HomeLayoutDesign.entries, key = { it.name }) { design ->
                                    val isSelected = selectedExternalLauncherPackage == null && currentLauncherDesign == design

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) Color(0xFF00E676) else Color(0xFF263238),
                                        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setLauncherFromRecovery(design)
                                                recoveryLogMessage = "-- Seçilen Başlatıcı: Vroxen UI [${design.title}]\n/system/etc/default_launcher.cfg güncellendi.\nYeniden başlatmaya hazır (Ready to reboot)."
                                                showLauncherSelectorDialog = false
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = when (design) {
                                                        HomeLayoutDesign.PIXEL_MODERN -> Icons.Filled.GridView
                                                        HomeLayoutDesign.CARD_DECK -> Icons.Filled.ViewCarousel
                                                        HomeLayoutDesign.COMPACT_GRID -> Icons.Filled.Apps
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.Black else Color(0xFF00E5FF),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = design.title,
                                                        color = if (isSelected) Color.Black else Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = when (design) {
                                                            HomeLayoutDesign.PIXEL_MODERN -> "4x4 dengeli ızgara & At-a-Glance"
                                                            HomeLayoutDesign.CARD_DECK -> "Kart tabanlı widget akışı & geniş önizleme"
                                                            HomeLayoutDesign.COMPACT_GRID -> "Yoğun 5 kolonlu kompakt ızgara"
                                                        },
                                                        color = if (isSelected) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.65f),
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }
                                            if (isSelected) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color.Black
                                                ) {
                                                    Text(
                                                        text = "[AKTİF]",
                                                        color = Color(0xFF00E676),
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = {
                                viewModel.setLauncherFromRecovery(HomeLayoutDesign.PIXEL_MODERN)
                                recoveryLogMessage = "-- Başlatıcı sıfırlandı: Vroxen UI Pixel Modern\n/system/etc/default_launcher.cfg varsayılana getirildi.\nYeniden başlatmaya hazır."
                                showLauncherSelectorDialog = false
                            }
                        ) {
                            Text("Varsayılana Sıfırla", color = Color(0xFFFF9100), fontSize = 12.sp)
                        }
                        TextButton(onClick = { showLauncherSelectorDialog = false }) {
                            Text("Kapat", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                containerColor = Color(0xFF141E17),
                textContentColor = Color.White
            )
        }
    }
}

@Composable
private fun DeviceLauncherItemCard(
    app: DeviceInstalledApp,
    isThisApp: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val imageBitmap = remember(app.iconDrawable) {
        app.iconDrawable?.toBitmapOrNull()?.asImageBitmap()
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) Color(0xFF00E676) else Color(0xFF263238),
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = app.appName,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color.Black else Color(0xFF37474F)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isThisApp) Icons.Filled.Home else Icons.Filled.Android,
                            contentDescription = null,
                            tint = if (isSelected) Color(0xFF00E676) else Color(0xFF3DDC84),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isThisApp) "${app.appName} (Vroxen UI)" else app.appName,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isThisApp) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) Color.Black else Color(0xFF0284C7)
                            ) {
                                Text(
                                    text = "SİSTEM",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${app.packageName} • v${app.versionName}",
                        color = if (isSelected) Color.Black.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.55f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black
                ) {
                    Text(
                        text = "[AKTİF]",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
