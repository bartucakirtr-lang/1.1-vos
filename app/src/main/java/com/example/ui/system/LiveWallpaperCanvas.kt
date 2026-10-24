package com.example.ui.system

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.example.model.LiveWallpaperType
import kotlin.math.*
import kotlin.random.Random

/**
 * Universal Live Animated Wallpaper Host Composable with real-time Canvas rendering
 * and interactive touch effects.
 */
@Composable
fun LiveWallpaperCanvas(
    liveType: LiveWallpaperType,
    speedMultiplier: Float = 1.0f,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var touchTime by remember { mutableLongStateOf(0L) }

    val interactiveModifier = if (isInteractive) {
        modifier.pointerInput(liveType) {
            detectTapGestures(
                onPress = { offset ->
                    touchPoint = offset
                    touchTime = System.currentTimeMillis()
                }
            )
        }.pointerInput(liveType) {
            detectDragGestures(
                onDragStart = { offset ->
                    touchPoint = offset
                    touchTime = System.currentTimeMillis()
                },
                onDrag = { change, _ ->
                    touchPoint = change.position
                    touchTime = System.currentTimeMillis()
                },
                onDragEnd = {
                    touchPoint = null
                },
                onDragCancel = {
                    touchPoint = null
                }
            )
        }
    } else {
        modifier
    }

    Box(modifier = interactiveModifier.fillMaxSize()) {
        when (liveType) {
            LiveWallpaperType.AURORA_GLOW -> AuroraGlowLiveCanvas(speedMultiplier, touchPoint)
            LiveWallpaperType.CYBER_MATRIX -> CyberMatrixLiveCanvas(speedMultiplier, touchPoint)
            LiveWallpaperType.DEEP_NEBULA -> DeepNebulaLiveCanvas(speedMultiplier, touchPoint)
            LiveWallpaperType.OCEAN_WAVES -> OceanWavesLiveCanvas(speedMultiplier, touchPoint)
            LiveWallpaperType.PARTICLE_SWARM -> ParticleSwarmLiveCanvas(speedMultiplier, touchPoint)
            LiveWallpaperType.DYNAMIC_BOKEH -> DynamicBokehLiveCanvas(speedMultiplier, touchPoint)
        }
    }
}

// -------------------------------------------------------------
// 1. AURORA GLOW LIVE CANVAS
// -------------------------------------------------------------
@Composable
private fun AuroraGlowLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((7000 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Star dust particles
    val particles = remember {
        List(40) {
            AuroraParticle(
                relX = Random.nextFloat(),
                relY = Random.nextFloat(),
                size = Random.nextFloat() * 3f + 1.5f,
                speed = Random.nextFloat() * 0.8f + 0.2f,
                alpha = Random.nextFloat() * 0.6f + 0.3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deep Cosmic Gradient Background
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF030B1E),
                    Color(0xFF0A192F),
                    Color(0xFF021B24),
                    Color(0xFF010A14)
                )
            )
        )

        // Aurora Wave Bands
        val bandCount = 3
        for (b in 0 until bandCount) {
            val path = Path()
            val baseHeight = height * (0.28f + b * 0.16f)
            val waveFreq = 0.003f + b * 0.0015f
            val waveAmp = height * (0.07f + b * 0.03f)
            val bandPhase = phase + b * 1.8f

            path.moveTo(0f, height)
            path.lineTo(0f, baseHeight)

            var x = 0f
            val step = 15f
            while (x <= width + step) {
                var touchDistortion = 0f
                if (touchPoint != null) {
                    val dist = abs(x - touchPoint.x)
                    if (dist < 200f) {
                        touchDistortion = (1f - dist / 200f) * 45f * sin(phase * 4f)
                    }
                }

                val y = baseHeight + sin(x * waveFreq + bandPhase) * waveAmp +
                        cos(x * waveFreq * 0.6f - bandPhase * 0.8f) * (waveAmp * 0.5f) + touchDistortion
                path.lineTo(x, y)
                x += step
            }
            path.lineTo(width, height)
            path.close()

            val auroraColors = when (b) {
                0 -> listOf(Color(0x0000FFCC), Color(0x6600E5FF), Color(0x4400B4D8), Color(0x00000000))
                1 -> listOf(Color(0x007209B7), Color(0x774CC9F0), Color(0x334361EE), Color(0x00000000))
                else -> listOf(Color(0x0010B981), Color(0x5506D6A0), Color(0x22118AB2), Color(0x00000000))
            }

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = auroraColors,
                    startY = baseHeight - waveAmp * 1.5f,
                    endY = height
                )
            )
        }

        // Floating celestial star particles
        particles.forEach { p ->
            val py = (p.relY + (phase * p.speed * 0.05f)) % 1.0f
            val px = (p.relX + sin(phase + p.relY * 5f) * 0.02f).coerceIn(0f, 1f)
            val particleAlpha = (sin(phase * 2f + p.relX * 10f) * 0.3f + 0.7f) * p.alpha

            drawCircle(
                color = Color.White.copy(alpha = particleAlpha.coerceIn(0.1f, 1f)),
                radius = p.size,
                center = Offset(px * width, py * height)
            )
        }
    }
}

