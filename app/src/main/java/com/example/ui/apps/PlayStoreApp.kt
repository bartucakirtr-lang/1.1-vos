@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var showSideloadSection by remember { mutableStateOf(true) }
    var urlInput by remember { mutableStateOf("https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/config.json") }
    var customAppName by remember { mutableStateOf("vos 1.1 App") }
    
    var isInstalling by remember { mutableStateOf(false) }
    var installProgress by remember { mutableFloatStateOf(0f) }
    var installStatus by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    
    val presetUrls = listOf(
        "vos 1.1 App" to "https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/config.json",
        "Retro Space Invaders" to "https://novaos.net/arcade/space_invaders.apk",
        "Chess Premium AI" to "https://novaos.net/arcade/chess_master.apk",
        "Scientific Calculator Pro" to "https://novaos.net/tools/sci_calculator.apk"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nexus Store", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner Card
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
                        Text(
                            text = "Featured for NovaOS",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Play Arcade Games & System Terminal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Install high-performance native tools and games directly into your NovaOS launcher.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSideloadSection = !showSideloadSection },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("Install App from URL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Download & sideload secure custom apps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Icon(
                                imageVector = if (showSideloadSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = "Toggle"
                            )
                        }

                        AnimatedVisibility(
                            visible = showSideloadSection,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (isInstalling) {
                                    // Sideload Installation Progress Bar
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
                                    // Preset Dropdown Chips
                                    Text(
                                        text = "Quick Select Demo Presets:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        presetUrls.take(2).forEach { (name, url) ->
                                            SuggestionChip(
                                                onClick = {
                                                    customAppName = name
                                                    urlInput = url
                                                },
                                                label = { Text(name, fontSize = 10.sp) }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        presetUrls.drop(2).forEach { (name, url) ->
                                            SuggestionChip(
                                                onClick = {
                                                    customAppName = name
                                                    urlInput = url
                                                },
                                                label = { Text(name, fontSize = 10.sp) }
                                            )
                                        }
                                    }

                                    // Custom Name Field
                                    OutlinedTextField(
                                        value = customAppName,
                                        onValueChange = { customAppName = it },
                                        label = { Text("Application Name", fontSize = 12.sp) },
                                        placeholder = { Text("e.g. My Custom Game") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    // Custom URL Field
                                    OutlinedTextField(
                                        value = urlInput,
                                        onValueChange = { urlInput = it },
                                        label = { Text("App Package URL (.apk / .json / .zip)", fontSize = 12.sp) },
                                        placeholder = { Text("https://example.com/app.apk") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (customAppName.isNotEmpty() && urlInput.isNotEmpty()) {
                                                isInstalling = true
                                                installProgress = 0f
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    try {
                                                        installStatus = "Connecting to URL host..."
                                                        installProgress = 0.15f
                                                        
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
                                                                installStatus = "Downloading package binaries..."
                                                                installProgress = 0.5f
                                                                val bodyBytes = response.body?.bytes()
                                                                val sizeKb = (bodyBytes?.size ?: 0) / 1024
                                                                
                                                                installStatus = "Verifying $sizeKb KB package integrity..."
                                                                installProgress = 0.8f
                                                                delay(900)
                                                                
                                                                installStatus = "Sideloading custom build into launcher desktop..."
                                                                installProgress = 0.95f
                                                                delay(700)
                                                                
                                                                withContext(Dispatchers.Main) {
                                                                    viewModel.installAppFromUrl(customAppName, urlInput)
                                                                    isInstalling = false
                                                                    showSideloadSection = false
                                                                    urlInput = ""
                                                                    customAppName = ""
                                                                }
                                                            } else {
                                                                withContext(Dispatchers.Main) {
                                                                    installStatus = "HTTP Connection Error: ${response.code}"
                                                                    delay(2000)
                                                                    isInstalling = false
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        withContext(Dispatchers.Main) {
                                                            installStatus = "Host Connection Error: ${e.message ?: "Check URL"}"
                                                            delay(2500)
                                                            isInstalling = false
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        enabled = customAppName.isNotEmpty() && urlInput.isNotEmpty()
                                    ) {
                                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Sideload & Install", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Recommended Apps & Games", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            items(storeApps, key = { it.appId.packageName }) { app ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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

                        Button(
                            onClick = { viewModel.toggleInstallStoreApp(app.appId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (app.isInstalled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = if (app.isInstalled) "Uninstall" else "Install",
                                fontSize = 12.sp,
                                color = if (app.isInstalled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
