package com.example.ui.system

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.AppId
import com.example.viewmodel.OSViewModel

data class IconPreset(
    val name: String,
    val category: String,
    val imageUrl: String
)

object PresetIconPacks {
    val presets = listOf(
        // Neon Cyberpunk
        IconPreset("Neon Camera", "Neon Cyber", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Cyber Music", "Neon Cyber", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Neon Matrix", "Neon Cyber", "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Synthwave Sun", "Neon Cyber", "https://images.unsplash.com/photo-1508739773434-c26b3d09e071?w=150&auto=format&fit=crop&q=80"),

        // Minimalist Glass
        IconPreset("Glass Orb", "Minimalist Glass", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Monochrome Minimal", "Minimalist Glass", "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Frosted Crystal", "Minimalist Glass", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=150&auto=format&fit=crop&q=80"),

        // Retro Pixel 8-Bit
        IconPreset("Retro Arcade", "Retro 8-Bit", "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Game Controller", "Retro 8-Bit", "https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Retro Tape", "Retro 8-Bit", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=80"),

        // Pastel Candy
        IconPreset("Pastel Cloud", "Pastel Soft", "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Soft Blossom", "Pastel Soft", "https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Cream Aesthetic", "Pastel Soft", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=150&auto=format&fit=crop&q=80"),

        // AMOLED Gold & Metallic
        IconPreset("Gold Luxury", "AMOLED Gold", "https://images.unsplash.com/photo-1610375461246-83df859d849d?w=150&auto=format&fit=crop&q=80"),
        IconPreset("Black Gold Geometric", "AMOLED Gold", "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=150&auto=format&fit=crop&q=80")
    )
}

@Composable
fun CustomIconDialog(
    viewModel: OSViewModel,
    onDismiss: () -> Unit
) {
    val homeApps by viewModel.homeApps.collectAsState()
    val customIcons by viewModel.customAppIcons.collectAsState()

    var selectedApp by remember { mutableStateOf<AppId>(homeApps.firstOrNull() ?: AppId.SETTINGS) }
    var customUrlInput by remember { mutableStateOf("") }
    var selectedPresetUrl by remember { mutableStateOf<String?>(null) }
    var activeCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Neon Cyber", "Minimalist Glass", "Retro 8-Bit", "Pastel Soft", "AMOLED Gold")

    val filteredPresets = remember(activeCategory) {
        if (activeCategory == "All") PresetIconPacks.presets
        else PresetIconPacks.presets.filter { it.category == activeCategory }
    }

    val previewIconUrl = remember(customUrlInput, selectedPresetUrl, customIcons, selectedApp) {
        if (customUrlInput.isNotEmpty()) customUrlInput
        else selectedPresetUrl ?: customIcons[selectedApp.packageName] ?: ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Özel Simge Değiştirici (Custom Icon Adapter)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Select Application
                item {
                    Text("1. Simgesi Değiştirilecek Uygulamayı Seçin:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(homeApps) { appId ->
                            FilterChip(
                                selected = selectedApp == appId,
                                onClick = {
                                    selectedApp = appId
                                    customUrlInput = ""
                                    selectedPresetUrl = customIcons[appId.packageName]
                                },
                                label = { Text(appId.title, fontSize = 11.sp) },
                                leadingIcon = {
                                    val iconUrl = customIcons[appId.packageName]
                                    if (!iconUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = iconUrl,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Apps, null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }
                }

                // 2. Live Launcher Adapter Preview Tile
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (previewIconUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = previewIconUrl,
                                                contentDescription = "Custom Icon Preview",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(18.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                    }
                                }

                                Column {
                                    Text(selectedApp.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = if (previewIconUrl.isNotEmpty()) "Özel Resim Simgesi Aktif" else "Varsayılan Sistem Simgesi",
                                        fontSize = 11.sp,
                                        color = if (previewIconUrl.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (customIcons.containsKey(selectedApp.packageName)) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.resetCustomAppIcon(selectedApp.packageName)
                                        selectedPresetUrl = null
                                        customUrlInput = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Sıfırla", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // 3. Preset HD Icon Packs
                item {
                    Text("2. Hazır HD Simge Paketlerinden Seçin:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { category ->
                            FilterChip(
                                selected = activeCategory == category,
                                onClick = { activeCategory = category },
                                label = { Text(category, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPresets) { preset ->
                            val isSelected = selectedPresetUrl == preset.imageUrl
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedPresetUrl = preset.imageUrl
                                        customUrlInput = ""
                                    }
                                    .padding(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    AsyncImage(
                                        model = preset.imageUrl,
                                        contentDescription = preset.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(preset.name, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                            }
                        }
                    }
                }

                // 4. Custom Image URL/Path Input
                item {
                    Text("3. Veya Kendi Resim Bağlantınızı / Görsel URL'nizi Girin:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = {
                            customUrlInput = it
                            if (it.isNotEmpty()) selectedPresetUrl = null
                        },
                        placeholder = { Text("https://example.com/my_icon.png", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalIcon = if (customUrlInput.isNotEmpty()) customUrlInput else selectedPresetUrl
                    if (!finalIcon.isNullOrEmpty()) {
                        viewModel.setCustomAppIcon(selectedApp.packageName, finalIcon)
                    }
                    onDismiss()
                },
                enabled = previewIconUrl.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simgeyi Uygula (Apply Adapter)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}
