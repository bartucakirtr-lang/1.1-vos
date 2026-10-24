package com.example.ui.apps

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WallpaperCatalog
import com.example.model.*
import com.example.ui.system.DynamicWallpaperRenderer
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperManagerApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val homeWallpaper by viewModel.homeWallpaper.collectAsState()
    val lockWallpaper by viewModel.lockWallpaper.collectAsState()
    val liveSpeed by viewModel.liveWallpaperSpeed.collectAsState()
    val liveInteractive by viewModel.liveWallpaperInteractive.collectAsState()

    var selectedCategory by remember { mutableStateOf(WallpaperCategory.ALL) }
    var selectedWallpaper by remember { mutableStateOf(homeWallpaper) }
    var previewMode by remember { mutableStateOf(WallpaperTarget.HOME) } // HOME or LOCK mockup
    var showApplyBottomSheet by remember { mutableStateOf(false) }
    var showLiveSettingsDialog by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo Picker for Custom Gallery Wallpapers
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val customDef = WallpaperDefinition(
                id = "custom_" + System.currentTimeMillis(),
                title = "Galeriden Seçilen",
                category = WallpaperCategory.CUSTOM,
                sourceType = WallpaperSourceType.CUSTOM_GALLERY,
                customImageUri = uri.toString(),
                badge = "Özel Galeri"
            )
            selectedWallpaper = customDef
            showApplyBottomSheet = true
        }
    }

    val filteredWallpapers = remember(selectedCategory) {
        when (selectedCategory) {
            WallpaperCategory.ALL -> WallpaperCatalog.ALL_WALLPAPERS
            WallpaperCategory.LIVE -> WallpaperCatalog.ALL_WALLPAPERS.filter { it.category == WallpaperCategory.LIVE }
            WallpaperCategory.STATIC -> WallpaperCatalog.ALL_WALLPAPERS.filter { it.category == WallpaperCategory.STATIC }
            WallpaperCategory.GRADIENTS -> WallpaperCatalog.ALL_WALLPAPERS.filter { it.category == WallpaperCategory.GRADIENTS }
            WallpaperCategory.SYSTEM -> WallpaperCatalog.ALL_WALLPAPERS.filter { it.category == WallpaperCategory.SYSTEM }
            WallpaperCategory.CUSTOM -> WallpaperCatalog.ALL_WALLPAPERS.filter { it.category == WallpaperCategory.CUSTOM }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Duvar Kağıdı Yöneticisi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Ana & Kilit Ekranı Özelleştirme",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.closeCurrentApp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Quick Pick Photo from Gallery Button
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Galeriden Seç",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Prominent Sticky "Duvar Kağıdı Olarak Ayarla" Button
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedWallpaper.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (selectedWallpaper.sourceType == WallpaperSourceType.LIVE_ANIMATED) "Canlı 60FPS Animasyon" else "Yüksek Çözünürlüklü Arka Plan",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showApplyBottomSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ayarla & Uygula", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -------------------------------------------------------------
            // 1. INTERACTIVE PHONE MOCKUP PREVIEW STAGE
            // -------------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview Switcher (Home Screen vs Lock Screen Mockup)
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        SegmentedButton(
                            selected = previewMode == WallpaperTarget.HOME,
                            onClick = { previewMode = WallpaperTarget.HOME },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text("Ana Ekran Önizleme", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        SegmentedButton(
                            selected = previewMode == WallpaperTarget.LOCK,
                            onClick = { previewMode = WallpaperTarget.LOCK },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = { Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp)) }
                        ) {
                            Text("Kilit Ekranı Önizleme", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Realistic Phone Frame Mockup
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color.Black,
                        shadowElevation = 14.dp,
                        border = BorderStroke(3.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .width(220.dp)
                            .height(390.dp)
                            .clip(RoundedCornerShape(32.dp))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // The Live Wallpaper Background Renderer
                            DynamicWallpaperRenderer(
                                wallpaper = selectedWallpaper,
                                speedMultiplier = liveSpeed,
                                isInteractive = true,
                                showVignetteOverlay = true,
                                modifier = Modifier.fillMaxSize()
                            )

                            // UI Overlays on top of Wallpaper
                            if (previewMode == WallpaperTarget.HOME) {
                                HomeMockupOverlay(viewModel)
                            } else {
                                LockMockupOverlay(viewModel)
                            }

                            // Dynamic Live Wallpaper Indicator Badge
                            if (selectedWallpaper.sourceType == WallpaperSourceType.LIVE_ANIMATED) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00FF66))
                                        )
                                        Text(
                                            "Canlı & Dokunmatik",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Live Wallpaper Controls Button if live wallpaper is active
                    if (selectedWallpaper.sourceType == WallpaperSourceType.LIVE_ANIMATED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showLiveSettingsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Tune, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Animasyon Hızı & Dokunma Ayarı", fontSize = 11.sp)
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. CATEGORY FILTER CHIPS
            // -------------------------------------------------------------
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(WallpaperCategory.entries) { category ->
                        val isSelected = selectedCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.title, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    when (category) {
                                        WallpaperCategory.ALL -> Icons.Default.AllInclusive
                                        WallpaperCategory.LIVE -> Icons.Default.AutoAwesome
                                        WallpaperCategory.STATIC -> Icons.Default.Landscape
                                        WallpaperCategory.GRADIENTS -> Icons.Default.Palette
                                        WallpaperCategory.SYSTEM -> Icons.Default.PhoneAndroid
                                        WallpaperCategory.CUSTOM -> Icons.Default.AddPhotoAlternate
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 3. WALLPAPER SELECTION GRID
            // -------------------------------------------------------------
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "${selectedCategory.title} (${filteredWallpapers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Custom Photo Tile Action if in CUSTOM or ALL category
                    if (selectedCategory == WallpaperCategory.CUSTOM || selectedCategory == WallpaperCategory.ALL) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Galeriden Kendi Fotoğrafını Yükle",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Cihazınızdaki herhangi bir resmi ana veya kilit ekranına uygulayın",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // 2-Column Responsive Wallpaper Cards Grid
                    val rows = filteredWallpapers.chunked(2)
                    rows.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { wp ->
                                val isSelected = selectedWallpaper.id == wp.id
                                val isHomeCurrent = homeWallpaper.id == wp.id
                                val isLockCurrent = lockWallpaper.id == wp.id

                                WallpaperGridCard(
                                    wallpaper = wp,
                                    isSelected = isSelected,
                                    isHomeCurrent = isHomeCurrent,
                                    isLockCurrent = isLockCurrent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(180.dp),
                                    onClick = { selectedWallpaper = wp }
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // 4. "UYGULA / AYARLA" MODAL BOTTOM SHEET
    // -------------------------------------------------------------
    if (showApplyBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showApplyBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            "Duvar Kağıdını Nereye Uygulayalım?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            selectedWallpaper.title,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider()

                // Option 1: Home Screen Only
                WallpaperApplyOptionTile(
                    icon = Icons.Default.Home,
                    title = "Ana Ekran Duvar Kağıdı",
                    subtitle = "Sadece ana ekran ve uygulama çekmecesinde görünür",
                    onClick = {
                        viewModel.setWallpaperTargeted(WallpaperTarget.HOME, selectedWallpaper)
                        Toast.makeText(context, "${selectedWallpaper.title} Ana Ekrana uygulandı ✨", Toast.LENGTH_SHORT).show()
                        showApplyBottomSheet = false
                    }
                )

                // Option 2: Lock Screen Only
                WallpaperApplyOptionTile(
                    icon = Icons.Default.Lock,
                    title = "Kilit Ekranı Duvar Kağıdı",
                    subtitle = "Sadece telefon kilitliyken kilit ekranında görünür",
                    onClick = {
                        viewModel.setWallpaperTargeted(WallpaperTarget.LOCK, selectedWallpaper)
                        Toast.makeText(context, "${selectedWallpaper.title} Kilit Ekranına uygulandı 🔒", Toast.LENGTH_SHORT).show()
                        showApplyBottomSheet = false
                    }
                )

                // Option 3: Both Screens
                WallpaperApplyOptionTile(
                    icon = Icons.Default.Sync,
                    title = "Her İki Ekrana da Uygula",
                    subtitle = "Hem ana ekran hem de kilit ekranı aynı arka planı kullanır",
                    isPrimary = true,
                    onClick = {
                        viewModel.setWallpaperTargeted(WallpaperTarget.BOTH, selectedWallpaper)
                        Toast.makeText(context, "${selectedWallpaper.title} Her İki Ekrana uygulandı 🌟", Toast.LENGTH_SHORT).show()
                        showApplyBottomSheet = false
                    }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // 5. LIVE WALLPAPER SPEED & INTERACTION SETTINGS DIALOG
    // -------------------------------------------------------------
    if (showLiveSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showLiveSettingsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Canlı Duvar Kağıdı Ayarları", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Speed Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Animasyon Hızı", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("${String.format(Locale.US, "%.1fx", liveSpeed)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = liveSpeed,
                            onValueChange = { viewModel.setLiveWallpaperSpeed(it) },
                            valueRange = 0.4f..2.5f,
                            steps = 6
                        )
                    }

                    // Interactive Touch Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dokunma Etkileşimi", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Ekrana dokunduğunuzda parçacık ve dalga tepkisi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = liveInteractive,
                            onCheckedChange = { viewModel.setLiveWallpaperInteractive(it) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLiveSettingsDialog = false }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@Composable
private fun WallpaperGridCard(
    wallpaper: WallpaperDefinition,
    isSelected: Boolean,
    isHomeCurrent: Boolean,
    isLockCurrent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            if (isSelected) 2.5.dp else 1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Live / Static Mini Preview
            DynamicWallpaperRenderer(
                wallpaper = wallpaper,
                speedMultiplier = 1.0f,
                isInteractive = false,
                showVignetteOverlay = true,
                modifier = Modifier.fillMaxSize()
            )

            // Dark gradient overlay at bottom for title readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Top Badges (Category & Active Status)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (wallpaper.badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (wallpaper.sourceType) {
                            WallpaperSourceType.LIVE_ANIMATED -> Color(0xFF00E5FF).copy(alpha = 0.9f)
                            WallpaperSourceType.CUSTOM_GALLERY -> Color(0xFFFF4081).copy(alpha = 0.9f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        }
                    ) {
                        Text(
                            text = wallpaper.badge,
                            color = Color.Black,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Active Home/Lock Badge
                if (isHomeCurrent && isLockCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.95f)
                    ) {
                        Text(
                            text = "Aktif (Tümü)",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else if (isHomeCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF2196F3).copy(alpha = 0.95f)
                    ) {
                        Text(
                            text = "Ana Ekran",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else if (isLockCurrent) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.95f)
                    ) {
                        Text(
                            text = "Kilit Ekranı",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Selection Checkmark
            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Seçili",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Bottom Title
            Text(
                text = wallpaper.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun WallpaperApplyOptionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, if (isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.CheckCircleOutline,
                contentDescription = null,
                tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------
// HOME MOCKUP OVERLAY (Simulates status bar, search bar, app icons dock)
// -------------------------------------------------------------
@Composable
private fun HomeMockupOverlay(viewModel: OSViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status bar & Time chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("12:45", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(Icons.Default.Wifi, null, tint = Color.White, modifier = Modifier.size(10.dp))
                Icon(Icons.Default.BatteryChargingFull, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }

        // Center Widget Mockup
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.35f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Çarşamba, 7 Ekim", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
                Text("12:45", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("24°C • Güneşli", color = Color.White.copy(alpha = 0.8f), fontSize = 8.sp)
            }
        }

        // Bottom Dock Icons Mockup
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Pair(Icons.Default.Phone, Color(0xFF00C853)),
                    Pair(Icons.Default.Message, Color(0xFF1E88E5)),
                    Pair(Icons.Default.CameraAlt, Color(0xFFE53935)),
                    Pair(Icons.Default.Settings, Color(0xFF546E7A))
                ).forEach { (icon, bg) ->
                    Surface(
                        shape = CircleShape,
                        color = bg,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// LOCK MOCKUP OVERLAY (Simulates big lock clock, date, notification, unlock chip)
// -------------------------------------------------------------
@Composable
private fun LockMockupOverlay(viewModel: OSViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Security Lock Icon
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(14.dp)
        )

        // Large Modern Lock Clock
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "12:45",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Çarşamba, 7 Ekim",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
        }

        // Notification Pill
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.40f),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(10.dp))
                Text("Tüm bildirimler güncel", color = Color.White, fontSize = 8.sp)
            }
        }

        // Bottom Shortcuts (Flashlight & Camera)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FlashlightOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
