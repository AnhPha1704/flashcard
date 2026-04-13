package com.example.flashcard.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.main.MainViewModel
import com.example.flashcard.main.SortType
import com.example.flashcard.ui.components.AddEditDeckDialog
import com.example.flashcard.ui.components.DeckCard
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.ui.theme.*

@Composable
fun DeckListScreen(
    modifier: Modifier = Modifier,
    onDeckClick: (Int) -> Unit = {},
    viewModel: MainViewModel = viewModel()
) {
    val decks by viewModel.decks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortType by viewModel.sortType.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var deckToEdit by remember { mutableStateOf<Deck?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NeoBackgroundPink,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 48.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Header ---
                item {
                    Text(
                        text = "Flashcard",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Search + Sort Row ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pill Search Bar
                        Surface(
                            color = NeoWhite,
                            shape = RoundedCornerShape(100),
                            border = BorderStroke(2.dp, NeoNavy),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = NeoNavy.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = NeoNavy,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 8.dp)
                                ) { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Tìm kiếm bộ thẻ...",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = NeoNavy.copy(alpha = 0.4f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.updateSearchQuery("") },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = NeoNavy)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Sort button (pill style)
                        Box {
                            Surface(
                                onClick = { showSortMenu = true },
                                color = NeoNavy,
                                shape = RoundedCornerShape(100),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Sắp xếp",
                                        tint = NeoWhite,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mới nhất", fontWeight = if (sortType == SortType.DATE_NEWEST) FontWeight.Black else FontWeight.Normal) },
                                    onClick = { viewModel.updateSortType(SortType.DATE_NEWEST); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cũ nhất", fontWeight = if (sortType == SortType.DATE_OLDEST) FontWeight.Black else FontWeight.Normal) },
                                    onClick = { viewModel.updateSortType(SortType.DATE_OLDEST); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Tên (A-Z)", fontWeight = if (sortType == SortType.NAME_ASC) FontWeight.Black else FontWeight.Normal) },
                                    onClick = { viewModel.updateSortType(SortType.NAME_ASC); showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Tên (Z-A)", fontWeight = if (sortType == SortType.NAME_DESC) FontWeight.Black else FontWeight.Normal) },
                                    onClick = { viewModel.updateSortType(SortType.NAME_DESC); showSortMenu = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- Empty / Result count ---
                if (decks.isEmpty() && searchQuery.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📚", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Chưa có bộ thẻ nào!\nNhấn + để tạo bộ thẻ đầu tiên.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NeoNavy,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (decks.isEmpty()) {
                    item {
                        Text(
                            "Không tìm thấy kết quả nào cho \"$searchQuery\"",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center,
                            color = NeoNavy.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // --- Deck List ---
                items(items = decks, key = { it.deck.id }) { deckWithCount ->
                    val deck = deckWithCount.deck
                    DeckCard(
                        deck = deck,
                        cardCount = deckWithCount.totalCount,
                        dueCount = deckWithCount.dueCount,
                        newCount = deckWithCount.newCount,
                        onClick = { onDeckClick(deck.id) },
                        onEdit = { deckToEdit = deck },
                        onDelete = {
                            viewModel.deleteDeck(deck)
                            scope.launch { snackbarHostState.showSnackbar("Đã xóa bộ thẻ ${deck.name}") }
                        }
                    )
                }
            }

            // --- Floating Action Button ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 5.dp, y = 5.dp)
                        .background(NeoNavy, RoundedCornerShape(16.dp))
                )
                Surface(
                    onClick = { showAddDialog = true },
                    color = NeoBackgroundBlue,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, NeoNavy),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tạo bộ thẻ",
                            tint = NeoNavy,
                            modifier = Modifier.size(32.dp)
                        )
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
                scope.launch { snackbarHostState.showSnackbar("Đã tạo bộ thẻ mới!") }
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
                scope.launch { snackbarHostState.showSnackbar("Đã cập nhật bộ thẻ!") }
            }
        )
    }
}
