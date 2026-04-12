package com.example.flashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.flashcard.ui.screens.HomeScreen
import com.example.flashcard.ui.screens.StudyScreen
import com.example.flashcard.ui.screens.DeckDetailScreen
import com.example.flashcard.ui.theme.FlashcardTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class Screen {
    object Home : Screen()
    data class DeckDetail(val deckId: Int) : Screen()
    data class Study(val deckId: Int) : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            onDeckClick = { deckId ->
                                currentScreen = Screen.DeckDetail(deckId)
                            }
                        )
                    }
                    is Screen.DeckDetail -> {
                        DeckDetailScreen(
                            deckId = screen.deckId,
                            onBack = { currentScreen = Screen.Home },
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