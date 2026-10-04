package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.R
import com.example.model.AppId
import com.example.model.WallpaperType
import com.example.ui.apps.*
import com.example.ui.system.*
import com.example.ui.theme.NovaOSTheme
import com.example.util.toBitmapOrNull
import com.example.viewmodel.OSViewModel

@Composable
fun NovaOSRootScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val themePalette by viewModel.themePalette.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isShadeExpanded by viewModel.isShadeExpanded.collectAsState()
    val isRecentsOpen by viewModel.isRecentsOpen.collectAsState()
    val isAppDrawerOpen by viewModel.isAppDrawerOpen.collectAsState()
    val currentApp by viewModel.currentApp.collectAsState()
    val currentWallpaper by viewModel.currentWallpaper.collectAsState()
    val brightness by viewModel.brightness.collectAsState()
    val nightLight by viewModel.nightLight.collectAsState()
    val nightLightIntensity by viewModel.nightLightIntensity.collectAsState()
    val isSetupActive by viewModel.isSetupActive.collectAsState()
    val showFpsHud by viewModel.showFpsHud.collectAsState()
    val isDexModeActive by viewModel.isDexModeActive.collectAsState()
    val showSystemBars by viewModel.showSystemBars.collectAsState()
    val showVroxenAssistant by viewModel.showVroxenAssistant.collectAsState()
    val isBooting by viewModel.isBooting.collectAsState()

    LaunchedEffect(Unit) {
        com.example.util.VroxenAssistantManager.onWakeWordDetected = {
            viewModel.openVroxenAssistant()
        }
    }

    // Android Hardware / Software Back Button Handler
    BackHandler {
        viewModel.navigateBack()
    }

    NovaOSTheme(
        palette = themePalette,
        themeMode = themeMode,
        isDarkModeExplicit = isDarkMode,
        wallpaper = currentWallpaper,
        onToggleDarkMode = { viewModel.toggleDarkMode() },
        onThemeModeChange = { viewModel.setThemeMode(it) },
        onPaletteChange = { viewModel.setThemePalette(it) }
    ) {
        val isSystemLight = !isDarkMode

        // Determine if system status and navigation bars should be dark-on-light
        val isBarsOverlayLight = isSystemLight && (
            currentApp != null || isAppDrawerOpen || isShadeExpanded || isRecentsOpen || currentWallpaper.isLight
        )

        Surface(
            modifier = modifier.fillMaxSize(),
            color = if (currentWallpaper == WallpaperType.DEVICE_SYSTEM) Color.Transparent else MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Wallpaper Layer
                WallpaperBackground(currentWallpaper)

                // 2. Main Desktop Layer (Current App OR HomeScreen)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (!isLocked && showSystemBars) 44.dp else 0.dp)
                ) {
                    if (currentApp != null) {
                        AnimatedContent(
                            targetState = currentApp,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.95f))
                                    .togetherWith(fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 1.05f))
                            },
                            label = "AppTransition"
                        ) { targetApp ->
                            when (targetApp) {
                                AppId.PHONE -> PhoneApp(viewModel)
                                AppId.MESSAGES -> MessagesApp(viewModel)
                                AppId.SETTINGS -> SettingsApp(viewModel)
                                AppId.BROWSER -> BrowserApp(viewModel)
                                AppId.CAMERA -> CameraApp(viewModel)
                                AppId.PHOTOS -> GalleryApp(viewModel)
                                AppId.MUSIC -> MusicApp(viewModel)
                                AppId.CLOCK -> ClockApp(viewModel)
                                AppId.NOTES -> NotesApp(viewModel)
                                AppId.CALCULATOR -> CalculatorApp(viewModel)
                                AppId.FILES -> FilesApp(viewModel)
                                AppId.STORE -> PlayStoreApp(viewModel)
                                AppId.ARCADE -> ArcadeApp(viewModel)
                                AppId.TERMINAL -> TerminalApp(viewModel)
                                AppId.WEATHER -> WeatherApp(viewModel)
                                AppId.TASKS -> TasksApp(viewModel)
                                AppId.ACCOUNT -> AccountApp(viewModel)
                                AppId.CUSTOM_APP -> CustomSideloadApp(viewModel)
                                AppId.YOUTUBE -> YouTubeApp(viewModel)
                                AppId.INSTAGRAM -> InstagramApp(viewModel)
                                AppId.GOOGLE -> GoogleApp(viewModel)
                                AppId.WHATSAPP -> WhatsAppApp(viewModel)
                                AppId.SPOTIFY -> SpotifyApp(viewModel)
                                AppId.MAPS -> GoogleMapsApp(viewModel)
                                AppId.NETFLIX -> NetflixApp(viewModel)
                                AppId.TIKTOK -> TikTokApp(viewModel)
                                else -> HomeScreen(viewModel)
                            }
                        }
                    } else {
                        HomeScreen(viewModel)
                    }
                }

                // 3. System Status Bar (Overlay at top)
                if (showSystemBars) {
                    SystemStatusBar(
                        viewModel = viewModel,
                        modifier = Modifier.align(Alignment.TopCenter),
                        isOverlayLight = isBarsOverlayLight
                    )
                }

                // 4. System Navigation Bar (Overlay at bottom)
                if (!isLocked && showSystemBars) {
                    SystemNavigationBar(
                        viewModel = viewModel,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        isOverlayLight = isBarsOverlayLight
                    )
                }

                // 5. App Drawer Overlay (Swipe up from Home)
                AnimatedVisibility(
                    visible = isAppDrawerOpen && !isLocked,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    AppDrawer(viewModel)
                }

                // 6. Recents Multitasking Overview Overlay
                AnimatedVisibility(
                    visible = isRecentsOpen && !isLocked,
                    enter = fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.92f),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.92f)
                ) {
                    RecentsOverview(viewModel)
                }

                // 7. Notification & Quick Settings Shade Overlay (Pull down from top)
                AnimatedVisibility(
                    visible = isShadeExpanded,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    NotificationAndQuickSettingsShade(viewModel)
                }

                // 8. Lock Screen Overlay (Top-most system lock)
                AnimatedVisibility(
                    visible = isLocked,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    LockScreen(viewModel)
                }

                // 8b. vdesk Desktop Overlay
                AnimatedVisibility(
                    visible = isDexModeActive && !isLocked,
                    enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.95f),
                    exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.05f)
                ) {
                    DexModeScreen(viewModel)
                }

                // 9. Setup Wizard Overlay (OOBE / Onboarding)
                AnimatedVisibility(
                    visible = isSetupActive,
                    enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.96f),
                    exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 1.04f)
                ) {
                    SetupWizardScreen(viewModel)
                }

                // 11. Vroxen Voice AI Assistant Overlay
                if (showVroxenAssistant) {
                    VroxenAssistantDialog(
                        onDismiss = { viewModel.closeVroxenAssistant() }
                    )
                }
                if (showFpsHud) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 32.dp, end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Text(
                                text = "60 FPS • 16.6ms • RAM: 4.2GB",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 11. Brightness Dimming Filter Overlay (simulated OS hardware display brightness)
                if (brightness < 0.95f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (1.0f - brightness) * 0.7f))
                    )
                }

                // 12. Night Light Warm Amber Filter Overlay (Applies warm color overlay across UI)
                AnimatedVisibility(
                    visible = nightLight,
                    enter = fadeIn(animationSpec = tween(400)),
                    exit = fadeOut(animationSpec = tween(400))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFFF9100).copy(alpha = nightLightIntensity))
                    )
                }
            }
        }
    }
}

