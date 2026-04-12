package com.example.flashcard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flashcard.data.local.entity.Flashcard

@Composable
fun AddEditFlashcardDialog(
    flashcard: Flashcard? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var front by remember { mutableStateOf(flashcard?.front ?: "") }
    var back by remember { mutableStateOf(flashcard?.back ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (flashcard == null) "Thêm thẻ mới" else "Chỉnh sửa thẻ") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = front,
                    onValueChange = { 
                        front = it
                        if (it.isNotBlank() && back.isNotBlank()) error = null
                    },
                    label = { Text("Mặt trước (Câu hỏi/Từ vựng)") },
                    placeholder = { Text("Ví dụ: Bonjour") },
                    isError = error != null && front.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = back,
                    onValueChange = { 
                        back = it
                        if (it.isNotBlank() && front.isNotBlank()) error = null
                    },
                    label = { Text("Mặt sau (Câu trả lời/Ý nghĩa)") },
                    placeholder = { Text("Ví dụ: Xin chào (tiếng Pháp)") },
                    isError = error != null && back.isBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (front.isNotBlank() && back.isNotBlank()) {
                        onConfirm(front, back)
                    } else {
                        error = "Vui lòng nhập đầy đủ cả hai mặt của thẻ"
                    }
                }
            ) {
                Text(if (flashcard == null) "Thêm thẻ" else "Cập nhật")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
