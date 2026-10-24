package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodel.OSViewModel
import com.example.viewmodel.VroxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VroxApp(osViewModel: OSViewModel) {
    val viewModel: VroxViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var showInfoDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "Vrox Güvenlik",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "Cihaz Koruma ve Gizlilik Merkezi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { osViewModel.closeCurrentApp() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Kapat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // 1. HERO STATUS CARD (Büyük ve Anlaşılır Güvenlik Durumu)
            item {
                val isPerfect = uiState.securityScore >= 90
                val heroColor = if (isPerfect) Color(0xFF00C853) else MaterialTheme.colorScheme.primary

                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPerfect) Color(0xFF00C853).copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.5.dp, heroColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = heroColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.isScanning) Icons.Default.Sync
                                    else if (isPerfect) Icons.Filled.CheckCircle
                                    else Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = heroColor,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }

                        Text(
                            text = if (uiState.isScanning) "Güvenlik Taraması Yapılıyor..." else uiState.status,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (uiState.isScanning) uiState.currentScanStage else uiState.statusSubtext,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        // Scan Progress Bar (Tarama yapılırken)
                        if (uiState.isScanning) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = { uiState.scanProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = heroColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Analiz ediliyor...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("%${(uiState.scanProgress * 100).toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "Güvenlik Puanı: %${uiState.securityScore}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = heroColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                                Text(
                                    text = "Son Tarama: ${uiState.lastScanTime}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Scan Button
                        Button(
                            onClick = { viewModel.scan() },
                            enabled = !uiState.isScanning,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("scan_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = heroColor)
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.isScanning) "Taranıyor..." else "Cihazı Şimdi Tara",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 2. ANLAŞILIR GÜVENLİK KALKANLARI
            item {
                Text(
                    text = "Güvenlik Kalkanlarınız",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Telefonunuzun arka planda sizi nasıl koruduğunu buradan yönetebilirsiniz.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Kalkan 1: Uygulama Koruması
                        SecurityShieldRow(
                            icon = Icons.Outlined.AppShortcut,
                            title = "Uygulama Kalkanı",
                            summary = "Yüklü uygulamaları ve güncellemeleri virüslere, casus yazılımlara karşı otomatik denetler.",
                            isEnabled = uiState.appShieldEnabled,
                            onToggle = { viewModel.toggleAppShield() },
                            onInfoClick = {
                                showInfoDialog = "Uygulama Kalkanı: Cihazınıza yüklenen her yeni dosya veya güncellemeyi zararlı kodlara karşı gerçek zamanlı inceler. Şüpheli bir durum tespit edildiğinde size hemen uyarı verir."
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Kalkan 2: Gizlilik ve İzin Bekçisi
                        SecurityShieldRow(
                            icon = Icons.Outlined.Visibility,
                            title = "Gizlilik Bekçisi",
                            summary = "Kamera, mikrofon veya konumunuza hangi uygulamanın ne zaman eriştiğini takip eder.",
                            isEnabled = uiState.privacyGuardEnabled,
                            onToggle = { viewModel.togglePrivacyGuard() },
                            onInfoClick = {
                                showInfoDialog = "Gizlilik Bekçisi: Arka planda gizlice çalışan hiçbir uygulamanın mikrofonunuza, kameranıza veya konumunuza sizden habersiz erişmesine izin vermez."
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Kalkan 3: Wi-Fi ve Ağ Güvenliği
                        SecurityShieldRow(
                            icon = Icons.Outlined.WifiLock,
                            title = "Wi-Fi & İnternet Güvenliği",
                            summary = "Bağlandığınız kablosuz ağları inceler; şifresiz veya sahte ağlara karşı korur.",
                            isEnabled = uiState.wifiSecurityEnabled,
                            onToggle = { viewModel.toggleWifiSecurity() },
                            onInfoClick = {
                                showInfoDialog = "Wi-Fi Koruması: Kafe, otel gibi açık ağlarda verilerinizin başkaları tarafından dinlenmesini önlemek için bağlantı güvenliğini kontrol eder."
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

                        // Kalkan 4: Arka Plan Koruması
                        SecurityShieldRow(
                            icon = Icons.Outlined.Speed,
                            title = "Arka Plan Koruma Kalkanı",
                            summary = "Sistemi yavaşlatan, pili aşırı tüketen şüpheli arka plan işlemlerini durdurur.",
                            isEnabled = uiState.backgroundProtectionEnabled,
                            onToggle = { viewModel.toggleBackgroundProtection() },
                            onInfoClick = {
                                showInfoDialog = "Arka Plan Kalkanı: Telefonunuzun her zaman akıcı çalışmasını sağlar. Gizlice pili tüketen veya aşırı veri kullanan yazılımları sınırlar."
                            }
                        )
                    }
                }
            }

            // 3. SON DENETLENEN ALANLAR (Checklist)
            item {
                Text(
                    text = "Güvenlik Denetim Özeti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        uiState.checks.forEach { check ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00C853),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(check.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(check.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF00C853).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Temiz",
                                        color = Color(0xFF00C853),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. KULLANICI İÇİN BASİT İPUÇLARI
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Outlined.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text("Biliyor muydunuz?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Tüm fotoğraflarınız, mesajlarınız ve uygulama verileriniz sistem düzeyinde AES-256 ile korunmaktadır. Telefonunuz kilitliyken verilerinize dışarıdan erişilemez.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Bilgilendirme Diyaloğu
    showInfoDialog?.let { infoText ->
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            title = { Text("Güvenlik Kalkanı Hakkında", fontWeight = FontWeight.Bold) },
            text = { Text(infoText, fontSize = 13.sp, lineHeight = 18.sp) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = null }) {
                    Text("Anladım")
                }
            }
        )
    }
}

@Composable
private fun SecurityShieldRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    summary: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Bilgi",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = summary,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() }
        )
    }
}
