package com.example.ui.apps

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.launch

enum class DrawingTool(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PENCIL("Kalem", Icons.Filled.Edit),
    BRUSH("Fırça", Icons.Filled.Brush),
    MARKER("Fosforlu", Icons.Filled.BorderColor),
    NEON("Neon Parıltı", Icons.Filled.AutoAwesome),
    CALLIGRAPHY("Dolma Kalem", Icons.Filled.FormatPaint),
    ERASER("Silgi", Icons.Filled.CleaningServices),
    SHAPE_LINE("Çizgi", Icons.Filled.HorizontalRule),
    SHAPE_CIRCLE("Daire", Icons.Filled.Circle),
    SHAPE_RECT("Dikdörtgen", Icons.Filled.CropSquare),
    SHAPE_STAR("Yıldız", Icons.Filled.Star),
    STAMP("Damga", Icons.Filled.Favorite)
}

enum class CanvasBackground(val title: String, val color: Color, val gridType: String) {
    WHITE("Beyaz Kağıt", Color(0xFFFCFCFC), "NONE"),
    DARK("OLED Siyah", Color(0xFF12141A), "NONE"),
    GRID("Kareli Defter", Color(0xFFF7F8FA), "GRID"),
    LINED("Çizgili Sayfa", Color(0xFFFFFDF5), "LINED"),
    BLUEPRINT("Mavi Taslak", Color(0xFF0F2B48), "GRID"),
    PARCHMENT("Parşömen", Color(0xFFF5ECD7), "NONE")
}

