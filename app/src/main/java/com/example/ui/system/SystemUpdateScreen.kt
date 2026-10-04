package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    val betaProgram by viewModel.betaProgramEnabled.collectAsState()
    val lastCheckTime by viewModel.lastUpdateCheckTime.collectAsState()

    var updateState by remember { mutableStateOf(UpdateState.UP_TO_DATE) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadSpeed by remember { mutableStateOf("0 MB/s") }
    var downloadedMB by remember { mutableIntStateOf(0) }
    val totalMB = 846

    var showChangelogDialog by remember { mutableStateOf(false) }
    var showPreferencesDialog by remember { mutableStateOf(false) }

    // Breathing glow animation for the official vos 3 update logo
    val infiniteTransition = rememberInfiniteTransition(label = "LogoGlow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowScale"
    )

    fun startCheckForUpdates() {
        coroutineScope.launch {
            updateState = UpdateState.CHECKING
            delay(1800)
            val nowStr = SimpleDateFormat("dd MMMM, HH:mm", Locale("tr")).format(Date())
            viewModel.updateLastCheckTime("Bugün, " + SimpleDateFormat("HH:mm", Locale("tr")).format(Date()))
            
            // If current system version is vos 3.0, update available is vos 3.1.2!
            // If already 3.1.2, it remains up to date
            if (systemVersion.contains("3.0")) {
                updateState = UpdateState.UPDATE_AVAILABLE
            } else {
                updateState = UpdateState.UP_TO_DATE
            }
        }
    }

    fun startDownloadAndInstall() {
        coroutineScope.launch {
            updateState = UpdateState.DOWNLOADING
            downloadProgress = 0f
            downloadedMB = 0

            // Simulate realistic progressive download
            for (step in 1..20) {
                delay(180)
                downloadProgress = step / 20f
                downloadedMB = (downloadProgress * totalMB).toInt()
                val speed = (22 + (step % 5) * 2.3f)
                downloadSpeed = String.format(Locale.US, "%.1f MB/s", speed)
            }

            updateState = UpdateState.VERIFYING
            delay(1200)

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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Hero Presentation Card with Official vos 3 Logo
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
                            .padding(vertical = 28.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Official vos 3 System Update Badge with Animated Halo
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(130.dp)
                        ) {
                            // Pulsing gradient halo ring
                            Box(
                                modifier = Modifier
                                    .size(126.dp)
                                    .scale(glowScale)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                            )

                            // Official Logo Squircle Container
                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = Color(0xFF0D0F17),
                                shadowElevation = 16.dp,
                                border = BorderStroke(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                                modifier = Modifier.size(108.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_system_update_v3),
                                    contentDescription = "vos 3 System Update Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(28.dp))
                                )
                            }
                        }

                        // Version & Status Typography
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = systemVersion,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Android 16 • Resmi Kararlı Sürüm",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
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
                                            text = "Sisteminiz Güncel",
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
                                            text = "Yeni Güncelleme Mevcut! • vos 3.1.2",
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
                                            text = "Paket İndiriliyor • ${(downloadProgress * 100).toInt()}%",
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
                                            text = "Yüklemeye Hazır",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                }
                            }
                        }

                        // Progress bar during downloading
                        if (updateState == UpdateState.DOWNLOADING || updateState == UpdateState.VERIFYING) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
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
                                        text = "$downloadedMB MB / $totalMB MB",
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

            // 2. Action Controls (Check / Download / Reboot)
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
                            Text("Sunucuyla İletişim Kuruluyor...")
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
                                    "İndir ve Yükle ($totalMB MB)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            OutlinedButton(
                                onClick = { updateState = UpdateState.UP_TO_DATE },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("Daha Sonra Hatırlat")
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
                                viewModel.applySystemUpdate("vos 3.1.2")
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
                                "Şimdi Yeniden Başlat ve Güncelle",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 3. Changelog / Yenilikler Card
            item {
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
                                    text = if (updateState == UpdateState.UPDATE_AVAILABLE || updateState == UpdateState.DOWNLOADING || updateState == UpdateState.READY_TO_INSTALL)
                                        "vos 3.1.2 Yenilikleri" else "vos 3 Sürüm Notları",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            TextButton(onClick = { showChangelogDialog = true }) {
                                Text("Tümünü Gör", fontSize = 12.sp)
                            }
                        }

                        // Feature bullet points
                        ChangelogItem(
                            icon = Icons.Outlined.Speed,
                            title = "HyperCore Akıcılık Motoru",
                            description = "Uygulama açılış süreleri %45 hızlandırıldı, RAM tüketimi azaltıldı."
                        )
                        ChangelogItem(
                            icon = Icons.Outlined.Wallpaper,
                            title = "Gerçek Telefon Duvar Kağıdı",
                            description = "Cihazın kendi Android sistem arka planı şeffaf pencere ile doğrudan entegre edildi."
                        )
                        ChangelogItem(
                            icon = Icons.Outlined.Widgets,
                            title = "Gelişmiş Widget Yöneticisi",
                            description = "Masaüstüne özgürce widget ekleme, silme ve yeniden sıralama desteği."
                        )
                        ChangelogItem(
                            icon = Icons.Outlined.Palette,
                            title = "Yeni Simge Stilleri",
                            description = "One UI Squircle, Pixel Yuvarlak ve Neon simge paketleri eklendi."
                        )
                    }
                }
            }

            // 4. System Specs & Last Checked Card
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
                        SystemInfoRow(label = "Cihaz Modeli", value = "vos Phone Pro (VOS-2026)")
                        SystemInfoRow(label = "Android Sürümü", value = "Android 16 (Baklava)")
                        SystemInfoRow(label = "Güvenlik Yaması Düzeyi", value = "1 Ekim 2026")
                        SystemInfoRow(label = "Çekirdek Sürümü", value = "Linux 6.6.45-vos-hypercore")
                        SystemInfoRow(label = "Yapı Numarası", value = "VOS-UKQ1.240905.001-R3")
                    }
                }
            }

            // 5. Update Preferences (Auto Download & Beta Program)
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
                                Text("Otomatik İndirme", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
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

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("vos Insider (Beta) Programı", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    "Erken erişim test sürümlerini ve deneysel özellikleri al",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = betaProgram,
                                onCheckedChange = {
                                    viewModel.setBetaProgramEnabled(it)
                                    if (it) {
                                        startCheckForUpdates()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Full Changelog Dialog
    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.HistoryEdu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("vos 3.1.2 Sürüm Günlüğü", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Resmi vos 3.1.2 Kararlı Güncellemesi (Ekim 2026)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "• Ultra Hızlı Başlatıcı: Sayfalama animasyonları ve çekme hareketleri optimize edildi.\n" +
                        "• Sistem Duvar Kağıdı: Canlı veya statik telefon duvar kağıdının doğrudan yansıtılması sağlandı.\n" +
                        "• Widget Kişiselleştirme: Ana ekrana istediğiniz widget'ları ekleyin, çıkarın veya sıralayın.\n" +
                        "• Dinamik Simge Biçimleri: Squircle, Daire, Yuvarlatılmış Kare, Gözyaşı ve Neon ışık efektleri.\n" +
                        "• Gemini AI / Hey Vroxen: Sesli asistan arayüzü ve yerel zeka yanıtları güçlendirildi.\n" +
                        "• Güvenlik: 2026.10 Android 16 güvenlik yaması entegrasyonu.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    Text("• Güncellemeler otomatik olarak arka planda indirilir.")
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
private fun ChangelogItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
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
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
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
