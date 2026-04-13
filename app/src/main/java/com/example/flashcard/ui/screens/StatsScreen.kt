package com.example.flashcard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flashcard.R
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.statsOverview.collectAsState()
    val history by viewModel.weeklyHistory.collectAsState()
    // --- Stats Content ---

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NeoBackgroundPink
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // --- Top Header ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thống kê",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = SimpleDateFormat("MMM yyyy", Locale("vi", "VN")).format(Date()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeoNavy
                        )
                    }
                }

                // --- Streak Component ---
                StreakNeoCard(streakDays = stats.streak)

                // --- Main Chart ---
                PremiumWeeklyChart(history = history)

                // --- Summary Row ---
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatBox(
                        modifier = Modifier.weight(1f),
                        value = "${stats.todayStudied}",
                        label = "Hôm nay",
                        color = NeoBackgroundBlue
                    )
                    StatBox(
                        modifier = Modifier.weight(1f),
                        value = "${stats.totalCards}",
                        label = "Tổng thẻ",
                        color = NeoWhite
                    )
                }

                // --- Easy / Hard Detail ---
                DetailStatsRow(stats = stats)

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun StreakNeoCard(streakDays: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(8.dp, 8.dp)
                .background(NeoNavy, RoundedCornerShape(24.dp))
        )
        // Main Surface
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = NeoWhite,
            border = androidx.compose.foundation.BorderStroke(3.dp, NeoNavy)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CHUỖI HỌC TẬP",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$streakDays Ngày",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    Text(
                        text = if (streakDays > 0) "Bạn đang làm rất tốt! 🔥" else "Bắt đầu học ngay thôi!",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeoNavy
                    )
                }
                
                Box(contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(80.dp).graphicsLayer(scaleX = scale, scaleY = scale),
                        shape = CircleShape,
                        color = Color(0xFFFF9BAA),
                        border = androidx.compose.foundation.BorderStroke(3.dp, NeoNavy)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = NeoNavy,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.scale(scale: Float) = this.then(Modifier.drawBehind {
    // Actually scale modifier is better, let's just use it
})

@Composable
private fun PremiumWeeklyChart(history: List<DayStudyCount>) {
    val displayData = buildWeekData(history)
    val maxCount = max(1, displayData.maxOfOrNull { it.second } ?: 1)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(6.dp, 6.dp)
                .background(NeoNavy, RoundedCornerShape(20.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = NeoWhite,
            border = androidx.compose.foundation.BorderStroke(3.dp, NeoNavy)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "7 NGÀY QUA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = NeoNavy.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                
                Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val barWidth = size.width / (displayData.size * 2f)
                    val spacing = (size.width - (barWidth * displayData.size)) / (displayData.size + 1)
                    
                    displayData.forEachIndexed { index, pair ->
                        val barHeight = (pair.second.toFloat() / maxCount) * size.height
                        val x = spacing + index * (barWidth + spacing)
                        
                        // Draw Bar
                        drawRoundRect(
                            color = if (pair.first == "Hôm nay") Color(0xFFB4D2FF) else NeoBackgroundPink,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                        // Bar Border
                        drawRoundRect(
                            color = NeoNavy,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx())
                        )
                    }
                }
                
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    displayData.forEach { pair ->
                        Text(
                            text = pair.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (pair.first == "Hôm nay") NeoNavy else NeoNavy.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(4.dp, 4.dp)
                .background(NeoNavy, RoundedCornerShape(16.dp))
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = color,
            border = androidx.compose.foundation.BorderStroke(3.dp, NeoNavy)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = value, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = NeoNavy)
                Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NeoNavy)
            }
        }
    }
}

@Composable
private fun DetailStatsRow(stats: StatsOverview) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailBox(
            modifier = Modifier.weight(1f),
            label = "THUỘC",
            count = stats.easyCards,
            color = Color(0xFFC7F4C2)
        )
        DetailBox(
            modifier = Modifier.weight(1f),
            label = "QUÊN",
            count = stats.hardCards,
            color = Color(0xFFFF9BAA)
        )
    }
}

@Composable
private fun DetailBox(modifier: Modifier = Modifier, label: String, count: Int, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, NeoNavy),
        color = color
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeoNavy)
            Text(text = "$count", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NeoNavy)
        }
    }
}

private fun buildWeekData(history: List<DayStudyCount>): List<Pair<String, Int>> {
    val historyMap = history.associate { it.dayTimestamp to it.count }
    val result = mutableListOf<Pair<String, Int>>()
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    for (i in 6 downTo 0) {
        val cal = today.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val ts = (cal.timeInMillis / 86400000L) * 86400000L
        val count = historyMap[ts] ?: 0
        val label = if (i == 0) "Hôm nay" else when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            else -> "CN"
        }
        result.add(label to count)
    }
    return result
}
