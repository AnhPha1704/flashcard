package com.example.flashcard.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.*
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.data.local.entity.DeckWithCount
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun HomeScreen(
    onDeckClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.statsOverview.collectAsState()
    val dueDecks by viewModel.dueDecks.collectAsState()
    val totalDue by viewModel.totalDueCount.collectAsState()
    val srsInsight by viewModel.srsInsight.collectAsState()
    val countdownText by viewModel.countdownText.collectAsState()
    val isAllCaughtUp by viewModel.isAllCaughtUp.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Header ---
        HeaderSection()

        // --- Quick Badges (Streak & Today) ---
        QuickStatsBadges(stats = stats)

        // --- Master Action Card (Global Study) ---
        GlobalStudyCard(
            totalDue = totalDue, 
            countdownText = countdownText,
            onClick = { onDeckClick(-1) },
            onLongPress = { viewModel.triggerDebugDue() }
        )

        // --- Smart SRS Insight ---
        SRSInsightCard(insight = srsInsight)

        // --- Decks Section ---
        if (dueDecks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (!isAllCaughtUp) "CÁC BỘ THẺ ĐẾN HẠN" else "BỘ THẺ CỦA BẠN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy.copy(alpha = 0.6f)
                )
                Text(
                    text = "XEM TẤT CẢ →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy,
                    modifier = Modifier.clickable { 
                        // Điều hướng sang tab DECKS sẽ được xử lý ở MainActivity thông qua callback tab
                    }
                )
            }
            
            // Grid layout for decks
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                dueDecks.chunked(2).forEach { rowDecks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowDecks.forEach { deckWithCount ->
                            HomeDeckGridItem(
                                modifier = Modifier.weight(1f),
                                deckWithCount = deckWithCount,
                                onClick = { onDeckClick(deckWithCount.deck.id) }
                            )
                        }
                        // Fill empty space if odd number
                        if (rowDecks.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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
private fun QuickStatsBadges(stats: StatsOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Streak Badge
        BadgeBox(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            value = "${stats.streak} ngày",
            label = "Chuỗi học",
            color = Color(0xFFFF9BAA)
        )
        // Today Badge
        BadgeBox(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            value = "${stats.todayStudied}/${stats.dailyGoal}",
            label = "Hôm nay",
            color = NeoBackgroundBlue
        )
    }
}

@Composable
private fun BadgeBox(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, NeoNavy),
        color = NeoWhite
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = color,
                border = BorderStroke(1.5.dp, NeoNavy)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = NeoNavy)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = NeoNavy)
                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeoNavy.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun GlobalStudyCard(
    totalDue: Int, 
    countdownText: String?, 
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(8.dp, 8.dp)
                .background(NeoNavy, RoundedCornerShape(24.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = if (totalDue > 0) Color(0xFFFFD54F) else NeoWhite,
            border = BorderStroke(3.dp, NeoNavy)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (totalDue > 0) "BẠN ĐÃ SẴN SÀNG CHƯA?" else "MỤC TIÊU ĐÃ XONG! 🎉",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
                Spacer(Modifier.height(8.dp))
                
                if (totalDue > 0) {
                    Text(
                        text = "Bạn có $totalDue thẻ đang chờ được ôn tập. Học ngay để ghi nhớ lâu hơn nhé!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeoNavy.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeoNavy),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("HỌC TẤT CẢ NGAY", fontWeight = FontWeight.Black, fontSize = 16.sp, color = NeoWhite)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NeoBackgroundBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.5.dp, NeoNavy), RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onLongPress() }
                                )
                            }
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = NeoNavy)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Thẻ tiếp theo sẽ xuất hiện sau:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeoNavy.copy(alpha = 0.6f)
                            )
                            Text(
                                text = countdownText ?: "Chưa có lịch mới",
                                fontSize = 18.sp,
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

@Composable
private fun SRSInsightCard(insight: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NeoBackgroundPink.copy(alpha = 0.3f),
        border = BorderStroke(2.dp, NeoNavy)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFF59E0B))
            Spacer(Modifier.width(12.dp))
            Text(
                text = insight,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = NeoNavy
            )
        }
    }
}

@Composable
private fun HomeDeckGridItem(
    modifier: Modifier = Modifier,
    deckWithCount: DeckWithCount,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = deckWithCount.deck.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = NeoBackgroundPink.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${deckWithCount.dueCount} thẻ",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeoNavy
                    )
                }
            }
        }
    }
}
