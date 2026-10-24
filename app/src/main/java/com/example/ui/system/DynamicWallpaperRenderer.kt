package com.example.ui.system

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmapOrNull
import com.example.R
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Universal Wallpaper Background Renderer supporting:
 * 1. Live Animated Canvas Wallpapers (Aurora, Cyber Matrix, Nebula, Ocean Waves, Particle Swarm, Bokeh)
 * 2. Static Presets (Drawables & Custom Multi-Stop Gradients)
 * 3. System Native Android Wallpaper (Direct system WallpaperManager bitmap or transparent background)
 * 4. Custom Gallery Photos (Loaded from URI or Base64)
 */
@Composable
fun DynamicWallpaperRenderer(
    wallpaper: WallpaperDefinition,
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1.0f,
    isInteractive: Boolean = true,
    showVignetteOverlay: Boolean = true
) {
    val context = LocalContext.current
    var customBitmap by remember(wallpaper.customImageUri) { mutableStateOf<ImageBitmap?>(null) }
    var deviceSystemBitmap by remember(wallpaper.sourceType) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(wallpaper) {
        if (wallpaper.sourceType == WallpaperSourceType.CUSTOM_GALLERY && wallpaper.customImageUri != null) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(wallpaper.customImageUri)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            customBitmap = bmp.asImageBitmap()
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        } else if (wallpaper.sourceType == WallpaperSourceType.SYSTEM_NATIVE) {
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
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (wallpaper.sourceType) {
            WallpaperSourceType.LIVE_ANIMATED -> {
                val liveType = wallpaper.liveType ?: LiveWallpaperType.AURORA_GLOW
                LiveWallpaperCanvas(
                    liveType = liveType,
                    speedMultiplier = speedMultiplier,
                    isInteractive = isInteractive,
                    modifier = Modifier.fillMaxSize()
                )
            }

            WallpaperSourceType.SYSTEM_NATIVE -> {
                if (deviceSystemBitmap != null) {
                    Image(
                        bitmap = deviceSystemBitmap!!,
                        contentDescription = "Telefon Sistem Arka Planı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Native transparent fallback with soft protective shading
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
                }
            }

            WallpaperSourceType.CUSTOM_GALLERY -> {
                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap!!,
                        contentDescription = "Özel Duvar Kağıdı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF1E1E2E), Color(0xFF11111B))
                                )
                            )
                    )
                }
            }

            WallpaperSourceType.STATIC_PRESET -> {
                val resId = when (wallpaper.staticType) {
                    WallpaperType.AURORA -> R.drawable.wp_aurora
                    WallpaperType.CYBERPUNK -> R.drawable.wp_cyber
                    WallpaperType.ABSTRACT -> R.drawable.wp_abstract
                    else -> 0
                }

                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = wallpaper.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val colors = if (wallpaper.gradientColors.isNotEmpty()) {
                        wallpaper.gradientColors.map { Color(it) }
                    } else {
                        when (wallpaper.staticType) {
                            WallpaperType.DEEP_SPACE -> listOf(Color(0xFF070414), Color(0xFF1B0F38), Color(0xFF2E1065), Color(0xFF030014))
                            WallpaperType.FOREST_MIST -> listOf(Color(0xFF0A1F12), Color(0xFF143820), Color(0xFF1E5230), Color(0xFF05120A))
                            WallpaperType.OCEAN_SUNRISE -> listOf(Color(0xFF08182B), Color(0xFF0F3254), Color(0xFF9E4822), Color(0xFFD97736))
                            WallpaperType.MINIMAL_GRADIENT -> listOf(Color(0xFF1C0A18), Color(0xFF4A1035), Color(0xFF7A184D), Color(0xFF0D030B))
                            WallpaperType.DARK_OBSIDIAN -> listOf(Color(0xFF000000), Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFF0A0A0A))
                            WallpaperType.SUNSET_BLVD -> listOf(Color(0xFF2C0B3B), Color(0xFF8B2635), Color(0xFFE0533C), Color(0xFFF99F44))
                            WallpaperType.NEON_GEOMETRY -> listOf(Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF3A506B), Color(0xFF5BC0BE))
                            WallpaperType.COSMIC_PASTEL -> listOf(Color(0xFF2D1B4E), Color(0xFF563D7C), Color(0xFF8E6FB8), Color(0xFFD8B4E2))
                            else -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B), Color(0xFF415A77))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors))
                    )
                }
            }
        }

        // Soft Protective Vignette Overlay for UI Readability
        if (showVignetteOverlay && wallpaper.sourceType != WallpaperSourceType.LIVE_ANIMATED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.28f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.42f)
                            )
                        )
                    )
            )
        }
    }
}
