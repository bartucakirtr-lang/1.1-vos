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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BrowserTab
import com.example.viewmodel.OSViewModel

@Composable
fun BrowserApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val browserTabs by viewModel.browserTabs.collectAsState()
    val currentTabId by viewModel.currentTabId.collectAsState()
    val urlInputText by viewModel.urlInputText.collectAsState()

    var showTabsOverview by remember { mutableStateOf(false) }
    var currentUrlDraft by remember { mutableStateOf(urlInputText) }

    LaunchedEffect(urlInputText) {
        currentUrlDraft = urlInputText
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    // Omnibox Address Bar & Navigation Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { viewModel.navigateHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                        }

                        // Omnibox
                        OutlinedTextField(
                            value = currentUrlDraft,
                            onValueChange = { currentUrlDraft = it },
                            placeholder = { Text("Search or type URL", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    if (currentUrlDraft.startsWith("https://")) Icons.Filled.Lock else Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (currentUrlDraft.startsWith("https://")) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (currentUrlDraft.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.loadBrowserUrl(currentUrlDraft) }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Go", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        // Tabs count badge button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { showTabsOverview = !showTabsOverview }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = browserTabs.size.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
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
            if (showTabsOverview) {
                // Tabs Switcher Grid
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Open Tabs (${browserTabs.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Button(onClick = {
                                viewModel.newBrowserTab()
                                showTabsOverview = false
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Tab")
                            }
                        }
                    }

                    items(browserTabs) { tab ->
                        Card(
                            onClick = {
                                viewModel.loadBrowserUrl(tab.url)
                                showTabsOverview = false
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (tab.id == currentTabId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(tab.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(tab.url, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }

                                if (browserTabs.size > 1) {
                                    IconButton(onClick = { viewModel.closeBrowserTab(tab.id) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Close tab")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Simulated Rich Web Page Content
                SimulatedWebContent(
                    url = currentUrlDraft,
                    onNavigate = {
                        viewModel.loadBrowserUrl(it)
                    }
                )
            }
        }
    }
}

@Composable
private fun SimulatedWebContent(
    url: String,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (url.contains("nexus.search") || url.contains("google")) {
            // Nova Search Homepage
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NovaSearch",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Explore trending topics in 2026", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }

            item {
                Text("Trending Headlines", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                WebNewsCard(
                    title = "Google Announces NovaOS Hyper-Kernel with Zero Latency",
                    source = "Tech Horizon • 15m ago",
                    summary = "The new operating system architecture brings seamless AI acceleration, dynamic Material You theming and fluid multitasking to all mobile form factors.",
                    onClick = { onNavigate("https://news.techhorizon.io/ai-os") }
                )
            }

            item {
                WebNewsCard(
                    title = "Quantum Sensors Enable 7-Day Ultra Battery Life in Smartphones",
                    source = "Science & Future • 1h ago",
                    summary = "Researchers demonstrate next-generation battery anodes offering 300% energy density improvement and sub-10 minute hyper charging.",
                    onClick = { onNavigate("https://news.techhorizon.io/battery") }
                )
            }

            item {
                WebNewsCard(
                    title = "SpaceX Mars Starship Crew Lands Safely at Olympus Mons Base",
                    source = "CosmoChronicle • 3h ago",
                    summary = "Historic milestone reached as the international exploration mission begins permanent Martian habitat construction.",
                    onClick = { onNavigate("https://news.techhorizon.io/mars") }
                )
            }
        } else if (url.contains("news.techhorizon")) {
            // Article Detail
            item {
                Text(
                    text = "Tech Horizon AI Report",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Google Announces NovaOS Hyper-Kernel with Zero Latency",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "By Alex Rivera, Senior OS Architect • September 25, 2026",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NovaOS represents a generational leap in mobile operating system design. Featuring dynamic Material Design 3 theming, instantaneous state restoration across background apps, and a built-in terminal hypervisor, users experience desktop-class productivity in a sleek handheld format.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The integrated system services include a high-fidelity SoundWave audio synthesis engine, full PIN and biometric security, and a rich native suite of utility and gaming apps.",
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Loaded Page: $url", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Secure SSL Connection established with 128-bit encryption.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNavigate("https://nexus.search/") }) {
                        Text("Return to Nova Search")
                    }
                }
            }
        }
    }
}

@Composable
private fun WebNewsCard(
    title: String,
    source: String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(source, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(summary, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 2)
        }
    }
}
