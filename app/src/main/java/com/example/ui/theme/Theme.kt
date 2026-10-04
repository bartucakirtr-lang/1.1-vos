package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode
import com.example.model.ThemePalette

/**
 * Global Compose State Management Interface for Light/Dark Theming
 * across the entire OS Forge UI.
 */
@Stable
interface GlobalThemeController {
    val themeMode: ThemeMode
    val isDark: Boolean
    val currentPalette: ThemePalette
    fun toggleDarkMode()
    fun setThemeMode(mode: ThemeMode)
    fun setDark(isDark: Boolean)
    fun setPalette(palette: ThemePalette)
}

/**
 * Global Compose State Management System for OS Forge UI Theming.
 * Encapsulates reactive Compose mutable state for ThemeMode, ThemePalette,
 * and effective isDark resolution across all apps, system bars, overlays, and widgets.
 */
@Stable
class ThemeState(
    initialMode: ThemeMode = ThemeMode.DARK,
    initialPalette: ThemePalette = ThemePalette.OCEAN_BLUE,
    isSystemDark: Boolean = false,
    private val onThemeModeChange: ((ThemeMode) -> Unit)? = null,
    private val onPaletteChange: ((ThemePalette) -> Unit)? = null,
    private val onToggleDarkMode: (() -> Unit)? = null
) : GlobalThemeController {
    var themeModeState by mutableStateOf(initialMode)
        private set

    var currentPaletteState by mutableStateOf(initialPalette)
        private set

    var isSystemInDarkState by mutableStateOf(isSystemDark)
        internal set

    override val themeMode: ThemeMode
        get() = themeModeState

    override val currentPalette: ThemePalette
        get() = currentPaletteState

    override val isDark: Boolean
        get() = when (themeModeState) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkState
        }

    override fun toggleDarkMode() {
        if (onToggleDarkMode != null) {
            onToggleDarkMode.invoke()
        } else {
            val target = !isDark
            setDark(target)
        }
    }

    override fun setThemeMode(mode: ThemeMode) {
        themeModeState = mode
        onThemeModeChange?.invoke(mode)
    }

    override fun setDark(isDark: Boolean) {
        val targetMode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
        themeModeState = targetMode
        onThemeModeChange?.invoke(targetMode)
    }

    override fun setPalette(palette: ThemePalette) {
        currentPaletteState = palette
        onPaletteChange?.invoke(palette)
    }

    fun sync(mode: ThemeMode, palette: ThemePalette, systemDark: Boolean, explicitDark: Boolean? = null) {
        themeModeState = if (explicitDark != null) {
            if (explicitDark) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            mode
        }
        currentPaletteState = palette
        isSystemInDarkState = systemDark
    }
}

/**
 * Creates and remembers a ThemeState instance across recompositions.
 */
@Composable
fun rememberThemeState(
    initialMode: ThemeMode = ThemeMode.DARK,
    initialPalette: ThemePalette = ThemePalette.OCEAN_BLUE,
    onThemeModeChange: ((ThemeMode) -> Unit)? = null,
    onPaletteChange: ((ThemePalette) -> Unit)? = null,
    onToggleDarkMode: (() -> Unit)? = null
): ThemeState {
    val systemInDark = isSystemInDarkTheme()
    val state = remember {
        ThemeState(
            initialMode = initialMode,
            initialPalette = initialPalette,
            isSystemDark = systemInDark,
            onThemeModeChange = onThemeModeChange,
            onPaletteChange = onPaletteChange,
            onToggleDarkMode = onToggleDarkMode
        )
    }
    LaunchedEffect(systemInDark) {
        state.isSystemInDarkState = systemInDark
    }
    return state
}

/**
 * CompositionLocal providing access to the reactive ThemeState.
 */
val LocalThemeState = staticCompositionLocalOf<ThemeState> {
    ThemeState()
}

/**
 * CompositionLocal providing access to the global theme controller anywhere in the Compose tree.
 */
val LocalThemeController = staticCompositionLocalOf<GlobalThemeController> {
    object : GlobalThemeController {
        override val themeMode: ThemeMode = ThemeMode.DARK
        override val isDark: Boolean = true
        override val currentPalette: ThemePalette = ThemePalette.OCEAN_BLUE
        override fun toggleDarkMode() {}
        override fun setThemeMode(mode: ThemeMode) {}
        override fun setDark(isDark: Boolean) {}
        override fun setPalette(palette: ThemePalette) {}
    }
}