@Composable
private fun WallpaperBackground(wallpaper: WallpaperType) {
    val context = LocalContext.current
    var deviceSystemBitmap by remember(wallpaper) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(wallpaper) {
        if (wallpaper == WallpaperType.DEVICE_SYSTEM) {
            withContext(Dispatchers.IO) {
                try {
                    val wallpaperManager = android.app.WallpaperManager.getInstance(context)
                    val drawable = try {
                        wallpaperManager.drawable
                    } catch (e: Throwable) {
                        null
                    } ?: try {
                        wallpaperManager.peekDrawable()
                    } catch (e: Throwable) {
                        null
                    } ?: try {
                        wallpaperManager.fastDrawable
                    } catch (e: Throwable) {
                        null
                    } ?: try {
                        wallpaperManager.peekFastDrawable()
                    } catch (e: Throwable) {
                        null
                    }
                    val bmp = drawable?.toBitmapOrNull()
                    if (bmp != null) {
                        deviceSystemBitmap = bmp.asImageBitmap()
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } else {
            deviceSystemBitmap = null
        }
    }

    if (wallpaper == WallpaperType.DEVICE_SYSTEM) {
        // 1. If system wallpaper bitmap was decoded, draw it
        if (deviceSystemBitmap != null) {
            Image(
                bitmap = deviceSystemBitmap!!,
                contentDescription = "Telefon Arka Planı",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // If deviceSystemBitmap is null, the OS WindowManager FLAG_SHOW_WALLPAPER renders the phone's
        // native wallpaper (including live wallpapers) directly behind the transparent window.
        // We apply a soft protective gradient to ensure app icons, text labels, and widgets remain crisp & readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.20f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )
    } else {
        val resId = remember(wallpaper) {
            when (wallpaper) {
                WallpaperType.AURORA -> R.drawable.wp_aurora
                WallpaperType.CYBERPUNK -> R.drawable.wp_cyber
                WallpaperType.ABSTRACT -> R.drawable.wp_abstract
                else -> 0
            }
        }

        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = "Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val gradientColors = when (wallpaper) {
                WallpaperType.DEEP_SPACE -> listOf(Color(0xFF070414), Color(0xFF1B0F38), Color(0xFF2E1065), Color(0xFF030014))
                WallpaperType.FOREST_MIST -> listOf(Color(0xFF0A1F12), Color(0xFF143820), Color(0xFF1E5230), Color(0xFF05120A))
                WallpaperType.OCEAN_SUNRISE -> listOf(Color(0xFF08182B), Color(0xFF0F3254), Color(0xFF9E4822), Color(0xFFD97736))
                WallpaperType.MINIMAL_GRADIENT -> listOf(Color(0xFF1C0A18), Color(0xFF4A1035), Color(0xFF7A184D), Color(0xFF0D030B))
                else -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
            )
        }

        // Subtle dark vignette gradient for readability on preset wallpapers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )
    }
}
