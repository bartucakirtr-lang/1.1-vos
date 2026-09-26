@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class WhatsAppChat(
    val id: String,
    val name: String,
    val avatarColor: Long,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val messages: List<WhatsAppMessage> = emptyList()
)

data class WhatsAppMessage(
    val text: String,
    val isMe: Boolean,
    val time: String,
    val isRead: Boolean = true
)

@Composable
fun WhatsAppApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var activeChat by remember { mutableStateOf<WhatsAppChat?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val initialChats = remember {
        mutableStateListOf(
            WhatsAppChat(
                id = "c1",
                name = "Mom ❤️",
                avatarColor = 0xFFE91E63,
                lastMessage = "Dinner is ready! Are you coming home?",
                time = "18:45",
                unreadCount = 2,
                isOnline = true,
                messages = listOf(
                    WhatsAppMessage("Hi dear, don't forget to take a break from coding!", false, "17:30"),
                    WhatsAppMessage("Almost finished with the new update Mom!", true, "17:42"),
                    WhatsAppMessage("Dinner is ready! Are you coming home?", false, "18:45")
                )
            ),
            WhatsAppChat(
                id = "c2",
                name = "Alex (Tech Lead) 💻",
                avatarColor = 0xFF1E88E5,
                lastMessage = "The v1.1 release looks stunning. Merged!",
                time = "16:20",
                unreadCount = 0,
                isOnline = true,
                messages = listOf(
                    WhatsAppMessage("Hey, how is the real-world app integration going?", false, "15:00"),
                    WhatsAppMessage("Just added WhatsApp, Spotify, Maps, and Netflix!", true, "15:30"),
                    WhatsAppMessage("The v1.1 release looks stunning. Merged!", false, "16:20")
                )
            ),
            WhatsAppChat(
                id = "c3",
                name = "NovaOS Devs Group 🚀",
                avatarColor = 0xFF00897B,
                lastMessage = "Sarah: Check out the new map canvas benchmark",
                time = "14:15",
                unreadCount = 5,
                messages = listOf(
                    WhatsAppMessage("Kernel 16.0 hyper-threading tests are passing.", false, "13:00"),
                    WhatsAppMessage("Sarah: Check out the new map canvas benchmark", false, "14:15")
                )
            ),
            WhatsAppChat(
                id = "c4",
                name = "Sarah 🌸",
                avatarColor = 0xFF8E24AA,
                lastMessage = "See you tomorrow at coffee shop!",
                time = "Yesterday",
                unreadCount = 0,
                messages = listOf(
                    WhatsAppMessage("See you tomorrow at coffee shop!", false, "Yesterday")
                )
            )
        )
    }

    val whatsappGreen = Color(0xFF075E54)
    val whatsappTeal = Color(0xFF128C7E)
    val whatsappLightGreen = Color(0xFF25D366)

    Scaffold(
        topBar = {
            if (activeChat == null) {
                Column(modifier = Modifier.background(whatsappGreen)) {
                    TopAppBar(
                        title = {
                            Text("WhatsApp", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
                        },
                        navigationIcon = {
                            IconButton(onClick = { viewModel.navigateHome() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { /* Camera */ }) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.White)
                            }
                            IconButton(onClick = { /* Search */ }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(onClick = { /* Menu */ }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = whatsappGreen)
                    )

                    // Navigation Tabs: CHATS, STATUS, CALLS
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = whatsappGreen,
                        contentColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("CHATS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .background(Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("7", color = whatsappGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("STATUS", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("CALLS", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                        )
                    }
                }
            } else {
                // Active Conversation Top Bar
                val chat = activeChat!!
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(chat.avatarColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(chat.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, maxLines = 1)
                                Text(if (chat.isOnline) "online" else "last seen recently", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { activeChat = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Video Call */ }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Color.White)
                        }
                        IconButton(onClick = { /* Voice Call */ }) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Color.White)
                        }
                        IconButton(onClick = { /* More */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = whatsappGreen)
                )
            }
        },
        floatingActionButton = {
            if (activeChat == null) {
                FloatingActionButton(
                    onClick = { /* New Chat */ },
                    containerColor = whatsappLightGreen,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = when (selectedTab) {
                            0 -> Icons.Default.Chat
                            1 -> Icons.Default.CameraAlt
                            else -> Icons.Default.AddIcCall
                        },
                        contentDescription = "Action"
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (activeChat == null) {
                when (selectedTab) {
                    0 -> {
                        // CHATS LIST
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(initialChats, key = { it.id }) { chat ->
                                ListItem(
                                    headlineContent = {
                                        Text(chat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    },
                                    supportingContent = {
                                        Text(chat.lastMessage, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                                    },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(chat.avatarColor), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(chat.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                    },
                                    trailingContent = {
                                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                chat.time,
                                                fontSize = 11.sp,
                                                color = if (chat.unreadCount > 0) whatsappLightGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (chat.unreadCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .background(whatsappLightGreen, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("${chat.unreadCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable {
                                        activeChat = chat
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                    1 -> {
                        // STATUS TAB
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .background(Color(0xFF607D8B), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("You", color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .background(whatsappLightGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Column {
                                        Text("My status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Tap to add status update", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            item {
                                Text("Recent updates", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val statusUpdates = listOf(
                                Triple("Sarah 🌸", "12 minutes ago", 0xFF8E24AA),
                                Triple("Alex (Tech Lead)", "45 minutes ago", 0xFF1E88E5),
                                Triple("Mom ❤️", "Today, 10:15", 0xFFE91E63)
                            )
                            items(statusUpdates) { (name, time, color) ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(whatsappLightGreen)
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(color), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column {
                                        Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(time, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // CALLS TAB
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            val callLogs = listOf(
                                Triple("Alex (Tech Lead)", "Today, 15:40", true),
                                Triple("Mom ❤️", "Yesterday, 20:12", false),
                                Triple("Sarah 🌸", "May 24, 18:30", true)
                            )
                            items(callLogs) { (name, time, isVideo) ->
                                ListItem(
                                    headlineContent = { Text(name, fontWeight = FontWeight.Bold) },
                                    supportingContent = {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.CallMade, null, tint = whatsappLightGreen, modifier = Modifier.size(14.dp))
                                            Text(time, fontSize = 12.sp)
                                        }
                                    },
                                    leadingContent = {
                                        Box(modifier = Modifier.size(46.dp).background(Color(0xFF37474F), CircleShape), contentAlignment = Alignment.Center) {
                                            Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    trailingContent = {
                                        Icon(if (isVideo) Icons.Default.Videocam else Icons.Default.Call, null, tint = whatsappGreen)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // ACTIVE CHAT CONVERSATION VIEW
                val chat = activeChat!!
                var messageInput by remember { mutableStateOf("") }
                var chatMessages by remember(chat.id) { mutableStateOf(chat.messages) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    // Messages Stream
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isMe = msg.isMe
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) Color(0xFFDCF8C6) else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                        Text(msg.text, fontSize = 14.sp, color = Color.Black)
                                        Row(
                                            modifier = Modifier.align(Alignment.End),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(msg.time, fontSize = 10.sp, color = Color.Gray)
                                            if (isMe) {
                                                Icon(Icons.Default.DoneAll, null, tint = Color(0xFF34B7F1), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Composer Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.weight(1f),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Mood, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(6.dp))
                                OutlinedTextField(
                                    value = messageInput,
                                    onValueChange = { messageInput = it },
                                    placeholder = { Text("Message...", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                                Icon(Icons.Default.AttachFile, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.CameraAlt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Send / Mic FAB
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(whatsappTeal, CircleShape)
                                .clickable {
                                    if (messageInput.isNotBlank()) {
                                        val newMsg = WhatsAppMessage(messageInput, true, "Now")
                                        chatMessages = chatMessages + newMsg
                                        val sentText = messageInput
                                        messageInput = ""

                                        // Auto-reply simulation!
                                        coroutineScope.launch {
                                            delay(1200)
                                            val reply = when {
                                                sentText.contains("hi", true) || sentText.contains("hello", true) -> "Hey! Glad to chat on NovaOS WhatsApp!"
                                                sentText.contains("dinner", true) -> "Delicious food is waiting!"
                                                else -> "Received your message loud and clear! 👍"
                                            }
                                            chatMessages = chatMessages + WhatsAppMessage(reply, false, "Now")
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (messageInput.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
