@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay

data class YouTubeVideo(
    val id: String,
    val title: String,
    val channel: String,
    val views: String,
    val duration: String,
    val date: String,
    val startColor: Long,
    val endColor: Long
)

@Composable
fun YouTubeApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activeVideo by remember { mutableStateOf<YouTubeVideo?>(null) }

    val videos = listOf(
        YouTubeVideo("v1", "MKBHD - Is Android Baklava (v16) Actually Good?", "Marques Brownlee", "1.4M views", "12:15", "2h ago", 0xFFE53935, 0xFFFF7043),
        YouTubeVideo("v2", "Lofi Hip Hop Radio - Relaxing beats to study/code to ☕️", "Lofi Girl", "24K watching", "LIVE", "Continuous", 0xFF6A11CB, 0xFF2575FC),
        YouTubeVideo("v3", "SpaceX Starship Crew Mars Landing Live Stream", "SpaceX", "8.9M views", "4:12:30", "1d ago", 0xFF141926, 0xFF0D101A),
        YouTubeVideo("v4", "Nexus Dev Summit 2026: Build Next-Gen Operating Systems", "Nova Core Devs", "120K views", "45:10", "3d ago", 0xFF00E676, 0xFF00B0FF),
        YouTubeVideo("v5", "10 Android Jetpack Compose Tips Every Senior Engineer Needs", "Compose Master", "430K views", "18:42", "1w ago", 0xFF7C4DFF, 0xFFE1306C)
    )

    val filteredVideos = remember(searchQuery, selectedCategory) {
        videos.filter { video ->
            val matchesSearch = video.title.contains(searchQuery, ignoreCase = true) || video.channel.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedCategory) {
                "LIVE" -> video.duration == "LIVE"
                "Tech" -> video.title.contains("MKBHD") || video.title.contains("Dev Summit") || video.title.contains("Compose")
                "Music" -> video.title.contains("Lofi")
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                        Text("YouTube", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search Trigger */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .background(Color(0xFFE91E63), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (activeVideo != null) {
                YouTubePlayerScreen(video = activeVideo!!, onClose = { activeVideo = null })
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search YouTube...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )

                    // Categories Scrollable Row
                    val categories = listOf("All", "LIVE", "Tech", "Music")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // Videos Feed
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredVideos, key = { it.id }) { video ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { activeVideo = video }
                            ) {
                                // Video Thumbnail
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(video.startColor), Color(video.endColor))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(64.dp)
                                    )

                                    // Duration badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(10.dp)
                                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(video.duration, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Metadata row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Avatar circle
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(video.startColor), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(video.channel.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${video.channel} • ${video.views} • ${video.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun YouTubePlayerScreen(
    video: YouTubeVideo,
    onClose: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.15f) }
    var likes by remember { mutableIntStateOf(1243) }
    var isLiked by remember { mutableStateOf(false) }
    var isDisliked by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    var commentsList by remember {
        mutableStateOf(
            listOf(
                "Dave82" to "This is absolutely incredible! Perfect walkthrough.",
                "Sora_Cloud" to "I love the clean presentation, subbed! 🚀",
                "AndroidDev_99" to "Jetpack Compose is indeed the future."
            )
        )
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (sliderValue < 1f) {
                delay(1000)
                sliderValue += 0.005f
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        // Embedded Player Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(video.startColor), Color(video.endColor))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }

                // Top left back button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            // Real-time progress seekbar
            LinearProgressIndicator(
                progress = { sliderValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(4.dp),
                color = Color.Red,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
        }

        // Stream Details Scroll View
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(video.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${video.views} • ${video.date}", fontSize = 11.sp, color = Color.LightGray)
            }

            // Likes/Dislikes/Share bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            if (!isLiked) {
                                likes++
                                isLiked = true
                                isDisliked = false
                            } else {
                                likes--
                                isLiked = false
                            }
                        },
                        label = { Text("$likes", color = Color.White, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.ThumbUp, null, tint = if (isLiked) Color.Red else Color.White, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
                    )

                    AssistChip(
                        onClick = {
                            isDisliked = !isDisliked
                            if (isDisliked) {
                                if (isLiked) {
                                    likes--
                                    isLiked = false
                                }
                            }
                        },
                        label = { Text("Dislike", color = Color.White, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.ThumbDown, null, tint = if (isDisliked) Color.Red else Color.White, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
                    )

                    AssistChip(
                        onClick = { /* Share dialog */ },
                        label = { Text("Share", color = Color.White, fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(14.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.15f))
                    )
                }
            }

            // Channel segment
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(video.startColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(video.channel.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(video.channel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("2.5M subscribers", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }

                    Button(
                        onClick = { /* Subscribed */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp)
                    ) {
                        Text("Subscribe", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Comments Header
            item {
                Text("Comments (${commentsList.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            }

            // Add Comment Field
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a public comment...", fontSize = 12.sp, color = Color.LightGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.Red,
                            unfocusedBorderColor = Color.Gray,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            if (commentText.isNotEmpty()) {
                                commentsList = listOf("You" to commentText) + commentsList
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Send, "Send", tint = if (commentText.isNotEmpty()) Color.Red else Color.Gray)
                    }
                }
            }

            // Comments Feed List
            items(commentsList) { (user, comment) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).background(Color.Gray, CircleShape), contentAlignment = Alignment.Center) {
                                Text(user.take(1).uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(user, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(comment, color = Color.LightGray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
