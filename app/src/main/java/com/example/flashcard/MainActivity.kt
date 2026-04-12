package com.example.flashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.flashcard.domain.repository.FlashcardRepository
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
}