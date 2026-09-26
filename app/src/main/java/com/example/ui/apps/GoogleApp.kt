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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel

data class GoogleResult(
    val title: String,
    val url: String,
    val description: String
)

@Composable
fun GoogleApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchExecuted by remember { mutableStateOf(false) }

    val recentSearches = listOf(
        "GitHub bartucakirtr-lang/1.1-vos",
        "Jetpack Compose Canvas charts",
        "Android Baklava API 16 release",
        "Sideload custom applications secure Android"
    )

    val mockResults = listOf(
        GoogleResult("GitHub - bartucakirtr-lang/1.1-vos", "https://github.com/bartucakirtr-lang/1.1-vos", "Version 1.1 config.json storage release for vos. Automatically monitors local state variables and triggers remote updates from custom secure servers."),
        GoogleResult("Android Baklava (API Level 16) - Developer Documentation", "https://developer.android.com/about/versions/baklava", "Explore the newly designed hyper-kernel with zero-latency thread pools, advanced edge-to-edge layouts, and dynamic Material You color matching in Android 16."),
        GoogleResult("Sideloading Apps Securely on modern mobile OS - Tech Horizon AI", "https://news.techhorizon.io/sideloading-guide", "Expert guide on how sandboxed package virtual machines run user sideloaded binaries from raw URLs safely with no broader runtime permissions required.")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (searchExecuted) "Google Search" else "Google Search Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (searchExecuted) {
                                searchExecuted = false
                                searchQuery = ""
                            } else {
                                viewModel.navigateHome()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (!searchExecuted) {
                // Large Google Multi-colored Logo Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color(0xFF4285F4))) { append("G") }
                                withStyle(style = SpanStyle(color = Color(0xFFEA4335))) { append("o") }
                                withStyle(style = SpanStyle(color = Color(0xFFFBBC05))) { append("o") }
                                withStyle(style = SpanStyle(color = Color(0xFF4285F4))) { append("g") }
                                withStyle(style = SpanStyle(color = Color(0xFF34A853))) { append("l") }
                                withStyle(style = SpanStyle(color = Color(0xFFEA4335))) { append("e") }
                            },
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Search the secure global web", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Omnibox Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search or type query...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF4285F4)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchExecuted = true }) {
                                    Icon(Icons.Default.ArrowForward, "Go", tint = Color(0xFF34A853))
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                }

                // Recent searches
                item {
                    Text("RECENT SEARCHES", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }

                items(recentSearches) { query ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchQuery = query
                                searchExecuted = true
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text(query, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Discovery Cards Row
                item {
                    Text("TRENDING TOPICS", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 8.dp))
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Weather Widget", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4), fontSize = 13.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("San Francisco • Sunny 72°F", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.WbSunny, null, tint = Color(0xFFFBBC05), modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            } else {
                // Search Results Active
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                searchQuery = ""
                                searchExecuted = false
                            }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    )
                }

                item {
                    Text("About 3 high-performance results for \"$searchQuery\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(mockResults) { res ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable { /* Navigate or open browser */ }
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(res.url, color = Color(0xFF34A853), fontSize = 10.sp, maxLines = 1)
                            Text(res.title, fontWeight = FontWeight.Bold, color = Color(0xFF1A0DAB), fontSize = 14.sp)
                            Text(res.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
