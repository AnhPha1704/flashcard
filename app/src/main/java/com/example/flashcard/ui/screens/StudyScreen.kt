package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.ui.components.FlashcardCard
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.StudyViewModel

/**
 * Màn hình học tập: Giao diện cao cấp với phong cách Glassmorphism và chuyển động mượt mà.
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
    // Lấy instance TtsHelper đã được khởi tạo sẵn từ Application (Singleton)
    val ttsHelper = remember {
        (context.applicationContext as com.example.flashcard.FlashcardApplication).ttsHelper
    }

    
    val cards by viewModel.cards.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val isFlipped by viewModel.isFlipped.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val isTtsReady by ttsHelper.isReady.collectAsState()
    val sessionLearnedCount by viewModel.sessionLearnedCount.collectAsState()
    val sessionReviewCount by viewModel.sessionReviewCount.collectAsState()

    // --- Tự động phát âm Mặt trước khi chuyển thẻ hoặc khi TTS vừa sẵn sàng ---
    LaunchedEffect(cards, currentIndex, isTtsReady) {
        if (isTtsReady && cards.isNotEmpty() && currentIndex < cards.size) {
            ttsHelper.speak(cards[currentIndex].front)
        }
    }

    // --- Tự động phát âm Mặt sau khi lật thẻ ---
    LaunchedEffect(isFlipped) {
        if (isFlipped && cards.isNotEmpty()) {
            ttsHelper.speak(cards[currentIndex].back)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // --- Lớp nền Glassmorphism (Blurred Blobs) ---
        StudyBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Học tập",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round
                        )
                    }
                } else if (cards.isEmpty()) {
                    EmptyStudyState(onBack)
                } else if (isCompleted) {
                    CompletionScreen(
                        totalCards = cards.size,
                        learnedCount = sessionLearnedCount,
                        reviewCount = sessionReviewCount,
                        onRestart = { viewModel.restartSession() },
                        onBack = onBack
                    )
                } else {
                    StudyMainContent(
                        cards = cards,
                        currentIndex = currentIndex,
                        isFlipped = isFlipped,
                        onFlip = { viewModel.flipCard() },
                        onPrevious = { viewModel.previousCard() },
                        onNext = { viewModel.nextCard() },
                        onLearned = { viewModel.swipeLearned() },
                        onReview = { viewModel.swipeReview() },
                        onSpeak = { text -> ttsHelper.speak(text) },
                        onBack = onBack,
                        onRestart = { viewModel.restartSession() },
                        sessionLearnedCount = sessionLearnedCount,
                        sessionReviewCount = sessionReviewCount
                    )
                }
            }
        }

        // --- TTS Loading Overlay (chỉ hiện khi chưa sẵn sàng) ---
        AnimatedVisibility(
            visible = !isTtsReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "🎧 Đang chuẩn bị giọng đọc...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Chỉ mất vài giây trong lần đầu tiên",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyBackgroundDecoration() {
    val infiniteTransition = rememberInfiniteTransition(label = "BgTransition")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Blob 1
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp + animOffset1.dp, y = 100.dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        listOf(PrimaryLight.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
        // Blob 2
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-50).dp - animOffset1.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        listOf(SecondaryLight.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun StudyMainContent(
    cards: List<com.example.flashcard.data.local.entity.Flashcard>,
    currentIndex: Int,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onLearned: () -> Unit,
    onReview: () -> Unit,
    onSpeak: (String) -> Unit,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    sessionLearnedCount: Int,
    sessionReviewCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Thanh tiến trình rực rỡ ---
        val progress by animateFloatAsState(
            targetValue = (currentIndex + 1).toFloat() / cards.size,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
            label = "StudyProgress"
        )
        
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.small),
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tiến độ học tập",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${currentIndex + 1} / ${cards.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // --- AnimatedContent cho việc chuyển thẻ ---
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

        Spacer(modifier = Modifier.weight(0.1f))

        // --- Mini session stats ---
        if (sessionLearnedCount > 0 || sessionReviewCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                if (sessionLearnedCount > 0) {
                    Surface(
                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "✓ $sessionLearnedCount đã thuộc",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF22C55E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (sessionReviewCount > 0) {
                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "✗ $sessionReviewCount cần ôn",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Các nút điều khiển ---
        StudyControls(
            currentIndex = currentIndex,
            totalCards = cards.size,
            isFlipped = isFlipped,
            onPrevious = onPrevious,
            onNext = onNext,
            onLearned = onLearned,
            onReview = onReview,
            onRestart = onRestart
        )
    }
}

@Composable
private fun CompletionScreen(
    totalCards: Int,
    learnedCount: Int,
    reviewCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tuyệt vời! 🎉",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Bạn đã hoàn thành phiên học!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Kết quả phiên học
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Đã thuộc
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF22C55E).copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "✓", fontSize = 28.sp, color = Color(0xFF22C55E))
                    Text(
                        text = "$learnedCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = "Đã thuộc",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Cần ôn lại
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.12f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "↺", fontSize = 28.sp, color = Color(0xFFEF4444))
                    Text(
                        text = "$reviewCount",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEF4444)
                    )
                    Text(
                        text = "Cần ôn lại",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Học lại từ đầu")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Về màn hình chính")
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
        Text(
            text = "😅",
            fontSize = 64.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chưa có thẻ nào trong bộ này!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBack,
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Quay lại")
        }
    }
}

@Composable
private fun StudyControls(
    currentIndex: Int,
    totalCards: Int,
    isFlipped: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onLearned: () -> Unit,
    onReview: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hàng 1: Nút Cần ôn lại và Đã thuộc (chỉ hiện khi đã lật thẻ)
        AnimatedVisibility(
            visible = isFlipped,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nút Cần ôn lại (đỏ)
                Button(
                    onClick = onReview,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "✗  Cần ôn lại",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Nút Đã thuộc (xanh)
                Button(
                    onClick = onLearned,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF22C55E),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "✓  Đã thuộc",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hàng 2: Quay lại + Tiếp theo/Hoàn thành
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = onPrevious,
                enabled = currentIndex > 0,
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Thẻ trước",
                    modifier = Modifier.size(24.dp)
                )
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                val text = if (currentIndex < totalCards - 1) "Bỏ qua →" else "Hoàn thành"
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
