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
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.ui.viewmodel.MainViewModel
import com.example.flashcard.ui.viewmodel.AuthViewModel
import com.example.flashcard.domain.repository.AuthRepository
import com.example.flashcard.domain.repository.FlashcardRepository
import androidx.core.content.ContextCompat
import com.example.flashcard.domain.worker.WorkManagerScheduler
import com.example.flashcard.ui.screens.LoginScreen
import com.example.flashcard.ui.screens.HomeScreen
import com.example.flashcard.ui.screens.StudyScreen
import com.example.flashcard.ui.screens.DeckDetailScreen
import com.example.flashcard.ui.theme.FlashcardTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

sealed class Screen {
    object Login : Screen()
    object Home : Screen()
    data class DeckDetail(val deckId: Int) : Screen()
    data class Study(val deckId: Int) : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

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
                val currentUser by authViewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        currentScreen = Screen.Login
                    } else if (currentScreen is Screen.Login) {
                        currentScreen = Screen.Home
                        repository.syncAllData()
                    }
                }
                
                val networkStatus by mainViewModel.networkStatus.collectAsState()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Banner báo Offline
                    if (networkStatus != ConnectivityObserver.Status.Available && currentUser != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.8f))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đang ngoại tuyến. Dữ liệu sẽ đồng bộ khi có mạng.",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (val screen = currentScreen) {
                            is Screen.Login -> {
                                LoginScreen(viewModel = authViewModel)
                            }
                            is Screen.Home -> {
                                HomeScreen(
                                    onDeckClick = { deckId ->
                                        currentScreen = Screen.DeckDetail(deckId)
                                    },
                                    onLogoutClick = {
                                        authViewModel.signOut()
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