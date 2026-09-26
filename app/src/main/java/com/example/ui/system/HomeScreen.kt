package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.*
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
import com.example.model.StoreAppItem
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val isPlayingMusic by viewModel.isPlayingMusic.collectAsState()
    val musicTracks by viewModel.musicTracks.collectAsState()
    val currentTrackIndex by viewModel.currentTrackIndex.collectAsState()
    val storeApps by viewModel.storeApps.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val remoteConfigVersion by viewModel.remoteConfigVersion.collectAsState()
    val remoteConfigStatus by viewModel.remoteConfigStatus.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    var activeWidgetIndex by remember { mutableIntStateOf(0) } // 0: Music, 1: Notes, 2: Wellbeing
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val isDark = LocalIsDarkMode.current
    val widgetCardBg = if (isDark) Color.Black.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.85f)
    val widgetPrimaryText = if (isDark) Color.White else Color(0xFF191C1E)
    val widgetSecondaryText = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF43474E)
    val searchBarBg = if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f)
    val searchBarText = if (isDark) Color.White.copy(alpha = 0.65f) else Color(0xFF43474E)

    val mainApps by viewModel.homeApps.collectAsState()
    val isAssistantOpen by viewModel.isAssistantOpen.collectAsState()
    val assistantWidgets by viewModel.assistantWidgets.collectAsState()
    val assistantEnabled by viewModel.assistantEnabled.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var isEditMode by remember { mutableStateOf(false) }
    var showAddAppDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -30) {
                        viewModel.toggleAppDrawer()
                    }
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 35) {
                        viewModel.setAssistantOpen(true)
                    } else if (dragAmount < -35) {
                        viewModel.setAssistantOpen(false)
                    }
                }
            }
            .statusBarsPadding()
            .padding(horizontal = 18.dp)
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Edit Mode Configuration Banner
            AnimatedVisibility(
                visible = isEditMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Home Configuration",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Drag left/right or tap (X) to remove",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.resetHomeApps() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isEditMode = false },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Top: Smart At-A-Glance & Interactive Widget Area
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Interactive Switchable Widget Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = widgetCardBg
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        when (activeWidgetIndex) {
                            0 -> {
                                // SoundWave Music Widget
                                val track = musicTracks[currentTrackIndex]
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { viewModel.openApp(AppId.MUSIC) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(RoundedCornerShape(14.dp))
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
                                            Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = track.title,
                                                color = widgetPrimaryText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = track.artist,
                                                color = widgetSecondaryText,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "SoundWave Hi-Fi",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.prevTrack() }) {
                                            Icon(Icons.Filled.SkipPrevious, "Prev", tint = widgetPrimaryText)
                                        }
                                        IconButton(
                                            onClick = { viewModel.togglePlayMusic() },
                                            modifier = Modifier
                                                .size(42.dp)
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
                                            Icon(Icons.Filled.SkipNext, "Next", tint = widgetPrimaryText)
                                        }
                                    }
                                }
                            }
                            1 -> {
                                // Quick Notes Checklist Widget
                                val topNote = notes.firstOrNull()
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable { viewModel.openApp(AppId.NOTES) },
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = topNote?.title ?: "Quick Notes",
                                            color = widgetPrimaryText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(Icons.Filled.EditNote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }

                                    Text(
                                        text = topNote?.content ?: "Tap to create a new note or checklist...",
                                        color = widgetSecondaryText,
                                        fontSize = 12.sp,
                                        maxLines = 3
                                    )

                                    Text(
                                        text = "Keep Notes • 3 items",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            2 -> {
                                // Digital Wellbeing / Battery Health
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Screen Time Today",
                                            color = widgetSecondaryText,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "3h 42m",
                                            color = widgetPrimaryText,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Focus Mode Active • 12 Unlocks",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier.size(72.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(
                                                progress = { 0.65f },
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = Color.White.copy(alpha = 0.1f),
                                                modifier = Modifier.size(56.dp)
                                            )
                                            Text("65%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Widget switcher dots bottom right
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 0..2) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (activeWidgetIndex == i) MaterialTheme.colorScheme.primary
                                            else Color.White.copy(alpha = 0.3f)
                                        )
                                         .clickable { activeWidgetIndex = i }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Apps Grid (4 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(mainApps) { index, appId ->
                    val hasUnread = notifications.any { it.appId == appId }
                    AppIconTile(
                        appId = appId,
                        index = index,
                        currentLang = currentLang,
                        hasUnread = hasUnread,
                        isEditMode = isEditMode,
                        onLongClick = { isEditMode = true },
                        onDelete = { viewModel.removeHomeApp(appId) },
                        onReorder = { from, to -> viewModel.reorderHomeApps(from, to) },
                        onClick = { viewModel.openApp(appId) }
                    )
                }

                // Add app tile button if editing
                if (isEditMode) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showAddAppDialog = true }
                                .padding(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add App",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add App",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Google / Nexus Style Unified Search Bar
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = searchBarBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { viewModel.openApp(AppId.BROWSER) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Multi-color G logo badge
                        Text(
                            text = "G",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Search apps, web, contacts...",
                            color = searchBarText,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Voice",
                            tint = Color(0xFF4285F4),
                            modifier = Modifier.size(18.dp)
                        )
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Lens",
                            tint = Color(0xFFEA4335),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Bottom Dock (Phone, Messages, Chrome, Camera, App Drawer button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Phone Dock Icon
                DockAppIcon(
                    appId = AppId.PHONE,
                    icon = Icons.Filled.Phone,
                    bgColor = Color(0xFF00C853),
                    hasUnread = notifications.any { it.appId == AppId.PHONE },
                    onClick = { viewModel.openApp(AppId.PHONE) }
                )

                // Messages Dock Icon
                DockAppIcon(
                    appId = AppId.MESSAGES,
                    icon = Icons.Filled.Chat,
                    bgColor = Color(0xFF1E88E5),
                    hasUnread = notifications.any { it.appId == AppId.MESSAGES },
                    onClick = { viewModel.openApp(AppId.MESSAGES) }
                )

                // Chrome Dock Icon
                DockAppIcon(
                    appId = AppId.BROWSER,
                    icon = Icons.Filled.Public,
                    bgColor = Color(0xFFFB8C00),
                    hasUnread = false,
                    onClick = { viewModel.openApp(AppId.BROWSER) }
                )

                // Camera Dock Icon
                DockAppIcon(
                    appId = AppId.CAMERA,
                    icon = Icons.Filled.CameraAlt,
                    bgColor = Color(0xFFE53935),
                    hasUnread = false,
                    onClick = { viewModel.openApp(AppId.CAMERA) }
                )

                // App Drawer Button
                Surface(
                    shape = CircleShape,
                    color = if (isDark) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { viewModel.toggleAppDrawer() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Apps,
                            contentDescription = "App Drawer",
                            tint = if (isDark) Color.White else Color(0xFF191C1E),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }

    // Add App Dialog selection
    val availableToAdd = remember(mainApps) {
        AppId.entries.filter { it !in mainApps }
    }

    if (showAddAppDialog && availableToAdd.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showAddAppDialog = false },
            title = { Text("Add App to Home") },
            text = {
                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(availableToAdd) { app ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.addHomeApp(app)
                                        showAddAppDialog = false
                                    }
                                    .padding(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = getAppColor(app),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            getAppIcon(app),
                                            contentDescription = app.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = com.example.util.TranslationManager.getTranslation(app.title, currentLang),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddAppDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- Smart Assistant Panel UI (Overlay) ---
    var showAssistantSettings by remember { mutableStateOf(false) }
    var simRamUsage by remember { mutableStateOf(2.4f) }
    var isMemoryClearing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle grab-handle on far left
        if (!isAssistantOpen && assistantEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(14.dp)
                    .height(84.dp)
                    .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                    .clickable { viewModel.setAssistantOpen(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Open Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Overlay slide panel
        AnimatedVisibility(
            visible = isAssistantOpen && assistantEnabled,
            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Content Card (left 84% width)
                Card(
                    shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF151922).copy(alpha = 0.98f) else Color(0xFFF2F6FC).copy(alpha = 0.98f)
                    ),
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.84f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Aura Assistant",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Aura Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A1C1E)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { showAssistantSettings = !showAssistantSettings }) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Configure Panel",
                                        tint = if (showAssistantSettings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewModel.setAssistantOpen(false) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close Panel",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Configuration Widget Options (visible when Settings toggled)
                            if (showAssistantSettings) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color(0xFF232A3B) else Color(0xFFE4E9F2)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Configure Assistant",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isDark) Color.White else Color(0xFF1A1C1E)
                                                )
                                                TextButton(onClick = { showAssistantSettings = false }) {
                                                    Text("Close", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(1.dp)
                                                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f))
                                            )

                                            val list = listOf(
                                                "greeting" to "Welcome greeting",
                                                "system_monitor" to "System RAM Optimizer",
                                                "quick_notes" to "Keep Notes module",
                                                "quick_tasks" to "Tasks Checklist module",
                                                "recommendations" to "Smart Action recommendations"
                                            )

                                            list.forEach { (id, label) ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontSize = 12.sp,
                                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF43474E)
                                                    )
                                                    Switch(
                                                        checked = assistantWidgets.contains(id),
                                                        onCheckedChange = { viewModel.toggleAssistantWidget(id) },
                                                        modifier = Modifier.scale(0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 1. Dynamic Greeting Card
                            if (assistantWidgets.contains("greeting")) {
                                item {
                                    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                                    val greeting = when (hour) {
                                        in 5..11 -> "Good morning"
                                        in 12..17 -> "Good afternoon"
                                        else -> "Good evening"
                                    }
                                    val user = viewModel.userAccount.collectAsState().value
                                    val displayName = if (user.username.isNotEmpty()) user.username else "User"

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(user.avatarEmoji.ifEmpty { "✨" }, fontSize = 24.sp)
                                            Column {
                                                Text(
                                                    text = "$greeting, $displayName",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "Let's organize and speed up your system.",
                                                    fontSize = 11.sp,
                                                    color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF43474E)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. Resource Monitor & Dynamic Optimizer Card
                            if (assistantWidgets.contains("system_monitor")) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "System Performance Boost",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                                Icon(
                                                    Icons.Default.Speed,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("Active RAM Memory", fontSize = 11.sp, color = widgetSecondaryText)
                                                    Text(
                                                        text = if (isMemoryClearing) "Recycling memory..." else "${"%.1f".format(simRamUsage)} GB / 4.0 GB",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        if (!isMemoryClearing) {
                                                            isMemoryClearing = true
                                                            coroutineScope.launch {
                                                                delay(1200)
                                                                simRamUsage = 1.4f
                                                                isMemoryClearing = false
                                                            }
                                                        }
                                                    },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        if (isMemoryClearing) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(12.dp),
                                                                color = Color.White,
                                                                strokeWidth = 2.dp
                                                            )
                                                        } else {
                                                            Icon(Icons.Default.RocketLaunch, null, modifier = Modifier.size(12.dp))
                                                        }
                                                        Text("Boost", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            LinearProgressIndicator(
                                                progress = { if (isMemoryClearing) 0.35f else (simRamUsage / 4.0f) },
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Battery Charge: $batteryLevel%", fontSize = 11.sp, color = widgetSecondaryText)
                                                Text("Health: Optimum", fontSize = 11.sp, color = widgetSecondaryText)
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Quick Tasks Widget Checklist
                            if (assistantWidgets.contains("quick_tasks")) {
                                item {
                                    val taskList = viewModel.tasks.collectAsState().value
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Tasks Calendar Checklist", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { viewModel.openApp(AppId.TASKS) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Launch,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            if (taskList.isEmpty()) {
                                                Text("No active tasks found in database.", fontSize = 11.sp, color = widgetSecondaryText)
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    taskList.take(3).forEach { task ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable { viewModel.toggleTaskCompletion(task.id) }
                                                                .padding(vertical = 2.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                                contentDescription = null,
                                                                tint = if (task.isCompleted) MaterialTheme.colorScheme.primary else widgetSecondaryText,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                            Text(
                                                                text = task.title,
                                                                fontSize = 12.sp,
                                                                color = if (task.isCompleted) widgetSecondaryText else widgetPrimaryText,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 4. Quick Notes Widget
                            if (assistantWidgets.contains("quick_notes")) {
                                item {
                                    val notesList = viewModel.notes.collectAsState().value
                                    val topNote = notesList.firstOrNull()

                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Recent Keep Note", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                IconButton(
                                                    onClick = { viewModel.openApp(AppId.NOTES) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Launch,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            if (topNote == null) {
                                                Text("Tap the launch button to write your first note.", fontSize = 11.sp, color = widgetSecondaryText)
                                            } else {
                                                Text(topNote.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = widgetPrimaryText)
                                                Text(
                                                    text = topNote.content,
                                                    fontSize = 11.sp,
                                                    color = widgetSecondaryText,
                                                    maxLines = 2,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. Smart Recommendations Widget Card
                            if (assistantWidgets.contains("recommendations")) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Smart Actions",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = widgetSecondaryText,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Toggle Dark Mode action chip
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.toggleDarkMode() }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Contrast,
                                                        null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Toggle Dark Mode",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // App Store action chip
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { viewModel.openApp(AppId.STORE) }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocalMall,
                                                        null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "Search Store",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Scrim panel (right 16% width) to catch tap closes
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable { viewModel.setAssistantOpen(false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppIconTile(
    appId: AppId,
    index: Int,
    currentLang: String,
    hasUnread: Boolean,
    isEditMode: Boolean,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onReorder: (Int, Int) -> Unit,
    onClick: () -> Unit
) {
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(125, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                if (isEditMode) {
                    rotationZ = rotation
                    scaleX = scale
                    scaleY = scale
                }
            }
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { if (!isEditMode) onClick() },
                onLongClick = { onLongClick() }
            )
            .pointerInput(isEditMode) {
                if (isEditMode) {
                    detectDragGestures(
                        onDragStart = { dragOffsetX = 0f },
                        onDragEnd = {
                            if (dragOffsetX > 60f) {
                                onReorder(index, index + 1)
                            } else if (dragOffsetX < -60f) {
                                onReorder(index, index - 1)
                            }
                            dragOffsetX = 0f
                        },
                        onDragCancel = { dragOffsetX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                        }
                    )
                }
            }
            .padding(4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = getAppColor(appId),
                shadowElevation = if (isEditMode) 8.dp else 4.dp,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getAppIcon(appId),
                        contentDescription = appId.title,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (isEditMode) {
                // Delete button (red circle with white X)
                Surface(
                    shape = CircleShape,
                    color = Color.Red,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clickable { onDelete() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            } else if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF1744))
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = com.example.util.TranslationManager.getTranslation(appId.title, currentLang),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        if (isEditMode) {
            // Nudge arrow controls below app title for quick accessible reordering
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                IconButton(
                    onClick = { onReorder(index, index - 1) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Move Left",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                IconButton(
                    onClick = { onReorder(index, index + 1) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "Move Right",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DockAppIcon(
    appId: AppId,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    hasUnread: Boolean,
    onClick: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(52.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = appId.title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .offset(x = 2.dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF1744))
            )
        }
    }
}

fun getAppColor(appId: AppId): Color {
    return when (appId) {
        AppId.PHONE -> Color(0xFF00C853)
        AppId.MESSAGES -> Color(0xFF1E88E5)
        AppId.SETTINGS -> Color(0xFF546E7A)
        AppId.BROWSER -> Color(0xFFFB8C00)
        AppId.CAMERA -> Color(0xFFE53935)
        AppId.PHOTOS -> Color(0xFF8E24AA)
        AppId.MUSIC -> Color(0xFF7C4DFF)
        AppId.CLOCK -> Color(0xFF00ACC1)
        AppId.NOTES -> Color(0xFFFFB300)
        AppId.CALCULATOR -> Color(0xFF3949AB)
        AppId.FILES -> Color(0xFF43A047)
        AppId.STORE -> Color(0xFF00B0FF)
        AppId.ARCADE -> Color(0xFFFF5722)
        AppId.TERMINAL -> Color(0xFF212121)
        AppId.WEATHER -> Color(0xFF039BE5)
        AppId.TASKS -> Color(0xFF43A047)
        AppId.ACCOUNT -> Color(0xFF1976D2)
        AppId.CUSTOM_APP -> Color(0xFFE91E63)
    }
}
