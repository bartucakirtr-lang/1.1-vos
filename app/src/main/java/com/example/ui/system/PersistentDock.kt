package com.example.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.ui.theme.IconStyleHelper
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel

@Composable
fun PersistentDock(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    isInDrawer: Boolean = false
) {
    val isDark = LocalIsDarkMode.current
    val dockApps by viewModel.dockApps.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val iconStyle by viewModel.iconStyle.collectAsState()
    val customIcons by viewModel.customAppIcons.collectAsState()

    // Frosted Glass Dock Container
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF141A24).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.85f),
        shadowElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.forEach { appId ->
                val hasUnread = notifications.any { it.appId == appId }
                val customIcon = customIcons[appId.name]
                PersistentDockItem(
                    appId = appId,
                    icon = getDockIcon(appId),
                    baseColor = getAppColor(appId),
                    hasUnread = hasUnread,
                    customIconUrl = customIcon,
                    iconStyle = iconStyle,
                    onClick = {
                        if (isInDrawer) {
                            viewModel.closeAppDrawer()
                        }
                        viewModel.openApp(appId)
                    }
                )
            }

            // Quick App Drawer toggle or Close Drawer button
            Surface(
                shape = IconStyleHelper.getShapeForStyle(iconStyle),
                color = if (isInDrawer) {
                    MaterialTheme.colorScheme.primary
                } else {
                    if (isDark) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant
                },
                shadowElevation = IconStyleHelper.getElevationForStyle(iconStyle),
                border = IconStyleHelper.getBorderForStyle(iconStyle, Color.White),
                modifier = Modifier
                    .size(50.dp)
                    .clickable {
                        viewModel.toggleAppDrawer()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isInDrawer) Icons.Filled.KeyboardArrowDown else Icons.Filled.Apps,
                        contentDescription = if (isInDrawer) "Close App Drawer" else "Open App Drawer",
                        tint = if (isInDrawer) MaterialTheme.colorScheme.onPrimary else if (isDark) Color.White else Color(0xFF191C1E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PersistentDockItem(
    appId: AppId,
    icon: ImageVector,
    baseColor: Color,
    hasUnread: Boolean,
    customIconUrl: String?,
    iconStyle: com.example.model.IconStyle,
    onClick: () -> Unit
) {
    val shape = remember(iconStyle) { IconStyleHelper.getShapeForStyle(iconStyle) }
    val border = remember(iconStyle, baseColor) { IconStyleHelper.getBorderForStyle(iconStyle, baseColor) }
    val elevation = remember(iconStyle) { IconStyleHelper.getElevationForStyle(iconStyle) }
    val bg = remember(iconStyle, baseColor) { IconStyleHelper.getBackgroundTint(iconStyle, baseColor) }

    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            shape = shape,
            color = bg,
            shadowElevation = elevation,
            border = border,
            modifier = Modifier
                .size(50.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!customIconUrl.isNullOrEmpty()) {
                    coil.compose.AsyncImage(
                        model = customIconUrl,
                        contentDescription = appId.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(shape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = appId.title,
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF1744))
            )
        }
    }
}

private fun getDockIcon(appId: AppId): ImageVector {
    return when (appId) {
        AppId.PHONE -> Icons.Filled.Phone
        AppId.MESSAGES -> Icons.Filled.Chat
        AppId.BROWSER -> Icons.Filled.Public
        AppId.CAMERA -> Icons.Filled.CameraAlt
        AppId.MUSIC -> Icons.Filled.MusicNote
        AppId.SETTINGS -> Icons.Filled.Settings
        AppId.PHOTOS -> Icons.Filled.PhotoLibrary
        else -> Icons.Filled.Apps
    }
}
