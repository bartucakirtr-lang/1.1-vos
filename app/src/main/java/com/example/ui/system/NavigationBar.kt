package com.example.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.NavMode
import com.example.viewmodel.OSViewModel

@Composable
fun SystemNavigationBar(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    isOverlayLight: Boolean = false
) {
    val navMode by viewModel.navigationMode.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()

    if (isLocked) return

    val iconColor = if (isOverlayLight) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        if (navMode == NavMode.GESTURE) {
            // Android Gesture Navigation Pill
            var totalDragY by remember { mutableFloatStateOf(0f) }
            var totalDragX by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(iconColor)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (totalDragY < -60) {
                                    if (totalDragY < -180) {
                                        viewModel.openRecents()
                                    } else {
                                        viewModel.navigateHome()
                                    }
                                } else if (totalDragX > 80) {
                                    viewModel.prevTrack()
                                } else if (totalDragX < -80) {
                                    viewModel.nextTrack()
                                }
                                totalDragY = 0f
                                totalDragX = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDragY += dragAmount.y
                                totalDragX += dragAmount.x
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { viewModel.navigateHome() },
                            onLongPress = { viewModel.openRecents() }
                        )
                    }
            )
        } else {
            // Android 3-Button Navigation Bar (Back, Home, Recents)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (Triangle)
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Home Button (Circle)
                IconButton(
                    onClick = { viewModel.navigateHome() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(iconColor)
                    )
                }

                // Recents Overview Button (Square)
                IconButton(
                    onClick = { viewModel.openRecents() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(iconColor)
                    )
                }
            }
        }
    }
}
