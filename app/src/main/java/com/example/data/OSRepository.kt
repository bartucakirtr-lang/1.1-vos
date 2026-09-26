package com.example.data

import com.example.model.*

object OSRepository {

    fun getInitialContacts(): List<ContactItem> = listOf(
        ContactItem("c1", "Mom ❤️", "+1 (555) 234-5678", 0xFFE91E63, isFavorite = true, email = "mom@family.net"),
        ContactItem("c2", "Alex Rivera (Tech Lead)", "+1 (555) 876-5432", 0xFF2196F3, isFavorite = true, email = "alex@cyberworks.io"),
        ContactItem("c3", "Emma Watson", "+1 (555) 345-6789", 0xFF9C27B0, isFavorite = true, email = "emma.w@designlab.org"),
        ContactItem("c4", "Nova Support & AI", "+1 (800) 555-NOVA", 0xFF00BCD4, isFavorite = false, email = "support@novaos.org"),
        ContactItem("c5", "Lucas Vance", "+1 (555) 654-3210", 0xFFFF9800, isFavorite = false, email = "lucas.vance@soundlab.fm"),
        ContactItem("c6", "Pizza Express 🍕", "+1 (555) 789-0123", 0xFFE65100, isFavorite = true, email = "order@pizzaexpress.com"),
        ContactItem("c7", "Dr. Samantha Reed", "+1 (555) 901-2345", 0xFF4CAF50, isFavorite = false, email = "clinic@reedhealth.org")
    )

    fun getInitialCallLogs(): List<CallLogItem> = listOf(
        CallLogItem("l1", "Mom ❤️", "+1 (555) 234-5678", CallType.INCOMING, 240, System.currentTimeMillis() - 1000 * 60 * 25),
        CallLogItem("l2", "Alex Rivera (Tech Lead)", "+1 (555) 876-5432", CallType.OUTGOING, 412, System.currentTimeMillis() - 1000 * 60 * 120),
        CallLogItem("l3", "Emma Watson", "+1 (555) 345-6789", CallType.MISSED, 0, System.currentTimeMillis() - 1000 * 60 * 340),
        CallLogItem("l4", "Pizza Express 🍕", "+1 (555) 789-0123", CallType.OUTGOING, 65, System.currentTimeMillis() - 1000 * 60 * 60 * 18)
    )

    fun getInitialChatThreads(): List<ChatThread> = listOf(
        ChatThread(
            contactId = "c1",
            contactName = "Mom ❤️",
            phoneNumber = "+1 (555) 234-5678",
            avatarColor = 0xFFE91E63,
            messages = listOf(
                ChatMessage("m1", "Mom", "Hey sweetheart, are you coming over for Sunday dinner?", System.currentTimeMillis() - 1000 * 60 * 90, isFromMe = false),
                ChatMessage("m2", "Me", "Yes! I'll bring the fresh apple pie we made yesterday 🥧", System.currentTimeMillis() - 1000 * 60 * 45, isFromMe = true),
                ChatMessage("m3", "Mom", "Wonderful! Can't wait to see you. Love you! 💕", System.currentTimeMillis() - 1000 * 60 * 20, isFromMe = false, isRead = false)
            ),
            unreadCount = 1
        ),
        ChatThread(
            contactId = "c2",
            contactName = "Alex Rivera (Tech Lead)",
            phoneNumber = "+1 (555) 876-5432",
            avatarColor = 0xFF2196F3,
            messages = listOf(
                ChatMessage("m4", "Alex", "The new NovaOS build is blazing fast on the kernel layer!", System.currentTimeMillis() - 1000 * 60 * 180, isFromMe = false),
                ChatMessage("m5", "Me", "Agreed! Jetpack Compose UI animations are rendering at a smooth 120Hz.", System.currentTimeMillis() - 1000 * 60 * 150, isFromMe = true),
                ChatMessage("m6", "Alex", "Let's push the OTA update to release ring today 🚀", System.currentTimeMillis() - 1000 * 60 * 30, isFromMe = false)
            ),
            unreadCount = 0
        ),
        ChatThread(
            contactId = "c6",
            contactName = "Pizza Express 🍕",
            phoneNumber = "+1 (555) 789-0123",
            avatarColor = 0xFFE65100,
            messages = listOf(
                ChatMessage("m7", "Pizza Express", "Your Truffle & Mushroom Artisan Pizza is out for delivery! Courier #48 is 5 minutes away 🛵", System.currentTimeMillis() - 1000 * 60 * 15, isFromMe = false, isRead = false)
            ),
            unreadCount = 1
        )
    )

