package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.components.DeckCard
import com.example.flashcard.ui.components.EmptyState
import com.example.flashcard.ui.theme.*

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
                    Column(modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            text = "Bộ thẻ của bạn",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            color = if (status == ConnectivityObserver.Status.Available) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (status == ConnectivityObserver.Status.Available)
                                        Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (status == ConnectivityObserver.Status.Available)
                                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (status == ConnectivityObserver.Status.Available) "Đã đồng bộ" else "Chế độ Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (status == ConnectivityObserver.Status.Available)
                                        MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
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
                        snackbarHostState.showSnackbar("Đã thêm bộ thẻ mới!")
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(text = "Tạo bộ thẻ", style = MaterialTheme.typography.labelLarge) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (decks.isEmpty()) {
                EmptyState(onAddClick = { viewModel.addDemoDeck() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚀",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Hôm nay bạn có ${decks.size} bộ thẻ tuyệt vời cần học!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
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


