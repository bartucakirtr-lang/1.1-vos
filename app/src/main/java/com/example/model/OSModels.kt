package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppId(
    val title: String,
    val packageName: String,
    val isSystemApp: Boolean = true
) {
    PHONE("Phone", "com.novaos.dialer"),
    MESSAGES("Messages", "com.novaos.messaging"),
    SETTINGS("Settings", "com.novaos.settings"),
    BROWSER("Chrome", "com.novaos.browser"),
    CAMERA("Camera", "com.novaos.camera"),
    PHOTOS("Photos", "com.novaos.gallery"),
    MUSIC("SoundWave", "com.novaos.music"),
    CLOCK("Clock", "com.novaos.clock"),
    NOTES("Notes", "com.novaos.notes"),
    CALCULATOR("Calculator", "com.novaos.calculator"),
    FILES("Files", "com.novaos.files"),
    STORE("Play Store", "com.novaos.vending"),
    ARCADE("Arcade Games", "com.novaos.arcade"),
    TERMINAL("Terminal", "com.novaos.terminal"),
    WEATHER("Weather", "com.novaos.weather"),
    TASKS("Tasks", "com.novaos.tasks"),
    ACCOUNT("Nova Account", "com.novaos.account"),
    CUSTOM_APP("Sideload App", "com.novaos.customsideload", false)
}

enum class LockType {
    SWIPE, PIN, BIOMETRIC
}

enum class ClockStyle {
    PIXEL_BOLD, MINIMAL, ANALOG, DUAL_COLOR
}

enum class ThemeMode(val title: String, val subtitle: String) {
    LIGHT("Light", "Clean bright theme for daytime"),
    DARK("Dark", "High contrast night palette"),
    SYSTEM("Follow System", "Matches device system theme")
}

enum class ThemePalette(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    OCEAN_BLUE("Ocean Blue", 0xFF1976D2, 0xFF0288D1),
    ANDROID_GREEN("Android Green", 0xFF2E7D32, 0xFF43A047),
    SUNSET_ORANGE("Sunset Orange", 0xFFE65100, 0xFFF57C00),
    LAVENDER_PURPLE("Lavender Purple", 0xFF7B1FA2, 0xFF9C27B0),
    CYBERPUNK_NEON("Cyberpunk Neon", 0xFF00E5FF, 0xFFFF007F),
    MONOCHROME("Monochrome Minimal", 0xFF37474F, 0xFF607D8B)
}

enum class NavMode {
    GESTURE, THREE_BUTTON
}

enum class WallpaperType(val title: String, val drawableResName: String, val isLight: Boolean = false) {
    AURORA("Aurora Borealis", "wp_aurora"),
    CYBERPUNK("Cyber City", "wp_cyber"),
    ABSTRACT("Material Wave", "wp_abstract"),
    DEEP_SPACE("Deep Nebula", "wp_aurora"),
    MINIMAL_GRADIENT("Velvet Gradient", "")
}

data class OSNotification(
    val id: String,
    val appId: AppId,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionLabel: String? = null,
    val iconName: String = "info"
)

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarColor: Long,
    val isFavorite: Boolean = false,
    val email: String = ""
)

data class CallLogItem(
    val id: String,
    val contactName: String,
    val phoneNumber: String,
    val type: CallType,
    val durationSeconds: Int,
    val timestamp: Long
)

enum class CallType {
    INCOMING, OUTGOING, MISSED
}

data class ChatMessage(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val isRead: Boolean = true
)

data class ChatThread(
    val contactId: String,
    val contactName: String,
    val phoneNumber: String,
    val avatarColor: Long,
    val messages: List<ChatMessage>,
    val unreadCount: Int = 0
)

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val colorHex: Long,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val checkListItems: List<Pair<String, Boolean>> = emptyList()
)

data class AlarmItem(
    val id: String,
    val timeFormatted: String,
    val label: String,
    val isEnabled: Boolean,
    val daysOfWeek: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
)

data class WorldClockCity(
    val cityName: String,
    val country: String,
    val timeDiffHours: Int,
    val currentFormattedTime: String,
    val isDaytime: Boolean
)

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val coverGradientStart: Long,
    val coverGradientEnd: Long,
    val toneFrequencyHz: Float = 440f,
    val lyrics: List<String> = emptyList()
)

data class PhotoItem(
    val id: String,
    val title: String,
    val dateAdded: String,
    val drawableResName: String,
    val isFavorite: Boolean = false,
    val category: String = "Camera",
    val filterApplied: String = "Original",
    val fileUri: String? = null,
    val isVideo: Boolean = false
)

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String,
    val isIncognito: Boolean = false
)

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long = 0,
    val extension: String = "",
    val modifiedDate: String = "Today"
)

data class StoreAppItem(
    val appId: AppId,
    val name: String,
    val developer: String,
    val rating: Float,
    val sizeMb: Int,
    val downloads: String,
    val description: String,
    val isInstalled: Boolean,
    val category: String,
    val iconColor: Long
)

data class WeatherForecast(
    val dayOfWeek: String,
    val condition: String,
    val tempHigh: Int,
    val tempLow: Int,
    val rainChancePercent: Int,
    val icon: String
)

data class HourlyForecast(
    val time: String,
    val temp: Int,
    val condition: String,
    val icon: String
)

data class UserAccount(
    val id: String = "user_default_01",
    val firstName: String = "Alex",
    val lastName: String = "Rivera",
    val email: String = "alex.rivera@novaos.net",
    val username: String = "alexrivera",
    val avatarEmoji: String = "🚀",
    val avatarColor: Long = 0xFF1976D2,
    val backupEnabled: Boolean = true,
    val storageUsedGb: Double = 14.8,
    val totalStorageGb: Double = 100.0,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val bio: String = "NovaOS Explorer & Mobile Creator"
)

enum class TaskPriority(val label: String, val colorHex: Long) {
    LOW("Low", 0xFF4CAF50),
    MEDIUM("Medium", 0xFFFF9800),
    HIGH("High", 0xFFE53935)
}

data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String = "Today",
    val isCompleted: Boolean = false,
    val subtasks: List<Pair<String, Boolean>> = emptyList(),
    val createdTimestamp: Long = System.currentTimeMillis()
)

enum class SetupStep {
    WELCOME,
    NETWORK,
    ACCOUNT,
    SECURITY,
    PERSONALIZE,
    FINISHING
}

