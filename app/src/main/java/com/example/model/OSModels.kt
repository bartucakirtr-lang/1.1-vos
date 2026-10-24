package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppId(
    val title: String,
    val packageName: String,
    val isSystemApp: Boolean = true,
    val category: String = "Utilities",
    val iconColorHex: Long = 0xFF1976D2
) {
    // 1. Core System Apps (1-20)
    PHONE("Phone", "com.novaos.dialer", true, "Communication", 0xFF00C853),
    CONTACTS("Contacts", "com.novaos.contacts", true, "Communication", 0xFF00897B),
    MESSAGES("Messages", "com.novaos.messaging", true, "Communication", 0xFF1E88E5),
    SETTINGS("Settings", "com.novaos.settings", true, "Utilities", 0xFF546E7A),
    BROWSER("Chrome", "com.novaos.browser", true, "Communication", 0xFFFB8C00),
    CAMERA("Camera", "com.novaos.camera", true, "Media", 0xFFE53935),
    PHOTOS("Photos", "com.novaos.gallery", true, "Media", 0xFF8E24AA),
    MUSIC("SoundWave", "com.novaos.music", true, "Media", 0xFF7C4DFF),
    CLOCK("Clock", "com.novaos.clock", true, "Utilities", 0xFF00ACC1),
    NOTES("Notes", "com.novaos.notes", true, "Productivity", 0xFFFFB300),
    CALCULATOR("Calculator", "com.novaos.calculator", true, "Utilities", 0xFF3949AB),
    FILES("Files", "com.novaos.files", true, "Utilities", 0xFF43A047),
    STORE("Play Store", "com.novaos.vending", true, "Utilities", 0xFF00B0FF),
    ARCADE("Arcade Games", "com.novaos.arcade", true, "Games", 0xFFFF5722),
    TERMINAL("Terminal", "com.novaos.terminal", true, "Developer", 0xFF212121),
    WEATHER("Weather", "com.novaos.weather", true, "Utilities", 0xFF039BE5),
    TASKS("Tasks", "com.novaos.tasks", true, "Productivity", 0xFF43A047),
    ACCOUNT("Nova Account", "com.novaos.account", true, "Utilities", 0xFF1976D2),
    THEMES("Temalar", "com.novaos.themes", true, "Utilities", 0xFF8E24AA),
    PAINT("Çizim", "com.novaos.paint", true, "Media", 0xFFFF4081),
    PACKAGE_MANAGER("Paket Yöneticisi", "com.novaos.packagemanager", true, "Utilities", 0xFF00897B),
    VROX("Vrox Security", "com.novaos.vrox", true, "Security", 0xFFD32F2F),
    VDESK("vdesk", "com.novaos.vdesk", true, "Productivity", 0xFF2563EB),
    WALLPAPERS("Duvar Kağıtları", "com.novaos.wallpapers", true, "Personalization", 0xFF7C4DFF),
    CUSTOM_APP("Sideload App", "com.novaos.customsideload", false, "Developer", 0xFFE91E63),

    // 2. Popular Social & Media Apps (21-40)
    YOUTUBE("YouTube", "com.google.android.youtube", false, "Media", 0xFFFF0000),
    INSTAGRAM("Instagram", "com.instagram.android", false, "Social", 0xFFE1306C),
    GOOGLE("Google", "com.google.android.googlequicksearchbox", false, "Utilities", 0xFF4285F4),
    WHATSAPP("WhatsApp", "com.whatsapp", false, "Communication", 0xFF25D366),
    SPOTIFY("Spotify", "com.spotify.music", false, "Media", 0xFF1DB954),
    MAPS("Google Maps", "com.google.android.apps.maps", false, "Navigation", 0xFF34A853),
    NETFLIX("Netflix", "com.netflix.mediaclient", false, "Media", 0xFFE50914),
    TIKTOK("TikTok", "com.zhiliaoapp.musically", false, "Social", 0xFF00F2FE),
    TWITTER_X("X (Twitter)", "com.twitter.android", false, "Social", 0xFF1DA1F2),
    TELEGRAM("Telegram", "org.telegram.messenger", false, "Communication", 0xFF26A5E4),
    DISCORD("Discord", "com.discord", false, "Communication", 0xFF5865F2),
    REDDIT("Reddit", "com.reddit.frontpage", false, "Social", 0xFFFF4500),
    LINKEDIN("LinkedIn", "com.linkedin.android", false, "Social", 0xFF0A66C2),
    SIGNAL("Signal", "org.thoughtcrime.securesms", false, "Communication", 0xFF3A76F0),
    ZOOM("Zoom Workplace", "us.zoom.videomeetings", false, "Communication", 0xFF2D8CFF),
    TEAMS("Microsoft Teams", "com.microsoft.teams", false, "Productivity", 0xFF6264A7),
    SNAPCHAT("Snapchat", "com.snapchat.android", false, "Social", 0xFFFFFC00),
    PINTEREST("Pinterest", "com.pinterest", false, "Social", 0xFFBD081C),
    THREADS("Threads", "com.instagram.barcelona", false, "Social", 0xFF101010),
    FACEBOOK("Facebook", "com.facebook.katana", false, "Social", 0xFF1877F2),

    // 3. Media & Creative Studio Apps (41-65)
    MESSENGER("Messenger", "com.facebook.orca", false, "Communication", 0xFF00B2FF),
    WECHAT("WeChat", "com.tencent.mm", false, "Communication", 0xFF07C160),
    SLACK("Slack", "com.slack", false, "Productivity", 0xFF4A154B),
    VIBER("Viber", "com.viber.voip", false, "Communication", 0xFF7360F2),
    SKYPE("Skype", "com.skype.raider", false, "Communication", 0xFF00AFF0),
    LINE("LINE", "jp.naver.line.android", false, "Communication", 0xFF06C755),
    GOOGLE_MEET("Google Meet", "com.google.android.apps.meetings", false, "Communication", 0xFF00897B),
    MASTODON("Mastodon", "org.joinmastodon.android", false, "Social", 0xFF6364FF),
    BLUESKY("Bluesky", "xyz.blueskyweb.app", false, "Social", 0xFF1185FE),
    TWITCH("Twitch", "tv.twitch.android.app", false, "Media", 0xFF9146FF),
    TUMBLR("Tumblr", "com.tumblr", false, "Social", 0xFF36465D),
    SOUNDCLOUD("SoundCloud", "com.soundcloud.android", false, "Media", 0xFFFF5500),
    DEEZER("Deezer Music", "deezer.android.app", false, "Media", 0xFFA238FF),
    TIDAL("TIDAL Hi-Fi", "com.aspiro.tidal", false, "Media", 0xFF000000),
    APPLE_MUSIC("Apple Music", "com.apple.android.music", false, "Media", 0xFFFA243C),
    VLC_PLAYER("VLC Media Player", "org.videolan.vlc", false, "Media", 0xFFFF8800),
    PODCASTS("Google Podcasts", "com.google.android.apps.podcasts", false, "Media", 0xFFEA4335),
    CINEMA_HD("Cinema HD Player", "com.novaos.cinema", false, "Media", 0xFFD81B60),
    AUDIO_STUDIO("Audio Studio DAW", "com.novaos.audiostudio", false, "Media", 0xFF00E5FF),
    GUITAR_TUNER("Guitar Tuner Pro", "com.novaos.tuner", false, "Media", 0xFF8BC34A),
    RADIO_FM("World Radio FM", "com.novaos.radio", false, "Media", 0xFFFF9800),
    BEAT_MAKER("Beat Maker Studio", "com.novaos.beatmaker", false, "Media", 0xFFE91E63),
    VIDEO_EDITOR("Video Cut Studio", "com.novaos.videoedit", false, "Media", 0xFF9C27B0),
    PHOTO_PRO("Photo Editor Pro", "com.novaos.photopro", false, "Media", 0xFF00BCD4),
    CANVAS_3D("3D Model Viewer", "com.novaos.canvas3d", false, "Media", 0xFF673AB7),

    // 4. Productivity & Office Suite (66-90)
    EQUALIZER_PRO("Audio Equalizer FX", "com.novaos.equalizer", false, "Media", 0xFF3F51B5),
    DRUM_PAD("Drum Pad Machine", "com.novaos.drumpad", false, "Media", 0xFFFF5722),
    PIANO_KEYS("Grand Piano Keyboard", "com.novaos.piano", false, "Media", 0xFF212121),
    VOICE_CHANGER("Voice FX Changer", "com.novaos.voicefx", false, "Media", 0xFF009688),
    SAMPLE_STUDIO("Sound Sampler Studio", "com.novaos.sampler", false, "Media", 0xFF607D8B),
    GOOGLE_DOCS("Google Docs", "com.google.android.apps.docs.editors.docs", false, "Productivity", 0xFF4285F4),
    GOOGLE_SHEETS("Google Sheets", "com.google.android.apps.docs.editors.sheets", false, "Productivity", 0xFF0F9D58),
    GOOGLE_SLIDES("Google Slides", "com.google.android.apps.docs.editors.slides", false, "Productivity", 0xFFF4B400),
    NOTION("Notion Workspace", "notion.id", false, "Productivity", 0xFF000000),
    OBSIDIAN("Obsidian Knowledge", "md.obsidian", false, "Productivity", 0xFF7A3EE8),
    TODOIST("Todoist Planner", "com.todoist", false, "Productivity", 0xFFE44332),
    TRELLO("Trello Boards", "com.trello", false, "Productivity", 0xFF0079BF),
    ASANA("Asana Workflows", "com.asana.app", false, "Productivity", 0xFFF06A6A),
    EVERNOTE("Evernote Notes", "com.evernote", false, "Productivity", 0xFF00A82D),
    MS_WORD("Microsoft Word", "com.microsoft.office.word", false, "Productivity", 0xFF185ABD),
    MS_EXCEL("Microsoft Excel", "com.microsoft.office.excel", false, "Productivity", 0xFF107C41),
    MS_POWERPOINT("Microsoft PowerPoint", "com.microsoft.office.powerpoint", false, "Productivity", 0xFFC43E1C),
    MS_ONENOTE("Microsoft OneNote", "com.microsoft.office.onenote", false, "Productivity", 0xFF7719AA),
    PDF_READER("PDF Expert Reader", "com.novaos.pdfreader", false, "Productivity", 0xFFD32F2F),
    SCANNER_PRO("Doc Scanner Pro", "com.novaos.docscan", false, "Productivity", 0xFF00897B),
    MIND_MAP("Mind Map Visualizer", "com.novaos.mindmap", false, "Productivity", 0xFFFF6F00),
    POMODORO_FOCUS("Pomodoro Focus Timer", "com.novaos.pomodoro", false, "Productivity", 0xFFE53935),
    HABIT_TRACKER("Habit Tracker & Goals", "com.novaos.habits", false, "Productivity", 0xFF43A047),
    TIME_LOGGER("Time Tracker Log", "com.novaos.timelog", false, "Productivity", 0xFF1E88E5),
    EXPENSE_TRACKER("Expense & Budget Log", "com.novaos.expenses", false, "Productivity", 0xFF00ACC1),

    // 5. Business, Finance & Crypto (91-115)
    INVOICE_MAKER("Invoice Maker Pro", "com.novaos.invoice", false, "Productivity", 0xFF5E35B1),
    CALENDAR_PRO("Calendar & Events", "com.novaos.calendar", false, "Productivity", 0xFF1976D2),
    REMINDERS("Smart Reminders", "com.novaos.reminders", false, "Productivity", 0xFFF57C00),
    DIARY_JOURNAL("Daily Diary & Lock", "com.novaos.diary", false, "Productivity", 0xFF8E24AA),
    RESUME_BUILDER("CV Resume Builder", "com.novaos.resume", false, "Productivity", 0xFF00838F),
    PRESENTATION_PRO("Deck Presentation", "com.novaos.deck", false, "Productivity", 0xFFD84315),
    BUDGET_PLANNER("Budget Planner AI", "com.novaos.budget", false, "Finance", 0xFF2E7D32),
    SPREADSHEET_LITE("Fast Spreadsheet", "com.novaos.calcpro", false, "Productivity", 0xFF388E3C),
    CRM_MOBILE("Mobile CRM Hub", "com.novaos.crm", false, "Productivity", 0xFF0288D1),
    KANBAN_FLOW("Kanban Project Flow", "com.novaos.kanban", false, "Productivity", 0xFF7B1FA2),
    PROJECT_HUB("Project Hub Agile", "com.novaos.projecthub", false, "Productivity", 0xFFC2185B),
    MILESTONE_PLANNER("Milestone Tracker", "com.novaos.milestones", false, "Productivity", 0xFF0097A7),
    WORKFLOW_STUDIO("Workflow Automator", "com.novaos.workflow", false, "Productivity", 0xFF455A64),
    CONTRACT_MAKER("Digital Contract Sign", "com.novaos.contract", false, "Productivity", 0xFF303F9F),
    TEAM_CONNECT("Team Chat Connect", "com.novaos.teamconnect", false, "Communication", 0xFF512DA8),
    AUDIT_TRAIL("Audit Security Trail", "com.novaos.audittrail", false, "Utilities", 0xFF37474F),
    SALES_TRACKER("Sales Lead Tracker", "com.novaos.salestrack", false, "Finance", 0xFF689F38),
    BIZ_ANALYTICS("Business Analytics", "com.novaos.analytics", false, "Finance", 0xFF009688),
    TAX_CALCULATOR("Smart Tax Calculator", "com.novaos.taxcalc", false, "Finance", 0xFFEF6C00),
    INVENTORY_MGR("Stock & Inventory Pro", "com.novaos.inventory", false, "Productivity", 0xFF4E342E),
    CRYPTO_WALLET("Nova Crypto Wallet", "com.novaos.cryptowallet", false, "Finance", 0xFFF7931A),
    STOCK_TRACKER("Stock Market & FX", "com.novaos.stocks", false, "Finance", 0xFF00C853),
    BANK_PORTAL("Mobile Banking Secure", "com.novaos.bankportal", false, "Finance", 0xFF1565C0),
    AMAZON("Amazon Shopping", "com.amazon.mShop.android.shopping", false, "Shopping", 0xFFFF9900),
    EBAY("eBay Marketplace", "com.ebay.mobile", false, "Shopping", 0xFFE53238),

    // 6. Shopping, Travel & Food (116-140)
    ALIEXPRESS("AliExpress Global", "com.alibaba.aliexpresshd", false, "Shopping", 0xFFFF4747),
    TRENDYOL("Trendyol Alışveriş", "com.trendyol.android", false, "Shopping", 0xFFF27A1A),
    HEPSIBURADA("Hepsiburada E-Ticaret", "com.pozitron.hepsiburada", false, "Shopping", 0xFFFF6000),
    YEMEKSEPETI("Yemeksepeti Sipariş", "com.in aeration.yemeksepeti", false, "Food", 0xFFEA0029),
    GETIR("Getir Dakikalar İçinde", "com.getir", false, "Food", 0xFF5D3EBC),
    UBER("Uber Rides & Delivery", "com.ubercab", false, "Travel", 0xFF000000),
    AIRBNB("Airbnb Rentals", "com.airbnb.android", false, "Travel", 0xFFFF5A5F),
    BOOKING_COM("Booking.com Hotels", "com.booking", false, "Travel", 0xFF003580),
    TRIP_ADVISOR("Tripadvisor Travel", "com.tripadvisor.tripadvisor", false, "Travel", 0xFF34E0A1),
    STARBUCKS("Starbucks Rewards", "com.starbucks.mobilecard", false, "Food", 0xFF006241),
    DUOLINGO("Duolingo Language", "com.duolingo", false, "Education", 0xFF58CC02),
    KHAN_ACADEMY("Khan Academy Learn", "org.khanacademy.android", false, "Education", 0xFF14BF96),
    TED_TALKS("TED Ideas Worth Spread", "com.ted.android", false, "Education", 0xFFE62B1E),
    WIKIPEDIA("Wikipedia Official", "org.wikipedia", false, "Education", 0xFF000000),
    RECIPE_BOOK("Gourmet Recipe Book", "com.novaos.recipes", false, "Food", 0xFFFF5722),
    COFFEE_BREW("Coffee Brew Master", "com.novaos.coffeebrew", false, "Food", 0xFF795548),
    WORKOUT_LOG("Gym Workout & Lift", "com.novaos.workout", false, "Health", 0xFFD32F2F),
    BMI_CALC("BMI Body Composition", "com.novaos.bmicalc", false, "Health", 0xFF388E3C),
    FASTING_TRACKER("Intermittent Fasting", "com.novaos.fasting", false, "Health", 0xFF7B1FA2),
    STEP_COUNTER_PRO("Pedometer Step Pro", "com.novaos.pedometer", false, "Health", 0xFF00B0FF),
    MED_REMINDER("Medication Pill Alert", "com.novaos.medalert", false, "Health", 0xFF00897B),
    VOS_HUB_NEWS("Vroxen UI Daily Tech News", "com.novaos.news", false, "News", 0xFF1976D2),
    PEDOMETER("Daily Step Tracker", "com.novaos.steptracker", false, "Health", 0xFF4CAF50),
    WATER_TRACKER("Hydro Water Reminder", "com.novaos.watertrack", false, "Health", 0xFF03A9F4),
    CALORIE_COUNTER("Calorie Counter AI", "com.novaos.caloriecount", false, "Health", 0xFFFF9800),

    // 7. Health, Tools & Developer Utilities (141-170)
    SLEEP_ANALYSIS("Sleep Cycle Tracker", "com.novaos.sleeptrack", false, "Health", 0xFF3F51B5),
    MEDITATION("Zen Meditation & Breath", "com.novaos.meditation", false, "Health", 0xFF009688),
    YOGA_GUIDE("Yoga Pose Daily", "com.novaos.yogaguide", false, "Health", 0xFFE91E63),
    RUNNING_COACH("GPS Running Coach", "com.novaos.runningcoach", false, "Health", 0xFFFF5722),
    SPEEDTEST("Speedtest Network 5G", "com.novaos.speedtest", false, "Utilities", 0xFF00E5FF),
    WIFI_ANALYZER("Wi-Fi Spectrum Radar", "com.novaos.wifianalyzer", false, "Utilities", 0xFF651FFF),
    CPU_Z("CPU & Hardware Sensor", "com.novaos.cpuz", false, "Utilities", 0xFF37474F),
    QR_SCANNER("QR & Barcode Scanner", "com.novaos.qrscanner", false, "Utilities", 0xFF00C853),
    COMPASS("Digital Compass 3D", "com.novaos.compass", false, "Utilities", 0xFFFFAB00),
    VOICE_RECORDER("HD Voice Audio Record", "com.novaos.voicerecorder", false, "Utilities", 0xFFE53935),
    FLASHLIGHT("Super Bright Flashlight", "com.novaos.flashlight", false, "Utilities", 0xFFFFD600),
    METRONOME("Pro Musician Metronome", "com.novaos.metronome", false, "Utilities", 0xFF8E24AA),
    UNIT_CONVERTER("Universal Unit Calc", "com.novaos.unitconverter", false, "Utilities", 0xFF0288D1),
    COLOR_PICKER("Hex RGB Color Studio", "com.novaos.colorpicker", false, "Developer", 0xFFFF4081),
    HASH_TOOL("MD5 SHA256 Hash Vault", "com.novaos.hashtool", false, "Developer", 0xFF546E7A),
    JSON_VIEWER("JSON & XML Formatter", "com.novaos.jsonviewer", false, "Developer", 0xFF2E7D32),
    REGEX_TESTER("Regex Pattern Sandbox", "com.novaos.regextester", false, "Developer", 0xFFC2185B),
    BT_SCANNER("Bluetooth LE Beacon Scan", "com.novaos.btscanner", false, "Developer", 0xFF1565C0),
    BATTERY_MONITOR("Battery Health & Temp", "com.novaos.batterymonitor", false, "Utilities", 0xFF43A047),
    DISK_CLEANER("RAM & Disk Junk Clean", "com.novaos.diskcleaner", false, "Utilities", 0xFF00ACC1),
    PING_TRACE("Ping & Traceroute Net", "com.novaos.pingtrace", false, "Developer", 0xFF3949AB),
    NOTEPAD_PLUS("Code Notepad Markdown", "com.novaos.notepadplus", false, "Developer", 0xFFE65100),
    CLIPBOARD_MGR("Clipboard History Pro", "com.novaos.clipboardmgr", false, "Utilities", 0xFF5C6BC0),
    TRANSLATE_PRO("Voice & Text Translator", "com.novaos.translatepro", false, "Utilities", 0xFF00897B),
    AUTHENTICATOR("2FA Auth Code Vault", "com.novaos.authenticator", false, "Security", 0xFF1976D2),
    PASSWORD_VAULT("Encrypted Pass Vault", "com.novaos.passvault", false, "Security", 0xFF263238),
    BACKUP_SYNC("Cloud Backup Engine", "com.novaos.backupsync", false, "Utilities", 0xFF0097A7),
    DEVICE_CLONE("Phone Transfer Clone", "com.novaos.deviceclone", false, "Utilities", 0xFF7B1FA2),
    SCREEN_RECORDER("60FPS Screen Capture", "com.novaos.screenrecord", false, "Media", 0xFFD81B60),
    NETWORK_MONITOR("Realtime Data Flow Mon", "com.novaos.networkmon", false, "Developer", 0xFF283593),

    // 8. Games & Arcade Collection (171-200)
    SENSOR_BOX("Gyroscope Sensor Box", "com.novaos.sensorbox", false, "Developer", 0xFF00838F),
    BENCHMARK_X("3D GPU Benchmark X", "com.novaos.benchmarkx", false, "Developer", 0xFFD84315),
    FILE_COMPRESSOR("Zip 7z Archive Unpack", "com.novaos.compressor", false, "Utilities", 0xFF5D4037),
    FTP_SERVER("WiFi FTP File Server", "com.novaos.ftpserver", false, "Developer", 0xFF303F9F),
    SSH_CLIENT("Terminal SSH Client", "com.novaos.sshclient", false, "Developer", 0xFF212121),
    CODE_STUDIO("Kotlin IDE Playground", "com.novaos.codestudio", false, "Developer", 0xFF7C4DFF),
    MARKDOWN_EDITOR("Markdown Live Viewer", "com.novaos.mdeditor", false, "Developer", 0xFF455A64),
    COLOR_HARMONY("Material 3 Palette Lab", "com.novaos.colorharmony", false, "Developer", 0xFF00E676),
    SYSTEM_LOGS("Logcat Live Telemetry", "com.novaos.systemlogs", false, "Developer", 0xFF37474F),
    FONT_PREVIEW("Typography Font Box", "com.novaos.fontpreview", false, "Developer", 0xFF8E24AA),
    APK_EXTRACTOR("APK Backup & Inspect", "com.novaos.apkextractor", false, "Developer", 0xFF00ACC1),
    REPO_EXPLORER("Git Repository Browser", "com.novaos.repoexplorer", false, "Developer", 0xFF1A237E),
    CRON_MANAGER("Background Cron Worker", "com.novaos.cronmgr", false, "Developer", 0xFF004D40),
    CACHE_OPTIMIZER("Kernel Cache Boost", "com.novaos.cacheboost", false, "Utilities", 0xFF1B5E20),
    RETRO_SNAKE("Retro Snake Classic", "com.novaos.retrosnake", false, "Games", 0xFF4CAF50),
    PUZZLE_2048("2048 Hex & Number", "com.novaos.puzzle2048", false, "Games", 0xFFFF9800),
    CHESS_AI("Grandmaster Chess AI", "com.novaos.chessai", false, "Games", 0xFF212121),
    SUDOKU_ZEN("Sudoku Zen Master", "com.novaos.sudokuzen", false, "Games", 0xFF1976D2),
    PIXEL_RUNNER("Pixel Cyber Runner", "com.novaos.pixelrunner", false, "Games", 0xFFE91E63),
    SPACE_INVADERS("Retro Galaxy Invaders", "com.novaos.spaceinvaders", false, "Games", 0xFF00E5FF),
    BRICK_BREAKER("Neon Brick Breaker", "com.novaos.brickbreaker", false, "Games", 0xFFFF5722),
    CROSSWORD_PRO("Daily Crossword AI", "com.novaos.crossword", false, "Games", 0xFF8E24AA),
    SOLITAIRE("Classic Klondike Cards", "com.novaos.solitaire", false, "Games", 0xFF388E3C),
    TIC_TAC_TOE("Tic-Tac-Toe Glow Pro", "com.novaos.tictactoe", false, "Games", 0xFF00BCD4),
    MINESWEEPER("Retro Minesweeper HD", "com.novaos.minesweeper", false, "Games", 0xFF546E7A),
    FLAPPY_JET("Flappy Jet Simulator", "com.novaos.flappyjet", false, "Games", 0xFFFFA000),
    BLOCK_PUZZLE("Jewel Block Puzzle", "com.novaos.blockpuzzle", false, "Games", 0xFF7B1FA2),
    WORDLE_VOS("Word Guess Daily Game", "com.novaos.wordguess", false, "Games", 0xFF00C853),
    RACING_3D("Speedway 3D Drift", "com.novaos.racing3d", false, "Games", 0xFFD50000),
    TOWER_DEFENSE("Cyber Tower Defense", "com.novaos.towerdefense", false, "Games", 0xFF6200EA)
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
    BATTERY_STATUS("Pil Durumu & Şarj", "Anlık pil yüzdesi, şarj durumu, sıcaklık ve kalan süre"),
    CLOCK("Material Saat & Tarih", "Büyük dijital saat, tarih ve alarm durumu"),
    WEATHER("Hava Durumu", "Anlık sıcaklık, durum ve haftalık tahmin"),
    MUSIC("SoundWave Müzik Çalar", "Parça bilgisi, albüm kapağı ve medya kontrolleri"),
    NOTES("Hızlı Notlar & Fikirler", "En son alınan not ve yapılacaklar listesi"),
    BATTERY_WELLBEING("Pil Sağlığı & Dijital Denge", "Pil yüzdesi, sıcaklık ve ekran süresi"),
    QUICK_TOGGLES("Hızlı Ayar Anahtarları", "Wi-Fi, Bluetooth, Fener ve Rahatsız Etmeyin")
}

