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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    Text("Phone", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    if (selectedTab == 2) {
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
                    label = { Text("Contacts") }
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
                0 -> FavoritesTab(contacts.filter { it.isFavorite }, onCall = { viewModel.startCall(it) })
                1 -> RecentsTab(callLogs, onCall = { viewModel.startCallWithNumber(it) })
                2 -> ContactsTab(contacts, onCall = { viewModel.startCall(it) })
                3 -> KeypadTab(
                    dialerNumber = dialerNumber,
                    onDigit = { viewModel.dialDigit(it) },
                    onClear = { viewModel.clearDialDigit() },
                    onCall = { viewModel.startCallWithNumber(dialerNumber) }
                )
            }
        }
    }

    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("New Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true
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
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FavoritesTab(favorites: List<ContactItem>, onCall: (ContactItem) -> Unit) {
    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No favorite contacts yet")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(favorites) { contact ->
                Card(
                    onClick = { onCall(contact) },
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
private fun ContactsTab(contacts: List<ContactItem>, onCall: (ContactItem) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(contacts) { contact ->
            ListItem(
                headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(contact.phoneNumber) },
                leadingContent = {
                    Surface(
                        shape = CircleShape,
                        color = Color(contact.avatarColor),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(contact.name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                trailingContent = {
                    IconButton(onClick = { onCall(contact) }) {
                        Icon(Icons.Filled.Call, "Call", tint = Color(0xFF00C853))
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCall(contact) }
            )
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
        val m = durationSeconds / 60
        val s = durationSeconds % 60
        "%02d:%02d".format(m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF10141D))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Calling info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(contact.avatarColor),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contact.name.take(1),
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = contact.name,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = contact.phoneNumber,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = durationFormatted,
                    color = Color(0xFF00E676),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // In-Call Controls (Mute, Keypad, Speaker, Add Call)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    InCallButton(
                        icon = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                        label = if (isMuted) "Unmute" else "Mute",
                        isActive = isMuted,
                        onClick = onMuteToggle
                    )
                    InCallButton(
                        icon = Icons.Filled.Dialpad,
                        label = "Keypad",
                        isActive = false,
                        onClick = {}
                    )
                    InCallButton(
                        icon = if (isSpeakerOn) Icons.Filled.VolumeUp else Icons.Outlined.VolumeUp,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        onClick = onSpeakerToggle
                    )
                }

                // End Call Button
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFF1744),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(72.dp)
                        .clickable { onEndCall() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CallEnd, "End Call", tint = Color.White, modifier = Modifier.size(34.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InCallButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.2f),
            modifier = Modifier
                .size(60.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = if (isActive) Color.Black else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}
