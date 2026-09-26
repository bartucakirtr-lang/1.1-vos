package com.example.ui.system

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.OSNotification
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SystemStatusBar(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    isOverlayLight: Boolean = false
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val wifiEnabled by viewModel.wifiEnabled.collectAsState()
    val bluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val dndEnabled by viewModel.dndEnabled.collectAsState()
    val flashlightOn by viewModel.flashlightOn.collectAsState()
    val airplaneMode by viewModel.airplaneMode.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    val timeFormatted = remember(systemTime) {
        SimpleDateFormat("h:mm", Locale.getDefault()).format(Date(systemTime))
    }

    val contentColor = if (isOverlayLight) Color.Black else Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 4.dp)
            .height(28.dp)
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 10) {
                        viewModel.expandShade()
                    }
                }
            }
            .clickable { viewModel.toggleShade() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Clock & App notification icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = timeFormatted,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (dndEnabled) {
                    Icon(
                        Icons.Filled.DoNotDisturbOn,
                        contentDescription = "DND",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Show top 2 active notification icons
                notifications.take(2).forEach { notif ->
                    val icon = when (notif.appId) {
                        AppId.MESSAGES -> Icons.Outlined.Chat
                        AppId.PHONE -> Icons.Outlined.Phone
                        AppId.PHOTOS -> Icons.Outlined.Image
                        AppId.CLOCK -> Icons.Outlined.Alarm
                        else -> Icons.Outlined.Notifications
                    }
                    Icon(
                        icon,
                        contentDescription = notif.title,
                        tint = contentColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // Right: Status Icons (Flashlight, BT, WiFi, Signal, Battery)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (flashlightOn) {
                    Icon(
                        Icons.Filled.FlashlightOn,
                        contentDescription = "Torch",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (bluetoothEnabled) {
                    Icon(
                        Icons.Filled.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                }

                if (airplaneMode) {
                    Icon(
                        Icons.Filled.AirplanemodeActive,
                        contentDescription = "Airplane",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    if (wifiEnabled) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = "WiFi",
                            tint = contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Icon(
                        Icons.Filled.SignalCellular4Bar,
                        contentDescription = "5G",
                        tint = contentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "5G",
                        color = contentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Battery Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(contentColor.copy(alpha = 0.18f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    if (isCharging) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = "Charging",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = "$batteryLevel%",
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
