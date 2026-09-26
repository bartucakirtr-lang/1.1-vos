package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppId
import com.example.model.NavMode
import com.example.model.ThemePalette
import com.example.viewmodel.OSViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class OSViewModelTest {

    private lateinit var viewModel: OSViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = OSViewModel(app)
    }

    @Test
    fun testAppNavigationLifecycle() {
        assertNull(viewModel.currentApp.value)
        viewModel.openApp(AppId.SETTINGS)
        assertEquals(AppId.SETTINGS, viewModel.currentApp.value)
        assertTrue(viewModel.runningApps.value.contains(AppId.SETTINGS))

        viewModel.navigateHome()
        assertNull(viewModel.currentApp.value)
    }

    @Test
    fun testLockAndUnlockFlow() {
        assertFalse(viewModel.isLocked.value)
        viewModel.lockPhone()
        assertTrue(viewModel.isLocked.value)

        viewModel.unlockPhone()
        assertFalse(viewModel.isLocked.value)
    }

    @Test
    fun testQuickSettingsToggles() {
        val initialWifi = viewModel.wifiEnabled.value
        viewModel.toggleWifi()
        assertEquals(!initialWifi, viewModel.wifiEnabled.value)

        val initialFlashlight = viewModel.flashlightOn.value
        viewModel.toggleFlashlight()
        assertEquals(!initialFlashlight, viewModel.flashlightOn.value)
    }

    @Test
    fun testCalculatorEvaluation() {
        viewModel.onCalcButton("C")
        viewModel.onCalcButton("1")
        viewModel.onCalcButton("2")
        viewModel.onCalcButton("+")
        viewModel.onCalcButton("8")
        viewModel.onCalcButton("=")
        assertEquals("20", viewModel.calcResult.value)
    }

    @Test
    fun testThemeAndPersonalization() {
        viewModel.setThemePalette(ThemePalette.CYBERPUNK_NEON)
        assertEquals(ThemePalette.CYBERPUNK_NEON, viewModel.themePalette.value)

        viewModel.setNavigationMode(NavMode.THREE_BUTTON)
        assertEquals(NavMode.THREE_BUTTON, viewModel.navigationMode.value)

        // Test Light / Dark Mode Toggle across global state
        val initialDark = viewModel.isDarkMode.value
        viewModel.toggleDarkMode()
        assertEquals(!initialDark, viewModel.isDarkMode.value)

        viewModel.setDarkMode(true)
        assertTrue(viewModel.isDarkMode.value)
        assertEquals(com.example.model.ThemeMode.DARK, viewModel.themeMode.value)

        viewModel.setDarkMode(false)
        assertFalse(viewModel.isDarkMode.value)
        assertEquals(com.example.model.ThemeMode.LIGHT, viewModel.themeMode.value)

        viewModel.setThemeMode(com.example.model.ThemeMode.SYSTEM)
        assertEquals(com.example.model.ThemeMode.SYSTEM, viewModel.themeMode.value)
    }

    @Test
    fun testGlobalThemeStateClass() {
        val themeState = com.example.ui.theme.ThemeState(
            initialMode = com.example.model.ThemeMode.DARK,
            initialPalette = ThemePalette.OCEAN_BLUE,
            isSystemDark = false
        )

        assertTrue(themeState.isDark)
        themeState.toggleDarkMode()
        assertFalse(themeState.isDark)
        assertEquals(com.example.model.ThemeMode.LIGHT, themeState.themeMode)

        themeState.setThemeMode(com.example.model.ThemeMode.SYSTEM)
        assertFalse(themeState.isDark) // because isSystemDark = false

        themeState.isSystemInDarkState = true
        assertTrue(themeState.isDark)

        themeState.setPalette(ThemePalette.ANDROID_GREEN)
        assertEquals(ThemePalette.ANDROID_GREEN, themeState.currentPalette)
    }
}
