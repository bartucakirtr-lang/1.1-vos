package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel

@Composable
fun RecentsOverview(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkMode.current
    val runningApps by viewModel.runningApps.collectAsState()

    val overviewBg = if (isDark) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f)
    val headerTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overviewBg)
            .clickable { viewModel.closeRecents() }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Apps",
                    color = headerTextColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                if (runningApps.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllRecents() }) {
                        Text(
                            text = "Clear all",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Cards Carousel
            if (runningApps.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.LayersClear,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent tasks",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(runningApps, key = { it.packageName }) { appId ->
                        var isDismissed by remember { mutableStateOf(false) }

                        AnimatedVisibility(
                            visible = !isDismissed,
                            exit = slideOutVertically { -it } + fadeOut()
                        ) {
                            RecentAppCard(
                                appId = appId,
                                onOpen = { viewModel.openApp(appId) },
                                onClose = {
                                    isDismissed = true
                                    viewModel.killRecentApp(appId)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Quick Actions: Screenshot & Split Screen
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = {
                        viewModel.capturePhoto()
                        viewModel.closeRecents()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(Icons.Outlined.Screenshot, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Screenshot", color = Color.White, fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = { viewModel.navigateHome() },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    Icon(Icons.Outlined.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Home", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun RecentAppCard(
    appId: AppId,
    onOpen: () -> Unit,
    onClose: () -> Unit
) {
    val isDark = LocalIsDarkMode.current
    var offsetY by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(240.dp)
            .height(360.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -100) {
                            onClose()
                        }
                        offsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount < 0) {
                            offsetY += dragAmount
                        }
                    }
                )
            }
    ) {
        // App Header (Icon + Title)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getAppIcon(appId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = appId.title,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // App Preview Snapshot Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E2430) else MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY.dp)
                .clickable { onOpen() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        getAppIcon(appId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = appId.title,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Running in background",
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Swipe up to close",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

fun getAppIcon(appId: AppId): androidx.compose.ui.graphics.vector.ImageVector {
    return when (appId) {
        AppId.PHONE -> Icons.Filled.Phone
        AppId.MESSAGES -> Icons.Filled.Chat
        AppId.SETTINGS -> Icons.Filled.Settings
        AppId.BROWSER -> Icons.Filled.Public
        AppId.CAMERA -> Icons.Filled.CameraAlt
        AppId.PHOTOS -> Icons.Filled.PhotoLibrary
        AppId.MUSIC -> Icons.Filled.MusicNote
        AppId.CLOCK -> Icons.Filled.AccessTime
        AppId.NOTES -> Icons.Filled.EditNote
        AppId.CALCULATOR -> Icons.Filled.Calculate
        AppId.FILES -> Icons.Filled.Folder
        AppId.STORE -> Icons.Filled.ShoppingBag
        AppId.ARCADE -> Icons.Filled.SportsEsports
        AppId.TERMINAL -> Icons.Filled.Terminal
        AppId.WEATHER -> Icons.Filled.Cloud
        AppId.TASKS -> Icons.Filled.CheckCircle
        AppId.ACCOUNT -> Icons.Filled.AccountCircle
        AppId.CUSTOM_APP -> Icons.Filled.CloudDownload
    }
}