data class DrawPathItem(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val alpha: Float,
    val tool: DrawingTool,
    val stampChar: String = "⭐"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateHome()
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // History & Canvas State
    val paths = remember { mutableStateListOf<DrawPathItem>() }
    val undonePaths = remember { mutableStateListOf<DrawPathItem>() }

    // Current Tool Settings
    var currentTool by remember { mutableStateOf(DrawingTool.BRUSH) }
    var selectedColor by remember { mutableStateOf(Color(0xFFE53935)) }
    var strokeWidth by remember { mutableFloatStateOf(12f) }
    var strokeOpacity by remember { mutableFloatStateOf(1f) }
    var canvasBg by remember { mutableStateOf(CanvasBackground.WHITE) }
    var selectedStamp by remember { mutableStateOf("⭐") }

    // Active drag path
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Dialogs
    var showClearDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var drawingTitle by remember { mutableStateOf("Vroxen UI Çizimim") }
    var showBgDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }

    val presetColors = remember {
        listOf(
            Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFFE53935), Color(0xFFD81B60),
            Color(0xFF8E24AA), Color(0xFF5E35B1), Color(0xFF1E88E5), Color(0xFF00ACC1),
            Color(0xFF00897B), Color(0xFF43A047), Color(0xFF7CB342), Color(0xFFFDD835),
            Color(0xFFFB8C00), Color(0xFFF4511E), Color(0xFF6D4C41), Color(0xFF546E7A),
            Color(0xFF00E5FF), Color(0xFFFF007F)
        )
    }

    val presetStamps = remember {
        listOf("⭐", "❤️", "🔥", "✨", "🤖", "🎨", "🚀", "🌸", "👑", "🍕", "⚡", "🍀")
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Draw,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Vroxen UI Çizim & Sanat",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "${paths.size} Çizgi • ${canvasBg.title}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    // Undo
                    IconButton(
                        onClick = {
                            if (paths.isNotEmpty()) {
                                val last = paths.removeAt(paths.size - 1)
                                undonePaths.add(last)
                            }
                        },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Geri Al")
                    }

                    // Redo
                    IconButton(
                        onClick = {
                            if (undonePaths.isNotEmpty()) {
                                val last = undonePaths.removeAt(undonePaths.size - 1)
                                paths.add(last)
                            }
                        },
                        enabled = undonePaths.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "İleri Al")
                    }

                    // Canvas Background Switcher
                    IconButton(onClick = { showBgDialog = true }) {
                        Icon(Icons.Default.Layers, contentDescription = "Arka Plan")
                    }

                    // Clear Canvas
                    IconButton(
                        onClick = { showClearDialog = true },
                        enabled = paths.isNotEmpty()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Temizle")
                    }

                    // Save to Gallery
                    Button(
                        onClick = { showSaveDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kaydet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Interactive Bottom Tool Station
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Tool Selector Ribbon
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(DrawingTool.entries) { tool ->
                            val isSelected = currentTool == tool
                            FilterChip(
                                selected = isSelected,
                                onClick = { currentTool = tool },
                                leadingIcon = {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tool.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // 2. Color Palette & Thickness / Opacity Sliders
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Quick Color Swatches
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(presetColors) { color ->
                                val isSelected = selectedColor == color
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(if (color == Color.White) Color.Black else Color.White, CircleShape)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Stroke Width Indicator & Slider Control
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((strokeWidth / 2.5f).coerceIn(6f, 22f).dp)
                                        .background(selectedColor, CircleShape)
                                )
                                Text(
                                    text = "${strokeWidth.toInt()}px",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 3. Stroke Width Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.LineWeight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Slider(
                            value = strokeWidth,
                            onValueChange = { strokeWidth = it },
                            valueRange = 2f..50f,
                            modifier = Modifier.weight(1f)
                        )

                        if (currentTool == DrawingTool.STAMP) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(presetStamps) { stamp ->
                                    Text(
                                        text = stamp,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (selectedStamp == stamp) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable { selectedStamp = stamp }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // Main Interactive Drawing Canvas Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(canvasBg.color)
                .pointerInput(currentTool, selectedColor, strokeWidth, strokeOpacity, selectedStamp) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentPoints = listOf(offset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            currentPoints = currentPoints + change.position
                        },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                val itemColor = if (currentTool == DrawingTool.ERASER) canvasBg.color else selectedColor
                                val alpha = if (currentTool == DrawingTool.MARKER) 0.45f else strokeOpacity

                                paths.add(
                                    DrawPathItem(
                                        points = currentPoints,
                                        color = itemColor,
                                        strokeWidth = strokeWidth,
                                        alpha = alpha,
                                        tool = currentTool,
                                        stampChar = selectedStamp
                                    )
                                )
                                undonePaths.clear()
                                currentPoints = emptyList()
                            }
                        }
                    )
                }
                .pointerInput(currentTool, selectedColor, strokeWidth, selectedStamp) {
                    detectTapGestures { tapOffset ->
                        val itemColor = if (currentTool == DrawingTool.ERASER) canvasBg.color else selectedColor
                        paths.add(
                            DrawPathItem(
                                points = listOf(tapOffset, tapOffset + Offset(1f, 1f)),
                                color = itemColor,
                                strokeWidth = strokeWidth,
                                alpha = strokeOpacity,
                                tool = currentTool,
                                stampChar = selectedStamp
                            )
                        )
                        undonePaths.clear()
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw Grid or Lines if configured
                if (canvasBg.gridType == "GRID") {
                    val step = 40.dp.toPx()
                    val gridColor = Color.Black.copy(alpha = 0.07f)
                    var x = step
                    while (x < size.width) {
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                        x += step
                    }
                    var y = step
                    while (y < size.height) {
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                        y += step
                    }
                } else if (canvasBg.gridType == "LINED") {
                    val step = 36.dp.toPx()
                    val lineColor = Color(0xFF2196F3).copy(alpha = 0.15f)
                    var y = step
                    while (y < size.height) {
                        drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5f)
                        y += step
                    }
                }

                // 2. Draw Committed Paths
                paths.forEach { item ->
                    renderPathItem(item)
                }

                // 3. Draw Active In-Progress Drag Path
                if (currentPoints.isNotEmpty()) {
                    val activeColor = if (currentTool == DrawingTool.ERASER) canvasBg.color else selectedColor
                    val activeAlpha = if (currentTool == DrawingTool.MARKER) 0.45f else strokeOpacity
                    renderPathItem(
                        DrawPathItem(
                            points = currentPoints,
                            color = activeColor,
                            strokeWidth = strokeWidth,
                            alpha = activeAlpha,
                            tool = currentTool,
                            stampChar = selectedStamp
                        )
                    )
                }
            }

            // Watermark Hint if canvas is empty
            if (paths.isEmpty() && currentPoints.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gesture,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Parmağınızla çizmeye başlayın",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Clear Canvas Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Default.DeleteSweep, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Tuvali Temizle") },
            text = { Text("Tüm çiziminiz silinecek. Devam etmek istiyor musunuz?") },
            confirmButton = {
                Button(
                    onClick = {
                        paths.clear()
                        undonePaths.clear()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Temizle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Save to Gallery Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            icon = { Icon(Icons.Default.Collections, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Galeriye Kaydet") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Çiziminiz Vroxen UI Galeri (Fotoğraflar) uygulamasına yüksek kalitede eklenecek.")
                    OutlinedTextField(
                        value = drawingTitle,
                        onValueChange = { drawingTitle = it },
                        label = { Text("Çizim Başlığı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveDrawingPhoto(drawingTitle)
                        showSaveDialog = false
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("🎨 \"$drawingTitle\" galeriye kaydedildi!")
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    // Canvas Background Dialog
    if (showBgDialog) {
        AlertDialog(
            onDismissRequest = { showBgDialog = false },
            title = { Text("Tuval Arka Planı Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CanvasBackground.entries.forEach { bg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (canvasBg == bg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    canvasBg = bg
                                    showBgDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(bg.color)
                                            .border(1.dp, Color.Black.copy(alpha = 0.2f), CircleShape)
                                    )
                                    Text(
                                        text = bg.title,
                                        fontWeight = if (canvasBg == bg) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                if (canvasBg == bg) {
                                    Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBgDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.renderPathItem(item: DrawPathItem) {
    if (item.points.isEmpty()) return

    when (item.tool) {
        DrawingTool.SHAPE_LINE -> {
            if (item.points.size >= 2) {
                drawLine(
                    color = item.color.copy(alpha = item.alpha),
                    start = item.points.first(),
                    end = item.points.last(),
                    strokeWidth = item.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
        DrawingTool.SHAPE_CIRCLE -> {
            if (item.points.size >= 2) {
                val start = item.points.first()
                val end = item.points.last()
                val center = Offset((start.x + end.x) / 2f, (start.y + end.y) / 2f)
                val radius = (end - start).getDistance() / 2f
                drawCircle(
                    color = item.color.copy(alpha = item.alpha),
                    radius = radius,
                    center = center,
                    style = Stroke(width = item.strokeWidth)
                )
            }
        }
        DrawingTool.SHAPE_RECT -> {
            if (item.points.size >= 2) {
                val start = item.points.first()
                val end = item.points.last()
                val topLeft = Offset(minOf(start.x, end.x), minOf(start.y, end.y))
                val size = Size(kotlin.math.abs(end.x - start.x), kotlin.math.abs(end.y - start.y))
                drawRect(
                    color = item.color.copy(alpha = item.alpha),
                    topLeft = topLeft,
                    size = size,
                    style = Stroke(width = item.strokeWidth)
                )
            }
        }
        DrawingTool.NEON -> {
            // Neon Glow: Outer aura + inner bright line
            val path = Path().apply {
                moveTo(item.points.first().x, item.points.first().y)
                for (p in item.points) {
                    lineTo(p.x, p.y)
                }
            }
            // Glow aura
            drawPath(
                path = path,
                color = item.color.copy(alpha = 0.35f),
                style = Stroke(width = item.strokeWidth * 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            // Core line
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.9f),
                style = Stroke(width = item.strokeWidth * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        DrawingTool.CALLIGRAPHY -> {
            // Angled calligraphy stroke
            for (i in 0 until item.points.size - 1) {
                val p1 = item.points[i]
                val p2 = item.points[i + 1]
                drawLine(
                    color = item.color.copy(alpha = item.alpha),
                    start = p1,
                    end = p2,
                    strokeWidth = item.strokeWidth,
                    cap = StrokeCap.Square
                )
            }
        }
        else -> {
            // Standard smooth path for Pencil, Brush, Marker, Eraser
            if (item.points.size == 1) {
                drawCircle(
                    color = item.color.copy(alpha = item.alpha),
                    radius = item.strokeWidth / 2f,
                    center = item.points.first()
                )
            } else {
                val path = Path().apply {
                    moveTo(item.points.first().x, item.points.first().y)
                    for (i in 1 until item.points.size) {
                        val p = item.points[i]
                        val prev = item.points[i - 1]
                        quadraticTo(prev.x, prev.y, (p.x + prev.x) / 2f, (p.y + prev.y) / 2f)
                    }
                    lineTo(item.points.last().x, item.points.last().y)
                }
                drawPath(
                    path = path,
                    color = item.color.copy(alpha = item.alpha),
                    style = Stroke(
                        width = item.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
