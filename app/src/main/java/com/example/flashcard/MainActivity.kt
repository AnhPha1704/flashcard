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
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.domain.worker.WorkManagerScheduler
import com.example.flashcard.ui.screens.*
import com.example.flashcard.ui.theme.FlashcardTheme
import com.example.flashcard.ui.theme.NeoBackgroundPink
import com.example.flashcard.ui.theme.NeoNavy
import com.example.flashcard.ui.theme.NeoWhite
import com.example.flashcard.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ──────────────────────────────────────────
// Navigation State
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
                val view = LocalView.current
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as ComponentActivity).window
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                    }
                }
                
                val currentUser by authViewModel.currentUser.collectAsState()
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }
                var currentTab by remember { mutableStateOf(BottomTab.HOME) }

                // Theo dõi trạng thái đăng nhập
                LaunchedEffect(currentUser) {
                    when (currentScreen) {
                        is Screen.Splash -> { /* Chờ màn hình khởi động kết thúc */ }
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

                // Luồng đồng bộ hóa dữ liệu tổng thể
                LaunchedEffect(Unit) {
                    repository.syncAllData()
                }

                when (val screen = currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen {
                            currentScreen = if (currentUser != null) Screen.MainApp else Screen.Login
                        }
                    }

                    is Screen.Login -> {
                        LoginScreen(viewModel = authViewModel)
                    }

                    is Screen.MainApp -> {
                        val snackbarHostState = remember { SnackbarHostState() }
                        Scaffold(
                            containerColor = NeoBackgroundPink,
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            bottomBar = {
                                NeoBottomNavigationBar(
                                    selectedTab = currentTab,
                                    onTabSelected = { currentTab = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .padding(innerPadding)
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            ) {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = {
                                        if (targetState.ordinal > initialState.ordinal) {
                                            (slideInHorizontally { it } + fadeIn()).togetherWith(slide
