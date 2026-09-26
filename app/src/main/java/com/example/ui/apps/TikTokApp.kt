@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class TikTokVideo(
    val id: String,
    val creator: String,
    val description: String,
    val music: String,
    val likes: String,
    val comments: String,
    val shares: String,
    val gradientStart: Long,
    val gradientEnd: Long
)

@Composable
fun TikTokApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentVideoIndex by remember { mutableIntStateOf(0) }
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Following, 1: For You
    var isLiked by remember { mutableStateOf(false) }
    var isFollowed by remember { mutableStateOf(false) }
    var doubleTapHeart by remember { mutableStateOf(false) }

    val videos = listOf(
        TikTokVideo("t1", "tech_innovator", "Testing real-world apps on NovaOS 1.1! The animation response time is insane 🤯✨ #novaos #android #coding #viral", "NovaOS Beats - Hyper-Kernel VIP", "1.4M", "28.4K", "89.2K", 0xFF0D47A1, 0xFF00E5FF),
        TikTokVideo("t2", "alex_dev", "When the Gradle build succeeds on the first try after 2,000 lines of Compose code 💃🕺 #relatable #developer #tech", "Original Sound - Victory Anthem", "842K", "12.1K", "45.0K", 0xFF880E4F, 0xFFFF4081),
        TikTokVideo("t3", "lofi_vibes", "Relaxing midnight coding session in San Francisco ☕️🌃 Tell me your favorite IDE below! #lofi #aesthetic", "Chill Lo-Fi Rain - Midnight Studio", "2.1M", "49.0K", "130K", 0xFF311B92, 0xFF651FFF)
    )

    val currentVideo = videos[currentVideoIndex]

    // Spinning vinyl animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                // Double tap like effect
                isLiked = true
                coroutineScope.launch {
                    doubleTapHeart = true
                    delay(500)
                    doubleTapHeart = false
                }
            }
    ) {
        // Full screen video background visualizer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(currentVideo.gradientStart),
                            Color(currentVideo.gradientEnd).copy(alpha = 0.8f),
                            Color.Black
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(90.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("🎵 ${currentVideo.music}", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }

            // Big Double-Tap Pop Heart Animation
            androidx.compose.animation.AnimatedVisibility(
                visible = doubleTapHeart,
                enter = scaleIn(initialScale = 0.3f) + fadeIn(),
                exit = scaleOut(targetScale = 1.6f) + fadeOut()
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFF0050),
                    modifier = Modifier.size(110.dp)
                )
            }
        }

        // Top Navigation Header: Back + Following | For You + Live
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.navigateHome() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.White)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Following",
                    color = if (selectedTab == 0) Color.White else Color.White.copy(alpha = 0.6f),
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { selectedTab = 0 }
                )
                Text(
                    text = "For You",
                    color = if (selectedTab == 1) Color.White else Color.White.copy(alpha = 0.6f),
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { selectedTab = 1 }
                )
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        }

        // Right-Side Interaction Floating Column
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar with Follow Plus
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(currentVideo.gradientStart)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentVideo.creator.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (!isFollowed) {
                    Box(
                        modifier = Modifier
                            .offset(y = 8.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0050))
                            .clickable { isFollowed = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Like Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { isLiked = !isLiked }) {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFFF0050) else Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(currentVideo.likes, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Comment Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Comments", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text(currentVideo.comments, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Bookmark Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("115K", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text(currentVideo.shares, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            // Next Video Floating Arrow
            IconButton(
                onClick = {
                    currentVideoIndex = (currentVideoIndex + 1) % videos.size
                    isLiked = false
                }
            ) {
                Icon(Icons.Default.KeyboardArrowDown, "Next Video", tint = Color.White, modifier = Modifier.size(36.dp))
            }

            // Spinning Vinyl Record
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF212121))
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(Color(currentVideo.gradientStart)))
            }
        }

        // Bottom Left Creator Info & Caption
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 24.dp, end = 80.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("@${currentVideo.creator}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                Icon(Icons.Default.CheckCircle, "Verified", tint = Color(0xFF20D5EC), modifier = Modifier.size(16.dp))
            }

            Text(
                text = currentVideo.description,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(currentVideo.music, color = Color.White, fontSize = 11.sp)
            }
        }
    }
}
