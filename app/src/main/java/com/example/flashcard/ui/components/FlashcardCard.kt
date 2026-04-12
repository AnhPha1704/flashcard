package com.example.flashcard.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

enum class SwipeDirection {
    LEFT, RIGHT
}

@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSpeak: (String) -> Unit = {},
    externalSwipeTrigger: SwipeDirection? = null,
    onSwipeComplete: () -> Unit = {},
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

    // Handle external swipe trigger (from buttons)
    LaunchedEffect(externalSwipeTrigger) {
        externalSwipeTrigger?.let { direction ->
            val targetX = if (direction == SwipeDirection.RIGHT) 1500f else -1500f
            offsetX.animateTo(targetX, spring(stiffness = Spring.StiffnessMedium))
            if (direction == SwipeDirection.RIGHT) onSwipeRight() else onSwipeLeft()
            onSwipeComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .pointerInput(flashcard.id) {
                detectDragGestures(
                    onDragEnd = {
                        if (offsetX.value > 400f) {
                            scope.launch {
                                offsetX.animateTo(1500f, spring(stiffness = Spring.StiffnessMedium))
                                onSwipeRight()
                            }
                        } else if (offsetX.value < -400f) {
                            scope.launch {
                                offsetX.animateTo(-1500f, spring(stiffness = Spring.StiffnessMedium))
                                onSwipeLeft()
                            }
                        } else {
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
                rotationZ = offsetX.value / 20f
                cameraDistance = 16f * density
            }
    ) {
        // --- Container for 3D Flip (Wrapping both Shadow and Surface) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 16f * density
                }
                .clickable { if (abs(offsetX.value) < 10f) onFlip() }
        ) {
            // Shadow (Inside the flip container)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 12.dp, y = 12.dp)
                    .background(NeoNavy, RoundedCornerShape(24.dp))
            )
            
            // Main Card Surface
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                color = if (rotation <= 90f) NeoWhite else NeoBackgroundBlue,
                border = BorderStroke(4.dp, NeoNavy)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (rotation <= 90f) {
                        FlashcardSide(
                            text = flashcard.front,
                            label = "MẶT TRƯỚC",
                            onSpeak = { onSpeak(flashcard.front) },
                            isFront = true
                        )
                    } else {
                        Box(
                            modifier = Modifier.graphicsLayer { rotationY = 180f },
                            contentAlignment = Alignment.Center
                        ) {
                            FlashcardSide(
                                text = flashcard.back,
                                label = "MẶT SAU",
                                onSpeak = { onSpeak(flashcard.back) },
                                isFront = false
                            )
                        }
                    }
                }
            }
        }

        // --- Swipe Indicators (Overlays - Outside the flip container to stay flat) ---
        val swipeProgress = offsetX.value / 400f
        if (swipeProgress > 0.1f) {
            SwipeIndicator(
                text = "ĐÃ THUỘC",
                color = Color(0xFF4CAF50),
                alpha = (swipeProgress * 1.5f).coerceIn(0f, 0.9f),
                alignment = Alignment.TopStart
            )
        } else if (swipeProgress < -0.1f) {
            SwipeIndicator(
                text = "QUÊN",
                color = Color(0xFFF44336),
                alpha = (abs(swipeProgress) * 1.5f).coerceIn(0f, 0.9f),
                alignment = Alignment.TopEnd
            )
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
            .padding(32.dp),
        contentAlignment = alignment
    ) {
        Surface(
            color = NeoWhite, // Nền trắng để chữ nổi bật
            border = androidx.compose.foundation.BorderStroke(4.dp, color),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.graphicsLayer {
                this.alpha = alpha
                rotationZ = if (alignment == Alignment.TopStart) -15f else 15f // Rotate the stamp
            }
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = MaterialTheme.typography.displaySmall,
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
    onSpeak: () -> Unit,
    isFront: Boolean
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
                color = if (isFront) NeoBackgroundPink else NeoWhite,
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, NeoNavy)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
            }
            
            // Speaker Icon Top Right
            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isFront) NeoWhite else NeoBackgroundPink, CircleShape)
                    .border(BorderStroke(2.dp, NeoNavy), CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = NeoNavy,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        // Center Content
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = NeoNavy,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer hint
        Text(
            text = "Chạm để lật",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = NeoNavy.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}
