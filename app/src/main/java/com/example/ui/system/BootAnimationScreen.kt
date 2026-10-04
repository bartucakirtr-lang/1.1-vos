package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay

@Composable
fun BootAnimationScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    var bootStage by remember { mutableIntStateOf(0) }
    var bootStatusText by remember { mutableStateOf("Sistem başlatılıyor...") }
    val progressAnim = remember { Animatable(0f) }

    // Sequential Boot Steps
    LaunchedEffect(Unit) {
        // Stage 0: Pure black & pulsing Google/Android dots
        delay(400)
        bootStage = 1
        bootStatusText = "vos 3 Çekirdeği Yükleniyor..."

        // Stage 1: Logo scale-up
        delay(800)
        bootStage = 2
        bootStatusText = "Sistem Hizmetleri Başlatılıyor..."
        progressAnim.animateTo(
            targetValue = 0.65f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )

        // Stage 2: Finalizing
        delay(800)
        bootStage = 3
        bootStatusText = "Android Hazır"
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )

        // Fade out & enter Launcher
        delay(500)
        viewModel.completeBoot()
    }

    // Infinite pulse animations for dots & logo glow
    val infiniteTransition = rememberInfiniteTransition(label = "boot_pulse")
    val dotOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_wave"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                // Allow user to tap anywhere to skip boot animation
                viewModel.completeBoot()
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Center Content: Logo & Boot Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Android 4-Colored Dots (Google / Pixel style)
            AnimatedVisibility(
                visible = bootStage < 2,
                enter = fadeIn(),
                exit = fadeOut(tween(300))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    val colors = listOf(
                        Color(0xFF4285F4), // Blue
                        Color(0xFFEA4335), // Red
                        Color(0xFFFBBC05), // Yellow
                        Color(0xFF34A853)  // Green
                    )
                    colors.forEachIndexed { i, color ->
                        val currentY = if (i % 2 == 0) dotOffset else -dotOffset
                        Box(
                            modifier = Modifier
                                .offset(y = currentY.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }

            // Official "3" Boot Logo with Halo
            AnimatedVisibility(
                visible = bootStage >= 1,
                enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.75f, animationSpec = tween(600, easing = FastOutSlowInEasing)),
                exit = fadeOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    // Ambient radial glow behind the logo
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .clip(CircleShape)
                            .alpha(glowAlpha)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4285F4).copy(alpha = 0.35f),
                                        Color(0xFF1E88E5).copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Squircle 3 Logo Surface
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xFF0D0E13),
                        shadowElevation = 18.dp,
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.22f)),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.img_system_update_v3),
                                contentDescription = "vos 3 Boot Logo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(32.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            // OS Name & Branding
            AnimatedVisibility(
                visible = bootStage >= 1,
                enter = fadeIn(tween(400)),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "vos 3",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "Android 16 • Baklava Edition",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9E9E9E),
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Progress bar & boot status text
            AnimatedVisibility(
                visible = bootStage >= 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(220.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Sleek horizontal progress line
                    LinearProgressIndicator(
                        progress = { progressAnim.value },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF4285F4),
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    Text(
                        text = bootStatusText,
                        fontSize = 11.sp,
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom "powered by android" Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "powered by",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                letterSpacing = 1.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Android,
                    contentDescription = "Android",
                    tint = Color(0xFF3DDC84), // Official Android Green
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "android",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
