package com.example.ui.apps

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DeviceInstalledApp
import com.example.util.PackageManagerUtil
import com.example.util.toBitmapOrNull
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageManagerApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateHome()
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0: Başlatılabilir, 1: Tümü, 2: Kullanıcı, 3: Sistem
    var isLoading by remember { mutableStateOf(false) }
    var rawPackagesList by remember { mutableStateOf<List<DeviceInstalledApp>>(emptyList()) }
    var selectedAppForDetail by remember { mutableStateOf<DeviceInstalledApp?>(null) }

    fun refreshPackages() {
        coroutineScope.launch {
            isLoading = true
            val queried = withContext(Dispatchers.IO) {
                PackageManagerUtil.getAllInstalledPackages(context)
            }
            rawPackagesList = queried
            delay(250)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshPackages()
    }

    val filteredApps = remember(rawPackagesList, searchQuery, selectedFilter) {
        rawPackagesList.filter { app ->
            val matchesFilter = when (selectedFilter) {
                0 -> app.isLaunchable
                2 -> !app.isSystemApp
                3 -> app.isSystemApp
                else -> true
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true) ||
                app.permissions.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesFilter && matchesQuery
        }
    }

    val totalCount = rawPackagesList.size
    val launchableCount = remember(rawPackagesList) { rawPackagesList.count { it.isLaunchable } }
    val userCount = remember(rawPackagesList) { rawPackagesList.count { !it.isSystemApp } }
    val systemCount = remember(rawPackagesList) { rawPackagesList.count { it.isSystemApp } }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Android,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text("Paket Yöneticisi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Android PackageManager API", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        val deviceHomeApps by viewModel.deviceHomeApps.collectAsState()
                        IconButton(onClick = {
                            val launchables = rawPackagesList.filter { it.isLaunchable }.map { it.packageName }
                            viewModel.addAllDeviceAppsToHome(launchables)
                            Toast.makeText(context, "${launchables.size} uygulama ana menüye eklendi!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.AddToHomeScreen, contentDescription = "Tümünü Ana Menüye Ekle", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { refreshPackages() }) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Uygulama veya paket adı ara (örn. com.google)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Filter Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == 0,
                            onClick = { selectedFilter = 0 },
                            label = { Text("Başlatılabilir ($launchableCount)", fontSize = 11.sp, fontWeight = if (selectedFilter == 0) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 1,
                            onClick = { selectedFilter = 1 },
                            label = { Text("Tümü ($totalCount)", fontSize = 11.sp, fontWeight = if (selectedFilter == 1) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Default.Apps, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 2,
                            onClick = { selectedFilter = 2 },
                            label = { Text("Kullanıcı ($userCount)", fontSize = 11.sp, fontWeight = if (selectedFilter == 2) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == 3,
                            onClick = { selectedFilter = 3 },
                            label = { Text("Sistem ($systemCount)", fontSize = 11.sp, fontWeight = if (selectedFilter == 3) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = { Icon(Icons.Default.SettingsApplications, null, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (filteredApps.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Eşleşen paket bulunamadı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Arama kriterini veya filtreyi değiştirin.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Stats Summary Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
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
                                    Text("Cihaz Paket Veritabanı", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "PackageManager ile taranan $totalCount paket • ${filteredApps.size} listeleniyor",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "LIVE SDK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(filteredApps, key = { it.packageName }) { app ->
                        val imageBitmap = remember(app.iconDrawable) {
                            app.iconDrawable?.toBitmapOrNull()?.asImageBitmap()
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAppForDetail = app }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // App Icon
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.size(48.dp)
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
                                                Icons.Default.Android,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                // App Info
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = app.appName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (app.isSystemApp) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    "SİSTEM",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = app.packageName,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text("v${app.versionName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("SDK ${app.targetSdkVersion}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (app.permissions.isNotEmpty()) {
                                            Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${app.permissions.size} İzin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                // Launch / Info Actions
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (app.isLaunchable) {
                                        FilledTonalButton(
                                            onClick = {
                                                val launched = PackageManagerUtil.launchPackage(context, app.packageName)
                                                if (!launched) {
                                                    Toast.makeText(context, "${app.appName} başlatılamadı.", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Launch, null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Aç", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        IconButton(onClick = { selectedAppForDetail = app }) {
                                            Icon(Icons.Default.Info, contentDescription = "Bilgi", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Package Details Modal Bottom Sheet
            selectedAppForDetail?.let { app ->
                val imageBitmap = remember(app.iconDrawable) {
                    app.iconDrawable?.toBitmapOrNull()?.asImageBitmap()
                }
                val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

                ModalBottomSheet(
                    onDismissRequest = { selectedAppForDetail = null },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (imageBitmap != null) {
                                        Image(
                                            bitmap = imageBitmap,
                                            contentDescription = app.appName,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Icon(Icons.Default.Android, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    app.packageName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (app.isLaunchable) {
                                Button(
                                    onClick = {
                                        PackageManagerUtil.launchPackage(context, app.packageName)
                                        selectedAppForDetail = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Uygulamayı Aç")
                                }
                            }

                            val isAddedToHome = viewModel.deviceHomeApps.collectAsState().value.contains(app.packageName)
                            FilledTonalButton(
                                onClick = {
                                    if (isAddedToHome) {
                                        viewModel.removeDeviceAppFromHome(app.packageName)
                                        Toast.makeText(context, "${app.appName} ana menüden kaldırıldı.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addDeviceAppToHome(app.packageName)
                                        Toast.makeText(context, "${app.appName} ana menüye eklendi!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (isAddedToHome) Icons.Default.Check else Icons.Default.AddToHomeScreen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAddedToHome) "Ana Menüde" else "Ana Menüye Ekle", maxLines = 1, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    PackageManagerUtil.openAppDetailsSettings(context, app.packageName)
                                    selectedAppForDetail = null
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Settings, null, modifier = Modifier.size(16.dp))
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(app.packageName))
                                    Toast.makeText(context, "Paket adı kopyalandı: ${app.packageName}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala")
                            }
                        }

                        // Technical Specs Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Teknik Paket Bilgileri", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Sürüm Adı / Kodu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${app.versionName} (${app.versionCode})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Hedef SDK (Target SDK)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Android API ${app.targetSdkVersion}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Kullanıcı Kimliği (UID)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${app.uid}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("İlk Kurulum Tarihi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dateFormat.format(Date(app.firstInstallTime)), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Son Güncelleme", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(dateFormat.format(Date(app.lastUpdateTime)), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                if (app.sourceDir.isNotBlank()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("APK Konumu:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            app.sourceDir,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Permissions List
                        if (app.permissions.isNotEmpty()) {
                            Text("Tanımlı İzinler (${app.permissions.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(app.permissions) { perm ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Security,
                                                null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = perm.substringAfterLast("."),
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium
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
    }
}
