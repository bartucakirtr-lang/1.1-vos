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
    CUSTOM_APP("Sideload App", "com.novaos.customsideload", false),
    YOUTUBE("YouTube", "com.google.android.youtube", false),
    INSTAGRAM("Instagram", "com.instagram.android", false),
    GOOGLE("Google", "com.google.android.googlequicksearchbox", false),
    WHATSAPP("WhatsApp", "com.whatsapp", false),
    SPOTIFY("Spotify", "com.spotify.music", false),
    MAPS("Google Maps", "com.google.android.apps.maps", false),
    NETFLIX("Netflix", "com.netflix.mediaclient", false),
    TIKTOK("TikTok", "com.zhiliaoapp.musically", false)
}

enum class LockType {
    SWIPE, PIN, BIOMETRIC
}

enum class NightLightSchedule(val title: String, val subtitle: String) {
    OFF("None", "Turn on or off manually"),
    SUNSET_TO_SUNRISE("Sunset to Sunrise", "Automatically on from 7:00 PM to 6:30 AM"),
    CUSTOM("Custom Schedule", "Turns on at your customized times")
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
    WALLPAPER_DYNAMIC_1("Wallpaper Tonal", 0xFF00E676, 0xFF00B0FF),
    WALLPAPER_DYNAMIC_2("Wallpaper Vibrant", 0xFF00F5FF, 0xFFFF007F),
    WALLPAPER_DYNAMIC_3("Wallpaper Muted", 0xFF81C784, 0xFF64B5F6),
    WALLPAPER_DYNAMIC_4("Wallpaper Dual-Tone", 0xFFFFB74D, 0xFF00E5FF),
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

enum class IconStyle(
    val title: String,
    val description: String
) {
    SQUIRCLE("Squircle (One UI)", "Yumuşak modern kıvrımlı kare"),
    CIRCLE("Daire (Pixel Pure)", "Tam yuvarlak modern minimalist"),
    ROUNDED_SQUARE("iOS Yuvarlatılmış Kare", "Dengeli köşeli yumuşak stil"),
    TEARDROP("Teardrop Damla", "Dinamik asimetrik damla"),
    HEXAGON("Fütüristik Altıgen", "Teknolojik poligon formu"),
    GLASSMORPHISM("Buzlu Cam (Glass)", "Işıltılı yarı saydam cam"),
    NEON_GLOW("Siber Neon", "Parlak neon kenarlıklı"),
    NEUMORPHIC("3D Kabartmalı", "Derinlikli yumuşak 3D gölgeli")
}

enum class HomeWidgetType(
    val title: String,
    val description: String
) {
    CLOCK("Material Saat & Tarih", "Büyük dijital saat, tarih ve alarm durumu"),
    WEATHER("Hava Durumu", "Anlık sıcaklık, durum ve haftalık tahmin"),
    MUSIC("SoundWave Müzik Çalar", "Parça bilgisi, albüm kapağı ve medya kontrolleri"),
    NOTES("Hızlı Notlar & Fikirler", "En son alınan not ve yapılacaklar listesi"),
    BATTERY_WELLBEING("Pil Sağlığı & Dijital Denge", "Pil yüzdesi, sıcaklık ve ekran süresi"),
    QUICK_TOGGLES("Hızlı Ayar Anahtarları", "Wi-Fi, Bluetooth, Fener ve Rahatsız Etmeyin")
}

enum class WallpaperType(val title: String, val drawableResName: String, val isLight: Boolean = false) {
    DEVICE_SYSTEM("Telefon Arka Planı (Sistem)", ""),
    AURORA("Aurora Borealis", "wp_aurora"),
    CYBERPUNK("Cyber City", "wp_cyber"),
    ABSTRACT("Material Wave", "wp_abstract"),
    DEEP_SPACE("Deep Nebula", "wp_aurora"),
    FOREST_MIST("Forest Mist", "wp_abstract"),
    OCEAN_SUNRISE("Ocean Sunrise", "wp_aurora"),
    MINIMAL_GRADIENT("Velvet Gradient", "")
}

data class ExtractedPaletteSwatch(
    val id: String,
    val title: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val tertiaryColor: Long,
    val surfaceColor: Long,
    val containerColor: Long,
    val paletteEnum: ThemePalette
)

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

enum class HomeLayoutDesign(val title: String) {
    PIXEL_MODERN("Pixel Modern"),
    CARD_DECK("Card Deck"),
    COMPACT_GRID("Compact Grid")
}

data class AppFolder(
    val id: String,
    val name: String,
    val appIds: List<AppId>,
    val colorHex: Long = 0xFF1976D2
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
    val iconColor: Long,
    val version: String = "1.0.0",
    val installedVersion: String = "1.0.0",
    val availableUpdateVersion: String? = null,
    val updateChangelog: String? = null,
    val updateSizeMb: Int = 14,
    val isUpdating: Boolean = false,
    val updateProgress: Float = 0f,
    val isInstalling: Boolean = false,
    val installProgress: Float = 0f,
    val permissions: List<String> = listOf("Internet Access", "Network State", "Notifications"),
    val dataSizeBytes: Long = 18_400_000L,
    val cacheSizeBytes: Long = 6_200_000L,
    val lastUpdatedDate: String = "September 2026",
    val isSystemPackage: Boolean = false,
    val screenshots: List<String> = emptyList(),
    val ratingCount: String = "45.2K",
    val minSdk: String = "Android 14+ (Baklava Ready)"
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

