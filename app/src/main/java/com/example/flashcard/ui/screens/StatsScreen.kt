package com.example.flashcard.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.flashcard.domain.model.DayStudyCount
import com.example.flashcard.domain.model.StatsOverview
import com.example.flashcard.ui.viewmodel.StatsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

// ─── Màu sắc cho Dashboard ───────────────────────────────────────────────────
private val EasyGreen    = Color(0xFF22C55E)
private val EasyGreenBg  = Color(0xFF14532D).copy(alpha = 0.25f)
private val HardRed      = Color(0xFFEF4444)
private val HardRedBg    = Color(0xFF7F1D1D).copy(alpha = 0.25f)
private val TotalPurple  = Color(0xFFA78BFA)
private val TotalPurpleBg= Color(0xFF4C1D95).copy(alpha = 0.25f)
private val StreakOrange = Color(0xFFF97316)
private val GoalBlue     = Color(0xFF38BDF8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.statsOverview.collectAsState()
    val history by viewModel.weeklyHistory.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Thống kê học tập",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("vi", "VN"))
                                .format(Date()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Streak Card
            StreakCard(streakDays = stats.streak)

            // 2. Thống kê Easy / Hard / Tổng
            StatsSummaryRow(stats = stats)

            // 3. Biểu đồ 7 ngày
            WeeklyBarChart(history = history)

            // 4. Mục tiêu hàng ngày
            DailyGoalCard(stats = stats)

            // Khoảng đệm dưới cho bottom nav
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ─── 1. Streak Card ──────────────────────────────────────────────────────────

@Composable
private fun StreakCard(streakDays: Int) {
    // Animation nhịp đập cho lửa
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFEA580C),
                            Color(0xFFF97316),
                            Color(0xFFFB923C)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Chuỗi học liên tục",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$streakDays",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "ngày",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when {
                            streakDays == 0 -> "Bắt đầu học hôm nay để tạo streak! 💪"
                            streakDays < 3  -> "Tốt lắm! Tiếp tục duy trì nhé!"
                            streakDays < 7  -> "Tuyệt vời! Bạn đang trên đà tốt! 🔥"
                            streakDays < 30 -> "Kiên trì xuất sắc! Không nghỉ nhé! 🏆"
                            else -> "Huyền thoại! Bạn cực kỳ chăm chỉ! 🥇"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                // Flame icon với animation
                Box(
                    modifier = Modifier
                        .size(72.dp * scale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        modifier = Modifier.size(44.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// ─── 2. Stats Summary Row (Easy / Hard / Tổng) ───────────────────────────────

@Composable
private fun StatsSummaryRow(stats: StatsOverview) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${stats.easyCards}",
            label = "Đã thuộc",
            icon = { Icon(Icons.Default.Star, contentDescription = null, tint = EasyGreen, modifier = Modifier.size(20.dp)) },
            valueColor = EasyGreen,
            bgColor = EasyGreenBg
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${stats.hardCards}",
            label = "Cần ôn lại",
            icon = { Icon(Icons.Default.School, contentDescription = null, tint = HardRed, modifier = Modifier.size(20.dp)) },
            valueColor = HardRed,
            bgColor = HardRedBg
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = "${stats.totalCards}",
            label = "Tổng thẻ",
            icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, tint = TotalPurple, modifier = Modifier.size(20.dp)) },
            valueColor = TotalPurple,
            bgColor = TotalPurpleBg
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: @Composable () -> Unit,
    valueColor: Color,
    bgColor: Color
) {
    val targetValue = value.toIntOrNull()?.toFloat() ?: 0f
    val animated by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "count_anim"
    )

    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon()
            Text(
                text = "${animated.toInt()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

// ─── 3. Biểu đồ 7 ngày ───────────────────────────────────────────────────────

@Composable
private fun WeeklyBarChart(history: List<DayStudyCount>) {
    // Xây danh sách 7 ngày đủ (kể cả ngày chưa học = 0)
    val displayData = buildWeekData(history)
    val maxCount = max(1, displayData.maxOfOrNull { it.second } ?: 1)

    // Animation cột
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(displayData) { triggered = true }
    val animProgress by animateFloatAsState(
        targetValue = if (triggered) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "bar_anim"
    )

    val barColor1 = MaterialTheme.colorScheme.primary
    val barColor2 = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoạt động 7 ngày qua",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${displayData.sumOf { it.second }} thẻ",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Canvas biểu đồ
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                drawBarChart(
                    data = displayData,
                    maxCount = maxCount,
                    animProgress = animProgress,
                    barColor1 = barColor1,
                    barColor2 = barColor2,
                    gridColor = gridColor
                )
            }

            Spacer(Modifier.height(8.dp))

            // Labels ngày dưới biểu đồ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                displayData.forEach { (label, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (label == "Hôm nay")
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (label == "Hôm nay") FontWeight.Bold else FontWeight.Normal
                        )
                        if (count > 0) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBarChart(
    data: List<Pair<String, Int>>,
    maxCount: Int,
    animProgress: Float,
    barColor1: Color,
    barColor2: Color,
    gridColor: Color
) {
    val chartHeight = size.height
    val chartWidth = size.width
    val barCount = data.size
    val barWidth = chartWidth / (barCount * 1.8f)
    val gap = (chartWidth - barWidth * barCount) / (barCount + 1)

    // Đường grid ngang (3 dòng)
    for (i in 1..3) {
        val y = chartHeight * (1f - i / 3f)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    data.forEachIndexed { index, (_, count) ->
        val x = gap + index * (barWidth + gap)
        val heightFraction = (count.toFloat() / maxCount) * animProgress
        val barHeight = chartHeight * heightFraction
        val top = chartHeight - barHeight

        // Radius cho góc trên
        val radius = 8.dp.toPx()

        if (barHeight > 0) {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(barColor2, barColor1),
                    startY = top,
                    endY = chartHeight
                ),
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(radius, radius)
            )
        } else {
            // Placeholder bar nhỏ xíu khi count = 0
            drawRoundRect(
                color = gridColor,
                topLeft = Offset(x, chartHeight - 4.dp.toPx()),
                size = Size(barWidth, 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/** Tạo danh sách 7 ngày đủ, fill 0 cho ngày chưa học */
private fun buildWeekData(history: List<DayStudyCount>): List<Pair<String, Int>> {
    val historyMap = history.associate { it.dayTimestamp to it.count }
    val dayLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val result = mutableListOf<Pair<String, Int>>()

    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    for (i in 6 downTo 0) {
        val cal = today.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val ts = (cal.timeInMillis / 86400000L) * 86400000L
        val count = historyMap[ts] ?: 0
        val label = if (i == 0) "Hôm nay" else {
            val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon,...
            when (dow) {
                Calendar.MONDAY    -> "T2"
                Calendar.TUESDAY   -> "T3"
                Calendar.WEDNESDAY -> "T4"
                Calendar.THURSDAY  -> "T5"
                Calendar.FRIDAY    -> "T6"
                Calendar.SATURDAY  -> "T7"
                else               -> "CN"
            }
        }
        result.add(label to count)
    }
    return result
}

// ─── 4. Daily Goal Card ───────────────────────────────────────────────────────

@Composable
private fun DailyGoalCard(stats: StatsOverview) {
    val progress = (stats.todayStudied.toFloat() / stats.dailyGoal).coerceIn(0f, 1f)
    val isCompleted = stats.todayStudied >= stats.dailyGoal

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "goal_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted)
                                    Color(0xFF22C55E).copy(alpha = 0.15f)
                                else
                                    Color(0xFF38BDF8).copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.EmojiEvents else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isCompleted) EasyGreen else GoalBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Mục tiêu hôm nay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isCompleted) "🎉 Hoàn thành! Tuyệt vời!" else "Học ${stats.dailyGoal} thẻ mỗi ngày",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompleted) EasyGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Badge số thẻ / mục tiêu
                Surface(
                    color = if (isCompleted) EasyGreenBg else Color(0xFF38BDF8).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${stats.todayStudied}/${stats.dailyGoal}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) EasyGreen else GoalBlue
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = if (isCompleted)
                                    listOf(EasyGreen, Color(0xFF86EFAC))
                                else
                                    listOf(GoalBlue, Color(0xFF7DD3FC))
                            )
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isCompleted)
                    "Bạn đã vượt mục tiêu! Tiếp tục học để nâng cao!"
                else {
                    val remaining = stats.dailyGoal - stats.todayStudied
                    "Còn $remaining thẻ nữa là đạt mục tiêu · ${(progress * 100).toInt()}%"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
