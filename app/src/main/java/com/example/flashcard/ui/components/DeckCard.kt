package com.example.flashcard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.ui.theme.AppGradients

/**
 * DeckCard thiết kế hiện đại, sử dụng Gradient sinh động để thu hút người dùng.
 */
@Composable
fun DeckCard(
    deck: Deck,
    onClick: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    // Chọn gradient dựa trên ID của bộ thẻ
    val gradientColors = AppGradients[deck.id % AppGradients.size]
    val brush = Brush.linearGradient(colors = gradientColors)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.extraLarge,
                ambientColor = gradientColors[0].copy(alpha = 0.5f),
                spotColor = gradientColors[0]
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                        deck.description?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.2f), MaterialTheme.shapes.small)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Thêm",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer với thông tin bổ sung và Nút bắt đầu nhanh
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge số lượng thẻ
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "12 Thẻ",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = gradientColors[0]
                        ),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Học ngay", 
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

}
