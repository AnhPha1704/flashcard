package com.example.flashcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.theme.FlashcardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val decks by viewModel.decks.collectAsState()
    val status by viewModel.networkStatus.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            NetworkStatusBar(status)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Demo Offline-first",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { viewModel.addDemoDeck() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Thêm Bộ Thẻ Demo")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Danh sách bộ thẻ (${decks.size}):",
                fontWeight = FontWeight.SemiBold
            )
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(decks) { deck ->
                    DeckItem(deck)
                }
            }
        }
    }
}

@Composable
fun NetworkStatusBar(status: ConnectivityObserver.Status) {
    val backgroundColor = when (status) {
        ConnectivityObserver.Status.Available -> Color(0xFF4CAF50)
        else -> Color(0xFFF44336)
    }
    
    val statusText = when (status) {
        ConnectivityObserver.Status.Available -> "Trạng thái: Online (Đang có mạng)"
        else -> "Trạng thái: Offline (Mất mạng)"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(backgroundColor)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = statusText, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DeckItem(deck: Deck) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = deck.name, fontWeight = FontWeight.Bold)
            deck.description?.let {
                Text(text = it, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}