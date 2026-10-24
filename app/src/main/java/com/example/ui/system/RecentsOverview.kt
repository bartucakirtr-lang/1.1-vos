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
        AppId.CONTACTS -> Icons.Filled.Contacts
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
        AppId.THEMES -> Icons.Filled.Palette
        AppId.WALLPAPERS -> Icons.Filled.Wallpaper
        AppId.PAINT -> Icons.Filled.Draw
        AppId.PACKAGE_MANAGER -> Icons.Filled.Android
        AppId.VDESK -> Icons.Filled.DesktopWindows
        AppId.CUSTOM_APP -> Icons.Filled.CloudDownload
        AppId.YOUTUBE -> Icons.Filled.PlayCircle
        AppId.INSTAGRAM -> Icons.Filled.PhotoCamera
        AppId.GOOGLE -> Icons.Filled.Search
        AppId.WHATSAPP -> Icons.Filled.Forum
        AppId.SPOTIFY -> Icons.Filled.Headphones
        AppId.MAPS -> Icons.Filled.Place
        AppId.NETFLIX -> Icons.Filled.Movie
        AppId.TIKTOK -> Icons.Filled.SlowMotionVideo
        AppId.TWITTER_X, AppId.THREADS, AppId.FACEBOOK, AppId.MESSENGER, AppId.DISCORD, AppId.REDDIT -> Icons.Filled.Forum
        AppId.TELEGRAM, AppId.SIGNAL, AppId.WECHAT, AppId.VIBER, AppId.LINE, AppId.SKYPE -> Icons.Filled.Chat
        AppId.ZOOM, AppId.TEAMS, AppId.GOOGLE_MEET -> Icons.Filled.Videocam
        AppId.LINKEDIN, AppId.CRM_MOBILE, AppId.TEAM_CONNECT -> Icons.Filled.Work
        AppId.SNAPCHAT, AppId.PHOTO_PRO, AppId.CANVAS_3D -> Icons.Filled.PhotoCamera
        AppId.PINTEREST, AppId.COLOR_HARMONY, AppId.COLOR_PICKER -> Icons.Filled.Palette
        AppId.SOUNDCLOUD, AppId.DEEZER, AppId.TIDAL, AppId.APPLE_MUSIC, AppId.RADIO_FM -> Icons.Filled.MusicNote
        AppId.VLC_PLAYER, AppId.CINEMA_HD, AppId.VIDEO_EDITOR, AppId.SCREEN_RECORDER -> Icons.Filled.Movie
        AppId.PODCASTS, AppId.VOICE_RECORDER, AppId.AUDIO_STUDIO -> Icons.Filled.Mic
        AppId.GUITAR_TUNER, AppId.EQUALIZER_PRO, AppId.DRUM_PAD, AppId.PIANO_KEYS, AppId.BEAT_MAKER -> Icons.Filled.Headphones
        AppId.GOOGLE_DOCS, AppId.MS_WORD, AppId.PDF_READER, AppId.NOTEPAD_PLUS, AppId.MARKDOWN_EDITOR -> Icons.Filled.Description
        AppId.GOOGLE_SHEETS, AppId.MS_EXCEL, AppId.SPREADSHEET_LITE, AppId.SALES_TRACKER, AppId.BIZ_ANALYTICS -> Icons.Filled.TableChart
        AppId.GOOGLE_SLIDES, AppId.MS_POWERPOINT, AppId.PRESENTATION_PRO -> Icons.Filled.Slideshow
        AppId.NOTION, AppId.OBSIDIAN, AppId.MIND_MAP -> Icons.Filled.Lightbulb
        AppId.TODOIST, AppId.TRELLO, AppId.ASANA, AppId.KANBAN_FLOW, AppId.PROJECT_HUB -> Icons.Filled.ViewKanban
        AppId.EVERNOTE, AppId.MS_ONENOTE, AppId.DIARY_JOURNAL -> Icons.Filled.AutoStories
        AppId.SCANNER_PRO, AppId.QR_SCANNER -> Icons.Filled.QrCodeScanner
        AppId.POMODORO_FOCUS, AppId.TIME_LOGGER, AppId.METRONOME -> Icons.Filled.Timer
        AppId.HABIT_TRACKER, AppId.MILESTONE_PLANNER, AppId.REMINDERS -> Icons.Filled.Checklist
        AppId.EXPENSE_TRACKER, AppId.BUDGET_PLANNER, AppId.TAX_CALCULATOR, AppId.INVOICE_MAKER -> Icons.Filled.AttachMoney
        AppId.CALENDAR_PRO -> Icons.Filled.CalendarMonth
        AppId.CRYPTO_WALLET -> Icons.Filled.AccountBalanceWallet
        AppId.STOCK_TRACKER -> Icons.Filled.TrendingUp
        AppId.BANK_PORTAL -> Icons.Filled.AccountBalance
        AppId.AMAZON, AppId.EBAY, AppId.ALIEXPRESS, AppId.TRENDYOL, AppId.HEPSIBURADA -> Icons.Filled.ShoppingCart
        AppId.YEMEKSEPETI, AppId.GETIR, AppId.RECIPE_BOOK, AppId.COFFEE_BREW -> Icons.Filled.Restaurant
        AppId.UBER, AppId.RACING_3D -> Icons.Filled.DirectionsCar
        AppId.AIRBNB, AppId.BOOKING_COM, AppId.TRIP_ADVISOR -> Icons.Filled.Hotel
        AppId.DUOLINGO, AppId.KHAN_ACADEMY, AppId.TED_TALKS, AppId.WIKIPEDIA -> Icons.Filled.School
        AppId.WORKOUT_LOG, AppId.RUNNING_COACH, AppId.STEP_COUNTER_PRO, AppId.PEDOMETER -> Icons.Filled.DirectionsRun
        AppId.WATER_TRACKER -> Icons.Filled.WaterDrop
        AppId.CALORIE_COUNTER, AppId.BMI_CALC, AppId.FASTING_TRACKER -> Icons.Filled.Favorite
        AppId.SLEEP_ANALYSIS, AppId.MEDITATION, AppId.YOGA_GUIDE -> Icons.Filled.SelfImprovement
        AppId.MED_REMINDER -> Icons.Filled.LocalHospital
        AppId.SPEEDTEST, AppId.NETWORK_MONITOR, AppId.PING_TRACE -> Icons.Filled.Speed
        AppId.WIFI_ANALYZER, AppId.BT_SCANNER -> Icons.Filled.Wifi
        AppId.CPU_Z, AppId.SENSOR_BOX, AppId.BENCHMARK_X -> Icons.Filled.Memory
        AppId.COMPASS -> Icons.Filled.Explore
        AppId.FLASHLIGHT -> Icons.Filled.FlashlightOn
        AppId.UNIT_CONVERTER -> Icons.Filled.Straighten
        AppId.HASH_TOOL, AppId.AUTHENTICATOR, AppId.PASSWORD_VAULT -> Icons.Filled.Lock
        AppId.JSON_VIEWER, AppId.REGEX_TESTER, AppId.SSH_CLIENT, AppId.CODE_STUDIO, AppId.REPO_EXPLORER -> Icons.Filled.Code
        AppId.BATTERY_MONITOR -> Icons.Filled.BatteryChargingFull
        AppId.DISK_CLEANER, AppId.CACHE_OPTIMIZER -> Icons.Filled.CleaningServices
        AppId.TRANSLATE_PRO -> Icons.Filled.Translate
        AppId.BACKUP_SYNC, AppId.DEVICE_CLONE, AppId.FTP_SERVER -> Icons.Filled.CloudUpload
        AppId.FILE_COMPRESSOR, AppId.APK_EXTRACTOR -> Icons.Filled.FolderZip
        AppId.SYSTEM_LOGS, AppId.CRON_MANAGER -> Icons.Filled.Dns
        AppId.FONT_PREVIEW -> Icons.Filled.TextFields
        AppId.RETRO_SNAKE, AppId.PUZZLE_2048, AppId.CHESS_AI, AppId.SUDOKU_ZEN, AppId.PIXEL_RUNNER,
        AppId.SPACE_INVADERS, AppId.BRICK_BREAKER, AppId.CROSSWORD_PRO, AppId.SOLITAIRE, AppId.TIC_TAC_TOE,
        AppId.MINESWEEPER, AppId.FLAPPY_JET, AppId.BLOCK_PUZZLE, AppId.WORDLE_VOS, AppId.TOWER_DEFENSE -> Icons.Filled.SportsEsports
        else -> Icons.Filled.Apps
    }
}
