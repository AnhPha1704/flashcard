package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.flashcard.ui.components.SwipeDirection
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.StudyViewModel

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

    // State to trigger external swipe
    var pendingSwipe by remember { mutableStateOf<SwipeDirection?>(null) }

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

    // --- Outer container: Pink background ---
    Box(modifier = Modifier.fillMaxSize().background(NeoBackgroundPink)) {

        // === Labyrinth pattern for FULL screen backdrop ===
        Image(
            painter = painterResource(id = R.drawable.labyrinth),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )

        // === Rounded Pink Bottom Shelf (Only visible during study) ===
        if (!isCompleted) {
            Surface(
                color = NeoBackgroundPink,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.18f)
                    .align(Alignment.BottomCenter)
            ) {}
        }

        // === Main content scaffold on top ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Đẩy toàn bộ nội dung xuống dưới thanh trạng thái
        ) {
            // --- Custom Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeoWhite, RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, NeoNavy), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = NeoNavy)
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                if (cards.isNotEmpty() && !isLoading && !isCompleted) {
                    Surface(
                        color = NeoWhite,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(2.dp, NeoNavy)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${cards.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(
                    onClick = { viewModel.restartSession() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(NeoWhite, RoundedCornerShape(12.dp))
                        .border(BorderStroke(2.dp, NeoNavy), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Học lại", tint = NeoNavy)
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
                        CircularProgressIndicator(color = NeoNavy, strokeWidth = 5.dp)
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
                        pendingSwipe = pendingSwipe,
                        onFlip = { viewModel.flipCard() },
                        onLearnedComplete = { 
                            pendingSwipe = null
                            viewModel.swipeLearned() 
                        },
                        onReviewComplete = { 
                            pendingSwipe = null
                            viewModel.swipeReview() 
                        },
                        onLearnedClick = { pendingSwipe = SwipeDirection.RIGHT },
                        onReviewClick = { pendingSwipe = SwipeDirection.LEFT },
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
    pendingSwipe: SwipeDirection?,
    onFlip: () -> Unit,
    onLearnedComplete: () -> Unit,
    onReviewComplete: () -> Unit,
    onLearnedClick: () -> Unit,
    onReviewClick: () -> Unit,
    onSpeak: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        Row(
            modifier = Modifier.fillMaxWidth().height(12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in cards.indices) {
                val color = if (i <= currentIndex) NeoNavy else NeoWhite
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(color, RoundedCornerShape(100))
                        .border(BorderStroke(if (i <= currentIndex) 0.dp else 2.dp, NeoNavy), RoundedCornerShape(100))
                )
            }
        }

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
                    // Enter from side faster, but exit with only fade
                    (slideInHorizontally(animationSpec = tween(200)) { it } + fadeIn(animationSpec = tween(200)))
                        .togetherWith(fadeOut(animationSpec = tween(150)))
                },
                label = "CardTransition"
            ) { index ->
                FlashcardCard(
                    flashcard = cards[index],
                    isFlipped = isFlipped,
                    onFlip = onFlip,
                    onSwipeLeft = onReviewComplete,
                    onSwipeRight = onLearnedComplete,
                    externalSwipeTrigger = pendingSwipe,
                    onSwipeComplete = { /* Handled by individual callbacks */ },
                    onSpeak = onSpeak
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // --- Forgot / Know buttons ---
        StudyControls(
            onReviewClick = onReviewClick,
            onLearnedClick = onLearnedClick
        )
    }
}

@Composable
private fun StudyControls(
    onReviewClick: () -> Unit,
    onLearnedClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val reviewColor = Color(0xFFFF9BAA) 
        val learnedColor = Color(0xFFC7F4C2)

        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NeoNavy, RoundedCornerShape(16.dp))
            )
            Surface(
                onClick = onReviewClick,
                color = reviewColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "☹\uFE0F", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "QUÊN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NeoNavy, RoundedCornerShape(16.dp))
            )
            Surface(
                onClick = onLearnedClick,
                color = learnedColor,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "😃", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "THUỘC",
                            style = MaterialTheme.typography.titleLarge,
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
private fun CompletionScreen(
    totalCards: Int,
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
        // --- Main Achievement Card ---
        Box(modifier = Modifier.fillMaxWidth()) {
            // Shadow for the card
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 10.dp, y = 10.dp)
                    .background(NeoNavy, RoundedCornerShape(32.dp))
            )
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = NeoWhite,
                border = BorderStroke(4.dp, NeoNavy)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Larger Badge Circle
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = NeoBackgroundBlue,
                        border = BorderStroke(4.dp, NeoNavy)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🏆", fontSize = 64.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "TUYỆT VỜI!",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Bạn đã hoàn thành",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeoNavy.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "$totalCards thẻ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons inside card area
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Restart Project Button
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 5.dp, y = 5.dp)
                                    .background(NeoNavy, RoundedCornerShape(16.dp))
                            )
                            Surface(
                                onClick = onRestart,
                                color = NeoBackgroundBlue,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(3.dp, NeoNavy),
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, tint = NeoNavy)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("HỌC LẠI", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeoNavy)
                                    }
                                }
                            }
                        }

                        // Home Button
                        Surface(
                            onClick = onBack,
                            color = NeoWhite,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(3.dp, NeoNavy),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("VỀ TRANG CHỦ", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeoNavy)
                            }
                        }
                    }
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
        Text(text = "😅", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "CHƯA CÓ THẺ NÀO!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NeoNavy
        )
        Text(
            text = "Hãy thêm thẻ vào bộ này trước nhé.",
            style = MaterialTheme.typography.bodyLarge,
            color = NeoNavy.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box {
             Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
                    .offset(x = 4.dp, y = 4.dp)
                    .background(NeoNavy, RoundedCornerShape(16.dp))
            )
            Surface(
                onClick = onBack,
                color = NeoWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.width(200.dp).height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "QUAY LẠI",
                        fontWeight = FontWeight.Black,
                        color = NeoNavy,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
