package com.example.flashcard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.example.flashcard.domain.repository.FlashcardRepository
import androidx.core.content.ContextCompat
import com.example.flashcard.domain.worker.WorkManagerScheduler
import com.example.flashcard.ui.screens.HomeScreen
import com.example.flashcard.ui.screens.StudyScreen
import com.example.flashcard.ui.theme.FlashcardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen {
    object Home : Screen()
    data class Study(val deckId: Int) : Screen()
}

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
                // Kích thực đồng bộ toàn diện khi mở App
                LaunchedEffect(Unit) {
                    repository.syncAllData()
                }
                
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            onDeckClick = { deckId ->
                                currentScreen = Screen.Study(deckId)
                            }
                        )
                    }
                    is Screen.Study -> {
                        StudyScreen(
                            deckId = screen.deckId,
                            onBack = { currentScreen = Screen.Home }
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