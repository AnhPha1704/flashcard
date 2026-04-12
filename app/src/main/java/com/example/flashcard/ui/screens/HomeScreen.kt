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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.domain.util.ConnectivityObserver
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.ui.components.AddEditDeckDialog
import com.example.flashcard.ui.components.DeckCard
import com.example.flashcard.ui.components.EmptyState
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onDeckClick: (Int) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val status by viewModel.networkStatus.collectAsState()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<Deck?>(null) }
    var expandedDeckId by remember { mutableStateOf<Int?>(null) }

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
                onClick = { showAddDialog = true },
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
            if (decks.isEmpty() && searchQuery.isEmpty()) {
                EmptyState(onAddClick = { viewModel.addDemoDeck() })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Tìm kiếm bộ thẻ...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (decks.isEmpty()) {
                        item {
                            Text(
                                "Không tìm thấy kết quả nào cho \"$searchQuery\"",
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
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
                                        text = "Bạn có ${decks.size} bộ thẻ phù hợp!",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    
                    items(
                        items = decks,
                        key = { it.deck.id }
                    ) { deckWithCount ->
                        val deck = deckWithCount.deck
                        val count = deckWithCount.cardCount
                        Box {
                            DeckCard(
                                deck = deck,
                                cardCount = count,
                                onClick = { onDeckClick(deck.id) },
                                onMoreClick = { expandedDeckId = deck.id }
                            )
                            
                            DropdownMenu(
                                expanded = expandedDeckId == deck.id,
                                onDismissRequest = { expandedDeckId = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Chỉnh sửa") },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                    onClick = {
                                        deckToEdit = deck
                                        expandedDeckId = null
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa bộ thẻ", color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        viewModel.deleteDeck(deck)
                                        expandedDeckId = null
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Đã xóa bộ thẻ ${deck.name}")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditDeckDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc ->
                viewModel.upsertDeck(name, desc)
                showAddDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Đã tạo bộ thẻ mới!")
                }
            }
        )
    }

    if (deckToEdit != null) {
        AddEditDeckDialog(
            deck = deckToEdit,
            onDismiss = { deckToEdit = null },
            onConfirm = { name, desc ->
                viewModel.upsertDeck(name, desc, deckToEdit!!.id)
                deckToEdit = null
                scope.launch {
                    snackbarHostState.showSnackbar("Đã cập nhật bộ thẻ!")
                }
            }
        )
    }
}