    fun getInitialNotes(): List<NoteItem> = listOf(
        NoteItem(
            id = "n1",
            title = "NovaOS Architecture Plan 📱",
            content = "1. Material You dynamic color palette switching\n2. Real-time system status bar with drag-down Quick Settings shade\n3. Full Gesture & 3-Button navigation switcher\n4. Multitasking recents overview carousel with swipe-to-dismiss",
            colorHex = 0xFF283593,
            isPinned = true,
            checkListItems = listOf(
                "Dynamic Material 3 Color Theme" to true,
                "Quick Settings & Brightness Shade" to true,
                "Lock Screen with PIN & Clock Styles" to true,
                "Interactive Built-in System Apps" to true
            )
        ),
        NoteItem(
            id = "n2",
            title = "Weekend Grocery List 🛒",
            content = "Organic oat milk, Greek yogurt, fresh sourdough bread, avocados, cold brew coffee beans, dark chocolate 85%.",
            colorHex = 0xFF00695C,
            isPinned = false,
            checkListItems = listOf(
                "Oat Milk" to true,
                "Avocados" to false,
                "Cold Brew Beans" to false,
                "Dark Chocolate" to false
            )
        ),
        NoteItem(
            id = "n3",
            title = "Ideas for SoundWave Music App 🎵",
            content = "Add lo-fi frequency spectrum canvas visualizer, synthesizer oscillator for real audio tones, lyrics auto-scroll, and playlist queue manager.",
            colorHex = 0xFF4A148C,
            isPinned = true
        )
    )

