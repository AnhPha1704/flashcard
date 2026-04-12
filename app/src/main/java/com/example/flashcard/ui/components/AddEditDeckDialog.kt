package com.example.flashcard.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flashcard.data.local.entity.Deck

@Composable
fun AddEditDeckDialog(
    deck: Deck? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(deck?.name ?: "") }
    var description by remember { mutableStateOf(deck?.description ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (deck == null) "Tạo bộ thẻ mới" else "Chỉnh sửa bộ thẻ") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        if (it.isNotBlank()) error = null
                    },
                    label = { Text("Tên bộ thẻ") },
                    placeholder = { Text("Ví dụ: Tiếng Anh giao tiếp") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả (không bắt buộc)") },
                    placeholder = { Text("Ghi chú ngắn gọn về bộ thẻ") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, description)
                    } else {
                        error = "Tên bộ thẻ không được để trống"
                    }
                }
            ) {
                Text(if (deck == null) "Tạo ngay" else "Cập nhật")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
