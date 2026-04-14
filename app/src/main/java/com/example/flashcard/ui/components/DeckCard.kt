package com.example.flashcard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.ui.theme.NeoNavy
import com.example.flashcard.ui.theme.NeoWhite
import com.example.flashcard.ui.theme.NeoBackgroundPink

@Composable
fun DeckCard(
    deck: Deck,
    cardCount: Int,
    dueCount: Int = 0,
    newCount: Int = 0,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val cardShape = RoundedCornerShape(12.dp)
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 4.dp) // Space for shadow
    ) {
        // Neo-Brutalism Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(NeoNavy, cardShape)
                .clip(cardShape)
        )

        // Main Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = cardShape,
            color = NeoWhite,
            border = BorderStroke(3.dp, NeoNavy)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deck.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = NeoNavy,
                            fontWeight = FontWeight.Black
                        )
                        
                        deck.description?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeoNavy.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }
                    }

                    // More actions button with anchored DropdownMenu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                                contentDescription = "Thêm",
                                tint = NeoNavy
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Chỉnh sửa", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa bộ thẻ", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Counts & Tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total cards
                    Surface(
                        color = NeoWhite,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(2.dp, NeoNavy)
                    ) {
                        Text(
                            text = "$cardCount thẻ",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeoNavy,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Spacer to push counts to right? No, we use Arrangement.spacedBy.
                    // Let's show New Count
                    if (newCount > 0) {
                        Surface(
                            color = com.example.flashcard.ui.theme.NeoBackgroundBlue,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(2.dp, NeoNavy)
                        ) {
                            Text(
                                text = "$newCount mới",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeoNavy,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Show Due Count
                    if (dueCount > 0) {
                        Surface(
                            color = NeoBackgroundPink,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(2.dp, NeoNavy)
                        ) {
                            Text(
                                text = "$dueCount đến hạn",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = NeoNavy,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

