package com.example.ui.system

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.VroxenAssistantManager

@Composable
fun VroxenAssistantDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        VroxenAssistantManager.initializeTts(context)
    }

    val messages by VroxenAssistantManager.messages.collectAsState()
    val isListening by VroxenAssistantManager.isListening.collectAsState()
    val isThinking by VroxenAssistantManager.isThinking.collectAsState()
    val isSpeaking by VroxenAssistantManager.isSpeaking.collectAsState()
    val userSpokenText by VroxenAssistantManager.userSpokenText.collectAsState()

    var textInput by remember { mutableStateOf("") }

    // Pulsing animation for active Voice Mic / Speaking state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    AlertDialog(
        onDismissRequest = {
            VroxenAssistantManager.stopSpeaking()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF8E24AA), Color(0xFF00E5FF))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    Column {
                        Text("Vroxen AI Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Gemini Sesli Yapay Zeka", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                IconButton(
                    onClick = {
                        VroxenAssistantManager.stopSpeaking()
                        onDismiss()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. AI Glowing Orb Status Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .scale(if (isListening || isSpeaking || isThinking) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isListening -> Color(0xFFE53935)
                                        isSpeaking -> Color(0xFF00E5FF)
                                        isThinking -> Color(0xFFFFB300)
                                        else -> Color(0xFF8E24AA)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                when {
                                    isListening -> Icons.Default.Mic
                                    isSpeaking -> Icons.Default.VolumeUp
                                    isThinking -> Icons.Default.HourglassTop
                                    else -> Icons.Default.GraphicEq
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                when {
                                    isListening -> "Vroxen Dinliyor..."
                                    isSpeaking -> "Vroxen Konuşuyor..."
                                    isThinking -> "Gemini Yanıt Hazırlıyor..."
                                    else -> "'Hey Vroxen' Diyerek Sesli Başlatın"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (userSpokenText.isNotEmpty()) {
                                Text(userSpokenText, fontSize = 11.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Chat History
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.widthIn(max = 260.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (isUser) "Siz" else "Vroxen AI",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Bottom Voice Mic & Text Input Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Voice Mic Button
                    IconButton(
                        onClick = {
                            VroxenAssistantManager.startListening(context)
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) Color.Red else MaterialTheme.colorScheme.primary
                            )
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = Color.White)
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Vroxen'e sorun...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                VroxenAssistantManager.sendMessage(textInput, context)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}
