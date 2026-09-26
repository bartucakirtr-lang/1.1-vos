package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.ClockStyle
import com.example.model.LockType
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LockScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val screenLockType by viewModel.screenLockType.collectAsState()
    val clockStyle by viewModel.clockStyle.collectAsState()
    val inputPin by viewModel.inputPin.collectAsState()
    val pinError by viewModel.pinError.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val musicTracks by viewModel.musicTracks.collectAsState()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsState()
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val remoteConfigVersion by viewModel.remoteConfigVersion.collectAsState()

    var showPinPad by remember { mutableStateOf(false) }

    val hourString = remember(systemTime) { SimpleDateFormat("hh", Locale.getDefault()).format(Date(systemTime)) }
    val minuteString = remember(systemTime) { SimpleDateFormat("mm", Locale.getDefault()).format(Date(systemTime)) }
    val dateString = remember(systemTime) { SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(systemTime)) }

    // Pulsing biometric animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        if (screenLockType == LockType.PIN) {
                            showPinPad = true
                        } else {
                            viewModel.unlockPhone()
                        }
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Date, Weather Chip & Clock
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Date & vos Security chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Outlined.Security,
                        contentDescription = "vos Protected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$dateString • vos Protected",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Clock Typography according to Style
                when (clockStyle) {
                    ClockStyle.PIXEL_BOLD -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hourString,
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 78.sp
                            )
                            Text(
                                text = minuteString,
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 78.sp
                            )
                        }
                    }
                    ClockStyle.MINIMAL -> {
                        Text(
                            text = "$hourString:$minuteString",
                            fontSize = 68.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    }
                    ClockStyle.DUAL_COLOR -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$hourString:",
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = minuteString,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    ClockStyle.ANALOG -> {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$hourString:$minuteString",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Middle: Lockscreen Media Player or Notifications List or PIN pad
            if (showPinPad) {
                // PIN Entry View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = if (pinError) "Wrong PIN code" else "Enter PIN",
                        color = if (pinError) Color(0xFFFF5252) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // PIN Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val filled = i < inputPin.length
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (pinError) Color(0xFFFF5252)
                                        else if (filled) MaterialTheme.colorScheme.primary
                                        else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Keypad Grid
                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("Cancel", "0", "⌫")
                    )

                    for (row in keys) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            modifier = Modifier.padding(vertical = 5.dp)
                        ) {
                            for (key in row) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (key == "Cancel" || key == "⌫") Color.Transparent else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clickable {
                                            when (key) {
                                                "Cancel" -> showPinPad = false
                                                "⌫" -> viewModel.clearPinDigit()
                                                else -> viewModel.inputPinDigit(key)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = key,
                                            fontSize = if (key.length > 1) 13.sp else 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Notifications & Media Player Stack
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Active Music Player Card on Lockscreen
                    if (isPlayingMusic) {
                        val track = musicTracks[currentTrackIndex]
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(20.dp),
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
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(track.coverGradientStart),
                                                        Color(track.coverGradientEnd)
                                                    )
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = track.title,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = track.artist,
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.prevTrack() }) {
                                        Icon(Icons.Filled.SkipPrevious, "Prev", tint = Color.White)
                                    }
                                    IconButton(
                                        onClick = { viewModel.togglePlayMusic() },
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            if (isPlayingMusic) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            "Play/Pause",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                    IconButton(onClick = { viewModel.nextTrack() }) {
                                        Icon(Icons.Filled.SkipNext, "Next", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // Notification Cards
                    notifications.take(2).forEach { notif ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.unlockPhone()
                                    viewModel.openApp(notif.appId)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            when (notif.appId) {
                                                AppId.MESSAGES -> Icons.Outlined.Chat
                                                AppId.PHONE -> Icons.Outlined.Phone
                                                AppId.PHOTOS -> Icons.Outlined.Image
                                                else -> Icons.Outlined.Notifications
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notif.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = notif.message,
                                        color = Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom: Biometric / Swipe indicator & Corner Shortcuts
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!showPinPad) {
                    // Biometric Sensor Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(68.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable {
                                viewModel.playTone(950f, 100)
                                viewModel.unlockPhone()
                            }
                    ) {
                        Icon(
                            Icons.Filled.Fingerprint,
                            contentDescription = "Fingerprint Unlock",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Touch sensor or swipe up to unlock",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Left & Right Quick Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flashlight Shortcut
                    Surface(
                        shape = CircleShape,
                        color = if (flashlightOn) Color(0xFFFFD54F) else Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { viewModel.toggleFlashlight() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.FlashlightOn,
                                contentDescription = "Torch",
                                tint = if (flashlightOn) Color.Black else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Emergency / PIN pad shortcut
                    TextButton(onClick = { showPinPad = !showPinPad }) {
                        Text(
                            text = if (showPinPad) "Cancel" else "PIN Lock",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }

                    // Camera Shortcut
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                viewModel.unlockPhone()
                                viewModel.openApp(AppId.CAMERA)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.CameraAlt,
                                contentDescription = "Camera",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
