package com.example.flashcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.theme.*

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState()
    val totalCards = decks.sumOf { it.totalCount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeoBackgroundPink)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Thống kê",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = NeoNavy
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                value = "${decks.size}",
                label = "Bộ thẻ",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = "$totalCards",
                label = "Flashcards",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Coming soon card
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .offset(x = 5.dp, y = 5.dp)
                    .background(NeoNavy, RoundedCornerShape(12.dp))
            )
            Surface(
                color = NeoBackgroundBlue,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(3.dp, NeoNavy),
                modifier = Modifier.fillMaxWidth().height(120.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📊", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thống kê nâng cao đang được phát triển!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
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
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeoNavy.copy(alpha = 0.7f)
                )
            }
        }
    }
}
