@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.model.AppId
import com.example.model.StoreAppItem
import com.example.ui.system.getAppIcon
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Composable
fun PlayStoreApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val storeApps by viewModel.storeApps.collectAsState()
    val isCheckingForUpdates by viewModel.isCheckingForUpdates.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedAppForDetail by remember { mutableStateOf<StoreAppItem?>(null) }
    var appToUninstallPrompt by remember { mutableStateOf<StoreAppItem?>(null) }

    // Sideload & URL State
    var showSideloadSection by remember { mutableStateOf(false) }
    var urlInput by remember { mutableStateOf("https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/config.json") }
    var customAppName by remember { mutableStateOf("vos 1.1 App") }
    var isInstallingSideload by remember { mutableStateOf(false) }
    var sideloadProgress by remember { mutableFloatStateOf(0f) }
    var sideloadStatus by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    val presetUrls = listOf(
        "vos 1.1 App" to "https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/config.json",
        "Retro Space Invaders" to "https://novaos.net/arcade/space_invaders.apk",
        "Chess AI Grandmaster" to "https://novaos.net/arcade/chess_master.apk",
        "Scientific Calculator Pro" to "https://novaos.net/tools/sci_calculator.apk"
    )

    val pendingUpdates = remember(storeApps) {
        storeApps.filter { it.isInstalled && it.availableUpdateVersion != null }
    }

    val installedApps = remember(storeApps) {
        storeApps.filter { it.isInstalled }
    }

    val categories = listOf("All", "Games", "Tools", "Social", "Entertainment", "Productivity", "Weather", "Navigation")

    val filteredApps = remember(storeApps, searchQuery, selectedCategory) {
        storeApps.filter { app ->
            val matchesCategory = if (selectedCategory == "All") true else app.category.contains(selectedCategory, ignoreCase = true)
            val matchesQuery = if (searchQuery.isBlank()) true else {
                app.name.contains(searchQuery, ignoreCase = true) ||
                app.developer.contains(searchQuery, ignoreCase = true) ||
                app.category.contains(searchQuery, ignoreCase = true) ||
                app.description.contains(searchQuery, ignoreCase = true) ||
                app.appId.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text("Nexus Store", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Package Manager & Apps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.checkForUpdates() }) {
                            if (isCheckingForUpdates) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Check for Updates")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Search Bar Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps, packages, games & tools...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Store & Package Manager Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Apps, null, modifier = Modifier.size(16.dp))
                                Text("Discover", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                BadgedBox(badge = {
                                    if (pendingUpdates.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("${pendingUpdates.size}")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.SystemUpdate, null, modifier = Modifier.size(16.dp))
                                }
                                Text("Updates", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                Text("Installed (${installedApps.size})", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(16.dp))
                                Text("Sideload", fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                            }
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> DiscoverTab(
                    apps = filteredApps,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onAppClick = { selectedAppForDetail = it },
                    onInstallClick = { app -> viewModel.installStoreAppWithProgress(app.appId) },
                    onOpenClick = { app -> viewModel.openApp(app.appId) }
                )
                1 -> UpdatesTab(
                    pendingUpdates = pendingUpdates,
                    isChecking = isCheckingForUpdates,
                    onCheckUpdates = { viewModel.checkForUpdates() },
                    onUpdateAll = { viewModel.updateAllStoreApps() },
                    onUpdateApp = { app -> viewModel.updateStoreApp(app.appId) },
                    onAppClick = { selectedAppForDetail = it }
                )
                2 -> InstalledPackageManagerTab(
                    installedApps = installedApps,
                    onAppClick = { selectedAppForDetail = it },
                    onOpenClick = { app -> viewModel.openApp(app.appId) },
                    onUninstallClick = { app -> appToUninstallPrompt = app },
                    onClearCache = { app -> viewModel.clearAppCache(app.appId) },
                    onClearData = { app -> viewModel.clearAppData(app.appId) }
                )
                3 -> SideloadTab(
                    urlInput = urlInput,
                    customAppName = customAppName,
                    presetUrls = presetUrls,
                    isInstalling = isInstallingSideload,
                    installProgress = sideloadProgress,
                    installStatus = sideloadStatus,
                    onUrlChange = { urlInput = it },
                    onNameChange = { customAppName = it },
                    onSideload = {
                        if (customAppName.isNotEmpty() && urlInput.isNotEmpty()) {
                            isInstallingSideload = true
                            sideloadProgress = 0f
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    sideloadStatus = "Connecting to URL host..."
                                    sideloadProgress = 0.15f

                                    val client = OkHttpClient.Builder()
                                        .connectTimeout(15, TimeUnit.SECONDS)
                                        .readTimeout(15, TimeUnit.SECONDS)
                                        .build()

                                    val request = Request.Builder()
                                        .url(urlInput)
                                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                        .build()

                                    client.newCall(request).execute().use { response ->
                                        if (response.isSuccessful) {
                                            sideloadStatus = "Downloading package binaries..."
                                            sideloadProgress = 0.5f
                                            val bodyBytes = response.body?.bytes()
                                            val sizeKb = (bodyBytes?.size ?: 0) / 1024

                                            sideloadStatus = "Verifying $sizeKb KB package integrity..."
                                            sideloadProgress = 0.8f
                                            delay(900)

                                            sideloadStatus = "Sideloading custom build into launcher desktop..."
                                            sideloadProgress = 0.95f
                                            delay(700)

                                            withContext(Dispatchers.Main) {
                                                viewModel.installAppFromUrl(customAppName, urlInput)
                                                isInstallingSideload = false
                                                urlInput = ""
                                                customAppName = ""
                                                selectedTab = 2
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                sideloadStatus = "HTTP Connection Error: ${response.code}"
                                                delay(2000)
                                                isInstallingSideload = false
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        sideloadStatus = "Host Connection Error: ${e.message ?: "Check URL"}"
                                        delay(2500)
                                        isInstallingSideload = false
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // App Detail Bottom Sheet / Modal Dialog
            selectedAppForDetail?.let { app ->
                AppDetailDialog(
                    app = app,
                    onDismiss = { selectedAppForDetail = null },
                    onInstall = {
                        viewModel.installStoreAppWithProgress(app.appId)
                        selectedAppForDetail = null
                    },
                    onOpen = {
                        viewModel.openApp(app.appId)
                        selectedAppForDetail = null
                    },
                    onUpdate = {
                        viewModel.updateStoreApp(app.appId)
                    },
                    onUninstall = {
                        appToUninstallPrompt = app
                        selectedAppForDetail = null
                    },
                    onClearCache = {
                        viewModel.clearAppCache(app.appId)
                    }
                )
            }

            // Uninstall Confirmation Prompt
            appToUninstallPrompt?.let { app ->
                AlertDialog(
                    onDismissRequest = { appToUninstallPrompt = null },
                    icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    title = { Text("Uninstall ${app.name}?") },
                    text = {
                        Text(
                            "This will delete the application binaries and clear all local cached files (${app.sizeMb} MB). You can reinstall it anytime from the Nexus Store.",
                            fontSize = 13.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.uninstallStoreApp(app.appId)
                                appToUninstallPrompt = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Uninstall")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { appToUninstallPrompt = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DiscoverTab(
    apps: List<StoreAppItem>,
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    onAppClick: (StoreAppItem) -> Unit,
    onInstallClick: (StoreAppItem) -> Unit,
    onOpenClick: (StoreAppItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Featured Carousel Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "FEATURED PACKAGE",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "v16.0 M3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nova Arcade & Developer Suite",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explore retro 2048, Snake, AI chess, terminal tools, and productivity planners optimized for NovaOS.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Category Filter Chips Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategory == "All") "Curated Repository Apps" else "$selectedCategory Applications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${apps.size} packages",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (apps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                        Text("No matching packages found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Try searching with another keyword or category.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(apps, key = { it.appId.packageName }) { app ->
                AppStoreItemCard(
                    app = app,
                    onClick = { onAppClick(app) },
                    onInstallClick = { onInstallClick(app) },
                    onOpenClick = { onOpenClick(app) }
                )
            }
        }
    }
}

@Composable
private fun UpdatesTab(
    pendingUpdates: List<StoreAppItem>,
    isChecking: Boolean,
    onCheckUpdates: () -> Unit,
    onUpdateAll: () -> Unit,
    onUpdateApp: (StoreAppItem) -> Unit,
    onAppClick: (StoreAppItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Updates Status Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingUpdates.isNotEmpty()) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (pendingUpdates.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (pendingUpdates.isNotEmpty()) Icons.Default.SystemUpdate else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (pendingUpdates.isNotEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (pendingUpdates.isNotEmpty()) "${pendingUpdates.size} Updates Available" else "All Apps Up To Date",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            val totalSize = pendingUpdates.sumOf { it.updateSizeMb }
                            Text(
                                text = if (pendingUpdates.isNotEmpty()) "Total download size: $totalSize MB" else "Checked just now • Repository connected",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (pendingUpdates.isNotEmpty()) {
                        Button(
                            onClick = onUpdateAll,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Update All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCheckUpdates,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Check", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        if (pendingUpdates.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                        Text("No Pending Updates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "Your installed packages are running the latest compiled release versions with active security patches.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            item {
                Text("Pending Updates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(pendingUpdates, key = { it.appId.packageName }) { app ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(app.iconColor),
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(getAppIcon(app.appId), contentDescription = app.name, tint = Color.White, modifier = Modifier.size(26.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("v${app.installedVersion} ➔ v${app.availableUpdateVersion ?: app.version}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Text("${app.updateSizeMb} MB update", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Button(
                                onClick = { onUpdateApp(app) },
                                enabled = !app.isUpdating,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (app.isUpdating) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (app.isUpdating) {
                            LinearProgressIndicator(
                                progress = { app.updateProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        app.updateChangelog?.let { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("What's New in this version:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(log, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun InstalledPackageManagerTab(
    installedApps: List<StoreAppItem>,
    onAppClick: (StoreAppItem) -> Unit,
    onOpenClick: (StoreAppItem) -> Unit,
    onUninstallClick: (StoreAppItem) -> Unit,
    onClearCache: (StoreAppItem) -> Unit,
    onClearData: (StoreAppItem) -> Unit
) {
    var filterType by remember { mutableIntStateOf(0) } // 0: All, 1: User Apps, 2: System Apps
    var appSearchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showPackageManagerScanner by remember { mutableStateOf(false) }
    var deviceQueriedApps by remember { mutableStateOf<List<com.example.util.DeviceInstalledApp>>(emptyList()) }

    val displayedApps = remember(installedApps, filterType, appSearchQuery) {
        installedApps.filter { app ->
            val matchesFilter = when (filterType) {
                1 -> !app.isSystemPackage
                2 -> app.isSystemPackage
                else -> true
            }
            val matchesQuery = app.name.contains(appSearchQuery, ignoreCase = true) ||
                    app.category.contains(appSearchQuery, ignoreCase = true) ||
                    app.appId.packageName.contains(appSearchQuery, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }

    val totalInstalledStorage = remember(installedApps) {
        installedApps.sumOf { it.sizeMb.toLong() * 1024 * 1024 + it.dataSizeBytes + it.cacheSizeBytes }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Storage & Package Metrics Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Package Storage Footprint", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = "${"%.1f".format(totalInstalledStorage / (1024.0 * 1024 * 1024))} GB Used",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LinearProgressIndicator(
                        progress = { 0.28f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${installedApps.size} Installed Packages", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${installedApps.count { it.isSystemPackage }} System • ${installedApps.count { !it.isSystemPackage }} User", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // PackageManager Real Device Inspector Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
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
                            Icon(Icons.Default.Android, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text("Android PackageManager Query", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Scan native device packages via PackageManager API", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = {
                                deviceQueriedApps = com.example.util.PackageManagerUtil.getInstalledDeviceApps(context)
                                showPackageManagerScanner = true
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ManageSearch, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan", fontSize = 12.sp)
                        }
                    }

                    if (deviceQueriedApps.isNotEmpty()) {
                        Text(
                            text = "Found ${deviceQueriedApps.size} PackageManager activities on device.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Search Bar for Installed Applications
        item {
            OutlinedTextField(
                value = appSearchQuery,
                onValueChange = { appSearchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search installed applications by name...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (appSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { appSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
        }

        // Sub Filter Tabs: All, User, System
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterType == 0,
                    onClick = { filterType = 0 },
                    label = { Text("All Installed (${installedApps.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterType == 1,
                    onClick = { filterType = 1 },
                    label = { Text("User Apps (${installedApps.count { !it.isSystemPackage }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterType == 2,
                    onClick = { filterType = 2 },
                    label = { Text("System Packages (${installedApps.count { it.isSystemPackage }})", fontSize = 11.sp) }
                )
            }
        }

        items(displayedApps, key = { it.appId.packageName }) { app ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAppClick(app) }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(app.iconColor),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(getAppIcon(app.appId), contentDescription = app.name, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (app.isSystemPackage) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            Text("SYSTEM", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                }
                                Text(app.appId.packageName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("v${app.installedVersion} • ${app.sizeMb} MB", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onOpenClick(app) },
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Launch, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (!app.isSystemPackage) {
                                IconButton(
                                    onClick = { onUninstallClick(app) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Uninstall", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Storage and Cache Quick Management
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val cacheMb = app.cacheSizeBytes / (1024 * 1024)
                        Text(
                            text = "Cache: $cacheMb MB • Data: ${(app.dataSizeBytes / (1024 * 1024))} MB",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (app.cacheSizeBytes > 0) {
                                TextButton(
                                    onClick = { onClearCache(app) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Clear Cache", fontSize = 10.sp)
                                }
                            }
                            TextButton(
                                onClick = { onClearData(app) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Reset Data", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPackageManagerScanner) {
        AlertDialog(
            onDismissRequest = { showPackageManagerScanner = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Android, null, tint = MaterialTheme.colorScheme.primary)
                    Text("PackageManager Grid (${deviceQueriedApps.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    com.example.ui.system.PackageManagerAppGrid(
                        columns = 3,
                        onAppClick = { app ->
                            com.example.util.PackageManagerUtil.launchPackage(context, app.packageName)
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPackageManagerScanner = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun SideloadTab(
    urlInput: String,
    customAppName: String,
    presetUrls: List<Pair<String, String>>,
    isInstalling: Boolean,
    installProgress: Float,
    installStatus: String,
    onUrlChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSideload: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Package Sideloading & URL Downloader", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Directly download & install APK or JSON manifests", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (isInstalling) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = installStatus,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            LinearProgressIndicator(
                                progress = { installProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "${(installProgress * 100).toInt()}% completed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = "Quick Presets:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetUrls.take(2).forEach { (name, url) ->
                                SuggestionChip(
                                    onClick = {
                                        onNameChange(name)
                                        onUrlChange(url)
                                    },
                                    label = { Text(name, fontSize = 10.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetUrls.drop(2).forEach { (name, url) ->
                                SuggestionChip(
                                    onClick = {
                                        onNameChange(name)
                                        onUrlChange(url)
                                    },
                                    label = { Text(name, fontSize = 10.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = customAppName,
                            onValueChange = onNameChange,
                            label = { Text("Application Name", fontSize = 12.sp) },
                            placeholder = { Text("e.g. My Custom Game") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = onUrlChange,
                            label = { Text("Package URL (.apk / .json / .zip)", fontSize = 12.sp) },
                            placeholder = { Text("https://example.com/app.apk") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Button(
                            onClick = onSideload,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = customAppName.isNotEmpty() && urlInput.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download & Sideload Package", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppStoreItemCard(
    app: StoreAppItem,
    onClick: () -> Unit,
    onInstallClick: () -> Unit,
    onOpenClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        shape = RoundedCornerShape(14.dp),
                        color = Color(app.iconColor),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                getAppIcon(app.appId),
                                contentDescription = app.name,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${app.developer} • ${app.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(13.dp))
                            Text("${app.rating} ★", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("• ${app.sizeMb} MB • ${app.downloads}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (app.isInstalling) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.primary)
                } else if (app.isInstalled) {
                    FilledTonalButton(
                        onClick = onOpenClick,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onInstallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Install", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (app.isInstalling) {
                LinearProgressIndicator(
                    progress = { app.installProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AppDetailDialog(
    app: StoreAppItem,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
    onOpen: () -> Unit,
    onUpdate: () -> Unit,
    onUninstall: () -> Unit,
    onClearCache: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(app.iconColor),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(getAppIcon(app.appId), contentDescription = app.name, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
                Column {
                    Text(app.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(app.developer, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(app.appId.packageName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Rating & Size Metric Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${app.rating} ★", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${app.ratingCount} reviews", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${app.sizeMb} MB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Download", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(app.downloads, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Downloads", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("v${app.version}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Version", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item {
                    Text("About this App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(app.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (app.availableUpdateVersion != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("What's New in v${app.availableUpdateVersion}:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                                Text(app.updateChangelog ?: "General stability and performance improvements.", fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    Text("Declared Permissions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        app.permissions.take(3).forEach { perm ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(perm, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }

                item {
                    Text("Package & Storage Breakdown", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("App Binaries:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${app.sizeMb} MB", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("User Data:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(app.dataSizeBytes / (1024 * 1024))} MB", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cache Storage:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${(app.cacheSizeBytes / (1024 * 1024))} MB", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (app.isInstalled) {
                    if (app.availableUpdateVersion != null) {
                        Button(
                            onClick = onUpdate,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Update")
                        }
                    }
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Open")
                    }
                    if (!app.isSystemPackage) {
                        OutlinedButton(
                            onClick = onUninstall,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Uninstall")
                        }
                    }
                } else {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Install")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
