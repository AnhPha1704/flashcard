package com.example.flashcard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDeckDialog(
    deck: Deck? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(deck?.name ?: "") }
    var description by remember { mutableStateOf(deck?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // --- Shadow ---
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 8.dp, y = 8.dp)
                    .background(NeoNavy, RoundedCornerShape(16.dp))
            )

            // --- Main Card ---
            Surface(
                color = NeoWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (deck == null) "Tạo bộ thẻ mới" else "Chỉnh sửa bộ thẻ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Name Field
                    NeoTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (it.isNotBlank()) error = null
                        },
                        label = "Tên bộ thẻ",
                        placeholder = "Ví dụ: Từ vựng IELTS",
                        isError = error != null
                    )

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(top = 4.dp, start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Field
                    NeoTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Mô tả (tùy chọn)",
                        placeholder = "Ghi chú ngắn gọn...",
                        singleLine = false,
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button (Outlined)
                        Surface(
                            onClick = onDismiss,
                            color = Color.Transparent,
                            border = BorderStroke(3.dp, NeoNavy),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).height(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Hủy", fontWeight = FontWeight.Black, color = NeoNavy)
                            }
                        }

                        // Confirm Button (Filled with Shadow Effect)
                        Box(modifier = Modifier.weight(1.2f).height(50.dp)) {
                            // Small dark shadow for the button itself
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(NeoNavy, RoundedCornerShape(12.dp))
                            )
                            Surface(
                                onClick = {
                                    if (name.isNotBlank()) {
                                        onConfirm(name, description)
                                    } else {
                                        error = "Bạn chưa nhập tên bộ thẻ kìa!"
                                    }
                                },
                                color = NeoBackgroundPink,
                                border = BorderStroke(3.dp, NeoNavy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        if (deck == null) "Tạo ngay" else "Lưu lại",
                                        fontWeight = FontWeight.Black,
                                        color = NeoNavy
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = NeoNavy,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = NeoNavy.copy(alpha = 0.4f)) },
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NeoWhite,
                unfocusedContainerColor = NeoWhite,
                focusedBorderColor = NeoNavy,
                unfocusedBorderColor = NeoNavy.copy(alpha = 0.6f),
                focusedTextColor = NeoNavy,
                unfocusedTextColor = NeoNavy,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorContainerColor = NeoWhite
            )
        )
    }
}