/**
 * CompositionLocal providing quick boolean read for whether dark mode is currently active.
 */
val LocalIsDarkMode = compositionLocalOf { true }

/**
 * CompositionLocal providing the current active Material You color palette.
 */
val LocalThemePalette = compositionLocalOf { ThemePalette.OCEAN_BLUE }

/**
 * Global accessor for OS Forge UI Theme state and controller.
 */
object OSForgeTheme {
    val state: ThemeState
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeState.current

    val controller: GlobalThemeController
        @Composable
        @ReadOnlyComposable
        get() = LocalThemeController.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkMode.current

    val palette: ThemePalette
        @Composable
        @ReadOnlyComposable
        get() = LocalThemePalette.current
}

/**
 * Main application theme wrapper that handles global state propagation,
 * system theme resolution, and animated color transitions.
 */
@Composable
fun NovaOSTheme(
    palette: ThemePalette = ThemePalette.OCEAN_BLUE,
    themeMode: ThemeMode = ThemeMode.DARK,
    isDarkModeExplicit: Boolean? = null,
    wallpaper: com.example.model.WallpaperType = com.example.model.WallpaperType.AURORA,
    onToggleDarkMode: (() -> Unit)? = null,
    onThemeModeChange: ((ThemeMode) -> Unit)? = null,
    onPaletteChange: ((ThemePalette) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()

    val initialResolvedMode = remember(themeMode, isDarkModeExplicit) {
        if (isDarkModeExplicit != null) {
            if (isDarkModeExplicit) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            themeMode
        }
    }

    val themeState = rememberThemeState(
        initialMode = initialResolvedMode,
        initialPalette = palette,
        onThemeModeChange = onThemeModeChange,
        onPaletteChange = onPaletteChange,
        onToggleDarkMode = onToggleDarkMode
    )

    // Keep themeState synchronized if external state changes from ViewModel or System
    LaunchedEffect(themeMode, palette, isDarkModeExplicit, systemInDark) {
        themeState.sync(themeMode, palette, systemInDark, isDarkModeExplicit)
    }

    val effectiveIsDark = themeState.isDark
    val targetScheme = getDynamicColorScheme(themeState.currentPalette, effectiveIsDark, wallpaper)
    val animatedScheme = animateColorScheme(targetScheme)

    CompositionLocalProvider(
        LocalThemeState provides themeState,
        LocalThemeController provides themeState,
        LocalIsDarkMode provides effectiveIsDark,
        LocalThemePalette provides themeState.currentPalette
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Smoothly animates transitions between light and dark color schemes
 * to create a seamless, non-jarring visual experience when switching modes.
 */
@Composable
fun animateColorScheme(target: ColorScheme): ColorScheme {
    val animDuration = 240
    val primary by animateColorAsState(target.primary, tween(animDuration), label = "anim_primary")
    val onPrimary by animateColorAsState(target.onPrimary, tween(animDuration), label = "anim_onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, tween(animDuration), label = "anim_primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, tween(animDuration), label = "anim_onPrimaryContainer")
    val secondary by animateColorAsState(target.secondary, tween(animDuration), label = "anim_secondary")
    val onSecondary by animateColorAsState(target.onSecondary, tween(animDuration), label = "anim_onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, tween(animDuration), label = "anim_secContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, tween(animDuration), label = "anim_onSecContainer")
    val background by animateColorAsState(target.background, tween(animDuration), label = "anim_background")
    val onBackground by animateColorAsState(target.onBackground, tween(animDuration), label = "anim_onBackground")
    val surface by animateColorAsState(target.surface, tween(animDuration), label = "anim_surface")
    val onSurface by animateColorAsState(target.onSurface, tween(animDuration), label = "anim_onSurface")
    val surfaceVariant by animateColorAsState(target.surfaceVariant, tween(animDuration), label = "anim_surfaceVariant")
    val onSurfaceVariant by animateColorAsState(target.onSurfaceVariant, tween(animDuration), label = "anim_onSurfaceVariant")

    return target.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant
    )
}