enum class WallpaperTarget(val title: String) {
    HOME("Ana Ekran"),
    LOCK("Kilit Ekranı"),
    BOTH("Her İki Ekran")
}

enum class WallpaperCategory(val title: String, val iconName: String) {
    ALL("Tümü", "all_inclusive"),
    LIVE("Canlı Animasyonlar", "auto_awesome"),
    STATIC("Statik Manzaralar", "wallpaper"),
    GRADIENTS("Minimal Gradyanlar", "palette"),
    SYSTEM("Sistem Arka Planı", "phone_android"),
    CUSTOM("Galeri / Özel", "add_photo_alternate")
}

enum class WallpaperSourceType {
    SYSTEM_NATIVE,
    STATIC_PRESET,
    LIVE_ANIMATED,
    CUSTOM_GALLERY
}

enum class LiveWallpaperType(
    val title: String,
    val subtitle: String,
    val description: String,
    val dominantColor: Long
) {
    AURORA_GLOW(
        "Aurora Kozmik Enerji",
        "Kuzey Işıkları & Yıldız Tozu",
        "Sürekli dalgalanan armonik kuzey ışıkları, hareketli ışık hüzmeleri ve parıldayan mikro parçacıklar.",
        0xFF00E5FF
    ),
    CYBER_MATRIX(
        "Siber Matris & Neon",
        "Dijital Veri Akışı",
        "Matrix tarzı düşen neon yeşil/mor dijital veri akışları, terminal kodları ve ışıltılı siber grid.",
        0xFF00FF66
    ),
    DEEP_NEBULA(
        "Derin Nebula & Galaksi",
        "Dönen Yıldız Bulutları",
        "Derin uzay spiral galaksi bulutları, göz kırpan yıldızlar ve arada geçen kayan meteorlar.",
        0xFF9C27B0
    ),
    OCEAN_WAVES(
        "Biyolüminesan Okyanus",
        "Armonik Su Dalgaları",
        "Sakinleştirici çok katmanlı okyanus dalgaları, ışıldayan biyolüminesans planktonlar ve yumuşak köpükler.",
        0xFF0288D1
    ),
    PARTICLE_SWARM(
        "Kuantum Parçacık Sürüsü",
        "Dokunmaya Duyarlı Fizik",
        "Ekrandaki dokunma ve kaydırmalarınıza göre bir araya toplanan ve dağılan interaktif fizik parçacıkları.",
        0xFFFF4081
    ),
    DYNAMIC_BOKEH(
        "Kromatik Işık Küreleri",
        "Yumuşak Işık Dansı",
        "Arka planda süzülen yumuşak odak dışı renkli bokeh küreleri ve atmosferik ışık parlamaları.",
        0xFFFF9800
    )
}

