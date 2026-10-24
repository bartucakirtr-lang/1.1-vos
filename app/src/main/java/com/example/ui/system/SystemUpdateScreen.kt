package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.VOSFeatureHighlight
import com.example.model.VOSRelease
import com.example.model.VOSVersionCatalog
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class UpdateState {
    UP_TO_DATE,
    CHECKING,
    UPDATE_AVAILABLE,
    DOWNLOADING,
    VERIFYING,
    READY_TO_INSTALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemUpdateScreen(
    viewModel: OSViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val systemVersion by viewModel.systemVersion.collectAsState()
    val autoDownload by viewModel.autoDownloadUpdates.collectAsState()
    val wifiOnly by viewModel.wifiOnlyUpdates.collectAsState()
    val lastCheckTime by viewModel.lastUpdateCheckTime.collectAsState()

    val currentRelease = remember(systemVersion) {
        VOSVersionCatalog.getRelease(systemVersion)
    }

    val nextPossibleRelease = remember(systemVersion) {
        VOSVersionCatalog.getNextRelease(systemVersion)
    }

    var targetRelease by remember(systemVersion) {
        mutableStateOf(nextPossibleRelease ?: currentRelease)
    }

    val isDue = remember(systemVersion) {
        viewModel.isUpdateDue()
    }

    var updateState by remember(systemVersion, isDue) {
        mutableStateOf(
            if (isDue && nextPossibleRelease != null) UpdateState.UPDATE_AVAILABLE
            else UpdateState.UP_TO_DATE
        )
    }

    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadSpeed by remember { mutableStateOf("0 MB/s") }
    var downloadedMB by remember { mutableIntStateOf(0) }

    var showChangelogDialog by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }
    var selectedReleaseForChangelog by remember { mutableStateOf<VOSRelease?>(null) }

    // Breathing glow animation for official Vroxen UI 3 logo
    val infiniteTransition = rememberInfiniteTransition(label = "LogoGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    fun startCheckForUpdates() {
        coroutineScope.launch {
            updateState = UpdateState.CHECKING
            delay(1400)
            val nowStr = "Bugün, " + SimpleDateFormat("HH:mm", Locale("tr")).format(Date())
            viewModel.updateLastCheckTime(nowStr)

            val next = VOSVersionCatalog.getNextRelease(systemVersion)
            if (next != null && viewModel.isUpdateDue()) {
                targetRelease = next
                updateState = UpdateState.UPDATE_AVAILABLE
            } else {
                targetRelease = currentRelease
                updateState = UpdateState.UP_TO_DATE
            }
        }
    }

    fun startDownloadAndInstall(release: VOSRelease = targetRelease) {
        targetRelease = release
        coroutineScope.launch {
            updateState = UpdateState.DOWNLOADING
            downloadProgress = 0f
            downloadedMB = 0

            // Progressive download simulation
            val totalSteps = 20
            for (step in 1..totalSteps) {
                delay(120)
                downloadProgress = step.toFloat() / totalSteps.toFloat()
                downloadedMB = (downloadProgress * targetRelease.sizeMB).toInt()
                val speed = 28.5f + (step % 4) * 4.2f
                downloadSpeed = String.format(Locale.US, "%.1f MB/s", speed)
            }

            updateState = UpdateState.VERIFYING
            delay(900)

            updateState = UpdateState.READY_TO_INSTALL
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sistem Güncellemesi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { startCheckForUpdates() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                    IconButton(onClick = { showPreferencesDialog = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Presentation Card with Official Vroxen UI 3 Logo
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp, horizontal = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Official Vroxen UI 2 vs Vroxen UI 3 Logo Presentation
                        val isCurrentLegacyVOS2 = (currentRelease.version.startsWith("Vroxen UI 2.", ignoreCase = true) || currentRelease.version.startsWith("vos 2.", ignoreCase = true))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(126.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(122.dp)
                                    .scale(glowScale)
                                    .background(
                                        Brush.radialGradient(
                                            colors = if (isCurrentLegacyVOS2) listOf(
                                                Color(0xFF00E676).copy(alpha = 0.45f),
                                                Color(0xFF00B0FF).copy(alpha = 0.2f),
                                                Color.Transparent
                                            ) else listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )

                            if (isCurrentLegacyVOS2) {
                                Surface(
                                    shape = RoundedCornerShape(26.dp),
                                    color = Color(0xFF002923),
                                    shadowElevation = 14.dp,
                                    border = BorderStroke(
                                        2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF00E676),
                                                Color(0xFF00B0FF)
                                            )
                                        )
                                    ),
                                    modifier = Modifier.size(102.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Vroxen UI 2",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF00E676)
                                            )
                                            Text(
                                                text = "AURORA",
                                                fontSize = 9.sp,
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
                                    color = Color(0xFF0D0F17),
                                    shadowElevation = 14.dp,
                                    border = BorderStroke(
                                        2.dp,
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                            )
                                        )
                                    ),
                                    modifier = Modifier.size(102.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_system_update_v3),
                                        contentDescription = "Vroxen UI 3 System Update Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(26.dp))
                                    )
                                }
                            }
                        }

                        // Version & Codename Heading
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = currentRelease.version,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCurrentLegacyVOS2) Color(0xFF004D40) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = currentRelease.badge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrentLegacyVOS2) Color(0xFF00E676) else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isCurrentLegacyVOS2) "Android 15 (Vanilla Ice Cream) • ${currentRelease.codename} (${currentRelease.buildNumber})"
                                else "Android 16 • ${currentRelease.codename} (${currentRelease.buildNumber})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isCurrentLegacyVOS2) Color(0xFF00B0FF) else MaterialTheme.colorScheme.primary
                            )
                        }

                        // Dynamic Status Badge Pill
                        when (updateState) {
                            UpdateState.UP_TO_DATE -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFF00C853).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF00C853).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF00C853),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (currentRelease.isGoldenMaster)
                                                "Sisteminiz En Güncel Sürümde (Vroxen UI 3.9 Golden Master)"
                                            else "Sisteminiz Güncel (${currentRelease.version})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00C853)
                                        )
                                    }
                                }
                            }
                            UpdateState.CHECKING -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Güncellemeler Denetleniyor...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            UpdateState.UPDATE_AVAILABLE -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFFF9100).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFFFF9100).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.NewReleases,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9100),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Yeni Güncelleme Mevcut! • ${targetRelease.version}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9100)
                                        )
                                    }
                                }
                            }
                            UpdateState.DOWNLOADING -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Download,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${targetRelease.version} İndiriliyor • ${(downloadProgress * 100).toInt()}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                            UpdateState.VERIFYING -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFF7C4DFF).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF7C4DFF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "SHA-256 Paketi Doğrulanıyor...",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7C4DFF)
                                        )
                                    }
                                }
                            }
                            UpdateState.READY_TO_INSTALL -> {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.SystemUpdateAlt,
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${targetRelease.version} Yüklemeye Hazır",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                }
                            }
                        }

                        // Progress Bar during download / verification
                        if (updateState == UpdateState.DOWNLOADING || updateState == UpdateState.VERIFYING) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "$downloadedMB MB / ${targetRelease.sizeMB} MB",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = downloadSpeed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Action Buttons (Check / Download / Reboot)
            item {
                when (updateState) {
                    UpdateState.UP_TO_DATE -> {
                        Button(
                            onClick = { startCheckForUpdates() },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Güncellemeleri Denetle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    UpdateState.CHECKING -> {
                        FilledTonalButton(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Güncellemeler Denetleniyor...")
                        }
                    }
                    UpdateState.UPDATE_AVAILABLE -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { startDownloadAndInstall() },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                            ) {
                                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${targetRelease.version} İndir ve Yükle (${targetRelease.sizeMB} MB)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { updateState = UpdateState.UP_TO_DATE },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                            ) {
                                Text("Daha Sonra Hatırlat", fontSize = 13.sp)
                            }
                        }
                    }
                    UpdateState.DOWNLOADING, UpdateState.VERIFYING -> {
                        OutlinedButton(
                            onClick = { updateState = UpdateState.UPDATE_AVAILABLE },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("İndirmeyi Duraklat")
                        }
                    }
                    UpdateState.READY_TO_INSTALL -> {
                        Button(
                            onClick = {
                                viewModel.applySystemUpdate(targetRelease.version)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00C853)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Şimdi Yeniden Başlat ve ${targetRelease.version}'a Güncelle",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 3. GitHub Raw Metadata Remote Updater Card
            item {
                GitHubRawUpdateCard(currentVersion = systemVersion)
            }

            // 4. Changelog / Features Card for Target or Current Release
            item {
                val displayRelease = if (updateState == UpdateState.UPDATE_AVAILABLE || updateState == UpdateState.DOWNLOADING || updateState == UpdateState.READY_TO_INSTALL)
                    targetRelease else currentRelease

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                    Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "${displayRelease.version} Yenilikleri (${displayRelease.codename})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(onClick = {
                                selectedReleaseForChangelog = displayRelease
                                showChangelogDialog = true
                            }) {
                                Text("Tümünü Gör", fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = displayRelease.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Feature bullet points
                        displayRelease.highlights.forEach { highlight ->
                            VOSHighlightRow(highlight)
                        }
                    }
                }
            }

            // 5. System Specifications & Status Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Cihaz ve Sistem Bilgileri",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        SystemInfoRow(label = "Son Denetleme", value = lastCheckTime)
                        SystemInfoRow(label = "Yüklü Sürüm", value = "${currentRelease.version} (${currentRelease.codename})")
                        SystemInfoRow(label = "Cihaz Modeli", value = "Vroxen UI Phone Pro (VROXEN-2026)")
                        SystemInfoRow(label = "Android Sürümü", value = "Android 16 (Baklava)")
                        SystemInfoRow(label = "Güvenlik Yaması Düzeyi", value = currentRelease.securityPatch)
                        SystemInfoRow(label = "Yapı Numarası", value = currentRelease.buildNumber)
                    }
                }
            }

            // 6. Update Preferences Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Otomatik Güncelleme İndirme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Yeni güncellemeleri Wi-Fi üzerinden otomatik indir",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoDownload,
                                onCheckedChange = { viewModel.setAutoDownloadUpdates(it) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Yalnızca Wi-Fi Üzerinden", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Hücresel veri kullanımını sınırla",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = wifiOnly,
                                onCheckedChange = { viewModel.setWifiOnlyUpdates(it) }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Full Changelog Dialog
    if (showChangelogDialog) {
        val displayRelease = selectedReleaseForChangelog ?: if (updateState == UpdateState.UPDATE_AVAILABLE || updateState == UpdateState.DOWNLOADING || updateState == UpdateState.READY_TO_INSTALL)
            targetRelease else currentRelease

        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            icon = {
                Icon(
                    Icons.Filled.HistoryEdu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${displayRelease.version} (${displayRelease.codename})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "${displayRelease.releaseDate} • ${displayRelease.sizeMB} MB",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = displayRelease.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = displayRelease.subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        Text(
                            text = "Önemli Değişiklikler:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(displayRelease.changelogList) { change ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(change, fontSize = 12.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Güvenlik Yaması: ${displayRelease.securityPatch}\nYapı: ${displayRelease.buildNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangelogDialog = false }) {
                    Text("Tamam", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Preferences Menu Dialog
    if (showPreferencesDialog) {
        AlertDialog(
            onDismissRequest = { showPreferencesDialog = false },
            title = { Text("Güncelleme Tercihleri", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("• Kararlı resmi güncellemeler güvenlik ve kararlılık denetiminden geçerek sunulur.")
                    Text("• Pil seviyesi %30'un üzerindeyken veya şarjdayken yükleme yapılır.")
                    Text("• Yükleme sonrasında tüm uygulama ve kişisel verileriniz korunur.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPreferencesDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
private fun VOSHighlightRow(highlight: VOSFeatureHighlight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = highlight.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = highlight.description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SystemInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
