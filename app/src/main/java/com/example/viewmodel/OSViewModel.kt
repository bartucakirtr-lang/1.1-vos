package com.example.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.OSRepository
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random

class OSViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs by lazy {
        application.getSharedPreferences("novaos_prefs", android.content.Context.MODE_PRIVATE)
    }

    // --- GitHub Remote Config State ---
    private val _remoteConfigVersion = MutableStateFlow<String?>("1.0.0")
    val remoteConfigVersion: StateFlow<String?> = _remoteConfigVersion.asStateFlow()

    private val _remoteConfigStatus = MutableStateFlow<String>("Initializing...")
    val remoteConfigStatus: StateFlow<String> = _remoteConfigStatus.asStateFlow()

    private val _remoteConfigRaw = MutableStateFlow<String>("")
    val remoteConfigRaw: StateFlow<String> = _remoteConfigRaw.asStateFlow()

    // --- Setup Wizard & Onboarding State ---
    private val _isSetupActive = MutableStateFlow(!prefs.getBoolean("has_completed_setup", false))
    val isSetupActive: StateFlow<Boolean> = _isSetupActive.asStateFlow()

    private val _isSetupCompleted = MutableStateFlow(prefs.getBoolean("has_completed_setup", false))
    val isSetupCompleted: StateFlow<Boolean> = _isSetupCompleted.asStateFlow()

    private val _homeApps = MutableStateFlow<List<AppId>>(
        listOf(
            AppId.SETTINGS,
            AppId.PHOTOS,
            AppId.MUSIC,
            AppId.CLOCK,
            AppId.NOTES,
            AppId.CALCULATOR,
            AppId.FILES,
            AppId.STORE,
            AppId.ARCADE,
            AppId.TERMINAL,
            AppId.TASKS,
            AppId.ACCOUNT
        )
    )
    val homeApps: StateFlow<List<AppId>> = _homeApps.asStateFlow()

    // --- Smart Assistant State ---
    private val _isAssistantOpen = MutableStateFlow(false)
    val isAssistantOpen: StateFlow<Boolean> = _isAssistantOpen.asStateFlow()

    private val _assistantWidgets = MutableStateFlow<Set<String>>(
        setOf("greeting", "system_monitor", "quick_notes", "quick_tasks", "recommendations")
    )
    val assistantWidgets: StateFlow<Set<String>> = _assistantWidgets.asStateFlow()

    private val _assistantEnabled = MutableStateFlow(true)
    val assistantEnabled: StateFlow<Boolean> = _assistantEnabled.asStateFlow()

    // --- Sideload / URL Installer State ---
    private val _sideloadedAppName = MutableStateFlow("Sideload App")
    val sideloadedAppName: StateFlow<String> = _sideloadedAppName.asStateFlow()

    private val _sideloadedAppUrl = MutableStateFlow("")
    val sideloadedAppUrl: StateFlow<String> = _sideloadedAppUrl.asStateFlow()

    private val _isSideloadInstalled = MutableStateFlow(false)
    val isSideloadInstalled: StateFlow<Boolean> = _isSideloadInstalled.asStateFlow()

    private val _setupStep = MutableStateFlow(SetupStep.WELCOME)
    val setupStep: StateFlow<SetupStep> = _setupStep.asStateFlow()

    private val _availableWifiNetworks = MutableStateFlow(OSRepository.getInitialWifiNetworks())
    val availableWifiNetworks: StateFlow<List<Pair<String, Boolean>>> = _availableWifiNetworks.asStateFlow()

    private val _connectedWifi = MutableStateFlow("Nova-Fiber_Ultra_5G")
    val connectedWifi: StateFlow<String> = _connectedWifi.asStateFlow()

    // --- User Account State ---
    private val _userAccount = MutableStateFlow(loadSavedUserAccount())
    val userAccount: StateFlow<UserAccount> = _userAccount.asStateFlow()

    private val _isSyncingAccount = MutableStateFlow(false)
    val isSyncingAccount: StateFlow<Boolean> = _isSyncingAccount.asStateFlow()

    // --- Nova Tasks App State ---
    private val _tasks = MutableStateFlow(OSRepository.getInitialTasks())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

    private val _taskFilter = MutableStateFlow("All")
    val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

    private val _taskSearchQuery = MutableStateFlow("")
    val taskSearchQuery: StateFlow<String> = _taskSearchQuery.asStateFlow()

    // --- System & OS State ---
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _pinCode = MutableStateFlow("1234")
    val pinCode: StateFlow<String> = _pinCode.asStateFlow()

    private val _inputPin = MutableStateFlow("")
    val inputPin: StateFlow<String> = _inputPin.asStateFlow()

    private val _pinError = MutableStateFlow(false)
    val pinError: StateFlow<Boolean> = _pinError.asStateFlow()

    private val _screenLockType = MutableStateFlow(LockType.SWIPE)
    val screenLockType: StateFlow<LockType> = _screenLockType.asStateFlow()

    private val _clockStyle = MutableStateFlow(ClockStyle.PIXEL_BOLD)
    val clockStyle: StateFlow<ClockStyle> = _clockStyle.asStateFlow()

    private val _isShadeExpanded = MutableStateFlow(false)
    val isShadeExpanded: StateFlow<Boolean> = _isShadeExpanded.asStateFlow()

    private val _isRecentsOpen = MutableStateFlow(false)
    val isRecentsOpen: StateFlow<Boolean> = _isRecentsOpen.asStateFlow()

    private val _isAppDrawerOpen = MutableStateFlow(false)
    val isAppDrawerOpen: StateFlow<Boolean> = _isAppDrawerOpen.asStateFlow()

    private val _currentApp = MutableStateFlow<AppId?>(null)
    val currentApp: StateFlow<AppId?> = _currentApp.asStateFlow()

    // --- Samsung DeX Mode State ---
    private val _isDexModeActive = MutableStateFlow(false)
    val isDexModeActive: StateFlow<Boolean> = _isDexModeActive.asStateFlow()

    private val _isDexStartMenuOpen = MutableStateFlow(false)
    val isDexStartMenuOpen: StateFlow<Boolean> = _isDexStartMenuOpen.asStateFlow()

    private val _dexOpenWindows = MutableStateFlow<List<AppId>>(listOf(AppId.BROWSER, AppId.FILES))
    val dexOpenWindows: StateFlow<List<AppId>> = _dexOpenWindows.asStateFlow()

    private val _dexActiveWindow = MutableStateFlow<AppId?>(AppId.BROWSER)
    val dexActiveWindow: StateFlow<AppId?> = _dexActiveWindow.asStateFlow()

    private val _dexMinimizedWindows = MutableStateFlow<Set<AppId>>(emptySet())
    val dexMinimizedWindows: StateFlow<Set<AppId>> = _dexMinimizedWindows.asStateFlow()

    private val _dexMaximizedWindows = MutableStateFlow<Set<AppId>>(emptySet())
    val dexMaximizedWindows: StateFlow<Set<AppId>> = _dexMaximizedWindows.asStateFlow()

    private val _runningApps = MutableStateFlow<List<AppId>>(listOf(AppId.SETTINGS, AppId.MESSAGES, AppId.MUSIC))
    val runningApps: StateFlow<List<AppId>> = _runningApps.asStateFlow()

    private val _navigationMode = MutableStateFlow(NavMode.GESTURE)
    val navigationMode: StateFlow<NavMode> = _navigationMode.asStateFlow()

    private val _themePalette = MutableStateFlow(ThemePalette.OCEAN_BLUE)
    val themePalette: StateFlow<ThemePalette> = _themePalette.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentWallpaper = MutableStateFlow(WallpaperType.AURORA)
    val currentWallpaper: StateFlow<WallpaperType> = _currentWallpaper.asStateFlow()

    // --- Quick Settings Toggles ---
    private val _wifiEnabled = MutableStateFlow(true)
    val wifiEnabled: StateFlow<Boolean> = _wifiEnabled.asStateFlow()

    private val _bluetoothEnabled = MutableStateFlow(true)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    private val _flashlightOn = MutableStateFlow(false)
    val flashlightOn: StateFlow<Boolean> = _flashlightOn.asStateFlow()

    private val _dndEnabled = MutableStateFlow(false)
    val dndEnabled: StateFlow<Boolean> = _dndEnabled.asStateFlow()

    private val _airplaneMode = MutableStateFlow(false)
    val airplaneMode: StateFlow<Boolean> = _airplaneMode.asStateFlow()

    private val _autoRotate = MutableStateFlow(true)
    val autoRotate: StateFlow<Boolean> = _autoRotate.asStateFlow()

    private val _batterySaver = MutableStateFlow(false)
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _nightLight = MutableStateFlow(false)
    val nightLight: StateFlow<Boolean> = _nightLight.asStateFlow()

    private val _brightness = MutableStateFlow(0.85f)
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _batteryLevel = MutableStateFlow(88)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _batteryStatusText = MutableStateFlow("Discharging")
    val batteryStatusText: StateFlow<String> = _batteryStatusText.asStateFlow()

    private val _batteryHealth = MutableStateFlow("Good")
    val batteryHealth: StateFlow<String> = _batteryHealth.asStateFlow()

    private val _batteryTemperature = MutableStateFlow(28.5f)
    val batteryTemperature: StateFlow<Float> = _batteryTemperature.asStateFlow()

    private val _batteryVoltage = MutableStateFlow(4120)
    val batteryVoltage: StateFlow<Int> = _batteryVoltage.asStateFlow()

    private val _batteryPluggedType = MutableStateFlow("On Battery Power")
    val batteryPluggedType: StateFlow<String> = _batteryPluggedType.asStateFlow()

    private val _batteryTechnology = MutableStateFlow("Li-ion")
    val batteryTechnology: StateFlow<String> = _batteryTechnology.asStateFlow()

    // --- Language & Localization (40 Languages Support) ---
    private val _currentLanguage = MutableStateFlow(prefs.getString("sys_language", "English") ?: "English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    // --- Developer Mode & Options ---
    private val _isDeveloperModeUnlocked = MutableStateFlow(prefs.getBoolean("dev_mode_unlocked", false))
    val isDeveloperModeUnlocked: StateFlow<Boolean> = _isDeveloperModeUnlocked.asStateFlow()

    private val _usbDebuggingEnabled = MutableStateFlow(prefs.getBoolean("dev_usb_debugging", true))
    val usbDebuggingEnabled: StateFlow<Boolean> = _usbDebuggingEnabled.asStateFlow()

    private val _showFpsHud = MutableStateFlow(prefs.getBoolean("dev_show_fps", false))
    val showFpsHud: StateFlow<Boolean> = _showFpsHud.asStateFlow()

    private val _showTouchPointer = MutableStateFlow(prefs.getBoolean("dev_show_touches", false))
    val showTouchPointer: StateFlow<Boolean> = _showTouchPointer.asStateFlow()

    private val _animatorScale = MutableStateFlow(prefs.getFloat("dev_anim_scale", 1.0f))
    val animatorScale: StateFlow<Float> = _animatorScale.asStateFlow()

    private val _strictModeEnabled = MutableStateFlow(prefs.getBoolean("dev_strict_mode", false))
    val strictModeEnabled: StateFlow<Boolean> = _strictModeEnabled.asStateFlow()

    // --- Notifications ---
    private val _notifications = MutableStateFlow(OSRepository.getInitialNotifications())
    val notifications: StateFlow<List<OSNotification>> = _notifications.asStateFlow()

    // --- Time Clock Ticker ---
    private val _systemTime = MutableStateFlow(System.currentTimeMillis())
    val systemTime: StateFlow<Long> = _systemTime.asStateFlow()

    // --- Phone & In-Call State ---
    private val _contacts = MutableStateFlow(OSRepository.getInitialContacts())
    val contacts: StateFlow<List<ContactItem>> = _contacts.asStateFlow()

    private val _callLogs = MutableStateFlow(OSRepository.getInitialCallLogs())
    val callLogs: StateFlow<List<CallLogItem>> = _callLogs.asStateFlow()

    private val _dialerNumber = MutableStateFlow("")
    val dialerNumber: StateFlow<String> = _dialerNumber.asStateFlow()

    private val _activeCallContact = MutableStateFlow<ContactItem?>(null)
    val activeCallContact: StateFlow<ContactItem?> = _activeCallContact.asStateFlow()

    private val _isInCall = MutableStateFlow(false)
    val isInCall: StateFlow<Boolean> = _isInCall.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    // --- Messages App ---
    private val _chatThreads = MutableStateFlow(OSRepository.getInitialChatThreads())
    val chatThreads: StateFlow<List<ChatThread>> = _chatThreads.asStateFlow()

    private val _selectedChatContactId = MutableStateFlow<String?>("c1")
    val selectedChatContactId: StateFlow<String?> = _selectedChatContactId.asStateFlow()

    private val _messageDraft = MutableStateFlow("")
    val messageDraft: StateFlow<String> = _messageDraft.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    // --- Music Player App & Visualizer ---
    private val _musicTracks = MutableStateFlow(OSRepository.getInitialMusicTracks())
    val musicTracks: StateFlow<List<MusicTrack>> = _musicTracks.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(0)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _isPlayingMusic = MutableStateFlow(false)
    val isPlayingMusic: StateFlow<Boolean> = _isPlayingMusic.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0)
    val playbackProgress: StateFlow<Int> = _playbackProgress.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _isRepeat = MutableStateFlow(false)
    val isRepeat: StateFlow<Boolean> = _isRepeat.asStateFlow()

    private val _audioVisualizerWave = MutableStateFlow(List(16) { 0.2f })
    val audioVisualizerWave: StateFlow<List<Float>> = _audioVisualizerWave.asStateFlow()

    // --- Notes App ---
    private val _notes = MutableStateFlow(OSRepository.getInitialNotes())
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery.asStateFlow()

    // --- Clock App ---
    private val _clockTab = MutableStateFlow(0) // 0: Alarm, 1: World, 2: Stopwatch, 3: Timer
    val clockTab: StateFlow<Int> = _clockTab.asStateFlow()

    private val _alarms = MutableStateFlow(OSRepository.getInitialAlarms())
    val alarms: StateFlow<List<AlarmItem>> = _alarms.asStateFlow()

    private val _worldClocks = MutableStateFlow(OSRepository.getInitialWorldClocks())
    val worldClocks: StateFlow<List<WorldClockCity>> = _worldClocks.asStateFlow()

    private val _stopwatchRunning = MutableStateFlow(false)
    val stopwatchRunning: StateFlow<Boolean> = _stopwatchRunning.asStateFlow()

    private val _stopwatchElapsedMillis = MutableStateFlow(0L)
    val stopwatchElapsedMillis: StateFlow<Long> = _stopwatchElapsedMillis.asStateFlow()

    private val _stopwatchLaps = MutableStateFlow<List<Long>>(emptyList())
    val stopwatchLaps: StateFlow<List<Long>> = _stopwatchLaps.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(300)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(300)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    // --- Gallery & Camera ---
    private val _photos = MutableStateFlow(OSRepository.getInitialPhotos())
    val photos: StateFlow<List<PhotoItem>> = _photos.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<PhotoItem?>(null)
    val selectedPhoto: StateFlow<PhotoItem?> = _selectedPhoto.asStateFlow()

    private val _cameraFilter = MutableStateFlow("Normal")
    val cameraFilter: StateFlow<String> = _cameraFilter.asStateFlow()

    private val _cameraMode = MutableStateFlow("Photo") // Photo, Video, Portrait, Night, Pro
    val cameraMode: StateFlow<String> = _cameraMode.asStateFlow()

    private val _cameraZoom = MutableStateFlow(1.0f)
    val cameraZoom: StateFlow<Float> = _cameraZoom.asStateFlow()

    private val _cameraFlash = MutableStateFlow(false)
    val cameraFlash: StateFlow<Boolean> = _cameraFlash.asStateFlow()

    // --- Calculator App ---
    private val _calcExpression = MutableStateFlow("0")
    val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

    private val _calcResult = MutableStateFlow("")
    val calcResult: StateFlow<String> = _calcResult.asStateFlow()

    private val _calcHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val calcHistory: StateFlow<List<Pair<String, String>>> = _calcHistory.asStateFlow()

    // --- Files App ---
    private val _files = MutableStateFlow(OSRepository.getInitialFiles())
    val files: StateFlow<List<FileItem>> = _files.asStateFlow()

    private val _currentFolderPath = MutableStateFlow("/storage/emulated/0")
    val currentFolderPath: StateFlow<String> = _currentFolderPath.asStateFlow()

    // --- App Store ---
    private val _storeApps = MutableStateFlow(OSRepository.getInitialStoreApps())
    val storeApps: StateFlow<List<StoreAppItem>> = _storeApps.asStateFlow()

    // --- Weather App ---
    private val _hourlyForecast = MutableStateFlow(OSRepository.getInitialWeather().first)
    val hourlyForecast: StateFlow<List<HourlyForecast>> = _hourlyForecast.asStateFlow()

    private val _dailyForecast = MutableStateFlow(OSRepository.getInitialWeather().second)
    val dailyForecast: StateFlow<List<WeatherForecast>> = _dailyForecast.asStateFlow()

    // --- Web Browser ---
    private val _browserTabs = MutableStateFlow(
        listOf(
            BrowserTab("tab1", "Nova Search", "https://nexus.search/"),
            BrowserTab("tab2", "Tech Horizon 2026", "https://news.techhorizon.io/ai-os")
        )
    )
    val browserTabs: StateFlow<List<BrowserTab>> = _browserTabs.asStateFlow()

    private val _currentTabId = MutableStateFlow("tab1")
    val currentTabId: StateFlow<String> = _currentTabId.asStateFlow()

    private val _urlInputText = MutableStateFlow("https://nexus.search/")
    val urlInputText: StateFlow<String> = _urlInputText.asStateFlow()

    // --- Terminal App ---
    private val _terminalOutput = MutableStateFlow(
        listOf(
            "NovaOS Kernel v16.4-baklava [aarch64]",
            "Type 'help' for available commands or 'neofetch' for system overview.",
            "nexus@novaos-pixel10:~$ "
        )
    )
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()

    // --- Arcade Games (2048, Snake, TicTacToe) ---
    private val _activeGame = MutableStateFlow("2048") // "2048", "SNAKE", "TICTACTOE"
    val activeGame: StateFlow<String> = _activeGame.asStateFlow()

    // 2048 state
    private val _grid2048 = MutableStateFlow(Array(4) { IntArray(4) })
    val grid2048: StateFlow<Array<IntArray>> = _grid2048.asStateFlow()
    private val _score2048 = MutableStateFlow(0)
    val score2048: StateFlow<Int> = _score2048.asStateFlow()
    private val _bestScore2048 = MutableStateFlow(2480)
    val bestScore2048: StateFlow<Int> = _bestScore2048.asStateFlow()
    private val _gameOver2048 = MutableStateFlow(false)
    val gameOver2048: StateFlow<Boolean> = _gameOver2048.asStateFlow()

    // Snake state
    private val _snakeBody = MutableStateFlow(listOf(Pair(5, 5), Pair(5, 6), Pair(5, 7)))
    val snakeBody: StateFlow<List<Pair<Int, Int>>> = _snakeBody.asStateFlow()
    private val _snakeFood = MutableStateFlow(Pair(10, 10))
    val snakeFood: StateFlow<Pair<Int, Int>> = _snakeFood.asStateFlow()
    private val _snakeDir = MutableStateFlow(Pair(0, -1)) // up
    val snakeDir: StateFlow<Pair<Int, Int>> = _snakeDir.asStateFlow()
    private val _snakeScore = MutableStateFlow(0)
    val snakeScore: StateFlow<Int> = _snakeScore.asStateFlow()
    private val _isSnakeRunning = MutableStateFlow(false)
    val isSnakeRunning: StateFlow<Boolean> = _isSnakeRunning.asStateFlow()
    private val _isSnakeGameOver = MutableStateFlow(false)
    val isSnakeGameOver: StateFlow<Boolean> = _isSnakeGameOver.asStateFlow()

    // Tic-Tac-Toe state
    private val _tttBoard = MutableStateFlow(List(9) { "" })
    val tttBoard: StateFlow<List<String>> = _tttBoard.asStateFlow()
    private val _tttWinner = MutableStateFlow<String?>(null) // "X", "O", "DRAW", or null
    val tttWinner: StateFlow<String?> = _tttWinner.asStateFlow()

    // Audio synthesizer track for sound effects and music
    private var audioTrack: AudioTrack? = null
    private var soundJob: Job? = null

    init {
        startSystemClockTicker()
        startCallTimerTicker()
        startMusicPlaybackTicker()
        startStopwatchTicker()
        startCountdownTimerTicker()
        init2048Game()
        fetchRemoteConfigVersion()
        initBatteryMonitoring()
    }

    fun fetchRemoteConfigVersion() {
        viewModelScope.launch(Dispatchers.IO) {
            _remoteConfigStatus.value = "Fetching from GitHub..."
            val rawUrl = "https://raw.githubusercontent.com/bartucakirtr-lang/1.1-vos/main/config.json"
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            try {
                val request = Request.Builder()
                    .url(rawUrl)
                    .header("User-Agent", "vos-App")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyString = response.body?.string() ?: ""
                        _remoteConfigRaw.value = bodyString
                        try {
                            val json = JSONObject(bodyString)
                            val version = when {
                                json.has("version") -> json.get("version").toString()
                                json.has("ver") -> json.get("ver").toString()
                                json.has("appVersion") -> json.get("appVersion").toString()
                                json.has("sürüm") -> json.get("sürüm").toString()
                                else -> "1.0.0"
                            }
                            _remoteConfigVersion.value = version
                            _remoteConfigStatus.value = "Fetched version $version from config.json"
                            android.util.Log.d("OSViewModel", "Successfully fetched config.json from GitHub: version = $version")
                        } catch (e: Exception) {
                            _remoteConfigStatus.value = "Parse error: ${e.message}"
                            _remoteConfigVersion.value = "1.0.0"
                        }
                    } else {
                        _remoteConfigStatus.value = "HTTP ${response.code} (GitHub config.json)"
                    }
                }
            } catch (e: Exception) {
                _remoteConfigStatus.value = "Offline / ${e.localizedMessage}"
            }
        }
    }

    private fun startSystemClockTicker() {
        viewModelScope.launch {
            while (isActive) {
                _systemTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    private fun startCallTimerTicker() {
        viewModelScope.launch {
            while (isActive) {
                if (_isInCall.value) {
                    _callDurationSeconds.value += 1
                }
                delay(1000)
            }
        }
    }

    private fun startMusicPlaybackTicker() {
        viewModelScope.launch {
            while (isActive) {
                if (_isPlayingMusic.value) {
                    val track = _musicTracks.value[_currentTrackIndex.value]
                    if (_playbackProgress.value < track.durationSeconds) {
                        _playbackProgress.value += 1
                        // Generate dynamic visualizer bars
                        _audioVisualizerWave.value = List(16) {
                            (0.15f + Random.nextFloat() * 0.85f).coerceIn(0.1f, 1.0f)
                        }
                    } else {
                        if (_isRepeat.value) {
                            _playbackProgress.value = 0
                        } else {
                            nextTrack()
                        }
                    }
                } else {
                    _audioVisualizerWave.value = List(16) { 0.15f }
                }
                delay(1000)
            }
        }
    }

    private fun startStopwatchTicker() {
        viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                if (_stopwatchRunning.value) {
                    _stopwatchElapsedMillis.value += (now - lastTime)
                }
                lastTime = now
                delay(33) // ~30 fps update
            }
        }
    }

    private fun startCountdownTimerTicker() {
        viewModelScope.launch {
            while (isActive) {
                if (_timerRunning.value) {
                    if (_timerRemainingSeconds.value > 0) {
                        _timerRemainingSeconds.value -= 1
                    } else {
                        _timerRunning.value = false
                        addNotification(
                            OSNotification(
                                id = "timer_alert_${System.currentTimeMillis()}",
                                appId = AppId.CLOCK,
                                title = "Timer Completed! ⏰",
                                message = "Your countdown timer has ended.",
                                actionLabel = "Dismiss"
                            )
                        )
                        playBeepTone(880f, 1500)
                    }
                }
                delay(1000)
            }
        }
    }

    // --- Audio Synthesis Helper ---
    fun playTone(freqHz: Float, durationMs: Long = 120) {
        soundJob?.cancel()
        soundJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freqHz)
                    val envelope = sin(Math.PI * i / numSamples) // smooth fade in/out
                    samples[i] = (sin(angle) * envelope * Short.MAX_VALUE * 0.4f * _volume.value).toInt().toShort()
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                delay(durationMs + 50)
                track.release()
            } catch (_: Exception) {
            }
        }
    }

    private fun playBeepTone(freqHz: Float, durationMs: Long) {
        playTone(freqHz, durationMs)
    }

    // --- Lock Screen Operations ---
    fun lockPhone() {
        _isLocked.value = true
        _inputPin.value = ""
        _pinError.value = false
        _isShadeExpanded.value = false
    }

    fun unlockPhone() {
        _isLocked.value = false
        _inputPin.value = ""
        _pinError.value = false
    }

    fun inputPinDigit(digit: String) {
        if (_inputPin.value.length < 6) {
            _inputPin.value += digit
            playTone(600f + _inputPin.value.length * 50f, 60)
            if (_inputPin.value == _pinCode.value) {
                unlockPhone()
            } else if (_inputPin.value.length >= 4 && _inputPin.value != _pinCode.value.take(_inputPin.value.length)) {
                _pinError.value = true
                viewModelScope.launch {
                    delay(800)
                    _inputPin.value = ""
                    _pinError.value = false
                }
            }
        }
    }

    fun clearPinDigit() {
        if (_inputPin.value.isNotEmpty()) {
            _inputPin.value = _inputPin.value.dropLast(1)
            _pinError.value = false
        }
    }

    fun setLockType(type: LockType) {
        _screenLockType.value = type
    }

    fun setPin(pin: String) {
        _pinCode.value = pin
    }

    fun setClockStyle(style: ClockStyle) {
        _clockStyle.value = style
    }

    // --- Notification & Shade Actions ---
    fun toggleShade() {
        _isShadeExpanded.value = !_isShadeExpanded.value
    }

    fun expandShade() {
        _isShadeExpanded.value = true
    }

    fun collapseShade() {
        _isShadeExpanded.value = false
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun addNotification(notification: OSNotification) {
        _notifications.value = listOf(notification) + _notifications.value
        playTone(880f, 150)
    }

    // --- Samsung DeX Desktop Methods ---
    fun toggleDexMode() {
        val next = !_isDexModeActive.value
        _isDexModeActive.value = next
        _isDexStartMenuOpen.value = false
        if (next) {
            addNotification(
                OSNotification(
                    id = "notif_vdesk_${System.currentTimeMillis()}",
                    appId = AppId.SETTINGS,
                    title = "🖥️ vdesk Active",
                    message = "Desktop mode enabled. Use taskbar and multi-window launcher for desktop productivity.",
                    actionLabel = "Open vdesk"
                )
            )
        }
        playTone(if (next) 1000f else 400f, 100)
    }

    fun setDexModeActive(active: Boolean) {
        _isDexModeActive.value = active
        _isDexStartMenuOpen.value = false
    }

    fun toggleDexStartMenu() {
        _isDexStartMenuOpen.value = !_isDexStartMenuOpen.value
        playTone(700f, 50)
    }

    fun closeDexStartMenu() {
        _isDexStartMenuOpen.value = false
    }

    fun openDexApp(appId: AppId) {
        if (!_dexOpenWindows.value.contains(appId)) {
            _dexOpenWindows.value = _dexOpenWindows.value + appId
        }
        _dexMinimizedWindows.value = _dexMinimizedWindows.value - appId
        _dexActiveWindow.value = appId
        _isDexStartMenuOpen.value = false
        playTone(850f, 60)
    }

    fun closeDexApp(appId: AppId) {
        _dexOpenWindows.value = _dexOpenWindows.value.filter { it != appId }
        if (_dexActiveWindow.value == appId) {
            _dexActiveWindow.value = _dexOpenWindows.value.lastOrNull()
        }
        playTone(500f, 50)
    }

    fun minimizeDexApp(appId: AppId) {
        if (_dexMinimizedWindows.value.contains(appId)) {
            _dexMinimizedWindows.value = _dexMinimizedWindows.value - appId
            _dexActiveWindow.value = appId
        } else {
            _dexMinimizedWindows.value = _dexMinimizedWindows.value + appId
            if (_dexActiveWindow.value == appId) {
                _dexActiveWindow.value = _dexOpenWindows.value.filter { it != appId && !_dexMinimizedWindows.value.contains(it) }.lastOrNull()
            }
        }
    }

    fun maximizeDexApp(appId: AppId) {
        if (_dexMaximizedWindows.value.contains(appId)) {
            _dexMaximizedWindows.value = _dexMaximizedWindows.value - appId
        } else {
            _dexMaximizedWindows.value = _dexMaximizedWindows.value + appId
        }
        _dexActiveWindow.value = appId
    }

    fun focusDexWindow(appId: AppId) {
        _dexActiveWindow.value = appId
        _dexMinimizedWindows.value = _dexMinimizedWindows.value - appId
    }

    // --- Quick Toggles ---
    fun toggleWifi() { _wifiEnabled.value = !_wifiEnabled.value }
    fun toggleBluetooth() { _bluetoothEnabled.value = !_bluetoothEnabled.value }
    fun toggleFlashlight() { _flashlightOn.value = !_flashlightOn.value }
    fun toggleDnd() { _dndEnabled.value = !_dndEnabled.value }
    fun toggleAirplane() { _airplaneMode.value = !_airplaneMode.value }
    fun toggleAutoRotate() { _autoRotate.value = !_autoRotate.value }
    fun toggleBatterySaver() { _batterySaver.value = !_batterySaver.value }
    fun toggleNightLight() { _nightLight.value = !_nightLight.value }
    fun toggleDarkMode() {
        val newDark = !_isDarkMode.value
        _isDarkMode.value = newDark
        _themeMode.value = if (newDark) ThemeMode.DARK else ThemeMode.LIGHT
        playTone(if (newDark) 520f else 880f, 60)
    }
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        when (mode) {
            ThemeMode.LIGHT -> _isDarkMode.value = false
            ThemeMode.DARK -> _isDarkMode.value = true
            ThemeMode.SYSTEM -> {
                // Preserved for Compose isSystemInDarkTheme resolution
            }
        }
        playTone(750f, 50)
    }
    fun setDarkMode(dark: Boolean) {
        _isDarkMode.value = dark
        _themeMode.value = if (dark) ThemeMode.DARK else ThemeMode.LIGHT
        playTone(if (dark) 520f else 880f, 60)
    }
    fun setBrightness(b: Float) { _brightness.value = b.coerceIn(0.1f, 1.0f) }
    fun setVolume(v: Float) { _volume.value = v.coerceIn(0.0f, 1.0f) }
    fun toggleCharging() {
        _isCharging.value = !_isCharging.value
        if (_isCharging.value) {
            _batteryLevel.value = min(100, _batteryLevel.value + 1)
        }
    }

    private fun initBatteryMonitoring() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val stickyIntent = getApplication<Application>().registerReceiver(null, filter)
            stickyIntent?.let { updateBatteryFromIntent(it) }

            getApplication<Application>().registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    intent?.let { updateBatteryFromIntent(it) }
                }
            }, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateBatteryFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        if (level >= 0 && scale > 0) {
            val pct = (level * 100) / scale
            _batteryLevel.value = pct
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isChargingNow = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        _isCharging.value = isChargingNow

        _batteryStatusText.value = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging ⚡"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged 🔋"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> if (isChargingNow) "Charging ⚡" else "Discharging"
        }

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        _batteryPluggedType.value = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Power Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Bus Connection"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Qi Wireless Charging"
            else -> "On Battery Power"
        }

        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        _batteryHealth.value = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Good"
        }

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        if (temp > 0) {
            _batteryTemperature.value = temp / 10.0f
        }

        val volt = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        if (volt > 0) {
            _batteryVoltage.value = volt
        }

        val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        if (!tech.isNullOrEmpty()) {
            _batteryTechnology.value = tech
        }
    }

    fun setLanguage(language: String) {
        _currentLanguage.value = language
        prefs.edit().putString("sys_language", language).apply()

        try {
            val iso = com.example.util.TranslationManager.LANGUAGE_ISO_MAP[language] ?: "en"
            val locale = Locale.forLanguageTag(iso)
            Locale.setDefault(locale)

            val config = getApplication<Application>().resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            getApplication<Application>().resources.updateConfiguration(
                config,
                getApplication<Application>().resources.displayMetrics
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        playTone(600f, 60)
    }

    fun tr(key: String): String {
        return com.example.util.TranslationManager.getTranslation(key, _currentLanguage.value)
    }

    fun addRealCapturedPhoto(title: String, fileUri: String) {
        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        val photo = PhotoItem(
            id = "real_photo_${System.currentTimeMillis()}",
            title = title,
            dateAdded = dateStr,
            drawableResName = "real_camera",
            isFavorite = false,
            category = "Camera",
            filterApplied = _cameraFilter.value,
            fileUri = fileUri,
            isVideo = false
        )
        _photos.value = listOf(photo) + _photos.value
        playTone(1200f, 80)
    }

    fun addRealCapturedVideo(title: String, fileUri: String) {
        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date())
        val videoPhoto = PhotoItem(
            id = "real_video_${System.currentTimeMillis()}",
            title = title,
            dateAdded = dateStr,
            drawableResName = "real_video",
            isFavorite = false,
            category = "Videos",
            filterApplied = "1080p Video",
            fileUri = fileUri,
            isVideo = true
        )
        _photos.value = listOf(videoPhoto) + _photos.value
        playTone(900f, 120)
    }

    // --- Personalization ---
    fun setThemePalette(palette: ThemePalette) { _themePalette.value = palette }
    fun setWallpaper(wallpaper: WallpaperType) { _currentWallpaper.value = wallpaper }
    fun setNavigationMode(mode: NavMode) { _navigationMode.value = mode }

    // --- App Lifecycle & Multitasking ---
    fun openApp(appId: AppId) {
        _currentApp.value = appId
        _isShadeExpanded.value = false
        _isRecentsOpen.value = false
        _isAppDrawerOpen.value = false
        if (!_runningApps.value.contains(appId)) {
            _runningApps.value = _runningApps.value + appId
        }
    }

    fun closeCurrentApp() {
        _currentApp.value = null
    }

    fun navigateHome() {
        if (_isSetupActive.value && _isSetupCompleted.value) {
            _isSetupActive.value = false
        }
        _currentApp.value = null
        _isRecentsOpen.value = false
        _isAppDrawerOpen.value = false
        _isShadeExpanded.value = false
    }

    fun navigateBack(): Boolean {
        if (_isSetupActive.value) {
            if (_setupStep.value != SetupStep.WELCOME) {
                prevSetupStep()
                return true
            } else if (_isSetupCompleted.value) {
                _isSetupActive.value = false
                return true
            }
            return false
        }
        if (_isShadeExpanded.value) {
            _isShadeExpanded.value = false
            return true
        }
        if (_isAppDrawerOpen.value) {
            _isAppDrawerOpen.value = false
            return true
        }
        if (_isRecentsOpen.value) {
            _isRecentsOpen.value = false
            return true
        }
        if (_currentApp.value != null) {
            _currentApp.value = null
            return true
        }
        return false
    }

    fun openRecents() {
        _isRecentsOpen.value = true
        _isShadeExpanded.value = false
        _isAppDrawerOpen.value = false
    }

    fun closeRecents() {
        _isRecentsOpen.value = false
    }

    fun toggleAppDrawer() {
        _isAppDrawerOpen.value = !_isAppDrawerOpen.value
    }

    fun closeAppDrawer() {
        _isAppDrawerOpen.value = false
    }

    fun killRecentApp(appId: AppId) {
        _runningApps.value = _runningApps.value.filter { it != appId }
        if (_currentApp.value == appId) {
            _currentApp.value = null
        }
    }

    fun clearAllRecents() {
        _runningApps.value = emptyList()
        _currentApp.value = null
        _isRecentsOpen.value = false
    }

    // --- Phone & In-Call Actions ---
    fun dialDigit(digit: String) {
        _dialerNumber.value += digit
        val freq = when (digit) {
            "1" -> 697f; "2" -> 770f; "3" -> 852f; "4" -> 941f; "5" -> 1000f;
            "6" -> 1100f; "7" -> 1209f; "8" -> 1336f; "9" -> 1477f; "0" -> 941f;
            else -> 800f
        }
        playTone(freq, 70)
    }

    fun clearDialDigit() {
        if (_dialerNumber.value.isNotEmpty()) {
            _dialerNumber.value = _dialerNumber.value.dropLast(1)
        }
    }

    fun startCall(contact: ContactItem) {
        _activeCallContact.value = contact
        _isInCall.value = true
        _callDurationSeconds.value = 0
        _isCallMuted.value = false
        _isSpeakerOn.value = false
        playTone(440f, 300)
    }

    fun startCallWithNumber(number: String) {
        val cleanTarget = number.replace("[^0-9]".toRegex(), "")
        val existing = _contacts.value.find { it.phoneNumber.replace("[^0-9]".toRegex(), "") == cleanTarget }
        val contact = existing ?: ContactItem("temp", number, number, 0xFF3F51B5)
        startCall(contact)
    }

    fun endCall() {
        val contact = _activeCallContact.value
        if (contact != null) {
            val log = CallLogItem(
                id = "call_${System.currentTimeMillis()}",
                contactName = contact.name,
                phoneNumber = contact.phoneNumber,
                type = CallType.OUTGOING,
                durationSeconds = _callDurationSeconds.value,
                timestamp = System.currentTimeMillis()
            )
            _callLogs.value = listOf(log) + _callLogs.value
        }
        _isInCall.value = false
        _activeCallContact.value = null
        _callDurationSeconds.value = 0
    }

    fun toggleMuteCall() { _isCallMuted.value = !_isCallMuted.value }
    fun toggleSpeaker() { _isSpeakerOn.value = !_isSpeakerOn.value }

    fun addContact(name: String, phone: String, email: String) {
        val newContact = ContactItem(
            id = "c_${System.currentTimeMillis()}",
            name = name,
            phoneNumber = phone,
            avatarColor = 0xFF000000 or (Random.nextLong(0xFFFFFF) and 0xFFFFFF),
            isFavorite = false,
            email = email
        )
        _contacts.value = _contacts.value + newContact
    }

    // --- Messages App Actions ---
    fun selectChatThread(contactId: String) {
        _selectedChatContactId.value = contactId
        // Mark as read
        _chatThreads.value = _chatThreads.value.map { thread ->
            if (thread.contactId == contactId) {
                thread.copy(
                    unreadCount = 0,
                    messages = thread.messages.map { it.copy(isRead = true) }
                )
            } else thread
        }
    }

    fun setMessageDraft(text: String) {
        _messageDraft.value = text
    }

    fun sendMessage() {
        val text = _messageDraft.value.trim()
        val contactId = _selectedChatContactId.value ?: return
        if (text.isEmpty()) return

        val newMessage = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "Me",
            text = text,
            timestamp = System.currentTimeMillis(),
            isFromMe = true,
            isRead = true
        )

        _chatThreads.value = _chatThreads.value.map { thread ->
            if (thread.contactId == contactId) {
                thread.copy(messages = thread.messages + newMessage)
            } else thread
        }
        _messageDraft.value = ""
        playTone(900f, 60)

        // Simulate intelligent realistic incoming reply after a short delay
        viewModelScope.launch {
            delay(1200)
            _isPartnerTyping.value = true
            delay(2500)
            _isPartnerTyping.value = false

            val thread = _chatThreads.value.find { it.contactId == contactId }
            val replyText = generateSimulatedReply(thread?.contactName ?: "", text)
            val incomingMsg = ChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                sender = thread?.contactName ?: "Contact",
                text = replyText,
                timestamp = System.currentTimeMillis(),
                isFromMe = false,
                isRead = _selectedChatContactId.value == contactId && _currentApp.value == AppId.MESSAGES
            )

            _chatThreads.value = _chatThreads.value.map { t ->
                if (t.contactId == contactId) {
                    t.copy(
                        messages = t.messages + incomingMsg,
                        unreadCount = if (_currentApp.value != AppId.MESSAGES) t.unreadCount + 1 else 0
                    )
                } else t
            }

            if (_currentApp.value != AppId.MESSAGES) {
                addNotification(
                    OSNotification(
                        id = "msg_notif_${System.currentTimeMillis()}",
                        appId = AppId.MESSAGES,
                        title = thread?.contactName ?: "New Message",
                        message = replyText,
                        actionLabel = "Reply"
                    )
                )
            } else {
                playTone(1050f, 100)
            }
        }
    }

    private fun generateSimulatedReply(contactName: String, userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey! Great to hear from you. How is your day going on NovaOS?"
            lower.contains("dinner") || lower.contains("food") || lower.contains("pizza") ->
                "Sounds delicious! I'm starving, let's definitely do that!"
            lower.contains("release") || lower.contains("build") || lower.contains("code") || lower.contains("app") ->
                "Awesome! Everything passed CI/CD unit tests with zero issues. Great work!"
            lower.contains("where") || lower.contains("time") ->
                "I should be there in about 15 minutes! Traffic is light."
            lower.contains("thanks") || lower.contains("thank you") ->
                "You're very welcome! Anytime! 😊"
            else ->
                "Got it! That sounds fantastic. Let's sync up more in a bit."
        }
    }

    // --- Music Player Actions ---
    fun togglePlayMusic() {
        _isPlayingMusic.value = !_isPlayingMusic.value
    }

    fun playTrack(index: Int) {
        _currentTrackIndex.value = index
        _playbackProgress.value = 0
        _isPlayingMusic.value = true
        playTone(_musicTracks.value[index].toneFrequencyHz, 250)
    }

    fun nextTrack() {
        val nextIdx = if (_isShuffle.value) {
            Random.nextInt(_musicTracks.value.size)
        } else {
            (_currentTrackIndex.value + 1) % _musicTracks.value.size
        }
        playTrack(nextIdx)
    }

    fun prevTrack() {
        val prevIdx = if (_currentTrackIndex.value > 0) _currentTrackIndex.value - 1 else _musicTracks.value.size - 1
        playTrack(prevIdx)
    }

    fun seekMusic(seconds: Int) {
        _playbackProgress.value = seconds.coerceIn(0, _musicTracks.value[_currentTrackIndex.value].durationSeconds)
    }

    fun toggleShuffle() { _isShuffle.value = !_isShuffle.value }
    fun toggleRepeat() { _isRepeat.value = !_isRepeat.value }

    // --- Notes App Actions ---
    fun setNoteSearchQuery(query: String) { _noteSearchQuery.value = query }

    fun addNote(title: String, content: String, colorHex: Long, checklists: List<Pair<String, Boolean>>) {
        val note = NoteItem(
            id = "note_${System.currentTimeMillis()}",
            title = title,
            content = content,
            colorHex = colorHex,
            isPinned = false,
            timestamp = System.currentTimeMillis(),
            checkListItems = checklists
        )
        _notes.value = listOf(note) + _notes.value
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
    }

    fun togglePinNote(id: String) {
        _notes.value = _notes.value.map { if (it.id == id) it.copy(isPinned = !it.isPinned) else it }
    }

    fun toggleCheckItem(noteId: String, itemIndex: Int) {
        _notes.value = _notes.value.map { note ->
            if (note.id == noteId) {
                val updated = note.checkListItems.toMutableList()
                if (itemIndex in updated.indices) {
                    val (text, checked) = updated[itemIndex]
                    updated[itemIndex] = text to !checked
                }
                note.copy(checkListItems = updated)
            } else note
        }
    }

    // --- Clock App Actions ---
    fun setClockTab(tab: Int) { _clockTab.value = tab }

    fun toggleAlarm(id: String) {
        _alarms.value = _alarms.value.map { if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it }
    }

    fun addAlarm(time: String, label: String) {
        val alarm = AlarmItem("alarm_${System.currentTimeMillis()}", time, label, true)
        _alarms.value = _alarms.value + alarm
    }

    fun deleteAlarm(id: String) {
        _alarms.value = _alarms.value.filter { it.id != id }
    }

    fun toggleStopwatch() {
        _stopwatchRunning.value = !_stopwatchRunning.value
    }

    fun resetStopwatch() {
        _stopwatchRunning.value = false
        _stopwatchElapsedMillis.value = 0L
        _stopwatchLaps.value = emptyList()
    }

    fun lapStopwatch() {
        if (_stopwatchRunning.value) {
            _stopwatchLaps.value = listOf(_stopwatchElapsedMillis.value) + _stopwatchLaps.value
        }
    }

    fun setTimerDuration(seconds: Int) {
        _timerTotalSeconds.value = seconds
        _timerRemainingSeconds.value = seconds
        _timerRunning.value = false
    }

    fun toggleTimer() {
        _timerRunning.value = !_timerRunning.value
    }

    fun resetTimer() {
        _timerRunning.value = false
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    // --- Camera & Gallery Actions ---
    fun setCameraMode(mode: String) { _cameraMode.value = mode }
    fun setCameraFilter(filter: String) { _cameraFilter.value = filter }
    fun setCameraZoom(zoom: Float) { _cameraZoom.value = zoom }
    fun toggleCameraFlash() { _cameraFlash.value = !_cameraFlash.value }

    fun capturePhoto() {
        playTone(1200f, 90)
        val photo = PhotoItem(
            id = "photo_${System.currentTimeMillis()}",
            title = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}",
            dateAdded = "Just now",
            drawableResName = when (_cameraFilter.value) {
                "Cyberpunk" -> "wp_cyber"
                "Abstract" -> "wp_abstract"
                else -> "wp_aurora"
            },
            isFavorite = false,
            category = "Camera",
            filterApplied = _cameraFilter.value
        )
        _photos.value = listOf(photo) + _photos.value
        addNotification(
            OSNotification(
                id = "photo_saved_${System.currentTimeMillis()}",
                appId = AppId.PHOTOS,
                title = "Photo Captured 📸",
                message = "Saved ${photo.title} to Gallery",
                actionLabel = "View"
            )
        )
    }

    fun selectPhoto(photo: PhotoItem?) { _selectedPhoto.value = photo }
    fun toggleFavoritePhoto(id: String) {
        _photos.value = _photos.value.map { if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it }
    }
    fun deletePhoto(id: String) {
        _photos.value = _photos.value.filter { it.id != id }
        if (_selectedPhoto.value?.id == id) _selectedPhoto.value = null
    }

    // --- Calculator Actions ---
    fun onCalcButton(btn: String) {
        when (btn) {
            "C" -> {
                _calcExpression.value = "0"
                _calcResult.value = ""
            }
            "⌫" -> {
                if (_calcExpression.value.length > 1) {
                    _calcExpression.value = _calcExpression.value.dropLast(1)
                } else {
                    _calcExpression.value = "0"
                }
            }
            "=" -> {
                evaluateCalc()
            }
            "±" -> {
                if (_calcExpression.value.startsWith("-")) {
                    _calcExpression.value = _calcExpression.value.drop(1)
                } else if (_calcExpression.value != "0") {
                    _calcExpression.value = "-" + _calcExpression.value
                }
            }
            "%" -> {
                val num = _calcExpression.value.toDoubleOrNull() ?: 0.0
                _calcExpression.value = (num / 100.0).toString()
            }
            "√" -> {
                val num = _calcExpression.value.toDoubleOrNull() ?: 0.0
                _calcExpression.value = sqrt(max(0.0, num)).toString()
            }
            "π" -> {
                _calcExpression.value = Math.PI.toString()
            }
            "sin" -> {
                val num = _calcExpression.value.toDoubleOrNull() ?: 0.0
                _calcExpression.value = sin(Math.toRadians(num)).toString()
            }
            "cos" -> {
                val num = _calcExpression.value.toDoubleOrNull() ?: 0.0
                _calcExpression.value = cos(Math.toRadians(num)).toString()
            }
            "tan" -> {
                val num = _calcExpression.value.toDoubleOrNull() ?: 0.0
                _calcExpression.value = tan(Math.toRadians(num)).toString()
            }
            else -> {
                if (_calcExpression.value == "0" && btn !in "+-×÷./") {
                    _calcExpression.value = btn
                } else {
                    _calcExpression.value += btn
                }
            }
        }
        playTone(700f, 40)
    }

    private fun evaluateCalc() {
        try {
            val expr = _calcExpression.value.replace("×", "*").replace("÷", "/")
            val res = simpleEval(expr)
            val resFormatted = if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res)
            _calcResult.value = resFormatted
            _calcHistory.value = listOf(_calcExpression.value to resFormatted) + _calcHistory.value
            _calcExpression.value = resFormatted
        } catch (_: Exception) {
            _calcResult.value = "Error"
        }
    }

    private fun simpleEval(expr: String): Double {
        // Basic parser for +, -, *, /
        val tokens = mutableListOf<String>()
        var current = ""
        for (c in expr) {
            if (c in "+-*/") {
                if (current.isNotEmpty()) tokens.add(current)
                tokens.add(c.toString())
                current = ""
            } else {
                current += c
            }
        }
        if (current.isNotEmpty()) tokens.add(current)

        if (tokens.isEmpty()) return 0.0
        // Multiply & divide pass
        val afterMul = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "*" || tokens[i] == "/") {
                val op = tokens[i]
                val prev = afterMul.removeAt(afterMul.size - 1).toDouble()
                val next = tokens[i + 1].toDouble()
                val res = if (op == "*") prev * next else prev / next
                afterMul.add(res.toString())
                i += 2
            } else {
                afterMul.add(tokens[i])
                i++
            }
        }

        // Add & subtract pass
        var total = afterMul[0].toDouble()
        var j = 1
        while (j < afterMul.size) {
            val op = afterMul[j]
            val next = afterMul[j + 1].toDouble()
            total = if (op == "+") total + next else total - next
            j += 2
        }
        return total
    }

    // --- Files App Actions ---
    fun createFolder(name: String) {
        val folder = FileItem(
            id = "file_${System.currentTimeMillis()}",
            name = name,
            path = "${_currentFolderPath.value}/$name",
            isDirectory = true,
            modifiedDate = "Just now"
        )
        _files.value = _files.value + folder
    }

    fun deleteFile(id: String) {
        _files.value = _files.value.filter { it.id != id }
    }

    // --- Play Store Actions ---
    fun toggleInstallStoreApp(appId: AppId) {
        _storeApps.value = _storeApps.value.map { item ->
            if (item.appId == appId) {
                val newStatus = !item.isInstalled
                if (newStatus) {
                    if (appId !in _homeApps.value) {
                        _homeApps.value = _homeApps.value + appId
                    }
                    addNotification(
                        OSNotification(
                            id = "installed_${System.currentTimeMillis()}",
                            appId = AppId.STORE,
                            title = "${item.name} Installed 🎉",
                            message = "Ready to launch from home screen.",
                            actionLabel = "Open"
                        )
                    )
                } else {
                    _homeApps.value = _homeApps.value.filter { it != appId }
                }
                item.copy(isInstalled = newStatus)
            } else item
        }
    }

    // --- Web Browser Actions ---
    fun setUrlInput(url: String) { _urlInputText.value = url }
    fun loadBrowserUrl(url: String) {
        var clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = if (clean.contains(".")) "https://$clean" else "https://nexus.search/?q=${clean.replace(" ", "+")}"
        }
        _urlInputText.value = clean
        _browserTabs.value = _browserTabs.value.map { tab ->
            if (tab.id == _currentTabId.value) tab.copy(url = clean, title = extractTitleFromUrl(clean)) else tab
        }
    }

    private fun extractTitleFromUrl(url: String): String {
        return when {
            url.contains("nexus.search") -> "Nova Search"
            url.contains("news.techhorizon") -> "Tech Horizon AI"
            url.contains("wikipedia") -> "Nova Encyclopedia"
            else -> url.replace("https://", "").take(20)
        }
    }

    fun newBrowserTab() {
        val id = "tab_${System.currentTimeMillis()}"
        val newTab = BrowserTab(id, "Nova Search", "https://nexus.search/")
        _browserTabs.value = _browserTabs.value + newTab
        _currentTabId.value = id
        _urlInputText.value = "https://nexus.search/"
    }

    fun closeBrowserTab(tabId: String) {
        if (_browserTabs.value.size > 1) {
            _browserTabs.value = _browserTabs.value.filter { it.id != tabId }
            if (_currentTabId.value == tabId) {
                _currentTabId.value = _browserTabs.value.last().id
                _urlInputText.value = _browserTabs.value.last().url
            }
        }
    }

    // --- Terminal Actions ---
    fun executeTerminalCommand(cmd: String) {
        val input = cmd.trim()
        val currentLines = _terminalOutput.value.toMutableList()
        currentLines.add("nexus@novaos-pixel10:~$ $input")

        val parts = input.split(" ")
        val command = parts[0].lowercase()

        when (command) {
            "help" -> {
                currentLines.add("Available commands:")
                currentLines.add("  neofetch   - Display system hardware & OS info")
                currentLines.add("  ps / top   - Show running background processes")
                currentLines.add("  free       - Memory and RAM allocation stats")
                currentLines.add("  uptime     - Device system uptime and load")
                currentLines.add("  date       - Show current timestamp")
                currentLines.add("  ls         - List files in current directory")
                currentLines.add("  whoami     - Current logged-in user")
                currentLines.add("  ping       - Test network connectivity")
                currentLines.add("  matrix     - Run digital rain simulation")
                currentLines.add("  clear      - Clear console buffer")
            }
            "neofetch" -> {
                currentLines.add("  __  __  _____  __      __          ____   _____ ")
                currentLines.add(" |  \\/  |/ ____| \\ \\    / /\\        / __ \\ / ____|")
                currentLines.add(" | \\  / | |  __   \\ \\  / /  \\      | |  | | (___  ")
                currentLines.add(" | |\\/| | | |_ |   \\ \\/ / /\\ \\     | |  | |\\___ \\ ")
                currentLines.add(" | |  | | |__| |    \\  / ____ \\    | |__| |____) |")
                currentLines.add(" |_|  |_|\\_____|     \\/_/    \\_\\    \\____/|_____/ ")
                currentLines.add(" -------------------------------------------------")
                currentLines.add(" OS: NovaOS (Baklava Edition) aarch64")
                currentLines.add(" Host: Google Pixel 10 Pro Hyper-Virtualizer")
                currentLines.add(" Kernel: android-novaos-mainline")
                currentLines.add(" UI Framework: Jetpack Compose Material 3 Dynamic")
                currentLines.add(" Memory: 4.8GB / 12.0GB (40% utilized)")
                currentLines.add(" CPU: Tensor G5 (8 cores @ 3.40GHz)")
                currentLines.add(" Battery: ${_batteryLevel.value}% [${if (_isCharging.value) "Charging ⚡" else "Discharging"}]")
            }
            "ps", "top" -> {
                currentLines.add("PID   USER     %CPU  %MEM  COMMAND")
                currentLines.add("1     root      0.1   0.2  /init (systemd)")
                currentLines.add("452   system    1.2   4.5  com.novaos.systemui")
                currentLines.add("620   system    0.8   2.1  com.novaos.launcher")
                currentLines.add("1032  nexus     2.4   6.8  com.novaos.runtime")
                for ((idx, app) in _runningApps.value.withIndex()) {
                    currentLines.add("${2000 + idx * 40}  nexus     0.5   3.2  ${app.packageName}")
                }
            }
            "free" -> {
                currentLines.add("               total        used        free      shared  buff/cache   available")
                currentLines.add("Mem:        12288000     4915200     5120000      204800     2252800     6963200")
                currentLines.add("Swap:        4096000           0     4096000")
            }
            "uptime" -> {
                currentLines.add(" 10:04:12 up 4 days, 18:22, 1 user, load average: 0.14, 0.22, 0.18")
            }
            "date" -> {
                currentLines.add(Date().toString())
            }
            "whoami" -> {
                currentLines.add("nexus (uid=1000 gid=1000 groups=wheel,audio,video,storage)")
            }
            "ls" -> {
                currentLines.add("Documents/  Downloads/  Music/  Pictures/  System/  nova_kernel_config.json")
            }
            "ping" -> {
                currentLines.add("PING 8.8.8.8 (8.8.8.8) 56(84) bytes of data.")
                currentLines.add("64 bytes from 8.8.8.8: icmp_seq=1 ttl=118 time=12.4 ms")
                currentLines.add("64 bytes from 8.8.8.8: icmp_seq=2 ttl=118 time=11.9 ms")
                currentLines.add("--- 8.8.8.8 ping statistics --- 2 packets transmitted, 0% packet loss")
            }
            "matrix" -> {
                currentLines.add("01011001 01100101 01110011 00100000 01001110 01101111 01110110 01100001")
                currentLines.add("Wake up, Neo... NovaOS has you. Follow the white droid. 🤖")
            }
            "clear" -> {
                _terminalOutput.value = listOf("nexus@novaos-pixel10:~$ ")
                return
            }
            "" -> { /* no-op */ }
            else -> {
                currentLines.add("nova-sh: command not found: $command. Type 'help' for commands.")
            }
        }
        currentLines.add("nexus@novaos-pixel10:~$ ")
        _terminalOutput.value = currentLines
    }

    // --- Arcade Games Logic (2048, Snake, TicTacToe) ---
    fun setActiveGame(game: String) {
        _activeGame.value = game
        if (game == "2048") init2048Game()
        if (game == "SNAKE") startSnakeGame()
        if (game == "TICTACTOE") resetTicTacToe()
    }

    // 2048 Logic
    fun init2048Game() {
        val grid = Array(4) { IntArray(4) }
        _score2048.value = 0
        _gameOver2048.value = false
        addRandomTile2048(grid)
        addRandomTile2048(grid)
        _grid2048.value = grid
    }

    private fun addRandomTile2048(grid: Array<IntArray>) {
        val empty = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) empty.add(Pair(r, c))
            }
        }
        if (empty.isNotEmpty()) {
            val (r, c) = empty.random()
            grid[r][c] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    fun move2048(dir: String) { // "LEFT", "RIGHT", "UP", "DOWN"
        if (_gameOver2048.value) return
        val current = Array(4) { r -> _grid2048.value[r].clone() }
        var moved = false
        var scoreAdd = 0

        fun slideAndMergeRow(row: IntArray): Pair<IntArray, Int> {
            val nonZero = row.filter { it != 0 }.toMutableList()
            var added = 0
            val res = mutableListOf<Int>()
            var i = 0
            while (i < nonZero.size) {
                if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                    val merged = nonZero[i] * 2
                    res.add(merged)
                    added += merged
                    i += 2
                } else {
                    res.add(nonZero[i])
                    i++
                }
            }
            while (res.size < 4) res.add(0)
            return Pair(res.toIntArray(), added)
        }

        when (dir) {
            "LEFT" -> {
                for (r in 0..3) {
                    val (newRow, added) = slideAndMergeRow(current[r])
                    if (!newRow.contentEquals(current[r])) moved = true
                    current[r] = newRow
                    scoreAdd += added
                }
            }
            "RIGHT" -> {
                for (r in 0..3) {
                    val reversed = current[r].reversedArray()
                    val (newRow, added) = slideAndMergeRow(reversed)
                    val unreversed = newRow.reversedArray()
                    if (!unreversed.contentEquals(current[r])) moved = true
                    current[r] = unreversed
                    scoreAdd += added
                }
            }
            "UP" -> {
                for (c in 0..3) {
                    val col = IntArray(4) { r -> current[r][c] }
                    val (newCol, added) = slideAndMergeRow(col)
                    for (r in 0..3) {
                        if (current[r][c] != newCol[r]) moved = true
                        current[r][c] = newCol[r]
                    }
                    scoreAdd += added
                }
            }
            "DOWN" -> {
                for (c in 0..3) {
                    val col = IntArray(4) { r -> current[r][c] }.reversedArray()
                    val (newCol, added) = slideAndMergeRow(col)
                    val unreversed = newCol.reversedArray()
                    for (r in 0..3) {
                        if (current[r][c] != unreversed[r]) moved = true
                        current[r][c] = unreversed[r]
                    }
                    scoreAdd += added
                }
            }
        }

        if (moved) {
            addRandomTile2048(current)
            _grid2048.value = current
            _score2048.value += scoreAdd
            if (_score2048.value > _bestScore2048.value) {
                _bestScore2048.value = _score2048.value
            }
            playTone(600f + scoreAdd * 2f, 40)
        }
    }

    // Snake Logic
    fun startSnakeGame() {
        _snakeBody.value = listOf(Pair(6, 6), Pair(6, 7), Pair(6, 8))
        _snakeFood.value = Pair(Random.nextInt(1, 14), Random.nextInt(1, 14))
        _snakeDir.value = Pair(0, -1)
        _snakeScore.value = 0
        _isSnakeGameOver.value = false
        _isSnakeRunning.value = true

        viewModelScope.launch {
            while (_isSnakeRunning.value && !_isSnakeGameOver.value) {
                delay(200)
                stepSnake()
            }
        }
    }

    fun turnSnake(dx: Int, dy: Int) {
        val current = _snakeDir.value
        // Prevent 180-degree turn
        if (current.first + dx != 0 || current.second + dy != 0) {
            _snakeDir.value = Pair(dx, dy)
        }
    }

    private fun stepSnake() {
        val head = _snakeBody.value.first()
        val dir = _snakeDir.value
        val newHead = Pair((head.first + dir.first + 16) % 16, (head.second + dir.second + 16) % 16)

        // Check self-collision
        if (_snakeBody.value.contains(newHead)) {
            _isSnakeGameOver.value = true
            _isSnakeRunning.value = false
            playTone(220f, 300)
            return
        }

        val newBody = mutableListOf(newHead)
        if (newHead == _snakeFood.value) {
            _snakeScore.value += 10
            newBody.addAll(_snakeBody.value) // Grow
            _snakeFood.value = Pair(Random.nextInt(0, 16), Random.nextInt(0, 16))
            playTone(880f, 60)
        } else {
            newBody.addAll(_snakeBody.value.dropLast(1))
        }
        _snakeBody.value = newBody
    }

    // Tic-Tac-Toe Logic
    fun resetTicTacToe() {
        _tttBoard.value = List(9) { "" }
        _tttWinner.value = null
    }

    fun makeTicTacToeMove(index: Int) {
        if (_tttBoard.value[index].isNotEmpty() || _tttWinner.value != null) return

        val newBoard = _tttBoard.value.toMutableList()
        newBoard[index] = "X"
        playTone(600f, 50)
        _tttBoard.value = newBoard

        val win = checkTTTWinner(newBoard)
        if (win != null) {
            _tttWinner.value = win
            return
        }

        // Smart AI Move (O)
        viewModelScope.launch {
            delay(350)
            val available = newBoard.indices.filter { newBoard[it].isEmpty() }
            if (available.isNotEmpty()) {
                val aiIndex = findBestTTTMove(newBoard, available)
                newBoard[aiIndex] = "O"
                playTone(450f, 50)
                _tttBoard.value = newBoard
                _tttWinner.value = checkTTTWinner(newBoard)
            }
        }
    }

    private fun findBestTTTMove(board: List<String>, available: List<Int>): Int {
        // Check if AI can win
        for (i in available) {
            val copy = board.toMutableList()
            copy[i] = "O"
            if (checkTTTWinner(copy) == "O") return i
        }
        // Block player win
        for (i in available) {
            val copy = board.toMutableList()
            copy[i] = "X"
            if (checkTTTWinner(copy) == "X") return i
        }
        // Take center or random
        return if (available.contains(4)) 4 else available.random()
    }

    private fun checkTTTWinner(b: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            if (b[line[0]].isNotEmpty() && b[line[0]] == b[line[1]] && b[line[1]] == b[line[2]]) {
                return b[line[0]]
            }
        }
        return if (b.all { it.isNotEmpty() }) "DRAW" else null
    }

    // --- Setup Wizard Controls ---
    fun startSetup() {
        _setupStep.value = SetupStep.WELCOME
        _isSetupActive.value = true
    }

    fun nextSetupStep() {
        val next = when (_setupStep.value) {
            SetupStep.WELCOME -> SetupStep.NETWORK
            SetupStep.NETWORK -> SetupStep.ACCOUNT
            SetupStep.ACCOUNT -> SetupStep.SECURITY
            SetupStep.SECURITY -> SetupStep.PERSONALIZE
            SetupStep.PERSONALIZE -> SetupStep.FINISHING
            SetupStep.FINISHING -> {
                finishSetup()
                return
            }
        }
        _setupStep.value = next
    }

    fun prevSetupStep() {
        val prev = when (_setupStep.value) {
            SetupStep.WELCOME -> {
                if (_isSetupCompleted.value) {
                    _isSetupActive.value = false
                }
                return
            }
            SetupStep.NETWORK -> SetupStep.WELCOME
            SetupStep.ACCOUNT -> SetupStep.NETWORK
            SetupStep.SECURITY -> SetupStep.ACCOUNT
            SetupStep.PERSONALIZE -> SetupStep.SECURITY
            SetupStep.FINISHING -> SetupStep.PERSONALIZE
        }
        _setupStep.value = prev
    }

    fun setSetupStep(step: SetupStep) {
        _setupStep.value = step
    }

    fun connectWifi(networkName: String) {
        _connectedWifi.value = networkName
        _wifiEnabled.value = true
    }

    fun finishSetup() {
        _isSetupCompleted.value = true
        _isSetupActive.value = false
        prefs.edit().putBoolean("has_completed_setup", true).apply()
        addNotification(
            OSNotification(
                id = "notif_welcome_${System.currentTimeMillis()}",
                appId = AppId.ACCOUNT,
                title = "Welcome to NovaOS, ${_userAccount.value.firstName}!",
                message = "Your NovaOS account (${_userAccount.value.email}) is active with 100 GB Cloud Storage.",
                actionLabel = "View Account"
            )
        )
    }

    fun skipSetup() {
        _isSetupCompleted.value = true
        _isSetupActive.value = false
        prefs.edit().putBoolean("has_completed_setup", true).apply()
    }

    fun removeHomeApp(appId: AppId) {
        _homeApps.value = _homeApps.value.filter { it != appId }
    }

    fun reorderHomeApps(fromIndex: Int, toIndex: Int) {
        val currentList = _homeApps.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val element = currentList.removeAt(fromIndex)
            currentList.add(toIndex, element)
            _homeApps.value = currentList
        }
    }

    fun addHomeApp(appId: AppId) {
        if (appId !in _homeApps.value) {
            _homeApps.value = _homeApps.value + appId
        }
    }

    fun resetHomeApps() {
        _homeApps.value = listOf(
            AppId.SETTINGS,
            AppId.PHOTOS,
            AppId.MUSIC,
            AppId.CLOCK,
            AppId.NOTES,
            AppId.CALCULATOR,
            AppId.FILES,
            AppId.STORE,
            AppId.ARCADE,
            AppId.TERMINAL,
            AppId.TASKS,
            AppId.ACCOUNT
        )
    }

    // --- Smart Assistant Panel Controls ---
    fun setAssistantOpen(open: Boolean) {
        if (_assistantEnabled.value) {
            _isAssistantOpen.value = open
        }
    }

    fun toggleAssistantWidget(widgetId: String) {
        val current = _assistantWidgets.value.toMutableSet()
        if (current.contains(widgetId)) {
            current.remove(widgetId)
        } else {
            current.add(widgetId)
        }
        _assistantWidgets.value = current
    }

    fun setAssistantEnabled(enabled: Boolean) {
        _assistantEnabled.value = enabled
        if (!enabled) {
            _isAssistantOpen.value = false
        }
    }

    // --- Sideload App Installation ---
    fun installAppFromUrl(name: String, url: String) {
        _sideloadedAppName.value = name
        _sideloadedAppUrl.value = url
        _isSideloadInstalled.value = true
        
        // Add to home apps list so it instantly appears on the home screen!
        if (AppId.CUSTOM_APP !in _homeApps.value) {
            _homeApps.value = _homeApps.value + AppId.CUSTOM_APP
        }

        addNotification(
            OSNotification(
                id = "sideload_${System.currentTimeMillis()}",
                appId = AppId.STORE,
                title = "$name Installed 🎉",
                message = "Ready to launch from Home Screen.",
                actionLabel = "Open"
            )
        )
    }

    fun uninstallSideloadedApp() {
        _isSideloadInstalled.value = false
        _homeApps.value = _homeApps.value.filter { it != AppId.CUSTOM_APP }
    }

    fun resetToFactorySetup() {
        // 1. Clear persistent local storage preferences
        prefs.edit().clear().apply()

        // 2. Reset onboarding & Setup Wizard states
        _isSetupCompleted.value = false
        _setupStep.value = SetupStep.WELCOME
        _isSetupActive.value = true

        // 3. Reset launcher apps & store inventory
        resetHomeApps()
        _storeApps.value = OSRepository.getInitialStoreApps()

        // 4. Reset Sideload App stats
        _isSideloadInstalled.value = false
        _sideloadedAppName.value = "Sideload App"
        _sideloadedAppUrl.value = ""

        // 5. Clear status bar notifications
        _notifications.value = emptyList()

        // 6. Reset built-in apps databases
        _notes.value = OSRepository.getInitialNotes()
        _tasks.value = OSRepository.getInitialTasks()

        // 7. Reset games parameters
        _score2048.value = 0
        _bestScore2048.value = 2480

        // 8. Restore default hardware telemetry & system values
        _wifiEnabled.value = true
        _bluetoothEnabled.value = true
        _connectedWifi.value = "Nova-Fiber_Ultra_5G"
        _batteryLevel.value = 88
        _isCharging.value = false
        _batteryStatusText.value = "Discharging"
        _dndEnabled.value = false
        _flashlightOn.value = false
        _isDeveloperModeUnlocked.value = false
        _isDexModeActive.value = false
        _airplaneMode.value = false
        _autoRotate.value = true
        _batterySaver.value = false
        _nightLight.value = false

        // 9. Reset user account profile
        _userAccount.value = OSRepository.getDefaultUserAccount()
    }

    // --- User Account Controls ---
    fun createOrUpdateAccount(
        firstName: String,
        lastName: String,
        email: String,
        username: String,
        avatarEmoji: String,
        avatarColor: Long,
        backupEnabled: Boolean,
        bio: String = _userAccount.value.bio
    ) {
        val updated = _userAccount.value.copy(
            firstName = firstName.trim().ifEmpty { "Alex" },
            lastName = lastName.trim().ifEmpty { "Rivera" },
            email = email.trim().ifEmpty { "${username.lowercase()}@novaos.net" },
            username = username.trim().ifEmpty { "alexrivera" },
            avatarEmoji = avatarEmoji,
            avatarColor = avatarColor,
            backupEnabled = backupEnabled,
            bio = bio,
            lastSyncedTimestamp = System.currentTimeMillis()
        )
        _userAccount.value = updated
        saveUserAccount(updated)
    }

    fun syncAccount() {
        viewModelScope.launch {
            _isSyncingAccount.value = true
            delay(1200)
            _userAccount.value = _userAccount.value.copy(
                lastSyncedTimestamp = System.currentTimeMillis()
            )
            _isSyncingAccount.value = false
        }
    }

    private fun loadSavedUserAccount(): UserAccount {
        return UserAccount(
            firstName = prefs.getString("user_first_name", "Alex") ?: "Alex",
            lastName = prefs.getString("user_last_name", "Rivera") ?: "Rivera",
            email = prefs.getString("user_email", "alex.rivera@novaos.net") ?: "alex.rivera@novaos.net",
            username = prefs.getString("user_username", "alexrivera") ?: "alexrivera",
            avatarEmoji = prefs.getString("user_avatar_emoji", "🚀") ?: "🚀",
            avatarColor = prefs.getLong("user_avatar_color", 0xFF1976D2),
            backupEnabled = prefs.getBoolean("user_backup_enabled", true),
            bio = prefs.getString("user_bio", "NovaOS Explorer & Mobile Creator") ?: "NovaOS Explorer & Mobile Creator"
        )
    }

    private fun saveUserAccount(account: UserAccount) {
        prefs.edit()
            .putString("user_first_name", account.firstName)
            .putString("user_last_name", account.lastName)
            .putString("user_email", account.email)
            .putString("user_username", account.username)
            .putString("user_avatar_emoji", account.avatarEmoji)
            .putLong("user_avatar_color", account.avatarColor)
            .putBoolean("user_backup_enabled", account.backupEnabled)
            .putString("user_bio", account.bio)
            .apply()
    }

    // --- Nova Tasks App Controls ---
    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val updatedState = !task.isCompleted
                playTone(if (updatedState) 800f else 500f, 60)
                task.copy(isCompleted = updatedState)
            } else task
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        priority: TaskPriority,
        dueDate: String,
        subtasks: List<String> = emptyList()
    ) {
        val newTask = TaskItem(
            id = "task_${System.currentTimeMillis()}",
            title = title,
            description = description,
            category = category,
            priority = priority,
            dueDate = dueDate,
            isCompleted = false,
            subtasks = subtasks.filter { it.isNotBlank() }.map { it to false },
            createdTimestamp = System.currentTimeMillis()
        )
        _tasks.value = listOf(newTask) + _tasks.value
        playTone(700f, 60)
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
        playTone(350f, 50)
    }

    fun toggleSubtask(taskId: String, subtaskIndex: Int) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId && subtaskIndex in task.subtasks.indices) {
                val updatedSubtasks = task.subtasks.toMutableList()
                val current = updatedSubtasks[subtaskIndex]
                updatedSubtasks[subtaskIndex] = current.first to !current.second
                task.copy(subtasks = updatedSubtasks)
            } else task
        }
    }

    fun setTaskFilter(filter: String) {
        _taskFilter.value = filter
    }

    fun setTaskSearchQuery(query: String) {
        _taskSearchQuery.value = query
    }

    // --- Developer Mode Controls ---
    fun unlockDeveloperMode() {
        _isDeveloperModeUnlocked.value = true
        prefs.edit().putBoolean("dev_mode_unlocked", true).apply()
        addNotification(
            OSNotification(
                id = "notif_dev_${System.currentTimeMillis()}",
                appId = AppId.SETTINGS,
                title = "{ } Developer Mode Unlocked",
                message = "You are now a vos developer! Access advanced system toggles in Settings -> Developer Options.",
                actionLabel = "Open Developer Options"
            )
        )
    }

    fun setDeveloperModeUnlocked(unlocked: Boolean) {
        _isDeveloperModeUnlocked.value = unlocked
        prefs.edit().putBoolean("dev_mode_unlocked", unlocked).apply()
    }

    fun toggleUsbDebugging() {
        val next = !_usbDebuggingEnabled.value
        _usbDebuggingEnabled.value = next
        prefs.edit().putBoolean("dev_usb_debugging", next).apply()
    }

    fun toggleFpsHud() {
        val next = !_showFpsHud.value
        _showFpsHud.value = next
        prefs.edit().putBoolean("dev_show_fps", next).apply()
    }

    fun toggleTouchPointer() {
        val next = !_showTouchPointer.value
        _showTouchPointer.value = next
        prefs.edit().putBoolean("dev_show_touches", next).apply()
    }

    fun setAnimatorScale(scale: Float) {
        _animatorScale.value = scale
        prefs.edit().putFloat("dev_anim_scale", scale).apply()
    }

    fun toggleStrictMode() {
        val next = !_strictModeEnabled.value
        _strictModeEnabled.value = next
        prefs.edit().putBoolean("dev_strict_mode", next).apply()
    }

    fun sendTestNotification() {
        addNotification(
            OSNotification(
                id = "notif_test_${System.currentTimeMillis()}",
                appId = AppId.SETTINGS,
                title = "🧪 Dev Test Notification",
                message = "Triggered from Developer Options at ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}",
                actionLabel = "Dev Options"
            )
        )
    }
}
