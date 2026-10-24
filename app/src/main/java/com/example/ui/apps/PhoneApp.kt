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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppId
import com.example.model.CallLogItem
import com.example.model.CallType
import com.example.model.ContactItem
import com.example.viewmodel.OSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PhoneApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val isInCall by viewModel.isInCall.collectAsState()
    val activeCallContact by viewModel.activeCallContact.collectAsState()
    val callDurationSeconds by viewModel.callDurationSeconds.collectAsState()
    val isCallMuted by viewModel.isCallMuted.collectAsState()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val callLogs by viewModel.callLogs.collectAsState()
    val dialerNumber by viewModel.dialerNumber.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Favorites, 1: Recents, 2: Contacts, 3: Keypad
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var selectedContactForDetails by remember { mutableStateOf<ContactItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    if (isInCall && activeCallContact != null) {
        // Full In-Call Screen
        InCallScreen(
            contact = activeCallContact!!,
            durationSeconds = callDurationSeconds,
            isMuted = isCallMuted,
            isSpeakerOn = isSpeakerOn,
            onMuteToggle = { viewModel.toggleMuteCall() },
            onSpeakerToggle = { viewModel.toggleSpeaker() },
            onEndCall = { viewModel.endCall() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Phone & Contacts", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    if (selectedTab == 2) {
                        IconButton(onClick = { showImportExportDialog = true }) {
                            Icon(Icons.Filled.ImportExport, contentDescription = "Import/Export Contacts")
                        }
                        IconButton(onClick = { showAddContactDialog = true }) {
                            Icon(Icons.Filled.PersonAdd, contentDescription = "Add Contact")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.History, contentDescription = "Recents") },
                    label = { Text("Recents") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Contacts, contentDescription = "Contacts") },
                    label = { Text("Contacts (${contacts.size})") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Dialpad, contentDescription = "Keypad") },
                    label = { Text("Keypad") }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> FavoritesTab(
                    favorites = contacts.filter { it.isFavorite },
                    onCall = { viewModel.startCall(it) },
                    onSelectContact = { selectedContactForDetails = it }
                )
                1 -> RecentsTab(
                    logs = callLogs,
                    onCall = { viewModel.startCallWithNumber(it) }
                )
                2 -> ContactsTab(
                    contacts = contacts,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCall = { viewModel.startCall(it) },
                    onSelectContact = { selectedContactForDetails = it }
                )
                3 -> KeypadTab(
                    dialerNumber = dialerNumber,
                    onDigit = { viewModel.dialDigit(it) },
                    onClear = { viewModel.clearDialDigit() },
                    onCall = { viewModel.startCallWithNumber(dialerNumber) }
                )
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Yeni Kişi Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Ad Soyad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Telefon Numarası") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-posta (İsteğe bağlı)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotEmpty() && phone.isNotEmpty()) {
                            viewModel.addContact(name, phone, email)
                            showAddContactDialog = false
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Import / Export Dialog
    if (showImportExportDialog) {
        ImportExportContactsDialog(
            viewModel = viewModel,
            onDismiss = { showImportExportDialog = false }
        )
    }

    // Contact Detail & Edit BottomSheet / Dialog
    selectedContactForDetails?.let { contact ->
        ContactDetailDialog(
            contact = contact,
            onDismiss = { selectedContactForDetails = null },
            onCall = {
                selectedContactForDetails = null
                viewModel.startCall(contact)
            },
            onMessage = {
                selectedContactForDetails = null
                viewModel.startNewChatThread(contact.phoneNumber, contact.name)
                viewModel.openApp(AppId.MESSAGES)
            },
            onToggleFavorite = {
                viewModel.toggleContactFavorite(contact.id)
                selectedContactForDetails = contact.copy(isFavorite = !contact.isFavorite)
            },
            onDelete = {
                viewModel.deleteContact(contact.id)
                selectedContactForDetails = null
            },
            onUpdate = { name, phone, email, isFav ->
                viewModel.updateContact(contact.id, name, phone, email, isFav)
                selectedContactForDetails = contact.copy(name = name, phoneNumber = phone, email = email, isFavorite = isFav)
            }
        )
    }
}

