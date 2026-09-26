@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class InstagramPost(
    val id: String,
    val username: String,
    val avatarColor: Long,
    val location: String,
    val caption: String,
    val imageGradientStart: Long,
    val imageGradientEnd: Long,
    var initialLikes: Int,
    var initialLiked: Boolean = false
)

@Composable
fun InstagramApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val posts = remember {
        mutableStateListOf(
            InstagramPost("p1", "tech_horizon", 0xFF6A11CB, "Silicon Valley, CA", "Just compiled the legendary NovaOS update v1.1! The remote version check is fully sync'd on GitHub. Absolutely flawless! 📱💻🔥", 0xFF8E24AA, 0xFFE1306C, 1284),
            InstagramPost("p2", "alex_rivera", 0xFF00E676, "San Francisco, CA", "Enjoying double shots of cold brew espresso while debugging Gradle tasks. Best weekend vibe! ☕️🌲✨", 0xFF00ACC1, 0xFF2E7D32, 420),
            InstagramPost("p3", "space_explorer", 0xFFFFD54F, "Olympus Mons, Mars", "Sideloading Retro Space Invaders package while watching Martians construct our second habitat dome! 🚀🪐🛰️", 0xFFE65100, 0xFF311B92, 8931),
            InstagramPost("p4", "lofi_coder", 0xFFE1306C, "Matcha Lounge", "Streaming lo-fi chill beats directly from the SoundWave applet. Ready to build something epic today. 🍵🎹🎧", 0xFF1E88E5, 0xFF7C4DFF, 725)
        )
    }

    val stories = listOf(
        Pair("Your Story", 0xFFE1306C),
        Pair("alex_rivera", 0xFF00E676),
        Pair("space_x", 0xFF141926),
        Pair("lofi_girl", 0xFF6A11CB),
        Pair("m_k_b_h_d", 0xFFFF0000)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Instagram",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Inbox dialog */ }) {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { /* Inbox */ }) {
                        Icon(Icons.Default.Send, contentDescription = "Direct Messenger")
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
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal Stories Segment
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(stories) { (name, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF3000), Color(0xFFC72D8E), Color(0xFF962D11))
                                        )
                                    )
                                    .padding(2.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(color)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            Text(
                                text = name,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Timeline feed
            items(posts) { post ->
                var isLiked by remember { mutableStateOf(post.initialLiked) }
                var likesCount by remember { mutableIntStateOf(post.initialLikes) }
                var doubleTapScale by remember { mutableFloatStateOf(0f) }

                val coroutineScope = rememberCoroutineScope()

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Profile Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(post.avatarColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    post.username.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Column {
                                Text(post.username, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(post.location, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(onClick = { /* Menu options */ }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                    }

                    // Main Image Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(post.imageGradientStart), Color(post.imageGradientEnd))
                                )
                            )
                            .clickable {
                                // Simulate Double Tap to Like!
                                if (!isLiked) {
                                    isLiked = true
                                    likesCount++
                                    post.initialLiked = true
                                    post.initialLikes = likesCount
                                }
                                coroutineScope.launch {
                                    doubleTapScale = 1.3f
                                    delay(400)
                                    doubleTapScale = 0f
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Double Tap Pop Heart Animation Overlay
                        androidx.compose.animation.AnimatedVisibility(
                            visible = doubleTapScale > 0f,
                            enter = scaleIn(initialScale = 0.3f) + fadeIn(),
                            exit = scaleOut(targetScale = 1.5f) + fadeOut()
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(80.dp)
                            )
                        }

                        Text(
                            text = "📸 Photo Highlight",
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isLiked = !isLiked
                                    if (isLiked) likesCount++ else likesCount--
                                    post.initialLiked = isLiked
                                    post.initialLikes = likesCount
                                }
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Like",
                                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = { /* Comment */ }) {
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
                            }

                            IconButton(onClick = { /* Share */ }) {
                                Icon(Icons.Outlined.Send, contentDescription = "Share")
                            }
                        }

                        IconButton(onClick = { /* Save Bookmark */ }) {
                            Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Save")
                        }
                    }

                    // Feed Details / Caption
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$likesCount likes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(post.username, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(post.caption, fontSize = 12.sp, maxLines = 3)
                        }

                        Text(
                            text = "View all comments...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
