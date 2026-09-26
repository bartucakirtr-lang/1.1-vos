@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlarmItem
import com.example.model.WorldClockCity
import com.example.viewmodel.OSViewModel

@Composable
fun ClockApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val clockTab by viewModel.clockTab.collectAsState()
    val alarms by viewModel.alarms.collectAsState()
    val worldClocks by viewModel.worldClocks.collectAsState()
    val stopwatchRunning by viewModel.stopwatchRunning.collectAsState()
    val stopwatchElapsedMillis by viewModel.stopwatchElapsedMillis.collectAsState()
    val stopwatchLaps by viewModel.stopwatchLaps.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val timerRemainingSeconds by viewModel.timerRemainingSeconds.collectAsState()

    var showAddAlarmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clock", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    if (clockTab == 0) {
                        IconButton(onClick = { showAddAlarmDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Alarm")
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
                    selected = clockTab == 0,
                    onClick = { viewModel.setClockTab(0) },
                    icon = { Icon(Icons.Filled.Alarm, "Alarm") },
                    label = { Text("Alarm") }
                )
                NavigationBarItem(
                    selected = clockTab == 1,
                    onClick = { viewModel.setClockTab(1) },
                    icon = { Icon(Icons.Filled.Language, "World") },
                    label = { Text("World") }
                )
                NavigationBarItem(
                    selected = clockTab == 2,
                    onClick = { viewModel.setClockTab(2) },
                    icon = { Icon(Icons.Filled.Timer, "Stopwatch") },
                    label = { Text("Stopwatch") }
                )
                NavigationBarItem(
                    selected = clockTab == 3,
                    onClick = { viewModel.setClockTab(3) },
                    icon = { Icon(Icons.Filled.HourglassEmpty, "Timer") },
                    label = { Text("Timer") }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (clockTab) {
                0 -> AlarmTabContent(alarms, onToggle = { viewModel.toggleAlarm(it) }, onDelete = { viewModel.deleteAlarm(it) })
                1 -> WorldClockTabContent(worldClocks)
                2 -> StopwatchTabContent(
                    running = stopwatchRunning,
                    elapsedMillis = stopwatchElapsedMillis,
                    laps = stopwatchLaps,
                    onToggle = { viewModel.toggleStopwatch() },
                    onReset = { viewModel.resetStopwatch() },
                    onLap = { viewModel.lapStopwatch() }
                )
                3 -> TimerTabContent(
                    running = timerRunning,
                    totalSeconds = timerTotalSeconds,
                    remainingSeconds = timerRemainingSeconds,
                    onToggle = { viewModel.toggleTimer() },
                    onReset = { viewModel.resetTimer() },
                    onSetDuration = { viewModel.setTimerDuration(it) }
                )
            }
        }
    }

    if (showAddAlarmDialog) {
        var alarmTime by remember { mutableStateOf("07:30 AM") }
        var alarmLabel by remember { mutableStateOf("Quick Reminder") }

        AlertDialog(
            onDismissRequest = { showAddAlarmDialog = false },
            title = { Text("Add Alarm") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = alarmTime,
                        onValueChange = { alarmTime = it },
                        label = { Text("Time (e.g. 07:30 AM)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = alarmLabel,
                        onValueChange = { alarmLabel = it },
                        label = { Text("Label") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (alarmTime.isNotEmpty()) {
                        viewModel.addAlarm(alarmTime, alarmLabel)
                        showAddAlarmDialog = false
                    }
                }) {
                    Text("Set Alarm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAlarmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AlarmTabContent(
    alarms: List<AlarmItem>,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(alarms, key = { it.id }) { alarm ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (alarm.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = alarm.timeFormatted,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = alarm.label,
                            fontSize = 13.sp,
                            color = if (alarm.isEnabled) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = alarm.daysOfWeek.joinToString(" "),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Switch(
                        checked = alarm.isEnabled,
                        onCheckedChange = { onToggle(alarm.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorldClockTabContent(cities: List<WorldClockCity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(cities) { city ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(city.cityName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "${city.country} • ${if (city.timeDiffHours >= 0) "+${city.timeDiffHours} hrs" else "${city.timeDiffHours} hrs"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (city.isDaytime) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                            contentDescription = null,
                            tint = if (city.isDaytime) Color(0xFFFFD54F) else Color(0xFF90CAF9),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = city.currentFormattedTime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StopwatchTabContent(
    running: Boolean,
    elapsedMillis: Long,
    laps: List<Long>,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onLap: () -> Unit
) {
    val totalSeconds = elapsedMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val millis = (elapsedMillis % 1000) / 10

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Digital Stopwatch Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "%02d:%02d.%02d".format(minutes, seconds, millis),
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Laps Table
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(laps) { idx, lapTime ->
                val lapM = (lapTime / 1000) / 60
                val lapS = (lapTime / 1000) % 60
                val lapMs = (lapTime % 1000) / 10
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Lap ${laps.size - idx}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text("%02d:%02d.%02d".format(lapM, lapS, lapMs), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Controls Bar (Reset, Start/Pause, Lap)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = CircleShape,
                color = if (running) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(68.dp)
                    .clickable { onToggle() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Start/Stop",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Button(
                onClick = onLap,
                enabled = running,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Lap")
            }
        }
    }
}

@Composable
private fun TimerTabContent(
    running: Boolean,
    totalSeconds: Int,
    remainingSeconds: Int,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSetDuration: (Int) -> Unit
) {
    val progress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    }
    val m = remainingSeconds / 60
    val s = remainingSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Circular Countdown Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 30.dp)
                .size(240.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%02d:%02d".format(m, s),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (running) "Counting Down" else "Ready",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }

        // Quick Presets (+1m, +5m, +10m, +15m)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf(60 to "+1 min", 300 to "+5 min", 600 to "+10 min", 900 to "+15 min").forEach { (sec, label) ->
                FilledTonalButton(
                    onClick = { onSetDuration(sec) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(label, fontSize = 11.sp)
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onReset,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Reset", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Surface(
                shape = CircleShape,
                color = if (running) Color(0xFFFF5252) else MaterialTheme.colorScheme.primary,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(68.dp)
                    .clickable { onToggle() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        "Start/Stop",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
