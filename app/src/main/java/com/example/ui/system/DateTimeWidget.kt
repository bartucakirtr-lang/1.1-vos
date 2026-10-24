package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlarmItem
import com.example.model.AppId
import com.example.model.ClockStyle
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Persistent At-A-Glance Date and Time widget for the Home Screen.
 * Updates in real-time matching the system font, Material 3 color tokens, and design language.
 */
@Composable
fun PersistentDateTimeHeader(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    onOpenClock: () -> Unit = { viewModel.openApp(AppId.CLOCK) },
    onOpenTasks: () -> Unit = { viewModel.openApp(AppId.TASKS) },
    onOpenWeather: () -> Unit = { viewModel.openApp(AppId.WEATHER) }
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val isDark = LocalIsDarkMode.current

    // Localized Date & Time Formatters
    val locale = remember(currentLang) {
        val iso = com.example.util.TranslationManager.LANGUAGE_ISO_MAP[currentLang] ?: "en"
        Locale.forLanguageTag(iso)
    }

    val timeFormatted = remember(systemTime, locale) {
        SimpleDateFormat("HH:mm", locale).format(Date(systemTime))
    }
    val secondsFormatted = remember(systemTime, locale) {
        SimpleDateFormat("ss", locale).format(Date(systemTime))
    }
    val dayOfWeek = remember(systemTime, locale) {
        SimpleDateFormat("EEEE", locale).format(Date(systemTime))
    }
    val dateFull = remember(systemTime, locale) {
        SimpleDateFormat("d MMMM yyyy", locale).format(Date(systemTime))
    }

    val nextAlarm = remember(alarms) {
        alarms.firstOrNull { it.isEnabled }
    }

    // Subtle breathing pulse for live time colon
    val infiniteTransition = rememberInfiniteTransition(label = "time_pulse")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_alpha"
    )

    val containerBg = if (isDark) {
        Color.Black.copy(alpha = 0.38f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }
    val borderStroke = BorderStroke(
        1.dp,
        if (isDark) Color.White.copy(alpha = 0.10f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = containerBg,
        border = borderStroke,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Real-time Digital Clock & Live Day/Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenClock() }
                    .padding(vertical = 2.dp, horizontal = 4.dp)
            ) {
                // Clock Icon / Accent Dot Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "Saat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.Center) {
                    // Time with live pulsing colon & live seconds
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val hours = timeFormatted.substringBefore(":")
                        val minutes = timeFormatted.substringAfter(":")

                        Text(
                            text = hours,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF191C1E),
                            fontSize = 20.sp,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = colonAlpha),
                            fontSize = 20.sp,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = minutes,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF191C1E),
                            fontSize = 20.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = secondsFormatted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Localized Full Date & Day
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dayOfWeek.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)
                        )
                        Text(
                            text = dateFull,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF43474E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right: Interactive Glance Chips (Weather & Alarm Status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Weather Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.60f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenWeather() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.WbSunny,
                            contentDescription = "Hava Durumu",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "24°C",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Next Alarm Chip or Tasks Shortcut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (nextAlarm != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.80f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (nextAlarm != null) onOpenClock() else onOpenTasks()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            if (nextAlarm != null) Icons.Filled.Alarm else Icons.Filled.EventNote,
                            contentDescription = null,
                            tint = if (nextAlarm != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = nextAlarm?.timeFormatted ?: "Takvim",
                            fontSize = 11.sp,
                            fontWeight = if (nextAlarm != null) FontWeight.Bold else FontWeight.Medium,
                            color = if (nextAlarm != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Rich Interactive Date & Time Clock Widget for the Home Screen Widget Area.
 * Supports multiple presentation modes (Material 3 Digital, Analog Canvas Dial, Pixel Bold, World Clock Dual).
 */
@Composable
fun DateTimeClockCard(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    onOpenClock: () -> Unit = { viewModel.openApp(AppId.CLOCK) }
) {
    val systemTime by viewModel.systemTime.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val clockStyle by viewModel.clockStyle.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val worldClocks by viewModel.worldClocks.collectAsState()
    val isDark = LocalIsDarkMode.current

    var selectedFaceMode by remember { mutableIntStateOf(0) } // 0: Material Digital & Alarm, 1: Canvas Analog Clock, 2: World Dual Zone

    val locale = remember(currentLang) {
        val iso = com.example.util.TranslationManager.LANGUAGE_ISO_MAP[currentLang] ?: "en"
        Locale.forLanguageTag(iso)
    }

    val cal = remember(systemTime) {
        Calendar.getInstance().apply { timeInMillis = systemTime }
    }
    val hours = cal.get(Calendar.HOUR_OF_DAY)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    val timeFormatted = remember(systemTime, locale) {
        SimpleDateFormat("HH:mm", locale).format(Date(systemTime))
    }
    val dayOfWeek = remember(systemTime, locale) {
        SimpleDateFormat("EEEE", locale).format(Date(systemTime))
    }
    val dateMonth = remember(systemTime, locale) {
        SimpleDateFormat("d MMMM yyyy", locale).format(Date(systemTime))
    }
    val nextAlarm = remember(alarms) {
        alarms.firstOrNull { it.isEnabled }
    }

    val primaryText = if (isDark) Color.White else Color(0xFF191C1E)
    val secondaryText = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF43474E)

    // Animated seconds progress for digital ring
    val secondsProgress = (seconds / 60f)

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        when (selectedFaceMode) {
            0 -> {
                // Mode 0: Material 3 Digital Clock & Alarm Quick Center
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Column: Big Digital Clock with live seconds bar & full date
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = timeFormatted,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                lineHeight = 36.sp,
                                style = MaterialTheme.typography.displaySmall
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = if (seconds < 10) "0$seconds" else "$seconds",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Localized Day & Date
                        Text(
                            text = "${dayOfWeek.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }}, $dateMonth",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = secondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Seconds progress bar
                        LinearProgressIndicator(
                            progress = { secondsProgress },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.10f)
                        )
                    }

                    // Right Column: Next Alarm Pill & Quick Controls
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (nextAlarm != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (nextAlarm != null) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (nextAlarm != null) {
                                        viewModel.toggleAlarm(nextAlarm.id)
                                    } else {
                                        onOpenClock()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        if (nextAlarm != null) Icons.Filled.AlarmOn else Icons.Filled.AlarmOff,
                                        contentDescription = null,
                                        tint = if (nextAlarm != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Column {
                                        Text(
                                            text = nextAlarm?.timeFormatted ?: "Alarm Yok",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (nextAlarm != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = nextAlarm?.label ?: "Dokun ve kur",
                                            fontSize = 9.sp,
                                            color = if (nextAlarm != null) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (nextAlarm != null) {
                                    Switch(
                                        checked = nextAlarm.isEnabled,
                                        onCheckedChange = { viewModel.toggleAlarm(nextAlarm.id) },
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }
                            }
                        }

                        // Quick buttons (Timer & Stopwatch launcher)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    viewModel.setClockTab(3) // Timer tab
                                    onOpenClock()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Filled.HourglassEmpty, null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Zamanlayıcı", fontSize = 9.sp)
                            }

                            FilledTonalButton(
                                onClick = {
                                    viewModel.setClockTab(2) // Stopwatch tab
                                    onOpenClock()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Icon(Icons.Filled.Timer, null, modifier = Modifier.size(11.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Kronometre", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Mode 1: Canvas Analog Clock Dial + Digital Glance
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Analog Dial Canvas
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(105.dp)
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val onSurfaceColor = primaryText
                        val accentColor = Color(0xFFFF5252)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val radius = size.minDimension / 2f - 4.dp.toPx()

                            // Outer dial ring
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.25f),
                                radius = radius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 3.dp.toPx())
                            )

                            // 12 Hour ticks
                            for (i in 0 until 12) {
                                val angleDeg = i * 30.0
                                val angleRad = Math.toRadians(angleDeg)
                                val tickLength = if (i % 3 == 0) 8.dp.toPx() else 4.dp.toPx()
                                val startX = centerX + (radius - tickLength) * sin(angleRad).toFloat()
                                val startY = centerY - (radius - tickLength) * cos(angleRad).toFloat()
                                val endX = centerX + radius * sin(angleRad).toFloat()
                                val endY = centerY - radius * cos(angleRad).toFloat()

                                drawLine(
                                    color = if (i % 3 == 0) primaryColor else onSurfaceColor.copy(alpha = 0.4f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = if (i % 3 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Hour hand (smooth)
                            val hourAngle = ((hours % 12) + minutes / 60f) * 30.0
                            val hourRad = Math.toRadians(hourAngle)
                            val hourLength = radius * 0.52f
                            val hourEndX = centerX + hourLength * sin(hourRad).toFloat()
                            val hourEndY = centerY - hourLength * cos(hourRad).toFloat()
                            drawLine(
                                color = onSurfaceColor,
                                start = Offset(centerX, centerY),
                                end = Offset(hourEndX, hourEndY),
                                strokeWidth = 3.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Minute hand
                            val minAngle = (minutes + seconds / 60f) * 6.0
                            val minRad = Math.toRadians(minAngle)
                            val minLength = radius * 0.75f
                            val minEndX = centerX + minLength * sin(minRad).toFloat()
                            val minEndY = centerY - minLength * cos(minRad).toFloat()
                            drawLine(
                                color = primaryColor,
                                start = Offset(centerX, centerY),
                                end = Offset(minEndX, minEndY),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Second hand (sweeping red)
                            val secAngle = seconds * 6.0
                            val secRad = Math.toRadians(secAngle)
                            val secLength = radius * 0.85f
                            val secEndX = centerX + secLength * sin(secRad).toFloat()
                            val secEndY = centerY - secLength * cos(secRad).toFloat()
                            drawLine(
                                color = accentColor,
                                start = Offset(centerX, centerY),
                                end = Offset(secEndX, secEndY),
                                strokeWidth = 1.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            // Center pin
                            drawCircle(
                                color = accentColor,
                                radius = 3.5.dp.toPx(),
                                center = Offset(centerX, centerY)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right side Digital Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Analog Kadran",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = timeFormatted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = "${dayOfWeek.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }}, $dateMonth",
                            fontSize = 11.sp,
                            color = secondaryText,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (nextAlarm != null) "⏰ ${nextAlarm.timeFormatted} (${nextAlarm.label})" else "⏰ Kurulu alarm yok",
                            fontSize = 10.sp,
                            color = if (nextAlarm != null) MaterialTheme.colorScheme.primary else secondaryText.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            2 -> {
                // Mode 2: World Dual Time Zone View
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Local Time
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "Yerel Saat (Sistem)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = timeFormatted,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = dateMonth,
                            fontSize = 11.sp,
                            color = secondaryText
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier
                            .height(60.dp)
                            .padding(horizontal = 8.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f)
                    )

                    // World City
                    val worldCity = worldClocks.firstOrNull() ?: com.example.model.WorldClockCity("Londra", "Birleşik Krallık", -3, "06:50", true)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            Text(
                                text = "${worldCity.cityName} (${if (worldCity.timeDiffHours >= 0) "+${worldCity.timeDiffHours}" else "${worldCity.timeDiffHours}"}s)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = worldCity.currentFormattedTime,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = worldCity.country,
                            fontSize = 11.sp,
                            color = secondaryText,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Top right face cycle button
        FilledTonalIconButton(
            onClick = { selectedFaceMode = (selectedFaceMode + 1) % 3 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesomeMotion,
                contentDescription = "Görünümü Değiştir",
                modifier = Modifier.size(13.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
