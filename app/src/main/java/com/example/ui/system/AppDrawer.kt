package com.example.ui.system

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.R
import com.example.model.AppFolder
import com.example.model.AppId
import com.example.ui.theme.IconStyleHelper
import com.example.ui.theme.LocalIsDarkMode
import com.example.util.DeviceInstalledApp
import com.example.util.PackageManagerUtil
import com.example.util.toBitmapOrNull
import com.example.viewmodel.OSViewModel

sealed class DrawerItem {
    data class Folder(val folder: AppFolder) : DrawerItem()
    data class SystemApp(val app: AppId) : DrawerItem()
    data class DeviceApp(val app: DeviceInstalledApp) : DrawerItem()

    val uniqueKey: String
        get() = when (this) {
            is Folder -> "folder_${folder.id}"
            is SystemApp -> "app_${app.packageName}"
            is DeviceApp -> "dev_${app.packageName}"
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDark = LocalIsDarkMode.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val appDrawerOrder by viewModel.appDrawerOrder.collectAsState()
    val appFolders by viewModel.appFolders.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val customIcons by viewModel.customAppIcons.collectAsState()
    val deviceHomeApps by viewModel.deviceHomeApps.collectAsState()

    val deviceInstalledApps = remember(context) { PackageManagerUtil.getInstalledDeviceApps(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var openedFolder by remember { mutableStateOf<AppFolder?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var selectedAppForDetails by remember { mutableStateOf<AppId?>(null) }
    var selectedDeviceAppForDetails by remember { mutableStateOf<DeviceInstalledApp?>(null) }
    var newFolderName by remember { mutableStateOf("") }
    var selectedAppsForNewFolder by remember { mutableStateOf(setOf<AppId>()) }

    // Reordering drag state
    var draggingAppId by remember { mutableStateOf<AppId?>(null) }

    val drawerBg = if (isDark) Color(0xFF0F141C).copy(alpha = 0.98f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.99f)
    val handleColor = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.3f)
    val appLabelColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    val categories = listOf("All", "Paket Yöneticisi", "Utilities", "Productivity", "Media", "Social", "Games", "Communication", "Developer", "Finance", "Shopping", "Health")

    val filteredItems = remember(appDrawerOrder, searchQuery, selectedCategory, appFolders, deviceInstalledApps) {
        val list = mutableListOf<DrawerItem>()
        if (selectedCategory == "Paket Yöneticisi") {
            val matchedDevice = deviceInstalledApps.filter {
                searchQuery.isBlank() ||
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
            list.addAll(matchedDevice.map { DrawerItem.DeviceApp(it) })
        } else if (selectedCategory == "All") {
            val matchedSys = appDrawerOrder.filter { app ->
                searchQuery.isBlank() ||
                app.title.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true) ||
                app.category.contains(searchQuery, ignoreCase = true)
            }
            val matchedDevice = deviceInstalledApps.filter {
                searchQuery.isBlank() ||
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
            list.addAll(matchedSys.map { DrawerItem.SystemApp(it) })
            list.addAll(matchedDevice.map { DrawerItem.DeviceApp(it) })
        } else {
            val matchedSys = appDrawerOrder.filter { app ->
                val matchesSearch = searchQuery.isBlank() ||
                    app.title.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true) ||
                    app.category.contains(searchQuery, ignoreCase = true)
                val matchesCategory = app.category.equals(selectedCategory, ignoreCase = true)
                matchesSearch && matchesCategory
            }
            list.addAll(matchedSys.map { DrawerItem.SystemApp(it) })
        }
        list
    }

    BackHandler {
        if (openedFolder != null) {
            openedFolder = null
        } else if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        } else {
            viewModel.closeAppDrawer()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(drawerBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 35) {
                        viewModel.closeAppDrawer()
                    }
                }
            }
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        val isTablet = maxWidth >= 600.dp
        val isExpandedTablet = maxWidth >= 840.dp
        val drawerColumns = if (isExpandedTablet) 8 else if (isTablet) 6 else 4
        val drawerPageSize = if (isExpandedTablet) 32 else if (isTablet) 24 else 16

        // Partition all items (folders first, then apps not inside folders + device apps) into adaptive pages
        val pagedDrawerItems = remember(appFolders, appDrawerOrder, deviceInstalledApps, drawerPageSize) {
            val appsNotInFolders = appDrawerOrder.filter { app ->
                appFolders.none { folder -> folder.appIds.contains(app) }
            }
            val list = mutableListOf<DrawerItem>()
            list.addAll(appFolders.map { DrawerItem.Folder(it) })
            list.addAll(appsNotInFolders.map { DrawerItem.SystemApp(it) })
            list.addAll(deviceInstalledApps.map { DrawerItem.DeviceApp(it) })
            list.chunked(drawerPageSize)
        }

        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { pagedDrawerItems.size.coerceAtLeast(1) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1100.dp)
                .align(Alignment.Center)
        ) {
            // Drag handle to close
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { viewModel.closeAppDrawer() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(handleColor)
                )
            }

            // Top Search Bar (Quickly filter and find installed applications by name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Uygulama ara...",
                            color = if (isDark) Color.White.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        "Clear",
                                        tint = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.openVroxenAssistant() }) {
                                Icon(
                                    Icons.Filled.Mic,
                                    "Hey Vroxen",
                                    tint = Color(0xFF4285F4),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        focusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                FilledTonalIconButton(
                    onClick = { showCreateFolderDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Create Folder", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Category Filter Chips Row (Scrollable to support all app categories)
            val categories = listOf("All", "Paket Yöneticisi", "Utilities", "Productivity", "Media", "Social", "Games", "Communication", "Developer", "Finance", "Shopping", "Health")
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val count = when (cat) {
                        "All" -> appDrawerOrder.size + deviceInstalledApps.size
                        "Paket Yöneticisi" -> deviceInstalledApps.size
                        else -> appDrawerOrder.count { it.category.equals(cat, ignoreCase = true) }
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("$cat ($count)", fontSize = 11.sp) },
                        leadingIcon = if (cat == "Paket Yöneticisi") {
                            { Icon(Icons.Default.Android, null, modifier = Modifier.size(13.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = if (isDark) Color.White.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Main App Drawer Area (Swipeable Pages or Instant Filtered Grid)
            Box(modifier = Modifier.weight(1f)) {
                if (searchQuery.isNotBlank() || selectedCategory != "All") {
                    // Filtered Results Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(drawerColumns),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // System Update search shortcut
                        if (searchQuery.isNotBlank() && (
                            searchQuery.contains("güncel", ignoreCase = true) ||
                            searchQuery.contains("update", ignoreCase = true) ||
                            searchQuery.contains("sistem", ignoreCase = true) ||
                            searchQuery.contains("vroxen", ignoreCase = true) ||
                            searchQuery.contains("ui", ignoreCase = true)
                        )) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.closeAppDrawer()
                                            viewModel.openSystemUpdate()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF0D0F17),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.img_system_update_v3),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Sistem Güncellemesi",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Vroxen UI 3 • Güncelleme Merkezini Aç",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            Icons.Filled.ArrowForwardIos,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        items(filteredItems, key = { it.uniqueKey }) { item ->
                            when (item) {
                                is DrawerItem.SystemApp -> {
                                    val app = item.app
                                    AppGridTile(
                                        app = app,
                                        isDark = isDark,
                                        labelColor = appLabelColor,
                                        isDragging = draggingAppId == app,
                                        iconStyle = iconStyle,
                                        customIconUrl = customIcons[app.name],
                                        onClick = {
                                            viewModel.closeAppDrawer()
                                            viewModel.openApp(app)
                                        },
                                        onLongClick = {
                                            selectedAppForDetails = app
                                        }
                                    )
                                }
                                is DrawerItem.DeviceApp -> {
                                    val devApp = item.app
                                    DeviceAppGridTile(
                                        app = devApp,
                                        isDark = isDark,
                                        labelColor = appLabelColor,
                                        iconStyle = iconStyle,
                                        onClick = {
                                            viewModel.closeAppDrawer()
                                            val launched = PackageManagerUtil.launchPackage(context, devApp.packageName)
                                            if (!launched) {
                                                PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                                            }
                                        },
                                        onLongClick = {
                                            selectedDeviceAppForDetails = devApp
                                        }
                                    )
                                }
                                is DrawerItem.Folder -> {
                                    FolderTile(
                                        folder = item.folder,
                                        isDark = isDark,
                                        labelColor = appLabelColor,
                                        iconStyle = iconStyle,
                                        onClick = { openedFolder = item.folder },
                                        onLongClick = {
                                            viewModel.deleteFolder(item.folder.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Paged App Drawer using HorizontalPager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val pageItems = pagedDrawerItems.getOrNull(pageIndex) ?: emptyList()
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(drawerColumns),
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pageItems, key = { it.uniqueKey }) { item ->
                                when (item) {
                                    is DrawerItem.Folder -> {
                                        FolderTile(
                                            folder = item.folder,
                                            isDark = isDark,
                                            labelColor = appLabelColor,
                                            iconStyle = iconStyle,
                                            onClick = { openedFolder = item.folder },
                                            onLongClick = {
                                                viewModel.deleteFolder(item.folder.id)
                                            }
                                        )
                                    }
                                    is DrawerItem.SystemApp -> {
                                        val app = item.app
                                        AppGridTile(
                                            app = app,
                                            isDark = isDark,
                                            labelColor = appLabelColor,
                                            isDragging = draggingAppId == app,
                                            iconStyle = iconStyle,
                                            customIconUrl = customIcons[app.name],
                                            onClick = {
                                                viewModel.closeAppDrawer()
                                                viewModel.openApp(app)
                                            },
                                            onLongClick = {
                                                selectedAppForDetails = app
                                            }
                                        )
                                    }
                                    is DrawerItem.DeviceApp -> {
                                        val devApp = item.app
                                        DeviceAppGridTile(
                                            app = devApp,
                                            isDark = isDark,
                                            labelColor = appLabelColor,
                                            iconStyle = iconStyle,
                                            onClick = {
                                                viewModel.closeAppDrawer()
                                                val launched = PackageManagerUtil.launchPackage(context, devApp.packageName)
                                                if (!launched) {
                                                    PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                                                }
                                            },
                                            onLongClick = {
                                                selectedDeviceAppForDetails = devApp
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Page Indicator Dots when paging through the drawer
            if (searchQuery.isBlank() && selectedCategory == "All" && pagedDrawerItems.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pagedDrawerItems.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isSelected) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isDark) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f)
                                )
                        )
                    }
                }
            }

            // Fixed, Persistent Dock at the bottom of the screen (keeping frequently used apps accessible while paging)
            PersistentDock(
                viewModel = viewModel,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                isInDrawer = true
            )
        }
    }

    // Folder Viewer Dialog
    openedFolder?.let { folder ->
        FolderDialog(
            folder = folder,
            allApps = appDrawerOrder,
            isDark = isDark,
            onDismiss = { openedFolder = null },
            onAppClick = { appId ->
                openedFolder = null
                viewModel.closeAppDrawer()
                viewModel.openApp(appId)
            },
            onRemoveApp = { appId ->
                viewModel.removeAppFromFolder(folder.id, appId)
            },
            onAddApp = { appId ->
                viewModel.addAppToFolder(folder.id, appId)
            },
            onRename = { newName ->
                // folder rename
            }
        )
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Create App Folder", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Select initial apps to group:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(appDrawerOrder) { app ->
                            val isSelected = app in selectedAppsForNewFolder
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        selectedAppsForNewFolder = if (isSelected) selectedAppsForNewFolder - app else selectedAppsForNewFolder + app
                                    }
                                    .padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(getAppIcon(app), null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(app.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank() && selectedAppsForNewFolder.isNotEmpty()) {
                            viewModel.createFolder(newFolderName, selectedAppsForNewFolder.toList())
                            showCreateFolderDialog = false
                            newFolderName = ""
                            selectedAppsForNewFolder = emptySet()
                        }
                    },
                    enabled = newFolderName.isNotBlank() && selectedAppsForNewFolder.isNotEmpty()
                ) {
                    Text("Create Folder")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Compact Long-Press App Shortcut Menu (Vroxen UI System Apps)
    selectedAppForDetails?.let { app ->
        val isPinnedOnHome = app in viewModel.homeApps.collectAsState().value
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedAppForDetails = null },
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
                            onClick = { selectedAppForDetails = null },
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
                    CompactMenuActionRow(
                        icon = Icons.Default.PlayArrow,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Uygulamayı Aç",
                        isPrimary = true,
                        onClick = {
                            selectedAppForDetails = null
                            viewModel.closeAppDrawer()
                            viewModel.openApp(app)
                        }
                    )

                    // 2. Pin/Unpin Home Screen
                    CompactMenuActionRow(
                        icon = if (isPinnedOnHome) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                        iconTint = if (isPinnedOnHome) Color(0xFFDC2626) else Color(0xFF2563EB),
                        label = if (isPinnedOnHome) "Ana Ekrandan Kaldır" else "Ana Ekrana Ekle",
                        onClick = {
                            if (isPinnedOnHome) {
                                viewModel.removeHomeApp(app)
                                Toast.makeText(context, "${app.title} ana ekrandan kaldırıldı", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addHomeApp(app)
                                Toast.makeText(context, "${app.title} ana ekrana eklendi", Toast.LENGTH_SHORT).show()
                            }
                            selectedAppForDetails = null
                        }
                    )

                    // 3. Open in vdesk Desktop
                    CompactMenuActionRow(
                        icon = Icons.Default.DesktopWindows,
                        iconTint = Color(0xFF0284C7),
                        label = "vdesk Masaüstünde Aç",
                        onClick = {
                            selectedAppForDetails = null
                            viewModel.closeAppDrawer()
                            viewModel.openDexApp(app)
                            viewModel.setDexModeActive(true)
                            Toast.makeText(context, "${app.title} vdesk penceresinde açıldı", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // 4. Clear Cache
                    CompactMenuActionRow(
                        icon = Icons.Default.CleaningServices,
                        iconTint = Color(0xFF10B981),
                        label = "Önbelleği Temizle (12 MB)",
                        onClick = {
                            viewModel.clearAppCache(app)
                            Toast.makeText(context, "${app.title} önbelleği temizlendi ✨", Toast.LENGTH_SHORT).show()
                            selectedAppForDetails = null
                        }
                    )

                    // 5. Copy Package Name
                    CompactMenuActionRow(
                        icon = Icons.Default.ContentCopy,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "Paket Adını Kopyala",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(app.packageName))
                            Toast.makeText(context, "Kopyalandı: ${app.packageName}", Toast.LENGTH_SHORT).show()
                            selectedAppForDetails = null
                        }
                    )
                }
            }
        }
    }

    // Compact Long-Press App Shortcut Menu (Device Package Manager Apps)
    selectedDeviceAppForDetails?.let { devApp ->
        val devBitmap = remember(devApp.iconDrawable) { devApp.iconDrawable?.toBitmapOrNull()?.asImageBitmap() }
        val isPinnedOnHome = deviceHomeApps.contains(devApp.packageName)

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedDeviceAppForDetails = null },
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
                            onClick = { selectedDeviceAppForDetails = null },
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
                    CompactMenuActionRow(
                        icon = Icons.Default.PlayArrow,
                        iconTint = MaterialTheme.colorScheme.primary,
                        label = "Uygulamayı Çalıştır",
                        isPrimary = true,
                        onClick = {
                            selectedDeviceAppForDetails = null
                            viewModel.closeAppDrawer()
                            val launched = PackageManagerUtil.launchPackage(context, devApp.packageName)
                            if (!launched) {
                                PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                            }
                        }
                    )

                    // 2. Pin/Unpin Home Screen
                    CompactMenuActionRow(
                        icon = if (isPinnedOnHome) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                        iconTint = if (isPinnedOnHome) Color(0xFFDC2626) else Color(0xFF2563EB),
                        label = if (isPinnedOnHome) "Ana Ekrandan Kaldır" else "Ana Ekrana Ekle",
                        onClick = {
                            if (isPinnedOnHome) {
                                viewModel.removeDeviceAppFromHome(devApp.packageName)
                                Toast.makeText(context, "${devApp.appName} ana ekrandan kaldırıldı", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.addDeviceAppToHome(devApp.packageName)
                                Toast.makeText(context, "${devApp.appName} ana ekrana eklendi", Toast.LENGTH_SHORT).show()
                            }
                            selectedDeviceAppForDetails = null
                        }
                    )

                    // 3. Android System App Settings
                    CompactMenuActionRow(
                        icon = Icons.Default.Settings,
                        iconTint = Color(0xFF64748B),
                        label = "Android Uygulama Ayarları",
                        onClick = {
                            PackageManagerUtil.openAppDetailsSettings(context, devApp.packageName)
                            selectedDeviceAppForDetails = null
                        }
                    )

                    // 4. Copy Package Name
                    CompactMenuActionRow(
                        icon = Icons.Default.ContentCopy,
                        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "Paket Adını Kopyala",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(devApp.packageName))
                            Toast.makeText(context, "Kopyalandı: ${devApp.packageName}", Toast.LENGTH_SHORT).show()
                            selectedDeviceAppForDetails = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactMenuActionRow(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderTile(
    folder: AppFolder,
    isDark: Boolean,
    labelColor: Color,
    iconStyle: com.example.model.IconStyle = com.example.model.IconStyle.SQUIRCLE,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = remember(iconStyle) { IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle) { IconStyleHelper.getBorderForStyle(iconStyle, Color(folder.colorHex)) }
    val elevation = remember(iconStyle) { IconStyleHelper.getElevationForStyle(iconStyle) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Surface(
            shape = shape,
            color = Color(folder.colorHex),
            border = border,
            shadowElevation = elevation,
            modifier = Modifier.size(54.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Mini 2x2 Grid preview of folder apps
                val previewApps = folder.appIds.take(4)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        previewApps.getOrNull(0)?.let { Icon(getAppIcon(it), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                        previewApps.getOrNull(1)?.let { Icon(getAppIcon(it), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        previewApps.getOrNull(2)?.let { Icon(getAppIcon(it), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                        previewApps.getOrNull(3)?.let { Icon(getAppIcon(it), null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = folder.name,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppGridTile(
    app: AppId,
    isDark: Boolean,
    labelColor: Color,
    isDragging: Boolean,
    iconStyle: com.example.model.IconStyle = com.example.model.IconStyle.SQUIRCLE,
    customIconUrl: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = remember(iconStyle) { IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle) { IconStyleHelper.getBorderForStyle(iconStyle, getAppColor(app)) }
    val elevation = remember(iconStyle, isDragging) {
        if (isDragging) 12.dp else IconStyleHelper.getElevationForStyle(iconStyle)
    }
    val bg = remember(iconStyle) { IconStyleHelper.getBackgroundTint(iconStyle, getAppColor(app)) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .border(if (isDragging) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Surface(
            shape = shape,
            color = bg,
            border = border,
            shadowElevation = elevation,
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!customIconUrl.isNullOrEmpty()) {
                    coil.compose.AsyncImage(
                        model = customIconUrl,
                        contentDescription = app.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        getAppIcon(app),
                        contentDescription = app.title,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.title,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeviceAppGridTile(
    app: DeviceInstalledApp,
    isDark: Boolean,
    labelColor: Color,
    iconStyle: com.example.model.IconStyle = com.example.model.IconStyle.SQUIRCLE,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val shape = remember(iconStyle) { IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle) { IconStyleHelper.getBorderForStyle(iconStyle, Color(0xFF00897B)) }
    val elevation = remember(iconStyle) { IconStyleHelper.getElevationForStyle(iconStyle) }
    val bg = remember(iconStyle) { IconStyleHelper.getBackgroundTint(iconStyle, Color(0xFF00897B)) }

    val imageBitmap = remember(app.iconDrawable) {
        app.iconDrawable?.toBitmapOrNull()?.asImageBitmap()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = shape,
                color = bg,
                border = border,
                shadowElevation = elevation,
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = app.appName,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Icon(
                            Icons.Filled.Android,
                            contentDescription = app.appName,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

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
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.appName,
            color = labelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderDialog(
    folder: AppFolder,
    allApps: List<AppId>,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onAppClick: (AppId) -> Unit,
    onRemoveApp: (AppId) -> Unit,
    onAddApp: (AppId) -> Unit,
    onRename: (String) -> Unit
) {
    var showAddAppsSheet by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(folder.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = { showAddAppsSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add app to folder")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (folder.appIds.isEmpty()) {
                    Text("Folder is empty. Tap '+' to add applications.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(folder.appIds) { appId ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAppClick(appId) }
                                        .padding(4.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = getAppColor(appId),
                                        modifier = Modifier.size(46.dp),
                                        shadowElevation = 4.dp
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(getAppIcon(appId), null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(appId.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                // Remove button
                                IconButton(
                                    onClick = { onRemoveApp(appId) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                if (showAddAppsSheet) {
                    HorizontalDivider()
                    Text("Add Apps to Folder:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allApps.filter { it !in folder.appIds }) { app ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        onAddApp(app)
                                    }
                                    .padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(getAppIcon(app), null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(app.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
