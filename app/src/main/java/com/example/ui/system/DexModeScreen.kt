package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.ui.apps.*
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DexModeScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val isDexStartMenuOpen by viewModel.isDexStartMenuOpen.collectAsState()
    val dexOpenWindows by viewModel.dexOpenWindows.collectAsState()
    val dexActiveWindow by viewModel.dexActiveWindow.collectAsState()
    val dexMinimizedWindows by viewModel.dexMinimizedWindows.collectAsState()
    val dexMaximizedWindows by viewModel.dexMaximizedWindows.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val systemTime by viewModel.systemTime.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    val timeFormatted = remember(systemTime) { SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(systemTime)) }
    val dateFormatted = remember(systemTime) { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(systemTime)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF090D16))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- DeX Top Bar Header ---
            Surface(
                color = Color.Black.copy(alpha = 0.55f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF2563EB)
                        ) {
                            Text(
                                text = "vdesk",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "vos Desktop Workspace • $currentLang",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "$timeFormatted • $dateFormatted",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Exit DeX Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.85f),
                            modifier = Modifier.clickable { viewModel.setDexModeActive(false) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.PowerSettingsNew, "Exit vdesk", tint = Color.White, modifier = Modifier.size(12.dp))
                                Text(com.example.util.TranslationManager.getTranslation("Exit vdesk", currentLang), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // --- DeX Desktop Workspace Canvas ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable(
                        onClick = { viewModel.closeDexStartMenu() },
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
            ) {
                // Desktop App Grid Icons (Left Column)
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .width(100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DesktopAppIcon("My Files", Icons.Filled.FolderSpecial, Color(0xFF3B82F6), currentLang) {
                        viewModel.openDexApp(AppId.FILES)
                    }
                    DesktopAppIcon("Internet", Icons.Filled.Language, Color(0xFF0EA5E9), currentLang) {
                        viewModel.openDexApp(AppId.BROWSER)
                    }
                    DesktopAppIcon("Terminal", Icons.Filled.Terminal, Color(0xFF10B981), currentLang) {
                        viewModel.openDexApp(AppId.TERMINAL)
                    }
                    DesktopAppIcon("Settings", Icons.Filled.Settings, Color(0xFF6366F1), currentLang) {
                        viewModel.openDexApp(AppId.SETTINGS)
                    }
                    DesktopAppIcon("Gallery", Icons.Filled.PhotoLibrary, Color(0xFFEC4899), currentLang) {
                        viewModel.openDexApp(AppId.PHOTOS)
                    }
                    DesktopAppIcon("Notes", Icons.Filled.NoteAlt, Color(0xFFF59E0B), currentLang) {
                        viewModel.openDexApp(AppId.NOTES)
                    }
                }

                // Multi-Window Floating Apps Layer
                dexOpenWindows.forEach { appId ->
                    val isMinimized = dexMinimizedWindows.contains(appId)
                    val isMaximized = dexMaximizedWindows.contains(appId)
                    val isFocused = dexActiveWindow == appId

                    if (!isMinimized) {
                        DexWindowFrame(
                            appId = appId,
                            isFocused = isFocused,
                            isMaximized = isMaximized,
                            currentLang = currentLang,
                            onFocus = { viewModel.focusDexWindow(appId) },
                            onMinimize = { viewModel.minimizeDexApp(appId) },
                            onMaximize = { viewModel.maximizeDexApp(appId) },
                            onClose = { viewModel.closeDexApp(appId) },
                            modifier = Modifier
                                .then(
                                    if (isMaximized) Modifier.fillMaxSize()
                                    else Modifier
                                        .fillMaxWidth(0.92f)
                                        .fillMaxHeight(0.88f)
                                        .align(Alignment.Center)
                                )
                        ) {
                            when (appId) {
                                AppId.BROWSER -> BrowserApp(viewModel)
                                AppId.FILES -> FilesApp(viewModel)
                                AppId.TERMINAL -> TerminalApp(viewModel)
                                AppId.SETTINGS -> SettingsApp(viewModel)
                                AppId.PHOTOS -> GalleryApp(viewModel)
                                AppId.NOTES -> NotesApp(viewModel)
                                AppId.CALCULATOR -> CalculatorApp(viewModel)
                                AppId.CAMERA -> CameraApp(viewModel)
                                AppId.MUSIC -> MusicApp(viewModel)
                                AppId.MESSAGES -> MessagesApp(viewModel)
                                AppId.PHONE -> PhoneApp(viewModel)
                                AppId.TASKS -> TasksApp(viewModel)
                                AppId.ARCADE -> ArcadeApp(viewModel)
                                else -> HomeScreen(viewModel)
                            }
                        }
                    }
                }

                // --- Samsung DeX Start Menu Popup ---
                if (isDexStartMenuOpen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 8.dp)
                    ) {
                        DexStartMenuPopup(
                            viewModel = viewModel,
                            onOpenApp = { appId ->
                                viewModel.openDexApp(appId)
                            }
                        )
                    }
                }
            }

            // --- Samsung DeX Bottom Taskbar ---
            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Taskbar Controls: DeX Start Button + Home + Apps
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Samsung DeX Start Menu Toggle Button
                        Button(
                            onClick = { viewModel.toggleDexStartMenu() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDexStartMenuOpen) Color(0xFF2563EB) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Apps, "vdesk Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("vdesk", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }

                        IconButton(onClick = { viewModel.navigateHome() }) {
                            Icon(Icons.Filled.Home, "Home", tint = Color.White.copy(alpha = 0.85f))
                        }

                        IconButton(onClick = { viewModel.toggleAppDrawer() }) {
                            Icon(Icons.Filled.Grid3x3, "All Apps", tint = Color.White.copy(alpha = 0.85f))
                        }
                    }

                    // Middle Taskbar: Open Windowed Apps
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    ) {
                        dexOpenWindows.forEach { appId ->
                            val isFocused = dexActiveWindow == appId
                            val isMinimized = dexMinimizedWindows.contains(appId)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isFocused && !isMinimized) Color(0xFF3B82F6).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .clickable {
                                        if (isFocused && !isMinimized) {
                                            viewModel.minimizeDexApp(appId)
                                        } else {
                                            viewModel.focusDexWindow(appId)
                                        }
                                    }
                                    .border(
                                        1.dp,
                                        if (isFocused) Color(0xFF3B82F6) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        getDexIcon(appId),
                                        contentDescription = appId.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = getAppName(appId, currentLang),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Right Taskbar System Tray
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Settings Shade Toggle Button
                        IconButton(onClick = { viewModel.toggleShade() }) {
                            Icon(Icons.Filled.Tune, "Quick Settings", tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        // Battery Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isCharging) {
                                    Icon(Icons.Filled.Bolt, "Charging", tint = Color(0xFF00E676), modifier = Modifier.size(12.dp))
                                }
                                Text("$batteryLevel%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Clock Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.clickable { viewModel.toggleShade() }
                        ) {
                            Text(
                                text = timeFormatted,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopAppIcon(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    currentLang: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = tint.copy(alpha = 0.22f),
            modifier = Modifier
                .size(54.dp)
                .border(1.dp, tint.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = name, tint = tint, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = com.example.util.TranslationManager.getTranslation(name, currentLang),
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DexWindowFrame(
    appId: AppId,
    isFocused: Boolean,
    isMaximized: Boolean,
    currentLang: String = "English",
    onFocus: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        shape = if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        shadowElevation = if (isFocused) 16.dp else 4.dp,
        modifier = modifier
            .border(
                1.dp,
                if (isFocused) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.15f),
                if (isMaximized) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onFocus)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Window Header Bar
            Surface(
                color = if (isFocused) Color(0xFF0F172A) else Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(getDexIcon(appId), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(getAppName(appId, currentLang), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minimize
                        IconButton(onClick = onMinimize, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Remove, "Minimize", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                        }
                        // Maximize
                        IconButton(onClick = onMaximize, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.CropSquare, "Maximize", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(12.dp))
                        }
                        // Close
                        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Close, "Close", tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Window App Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun DexStartMenuPopup(
    viewModel: OSViewModel,
    onOpenApp: (AppId) -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val allApps = remember {
        listOf(
            AppId.BROWSER, AppId.FILES, AppId.TERMINAL, AppId.SETTINGS,
            AppId.PHOTOS, AppId.NOTES, AppId.CALCULATOR, AppId.CAMERA,
            AppId.MUSIC, AppId.MESSAGES, AppId.PHONE, AppId.TASKS,
            AppId.ARCADE, AppId.STORE, AppId.CLOCK, AppId.ACCOUNT
        )
    }

    val filteredApps = remember(searchQuery, currentLang) {
        if (searchQuery.isBlank()) allApps
        else allApps.filter { getAppName(it, currentLang).contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.98f),
        shadowElevation = 24.dp,
        modifier = Modifier
            .width(360.dp)
            .height(480.dp)
            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Search Bar & Profile
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Person, "User", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("vos User", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("vdesk Mode", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF3B82F6).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "40 Languages",
                            color = Color(0xFF60A5FA),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(com.example.util.TranslationManager.getTranslation("Search 40 Languages", currentLang), fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = Color.White) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // App Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                items(filteredApps) { appId ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenApp(appId) }
                            .padding(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    getDexIcon(appId),
                                    contentDescription = getAppName(appId, currentLang),
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getAppName(appId, currentLang),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.openDexApp(AppId.SETTINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Filled.Settings, "Settings", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("vdesk Settings", fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = { viewModel.setDexModeActive(false) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Filled.PowerSettingsNew, "Exit vdesk", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(com.example.util.TranslationManager.getTranslation("Exit vdesk", currentLang), fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

private fun getAppName(appId: AppId, lang: String = "English"): String {
    val key = when (appId) {
        AppId.PHONE -> "Phone"
        AppId.MESSAGES -> "Messages"
        AppId.SETTINGS -> "Settings"
        AppId.BROWSER -> "Internet"
        AppId.CAMERA -> "Camera"
        AppId.PHOTOS -> "Gallery"
        AppId.MUSIC -> "Music"
        AppId.CLOCK -> "Clock"
        AppId.NOTES -> "Notes"
        AppId.CALCULATOR -> "Calculator"
        AppId.FILES -> "Files"
        AppId.STORE -> "Play Store"
        AppId.ARCADE -> "Arcade"
        AppId.TERMINAL -> "Terminal"
        AppId.WEATHER -> "Weather"
        AppId.TASKS -> "Tasks"
        AppId.ACCOUNT -> "Account"
        AppId.CUSTOM_APP -> "Sideload App"
    }
    return com.example.util.TranslationManager.getTranslation(key, lang)
}

private fun getDexIcon(appId: AppId): androidx.compose.ui.graphics.vector.ImageVector = when (appId) {
    AppId.PHONE -> Icons.Filled.Phone
    AppId.MESSAGES -> Icons.Filled.Chat
    AppId.SETTINGS -> Icons.Filled.Settings
    AppId.BROWSER -> Icons.Filled.Language
    AppId.CAMERA -> Icons.Filled.PhotoCamera
    AppId.PHOTOS -> Icons.Filled.PhotoLibrary
    AppId.MUSIC -> Icons.Filled.MusicNote
    AppId.CLOCK -> Icons.Filled.Schedule
    AppId.NOTES -> Icons.Filled.NoteAlt
    AppId.CALCULATOR -> Icons.Filled.Calculate
    AppId.FILES -> Icons.Filled.FolderSpecial
    AppId.STORE -> Icons.Filled.ShoppingBag
    AppId.ARCADE -> Icons.Filled.SportsEsports
    AppId.TERMINAL -> Icons.Filled.Terminal
    AppId.WEATHER -> Icons.Filled.Cloud
    AppId.TASKS -> Icons.Filled.CheckCircle
    AppId.ACCOUNT -> Icons.Filled.AccountCircle
    AppId.CUSTOM_APP -> Icons.Filled.CloudDownload
}
