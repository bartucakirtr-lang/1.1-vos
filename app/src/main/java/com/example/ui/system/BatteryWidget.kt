package com.example.ui.system

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel
import kotlin.math.roundToInt

@Composable
fun BatteryStatusWidget(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier,
    onOpenDetails: (() -> Unit)? = null
) {
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val batteryStatusText by viewModel.batteryStatusText.collectAsState()
    val batteryPluggedType by viewModel.batteryPluggedType.collectAsState()
    val batteryHealth by viewModel.batteryHealth.collectAsState()
    val batteryTemp by viewModel.batteryTemperature.collectAsState()
    val batteryVoltage by viewModel.batteryVoltage.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val isDark = LocalIsDarkMode.current

    // Animated battery progress (0f to 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = (batteryLevel.coerceIn(0, 100)) / 100f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "BatteryProgressAnimation"
    )

    // Infinite breathing pulse for charging state
    val infiniteTransition = rememberInfiniteTransition(label = "ChargingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = if (isCharging) 1.0f else 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Color gradient based on percentage and charging state
    val batteryColor = when {
        isCharging -> Color(0xFF00E676)
        batterySaver -> Color(0xFFFF9100)
        batteryLevel <= 15 -> Color(0xFFFF3D00)
        batteryLevel <= 30 -> Color(0xFFFFB300)
        batteryLevel <= 70 -> Color(0xFF00E5FF)
        else -> Color(0xFF00E676)
    }

    val batterySecondaryColor = when {
        isCharging -> Color(0xFF00B0FF)
        batterySaver -> Color(0xFFFF6D00)
        batteryLevel <= 15 -> Color(0xFFD50000)
        batteryLevel <= 30 -> Color(0xFFFF8F00)
        batteryLevel <= 70 -> Color(0xFF2979FF)
        else -> Color(0xFF00C853)
    }

    val primaryTextColor = if (isDark) Color.White else Color(0xFF191C1E)
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF43474E)

    // Estimated time calculation
    val estimatedTimeText = remember(batteryLevel, isCharging) {
        if (isCharging) {
            val minutesToFull = ((100 - batteryLevel) * 0.85).roundToInt().coerceAtLeast(5)
            if (minutesToFull < 60) "~$minutesToFull dk tam doluma" else "~${minutesToFull / 60} sa ${minutesToFull % 60} dk tam doluma"
        } else {
            val hoursRemaining = ((batteryLevel / 100f) * 16.5).coerceAtLeast(0.5)
            val h = hoursRemaining.toInt()
            val m = ((hoursRemaining - h) * 60).toInt()
            "~$h sa $m dk kullanım süresi"
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Radial Progress Gauge with Lightning Icon & Big Percentage
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1.35f)
        ) {
            // Circular battery gauge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .testTag("battery_widget_gauge"),
                contentAlignment = Alignment.Center
            ) {
                // Background Track and Fill Canvas
                Canvas(modifier = Modifier.size(68.dp)) {
                    val strokeWidth = 7.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    // Background track
                    drawArc(
                        color = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active progress arc with gradient
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(batteryColor, batterySecondaryColor, batteryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content: Icon or Animated Bolt
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCharging) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Şarj Ediliyor",
                            tint = batteryColor.copy(alpha = pulseAlpha),
                            modifier = Modifier
                                .size(24.dp)
                                .scale(pulseScale)
                        )
                    } else if (batterySaver) {
                        Icon(
                            imageVector = Icons.Filled.EnergySavingsLeaf,
                            contentDescription = "Güç Tasarrufu",
                            tint = Color(0xFFFF9100),
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (batteryLevel >= 80) Icons.Filled.BatteryFull
                            else if (batteryLevel >= 50) Icons.Filled.Battery6Bar
                            else if (batteryLevel >= 20) Icons.Filled.Battery3Bar
                            else Icons.Filled.BatteryAlert,
                            contentDescription = "Pil Seviyesi",
                            tint = batteryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "$batteryLevel%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryTextColor
                    )
                }
            }

            // Status Title, Charging Plug Type & Remaining Estimate
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isCharging) "Şarj Ediliyor" else if (batteryLevel == 100) "Tam Dolu" else "Pil Durumu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = primaryTextColor
                    )

                    if (isCharging) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676).copy(alpha = 0.2f),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "HIZLI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isCharging) batteryPluggedType else estimatedTimeText,
                    fontSize = 11.sp,
                    color = secondaryTextColor,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Telemetry Badges: Temp & Health
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = if (batteryTemp > 38f) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "${"%.1f".format(batteryTemp)}°C",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Sağlık: $batteryHealth",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = secondaryTextColor
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Quick Interactive Actions (Simulate Charger & Battery Saver Mode)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(start = 6.dp)
        ) {
            // Interactive Plug/Unplug Test Button (Allows easy verification and testing)
            FilledTonalButton(
                onClick = { viewModel.toggleCharging() },
                modifier = Modifier
                    .height(34.dp)
                    .testTag("battery_widget_toggle_charging"),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isCharging) Color(0xFF00E676).copy(alpha = 0.25f)
                    else if (isDark) Color.White.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isCharging) Color(0xFF00C853) else primaryTextColor
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isCharging) Icons.Filled.Power else Icons.Outlined.PowerOff,
                        contentDescription = "Şarj Simülasyonu",
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isCharging) "Şarjda" else "Prize Tak",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Power Saver Toggle Quick Action
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (batterySaver) Color(0xFFFF9100).copy(alpha = 0.22f)
                else if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                border = if (batterySaver) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9100)) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { viewModel.toggleBatterySaver() }
                    .testTag("battery_widget_toggle_saver")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (batterySaver) Icons.Filled.EnergySavingsLeaf else Icons.Outlined.EnergySavingsLeaf,
                        contentDescription = "Tasarruf Modu",
                        tint = if (batterySaver) Color(0xFFFF9100) else secondaryTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = if (batterySaver) "Tasarruf Açık" else "Tasarruf",
                        fontSize = 10.sp,
                        fontWeight = if (batterySaver) FontWeight.Bold else FontWeight.Normal,
                        color = if (batterySaver) Color(0xFFFF9100) else secondaryTextColor
                    )
                }
            }
        }
    }
}
