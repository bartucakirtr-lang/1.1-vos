@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ChatThread
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val chatThreads by viewModel.chatThreads.collectAsState()
    val selectedChatContactId by viewModel.selectedChatContactId.collectAsState()
    val messageDraft by viewModel.messageDraft.collectAsState()
    val isPartnerTyping by viewModel.isPartnerTyping.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    var inThreadView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewMessageDialog by remember { mutableStateOf(false) }

    val currentThread = remember(selectedChatContactId, chatThreads) {
        chatThreads.find { it.contactId == selectedChatContactId }
    }

    if (inThreadView && currentThread != null) {
        // Individual Chat Thread View
        ChatDetailScreen(
            thread = currentThread,
            draft = messageDraft,
            isPartnerTyping = isPartnerTyping,
            onDraftChange = { viewModel.setMessageDraft(it) },
            onSend = { viewModel.sendMessage() },
            onSendMms = { text, type, title ->
                viewModel.sendMmsMessage(text, type, title)
            },
            onDeleteMessage = { msgId ->
                viewModel.deleteChatMessage(currentThread.contactId, msgId)
            },
            onBack = { inThreadView = false },
            onCall = { viewModel.launchCallWithNumber(currentThread.phoneNumber) },
            onOpenUrl = { url -> viewModel.openUrlInBrowser(url) }
        )
    } else {
        // Conversations List Screen
        val filteredThreads = remember(chatThreads, searchQuery) {
            if (searchQuery.isBlank()) chatThreads else {
                chatThreads.filter {
                    it.contactName.contains(searchQuery, ignoreCase = true) ||
                    it.phoneNumber.contains(searchQuery) ||
                    it.messages.any { m -> m.text.contains(searchQuery, ignoreCase = true) }
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SMS & MMS Messages", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.navigateHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showNewMessageDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "New Message")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewMessageDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = "Start Chat")
                }
            },
            modifier = modifier
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Mesajlarda veya kişilerde ara...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Temizle")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true
                )

                if (filteredThreads.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz mesaj bulunmuyor", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredThreads, key = { it.contactId }) { thread ->
                            val lastMsg = thread.messages.lastOrNull()
                            val timeStr = lastMsg?.timestamp?.let {
                                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it))
                            } ?: ""

                            Card(
                                onClick = {
                                    viewModel.selectChatThread(thread.contactId)
                                    inThreadView = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(thread.avatarColor),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = thread.contactName.take(1),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = thread.contactName,
                                                fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = timeStr,
                                                color = if (thread.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp,
                                                fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (lastMsg?.mmsAttachmentType != null) {
                                                    "📎 [MMS ${lastMsg.mmsAttachmentTitle ?: "Ek"}] ${lastMsg.text}"
                                                } else {
                                                    lastMsg?.text ?: "Mesaj yok"
                                                },
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                modifier = Modifier.weight(1f)
                                            )

                                            if (thread.unreadCount > 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(start = 8.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = thread.unreadCount.toString(),
                                                        color = MaterialTheme.colorScheme.onPrimary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Message Modal Dialog
    if (showNewMessageDialog) {
        var targetNumber by remember { mutableStateOf("") }
        var targetName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewMessageDialog = false },
            title = { Text("Yeni Mesaj Başlat", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = targetNumber,
                        onValueChange = { targetNumber = it },
                        label = { Text("Telefon Numarası") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetName,
                        onValueChange = { targetName = it },
                        label = { Text("Kişi İsmi (İsteğe Bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (contacts.isNotEmpty()) {
                        Text("Veya Kayıtlı Kişilerden Seçin:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(contacts) { c ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.startNewChatThread(c.phoneNumber, c.name)
                                            showNewMessageDialog = false
                                            inThreadView = true
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(c.name, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                        Text("•", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(c.phoneNumber, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (targetNumber.isNotBlank()) {
                            viewModel.startNewChatThread(targetNumber, targetName)
                            showNewMessageDialog = false
                            inThreadView = true
                        }
                    }
                ) {
                    Text("Sohbeti Başlat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewMessageDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun ChatDetailScreen(
    thread: ChatThread,
    draft: String,
    isPartnerTyping: Boolean,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendMms: (String, String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val quickReplies = listOf("Harika! 👍", "Geliyorum 🚗", "Fotoğraf gönderir misin? 📸", "Teşekkürler! ❤️")
    var showMmsMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(thread.avatarColor),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(thread.contactName.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(thread.contactName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                if (isPartnerTyping) "yazıyor..." else "SMS / MMS Aktif",
                                color = if (isPartnerTyping) MaterialTheme.colorScheme.primary else Color(0xFF00C853),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onCall) {
                        Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color(0xFF00C853))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Quick Smart Reply Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickReplies.forEach { chip ->
                        SuggestionChip(
                            onClick = {
                                onDraftChange(chip)
                            },
                            label = { Text(chip, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Input Bar with MMS Attachment Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // MMS Attach button
                    IconButton(
                        onClick = { showMmsMenu = !showMmsMenu },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            Icons.Filled.AttachFile,
                            contentDescription = "Attach MMS",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        placeholder = { Text("Mesaj yazın • SMS/MMS", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4
                    )

                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // MMS Attachments Quick Drawer
                AnimatedVisibility(visible = showMmsMenu) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MmsOptionItem(
                                icon = Icons.Filled.Image,
                                label = "Fotoğraf",
                                color = Color(0xFFE91E63),
                                onClick = {
                                    showMmsMenu = false
                                    onSendMms(draft.ifBlank { "Fotoğraf paylaşıldı." }, "IMAGE", "Manzara_2026.jpg")
                                }
                            )
                            MmsOptionItem(
                                icon = Icons.Filled.Mic,
                                label = "Ses Kaydı",
                                color = Color(0xFF9C27B0),
                                onClick = {
                                    showMmsMenu = false
                                    onSendMms(draft.ifBlank { "Sesli mesaj (0:14)" }, "AUDIO", "SesKaydı_014.aac")
                                }
                            )
                            MmsOptionItem(
                                icon = Icons.Filled.LocationOn,
                                label = "Konum",
                                color = Color(0xFF00C853),
                                onClick = {
                                    showMmsMenu = false
                                    onSendMms(draft.ifBlank { "Mevcut Konum Paylaşımı" }, "LOCATION", "37.7749° N, 122.4194° W")
                                }
                            )
                            MmsOptionItem(
                                icon = Icons.Filled.ContactPhone,
                                label = "Kişi Kartı",
                                color = Color(0xFF1E88E5),
                                onClick = {
                                    showMmsMenu = false
                                    onSendMms(draft.ifBlank { "Kişi kartı gönderildi." }, "CONTACT", "Kart: Sistem Ekibi")
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(thread.messages, key = { it.id }) { msg ->
                MessageBubble(msg, onOpenUrl)
            }

            if (isPartnerTyping) {
                item {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 40.dp)
                        ) {
                            Text(
                                text = "yazıyor...",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MmsOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
            }
        }
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, onOpenUrl: (String) -> Unit) {
    val timeFormatted = remember(msg.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
    }

    val extractedUrl = remember(msg.text) {
        val regex = Regex("(https?://[a-zA-Z0-9.-]+(?:/[a-zA-Z0-9._~:/?#\\[\\]@!$&'()*+,;=-]*)?)")
        regex.find(msg.text)?.value
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (msg.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (msg.isFromMe) 4.dp else 18.dp
            ),
            color = if (msg.isFromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                // Render MMS Header / Attachment Card if present
                msg.mmsAttachmentType?.let { type ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = (if (msg.isFromMe) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface).copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                when (type) {
                                    "IMAGE" -> Icons.Filled.Image
                                    "AUDIO" -> Icons.Filled.AudioFile
                                    "LOCATION" -> Icons.Filled.Place
                                    else -> Icons.Filled.PermContactCalendar
                                },
                                contentDescription = null,
                                tint = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MMS: ${msg.mmsAttachmentTitle ?: "Ek"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (type) {
                                        "IMAGE" -> "Yüksek çözünürlüklü görüntü"
                                        "AUDIO" -> "Sesli mesaj kaydı (0:14)"
                                        "LOCATION" -> "Canlı GPS konumu"
                                        else -> "vCard kişi bilgisi"
                                    },
                                    fontSize = 10.sp,
                                    color = (if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = msg.text,
                    color = if (msg.isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )

                if (extractedUrl != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (msg.isFromMe) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenUrl(extractedUrl) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Public,
                                contentDescription = null,
                                tint = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bağlantıyı Aç: $extractedUrl",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (msg.isFromMe) Color.White else MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        color = (if (msg.isFromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                    if (msg.isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.DoneAll,
                            contentDescription = "Read",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
