@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ContactItem
import com.example.viewmodel.OSViewModel

@Composable
fun ContactsApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contacts.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var selectedContactForDetails by remember { mutableStateOf<ContactItem?>(null) }
    var contactToEdit by remember { mutableStateOf<ContactItem?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts.sortedBy { it.name }
        } else {
            contacts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery) ||
                it.email.contains(searchQuery, ignoreCase = true)
            }.sortedBy { it.name }
        }
    }

    val favoriteContacts = remember(contacts) {
        contacts.filter { it.isFavorite }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Contacts", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateHome() },
                        modifier = Modifier.testTag("contacts_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportExportDialog = true }) {
                        Icon(Icons.Filled.ImportExport, contentDescription = "Import or Export Contacts")
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_contact_button")
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Add Contact")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("contacts_fab_add")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create New Contact")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search ${contacts.size} contacts...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("contacts_search_input")
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Favorites Section (if any and not filtering)
                if (favoriteContacts.isNotEmpty() && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "FAVORITES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(favoriteContacts) { contact ->
                                FavoriteContactChip(
                                    contact = contact,
                                    onClick = { selectedContactForDetails = contact },
                                    onCall = { viewModel.launchCallWithContact(contact) },
                                    onMessage = { viewModel.launchMessageWithContact(contact) }
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                // All Contacts Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "ALL CONTACTS (${filteredContacts.size})" else "SEARCH RESULTS (${filteredContacts.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (filteredContacts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Outlined.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No contacts found matching \"$searchQuery\"" else "No contacts yet",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredContacts, key = { it.id }) { contact ->
                        ContactRowItem(
                            contact = contact,
                            onClick = { selectedContactForDetails = contact },
                            onCall = { viewModel.launchCallWithContact(contact) },
                            onMessage = { viewModel.launchMessageWithContact(contact) },
                            onToggleFavorite = { viewModel.toggleContactFavorite(contact.id) }
                        )
                    }
                }
            }
        }
    }

    // Contact Details Modal Sheet
    if (selectedContactForDetails != null) {
        val contact = selectedContactForDetails!!
        AlertDialog(
            onDismissRequest = { selectedContactForDetails = null },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(contact.avatarColor)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilledTonalIconButton(
                            onClick = {
                                selectedContactForDetails = null
                                viewModel.launchCallWithContact(contact)
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color(0xFF00C853))
                        }

                        FilledTonalIconButton(
                            onClick = {
                                selectedContactForDetails = null
                                viewModel.launchMessageWithContact(contact)
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "Message", tint = Color(0xFF1E88E5))
                        }

                        FilledTonalIconButton(
                            onClick = {
                                viewModel.toggleContactFavorite(contact.id)
                                selectedContactForDetails = contact.copy(isFavorite = !contact.isFavorite)
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                if (contact.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (contact.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalIconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${contact.name}\n${contact.phoneNumber}\n${contact.email}"))
                                snackbarMessage = "Contact info copied to clipboard"
                            },
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Phone row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Mobile", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(contact.phoneNumber, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Email row
                    if (contact.email.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Email", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(contact.email, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        contactToEdit = contact
                        selectedContactForDetails = null
                    }
                ) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteContact(contact.id)
                        selectedContactForDetails = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        )
    }

    // Add Contact Dialog
    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var phoneInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_contact_name_input")
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_contact_phone_input")
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_contact_email_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                            viewModel.addContact(nameInput.trim(), phoneInput.trim(), emailInput.trim())
                            showAddDialog = false
                        }
                    },
                    enabled = nameInput.isNotBlank() && phoneInput.isNotBlank(),
                    modifier = Modifier.testTag("save_contact_button")
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Contact Dialog
    if (contactToEdit != null) {
        val target = contactToEdit!!
        var editName by remember { mutableStateOf(target.name) }
        var editPhone by remember { mutableStateOf(target.phoneNumber) }
        var editEmail by remember { mutableStateOf(target.email) }
        var editFavorite by remember { mutableStateOf(target.isFavorite) }

        AlertDialog(
            onDismissRequest = { contactToEdit = null },
            title = { Text("Edit Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { editFavorite = !editFavorite }
                    ) {
                        Checkbox(checked = editFavorite, onCheckedChange = { editFavorite = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Favorites")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateContact(target.id, editName.trim(), editPhone.trim(), editEmail.trim(), editFavorite)
                        contactToEdit = null
                    },
                    enabled = editName.isNotBlank() && editPhone.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import / Export Dialog
    if (showImportExportDialog) {
        var rawImportText by remember { mutableStateOf("") }
        var activeTab by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showImportExportDialog = false },
            title = {
                TabRow(selectedTabIndex = activeTab) {
                    Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Export") })
                    Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Import") })
                }
            },
            text = {
                if (activeTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Export your contacts to standard portable formats:", fontSize = 13.sp)
                        Button(
                            onClick = {
                                val vcard = viewModel.exportContactsVCard()
                                clipboardManager.setText(AnnotatedString(vcard))
                                showImportExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy as vCard (.vcf)")
                        }
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportContactsJson()
                                clipboardManager.setText(AnnotatedString(json))
                                showImportExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Code, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy as JSON")
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Paste raw vCard, JSON, or CSV (Name, Phone, Email) text below:", fontSize = 12.sp)
                        OutlinedTextField(
                            value = rawImportText,
                            onValueChange = { rawImportText = it },
                            placeholder = { Text("BEGIN:VCARD...\nor [{\"name\":\"...\",\"phoneNumber\":\"...\"}]") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 6
                        )
                        Button(
                            onClick = {
                                if (rawImportText.isNotBlank()) {
                                    viewModel.importContacts(rawImportText)
                                    showImportExportDialog = false
                                }
                            },
                            enabled = rawImportText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Import Contacts")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImportExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun FavoriteContactChip(
    contact: ContactItem,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(contact.avatarColor)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.name.split(" ").firstOrNull() ?: contact.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ContactRowItem(
    contact: ContactItem,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(contact.avatarColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and Phone
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contact.phoneNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Call & Message Shortcuts
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Call,
                        contentDescription = "Call ${contact.name}",
                        tint = Color(0xFF00C853),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onMessage, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Chat,
                        contentDescription = "Message ${contact.name}",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
