@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CustomSideloadApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val appName by viewModel.sideloadedAppName.collectAsState()
    val appUrl by viewModel.sideloadedAppUrl.collectAsState()
    val isInstalled by viewModel.isSideloadInstalled.collectAsState()

    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var gameActive by remember { mutableStateOf(true) }
    
    // Falling alien spaceship positions & emojis
    var targetX by remember { mutableFloatStateOf(0.4f) } // 0.0f to 0.8f
    var targetY by remember { mutableFloatStateOf(0.1f) } // 0.1f to 0.7f
    val alienEmojis = listOf("🛸", "👾", "☄️", "🚀", "🪐")
    var currentAlien by remember { mutableStateOf("🛸") }

    val coroutineScope = rememberCoroutineScope()

    // Game physics simulation
    LaunchedEffect(gameActive) {
        if (gameActive) {
            while (lives > 0) {
                delay(1200)
                // Move alien down
                targetY += 0.15f
                if (targetY > 0.75f) {
                    // Missed! Lose a life
                    lives--
                    targetX = Random.nextFloat() * 0.7f
                    targetY = 0.1f
                    currentAlien = alienEmojis.random()
                    if (lives <= 0) {
                        gameActive = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(appName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = appUrl.take(35) + if (appUrl.length > 35) "..." else "",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.uninstallSideloadedApp()
                            viewModel.navigateHome()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Uninstall App", tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F121A))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Info Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Score: $score",
                        color = Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Life",
                            tint = if (i <= lives) Color.Red else Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Central Game Field (Laser Sandbox)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF141926), Color(0xFF0D101A))
                        )
                    )
            ) {
                if (gameActive && lives > 0) {
                    // Falling space invaders target
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.2f)
                                    .fillMaxHeight(0.15f)
                                    .offset(
                                        x = (targetX * 280).dp,
                                        y = (targetY * 400).dp
                                    )
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable {
                                        // Shot success!
                                        score += 10
                                        targetX = Random.nextFloat() * 0.7f
                                        targetY = 0.1f
                                        currentAlien = alienEmojis.random()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = currentAlien,
                                        fontSize = 32.sp
                                    )
                                    Text(
                                        text = "TAP!",
                                        fontSize = 9.sp,
                                        color = Color.Green,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Space Defense Gun at the bottom
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Cyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Rocket,
                                contentDescription = "Sideload Ship",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    // Game Over Screen
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SportsEsports,
                            contentDescription = "Game Over",
                            tint = Color.Red,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "GAME OVER",
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sideloaded package executed successfully.\nFinal Score: $score",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                score = 0
                                lives = 3
                                targetX = 0.4f
                                targetY = 0.1f
                                gameActive = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                        ) {
                            Text("Restart App Engine", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sideload Package Verification Panel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.05f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text("Verified Sideload Bundle", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text(
                            text = "Sandboxed inside secure VM. No permissions requested.",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
