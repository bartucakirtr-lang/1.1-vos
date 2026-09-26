@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.PhotoItem
import com.example.viewmodel.OSViewModel

@Composable
fun GalleryApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val photos by viewModel.photos.collectAsState()
    val selectedPhoto by viewModel.selectedPhoto.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Photos, 1: Albums, 2: Favorites

    if (selectedPhoto != null) {
        // Fullscreen Photo Detail Viewer
        FullscreenPhotoViewer(
            photo = selectedPhoto!!,
            onClose = { viewModel.selectPhoto(null) },
            onFavoriteToggle = { viewModel.toggleFavoritePhoto(selectedPhoto!!.id) },
            onDelete = {
                viewModel.deletePhoto(selectedPhoto!!.id)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateHome() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
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
                    icon = { Icon(Icons.Filled.PhotoLibrary, "Photos") },
                    label = { Text("Photos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Collections, "Albums") },
                    label = { Text("Albums") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Favorite, "Favorites") },
                    label = { Text("Favorites") }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        val displayedPhotos = when (selectedTab) {
            2 -> photos.filter { it.isFavorite }
            else -> photos
        }

        if (displayedPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No photos found in this album", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(displayedPhotos, key = { it.id }) { photo ->
                    Card(
                        onClick = { viewModel.selectPhoto(photo) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    when (photo.drawableResName) {
                                        "wp_cyber" -> Brush.verticalGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC)))
                                        "wp_abstract" -> Brush.verticalGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199)))
                                        "ic_phone_os_icon" -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4338CA)))
                                        else -> Brush.verticalGradient(listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC)))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photo.fileUri != null) {
                                AsyncImage(
                                    model = photo.fileUri,
                                    contentDescription = photo.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    if (photo.isVideo) Icons.Filled.Videocam else Icons.Filled.Image,
                                    contentDescription = photo.title,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            if (photo.isVideo) {
                                Icon(
                                    Icons.Filled.PlayCircle,
                                    contentDescription = "Video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(28.dp)
                                )
                            }

                            if (photo.isFavorite) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "Favorite",
                                    tint = Color(0xFFFF1744),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullscreenPhotoViewer(
    photo: PhotoItem,
    onClose: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(photo.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(photo.dateAdded, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        if (photo.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        "Favorite",
                        tint = if (photo.isFavorite) Color(0xFFFF1744) else Color.White
                    )
                }
            }

            // Photo Canvas in Center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        when (photo.drawableResName) {
                            "wp_cyber" -> Brush.verticalGradient(listOf(Color(0xFF6A11CB), Color(0xFF2575FC)))
                            "wp_abstract" -> Brush.verticalGradient(listOf(Color(0xFFFF0844), Color(0xFFFFB199)))
                            "ic_phone_os_icon" -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF4338CA)))
                            else -> Brush.verticalGradient(listOf(Color(0xFF0052D4), Color(0xFF4364F7), Color(0xFF6FB1FC)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (photo.fileUri != null) {
                    AsyncImage(
                        model = photo.fileUri,
                        contentDescription = photo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (photo.isVideo) Icons.Filled.Videocam else Icons.Filled.Image,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(84.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Filter: ${photo.filterApplied}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Share, "Share", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Edit, "Edit", tint = Color.White)
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Outlined.Info, "Details", tint = Color.White)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, "Delete", tint = Color(0xFFFF5252))
                }
            }
        }
    }
}
