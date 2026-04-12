package com.example.flashcard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.flashcard.domain.worker.WorkManagerScheduler
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.screens.*
import com.example.flashcard.ui.theme.FlashcardTheme
import com.example.flashcard.ui.theme.NeoBackgroundPink
import com.example.flashcard.ui.theme.NeoNavy
import com.example.flashcard.ui.theme.NeoWhite
import com.example.flashcard.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ──────────────────────────────────────────
// Navigation state
// ──────────────────────────────────────────
sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object MainApp : Screen()
    data class DeckDetail(val deckId: Int) : Screen()
    data class Study(val deckId: Int) : Screen()
}

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Trang chủ", Icons.Default.Home),
    DECKS("Bộ thẻ", Icons.Default.Style),
    STATS("Thống kê", Icons.Default.BarChart),
    SETTINGS("Cài đặt", Icons.Default.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var repository: FlashcardRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) WorkManagerScheduler.scheduleDailyReminder(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestNotificationPermission()
        WorkManagerScheduler.scheduleDailyReminder(this)
        enableEdgeToEdge()

        setContent {
            FlashcardTheme {
                val currentUser by authViewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                var currentTab by remember { mutableStateOf(BottomTab.HOME) }

                // Watch auth state changes
                LaunchedEffect(currentUser) {
                    when (currentScreen) {
                        is Screen.Splash -> { /* wait for splash to finish */ }
                        else -> {
                            if (currentUser == null) {
                                currentScreen = Screen.Login
                            } else if (currentScreen is Screen.Login) {
                                currentScreen = Screen.MainApp
                                repository.syncAllData()
                            }
                        }
                    }
                }

                when (val screen = currentScreen) {
                    // ── Splash ────────────────────────────────────────────
                    is Screen.Splash -> {
                        SplashScreen {
                            currentScreen = if (currentUser != null) Screen.MainApp else Screen.Login
                        }
                    }

                    // ── Login ─────────────────────────────────────────────
                    is Screen.Login -> {
                        LoginScreen(viewModel = authViewModel)
                    }

                    // ── Main App with Bottom Navigation ───────────────────
                    is Screen.MainApp -> {
                        Scaffold(
                            containerColor = NeoBackgroundPink,
                            bottomBar = {
                                NeoBottomNavigationBar(
                                    selectedTab = currentTab,
                                    onTabSelected = { currentTab = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentTab) {
                                    BottomTab.HOME -> HomeScreen(
                                        onDeckClick = { deckId ->
                                            currentScreen = Screen.DeckDetail(deckId)
                                        }
                                    )
                                    BottomTab.DECKS -> HomeScreen(
                                        onDeckClick = { deckId ->
                                            currentScreen = Screen.DeckDetail(deckId)
                                        }
                                    )
                                    BottomTab.STATS -> StatisticsScreen()
                                    BottomTab.SETTINGS -> SettingsScreen(
                                        onLogoutClick = { authViewModel.signOut() }
                                    )
                                }
                            }
                        }
                    }

                    // ── Deck Detail ───────────────────────────────────────
                    is Screen.DeckDetail -> {
                        DeckDetailScreen(
                            deckId = screen.deckId,
                            onBack = { currentScreen = Screen.MainApp },
                            onStudyClick = { deckId -> currentScreen = Screen.Study(deckId) }
                        )
                    }

                    // ── Study ─────────────────────────────────────────────
                    is Screen.Study -> {
                        StudyScreen(
                            deckId = screen.deckId,
                            onBack = { currentScreen = Screen.DeckDetail(screen.deckId) }
                        )
                    }
                }
            }
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

// ──────────────────────────────────────────
// Bottom Navigation Bar (Neo-Brutalism)
// ──────────────────────────────────────────
@Composable
fun NeoBottomNavigationBar(
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Surface(
        color = NeoNavy,
        border = BorderStroke(3.dp, NeoNavy),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        onClick = { onTabSelected(tab) },
                        color = if (isSelected) NeoBackgroundPink else NeoNavy,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) BorderStroke(2.dp, NeoWhite) else null,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) NeoNavy else NeoWhite.copy(alpha = 0.6f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) NeoNavy else NeoWhite.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}