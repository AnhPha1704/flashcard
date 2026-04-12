package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.components.DeckCard
import com.example.flashcard.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDeckClick: (Int) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState()
    val status by viewModel.networkStatus.collectAsState()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bộ thẻ của bạn",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (status == ConnectivityObserver.Status.Available)
                                    Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (status == ConnectivityObserver.Status.Available)
                                    Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (status == ConnectivityObserver.Status.Available) "Sẵn sàng" else "Chế độ Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    viewModel.addDemoDeck()
                    scope.launch {
                        snackbarHostState.showSnackbar("Đã thêm bộ thẻ mẫu thành công!")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = "Thêm bộ thẻ", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (decks.isEmpty()) {
                EmptyState(onAddClick = { viewModel.addDemoDeck() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Hôm nay bạn có ${decks.size} bộ thẻ tuyệt vời cần học!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(
                        items = decks,
                        key = { it.id }
                    ) { deck ->
                        DeckCard(
                            deck = deck,
                            onClick = { onDeckClick(deck.id) },
                            onMoreClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Tính năng chỉnh sửa sẽ sớm ra mắt!")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

