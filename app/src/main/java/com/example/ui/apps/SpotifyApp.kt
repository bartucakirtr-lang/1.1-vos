@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay

data class SpotifyTrack(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val gradientStart: Long,
    val gradientEnd: Long
)

@Composable
fun SpotifyApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val spotifyGreen = Color(0xFF1DB954)
    val spotifyDark = Color(0xFF121212)
    val spotifyCard = Color(0xFF242424)

    var isPlaying by remember { mutableStateOf(true) }
    var currentTrackIndex by remember { mutableIntStateOf(0) }
    var isLiked by remember { mutableStateOf(true) }
    var isFullPlayerOpen by remember { mutableStateOf(false) }
    var progressSeconds by remember { mutableIntStateOf(45) }

    val playlist = listOf(
        SpotifyTrack("Blinding Lights", "The Weeknd", "After Hours", "3:20", 0xFFE91E63, 0xFF3F51B5),
        SpotifyTrack("Anti-Hero", "Taylor Swift", "Midnights", "3:21", 0xFF673AB7, 0xFF009688),
        SpotifyTrack("Starboy", "The Weeknd ft. Daft Punk", "Starboy", "3:50", 0xFFFF5722, 0xFF795548),
        SpotifyTrack("Viva La Vida", "Coldplay", "Viva la Vida", "4:02", 0xFF4CAF50, 0xFF03A9F4),
        SpotifyTrack("Sunflower", "Post Malone & Swae Lee", "Spider-Man", "2:38", 0xFFFFEB3B, 0xFFFF9800)
    )

    val currentTrack = playlist[currentTrackIndex]

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            progressSeconds = (progressSeconds + 1) % 200
        }
    }

    val quickPicks = listOf(
        "Today's Top Hits" to 0xFF1DB954,
        "Discover Weekly" to 0xFF7C4DFF,
        "Lofi Chill Beats" to 0xFFE91E63,
        "Rock Classics" to 0xFFFF5722,
        "Daily Mix 1" to 0xFF00ACC1,
        "Deep Focus" to 0xFF3F51B5
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Good evening", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Outlined.Notifications, null, tint = Color.White) }
                    IconButton(onClick = { }) { Icon(Icons.Outlined.History, null, tint = Color.White) }
                    IconButton(onClick = { }) { Icon(Icons.Outlined.Settings, null, tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = spotifyDark)
            )
        },
        bottomBar = {
            // Persistent Mini Music Player Bar
            Surface(
                color = Color(0xFF282828),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isFullPlayerOpen = true }
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(currentTrack.gradientStart), Color(currentTrack.gradientEnd))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MusicNote, null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(currentTrack.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                Text(currentTrack.artist, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isLiked = !isLiked }) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) spotifyGreen else Color.White
                                )
                            }
                            IconButton(onClick = { isPlaying = !isPlaying }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progressSeconds / 200f },
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        },
        containerColor = spotifyDark,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Quick 6 grid
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPicks) { (name, color) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = spotifyCard),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    isPlaying = true
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(color)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.GraphicEq, null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 2)
                            }
                        }
                    }
                }
            }

            // Popular Artists row
            item {
                Text("Popular Artists", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }

            item {
                val artists = listOf("The Weeknd", "Taylor Swift", "Drake", "Coldplay", "Billie Eilish")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(artists) { artist ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF333333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(artist.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                            Text(artist, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Artist", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Made For You playlists
            item {
                Text("Made For You", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }

            item {
                val madeForYou = listOf(
                    Triple("Daily Mix 1", "The Weeknd, Daft Punk, Post Malone", 0xFFE91E63),
                    Triple("Release Radar", "Catch all the latest music from artists you follow", 0xFF1DB954),
                    Triple("Discover Weekly", "Your weekly mixtape of fresh music", 0xFF3F51B5),
                    Triple("Chill Tracks", "Relaxed beats to unwind and code", 0xFF009688)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(madeForYou) { (title, subtitle, color) ->
                        Column(
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                                    isPlaying = true
                                },
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(color)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayCircle, null, tint = Color.White, modifier = Modifier.size(44.dp))
                            }
                            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                            Text(subtitle, color = Color.Gray, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }

    // Expandable Full-Screen Spotify Player Modal
    if (isFullPlayerOpen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(currentTrack.gradientStart).copy(alpha = 0.8f), spotifyDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top control row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isFullPlayerOpen = false }) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Text("PLAYING FROM PLAYLIST", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, null, tint = Color.White)
                    }
                }

                // Big Album Art
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(currentTrack.gradientStart), Color(currentTrack.gradientEnd))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(90.dp))
                }

                // Title + Artist + Like
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(currentTrack.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                        Text(currentTrack.artist, color = Color.LightGray, fontSize = 14.sp)
                    }
                    IconButton(onClick = { isLiked = !isLiked }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) spotifyGreen else Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Scrubber
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progressSeconds / 200f,
                        onValueChange = { progressSeconds = (it * 200).toInt() },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val min = progressSeconds / 60
                        val sec = progressSeconds % 60
                        Text("$min:${if (sec < 10) "0$sec" else "$sec"}", color = Color.Gray, fontSize = 11.sp)
                        Text(currentTrack.duration, color = Color.Gray, fontSize = 11.sp)
                    }
                }

                // Playback Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) { Icon(Icons.Default.Shuffle, null, tint = Color.Gray) }
                    IconButton(
                        onClick = {
                            currentTrackIndex = if (currentTrackIndex > 0) currentTrackIndex - 1 else playlist.size - 1
                        }
                    ) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { isPlaying = !isPlaying },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.Black,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            currentTrackIndex = (currentTrackIndex + 1) % playlist.size
                        }
                    ) {
                        Icon(Icons.Default.SkipNext, null, tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                    IconButton(onClick = { }) { Icon(Icons.Default.Repeat, null, tint = Color.Gray) }
                }

                // Lyrics Preview Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Lyrics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("I'm running on hyper-kernel beats tonight...", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("All real-world apps are in full sync.", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
