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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.ui.system.getAppIcon
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VDeskApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateHome()
    }

    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkMode.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val isDexActive by viewModel.isDexModeActive.collectAsState()
    val openWindows by viewModel.dexOpenWindows.collectAsState()
    val activeWindow by viewModel.dexActiveWindow.collectAsState()
    val minimizedWindows by viewModel.dexMinimizedWindows.collectAsState()
    val maximizedWindows by viewModel.dexMaximizedWindows.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Genel Bakış & Başlatıcı, 1: Sanal Masaüstleri, 2: Pencereler & Görevler, 3: Ekran & Ayarlar
    var activeWorkspaceIndex by remember { mutableIntStateOf(0) } // 0: Genel, 1: Üretkenlik, 2: Geliştirici, 3: Medya
    var resolutionMode by remember { mutableStateOf("1080p (Full HD)") }
    var taskbarPosition by remember { mutableStateOf("Alt (Bottom)") }
    var isWirelessCastConnected by remember { mutableStateOf(false) }
    var isConnectingCast by remember { mutableStateOf(false) }

    val workspaces = remember {
        listOf(
            Triple("Masaüstü 1", "Genel & Web", listOf(AppId.BROWSER, AppId.FILES, AppId.NOTES)),
            Triple("Masaüstü 2", "İş & Üretkenlik", listOf(AppId.TASKS, AppId.CALCULATOR, AppId.ACCOUNT)),
            Triple("Masaüstü 3", "Geliştirici & Terminal", listOf(AppId.TERMINAL, AppId.PACKAGE_MANAGER, AppId.SETTINGS)),
            Triple("Masaüstü 4", "Medya & Stüdyo", listOf(AppId.MUSIC, AppId.PAINT, AppId.ARCADE))
        )
    }

    val brandBlue = Color(0xFF2563EB)
    val brandCyan = Color(0xFF00D2FF)
    val brandPurple = Color(0xFF7C3AED)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = brandBlue,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.DesktopWindows,
                                    contentDescription = "vdesk",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "vdesk Workspace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                if (isDexActive) "Masaüstü Motoru Aktif • 60 FPS" else "Masaüstü Bekleme Modunda",
                                fontSize = 11.sp,
                                color = if (isDexActive) Color(0xFF00E676) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = {
                            viewModel.toggleDexMode()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isDexActive) Color(0xFFDC2626) else brandBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            if (isDexActive) Icons.Default.Close else Icons.Default.OpenInNew,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isDexActive) "Kapat" else "Başlat",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Genel Bakış", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Dashboard, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Masaüstleri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.GridView, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Pencereler (${openWindows.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Layers, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Ayarlar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> OverviewTabContent(
                    viewModel = viewModel,
                    isDexActive = isDexActive,
                    openWindows = openWindows,
                    onLaunchDex = { viewModel.setDexModeActive(true) },
                    onOpenAppInDex = { appId ->
                        viewModel.openDexApp(appId)
                        viewModel.setDexModeActive(true)
                    }
                )
                1 -> VirtualDesktopsTabContent(
                    workspaces = workspaces,
                    activeWorkspaceIndex = activeWorkspaceIndex,
                    onSelectWorkspace = { idx ->
                        activeWorkspaceIndex = idx
                        val targetApps = workspaces[idx].third
                        targetApps.forEach { viewModel.openDexApp(it) }
                        viewModel.setDexModeActive(true)
                    }
                )
                2 -> WindowsManagerTabContent(
                    viewModel = viewModel,
                    openWindows = openWindows,
                    activeWindow = activeWindow,
                    minimizedWindows = minimizedWindows,
                    maximizedWindows = maximizedWindows,
                    onFocus = { viewModel.focusDexWindow(it) },
                    onMinimize = { viewModel.minimizeDexApp(it) },
                    onMaximize = { viewModel.maximizeDexApp(it) },
                    onClose = { viewModel.closeDexApp(it) },
                    onCloseAll = {
                        openWindows.toList().forEach { viewModel.closeDexApp(it) }
                    }
                )
                3 -> SettingsTabContent(
                    resolutionMode = resolutionMode,
                    onResolutionChange = { resolutionMode = it },
                    taskbarPosition = taskbarPosition,
                    onTaskbarPositionChange = { taskbarPosition = it },
                    isWirelessCastConnected = isWirelessCastConnected,
                    isConnectingCast = isConnectingCast,
                    onToggleCast = {
                        if (isWirelessCastConnected) {
                            isWirelessCastConnected = false
                        } else {
                            coroutineScope.launch {
                                isConnectingCast = true
                                delay(1200)
                                isConnectingCast = false
                                isWirelessCastConnected = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    viewModel: OSViewModel,
    isDexActive: Boolean,
    openWindows: List<AppId>,
    onLaunchDex: () -> Unit,
    onOpenAppInDex: (AppId) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Launch Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp))
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF0284C7))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isDexActive) Color(0xFF00E676) else Color(0xFFFFB300))
                                    )
                                    Text(
                                        if (isDexActive) "Masaüstü Çalışıyor" else "vdesk Hazır",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                "Vroxen UI v2.4 Desktop",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            "vdesk Çoklu Pencere & Masaüstü Deneyimi",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Cihazınızı tam teşekküllü bir masaüstü bilgisayara dönüştürün. Çoklu pencereler, görev çubuğu ve sanal masaüstleri ile maksimum üretkenlik.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Button(
                            onClick = onLaunchDex,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1E3A8A)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LaptopChromebook, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isDexActive) "Masaüstü Ekranına Geç" else "vdesk Masaüstünü Başlat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Workspaces
        item {
            Text(
                "⚡ Hızlı Çalışma Alanı Şablonları",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PresetCard(
                    title = "Ofis & Çalışma",
                    icon = Icons.Default.WorkOutline,
                    color = Color(0xFF2563EB),
                    subtitle = "Chrome + Notlar",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.openDexApp(AppId.BROWSER)
                        viewModel.openDexApp(AppId.NOTES)
                        viewModel.setDexModeActive(true)
                    }
                )
                PresetCard(
                    title = "Yazılım Geliştirici",
                    icon = Icons.Default.Code,
                    color = Color(0xFF7C3AED),
                    subtitle = "Terminal + Dosyalar",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.openDexApp(AppId.TERMINAL)
                        viewModel.openDexApp(AppId.FILES)
                        viewModel.setDexModeActive(true)
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PresetCard(
                    title = "Kreatif Çizim",
                    icon = Icons.Default.Brush,
                    color = Color(0xFFE11D48),
                    subtitle = "Çizim + Müzik",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.openDexApp(AppId.PAINT)
                        viewModel.openDexApp(AppId.MUSIC)
                        viewModel.setDexModeActive(true)
                    }
                )
                PresetCard(
                    title = "Oyun & Eğlence",
                    icon = Icons.Default.SportsEsports,
                    color = Color(0xFF059669),
                    subtitle = "Arcade + Galeri",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.openDexApp(AppId.ARCADE)
                        viewModel.openDexApp(AppId.PHOTOS)
                        viewModel.setDexModeActive(true)
                    }
                )
            }
        }

        // Quick Launch App Grid for vdesk
        item {
            Text(
                "🚀 vdesk Masaüstünde Uygulama Aç",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            val desktopApps = listOf(
                AppId.BROWSER, AppId.FILES, AppId.TERMINAL, AppId.NOTES,
                AppId.CALCULATOR, AppId.MUSIC, AppId.PAINT, AppId.SETTINGS,
                AppId.ARCADE, AppId.TASKS, AppId.WEATHER, AppId.STORE
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(desktopApps) { app ->
                    val isAppOpen = openWindows.contains(app)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isAppOpen) BorderStroke(1.5.dp, Color(0xFF2563EB)) else null,
                        modifier = Modifier
                            .clickable { onOpenAppInDex(app) }
                            .padding(2.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(app.iconColorHex),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        getAppIcon(app),
                                        contentDescription = app.title,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                app.title,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun VirtualDesktopsTabContent(
    workspaces: List<Triple<String, String, List<AppId>>>,
    activeWorkspaceIndex: Int,
    onSelectWorkspace: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Sanal Masaüstü Alanları (Workspaces)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "Farklı görevler için izole çalışma alanları arasında tek dokunuşla geçiş yapın.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(workspaces.indices.toList()) { idx ->
            val ws = workspaces[idx]
            val isSelected = activeWorkspaceIndex == idx

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWorkspace(idx) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF64748B),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Column {
                                Text(ws.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(ws.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text("Aktif", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Previews of apps in workspace
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Uygulamalar:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ws.third.forEach { app ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(app.iconColorHex),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(getAppIcon(app), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WindowsManagerTabContent(
    viewModel: OSViewModel,
    openWindows: List<AppId>,
    activeWindow: AppId?,
    minimizedWindows: Set<AppId>,
    maximizedWindows: Set<AppId>,
    onFocus: (AppId) -> Unit,
    onMinimize: (AppId) -> Unit,
    onMaximize: (AppId) -> Unit,
    onClose: (AppId) -> Unit,
    onCloseAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Açık Pencereler (${openWindows.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                if (openWindows.isNotEmpty()) {
                    TextButton(onClick = onCloseAll) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tümünü Kapat", fontSize = 11.sp, color = Color(0xFFDC2626))
                    }
                }
            }
        }

        if (openWindows.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.LayersClear, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Açık Pencere Yok", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("vdesk masaüstünde henüz bir pencere açmadınız.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(openWindows) { app ->
                val isActive = activeWindow == app
                val isMinimized = minimizedWindows.contains(app)
                val isMaximized = maximizedWindows.contains(app)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isActive) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(app.iconColorHex),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(getAppIcon(app), null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }

                            Column {
                                Text(app.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    when {
                                        isMinimized -> "Küçültüldü (Görev Çubuğunda)"
                                        isMaximized -> "Tam Ekran"
                                        isActive -> "Ön Planda Aktif"
                                        else -> "Arka Planda Açık"
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = { onFocus(app) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Visibility, "Focus", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onMinimize(app) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Minimize, "Minimize", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onMaximize(app) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.CropSquare, "Maximize", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onClose(app) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Close, "Close", tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    resolutionMode: String,
    onResolutionChange: (String) -> Unit,
    taskbarPosition: String,
    onTaskbarPositionChange: (String) -> Unit,
    isWirelessCastConnected: Boolean,
    isConnectingCast: Boolean,
    onToggleCast: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🖥️ Ekran & Görüntü Ayarları", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Masaüstü Çözünürlüğü", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1080p (Full HD)", "2K Geniş", "Kompakt").forEach { res ->
                            FilterChip(
                                selected = resolutionMode == res,
                                onClick = { onResolutionChange(res) },
                                label = { Text(res, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Görev Çubuğu Konumu", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Alt (Bottom)", "Üst (Top)", "Otomatik Gizle").forEach { pos ->
                            FilterChip(
                                selected = taskbarPosition == pos,
                                onClick = { onTaskbarPositionChange(pos) },
                                label = { Text(pos, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("📡 Kablosuz Ekran & Monitör Yansıtma", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Kablosuz Monitör Bağlantısı", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            if (isWirelessCastConnected) "Bağlandı: UltraHD 4K Display" else "Yakındaki ekranlar aranıyor...",
                            fontSize = 11.sp,
                            color = if (isWirelessCastConnected) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onToggleCast,
                        enabled = !isConnectingCast,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isWirelessCastConnected) Color(0xFFDC2626) else Color(0xFF2563EB)
                        )
                    ) {
                        if (isConnectingCast) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (isWirelessCastConnected) "Bağlantıyı Kes" else "Bağlan", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