private data class AuroraParticle(
    val relX: Float,
    val relY: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
)

// -------------------------------------------------------------
// 2. CYBER MATRIX & NEON LIVE CANVAS
// -------------------------------------------------------------
@Composable
private fun CyberMatrixLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "matrix")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween((20000 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_time"
    )

    val streams = remember {
        List(24) { index ->
            MatrixStream(
                column = index,
                speed = Random.nextFloat() * 1.2f + 0.6f,
                length = Random.nextInt(8, 18),
                offsetY = Random.nextFloat() * 1000f,
                charCodes = List(20) { Random.nextInt(0x30A0, 0x30FF) }
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark Cyber Grid Background
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF030708),
                    Color(0xFF08120E),
                    Color(0xFF03080A)
                )
            )
        )

        // Subtle Perspective Grid Floor at bottom
        val gridY = height * 0.75f
        for (i in 0..10) {
            val y = gridY + (height - gridY) * (i.toFloat() / 10f).pow(2)
            drawLine(
                color = Color(0x2200FF66),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }
        for (i in -6..6) {
            val startX = width * 0.5f + i * (width * 0.03f)
            val endX = width * 0.5f + i * (width * 0.12f)
            drawLine(
                color = Color(0x2200FF66),
                start = Offset(startX, gridY),
                end = Offset(endX, height),
                strokeWidth = 1f
            )
        }

        // Falling Digital Matrix Columns
        val colWidth = width / streams.size.toFloat()
        streams.forEach { stream ->
            val colX = stream.column * colWidth + colWidth * 0.5f
            val streamY = (stream.offsetY + time * 30f * stream.speed) % (height + 300f) - 150f

            for (i in 0 until stream.length) {
                val charY = streamY - i * 26f
                if (charY in -20f..height + 20f) {
                    val fade = 1f - (i.toFloat() / stream.length.toFloat())
                    val isHead = (i == 0)

                    val charColor = when {
                        isHead -> Color(0xFFE0FFE0)
                        i == 1 -> Color(0xFF00FF66)
                        else -> Color(0xFF00A844).copy(alpha = fade.coerceIn(0.15f, 0.85f))
                    }

                    // Draw glowing cyber glyph / node
                    drawCircle(
                        color = charColor,
                        radius = if (isHead) 3.5f else 2.2f,
                        center = Offset(colX, charY)
                    )

                    if (isHead) {
                        drawCircle(
                            color = Color(0x5500FF66),
                            radius = 9f,
                            center = Offset(colX, charY)
                        )
                    }
                }
            }
        }

        // Interactive touch neon pulse
        if (touchPoint != null) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x9900FF66), Color(0x3300E5FF), Color.Transparent),
                    center = touchPoint,
                    radius = 160f
                ),
                radius = 160f,
                center = touchPoint
            )
        }
    }
}

private data class MatrixStream(
    val column: Int,
    val speed: Float,
    val length: Int,
    val offsetY: Float,
    val charCodes: List<Int>
)

