package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.R
import com.example.flashcard.ui.components.FlashcardCard
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.StudyViewModel

/**
 * Màn hình học tập: Neo-Brutalism với nền labyrinth Navy đậm và nút Forgot/Know.
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

    val context = LocalContext.current
    val ttsHelper = remember {
        (context.applicationContext as com.example.flashcard.FlashcardApplication).ttsHelper
    }

    val cards by viewModel.cards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val isTtsReady by ttsHelper.isReady.collectAsState()

    LaunchedEffect(cards, currentIndex, isTtsReady) {
        if (isTtsReady && cards.isNotEmpty() && currentIndex < cards.size) {
            ttsHelper.speak(cards[currentIndex].front)
        }
    }

    LaunchedEffect(isFlipped) {
        if (isFlipped && cards.isNotEmpty()) {
            ttsHelper.speak(cards[currentIndex].back)
        }
    }

    // --- Outer container: Navy background ---
    Box(modifier = Modifier.fillMaxSize().background(NeoNavy)) {

        // === Labyrinth strip TOP ===
        Image(
            painter = painterResource(id = R.drawable.labyrinth),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // === Labyrinth strip BOTTOM ===
        Image(
            painter = painterResource(id = R.drawable.labyrinth),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.Crop,
            alpha = 0.9f
        )

        // === Main content scaffold on top ===
        Column(modifier = Modifier.fillMaxSize()) {
            // --- Custom Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoWhite
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (cards.isNotEmpty() && !isLoading) {
                    Text(
                        text = "${currentIndex + 1} of ${cards.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoWhite
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.restartSession() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Học lại", tint = NeoWhite)
                }
            }

            // --- Content area ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeoWhite, strokeWidth = 5.dp)
                    }
                } else if (cards.isEmpty()) {
                    EmptyStudyState(onBack)
                } else if (isCompleted) {
                    CompletionScreen(
                        totalCards = cards.size,
                        onRestart = { viewModel.restartSession() },
                        onBack = onBack
                    )
                } else {
                    StudyMainContent(
                        cards = cards,
                        currentIndex = currentIndex,
                        isFlipped = isFlipped,
                        onFlip = { viewModel.flipCard() },
                        onLearned = { viewModel.swipeLearned() },
                        onReview = { viewModel.swipeReview() },
                        onSpeak = { text -> ttsHelper.speak(text) }
                    )
                }
            }
        }

        // --- TTS Loading Overlay ---
        AnimatedVisibility(
            visible = !isTtsReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NeoNavy.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeoWhite,
                    border = BorderStroke(3.dp, NeoNavy)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = NeoNavy
                        )
                        Text(
                            "🎧 Đang chuẩn bị giọng đọc...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyMainContent(
    cards: List<com.example.flashcard.data.local.entity.Flashcard>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onLearned: () -> Unit,
    onReview: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.05f))

        // --- Progress bar ---
        val progress by animateFloatAsState(
            targetValue = (currentIndex + 1).toFloat() / cards.size,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "StudyProgress"
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100)),
            color = NeoBackgroundPink,
            trackColor = NeoWhite.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.weight(0.05f))

        // --- Card area ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "CardTransition"
            ) { index ->
                FlashcardCard(
                    flashcard = cards[index],
                    isFlipped = isFlipped,
                    onFlip = onFlip,
                    onSwipeLeft = { onReview() },
                    onSwipeRight = { onLearned() },
                    onSpeak = onSpeak
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // --- Forgot / Know buttons ---
        StudyControls(
            onReview = onReview,
            onLearned = onLearned
        )
    }
}

@Composable
private fun CompletionScreen(
    totalCards: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Completion icon
        Box {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NeoNavy, CircleShape)
            )
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = NeoBackgroundPink,
                border = BorderStroke(3.dp, NeoNavy)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = NeoNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tuyệt vời!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = NeoWhite
        )
        Text(
            text = "Bạn đã hoàn thành $totalCards thẻ.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = NeoWhite.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .offset(x = 5.dp, y = 5.dp)
                        .background(NeoWhite.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                )
                Surface(
                    onClick = onRestart,
                    color = NeoBackgroundPink,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(3.dp, NeoWhite),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = NeoNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Học lại từ đầu", fontWeight = FontWeight.Black, color = NeoNavy)
                        }
                    }
                }
            }

            Surface(
                onClick = onBack,
                color = Color.Transparent,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(3.dp, NeoWhite),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Về màn hình chính", fontWeight = FontWeight.Black, color = NeoWhite)
                }
            }
        }
    }
}

@Composable
private fun EmptyStudyState(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "😅", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có thẻ nào trong bộ này!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = NeoWhite
        )
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            onClick = onBack,
            color = NeoBackgroundPink,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(3.dp, NeoWhite)
        ) {
            Text(
                "Quay lại",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                fontWeight = FontWeight.Black,
                color = NeoNavy
            )
        }
    }
}

@Composable
private fun StudyControls(
    onReview: () -> Unit,
    onLearned: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút Forgot
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(NeoWhite.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )
            Surface(
                onClick = onReview,
                color = NeoWhite,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☹\uFE0F", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Forgot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy
                        )
                    }
                }
            }
        }

        // Nút Know
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(NeoNavy.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            )
            Surface(
                onClick = onLearned,
                color = NeoBackgroundPink,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "😃", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Know",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy
                        )
                    }
                }
            }
        }
    }
}