enum class WallpaperType(val title: String, val drawableResName: String, val isLight: Boolean = false) {
    DEVICE_SYSTEM("Telefon Arka Planı (Sistem)", ""),
    AURORA("Aurora Borealis", "wp_aurora"),
    CYBERPUNK("Cyber City", "wp_cyber"),
    ABSTRACT("Material Wave", "wp_abstract"),
    DEEP_SPACE("Deep Nebula", "wp_aurora"),
    FOREST_MIST("Forest Mist", "wp_abstract"),
    OCEAN_SUNRISE("Ocean Sunrise", "wp_aurora"),
    MINIMAL_GRADIENT("Velvet Gradient", ""),
    NEON_GEOMETRY("Neon Geometri", ""),
    SUNSET_BLVD("Sunset Boulevard", ""),
    DARK_OBSIDIAN("Karanlık Obsidyen (AMOLED)", ""),
    COSMIC_PASTEL("Kozmik Pastel", "")
}

data class WallpaperDefinition(
    val id: String,
    val title: String,
    val category: WallpaperCategory,
    val sourceType: WallpaperSourceType,
    val staticType: WallpaperType = WallpaperType.DEVICE_SYSTEM,
    val liveType: LiveWallpaperType? = null,
    val customImageUri: String? = null,
    val gradientColors: List<Long> = emptyList(),
    val isLight: Boolean = false,
    val badge: String? = null,
    val description: String = ""
)

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
    val isRead: Boolean = true,
    val mmsAttachmentType: String? = null, // "IMAGE", "AUDIO", "CONTACT", "LOCATION"
    val mmsAttachmentUri: String? = null,
    val mmsAttachmentTitle: String? = null
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

data class BrowserBookmark(
    val id: String,
    val title: String,
    val url: String,
    val favicon: String = "globe",
    val timestamp: Long = System.currentTimeMillis()
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

enum class SoundProfile(val label: String) {
    RING("Ring"),
    VIBRATE("Vibrate"),
    SILENT("Silent")
}

enum class BootMode(val title: String, val subtitle: String) {
    NORMAL("Normal Başlatma", "Vroxen UI Tam Sistem Başlatma"),
    FAST("Hızlı Boot", "Saniyeler İçinde Hızlı Açılış"),
    RECOVERY("Kurtarma Modu (Recovery)", "Sistem Onarımı & Kurtarma Konsolu"),
    SAFE_MODE("Güvenli Mod", "Temel Sistem Sürücüleri ve Güvenli Başlatma")
}

