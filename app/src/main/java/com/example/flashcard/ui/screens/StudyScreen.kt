package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.ui.components.FlashcardCard
import com.example.flashcard.ui.viewmodel.StudyViewModel

/**
 * Màn hình học tập: Hiển thị thẻ hiện tại và các nút điều hướng.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: Int,
    onBack: () -> Unit,
    viewModel: StudyViewModel = viewModel()
) {
    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }
    
    val cards by viewModel.cards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Học bộ thẻ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (cards.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Chưa có thẻ nào trong bộ này!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Quay lại")
                    }
                }
            } else {
                // Thanh tiến trình (Progress Bar)
                val progress = (currentIndex + 1).toFloat() / cards.size
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tiến độ học",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${currentIndex + 1} / ${cards.size}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Khu vực hiển thị thẻ với hiệu ứng lật
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FlashcardCard(
                        flashcard = cards[currentIndex],
                        isFlipped = isFlipped,
                        onFlip = { viewModel.flipCard() }
                    )
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Các nút điều khiển điều hướng
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Quay lại thẻ trước
                    FilledTonalIconButton(
                        onClick = { viewModel.previousCard() },
                        enabled = currentIndex > 0,
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Thẻ trước",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Nút Reset/Học lại (Chỉ hiện khi ở thẻ cuối)
                    if (currentIndex == cards.size - 1 && isFlipped) {
                        Button(
                            onClick = { viewModel.restartSession() },
                            modifier = Modifier.height(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Học lại")
                        }
                    }

                    // Nút Thẻ tiếp theo
                    FilledIconButton(
                        onClick = { 
                            if (currentIndex < cards.size - 1) {
                                viewModel.nextCard()
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        val icon = if (currentIndex < cards.size - 1) 
                            Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Close
                        Icon(
                            imageVector = icon,
                            contentDescription = "Thẻ tiếp theo",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
