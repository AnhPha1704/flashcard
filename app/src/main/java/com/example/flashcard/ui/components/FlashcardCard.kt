package com.example.flashcard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.ui.theme.*

@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "CardRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density 
            }
            .clickable { onFlip() },
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
                    textColor = MaterialTheme.colorScheme.onSurface
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
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashcardSide(
    text: String,
    label: String,
    gradientColors: List<Color>,
    textColor: Color
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
            
            Spacer(modifier = Modifier.height(60.dp))
            
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


