@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.ui.apps

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.OSViewModel

@Composable
fun ArcadeApp(
    viewModel: OSViewModel,
    modifier: Modifier = Modifier
) {
    val activeGame by viewModel.activeGame.collectAsState()
    val grid2048 by viewModel.grid2048.collectAsState()
    val score2048 by viewModel.score2048.collectAsState()
    val bestScore2048 by viewModel.bestScore2048.collectAsState()

    val snakeBody by viewModel.snakeBody.collectAsState()
    val snakeFood by viewModel.snakeFood.collectAsState()
    val snakeScore by viewModel.snakeScore.collectAsState()
    val isSnakeRunning by viewModel.isSnakeRunning.collectAsState()
    val isSnakeGameOver by viewModel.isSnakeGameOver.collectAsState()

    val tttBoard by viewModel.tttBoard.collectAsState()
    val tttWinner by viewModel.tttWinner.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Arcade", fontWeight = FontWeight.Bold) },
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
                    selected = activeGame == "2048",
                    onClick = { viewModel.setActiveGame("2048") },
                    icon = { Icon(Icons.Filled.Grid4x4, "2048") },
                    label = { Text("2048") }
                )
                NavigationBarItem(
                    selected = activeGame == "SNAKE",
                    onClick = { viewModel.setActiveGame("SNAKE") },
                    icon = { Icon(Icons.Filled.Games, "Snake") },
                    label = { Text("Snake") }
                )
                NavigationBarItem(
                    selected = activeGame == "TICTACTOE",
                    onClick = { viewModel.setActiveGame("TICTACTOE") },
                    icon = { Icon(Icons.Filled.SportsEsports, "Tic-Tac-Toe") },
                    label = { Text("Tic-Tac-Toe") }
                )
            }
        },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeGame) {
                "2048" -> Game2048Screen(
                    grid = grid2048,
                    score = score2048,
                    bestScore = bestScore2048,
                    onMove = { viewModel.move2048(it) },
                    onReset = { viewModel.init2048Game() }
                )
                "SNAKE" -> SnakeGameScreen(
                    snake = snakeBody,
                    food = snakeFood,
                    score = snakeScore,
                    isRunning = isSnakeRunning,
                    isGameOver = isSnakeGameOver,
                    onTurn = { dx, dy -> viewModel.turnSnake(dx, dy) },
                    onStart = { viewModel.startSnakeGame() }
                )
                "TICTACTOE" -> TicTacToeScreen(
                    board = tttBoard,
                    winner = tttWinner,
                    onMove = { viewModel.makeTicTacToeMove(it) },
                    onReset = { viewModel.resetTicTacToe() }
                )
            }
        }
    }
}

@Composable
private fun Game2048Screen(
    grid: Array<IntArray>,
    score: Int,
    bestScore: Int,
    onMove: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxSize()
    ) {
        // Score Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("2048", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ScoreBadge("SCORE", score)
                ScoreBadge("BEST", bestScore)
            }
        }

        // 4x4 Game Grid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2430)),
            modifier = Modifier
                .size(300.dp)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0..3) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..3) {
                            val v = grid[r][c]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(get2048TileColor(v)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (v > 0) {
                                    Text(
                                        text = v.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (v >= 1024) 16.sp else 20.sp,
                                        color = if (v <= 4) Color(0xFF2E3440) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Virtual D-pad directional controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onMove("UP") }) {
                Icon(Icons.Filled.KeyboardArrowUp, "Up", modifier = Modifier.size(36.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                IconButton(onClick = { onMove("LEFT") }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, "Left", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = onReset) {
                    Icon(Icons.Filled.Refresh, "Restart", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onMove("RIGHT") }) {
                    Icon(Icons.Filled.KeyboardArrowRight, "Right", modifier = Modifier.size(36.dp))
                }
            }
            IconButton(onClick = { onMove("DOWN") }) {
                Icon(Icons.Filled.KeyboardArrowDown, "Down", modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun ScoreBadge(label: String, score: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(score.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

fun get2048TileColor(v: Int): Color {
    return when (v) {
        0 -> Color(0xFF2C3240)
        2 -> Color(0xFFEEE4DA)
        4 -> Color(0xFFEDE0C8)
        8 -> Color(0xFFF2B179)
        16 -> Color(0xFFF59563)
        32 -> Color(0xFFF67C5F)
        64 -> Color(0xFFF65E3B)
        128 -> Color(0xFFEDCF72)
        256 -> Color(0xFFEDCC61)
        528, 1024 -> Color(0xFFEDC53F)
        2048 -> Color(0xFF00E676)
        else -> Color(0xFF3F51B5)
    }
}

@Composable
private fun SnakeGameScreen(
    snake: List<Pair<Int, Int>>,
    food: Pair<Int, Int>,
    score: Int,
    isRunning: Boolean,
    isGameOver: Boolean,
    onTurn: (Int, Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Retro Snake", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Text("Score: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        // 16x16 Game Field Grid
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.size(280.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    for (y in 0 until 16) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            for (x in 0 until 16) {
                                val isHead = snake.firstOrNull() == Pair(x, y)
                                val isBody = snake.contains(Pair(x, y))
                                val isFood = food == Pair(x, y)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            when {
                                                isHead -> Color(0xFF00E676)
                                                isBody -> Color(0xFF00C853).copy(alpha = 0.8f)
                                                isFood -> Color(0xFFFF1744)
                                                else -> Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                if (!isRunning || isGameOver) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isGameOver) "Game Over!" else "Press Start",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = onStart) {
                                Text(if (isGameOver) "Play Again" else "Start Game")
                            }
                        }
                    }
                }
            }
        }

        // D-Pad Controller
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = { onTurn(0, -1) }) {
                Icon(Icons.Filled.KeyboardArrowUp, "Up", modifier = Modifier.size(36.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
                IconButton(onClick = { onTurn(-1, 0) }) {
                    Icon(Icons.Filled.KeyboardArrowLeft, "Left", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { onTurn(1, 0) }) {
                    Icon(Icons.Filled.KeyboardArrowRight, "Right", modifier = Modifier.size(36.dp))
                }
            }
            IconButton(onClick = { onTurn(0, 1) }) {
                Icon(Icons.Filled.KeyboardArrowDown, "Down", modifier = Modifier.size(36.dp))
            }
        }
    }
}

@Composable
private fun TicTacToeScreen(
    board: List<String>,
    winner: String?,
    onMove: (Int) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tic-Tac-Toe vs Nova AI", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (winner) {
                    "X" -> "🎉 You Won!"
                    "O" -> "🤖 Nova AI Won!"
                    "DRAW" -> "🤝 It's a Draw!"
                    else -> "Your Turn (X)"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 3x3 Board Grid
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.size(280.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (r in 0..2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (c in 0..2) {
                            val idx = r * 3 + c
                            val value = board[idx]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onMove(idx) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = value,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (value == "X") MaterialTheme.colorScheme.primary else Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onReset,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("New Match")
        }
    }
}
