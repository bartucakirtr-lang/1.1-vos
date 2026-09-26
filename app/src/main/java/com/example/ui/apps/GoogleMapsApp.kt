@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel

data class MapPlace(
    val name: String,
    val category: String,
    val rating: String,
    val address: String,
    val xRatio: Float,
    val yRatio: Float,
    val color: Long = 0xFFE53935
)

@Composable
fun GoogleMapsApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isNavigating by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<MapPlace?>(null) }
    var isTrafficOn by remember { mutableStateOf(true) }
    var isSatelliteOn by remember { mutableStateOf(false) }

    val places = listOf(
        MapPlace("NovaOS Tech HQ", "Technology Park", "4.9 ★ (1,240)", "100 Innovation Way", 0.5f, 0.45f, 0xFF4285F4),
        MapPlace("Blue Bottle Coffee", "Coffee Shop", "4.8 ★ (890)", "420 Market Street", 0.35f, 0.38f, 0xFF795548),
        MapPlace("Golden Gate Vista", "Scenic Point", "4.9 ★ (24,500)", "Presidio Viewpoint", 0.22f, 0.25f, 0xFF00C853),
        MapPlace("Marina Bay Seafood", "Restaurant", "4.6 ★ (650)", "Pier 39 Bay Front", 0.68f, 0.62f, 0xFFFF7043)
    )

    // Pulse animation for GPS dot
    val infiniteTransition = rememberInfiniteTransition(label = "gps_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Scaffold(
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Interactive Canvas Map
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedPlace = null }
            ) {
                val w = size.width
                val h = size.height

                // Base ground
                val groundColor = if (isSatelliteOn) Color(0xFF263238) else Color(0xFFF5F5F0)
                drawRect(color = groundColor, size = size)

                // Water body (bay/river on top-right)
                val waterPath = Path().apply {
                    moveTo(w * 0.65f, 0f)
                    cubicTo(w * 0.7f, h * 0.3f, w * 0.6f, h * 0.5f, w, h * 0.55f)
                    lineTo(w, 0f)
                    close()
                }
                val waterColor = if (isSatelliteOn) Color(0xFF0D47A1) else Color(0xFFAAD3DF)
                drawPath(waterPath, color = waterColor)

                // Parks (green polygons)
                val parkColor = if (isSatelliteOn) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
                drawRect(color = parkColor, topLeft = Offset(w * 0.1f, h * 0.18f), size = Size(w * 0.25f, h * 0.15f))
                drawRect(color = parkColor, topLeft = Offset(w * 0.45f, h * 0.65f), size = Size(w * 0.35f, h * 0.2f))

                // Roads Grid
                val roadColor = if (isSatelliteOn) Color(0xFF455A64) else Color(0xFFFFFFFF)
                val strokeW = 12f

                // Horizontal roads
                for (i in 1..8) {
                    val y = h * (i / 9f)
                    drawLine(
                        color = roadColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = strokeW
                    )
                }

                // Vertical roads
                for (i in 1..6) {
                    val x = w * (i / 7f)
                    drawLine(
                        color = roadColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = strokeW
                    )
                }

                // Highway (major curved artery)
                val highwayColor = Color(0xFFFFD54F)
                val highwayPath = Path().apply {
                    moveTo(0f, h * 0.75f)
                    cubicTo(w * 0.3f, h * 0.7f, w * 0.5f, h * 0.4f, w * 0.75f, 0f)
                }
                drawPath(highwayPath, color = highwayColor, style = Stroke(width = 18f))

                // Traffic layer
                if (isTrafficOn) {
                    drawLine(
                        color = Color(0xFF4CAF50), // Green traffic
                        start = Offset(0f, h * (3 / 9f) - 3f),
                        end = Offset(w * 0.6f, h * (3 / 9f) - 3f),
                        strokeWidth = 4f
                    )
                    drawLine(
                        color = Color(0xFFF44336), // Heavy red traffic
                        start = Offset(w * 0.6f, h * (3 / 9f) - 3f),
                        end = Offset(w, h * (3 / 9f) - 3f),
                        strokeWidth = 4f
                    )
                }

                // Navigation route highlight line
                if (isNavigating) {
                    val routePath = Path().apply {
                        moveTo(w * 0.5f, h * 0.45f) // User GPS dot
                        lineTo(w * (4 / 7f), h * 0.45f)
                        lineTo(w * (4 / 7f), h * 0.25f)
                        lineTo(w * 0.22f, h * 0.25f) // Destination
                    }
                    drawPath(routePath, color = Color(0xFF1E88E5), style = Stroke(width = 12f))
                }

                // Places Pins
                places.forEach { place ->
                    val px = w * place.xRatio
                    val py = h * place.yRatio
                    // Pin circle
                    drawCircle(color = Color(place.color), radius = 16f, center = Offset(px, py))
                    drawCircle(color = Color.White, radius = 6f, center = Offset(px, py))
                }

                // GPS User Location Blue Dot
                val userX = w * 0.5f
                val userY = h * 0.45f
                // Radar pulse
                drawCircle(color = Color(0xFF4285F4).copy(alpha = pulseAlpha), radius = pulseRadius, center = Offset(userX, userY))
                // Direction beam
                val conePath = Path().apply {
                    moveTo(userX, userY)
                    lineTo(userX - 24f, userY - 50f)
                    lineTo(userX + 24f, userY - 50f)
                    close()
                }
                drawPath(conePath, color = Color(0xFF4285F4).copy(alpha = 0.25f), style = Fill)
                // Solid center
                drawCircle(color = Color.White, radius = 12f, center = Offset(userX, userY))
                drawCircle(color = Color(0xFF4285F4), radius = 8f, center = Offset(userX, userY))
            }

            // Top Floating Search Bar (when not navigating)
            if (!isNavigating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.navigateHome() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home", tint = Color.Gray)
                            }
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search Google Maps...", fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            IconButton(onClick = { /* Mic */ }) {
                                Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color(0xFF4285F4))
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFE91E63), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    // Category Chips
                    val categories = listOf("🍽️ Restaurants", "⛽ Gas", "☕ Coffee", "🛒 Groceries", "🏨 Hotels")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 3.dp,
                                modifier = Modifier.clickable {
                                    selectedPlace = places.random()
                                }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // TURN-BY-TURN NAVIGATION TOP BANNER
                Card(
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F9D58)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.TurnRight,
                            contentDescription = "Turn Right",
                            tint = Color.White,
                            modifier = Modifier.size(44.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("In 500 ft", fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f))
                            Text("Turn right onto Market St", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Right-Side Map Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Layer toggle
                FloatingActionButton(
                    onClick = { isSatelliteOn = !isSatelliteOn },
                    modifier = Modifier.size(42.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Layers, contentDescription = "Layers", modifier = Modifier.size(20.dp))
                }

                // Traffic toggle
                FloatingActionButton(
                    onClick = { isTrafficOn = !isTrafficOn },
                    modifier = Modifier.size(42.dp),
                    containerColor = if (isTrafficOn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surface,
                    contentColor = if (isTrafficOn) Color.White else MaterialTheme.colorScheme.onSurface,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Traffic, contentDescription = "Traffic", modifier = Modifier.size(20.dp))
                }

                // Recenter GPS FAB
                FloatingActionButton(
                    onClick = { selectedPlace = places.first() },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF4285F4),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }

            // Bottom Panel: Selected Place Details OR Navigation Status
            if (isNavigating) {
                // Navigation Active Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("14 min", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF0F9D58))
                                Text("(5.8 mi)", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("Fastest route now • ETA 3:45 PM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = { isNavigating = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Exit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (selectedPlace != null) {
                val place = selectedPlace!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(14.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(place.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(place.category, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(place.rating, fontSize = 12.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
                            }
                            IconButton(onClick = { selectedPlace = null }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }

                        Text(place.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { isNavigating = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                            ) {
                                Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}
