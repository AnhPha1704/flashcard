package com.example.flashcard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.data.local.entity.Flashcard

/**
 * FlashcardCard hỗ trợ hiệu ứng lật 3D chuyên nghiệp.
 * Sử dụng rotationY và cameraDistance để tạo độ sâu.
 */
@Composable
fun FlashcardCard(
    flashcard: Flashcard,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation cho góc xoay
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "CardRotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(450.dp)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density // Tăng độ sâu cho hiệu ứng 3D
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFlipped) 4.dp else 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Kiểm tra góc xoay để quyết định hiển thị mặt nào
            if (rotation <= 90f) {
                // Mặt trước (Front)
                FlashcardSide(
                    text = flashcard.front,
                    label = "CÂU HỎI",
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                // Mặt sau (Back)
                // Phải xoay ngược lại 180 độ để nội dung không bị đối xứng (ngược chữ)
                Box(
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    FlashcardSide(
                        text = flashcard.back,
                        label = "ĐÁP ÁN",
                        color = MaterialTheme.colorScheme.secondary
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
    color: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Label chỉ dẫn (CÂU HỎI / ĐÁP ÁN)
        Surface(
            color = color.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Nội dung chính
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                lineHeight = 38.sp,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Chỉ dẫn nhẹ nhàng ở cuối
        Text(
            text = "Chạm để lật thẻ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
