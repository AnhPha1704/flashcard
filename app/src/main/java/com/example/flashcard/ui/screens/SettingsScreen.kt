package com.example.flashcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.theme.*

@Composable
fun SettingsScreen(
    onLogoutClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val status by viewModel.networkStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackgroundPink)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Cài đặt",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = NeoNavy
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sync Status Card
        SettingsCard {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(
                    imageVector = if (status == ConnectivityObserver.Status.Available)
                        Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = NeoNavy,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Đồng bộ dữ liệu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    Text(
                        text = if (status == ConnectivityObserver.Status.Available)
                            "✅ Đã đồng bộ với Cloud" else "⚠\uFE0F Đang ngoại tuyến",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoNavy.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Card
        SettingsCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Thông tin ứng dụng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
                Text(
                    text = "Flashcard v1.0 • Neo-Brutalism UI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeoNavy.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout button — danger zone
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            // shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .offset(x = 5.dp, y = 5.dp)
                    .background(NeoNavy, RoundedCornerShape(12.dp))
            )
            Surface(
                onClick = onLogoutClick,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Đăng xuất",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // shadow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .background(NeoNavy, RoundedCornerShape(12.dp))
        )
        Surface(
            color = NeoWhite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(3.dp, NeoNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}
