@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.viewmodel.OSViewModel

data class NetflixMedia(
    val title: String,
    val category: String,
    val match: String,
    val year: String,
    val seasons: String,
    val synopsis: String,
    val colorStart: Long,
    val colorEnd: Long
)

@Composable
fun NetflixApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val netflixRed = Color(0xFFE50914)
    val netflixBlack = Color(0xFF141414)

    var selectedMedia by remember { mutableStateOf<NetflixMedia?>(null) }
    var inMyList by remember { mutableStateOf(false) }

    val continueWatching = listOf(
        NetflixMedia("Stranger Things", "Sci-Fi • Horror", "98% Match", "2026", "5 Seasons", "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.", 0xFFB71C1C, 0xFF0D47A1),
        NetflixMedia("Wednesday", "Mystery • Comedy", "96% Match", "2025", "2 Seasons", "Smart, sarcastic and a little dead inside, Wednesday Addams investigates a murder spree while making new friends — and foes — at Nevermore Academy.", 0xFF311B92, 0xFF212121),
        NetflixMedia("Squid Game", "Thriller • Drama", "99% Match", "2025", "2 Seasons", "Hundreds of cash-strapped players accept a strange invitation to compete in children's games. Inside, a tempting prize awaits with deadly high stakes.", 0xFF880E4F, 0xFF004D40)
    )

    val trendingNow = listOf(
        NetflixMedia("Cyberpunk: Edgerunners", "Anime • Sci-Fi", "97% Match", "2024", "1 Season", "A street kid trying to survive in a technology and body modification-obsessed city of the future.", 0xFFFFEB3B, 0xFF00E5FF),
        NetflixMedia("Black Mirror", "Sci-Fi • Dystopian", "95% Match", "2025", "6 Seasons", "This sci-fi anthology series explores a twisted, high-tech near-future where humanity's greatest innovations and darkest instincts collide.", 0xFF37474F, 0xFF263238),
        NetflixMedia("One Piece", "Adventure • Action", "94% Match", "2024", "1 Season", "With his straw hat and ragtag crew, young pirate Monkey D. Luffy goes on an epic voyage for treasure.", 0xFFFF9800, 0xFF1E88E5),
        NetflixMedia("The Queen's Gambit", "Drama", "98% Match", "2023", "Limited Series", "In a 1950s orphanage, a young girl reveals an astonishing talent for chess and begins an unlikely journey to stardom while grappling with addiction.", 0xFF5D4037, 0xFFBF360C)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Netflix N Logo
                            Text("N", fontWeight = FontWeight.Black, fontSize = 28.sp, color = netflixRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TV Shows", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Movies", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { }) { Icon(Icons.Default.Search, null, tint = Color.White) }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFFE50914), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = netflixBlack)
            )
        },
        containerColor = netflixBlack,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Massive Featured Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFB71C1C), Color(0xFF1F0808), netflixBlack)
                            )
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(netflixRed, RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("TOP 10", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("#1 in TV Shows Today", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("STRANGER THINGS 5", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color.White)
                        Text("Sci-Fi • Horror • 80s Nostalgia • Drama", color = Color.LightGray, fontSize = 11.sp)

                        // Action Buttons: Play + My List + Info
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { inMyList = !inMyList }) {
                                Icon(if (inMyList) Icons.Default.Check else Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text(if (inMyList) "In List" else "My List", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { selectedMedia = continueWatching.first() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Play", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { selectedMedia = continueWatching.first() }) {
                                Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text("Info", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            // Continue Watching Row
            item {
                Text("Continue Watching for Alex", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 14.dp))
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(continueWatching) { media ->
                        Column(
                            modifier = Modifier
                                .width(120.dp)
                                .clickable { selectedMedia = media }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(media.colorStart), Color(media.colorEnd))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            // Red Progress Bar
                            LinearProgressIndicator(
                                progress = { 0.65f },
                                modifier = Modifier.fillMaxWidth().height(3.dp),
                                color = netflixRed,
                                trackColor = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(media.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                }
            }

            // Top 10 Today Row with Giant Numbers
            item {
                Text("Top 10 TV Shows in Your Country Today", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 14.dp))
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(trendingNow) { index, media ->
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.clickable { selectedMedia = media }
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.DarkGray
                            )
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(media.colorStart), Color(media.colorEnd))
                                        )
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(media.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Trending Now Row
            item {
                Text("Trending Now", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 14.dp))
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(trendingNow.reversed()) { media ->
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(170.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(media.colorStart), Color(media.colorEnd))
                                    )
                                )
                                .clickable { selectedMedia = media }
                                .padding(8.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(media.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Interactive Media Details Modal
    if (selectedMedia != null) {
        val media = selectedMedia!!
        AlertDialog(
            onDismissRequest = { selectedMedia = null },
            confirmButton = {
                Button(
                    onClick = { selectedMedia = null },
                    colors = ButtonDefaults.buttonColors(containerColor = netflixRed),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Episode 1")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMedia = null }) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF202020),
            title = {
                Text(media.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(media.match, color = Color(0xFF46D369), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(media.year, color = Color.LightGray, fontSize = 12.sp)
                        Box(
                            modifier = Modifier
                                .background(Color.DarkGray, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("16+", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(media.seasons, color = Color.LightGray, fontSize = 12.sp)
                    }

                    Text(media.synopsis, color = Color.LightGray, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Genres: ${media.category}", color = Color.Gray, fontSize = 11.sp)
                }
            }
        )
    }
}