@Composable
private fun ImportExportContactsDialog(
    viewModel: OSViewModel,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Export, 1: Import
    var importText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val contacts by viewModel.contacts.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.ImportExport, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Kişileri İçe / Dışa Aktar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Dışa Aktar (Export)") }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("İçe Aktar (Import)") }
                    )
                }

                if (activeTab == 0) {
                    // Export options
                    Text(
                        text = "Mevcut ${contacts.size} kişiyi vCard (.vcf) veya JSON formatında dışa aktarın:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val vcard = viewModel.exportContactsVCard()
                                clipboardManager.setText(AnnotatedString(vcard))
                                statusMessage = "vCard panoya kopyalandı! (.vcf)"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("vCard (.vcf)")
                        }
                        FilledTonalButton(
                            onClick = {
                                val json = viewModel.exportContactsJson()
                                clipboardManager.setText(AnnotatedString(json))
                                statusMessage = "JSON panoya kopyalandı!"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("JSON format")
                        }
                    }
                } else {
                    // Import options
                    Text(
                        text = "vCard (.vcf), JSON veya CSV formatında kişi metnini yapıştırın:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        placeholder = { Text("BEGIN:VCARD ... veya JSON veya İsim,Telefon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (importText.isNotBlank()) {
                                    val count = viewModel.importContacts(importText)
                                    statusMessage = "$count kişi başarıyla içeri aktarıldı!"
                                    importText = ""
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İçeri Aktar")
                        }
                        OutlinedButton(
                            onClick = {
                                val sampleBackup = "BEGIN:VCARD\nVERSION:3.0\nFN:Yazılım Ekip Lideri\nTEL;TYPE=CELL:555-0999\nEMAIL:team@novaos.org\nEND:VCARD\n\nBEGIN:VCARD\nVERSION:3.0\nFN:Android HAL Uzmanı\nTEL;TYPE=CELL:555-0888\nEMAIL:hal@novaos.org\nEND:VCARD"
                                importText = sampleBackup
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Örnek Yükle")
                        }
                    }
                }

                statusMessage?.let {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
private fun ContactDetailDialog(
    contact: ContactItem,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (String, String, String, Boolean) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(contact.name) }
    var editPhone by remember { mutableStateOf(contact.phoneNumber) }
    var editEmail by remember { mutableStateOf(contact.email) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            if (!isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(contact.avatarColor),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = contact.name.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Column {
                        Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(contact.phoneNumber, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Text("Kişiyi Düzenle", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            if (!isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (contact.email.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(contact.email, fontSize = 13.sp)
                        }
                    }

                    // Quick Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF00C853))
                        ) {
                            Icon(Icons.Filled.Call, "Ara", tint = Color.White)
                        }
                        IconButton(
                            onClick = onMessage,
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF1E88E5))
                        ) {
                            Icon(Icons.Filled.Message, "Mesaj", tint = Color.White)
                        }
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                if (contact.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                "Favori",
                                tint = if (contact.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { isEditing = true },
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Filled.Edit, "Düzenle", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Ad Soyad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Telefon") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("E-posta") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!isEditing) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Sil")
                }
            } else {
                Button(
                    onClick = {
                        onUpdate(editName, editPhone, editEmail, contact.isFavorite)
                        isEditing = false
                    }
                ) {
                    Text("Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (isEditing) isEditing = false else onDismiss()
                }
            ) {
                Text(if (isEditing) "Vazgeç" else "Kapat")
            }
        }
    )
}

@Composable
private fun FavoritesTab(
    favorites: List<ContactItem>,
    onCall: (ContactItem) -> Unit,
    onSelectContact: (ContactItem) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.StarOutline, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Henüz favori kişi eklenmedi", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                Text("Kişiler sekmesinden yıldız simgesine dokunarak ekleyebilirsiniz", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites) { contact ->
                Card(
                    onClick = { onSelectContact(contact) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(contact.avatarColor),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.name.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(contact.phoneNumber, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            }
                        }

                        IconButton(
                            onClick = { onCall(contact) },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853))
                        ) {
                            Icon(Icons.Filled.Call, "Call", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentsTab(logs: List<CallLogItem>, onCall: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(logs) { log ->
            Card(
                onClick = { onCall(log.phoneNumber) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            when (log.type) {
                                CallType.INCOMING -> Icons.Filled.CallReceived
                                CallType.OUTGOING -> Icons.Filled.CallMade
                                CallType.MISSED -> Icons.Filled.CallMissed
                            },
                            contentDescription = null,
                            tint = if (log.type == CallType.MISSED) Color(0xFFFF1744) else Color(0xFF00C853),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(log.contactName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(log.phoneNumber, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = if (log.durationSeconds > 0) "${log.durationSeconds / 60}m ${log.durationSeconds % 60}s" else "Missed",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactsTab(
    contacts: List<ContactItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCall: (ContactItem) -> Unit,
    onSelectContact: (ContactItem) -> Unit
) {
    val filtered = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) || it.phoneNumber.contains(searchQuery)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Kişi veya numara ara...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered, key = { it.id }) { contact ->
                Card(
                    onClick = { onSelectContact(contact) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(contact.avatarColor),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(contact.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(contact.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(contact.phoneNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        IconButton(onClick = { onCall(contact) }) {
                            Icon(Icons.Filled.Call, "Ara", tint = Color(0xFF00C853))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadTab(
    dialerNumber: String,
    onDigit: (String) -> Unit,
    onClear: () -> Unit,
    onCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display dialed number
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = if (dialerNumber.isEmpty()) " " else dialerNumber,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad Grid
        val keys = listOf(
            listOf("1" to "", "2" to "ABC", "3" to "DEF"),
            listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
            listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
            listOf("*" to "", "0" to "+", "#" to "")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in keys) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    for ((digit, letters) in row) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(64.dp)
                                .clickable { onDigit(digit) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(digit, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                if (letters.isNotEmpty()) {
                                    Text(letters, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Row: Clear & Call
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))

            // Call Button
            Surface(
                shape = CircleShape,
                color = Color(0xFF00C853),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(68.dp)
                    .clickable {
                        if (dialerNumber.isNotEmpty()) onCall()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Call, "Call", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }

            // Clear Button
            if (dialerNumber.isNotEmpty()) {
                IconButton(onClick = { onClear() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Backspace, "Backspace", modifier = Modifier.size(24.dp))
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Composable
private fun InCallScreen(
    contact: ContactItem,
    durationSeconds: Int,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    val durationFormatted = remember(durationSeconds) {
        val mins = durationSeconds / 60
        val secs = durationSeconds % 60
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Contact & Duration Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(contact.avatarColor),
                    shadowElevation = 12.dp,
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contact.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    }
                }

                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White)
                Text(contact.phoneNumber, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))

                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = durationFormatted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
            }

            // In-Call Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InCallControlButton(
                        icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        onClick = onMuteToggle
                    )

                    InCallControlButton(
                        icon = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Filled.VolumeDown,
                        label = if (isSpeakerOn) "Speaker ON" else "Speaker",
                        isActive = isSpeakerOn,
                        onClick = onSpeakerToggle
                    )

                    InCallControlButton(
                        icon = Icons.Filled.Dialpad,
                        label = "Keypad",
                        isActive = false,
                        onClick = {}
                    )
                }

                // End Call Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF1744),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable { onEndCall() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InCallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.15f),
            modifier = Modifier
                .size(56.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
    }
}
