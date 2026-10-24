package com.example.ui.system

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.GitHubUpdateService
import com.example.util.DownloadStatus
import com.example.util.GitHubUpdateManager
import com.example.util.RemoteAppMetadata
import com.example.util.UpdateCheckResult
import kotlinx.coroutines.launch

@Composable
fun GitHubRawUpdateCard(
    currentVersion: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var rawUrlInput by remember {
        mutableStateOf("https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/metadata.json")
    }
    var isExpanded by remember { mutableStateOf(false) }

    val checkState by GitHubUpdateManager.checkState.collectAsState()
    val downloadStatus by GitHubUpdateManager.downloadStatus.collectAsState()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "GitHub Raw Metadata Güncelleyici",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Uzak metadata.json kontrolü ve indirme servisi",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Genişlet/Daralt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Herhangi bir GitHub raw URL'sinden metadata.json dosyasını çekip sürüm kontrolü yapabilir ve varsa güncellemeyi arka planda indirebilirsiniz.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // URL Input Field
            OutlinedTextField(
                value = rawUrlInput,
                onValueChange = { rawUrlInput = it },
                label = { Text("GitHub Raw metadata.json URL") },
                placeholder = { Text("https://raw.githubusercontent.com/.../metadata.json") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (rawUrlInput.isNotBlank()) {
                        IconButton(onClick = { rawUrlInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            )

            // Preset Buttons for quick testing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {
                        rawUrlInput = "https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/metadata.json"
                    },
                    label = { Text("1.1-vos (metadata.json)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                SuggestionChip(
                    onClick = {
                        rawUrlInput = "https://raw.githubusercontent.com/google/ai-studio/main/metadata.json"
                    },
                    label = { Text("AI Studio Örneği", fontSize = 11.sp) }
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = GitHubUpdateManager.checkUpdateFromRawUrl(
                                rawUrl = rawUrlInput,
                                currentVersion = currentVersion
                            )
                            when (result) {
                                is UpdateCheckResult.UpdateAvailable -> {
                                    Toast.makeText(context, "Yeni sürüm bulundu: ${result.remoteMetadata.version}", Toast.LENGTH_SHORT).show()
                                }
                                is UpdateCheckResult.UpToDate -> {
                                    Toast.makeText(context, "Sistem zaten en son sürümde", Toast.LENGTH_SHORT).show()
                                }
                                is UpdateCheckResult.Error -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                }
                                else -> Unit
                            }
                        }
                    },
                    enabled = rawUrlInput.isNotBlank() && checkState !is UpdateCheckResult.Checking,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    if (checkState is UpdateCheckResult.Checking) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Çekiliyor...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Metadata'yı Çek", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                FilledTonalButton(
                    onClick = {
                        GitHubUpdateService.startCheck(
                            context = context,
                            rawUrl = rawUrlInput,
                            currentVersion = currentVersion
                        )
                        Toast.makeText(context, "Arka plan Android Servisi başlatıldı", Toast.LENGTH_SHORT).show()
                    },
                    enabled = rawUrlInput.isNotBlank(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(46.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Servisi Çalıştır", fontSize = 12.sp)
                }
            }

            // Results Display
            when (val state = checkState) {
                is UpdateCheckResult.Checking -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("GitHub Raw sunucusundan metadata.json alınıyor ve ayrıştırılıyor...", fontSize = 12.sp)
                        }
                    }
                }

                is UpdateCheckResult.Error -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                        }
                    }
                }

                is UpdateCheckResult.UpdateAvailable -> {
                    val meta = state.remoteMetadata
                    MetadataResultCard(
                        metadata = meta,
                        isNewer = true,
                        currentVersion = state.currentVersion,
                        downloadStatus = downloadStatus,
                        onDownloadClick = {
                            coroutineScope.launch {
                                val downloadedFile = GitHubUpdateManager.downloadUpdateFile(context, meta)
                                if (downloadedFile != null) {
                                    Toast.makeText(context, "İndirme tamamlandı: ${downloadedFile.name}", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        onSystemDownloadManagerClick = {
                            GitHubUpdateManager.enqueueViaDownloadManager(context, meta)
                            Toast.makeText(context, "Android DownloadManager'a kuyruğa alındı", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                is UpdateCheckResult.UpToDate -> {
                    val meta = state.remoteMetadata
                    MetadataResultCard(
                        metadata = meta,
                        isNewer = false,
                        currentVersion = state.currentVersion,
                        downloadStatus = downloadStatus,
                        onDownloadClick = {
                            coroutineScope.launch {
                                GitHubUpdateManager.downloadUpdateFile(context, meta)
                            }
                        },
                        onSystemDownloadManagerClick = {
                            GitHubUpdateManager.enqueueViaDownloadManager(context, meta)
                        }
                    )
                }

                UpdateCheckResult.Idle -> Unit
            }
        }
    }
}

@Composable
private fun MetadataResultCard(
    metadata: RemoteAppMetadata,
    isNewer: Boolean,
    currentVersion: String,
    downloadStatus: DownloadStatus,
    onDownloadClick: () -> Unit,
    onSystemDownloadManagerClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isNewer) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isNewer) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
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
                Column {
                    Text(
                        text = metadata.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Uzak Sürüm: ${metadata.version} (Kod: ${metadata.versionCode}) • Mevcut: $currentVersion",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isNewer) MaterialTheme.colorScheme.primary else Color(0xFF00C853)
                ) {
                    Text(
                        text = if (isNewer) "YENİ SÜRÜM" else "GÜNCEL",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (metadata.changelog.isNotEmpty()) {
                Text("Değişiklik Notları:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                metadata.changelog.forEach { logItem ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(logItem, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Download Status & Progress Bar
            when (downloadStatus) {
                is DownloadStatus.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("İndiriliyor: ${metadata.fileName}", fontSize = 11.sp)
                            Text("%${downloadStatus.progressPercent}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { downloadStatus.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                is DownloadStatus.Completed -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF00C853)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF00C853), modifier = Modifier.size(18.dp))
                            Column {
                                Text("İndirme Tamamlandı!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                                Text("Kayıt: ${downloadStatus.file.absolutePath}", fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (downloadStatus.isChecksumValid) {
                                    Text("✓ SHA-256 bütünlüğü doğrulandı.", fontSize = 10.sp, color = Color(0xFF00C853))
                                }
                            }
                        }
                    }
                }

                is DownloadStatus.Failed -> {
                    Text("Hata: ${downloadStatus.error}", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                DownloadStatus.Idle -> Unit
            }

            // Download Trigger Buttons
            if (metadata.downloadUrl.isNotBlank() && downloadStatus !is DownloadStatus.Downloading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDownloadClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNewer) "Güncellemeyi İndir" else "Yeniden İndir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onSystemDownloadManagerClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DownloadManager", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
