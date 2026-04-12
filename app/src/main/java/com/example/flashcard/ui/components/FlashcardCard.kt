package com.example.flashcard.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.ui.theme.*
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSpeak: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "CardRotation"
    )

    // Reset offset when card changes (if needed)
    LaunchedEffect(flashcard.id) {
        offsetX.snapTo(0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .pointerInput(flashcard.id) {
                detectDragGestures(
                    onDragEnd = {
                        val velocity = 800f 
                        if (offsetX.value > 400f) {
                            // Swipe Right -> Learned
                            scope.launch {
                                offsetX.animateTo(1500f, spring(stiffness = Spring.StiffnessLow))
                                onSwipeRight()
                            }
                        } else if (offsetX.value < -400f) {
                            // Swipe Left -> Review
                            scope.launch {
                                offsetX.animateTo(-1500f, spring(stiffness = Spring.StiffnessLow))
                                onSwipeLeft()
                            }
                        } else {
                            // Snap back to center
                            scope.launch {
                                offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                        }
                    }
                )
            }
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = offsetX.value / 20f // Xoay nhẹ theo hướng kéo
                cameraDistance = 16f * density
            }
    ) {
        // Neo-Brutalism Shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 8.dp, y = 8.dp)
                .background(NeoNavy, RoundedCornerShape(12.dp))
        )
        
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                }
                .clickable { if (abs(offsetX.value) < 10f) onFlip() }, // Chỉ flip nếu không đang swipe
            shape = RoundedCornerShape(12.dp),
            color = NeoWhite,
            border = BorderStroke(3.dp, NeoNavy)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (rotation <= 90f) {
                    // Mặt trước (Front)
                    FlashcardSide(
                        text = flashcard.front,
                        label = "question",
                        onSpeak = { onSpeak(flashcard.front) }
                    )
                } else {
                    // Mặt sau (Back)
                    Box(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        contentAlignment = Alignment.Center
                    ) {
                        FlashcardSide(
                            text = flashcard.back,
                            label = "answer",
                            onSpeak = { onSpeak(flashcard.back) }
                        )
                    }
                }

                // --- Swipe Indicators (Overlays) ---
                val swipeProgress = offsetX.value / 400f
                if (swipeProgress > 0.1f) {
                    SwipeIndicator(
                        text = "THUỘC",
                        color = Color(0xFF4CAF50),
                        alpha = (swipeProgress * 1.5f).coerceIn(0f, 0.9f),
                        alignment = Alignment.TopStart
                    )
                } else if (swipeProgress < -0.1f) {
                    SwipeIndicator(
                        text = "ÔN LẠI",
                        color = Color(0xFFF44336),
                        alpha = (abs(swipeProgress) * 1.5f).coerceIn(0f, 0.9f),
                        alignment = Alignment.TopEnd
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeIndicator(
    text: String,
    color: Color,
    alpha: Float,
    alignment: Alignment
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = alignment
    ) {
        Surface(
            color = Color.Transparent,
            border = androidx.compose.foundation.BorderStroke(4.dp, color.copy(alpha = alpha)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
private fun FlashcardSide(
    text: String,
    label: String,
    onSpeak: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pill Label
            Surface(
                color = NeoWhite,
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, NeoNavy)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
            }
            
            Text(
                text = "Tap to flip",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = NeoNavy
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(56.dp)
                    .background(NeoBackgroundBlue, CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = NeoNavy,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                color = NeoNavy
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer layout block to balance top header
        Spacer(modifier = Modifier.height(32.dp))
    }
}