// -------------------------------------------------------------
// 3. DEEP NEBULA & STARFIELD LIVE CANVAS
// -------------------------------------------------------------
@Composable
private fun DeepNebulaLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "nebula")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((35000 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val stars = remember {
        List(65) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 2.2f + 0.8f,
                alpha = Random.nextFloat() * 0.7f + 0.3f,
                twinkleSpeed = Random.nextFloat() * 3f + 1f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width * 0.5f, height * 0.42f)

        // Deep Space Background
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF040209),
                    Color(0xFF0D061A),
                    Color(0xFF080212),
                    Color(0xFF010005)
                )
            )
        )

        // Cosmic Nebula Clouds (Multi-layered blended glowing centers)
        val rad1 = width * 0.65f
        val rad2 = width * 0.55f

        val angleRad = Math.toRadians(rotation.toDouble()).toFloat()
        val nebula1Center = center + Offset(cos(angleRad) * 45f, sin(angleRad) * 35f)
        val nebula2Center = center + Offset(-cos(angleRad * 0.7f) * 60f, -sin(angleRad * 0.7f) * 50f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x557B1FA2), Color(0x334A148C), Color(0x11311B92), Color.Transparent),
                center = nebula1Center,
                radius = rad1
            ),
            radius = rad1,
            center = nebula1Center
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4400B0FF), Color(0x221E88E5), Color(0x080D47A1), Color.Transparent),
                center = nebula2Center,
                radius = rad2
            ),
            radius = rad2,
            center = nebula2Center
        )

        // Center Cosmic Core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x66FF4081), Color(0x22E040FB), Color.Transparent),
                center = center,
                radius = width * 0.35f
            ),
            radius = width * 0.35f,
            center = center
        )

        // Twinkling Starfield
        stars.forEach { star ->
            val twinkle = (sin(rotation * star.twinkleSpeed * 0.1f + star.x * 20f) * 0.35f + 0.65f) * star.alpha
            drawCircle(
                color = Color.White.copy(alpha = twinkle.coerceIn(0.1f, 1f)),
                radius = star.radius,
                center = Offset(star.x * width, star.y * height)
            )
        }

        // Touch Interactive Star Cluster
        if (touchPoint != null) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x88FFD54F), Color(0x44FF80AB), Color.Transparent),
                    center = touchPoint,
                    radius = 120f
                ),
                radius = 120f,
                center = touchPoint
            )
        }
    }
}

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val twinkleSpeed: Float
)

// -------------------------------------------------------------
// 4. BIOLUMINESCENT OCEAN WAVES LIVE CANVAS
// -------------------------------------------------------------
@Composable
private fun OceanWavesLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((6500 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ocean_time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deep Sea Abyss Gradient
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF021024),
                    Color(0xFF05264E),
                    Color(0xFF0A4068),
                    Color(0xFF031B33)
                )
            )
        )

        // Multiple Harmonically Layered Ocean Waves
        val waveLayers = listOf(
            Triple(0.48f, Color(0x4400E5FF), 0.0035f),
            Triple(0.60f, Color(0x660091EA), 0.0045f),
            Triple(0.72f, Color(0x8801579B), 0.0055f),
            Triple(0.85f, Color(0xCC00284D), 0.0065f)
        )

        waveLayers.forEachIndexed { index, (relHeight, color, freq) ->
            val path = Path()
            val base = height * relHeight
            val layerPhase = time * (1f + index * 0.35f) + index * 1.5f
            val amp = height * 0.035f + index * 8f

            path.moveTo(0f, height)
            path.lineTo(0f, base)

            var x = 0f
            val step = 14f
            while (x <= width + step) {
                var touchOffset = 0f
                if (touchPoint != null) {
                    val dist = abs(x - touchPoint.x)
                    if (dist < 180f) {
                        touchOffset = (1f - dist / 180f) * 35f * sin(time * 5f)
                    }
                }

                val y = base + sin(x * freq + layerPhase) * amp +
                        cos(x * freq * 0.5f - layerPhase * 0.7f) * (amp * 0.4f) + touchOffset
                path.lineTo(x, y)
                x += step
            }
            path.lineTo(width, height)
            path.close()

            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = color.alpha * 0.6f), Color(0xFF011222)),
                    startY = base - amp,
                    endY = height
                )
            )

            // Crest Bioluminescent highlights
            drawCircle(
                color = Color(0x9964FFDA),
                radius = 3.5f,
                center = Offset((width * 0.2f + sin(time + index) * width * 0.3f), base - amp * 0.4f)
            )
        }
    }
}

