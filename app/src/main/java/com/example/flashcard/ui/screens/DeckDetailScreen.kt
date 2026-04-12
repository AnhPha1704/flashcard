package com.example.flashcard.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.net.Uri
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.ui.components.AddEditFlashcardDialog
import com.example.flashcard.ui.viewmodel.DeckDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    deckId: Int,
    onBack: () -> Unit,
    onStudyClick: (Int) -> Unit,
    viewModel: DeckDetailViewModel = viewModel()
) {
    val deck by viewModel.deck.collectAsState()
    val flashcards by viewModel.flashcards.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var flashcardToEdit by remember { mutableStateOf<Flashcard?>(null) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importCsv(it, context) { success, msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportCsv(it, context) { success, msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(deck?.name ?: "Chi tiết bộ thẻ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    if (flashcards.isNotEmpty()) {
                        Button(
                            onClick = { onStudyClick(deckId) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Học ngay")
                        }
                    }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nhập từ CSV") },
                                leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    importLauncher.launch("*/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xuất ra CSV") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    val deckName = deck?.name ?: "flashcards"
                                    exportLauncher.launch("$deckName.csv") 
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thẻ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (flashcards.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Bộ thẻ này chưa có thẻ nào",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Thêm thẻ đầu tiên")
                        }
                    }
                }
            } else {
                Text(
                    text = "Danh sách thẻ (${flashcards.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(flashcards) { card ->
                        FlashcardItem(
                            card = card,
                            onEdit = { flashcardToEdit = it },
                            onDelete = { viewModel.deleteFlashcard(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditFlashcardDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { front, back ->
                viewModel.upsertFlashcard(front, back)
                showAddDialog = false
            }
        )
    }

    if (flashcardToEdit != null) {
        AddEditFlashcardDialog(
            flashcard = flashcardToEdit,
            onDismiss = { flashcardToEdit = null },
            onConfirm = { front, back ->
                viewModel.upsertFlashcard(front, back, flashcardToEdit)
                flashcardToEdit = null
            }
        )
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    onEdit: (Flashcard) -> Unit,
    onDelete: (Flashcard) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.front,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.back,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = { onEdit(card) }) {
                Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { onDelete(card) }) {
                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
