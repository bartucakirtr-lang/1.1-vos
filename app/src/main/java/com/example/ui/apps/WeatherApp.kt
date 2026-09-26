@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HourlyForecast
import com.example.model.WeatherForecast
import com.example.ui.theme.LocalIsDarkMode
import com.example.viewmodel.OSViewModel

@Composable
fun WeatherApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkMode.current
    val weatherGradient = if (isDark) {
        listOf(Color(0xFF0288D1), Color(0xFF01579B), Color(0xFF0A192F))
    } else {
        listOf(Color(0xFF29B6F6), Color(0xFF0288D1), Color(0xFF01579B))
    }

    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val dailyForecast by viewModel.dailyForecast.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nexus Weather", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier.background(
            Brush.verticalGradient(weatherGradient)
        )
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main City Header & Temperature
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("San Francisco", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("72°", fontSize = 72.sp, fontWeight = FontWeight.Thin, color = Color.White)
                    Text("Sunny & Clear Sky", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.9f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("H: 80° • L: 62° • AQI: 32 (Good)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }

            // 24-Hour Forecast Horizontal Bar
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "24-HOUR HOURLY FORECAST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(hourlyForecast) { h ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(h.time, color = Color.White, fontSize = 12.sp)
                                    Icon(
                                        when (h.icon) {
                                            "rain" -> Icons.Filled.WaterDrop
                                            "cloudy" -> Icons.Filled.Cloud
                                            else -> Icons.Filled.WbSunny
                                        },
                                        contentDescription = null,
                                        tint = if (h.icon == "rain") Color(0xFF81D4FA) else Color(0xFFFFD54F),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text("${h.temp}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 7-Day Forecast Table
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "7-DAY OUTLOOK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        dailyForecast.forEach { day ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(day.dayOfWeek, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.width(60.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.width(110.dp)
                                ) {
                                    Icon(
                                        when (day.icon) {
                                            "rain" -> Icons.Filled.WaterDrop
                                            "thunder" -> Icons.Filled.Thunderstorm
                                            "cloudy" -> Icons.Filled.Cloud
                                            else -> Icons.Filled.WbSunny
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(day.condition.split(" ").first(), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("${day.tempLow}°", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                                    LinearProgressIndicator(
                                        progress = { (day.tempHigh - 50) / 40f },
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFFFFB74D),
                                        trackColor = Color.White.copy(alpha = 0.2f)
                                    )
                                    Text("${day.tempHigh}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Weather Metric Tiles (UV, Humidity, Wind, Pressure)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeatherMetricTile(
                        title = "UV INDEX",
                        value = "3 (Moderate)",
                        subtitle = "Low danger today",
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricTile(
                        title = "WIND",
                        value = "12 mph",
                        subtitle = "NW gusts to 16 mph",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    WeatherMetricTile(
                        title = "HUMIDITY",
                        value = "45%",
                        subtitle = "The dew point is 52°",
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricTile(
                        title = "AIR QUALITY",
                        value = "32 (Good)",
                        subtitle = "Ideal for outdoor activities",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricTile(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}
