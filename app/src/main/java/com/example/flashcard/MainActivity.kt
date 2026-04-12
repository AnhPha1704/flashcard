package com.example.flashcard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.flashcard.domain.repository.FlashcardRepository
import androidx.core.content.ContextCompat
import com.example.flashcard.domain.worker.WorkManagerScheduler
import com.example.flashcard.ui.screens.HomeScreen
import com.example.flashcard.ui.screens.StudyScreen
import com.example.flashcard.ui.screens.DeckDetailScreen
import com.example.flashcard.ui.screens.StatsScreen
import com.example.flashcard.ui.theme.FlashcardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ─── Navigation Sealed Classes ────────────────────────────────────────────────

sealed class Screen {
    object Home   : Screen()
    object Stats  : Screen()
    data class DeckDetail(val deckId: Int) : Screen()
    data class Study(val deckId: Int) : Screen()
}

// Tab bottom navigation
enum class BottomTab { HOME, STATS }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: FlashcardRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            WorkManagerScheduler.scheduleDailyReminder(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestNotificationPermission()
        WorkManagerScheduler.scheduleDailyReminder(this)

        enableEdgeToEdge()
        setContent {
            FlashcardTheme {
                // Đồng bộ dữ liệu khi mở app
                LaunchedEffect(Unit) {
                    repository.syncAllData()
                }

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                var selectedTab  by remember { mutableStateOf(BottomTab.HOME) }

                // Xác định xem có đang ở màn hình cấp 1 không (Home / Stats)
                val isTopLevel = currentScreen is Screen.Home || currentScreen is Screen.Stats

                Scaffold(
                    bottomBar = {
                        if (isTopLevel) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected  = selectedTab == BottomTab.HOME,
                                    onClick   = {
                                        selectedTab   = BottomTab.HOME
                                        currentScreen = Screen.Home
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == BottomTab.HOME)
                                                Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Trang chủ"
                                        )
                                    },
                                    label = { Text("Trang chủ") }
                                )
                                NavigationBarItem(
                                    selected  = selectedTab == BottomTab.STATS,
                                    onClick   = {
                                        selectedTab   = BottomTab.STATS
                                        currentScreen = Screen.Stats
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selectedTab == BottomTab.STATS)
                                                Icons.Filled.BarChart else Icons.Outlined.BarChart,
                                            contentDescription = "Thống kê"
                                        )
                                    },
                                    label = { Text("Thống kê") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)) {
                        when (val screen = currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    onDeckClick = { deckId ->
                                        currentScreen = Screen.DeckDetail(deckId)
                                    }
                                )
                            }
                            is Screen.Stats -> {
                                StatsScreen()
                            }
                            is Screen.DeckDetail -> {
                                DeckDetailScreen(
                                    deckId = screen.deckId,
                                    onBack = {
                                        currentScreen = Screen.Home
                                        selectedTab   = BottomTab.HOME
                                    },
                                    onStudyClick = { deckId ->
                                        currentScreen = Screen.Study(deckId)
                                    }
                                )
                            }
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