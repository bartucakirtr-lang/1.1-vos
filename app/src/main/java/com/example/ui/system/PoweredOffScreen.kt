package com.example.ui.system

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BootMode
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * PoweredOffScreen represents the hardware powered-off state of the device.
 * Users can tap or press and hold the Power Button to boot (boot'layabilmek) the system,
 * select Boot Mode (Normal, Fast, Recovery), or inspect battery status.
 */
@Composable
fun PoweredOffScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isPoweringUp by remember { mutableStateOf(false) }
    var bootHoldProgress by remember { mutableFloatStateOf(0f) }
    var showBootMenuModal by remember { mutableStateOf(false) }
    val batteryPct by viewModel.batteryLevel.collectAsState()

    // Breathing glow animation around the power button
    val infiniteTransition = rememberInfiniteTransition(label = "powerPulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF040608))
            .systemBarsPadding()
    ) {
        // Subtle background grid or deep black
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Battery level & charging indicator in sleep mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                        Text(
                            text = "CİHAZ KAPALI",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Battery Status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BatteryChargingFull,
                        contentDescription = "Battery Status",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$batteryPct%",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Center Area: Power Button & Boot Prompt
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Outer Pulse Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .scale(pulseGlow)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF00E5FF).copy(alpha = 0.22f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Interactive Hardware Power Button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10161E),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00E5FF),
                                    Color(0xFF7C4DFF),
                                    Color(0xFF00E676),
                                    Color(0xFF00E5FF)
                                )
                            )
                        ),
                        shadowElevation = 16.dp,
                        modifier = Modifier
                            .size(100.dp)
                            .testTag("power_on_button")
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        // Tap boots device normally!
                                        isPoweringUp = true
                                        viewModel.powerOnSystem(BootMode.NORMAL)
                                    },
                                    onLongPress = {
                                        // Long press opens Boot Mode Menu
                                        showBootMenuModal = true
                                    }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.PowerSettingsNew,
                                contentDescription = "Gücü Aç / Boot Et",
                                tint = if (isPoweringUp) Color(0xFF00E676) else Color(0xFF00E5FF),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "Vroxen UI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Açmak için güç simgesine dokunun\nveya gelişmiş boot menüsü için basılı tutun",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                // Quick Action Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            viewModel.powerOnSystem(BootMode.NORMAL)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("boot_system_normal")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sistemi Başlat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showBootMenuModal = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.85f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("open_boot_menu_button")
                    ) {
                        Icon(Icons.Filled.SettingsSystemDaydream, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Boot Menüsü", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Bottom Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Android Bootloader v4.2 • Vroxen Kernel 6.6",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }

    // Advanced Boot Mode Dialog
    if (showBootMenuModal) {
        AlertDialog(
            onDismissRequest = { showBootMenuModal = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Filled.Terminal,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Boot Seçenekleri", fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vroxen UI sistemini nasıl başlatmak (boot etmek) istiyorsunuz?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    BootMode.entries.forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBootMenuModal = false
                                    viewModel.powerOnSystem(mode)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (mode) {
                                        BootMode.NORMAL -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                        BootMode.FAST -> Color(0xFF00E676).copy(alpha = 0.2f)
                                        BootMode.RECOVERY -> Color(0xFFFF9100).copy(alpha = 0.2f)
                                        BootMode.SAFE_MODE -> Color(0xFF7C4DFF).copy(alpha = 0.2f)
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = when (mode) {
                                                BootMode.NORMAL -> Icons.Filled.PlayArrow
                                                BootMode.FAST -> Icons.Filled.Bolt
                                                BootMode.RECOVERY -> Icons.Filled.Build
                                                BootMode.SAFE_MODE -> Icons.Filled.Security
                                            },
                                            contentDescription = null,
                                            tint = when (mode) {
                                                BootMode.NORMAL -> Color(0xFF00E5FF)
                                                BootMode.FAST -> Color(0xFF00E676)
                                                BootMode.RECOVERY -> Color(0xFFFF9100)
                                                BootMode.SAFE_MODE -> Color(0xFF7C4DFF)
                                            },
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = mode.subtitle,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBootMenuModal = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
