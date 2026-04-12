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
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                }
                .clickable { if (abs(offsetX.value) < 10f) onFlip() }, // Chỉ flip nếu không đang swipe
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isFlipped) 2.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (rotation <= 90f) {
                    // Mặt trước (Front)
                    FlashcardSide(
                        text = flashcard.front,
                        label = "CÂU HỎI",
                        gradientColors = listOf(OceanStart, OceanEnd),
                        textColor = MaterialTheme.colorScheme.onSurface,
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
                            label = "ĐÁP ÁN",
                            gradientColors = listOf(SunsetStart, SunsetEnd),
                            textColor = MaterialTheme.colorScheme.onSurface,
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = color.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun FlashcardSide(
    text: String,
    label: String,
    gradientColors: List<Color>,
    textColor: Color,
    onSpeak: () -> Unit
) {
    val mainGradient = Brush.horizontalGradient(gradientColors)
    val faintGradient = Brush.horizontalGradient(gradientColors.map { it.copy(alpha = 0.05f) })

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Decoration Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(mainGradient)
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Label Header
            Surface(
                color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, mainGradient),
                shape = MaterialTheme.shapes.medium
            ) {
                Box(modifier = Modifier.background(faintGradient)) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            IconButton(
                onClick = onSpeak,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Phát âm",
                    tint = gradientColors[0]
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier.weight(1f, fill = false)
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Interaction Instruction
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Chạm để lật thẻ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



