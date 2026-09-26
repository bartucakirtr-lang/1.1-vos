@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.system

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay

@Composable
fun SetupWizardScreen(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val setupStep by viewModel.setupStep.collectAsState()
    val isSetupCompleted by viewModel.isSetupCompleted.collectAsState()

    BackHandler {
        viewModel.prevSetupStep()
    }

    val stepIndex = when (setupStep) {
        SetupStep.WELCOME -> 0
        SetupStep.NETWORK -> 1
        SetupStep.ACCOUNT -> 2
        SetupStep.SECURITY -> 3
        SetupStep.PERSONALIZE -> 4
        SetupStep.FINISHING -> 5
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("setup_wizard_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (stepIndex > 0 || isSetupCompleted) {
                    IconButton(
                        onClick = { viewModel.prevSetupStep() },
                        modifier = Modifier.testTag("setup_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Progress Step Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..5) {
                        val active = i == stepIndex
                        val passed = i < stepIndex
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (active) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        active -> MaterialTheme.colorScheme.primary
                                        passed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.18f)
                                    }
                                )
                        )
                    }
                }

                if (stepIndex in 1..4) {
                    TextButton(
                        onClick = { viewModel.nextSetupStep() },
                        modifier = Modifier.testTag("setup_skip_button")
                    ) {
                        Text("Skip", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Step Content Animated
            AnimatedContent(
                targetState = setupStep,
                transitionSpec = {
                    fadeIn(animationSpec = tween(280)) + slideInHorizontally { it / 4 } togetherWith
                            fadeOut(animationSpec = tween(200)) + slideOutHorizontally { -it / 4 }
                },
                modifier = Modifier.weight(1f),
                label = "SetupStepTransition"
            ) { step ->
                when (step) {
                    SetupStep.WELCOME -> WelcomeStep(viewModel)
                    SetupStep.NETWORK -> NetworkStep(viewModel)
                    SetupStep.ACCOUNT -> AccountSetupStep(viewModel)
                    SetupStep.SECURITY -> SecuritySetupStep(viewModel)
                    SetupStep.PERSONALIZE -> PersonalizeStep(viewModel)
                    SetupStep.FINISHING -> FinishingStep(viewModel)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 1. WELCOME STEP
// ---------------------------------------------------------------------------
@Composable
private fun WelcomeStep(viewModel: OSViewModel) {
    var selectedLanguage by remember { mutableStateOf("English (United States)") }
    var languageMenuOpen by remember { mutableStateOf(false) }

    val languages = listOf(
        "English (United States)",
        "Español (Latinoamérica)",
        "Français (France)",
        "Deutsch (Deutschland)",
        "日本語 (日本)",
        "Türkçe (Türkiye)",
        "Português (Brasil)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Animated Pulsing Orb
            val infiniteTransition = rememberInfiniteTransition(label = "HeroOrb")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "HeroScale"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp * scale)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Welcome to your\nvos Phone",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Next-generation Android experience powered by dynamic Material You, deep customization, and fluid multitasking.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Language Selector Button
            Box {
                OutlinedButton(
                    onClick = { languageMenuOpen = true },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Filled.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedLanguage, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = languageMenuOpen,
                    onDismissRequest = { languageMenuOpen = false }
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang) },
                            onClick = {
                                selectedLanguage = lang
                                languageMenuOpen = false
                            },
                            leadingIcon = {
                                if (lang == selectedLanguage) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.nextSetupStep() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("setup_get_started_button"),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Get started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(
                onClick = { /* Simulated accessibility features */ }
            ) {
                Icon(Icons.Filled.AccessibilityNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Vision & Accessibility settings", fontSize = 12.sp)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 2. NETWORK STEP
// ---------------------------------------------------------------------------
@Composable
private fun NetworkStep(viewModel: OSViewModel) {
    val networks by viewModel.availableWifiNetworks.collectAsState()
    val connectedWifi by viewModel.connectedWifi.collectAsState()

    var selectedNetworkForPassword by remember { mutableStateOf<String?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var isConnecting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Connect to Wi-Fi",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "To download updates and sync your NovaOS Account",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Available Networks",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                networks.forEach { (name, isSecured) ->
                    val isCurrent = name == connectedWifi
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isCurrent) {
                                    if (isSecured) {
                                        selectedNetworkForPassword = name
                                        passwordInput = ""
                                    } else {
                                        viewModel.connectWifi(name)
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Wifi,
                                    contentDescription = null,
                                    tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = name,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = if (isCurrent) "Connected • Excellent signal" else if (isSecured) "WPA3 Secure" else "Open Network",
                                        fontSize = 11.sp,
                                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            if (isCurrent) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Filled.Check, contentDescription = "Connected", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else if (isSecured) {
                                Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Continue Button
        Button(
            onClick = { viewModel.nextSetupStep() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("network_continue_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Password Sheet Dialog
    if (selectedNetworkForPassword != null) {
        val networkName = selectedNetworkForPassword ?: ""
        AlertDialog(
            onDismissRequest = { selectedNetworkForPassword = null },
            title = { Text("Connect to $networkName") },
            text = {
                Column {
                    Text("Enter the network security password:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isConnecting = true
                        viewModel.connectWifi(networkName)
                        selectedNetworkForPassword = null
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNetworkForPassword = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 3. ACCOUNT SETUP STEP (CREATE ACCOUNT / SIGN IN)
// ---------------------------------------------------------------------------
@Composable
private fun AccountSetupStep(viewModel: OSViewModel) {
    val currentAccount by viewModel.userAccount.collectAsState()

    var isCreatingNew by remember { mutableStateOf(true) }
    var firstName by remember { mutableStateOf(currentAccount.firstName) }
    var lastName by remember { mutableStateOf(currentAccount.lastName) }
    var username by remember { mutableStateOf(currentAccount.username) }
    var email by remember { mutableStateOf(currentAccount.email) }
    var password by remember { mutableStateOf("NovaOS2026!") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedAvatarEmoji by remember { mutableStateOf(currentAccount.avatarEmoji) }
    var selectedAvatarColor by remember { mutableLongStateOf(currentAccount.avatarColor) }
    var backupEnabled by remember { mutableStateOf(true) }

    val emojis = listOf("🚀", "⚡", "🌟", "🦊", "🎨", "👾", "👑", "🎧")
    val avatarColors = listOf(
        0xFF1976D2, // Ocean Blue
        0xFF2E7D32, // Forest Green
        0xFFE65100, // Sunset Orange
        0xFF7B1FA2, // Royal Purple
        0xFF00838F, // Teal Cyan
        0xFFC2185B  // Pink Rose
    )

    // Password strength calculation
    val passwordStrength = remember(password) {
        when {
            password.length < 4 -> 0
            password.length < 8 -> 1
            password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> 3
            else -> 2
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (isCreatingNew) "Create NovaOS Account" else "Sign In to NovaOS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Unified identity for backup, settings, and apps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Switcher Tabs (Create Account vs Sign In)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isCreatingNew) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCreatingNew = true }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Create Account",
                            fontWeight = if (isCreatingNew) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (isCreatingNew) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isCreatingNew) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isCreatingNew = false }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            fontWeight = if (!isCreatingNew) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = if (!isCreatingNew) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Avatar Preview & Customization (when creating)
            if (isCreatingNew) {
                Text(
                    text = "Choose Profile Avatar & Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Big Preview
                    Surface(
                        shape = CircleShape,
                        color = Color(selectedAvatarColor),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = selectedAvatarEmoji, fontSize = 32.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Emojis row
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        emojis.take(4).forEach { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = if (selectedAvatarEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clickable { selectedAvatarEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Color Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    avatarColors.forEach { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (selectedAvatarColor == colorHex) 2.5.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatarColor = colorHex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Input Fields
            if (isCreatingNew) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("account_first_name_input")
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("account_last_name_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' }
                        email = "$username@novaos.net"
                    },
                    label = { Text("Username") },
                    prefix = { Text("@") },
                    singleLine = true,
                    supportingText = { Text("Your NovaID will be @$username") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_username_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("NovaOS Account Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_email_input")
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email or NovaID (@username)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_email_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "Toggle password"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("account_password_input")
            )

            // Password strength bar (when creating)
            if (isCreatingNew) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (i <= passwordStrength) {
                                        when (passwordStrength) {
                                            0 -> Color(0xFFE53935)
                                            1 -> Color(0xFFFB8C00)
                                            2 -> Color(0xFFFDD835)
                                            else -> Color(0xFF43A047)
                                        }
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    }
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (passwordStrength) {
                        0 -> "Password too short"
                        1 -> "Fair password"
                        2 -> "Good password"
                        else -> "Strong password • Protected"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cloud Backup Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { backupEnabled = !backupEnabled }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = backupEnabled,
                    onCheckedChange = { backupEnabled = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Backup data to NovaCloud (100 GB Free)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Contacts, Notes, Tasks, and Settings synced automatically", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Create Account & Continue Button
        Button(
            onClick = {
                viewModel.createOrUpdateAccount(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    username = username,
                    avatarEmoji = selectedAvatarEmoji,
                    avatarColor = selectedAvatarColor,
                    backupEnabled = backupEnabled
                )
                viewModel.nextSetupStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("account_submit_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = if (isCreatingNew) "Agree & Create Account" else "Sign In & Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 4. SECURITY SETUP STEP
// ---------------------------------------------------------------------------
@Composable
private fun SecuritySetupStep(viewModel: OSViewModel) {
    val lockType by viewModel.screenLockType.collectAsState()
    val pinCode by viewModel.pinCode.collectAsState()

    var selectedLockType by remember { mutableStateOf(lockType) }
    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }
    var biometricScannedPercent by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Protect Your Phone",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Prevent unauthorized access to your NovaOS data",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Lock Type Choices
            listOf(
                Triple(LockType.PIN, "PIN Code", "4-digit numeric code"),
                Triple(LockType.BIOMETRIC, "Fingerprint + PIN", "Fast biometric unlock simulation"),
                Triple(LockType.SWIPE, "Swipe to Unlock", "No security, quick access")
            ).forEach { (type, title, desc) ->
                val isSelected = selectedLockType == type
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedLockType = type }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedLockType = type }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Icon(
                            imageVector = when (type) {
                                LockType.PIN -> Icons.Filled.Pin
                                LockType.BIOMETRIC -> Icons.Filled.Fingerprint
                                LockType.SWIPE -> Icons.Filled.Swipe
                            },
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive PIN entry if PIN or BIOMETRIC chosen
            if (selectedLockType != LockType.SWIPE) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (!isConfirming) "Enter a 4-digit PIN:" else "Re-enter your PIN to confirm:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // PIN Dots
                        val currentDigits = if (!isConfirming) enteredPin else confirmPin
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            for (i in 0..3) {
                                val filled = i < currentDigits.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (filled) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                        )
                                )
                            }
                        }

                        if (pinError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(pinError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Compact Keypad
                        val keys = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "⌫")
                        )

                        keys.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(0.8f),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                row.forEach { key ->
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clickable {
                                                pinError = null
                                                when (key) {
                                                    "C" -> {
                                                        if (!isConfirming) enteredPin = "" else confirmPin = ""
                                                    }
                                                    "⌫" -> {
                                                        if (!isConfirming) {
                                                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                                        } else {
                                                            if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                                                        }
                                                    }
                                                    else -> {
                                                        if (!isConfirming) {
                                                            if (enteredPin.length < 4) {
                                                                enteredPin += key
                                                                if (enteredPin.length == 4) {
                                                                    isConfirming = true
                                                                }
                                                            }
                                                        } else {
                                                            if (confirmPin.length < 4) {
                                                                confirmPin += key
                                                                if (confirmPin.length == 4) {
                                                                    if (confirmPin == enteredPin) {
                                                                        viewModel.setPin(enteredPin)
                                                                    } else {
                                                                        pinError = "PINs do not match. Try again."
                                                                        confirmPin = ""
                                                                        isConfirming = false
                                                                        enteredPin = ""
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(key, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Biometric sensor simulation if chosen
            if (selectedLockType == LockType.BIOMETRIC) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (biometricScannedPercent >= 1.0f) "Fingerprint Enrolled! ✓" else "Tap sensor repeatedly to register print:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (biometricScannedPercent >= 1.0f) Color(0xFF43A047) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = if (biometricScannedPercent >= 1.0f) Color(0xFF43A047).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(68.dp)
                            .clickable {
                                if (biometricScannedPercent < 1.0f) {
                                    biometricScannedPercent = (biometricScannedPercent + 0.34f).coerceAtMost(1.0f)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Fingerprint,
                                contentDescription = "Fingerprint Sensor",
                                tint = if (biometricScannedPercent >= 1.0f) Color(0xFF43A047) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { biometricScannedPercent },
                        modifier = Modifier
                            .width(140.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                viewModel.setLockType(selectedLockType)
                if (enteredPin.length == 4 && confirmPin == enteredPin) {
                    viewModel.setPin(enteredPin)
                }
                viewModel.nextSetupStep()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("security_continue_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// 5. PERSONALIZE STEP
// ---------------------------------------------------------------------------
@Composable
private fun PersonalizeStep(viewModel: OSViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val themePalette by viewModel.themePalette.collectAsState()
    val navMode by viewModel.navigationMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Make it Yours",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Customize theme, accent colors, and navigation",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Theme Mode
            Text("System Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    ThemeMode.DARK to ("Dark" to Icons.Filled.DarkMode),
                    ThemeMode.LIGHT to ("Light" to Icons.Filled.LightMode),
                    ThemeMode.SYSTEM to ("System" to Icons.Filled.AutoMode)
                ).forEach { (mode, pair) ->
                    val isSelected = themeMode == mode
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setThemeMode(mode) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(pair.second, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(pair.first, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Material You Dynamic Accent Palette
            Text("Dynamic Accent Palette", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePalette.entries.chunked(2).forEach { rowPalettes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPalettes.forEach { palette ->
                            val isSelected = themePalette == palette
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setThemePalette(palette) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(palette.primaryHex), Color(palette.secondaryHex))
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = palette.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Navigation Mode
            Text("System Navigation", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (navMode == NavMode.GESTURE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setNavigationMode(NavMode.GESTURE) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.Swipe, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gesture Nav", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Swipe to go back & home", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (navMode == NavMode.THREE_BUTTON) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setNavigationMode(NavMode.THREE_BUTTON) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.Circle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Icon(Icons.Filled.Square, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("3-Button Nav", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Classic Back, Home, Recents", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.nextSetupStep() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("personalize_continue_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// 6. FINISHING STEP
// ---------------------------------------------------------------------------
@Composable
private fun FinishingStep(viewModel: OSViewModel) {
    val userAccount by viewModel.userAccount.collectAsState()
    var progress by remember { mutableFloatStateOf(0.1f) }
    var currentTaskLabel by remember { mutableStateOf("Preparing system services...") }

    LaunchedEffect(Unit) {
        delay(300)
        progress = 0.35f
        currentTaskLabel = "Registering NovaOS Account & Cloud Sync..."
        delay(500)
        progress = 0.65f
        currentTaskLabel = "Personalizing desktop and dynamic widgets..."
        delay(500)
        progress = 0.9f
        currentTaskLabel = "Installing native tools and core apps..."
        delay(400)
        progress = 1.0f
        currentTaskLabel = "All set! Your phone is ready to explore."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Card Preview
            Surface(
                shape = CircleShape,
                color = Color(userAccount.avatarColor),
                modifier = Modifier.size(92.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(userAccount.avatarEmoji, fontSize = 48.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Welcome aboard,\n${userAccount.firstName}!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${userAccount.email} • 100 GB Cloud Active",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentTaskLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Checkmarks
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SetupFeatureCheck(title = "Nova Account created & active", isDone = progress >= 0.35f)
                    SetupFeatureCheck(title = "Dynamic Material You theme configured", isDone = progress >= 0.65f)
                    SetupFeatureCheck(title = "Built-in Tasks, Camera, Notes ready", isDone = progress >= 0.9f)
                    SetupFeatureCheck(title = "Ready for everyday use", isDone = progress >= 1.0f)
                }
            }
        }

        // Finish Button
        Button(
            onClick = { viewModel.finishSetup() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("setup_finish_button"),
            shape = RoundedCornerShape(27.dp),
            enabled = progress >= 0.8f,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Start using vos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.RocketLaunch, contentDescription = null)
        }
    }
}

@Composable
private fun SetupFeatureCheck(title: String, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (isDone) Color(0xFF43A047) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
