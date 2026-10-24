package com.example.ui.system

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.model.AppId
import com.example.model.HomeWidgetType
import com.example.model.StoreAppItem
import com.example.ui.theme.LocalIsDarkMode
import com.example.util.DeviceInstalledApp
import com.example.util.PackageManagerUtil
import com.example.util.toBitmapOrNull
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

sealed class LauncherGridItem {
    data class SystemApp(val appId: AppId) : LauncherGridItem()
    data class DeviceApp(val app: DeviceInstalledApp) : LauncherGridItem()

    val uniqueKey: String
        get() = when (this) {
            is SystemApp -> "sys_${appId.name}"
            is DeviceApp -> "pkg_${app.packageName}"
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val deviceInstalledApps = remember(context) { PackageManagerUtil.getInstalledDeviceApps(context) }
    val homeGridFilterMode by viewModel.homeGridFilterMode.collectAsState()
    val deviceHomeApps by viewModel.deviceHomeApps.collectAsState()

    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val musicTracks by viewModel.musicTracks.collectAsState()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsState()
    val storeApps by viewModel.storeApps.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val remoteConfigVersion by viewModel.remoteConfigVersion.collectAsState()
    val remoteConfigStatus by viewModel.remoteConfigStatus.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeWidgetIndex by remember { mutableIntStateOf(0) } // 0: Music, 1: Notes, 2: Wellbeing
    var selectedHomeGridMode by remember { mutableIntStateOf(0) } // 0: System Apps, 1: All Device Apps (PackageManager)
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkMode.current
    val widgetCardBg = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.85f)
    val widgetPrimaryText = if (isDark) Color.White else Color(0xFF191C1E)
    val widgetSecondaryText = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF43474E)
    val searchBarBg = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f)
    val searchBarText = if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF43474E)

    val mainApps by viewModel.homeApps.collectAsState()
    val customAppIcons by viewModel.customAppIcons.collectAsState()
    var showCustomIconDialog by remember { mutableStateOf(false) }
    val homeLayoutDesign by viewModel.homeLayoutDesign.collectAsState()
    val selectedExternalLauncherPackage by viewModel.selectedExternalLauncherPackage.collectAsState()
    val selectedExternalLauncherName by viewModel.selectedExternalLauncherName.collectAsState()
    val isAssistantOpen by viewModel.isAssistantOpen.collectAsState()
    val assistantWidgets by viewModel.assistantWidgets.collectAsState()
    val assistantEnabled by viewModel.assistantEnabled.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val activeWidgets by viewModel.activeHomeWidgets.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(false) }
    var selectedHomeAppForMenu by remember { mutableStateOf<AppId?>(null) }
    var selectedHomeDeviceAppForMenu by remember { mutableStateOf<DeviceInstalledApp?>(null) }
    var selectedWidgetForMenu by remember { mutableStateOf<HomeWidgetType?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showDesignDialog by remember { mutableStateOf(false) }
    var showIconStyleDialog by remember { mutableStateOf(false) }
    var showAddWidgetDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15) {
                        viewModel.openAppDrawer()
                    } else if (dragAmount > 25) {
                        viewModel.expandShade()
                    }
                }
            }
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        val isTablet = maxWidth >= 600.dp
        val isExpandedTablet = maxWidth >= 840.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1100.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BackHandler(enabled = isEditMode) {
                isEditMode = false
            }

            // Edit Mode Configuration Banner
            AnimatedVisibility(
                visible = isEditMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Title + Done Button Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Ana Ekranı Düzenle",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Simgeleri sıralayın, silin veya özelleştirin",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = { isEditMode = false },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Filled.Check, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Bitti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Horizontally scrollable actions row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { showAddWidgetDialog = true },
                                label = { Text("Widget'lar", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Widgets, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = { showAddAppDialog = true },
                                label = { Text("Uygulama Ekle", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = { viewModel.openApp(AppId.WALLPAPERS) },
                                label = { Text("Duvar Kağıdı", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Wallpaper, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = { showDesignDialog = true },
                                label = { Text("Düzen Tasarımı", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Palette, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = { showCustomIconDialog = true },
                                label = { Text("Simgeler", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.ImageSearch, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = { showIconStyleDialog = true },
                                label = { Text("Simge Tarzı", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.AutoFixHigh, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            )
                            AssistChip(
                                onClick = {
                                    viewModel.resetHomeApps()
                                    Toast.makeText(context, "Ana ekran varsayılana sıfırlandı", Toast.LENGTH_SHORT).show()
                                },
                                label = { Text("Sıfırla", fontSize = 11.sp, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                }
            }

            // Top: Smart At-A-Glance & Interactive Widget Area
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Persistent Real-Time Date and Time At-A-Glance Widget
                PersistentDateTimeHeader(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    onOpenClock = { viewModel.openApp(AppId.CLOCK) },
                    onOpenTasks = { viewModel.openApp(AppId.TASKS) },
                    onOpenWeather = { viewModel.openApp(AppId.WEATHER) }
                )

                // Interactive Dynamic Switchable Widget Card (Only shown when widgets exist)
                if (activeWidgets.isNotEmpty()) {
                    val safeWidgetIndex = (activeWidgetIndex).coerceIn(0, activeWidgets.size - 1)
                    val currentWidget = activeWidgets[safeWidgetIndex]

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = widgetCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .combinedClickable(
                                onClick = {
                                    if (!isEditMode) {
                                        when (currentWidget) {
                                            HomeWidgetType.CLOCK -> viewModel.openApp(AppId.CLOCK)
                                            HomeWidgetType.BATTERY_STATUS -> viewModel.openApp(AppId.SETTINGS)
                                            HomeWidgetType.MUSIC -> viewModel.openApp(AppId.MUSIC)
                                            HomeWidgetType.NOTES -> viewModel.openApp(AppId.NOTES)
                                            HomeWidgetType.BATTERY_WELLBEING -> viewModel.openApp(AppId.SETTINGS)
                                            HomeWidgetType.WEATHER -> viewModel.openApp(AppId.WEATHER)
                                            HomeWidgetType.QUICK_TOGGLES -> viewModel.openApp(AppId.SETTINGS)
                                        }
                                    }
                                },
                                onLongClick = { selectedWidgetForMenu = currentWidget }
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            // Widget Content
                            when (currentWidget) {
                                HomeWidgetType.CLOCK -> {
                                    DateTimeClockCard(
                                        viewModel = viewModel,
                                        onOpenClock = { viewModel.openApp(AppId.CLOCK) }
                                    )
                                }
                                HomeWidgetType.BATTERY_STATUS -> {
                                    BatteryStatusWidget(
                                        viewModel = viewModel,
                                        onOpenDetails = { viewModel.openApp(AppId.SETTINGS) }
                                    )
                                }
                                HomeWidgetType.MUSIC -> {
                                    HomeMusicWidget(
                                        viewModel = viewModel,
                                        widgetPrimaryText = widgetPrimaryText,
                                        widgetSecondaryText = widgetSecondaryText
                                    )
                                }
                                HomeWidgetType.NOTES -> {
                                    HomeNotesWidget(
                                        viewModel = viewModel,
                                        widgetPrimaryText = widgetPrimaryText,
                                        widgetSecondaryText = widgetSecondaryText
                                    )
                                }
                                HomeWidgetType.BATTERY_WELLBEING -> {
                                    HomeWellbeingWidget(
                                        viewModel = viewModel,
                                        widgetPrimaryText = widgetPrimaryText,
                                        widgetSecondaryText = widgetSecondaryText
                                    )
                                }
                                HomeWidgetType.WEATHER -> {
                                    HomeWeatherWidget(
                                        viewModel = viewModel,
                                        widgetPrimaryText = widgetPrimaryText,
                                        widgetSecondaryText = widgetSecondaryText
                                    )
                                }
                                HomeWidgetType.QUICK_TOGGLES -> {
                                    HomeQuickTogglesWidget(
                                        viewModel = viewModel,
                                        widgetPrimaryText = widgetPrimaryText,
                                        widgetSecondaryText = widgetSecondaryText
                                    )
                                }
                            }

                            // Edit Mode Overlay Controls
                            if (isEditMode) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            currentWidget.title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            modifier = Modifier.widthIn(max = 90.dp),
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { showAddWidgetDialog = true },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.AddCircleOutline,
                                                contentDescription = "Widget Ekle",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.removeHomeWidget(currentWidget)
                                                if (activeWidgetIndex > 0 && activeWidgetIndex >= activeWidgets.size - 1) {
                                                    activeWidgetIndex = maxOf(0, activeWidgets.size - 2)
                                                }
                                            },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.Delete,
                                                contentDescription = "Widget'ı Kaldır",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Switcher Dots and Navigation
                            if (activeWidgets.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            activeWidgetIndex = if (safeWidgetIndex > 0) safeWidgetIndex - 1 else activeWidgets.size - 1
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.KeyboardArrowLeft,
                                            contentDescription = "Önceki",
                                            tint = widgetSecondaryText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    for (i in activeWidgets.indices) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (safeWidgetIndex == i) 7.dp else 5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (safeWidgetIndex == i) MaterialTheme.colorScheme.primary
                                                    else if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)
                                                )
                                                .clickable { activeWidgetIndex = i }
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            activeWidgetIndex = (safeWidgetIndex + 1) % activeWidgets.size
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.KeyboardArrowRight,
                                            contentDescription = "Sonraki",
                                            tint = widgetSecondaryText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recovery-selected External Launcher Banner
            if (selectedExternalLauncherPackage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Aktif Başlatıcı: ${selectedExternalLauncherName ?: selectedExternalLauncherPackage}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Kurtarma modunda seçildi ($selectedExternalLauncherPackage)",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    PackageManagerUtil.launchPackage(context, selectedExternalLauncherPackage!!)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Aç", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = {
                                    viewModel.setSelectedExternalLauncher(null, null)
                                },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Vroxen'a Dön", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Unified App Source Selector Header (Tümü, Vroxen UI, Paket Yöneticisi)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val filterOptions = listOf(
                            Triple(0, "Tümü (${mainApps.size + deviceInstalledApps.size})", Icons.Default.Apps),
                            Triple(1, "Vroxen UI (${mainApps.size})", Icons.Default.Widgets),
                            Triple(2, "Paketler (${deviceInstalledApps.size})", Icons.Default.Android)
                        )
                        filterOptions.forEach { (mode, label, icon) ->
                            val isSelected = homeGridFilterMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.setHomeGridFilterMode(mode) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick button to open full Package Manager app
                FilledTonalIconButton(
                    onClick = { viewModel.openApp(AppId.PACKAGE_MANAGER) },
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.SettingsApplications,
                        contentDescription = "Paket Yöneticisi",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Paged Home Apps (16/20/25 apps per page)
            val activeGridItems: List<LauncherGridItem> = remember(mainApps, deviceInstalledApps, homeGridFilterMode, deviceHomeApps) {
                when (homeGridFilterMode) {
                    1 -> mainApps.map { LauncherGridItem.SystemApp(it) }
                    2 -> deviceInstalledApps.map { LauncherGridItem.DeviceApp(it) }
                    else -> {
                        // Unified: All system apps + all device installed apps
                        val list = mutableListOf<LauncherGridItem>()
                        list.addAll(mainApps.map { LauncherGridItem.SystemApp(it) })
                        list.addAll(deviceInstalledApps.map { LauncherGridItem.DeviceApp(it) })
                        list
                    }
                }
            }

            val gridColumns = when {
                isExpandedTablet -> if (homeLayoutDesign == com.example.model.HomeLayoutDesign.COMPACT_GRID) 7 else 6
                isTablet -> if (homeLayoutDesign == com.example.model.HomeLayoutDesign.COMPACT_GRID) 6 else 5
                homeLayoutDesign == com.example.model.HomeLayoutDesign.COMPACT_GRID -> 5
                else -> 4
            }
            val pageSize = when {
                isExpandedTablet -> if (activeWidgets.isEmpty()) 35 else 28
                isTablet -> if (activeWidgets.isEmpty()) 30 else 24
                homeLayoutDesign == com.example.model.HomeLayoutDesign.COMPACT_GRID -> if (activeWidgets.isEmpty()) 25 else 20
                activeWidgets.isEmpty() -> 20
                else -> 16
            }
            val pagedHomeApps = remember(activeGridItems, pageSize) {
                val chunks = activeGridItems.chunked(pageSize)
                if (chunks.isEmpty()) listOf(emptyList()) else chunks
            }

            val homePagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { pagedHomeApps.size.coerceAtLeast(1) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                HorizontalPager(
                    state = homePagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { pageIndex ->
                    val pageApps = pagedHomeApps.getOrNull(pageIndex) ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(if (activeWidgets.isEmpty()) 10.dp else 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(
                            items = pageApps,
                            key = { _, item -> item.uniqueKey }
                        ) { index, gridItem ->
                            val globalIndex = pageIndex * pageSize + index
                            when (gridItem) {
                                is LauncherGridItem.SystemApp -> {
                                    val appId = gridItem.appId
                                    val hasUnread = notifications.any { it.appId == appId }
                                    val sysIndex = mainApps.indexOf(appId)
                                    AppIconTile(
                                        appId = appId,
                                        index = sysIndex,
                                        maxIndex = mainApps.size - 1,
                                        currentLang = currentLang,
                                        hasUnread = hasUnread,
                                        isEditMode = isEditMode,
                                        customIconUrl = customAppIcons[appId.packageName],
                                        iconStyle = iconStyle,
                                        onLongClick = {
                                            if (isEditMode) {
                                                // Already in edit mode
                                            } else {
                                                selectedHomeAppForMenu = appId
                                            }
                                        },
                                        onDelete = { viewModel.removeHomeApp(appId) },
                                        onReorder = { from, to ->
                                            if (from >= 0 && to in mainApps.indices) {
                                                viewModel.reorderHomeApps(from, to)
                                            }
                                        },
                                        onClick = { viewModel.openApp(appId) }
                                    )
                                }
                                is LauncherGridItem.DeviceApp -> {
                                    val deviceApp = gridItem.app
                                    DeviceAppIconTile(
                                        app = deviceApp,
                                        index = globalIndex,
                                        isEditMode = isEditMode,
                                        iconStyle = iconStyle,
                                        onLongClick = {
                                            if (isEditMode) {
                                                // Already in edit mode
                                            } else {
                                                selectedHomeDeviceAppForMenu = deviceApp
                                            }
                                        },
                                        onDelete = { viewModel.removeDeviceAppFromHome(deviceApp.packageName) },
                                        onClick = {
                                            val launched = PackageManagerUtil.launchPackage(context, deviceApp.packageName)
                                            if (!launched) {
                                                PackageManagerUtil.openAppDetailsSettings(context, deviceApp.packageName)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Add app tile button if editing on last page
                        if (isEditMode && pageIndex == pagedHomeApps.size - 1) {
                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { showAddAppDialog = true }
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = "Add App",
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Add App",
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Page Indicator Dots / Sliding Pill (Handles 1 to 20+ pages gracefully)
                if (pagedHomeApps.size > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagedHomeApps.size <= 7) {
                            repeat(pagedHomeApps.size) { index ->
                                val isSelected = homePagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (isSelected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (isDark) Color.White.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.25f)
                                        )
                                        .clickable {
                                            coroutineScope.launch {
                                                homePagerState.animateScrollToPage(index)
                                            }
                                        }
                                )
                            }
                        } else {
                            // Compact Page Pill for large capacity (e.g. 200 apps / 13 pages)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Sayfa ${homePagerState.currentPage + 1} / ${pagedHomeApps.size} (${mainApps.size} Uygulama)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF191C1E)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Google / Nexus Style Unified Search Bar
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = searchBarBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { viewModel.openApp(AppId.BROWSER) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Multi-color G logo badge
                        Text(
                            text = "G",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Search apps, web, contacts...",
                            color = searchBarText,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { viewModel.openVroxenAssistant() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = "Hey Vroxen",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text("Hey Vroxen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Hey Vroxen Voice",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { viewModel.openVroxenAssistant() }
                        )
                    }
                }
            }

            // Fixed, Persistent Bottom Dock
            PersistentDock(
                viewModel = viewModel,
                modifier = Modifier.padding(bottom = 6.dp),
                isInDrawer = false
            )
        }
    }

    // Add App Dialog selection
    val availableToAdd = remember(mainApps) {
        AppId.entries.filter { it !in mainApps }
    }

    if (showAddWidgetDialog) {
        AddWidgetDialog(
            viewModel = viewModel,
            onDismiss = { showAddWidgetDialog = false }
        )
    }

    if (showIconStyleDialog) {
        IconStyleDialog(
            viewModel = viewModel,
            onDismiss = { showIconStyleDialog = false }
        )
    }

    if (showCustomIconDialog) {
        CustomIconDialog(
            viewModel = viewModel,
            onDismiss = { showCustomIconDialog = false }
        )
    }

    if (showDesignDialog) {
        AlertDialog(
            onDismissRequest = { showDesignDialog = false },
            title = { Text("Ana Ekran Düzen Tasarımı", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.model.HomeLayoutDesign.entries.forEach { design ->
                        val isSelected = homeLayoutDesign == design
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setHomeLayoutDesign(design)
                                    showDesignDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    val designTitle = when (design) {
                                        com.example.model.HomeLayoutDesign.PIXEL_MODERN -> "Pixel Modern (4 Kolon)"
                                        com.example.model.HomeLayoutDesign.CARD_DECK -> "Kart Deste Düzeni"
                                        com.example.model.HomeLayoutDesign.COMPACT_GRID -> "Kompakt Izgara (5 Kolon)"
                                    }
                                    Text(designTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        when (design) {
                                            com.example.model.HomeLayoutDesign.PIXEL_MODERN -> "Temiz At-A-Glance widget'ı ve 4x4 dengeli uygulama ızgarası"
                                            com.example.model.HomeLayoutDesign.CARD_DECK -> "Kart tabanlı widget'lar ve medya odaklı düzen"
                                            com.example.model.HomeLayoutDesign.COMPACT_GRID -> "Yoğun 5 kolonlu ızgara ile ekranda daha fazla uygulama"
                                        },
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDesignDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Compact Long-Press App Shortcut Menu on Home Screen (Vroxen UI System Apps)
    selectedHomeAppForMenu?.let { app ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedHomeAppForMenu = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .widthIn(min = 250.dp, max = 285.dp)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Slim Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = getAppColor(app),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(getAppIcon(app), null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = app.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${app.category} • Sistem",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedHomeAppForMenu = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // 1. Open App (Primary)
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.PlayArrow,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Uygulamayı Aç",
                        isPrimary = true,
                        onClick = {
                            selectedHomeAppForMenu = null
                            viewModel.openApp(app)
                        }
                    )

                    // 2. Edit Home Screen Mode
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Edit,
                        iconTint = Color(0xFFF59E0B),
                        label = "Ana Ekranı Düzenle",
                        onClick = {
                            selectedHomeAppForMenu = null
                            isEditMode = true
                        }
                    )

                    // 3. Remove from Home Screen
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.RemoveCircleOutline,
                        iconTint = Color(0xFFDC2626),
                        label = "Ana Ekrandan Kaldır",
                        onClick = {
                            viewModel.removeHomeApp(app)
                            Toast.makeText(context, "${app.title} ana ekrandan kaldırıldı", Toast.LENGTH_SHORT).show()
                            selectedHomeAppForMenu = null
                        }
                    )

                    // 4. Open in vdesk Desktop
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.DesktopWindows,
                        iconTint = Color(0xFF0284C7),
                        label = "vdesk Masaüstünde Aç",
                        onClick = {
                            selectedHomeAppForMenu = null
                            viewModel.openDexApp(app)
                            viewModel.setDexModeActive(true)
                            Toast.makeText(context, "${app.title} vdesk penceresinde açıldı", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 5. Clear Cache
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.CleaningServices,
                        iconTint = Color(0xFF10B981),
                        label = "Önbelleği Temizle",
                        onClick = {
                            viewModel.clearAppCache(app)
                            Toast.makeText(context, "${app.title} önbelleği temizlendi ✨", Toast.LENGTH_SHORT).show()
                            selectedHomeAppForMenu = null
                        }
                    )
                }
            }
        }
    }

    // Compact Long-Press App Shortcut Menu on Home Screen (Device Package Manager Apps)
    selectedHomeDeviceAppForMenu?.let { devApp ->
        val devBitmap = remember(devApp.iconDrawable) { devApp.iconDrawable?.toBitmapOrNull()?.asImageBitmap() }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedHomeDeviceAppForMenu = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .widthIn(min = 250.dp, max = 285.dp)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Slim Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (devBitmap != null) {
                                        Image(
                                            bitmap = devBitmap,
                                            contentDescription = devApp.appName,
                                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(6.dp))
                                        )
                                    } else {
                                        Icon(Icons.Default.Android, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            Column {
                                Text(
                                    text = devApp.appName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "v${devApp.versionName} • Android Paketi",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedHomeDeviceAppForMenu = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // 1. Launch App (Primary)
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.PlayArrow,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Uygulamayı Çalıştır",
                        isPrimary = true,
                        onClick = {
                            selectedHomeDeviceAppForMenu = null
                            val launched = PackageManagerUtil.launchPackage(context, devApp.packageName)
                            if (!launched) {
                                PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                            }
                        }
                    )

                    // 2. Edit Home Screen Mode
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Edit,
                        iconTint = Color(0xFFF59E0B),
                        label = "Ana Ekranı Düzenle",
                        onClick = {
                            selectedHomeDeviceAppForMenu = null
                            isEditMode = true
                        }
                    )

                    // 3. Remove from Home Screen
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.RemoveCircleOutline,
                        iconTint = Color(0xFFDC2626),
                        label = "Ana Ekrandan Kaldır",
                        onClick = {
                            viewModel.removeDeviceAppFromHome(devApp.packageName)
                            Toast.makeText(context, "${devApp.appName} ana ekrandan kaldırıldı", Toast.LENGTH_SHORT).show()
                            selectedHomeDeviceAppForMenu = null
                        }
                    )

                    // 4. Android System App Settings
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFF64748B),
                        label = "Android Uygulama Ayarları",
                        onClick = {
                            PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                            selectedHomeDeviceAppForMenu = null
                        }
                    )
                }
            }
        }
    }

    // Compact Long-Press Widget Shortcut Menu on Home Screen
    selectedWidgetForMenu?.let { widget ->
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedWidgetForMenu = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .widthIn(min = 250.dp, max = 285.dp)
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Slim Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        when (widget) {
                                            HomeWidgetType.BATTERY_STATUS -> Icons.Filled.BatteryChargingFull
                                            HomeWidgetType.CLOCK -> Icons.Filled.Schedule
                                            HomeWidgetType.WEATHER -> Icons.Filled.WbSunny
                                            HomeWidgetType.MUSIC -> Icons.Filled.MusicNote
                                            HomeWidgetType.NOTES -> Icons.Filled.EditNote
                                            HomeWidgetType.BATTERY_WELLBEING -> Icons.Filled.HealthAndSafety
                                            HomeWidgetType.QUICK_TOGGLES -> Icons.Filled.Tune
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = widget.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Home Widget",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = { selectedWidgetForMenu = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Kapat", modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    // 1. Remove Widget
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.DeleteOutline,
                        iconTint = Color(0xFFDC2626),
                        label = "Widget'ı Kaldır",
                        isPrimary = false,
                        onClick = {
                            viewModel.removeHomeWidget(widget)
                            Toast.makeText(context, "${widget.title} kaldırıldı", Toast.LENGTH_SHORT).show()
                            selectedWidgetForMenu = null
                        }
                    )

                    // 2. Add / Manage Widgets Dialog
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Widgets,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Widget Ekle & Yönet",
                        isPrimary = true,
                        onClick = {
                            selectedWidgetForMenu = null
                            showAddWidgetDialog = true
                        }
                    )

                    // 3. Edit Mode
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Edit,
                        iconTint = Color(0xFFF59E0B),
                        label = "Ana Ekran Düzenleme Modu",
                        onClick = {
                            selectedWidgetForMenu = null
                            isEditMode = true
                        }
                    )

                    // 4. Open Related App
                    val relatedApp = when (widget) {
                        HomeWidgetType.CLOCK -> AppId.CLOCK
                        HomeWidgetType.BATTERY_STATUS -> AppId.SETTINGS
                        HomeWidgetType.WEATHER -> AppId.WEATHER
                        HomeWidgetType.MUSIC -> AppId.MUSIC
                        HomeWidgetType.NOTES -> AppId.NOTES
                        HomeWidgetType.BATTERY_WELLBEING -> AppId.SETTINGS
                        HomeWidgetType.QUICK_TOGGLES -> AppId.SETTINGS
                    }
                    HomeScreenCompactMenuRow(
                        icon = Icons.Default.Launch,
                        iconTint = Color(0xFF0284C7),
                        label = "${relatedApp.title} Aç",
                        onClick = {
                            selectedWidgetForMenu = null
                            viewModel.openApp(relatedApp)
                        }
                    )
                }
            }
        }
    }

    if (showAddAppDialog) {
        var addAppTab by remember { mutableIntStateOf(0) } // 0: Vroxen UI Apps, 1: PackageManager Apps

        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ana Ekrana Uygulama Ekle", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = addAppTab == 0,
                            onClick = { addAppTab = 0 },
                            label = { Text("Vroxen UI (${availableToAdd.size})", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Widgets, null, modifier = Modifier.size(14.dp)) }
                        )
                        FilterChip(
                            selected = addAppTab == 1,
                            onClick = { addAppTab = 1 },
                            label = { Text("Paketler (${deviceInstalledApps.size})", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Android, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                    if (addAppTab == 0) {
                        if (availableToAdd.isEmpty()) {
                            Text("Tüm Vroxen UI uygulamaları zaten ana ekranda mevcut.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(availableToAdd) { app ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                viewModel.addHomeApp(app)
                                                showAddAppDialog = false
                                            }
                                            .padding(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = getAppColor(app),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    getAppIcon(app),
                                                    contentDescription = app.title,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = com.example.util.TranslationManager.getTranslation(app.title, currentLang),
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        if (deviceInstalledApps.isEmpty()) {
                            Text("Cihazda Paket Yöneticisi uygulaması bulunamadı.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(deviceInstalledApps) { devApp ->
                                    val isPinned = deviceHomeApps.contains(devApp.packageName)
                                    val devBitmap = remember(devApp.iconDrawable) { devApp.iconDrawable?.toBitmapOrNull()?.asImageBitmap() }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (isPinned) {
                                                    viewModel.removeDeviceAppFromHome(devApp.packageName)
                                                } else {
                                                    viewModel.addDeviceAppToHome(devApp.packageName)
                                                }
                                                showAddAppDialog = false
                                            }
                                            .padding(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isPinned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (devBitmap != null) {
                                                    Image(
                                                        bitmap = devBitmap,
                                                        contentDescription = devApp.appName,
                                                        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Filled.Android,
                                                        contentDescription = devApp.appName,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = devApp.appName,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // --- Smart Assistant Panel UI (Overlay) ---
    var showAssistantSettings by remember { mutableStateOf(false) }
    var simRamUsage by remember { mutableStateOf(2.4f) }
    var isMemoryClearing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle grab-handle on far left
        if (!isAssistantOpen && assistantEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(14.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                    .clickable { viewModel.setAssistantOpen(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Overlay slide panel
        AnimatedVisibility(
            visible = isAssistantOpen && assistantEnabled,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Content Card (left 84% width)
                Card(
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF151922).copy(alpha = 0.98f) else Color(0xFFF2F6FC).copy(alpha = 0.98f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.84f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
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
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Aura Assistant",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Aura Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A1C1E)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { showAssistantSettings = !showAssistantSettings }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Configure Panel",
                                        tint = if (showAssistantSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.setAssistantOpen(false) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close Panel",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Configuration Widget Options (visible when Settings toggled)
                            if (showAssistantSettings) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color(0xFF232A3B) else Color(0xFFE4E9F2)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Configure Assistant",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isDark) Color.White else Color(0xFF1A1C1E)
                                                )
                                                TextButton(onClick = { showAssistantSettings = false }) {
                                                    Text("Close", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                                            )

                                            val list = listOf(
                                                "greeting" to "Welcome greeting",
                                                "system_monitor" to "System RAM Optimizer",
                                                "quick_notes" to "Keep Notes module",
                                                "quick_tasks" to "Tasks Checklist module",
                                                "recommendations" to "Smart Action recommendations"
                                            )

                                            list.forEach { (id, label) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF43474E)
                                                    )
                                                    Switch(
                                                        checked = assistantWidgets.contains(id),
                                                        onCheckedChange = { viewModel.toggleAssistantWidget(id) },
                                                        modifier = Modifier.scale(0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Dynamic Greeting Card
                            if (assistantWidgets.contains("greeting")) {
                                item {
                                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                                    val greeting = when (hour) {
                                        in 5..11 -> "Good morning"
                                        in 12..17 -> "Good afternoon"
                                        else -> "Good evening"
                                    }
                                    val user = viewModel.userAccount.collectAsState().value
                                    val displayName = if (user.username.isNotEmpty()) user.username else "User"

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(user.avatarEmoji.ifEmpty { "✨" }, fontSize = 24.sp)
                                            Column {
                                                Text(
                                                    text = "$greeting, $displayName",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Let's organize and speed up your system.",
                                                    fontSize = 11.sp,
                                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF43474E)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Resource Monitor & Dynamic Optimizer Card
                            if (assistantWidgets.contains("system_monitor")) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "System Performance Boost",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Icon(
                                                    Icons.Default.Speed,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Active RAM Memory", fontSize = 11.sp, color = widgetSecondaryText)
                                                    Text(
                                                        text = if (isMemoryClearing) "Recycling memory..." else "${"%.1f".format(simRamUsage)} GB / 4.0 GB",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        if (!isMemoryClearing) {
                                                            isMemoryClearing = true
                                                            coroutineScope.launch {
                                                                delay(1200)
                                                                simRamUsage = 1.4f
                                                                isMemoryClearing = false
                                                            }
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (isMemoryClearing) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(12.dp),
                                                                color = Color.White,
                                                                strokeWidth = 2.dp
                                                            )
                                                        } else {
                                                            Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(12.dp))
                                                        }
                                                        Text("Boost", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            LinearProgressIndicator(
                                                progress = { if (isMemoryClearing) 0.35f else (simRamUsage / 4.0f) },
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Battery Charge: $batteryLevel%", fontSize = 11.sp, color = widgetSecondaryText)
                                                Text("Health: Optimum", fontSize = 11.sp, color = widgetSecondaryText)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Quick Tasks Widget Checklist
                            if (assistantWidgets.contains("quick_tasks")) {
                                item {
                                    val taskList = viewModel.tasks.collectAsState().value
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Tasks Calendar Checklist", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { viewModel.openApp(AppId.TASKS) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Launch,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (taskList.isEmpty()) {
                                                Text("No active tasks found in database.", fontSize = 11.sp, color = widgetSecondaryText)
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    taskList.take(3).forEach { task ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { viewModel.toggleTaskCompletion(task.id) }
                                                                .padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                                contentDescription = null,
                                                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else widgetSecondaryText,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Text(
                                                                text = task.title,
                                                                fontSize = 12.sp,
                                                                color = if (task.isCompleted) widgetSecondaryText else widgetPrimaryText,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Quick Notes Widget
                            if (assistantWidgets.contains("quick_notes")) {
                                item {
                                    val notesList = viewModel.notes.collectAsState().value
                                    val topNote = notesList.firstOrNull()

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Recent Keep Note", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { viewModel.openApp(AppId.NOTES) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Launch,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            if (topNote == null) {
                                                Text("Tap the launch button to write your first note.", fontSize = 11.sp, color = widgetSecondaryText)
                                            } else {
                                                Text(topNote.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = widgetPrimaryText)
                                                Text(
                                                    text = topNote.content,
                                                    fontSize = 11.sp,
                                                    color = widgetSecondaryText,
                                                    maxLines = 2,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Smart Recommendations Widget Card
                            if (assistantWidgets.contains("recommendations")) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Smart Actions",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = widgetSecondaryText,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Toggle Dark Mode action chip
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.toggleDarkMode() }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Contrast,
                                                        null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Toggle Dark Mode",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // App Store action chip
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.openApp(AppId.STORE) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocalMall,
                                                        null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Search Store",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Scrim panel (right 16% width) to catch tap closes
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { viewModel.setAssistantOpen(false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppIconTile(
    appId: AppId,
    index: Int,
    maxIndex: Int = Int.MAX_VALUE,
    currentLang: String,
    hasUnread: Boolean,
    isEditMode: Boolean,
    customIconUrl: String? = null,
    iconStyle: com.example.model.IconStyle = com.example.model.IconStyle.SQUIRCLE,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onReorder: (Int, Int) -> Unit,
    onClick: () -> Unit
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val shape = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getBorderForStyle(iconStyle, getAppColor(appId)) }
    val elevation = remember(iconStyle, isEditMode) {
        if (isEditMode) 8.dp else com.example.ui.theme.IconStyleHelper.getElevationForStyle(iconStyle)
    }
    val bg = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getBackgroundTint(iconStyle, getAppColor(appId)) }

    val rotation: Float
    val scale: Float
    if (isEditMode) {
        val infiniteTransition = rememberInfiniteTransition(label = "shake")
        val r by infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(125, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
        val s by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        rotation = r
        scale = s
    } else {
        rotation = 0f
        scale = 1f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                if (isEditMode) {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { if (!isEditMode) onClick() },
                onLongClick = { onLongClick() }
            )
            .pointerInput(isEditMode) {
                if (isEditMode) {
                    detectDragGestures(
                        onDragStart = { dragOffsetX = 0f },
                        onDragEnd = {
                            if (dragOffsetX > 60f) {
                                onReorder(index, index + 1)
                            } else if (dragOffsetX < -60f) {
                                onReorder(index, index - 1)
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = { dragOffsetX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                        }
                    )
                }
            }
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = shape,
                color = bg,
                border = border,
                shadowElevation = elevation,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (!customIconUrl.isNullOrEmpty()) {
                        coil.compose.AsyncImage(
                            model = customIconUrl,
                            contentDescription = appId.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            getAppIcon(appId),
                            contentDescription = appId.title,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            if (isEditMode) {
                // Delete button (red circle with white X)
                Surface(
                    shape = CircleShape,
                    color = Color.Red,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clickable { onDelete() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            } else if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = com.example.util.TranslationManager.getTranslation(appId.title, currentLang),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (isEditMode) {
            // Nudge arrow controls below app title for quick accessible reordering
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                IconButton(
                    onClick = { if (index > 0) onReorder(index, index - 1) },
                    enabled = index > 0,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Move Left",
                        tint = if (index > 0) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                IconButton(
                    onClick = { if (index < maxIndex) onReorder(index, index + 1) },
                    enabled = index < maxIndex,
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Move Right",
                        tint = if (index < maxIndex) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeviceAppIconTile(
    app: DeviceInstalledApp,
    index: Int,
    isEditMode: Boolean,
    iconStyle: com.example.model.IconStyle = com.example.model.IconStyle.SQUIRCLE,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val shape = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getBorderForStyle(iconStyle, Color(0xFF00897B)) }
    val elevation = remember(iconStyle, isEditMode) {
        if (isEditMode) 8.dp else com.example.ui.theme.IconStyleHelper.getElevationForStyle(iconStyle)
    }
    val bg = remember(iconStyle) { com.example.ui.theme.IconStyleHelper.getBackgroundTint(iconStyle, Color(0xFF00897B)) }

    val imageBitmap = remember(app.iconDrawable) {
        app.iconDrawable?.toBitmapOrNull()?.asImageBitmap()
    }

    val rotation: Float
    val scale: Float
    if (isEditMode) {
        val infiniteTransition = rememberInfiniteTransition(label = "shake")
        val r by infiniteTransition.animateFloat(
            initialValue = -2.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(125, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
        val s by infiniteTransition.animateFloat(
            initialValue = 0.97f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        rotation = r
        scale = s
    } else {
        rotation = 0f
        scale = 1f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                if (isEditMode) {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { if (!isEditMode) onClick() },
                onLongClick = { onLongClick() }
            )
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = shape,
                color = bg,
                border = border,
                shadowElevation = elevation,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Icon(
                            Icons.Filled.Android,
                            contentDescription = app.appName,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Android badge for device package apps
            Surface(
                shape = CircleShape,
                color = Color(0xFF00C853),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 2.dp, y = (-2).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Android,
                        contentDescription = "Device App",
                        tint = Color.White,
                        modifier = Modifier.size(9.dp)
                    )
                }
            }

            if (isEditMode) {
                Surface(
                    shape = CircleShape,
                    color = Color.Red,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clickable { onDelete() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.appName,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun DockAppIcon(
    appId: AppId,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    hasUnread: Boolean,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(52.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = appId.title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF1744))
            )
        }
    }
}

fun getAppColor(appId: AppId): Color {
    return Color(appId.iconColorHex)
}

@Composable
private fun HomeScreenCompactMenuRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeMusicWidget(
    viewModel: OSViewModel,
    widgetPrimaryText: Color,
    widgetSecondaryText: Color
) {
    val musicTracks by viewModel.musicTracks.collectAsState()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val track = musicTracks.getOrElse(currentTrackIndex) { musicTracks.firstOrNull() ?: return }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
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
                Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = track.title,
                    color = widgetPrimaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    color = widgetSecondaryText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPlayingMusic) "Çalıyor • SoundWave Hi-Fi" else "Duraklatıldı",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.prevTrack() }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.SkipPrevious, "Önceki", tint = widgetPrimaryText, modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = { viewModel.togglePlayMusic() },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    if (isPlayingMusic) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    "Oynat/Duraklat",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = { viewModel.nextTrack() }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.SkipNext, "Sonraki", tint = widgetPrimaryText, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun HomeNotesWidget(
    viewModel: OSViewModel,
    widgetPrimaryText: Color,
    widgetSecondaryText: Color
) {
    val notes by viewModel.notes.collectAsState()
    val topNote = notes.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.EditNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    }
                }
                Text(
                    text = topNote?.title ?: "Hızlı Notlar",
                    color = widgetPrimaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "${notes.size} not",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = topNote?.content?.ifBlank { "Yeni bir fikir veya yapılacaklar listesi yazın..." } ?: "Dokunarak yeni not oluşturun...",
            color = widgetSecondaryText,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 15.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keep Notes • Eşitlendi",
                color = widgetSecondaryText.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
            Text(
                text = "+ Not Ekle",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HomeWellbeingWidget(
    viewModel: OSViewModel,
    widgetPrimaryText: Color,
    widgetSecondaryText: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Bugünkü Ekran Süresi",
                color = widgetSecondaryText,
                fontSize = 11.sp
            )
            Text(
                text = "3s 42dk",
                color = widgetPrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Odak Modu Aktif • 12 Kilit Açma",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(62.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 0.65f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = widgetSecondaryText.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                )
                Text("%65", color = widgetPrimaryText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HomeWeatherWidget(
    viewModel: OSViewModel,
    widgetPrimaryText: Color,
    widgetSecondaryText: Color
) {
    val dailyForecast by viewModel.dailyForecast.collectAsState()
    val today = dailyForecast.firstOrNull()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                Text("İstanbul, TR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = widgetPrimaryText)
            }
            Text(
                text = "${today?.tempHigh ?: 24}°C",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = widgetPrimaryText
            )
            Text(
                text = today?.condition ?: "Parçalı Bulutlu • Nem %45",
                fontSize = 10.sp,
                color = widgetSecondaryText
            )
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeQuickTogglesWidget(
    viewModel: OSViewModel,
    widgetPrimaryText: Color,
    widgetSecondaryText: Color
) {
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickTogglePill(
            icon = if (wifiEnabled) Icons.Filled.Wifi else Icons.Filled.WifiOff,
            label = "Wi-Fi",
            isActive = wifiEnabled,
            onClick = { viewModel.toggleWifi() }
        )
        QuickTogglePill(
            icon = if (bluetoothEnabled) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled,
            label = "Bluetooth",
            isActive = bluetoothEnabled,
            onClick = { viewModel.toggleBluetooth() }
        )
        QuickTogglePill(
            icon = if (flashlightOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
            label = "Fener",
            isActive = flashlightOn,
            onClick = { viewModel.toggleFlashlight() }
        )
        QuickTogglePill(
            icon = if (dndEnabled) Icons.Filled.DoNotDisturbOn else Icons.Filled.DoNotDisturbOff,
            label = "Sessiz",
            isActive = dndEnabled,
            onClick = { viewModel.toggleDnd() }
        )
    }
}

@Composable
private fun QuickTogglePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isActive) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .width(62.dp)
            .height(72.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