    fun getInitialAlarms(): List<AlarmItem> = listOf(
        AlarmItem("a1", "07:00 AM", "Morning Workout & Espresso", true, listOf("Mon", "Tue", "Wed", "Thu", "Fri")),
        AlarmItem("a2", "08:30 AM", "Daily Standup Meeting", true, listOf("Mon", "Tue", "Wed", "Thu", "Fri")),
        AlarmItem("a3", "09:45 PM", "Wind Down & Read Book", false, listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"))
    )

    fun getInitialWorldClocks(): List<WorldClockCity> = listOf(
        WorldClockCity("Tokyo", "Japan", +16, "02:00 AM", false),
        WorldClockCity("London", "United Kingdom", +8, "06:00 PM", true),
        WorldClockCity("New York", "United States", +3, "01:00 PM", true),
        WorldClockCity("San Francisco", "United States", 0, "10:00 AM", true),
        WorldClockCity("Paris", "France", +9, "07:00 PM", true),
        WorldClockCity("Sydney", "Australia", +17, "03:00 AM", false)
    )

    fun getInitialMusicTracks(): List<MusicTrack> = listOf(
        MusicTrack(
            id = "t1",
            title = "Neon Skyline (Synthwave)",
            artist = "Aether Waves",
            album = "Cyberpunk Odyssey 2026",
            durationSeconds = 214,
            coverGradientStart = 0xFF6A11CB,
            coverGradientEnd = 0xFF2575FC,
            toneFrequencyHz = 440f,
            lyrics = listOf(
                "Glowing neon in the rain tonight",
                "Speeding through the cyber highway lines",
                "Data streams pulsating in the dark",
                "Synthesizer echoes leave their mark",
                "NovaOS running free and bright"
            )
        ),
        MusicTrack(
            id = "t2",
            title = "Midnight Lo-Fi Chill",
            artist = "Tokyo Coffee Club",
            album = "Rainy Rooftops EP",
            durationSeconds = 185,
            coverGradientStart = 0xFF37ecba,
            coverGradientEnd = 0xFF72afd3,
            toneFrequencyHz = 330f,
            lyrics = listOf(
                "Raindrops tapping on the window sill",
                "Warm cup of matcha sitting still",
                "Vinyl crackles gently on the beat",
                "Resting tired feet from the bustling street"
            )
        ),
        MusicTrack(
            id = "t3",
            title = "Atmospheric Horizon",
            artist = "Solaris Ensemble",
            album = "Cosmic Dreams",
            durationSeconds = 260,
            coverGradientStart = 0xFFFF0844,
            coverGradientEnd = 0xFFFFB199,
            toneFrequencyHz = 523.25f,
            lyrics = listOf(
                "Drifting beyond the stratosphere",
                "Aurora lights so crystal clear",
                "Weightless harmony in the blue",
                "Infinite horizons calling you"
            )
        ),
        MusicTrack(
            id = "t4",
            title = "Deep Focus Flow",
            artist = "Neural Rhythm",
            album = "Binaural Sessions",
            durationSeconds = 240,
            coverGradientStart = 0xFF13547A,
            coverGradientEnd = 0xFF80D0C7,
            toneFrequencyHz = 392f,
            lyrics = listOf(
                "Pure state of deep cognitive flow",
                "Lines of clean code begin to glow",
                "Zero distractions in this zone",
                "Building an OS of our own"
            )
        )
    )

    fun getInitialPhotos(): List<PhotoItem> = listOf(
        PhotoItem("p1", "Aurora Borealis Peak", "Today, 10:15 AM", "wp_aurora", isFavorite = true, category = "Wallpapers"),
        PhotoItem("p2", "Cyberpunk Metropolis", "Yesterday, 8:40 PM", "wp_cyber", isFavorite = true, category = "Wallpapers"),
        PhotoItem("p3", "Material Prism Waves", "Sep 22, 2:10 PM", "wp_abstract", isFavorite = false, category = "Wallpapers"),
        PhotoItem("p4", "Nova OS Emblem", "Sep 20, 11:30 AM", "ic_phone_os_icon", isFavorite = true, category = "Screenshots")
    )

    fun getInitialFiles(): List<FileItem> = listOf(
        FileItem("f1", "Documents", "/storage/emulated/0/Documents", isDirectory = true),
        FileItem("f2", "Downloads", "/storage/emulated/0/Downloads", isDirectory = true),
        FileItem("f3", "Pictures", "/storage/emulated/0/Pictures", isDirectory = true),
        FileItem("f4", "Music", "/storage/emulated/0/Music", isDirectory = true),
        FileItem("f5", "System", "/system", isDirectory = true),
        FileItem("f6", "nova_kernel_config.json", "/storage/emulated/0/Documents", isDirectory = false, sizeBytes = 4096, extension = "json"),
        FileItem("f7", "release_notes_v16.md", "/storage/emulated/0/Downloads", isDirectory = false, sizeBytes = 12500, extension = "md"),
        FileItem("f8", "synthwave_anthem.mp3", "/storage/emulated/0/Music", isDirectory = false, sizeBytes = 5242880, extension = "mp3")
    )

    fun getInitialStoreApps(): List<StoreAppItem> = listOf(
        StoreAppItem(
            appId = AppId.ARCADE,
            name = "Nova Arcade Games",
            developer = "Nova Studios",
            rating = 4.9f,
            sizeMb = 12,
            downloads = "5M+",
            description = "Play 2048 Puzzle, Retro Snake, and Tic-Tac-Toe vs smart AI inside NovaOS!",
            isInstalled = true,
            category = "Games",
            iconColor = 0xFFFF5722
        ),
        StoreAppItem(
            appId = AppId.TERMINAL,
            name = "Nova Terminal Pro",
            developer = "Core OS Tools",
            rating = 4.8f,
            sizeMb = 4,
            downloads = "1M+",
            description = "Powerful command line emulator with real system commands, neofetch, matrix effect & uptime diagnostics.",
            isInstalled = true,
            category = "Developer Tools",
            iconColor = 0xFF00E676
        ),
        StoreAppItem(
            appId = AppId.WEATHER,
            name = "Nexus Weather Radar",
            developer = "Meteo AI",
            rating = 4.7f,
            sizeMb = 18,
            downloads = "10M+",
            description = "Live dynamic particle forecasts, 7-day outlook, UV index, air quality and radar animations.",
            isInstalled = true,
            category = "Weather",
            iconColor = 0xFF00B0FF
        ),
        StoreAppItem(
            appId = AppId.TASKS,
            name = "Nova Tasks & Planner",
            developer = "Nova Productivity",
            rating = 4.9f,
            sizeMb = 8,
            downloads = "3M+",
            description = "Smart project task manager with subtasks, priority tags, daily completion stats and cloud account sync.",
            isInstalled = true,
            category = "Productivity",
            iconColor = 0xFF43A047
        ),
        StoreAppItem(
            appId = AppId.ACCOUNT,
            name = "Nova Account & Cloud",
            developer = "Nova Core",
            rating = 4.9f,
            sizeMb = 5,
            downloads = "20M+",
            description = "Centralized identity, cloud storage visualizer, security credentials, and multi-device synchronization.",
            isInstalled = true,
            category = "Tools",
            iconColor = 0xFF1976D2
        ),
        StoreAppItem(
            appId = AppId.YOUTUBE,
            name = "YouTube",
            developer = "Google LLC",
            rating = 4.8f,
            sizeMb = 32,
            downloads = "5B+",
            description = "Watch music videos, gaming trailers, tech reviews, comedy shows, and live stream channels securely.",
            isInstalled = false,
            category = "Entertainment",
            iconColor = 0xFFFF0000
        ),
        StoreAppItem(
            appId = AppId.INSTAGRAM,
            name = "Instagram",
            developer = "Meta Platforms, Inc.",
            rating = 4.7f,
            sizeMb = 45,
            downloads = "1B+",
            description = "Share photo moments, view social reels feed, react with comments and browse community stories.",
            isInstalled = false,
            category = "Social Network",
            iconColor = 0xFFE1306C
        ),
        StoreAppItem(
            appId = AppId.GOOGLE,
            name = "Google Search",
            developer = "Google LLC",
            rating = 4.9f,
            sizeMb = 22,
            downloads = "10B+",
            description = "Get instant query search results, browse daily highlights, check forecast widgets and inspect web topics.",
            isInstalled = false,
            category = "Tools & Search",
            iconColor = 0xFF4285F4
        ),
        StoreAppItem(
            appId = AppId.WHATSAPP,
            name = "WhatsApp Messenger",
            developer = "WhatsApp LLC",
            rating = 4.6f,
            sizeMb = 38,
            downloads = "5B+",
            description = "Simple. Reliable. Private. Call and message friends and family for free across devices.",
            isInstalled = false,
            category = "Communication",
            iconColor = 0xFF25D366
        ),
        StoreAppItem(
            appId = AppId.SPOTIFY,
            name = "Spotify: Music & Podcasts",
            developer = "Spotify AB",
            rating = 4.8f,
            sizeMb = 28,
            downloads = "1B+",
            description = "Play millions of songs, albums, and original podcasts. Enjoy high-fidelity audio and personalized playlists.",
            isInstalled = false,
            category = "Music & Audio",
            iconColor = 0xFF1DB954
        ),
        StoreAppItem(
            appId = AppId.MAPS,
            name = "Google Maps",
            developer = "Google LLC",
            rating = 4.7f,
            sizeMb = 42,
            downloads = "10B+",
            description = "Navigate faster and easier with real-time GPS navigation, traffic, transit, and discover local neighborhoods.",
            isInstalled = false,
            category = "Navigation",
            iconColor = 0xFF34A853
        ),
        StoreAppItem(
            appId = AppId.NETFLIX,
            name = "Netflix",
            developer = "Netflix, Inc.",
            rating = 4.5f,
            sizeMb = 52,
            downloads = "1B+",
            description = "Stream award-winning TV shows, movies, documentaries, and stand-up specials on demand.",
            isInstalled = false,
            category = "Entertainment",
            iconColor = 0xFFE50914
        ),
        StoreAppItem(
            appId = AppId.TIKTOK,
            name = "TikTok",
            developer = "TikTok Pte. Ltd.",
            rating = 4.6f,
            sizeMb = 68,
            downloads = "1B+",
            description = "Discover short-form videos, trending music, creative filters, and an endless stream of creators worldwide.",
            isInstalled = false,
            category = "Social & Video",
            iconColor = 0xFF000000
        )
    )

    fun getDefaultUserAccount(): UserAccount = UserAccount(
        id = "user_01",
        firstName = "Alex",
        lastName = "Rivera",
        email = "alex.rivera@novaos.net",
        username = "alexrivera",
        avatarEmoji = "🚀",
        avatarColor = 0xFF1976D2,
        backupEnabled = true,
        storageUsedGb = 14.8,
        totalStorageGb = 100.0,
        lastSyncedTimestamp = System.currentTimeMillis() - 1000 * 60 * 12,
        bio = "Exploring NovaOS next-gen mobile experience!"
    )

    fun getInitialTasks(): List<TaskItem> = listOf(
        TaskItem(
            id = "t1",
            title = "Personalize NovaOS Desktop 🎨",
            description = "Switch Material You dynamic palette and test light vs dark mode in Quick Settings.",
            category = "Personal",
            priority = TaskPriority.HIGH,
            dueDate = "Today",
            isCompleted = true,
            subtasks = listOf(
                "Test Ocean Blue and Sunset Orange palettes" to true,
                "Toggle Dark Mode from Notification Shade" to true,
                "Set dynamic wallpaper" to false
            )
        ),
        TaskItem(
            id = "t2",
            title = "Set Up NovaOS Cloud Account ☁️",
            description = "Verify backup status, review 100 GB storage breakdown, and sync connected devices.",
            category = "Work",
            priority = TaskPriority.HIGH,
            dueDate = "Today",
            isCompleted = false,
            subtasks = listOf(
                "Verify email address and profile avatar" to true,
                "Enable automated photo and notes backup" to false,
                "Link secondary tablet device" to false
            )
        ),
        TaskItem(
            id = "t3",
            title = "Explore Built-in Arcade Games 🎮",
            description = "Try 2048 Puzzle or Retro Snake in the Arcade app during break time.",
            category = "Ideas",
            priority = TaskPriority.LOW,
            dueDate = "Tomorrow",
            isCompleted = false,
            subtasks = listOf(
                "Reach 1024 tile in 2048" to false,
                "Beat high score in Snake" to false
            )
        ),
        TaskItem(
            id = "t4",
            title = "Grocery run for artisan sourdough 🥖",
            description = "Pick up cold brew coffee beans, Greek yogurt, and dark chocolate 85%.",
            category = "Personal",
            priority = TaskPriority.MEDIUM,
            dueDate = "Saturday",
            isCompleted = false,
            subtasks = emptyList()
        ),
        TaskItem(
            id = "t5",
            title = "Deploy Kernel Optimizations ⚡",
            description = "Review Compose 120Hz smooth scrolling frame rate metrics.",
            category = "Work",
            priority = TaskPriority.MEDIUM,
            dueDate = "Next Week",
            isCompleted = true,
            subtasks = emptyList()
        )
    )

    fun getInitialWifiNetworks(): List<Pair<String, Boolean>> = listOf(
        "Nova-Fiber_Ultra_5G" to true,
        "Pixel_Home_Fast" to true,
        "Starlink_Guest_Access" to false,
        "CyberCafe_Public_WiFi" to false,
        "Airport_Free_5G" to false
    )

    fun getInitialWeather(): Pair<List<HourlyForecast>, List<WeatherForecast>> {
        val hourly = listOf(
            HourlyForecast("Now", 72, "Sunny", "sunny"),
            HourlyForecast("11 AM", 75, "Partly Cloudy", "cloudy"),
            HourlyForecast("12 PM", 77, "Clear", "sunny"),
            HourlyForecast("1 PM", 79, "Warm", "sunny"),
            HourlyForecast("2 PM", 80, "Partly Cloudy", "cloudy"),
            HourlyForecast("3 PM", 78, "Light Rain", "rain"),
            HourlyForecast("4 PM", 74, "Clear", "sunny"),
            HourlyForecast("5 PM", 71, "Sunset Gold", "sunset"),
            HourlyForecast("6 PM", 68, "Clear Night", "night")
        )
        val daily = listOf(
            WeatherForecast("Today", "Sunny & Warm", 80, 62, 10, "sunny"),
            WeatherForecast("Sat", "Partly Cloudy", 78, 60, 20, "cloudy"),
            WeatherForecast("Sun", "Golden Clear", 82, 64, 5, "sunny"),
            WeatherForecast("Mon", "Scattered Showers", 73, 58, 65, "rain"),
            WeatherForecast("Tue", "Thunderstorms", 70, 56, 80, "thunder"),
            WeatherForecast("Wed", "Mild Breeze", 75, 59, 15, "wind"),
            WeatherForecast("Thu", "Crystal Blue Sky", 79, 61, 0, "sunny")
        )
        return Pair(hourly, daily)
    }

    fun getInitialNotifications(): List<OSNotification> = listOf(
        OSNotification(
            id = "notif1",
            appId = AppId.MESSAGES,
            title = "Mom ❤️",
            message = "Wonderful! Can't wait to see you. Love you! 💕",
            actionLabel = "Reply"
        ),
        OSNotification(
            id = "notif2",
            appId = AppId.MESSAGES,
            title = "Pizza Express 🍕",
            message = "Courier #48 is 5 minutes away with your order! 🛵",
            actionLabel = "Track Order"
        ),
        OSNotification(
            id = "notif3",
            appId = AppId.SETTINGS,
            title = "NovaOS Update Ready",
            message = "Baklava edition with enhanced Material You Dynamic Color and Fluid Navigation is ready.",
            actionLabel = "View Details"
        )
    )
}
