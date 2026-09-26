package com.example.ui.apps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.model.AppId
import com.example.viewmodel.OSViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun CameraApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraMode by viewModel.cameraMode.collectAsState()
    val cameraFilter by viewModel.cameraFilter.collectAsState()
    val cameraZoom by viewModel.cameraZoom.collectAsState()
    val cameraFlash by viewModel.cameraFlash.collectAsState()
    val photos by viewModel.photos.collectAsState()

    var showFlashEffect by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val scope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] == true
    }

    // Real photo capture launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val file = File(context.cacheDir, "captured_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                viewModel.addRealCapturedPhoto("Real Camera Photo", Uri.fromFile(file).toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Real video capture launcher
    var videoFileUri by remember { mutableStateOf<Uri?>(null) }
    val recordVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && videoFileUri != null) {
            viewModel.addRealCapturedVideo("Real Captured Video", videoFileUri.toString())
        }
    }

    val modes = listOf("Photo", "Video", "Portrait", "Night Sight", "Pro")
    val filters = listOf("Normal", "Cyberpunk", "Vintage 90s", "Noir B&W", "Golden Hour", "HDR Vivid")
    val zooms = listOf(0.5f, 1.0f, 2.0f, 5.0f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Camera Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateHome() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Home", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(onClick = { viewModel.toggleCameraFlash() }) {
                        Icon(
                            if (cameraFlash) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            "Flash",
                            tint = if (cameraFlash) Color(0xFFFFD54F) else Color.White
                        )
                    }
                    IconButton(onClick = {
                        if (!hasCameraPermission) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                        }
                    }) {
                        Icon(
                            if (hasCameraPermission) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                            "Camera Access",
                            tint = if (hasCameraPermission) Color(0xFF00E676) else Color(0xFFFF5252)
                        )
                    }
                }
            }

            // Viewfinder Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (cameraFilter) {
                            "Cyberpunk" -> Brush.verticalGradient(listOf(Color(0xFF1A0033), Color(0xFF003344)))
                            "Vintage 90s" -> Brush.verticalGradient(listOf(Color(0xFF3E2723), Color(0xFF5D4037)))
                            "Noir B&W" -> Brush.verticalGradient(listOf(Color(0xFF1C1C1C), Color(0xFF383838)))
                            "Golden Hour" -> Brush.verticalGradient(listOf(Color(0xFF4E342E), Color(0xFFBF360C)))
                            "HDR Vivid" -> Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF01579B)))
                            else -> Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF1F2937)))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // Render CameraX Preview
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview
                                    )
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Request Permission View
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = "Camera Permission",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant camera and microphone access to take real photos and videos.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Grant Camera Access")
                        }
                    }
                }

                // Filter & Mode Badge Overlay
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "📷 REAL CAMERA • $cameraMode Mode • $cameraFilter",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Zoom Level Controls
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    zooms.forEach { z ->
                        Surface(
                            shape = CircleShape,
                            color = if (cameraZoom == z) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { viewModel.setCameraZoom(z) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${z.toInt().let { if (z % 1.0f == 0f) it.toString() else z.toString() }}x",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Flash Shutter Overlay
                if (showFlashEffect) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
            }

            // Filters Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(filters) { f ->
                    FilterChip(
                        selected = cameraFilter == f,
                        onClick = { viewModel.setCameraFilter(f) },
                        label = { Text(f, fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Mode Selector
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(modes) { m ->
                    Text(
                        text = m.uppercase(),
                        color = if (cameraMode == m) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = if (cameraMode == m) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier
                            .clickable { viewModel.setCameraMode(m) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Shutter Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Shortcut
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { viewModel.openApp(AppId.PHOTOS) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.PhotoLibrary, "Gallery", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }

                // Shutter Button (Takes Real Photos or Records Real Videos)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(76.dp)
                        .clickable {
                            if (!hasCameraPermission) {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                )
                            } else {
                                scope.launch {
                                    showFlashEffect = true
                                    delay(100)
                                    showFlashEffect = false
                                }
                                if (cameraMode == "Video") {
                                    // Trigger real video capture launcher
                                    val file = File(context.cacheDir, "captured_video_${System.currentTimeMillis()}.mp4")
                                    val uri = Uri.fromFile(file)
                                    videoFileUri = uri
                                    try {
                                        recordVideoLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        // Fallback if intent handler not present
                                        viewModel.addRealCapturedVideo("Recorded Video", uri.toString())
                                    }
                                } else {
                                    // Trigger real photo capture launcher
                                    try {
                                        takePhotoLauncher.launch(null)
                                    } catch (e: Exception) {
                                        // Fallback simulate capture
                                        viewModel.capturePhoto()
                                    }
                                }
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(if (cameraMode == "Video") Color(0xFFFF1744) else Color.White)
                                .padding(4.dp)
                        )
                    }
                }

                // Flip Front/Back Camera
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { viewModel.playTone(800f, 60) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.FlipCameraAndroid, "Flip Camera", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