// -------------------------------------------------------------
// 5. PARTICLE SWARM LIVE CANVAS (Interactive touch physics)
// -------------------------------------------------------------
@Composable
private fun ParticleSwarmLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "swarm")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((5000 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swarm_time"
    )

    val swarmNodes = remember {
        List(50) {
            SwarmNode(
                baseX = Random.nextFloat(),
                baseY = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                color = when (Random.nextInt(4)) {
                    0 -> Color(0xFFFF4081)
                    1 -> Color(0xFF00E5FF)
                    2 -> Color(0xFFFFD700)
                    else -> Color(0xFF7C4DFF)
                },
                speedX = Random.nextFloat() * 2f - 1f,
                speedY = Random.nextFloat() * 2f - 1f,
                orbitRadius = Random.nextFloat() * 80f + 20f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Dark Canvas
        drawRect(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFF140727),
                    Color(0xFF080212),
                    Color(0xFF020005)
                ),
                center = Offset(width * 0.5f, height * 0.5f),
                radius = width * 0.8f
            )
        )

        val computedPositions = swarmNodes.map { node ->
            var px = node.baseX * width + cos(time * node.speedX) * node.orbitRadius
            var py = node.baseY * height + sin(time * node.speedY) * node.orbitRadius

            // Touch gravity attraction
            if (touchPoint != null) {
                val dx = touchPoint.x - px
                val dy = touchPoint.y - py
                val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
                if (dist < 350f) {
                    val force = (1f - dist / 350f) * 80f
                    px += (dx / dist) * force
                    py += (dy / dist) * force
                }
            }

            Offset(px, py)
        }

        // Draw connecting neural constellation lines between nearby nodes
        for (i in computedPositions.indices) {
            for (j in i + 1 until computedPositions.size) {
                val p1 = computedPositions[i]
                val p2 = computedPositions[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 110f) {
                    val lineAlpha = (1f - dist / 110f) * 0.35f
                    drawLine(
                        color = Color.White.copy(alpha = lineAlpha),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2f
                    )
                }
            }
        }

        // Draw Nodes
        swarmNodes.forEachIndexed { idx, node ->
            val pos = computedPositions[idx]
            drawCircle(
                color = node.color,
                radius = node.radius,
                center = pos
            )
            drawCircle(
                color = node.color.copy(alpha = 0.3f),
                radius = node.radius * 2.2f,
                center = pos
            )
        }
    }
}

private data class SwarmNode(
    val baseX: Float,
    val baseY: Float,
    val radius: Float,
    val color: Color,
    val speedX: Float,
    val speedY: Float,
    val orbitRadius: Float
)

// -------------------------------------------------------------
// 6. DYNAMIC CHROMATIC BOKEH LIVE CANVAS
// -------------------------------------------------------------
@Composable
private fun DynamicBokehLiveCanvas(
    speedMultiplier: Float,
    touchPoint: Offset?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bokeh")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween((9000 / speedMultiplier.coerceAtLeast(0.3f)).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bokeh_time"
    )

    val orbs = remember {
        List(18) {
            BokehOrb(
                relX = Random.nextFloat(),
                relY = Random.nextFloat(),
                radius = Random.nextFloat() * 70f + 35f,
                color = when (Random.nextInt(5)) {
                    0 -> Color(0xFFFF5722)
                    1 -> Color(0xFFFF4081)
                    2 -> Color(0xFF7C4DFF)
                    3 -> Color(0xFF00E5FF)
                    else -> Color(0xFFFFD600)
                },
                speed = Random.nextFloat() * 0.8f + 0.3f
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Ambient Dark Warm Vignette
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF140812),
                    Color(0xFF240E20),
                    Color(0xFF160613),
                    Color(0xFF0A0208)
                )
            )
        )

        orbs.forEach { orb ->
            val ox = (orb.relX * width + sin(time * orb.speed + orb.relY * 5f) * 60f)
            val oy = (orb.relY * height + cos(time * orb.speed * 0.8f + orb.relX * 5f) * 75f)
            val center = Offset(ox, oy)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb.color.copy(alpha = 0.45f),
                        orb.color.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orb.radius
                ),
                radius = orb.radius,
                center = center
            )
        }

        if (touchPoint != null) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x66FFFFFF), Color(0x33FF4081), Color.Transparent),
                    center = touchPoint,
                    radius = 180f
                ),
                radius = 180f,
                center = touchPoint
            )
        }
    }
}

private data class BokehOrb(
    val relX: Float,
    val relY: Float,
    val radius: Float,
    val color: Color,
    val speed: Float
)
