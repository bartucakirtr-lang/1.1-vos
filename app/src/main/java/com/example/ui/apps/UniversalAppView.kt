package com.example.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.OSNotification
import com.example.ui.system.getAppIcon
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalAppView(
    appId: AppId,
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateHome()
    }

    val coroutineScope = rememberCoroutineScope()
    var isRunning by remember { mutableStateOf(true) }
    var actionCounter by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Genel Bakış, 1: Eylemler, 2: Sistem & Bilgi
    var isOptimizing by remember { mutableStateOf(false) }
    var logEntries by remember {
        mutableStateOf(
            listOf(
                "Vroxen UI Sanal Alanı (Sandbox) başlatıldı",
                "Bellek tahsisi: 14.8 MB",
                "İş parçacığı havuzu: 4 Worker Threads",
                "Ağ durumu: Çevrimiçi / 5G Ultra"
            )
        )
    }

    val appColor = Color(appId.iconColorHex)

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
                            color = appColor,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    getAppIcon(appId),
                                    contentDescription = appId.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(appId.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "${appId.packageName} • ${appId.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    IconButton(onClick = {
                        coroutineScope.launch {
                            isOptimizing = true
                            delay(600)
                            logEntries = listOf("Bellek temizlendi (Önbellek sıfırlandı)", "Veri akışı senkronize edildi") + logEntries
                            isOptimizing = false
                            viewModel.addNotification(
                                OSNotification(
                                    id = "opt_${System.currentTimeMillis()}",
                                    appId = appId,
                                    title = "${appId.title} Optimize Edildi",
                                    message = "Önbellek temizlendi ve performans 60 FPS'e sabitlendi."
                                )
                            )
                        }
                    }) {
                        if (isOptimizing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Optimize")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Category & Performance Banner
            Surface(
                color = appColor.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRunning) Color(0xFF00C853) else Color(0xFFFF5252),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = if (isRunning) "Çalışıyor (Aktif)" else "Beklemede",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = appColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "60 FPS • Vroxen UI Native Core",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Tab Bar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Genel Bakış", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("İşlemler", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Günlük & Sistem", fontSize = 12.sp) }
                )
            }

            // Tab Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            // Hero Interactive Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = appColor,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    getAppIcon(appId),
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(appId.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("Sürüm 2.4.0 (Vroxen UI Uyumlu)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                actionCounter++
                                                logEntries = listOf("Eylem #${actionCounter} başarıyla tetiklendi (${System.currentTimeMillis() % 1000}ms)") + logEntries
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = appColor),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Çalıştır ($actionCounter)", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                isRunning = !isRunning
                                                logEntries = listOf(if (isRunning) "Uygulama aktif duruma alındı" else "Uygulama duraklatıldı") + logEntries
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (isRunning) "Durdur" else "Başlat", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Quick Metric Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Bellek Kullanımı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("14.2 MB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                        Text("RAM optimizasyonu aktif", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("İşlem Sayısı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$actionCounter İşlem", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = appColor)
                                        Text("Hata: 0 • Çökme: 0", fontSize = 10.sp, color = Color(0xFF00C853))
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        item {
                            Text("Uygulama Araçları & Fonksiyonlar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        val actions = listOf(
                            "Veritabanını Yerel Olarak Senkronize Et" to Icons.Default.Sync,
                            "Arka Plan Görevlerini Başlat" to Icons.Default.Schedule,
                            "Önbelleği ve Geçici Dosyaları Boşalt" to Icons.Default.CleaningServices,
                            "Vroxen UI Sistem Entegrasyon Testi Yap" to Icons.Default.CheckCircle,
                            "Rapor ve Analitik Dökümü Al" to Icons.Default.Assessment
                        )

                        items(actions) { (title, icon) ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        actionCounter++
                                        logEntries = listOf("$title tamamlandı.") + logEntries
                                        viewModel.addNotification(
                                            OSNotification(
                                                id = "act_${System.currentTimeMillis()}",
                                                appId = appId,
                                                title = appId.title,
                                                message = "$title başarıyla gerçekleştirildi."
                                            )
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = appColor.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(icon, null, tint = appColor, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    2 -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Canlı İşlem & Telemetri Kayıtları", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                TextButton(onClick = { logEntries = emptyList() }) {
                                    Text("Temizle", fontSize = 11.sp)
                                }
                            }
                        }

                        items(logEntries) { entry ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Terminal, null, modifier = Modifier.size(14.dp), tint = appColor)
                                    Text(entry, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
