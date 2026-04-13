package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.data.local.entity.DeckWithCount
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onDeckClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.statsOverview.collectAsState()
    val dueDecks by viewModel.dueDecks.collectAsState()
    val totalDue by viewModel.totalDueCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header ---
        HeaderSection()

        // --- Streak & Goal Card ---
        StreakDashboardCard(stats = stats)

        // --- Important Notice (Due Cards) ---
        if (totalDue > 0) {
            DueSummaryCard(totalDue = totalDue)
        }

        // --- Main Sections ---
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "CẦN ÔN NGAY 🔥",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = NeoNavy
            )
            
            if (dueDecks.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeoWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, NeoNavy)
                ) {
                    Text(
                        "Tuyệt quá! Bạn đã hoàn thành hết các thẻ cần ôn rồi. 🚀",
                        modifier = Modifier.padding(20.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = NeoNavy.copy(alpha = 0.6f)
                    )
                }
            } else {
                dueDecks.forEach { deckWithCount ->
                    HomeDeckItem(
                        deckWithCount = deckWithCount,
                        onClick = { onDeckClick(deckWithCount.deck.id) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Chào bạn! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = NeoNavy
            )
            Text(
                text = "Hôm nay định học gì nào?",
                style = MaterialTheme.typography.bodyLarge,
                color = NeoNavy.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StreakDashboardCard(stats: StatsOverview) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(8.dp, 8.dp)
                .background(NeoNavy, RoundedCornerShape(24.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = NeoWhite,
            border = BorderStroke(3.dp, NeoNavy)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHUỖI HỌC TẬP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${stats.streak} Ngày",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    
                    val progress = (stats.todayStudied.toFloat() / stats.dailyGoal.toFloat()).coerceIn(0f, 1f)
                    
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Mục tiêu ngày: ${stats.todayStudied}/${stats.dailyGoal} thẻ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoNavy
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(NeoNavy.copy(alpha = 0.1f), RoundedCornerShape(100))
                            .border(BorderStroke(2.dp, NeoNavy), RoundedCornerShape(100))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(NeoBackgroundBlue, RoundedCornerShape(100))
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF9BAA),
                    border = BorderStroke(3.dp, NeoNavy)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = NeoNavy, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DueSummaryCard(totalDue: Int) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(NeoNavy, RoundedCornerShape(16.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFD54F), // Vàng cảnh báo
            border = BorderStroke(3.dp, NeoNavy)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.NotificationImportant, contentDescription = null, tint = NeoNavy, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Bạn có $totalDue thẻ đã đến hạn ôn tập. Hãy học ngay để không bị quên nhé!",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = NeoNavy
                )
            }
        }
    }
}

@Composable
private fun HomeDeckItem(
    deckWithCount: DeckWithCount,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(NeoNavy, RoundedCornerShape(16.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = NeoWhite,
            border = BorderStroke(2.dp, NeoNavy)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deckWithCount.deck.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = NeoBackgroundPink.copy(alpha = 0.5f), shape = RoundedCornerShape(6.dp)) {
                           Text(
                               text = "${deckWithCount.dueCount} cần ôn",
                               modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                               fontSize = 11.sp,
                               fontWeight = FontWeight.Bold,
                               color = NeoNavy
                           )
                        }
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NeoNavy)
            }
        }
    }
}
