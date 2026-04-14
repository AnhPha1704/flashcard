package com.example.flashcard.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.ui.components.AddEditFlashcardDialog
import com.example.flashcard.ui.theme.*
import com.example.flashcard.ui.viewmodel.DeckDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun DeckDetailScreen(
    deckId: Int,
    onBack: () -> Unit,
    onStudyClick: (Int, com.example.flashcard.StudyMode) -> Unit,
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
            viewModel.importCsv(it, context) { _, msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportCsv(it, context) { _, msg ->
                scope.launch { snackbarHostState.showSnackbar(msg) }
            }
        }
    }

    LaunchedEffect(deckId) {
        viewModel.loadDeck(deckId)
    }

    Scaffold(
        containerColor = NeoBackgroundPink,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).statusBarsPadding()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Custom Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(NeoWhite, RoundedCornerShape(12.dp))
                            .border(BorderStroke(2.dp, NeoNavy), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = NeoNavy)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deck?.name ?: "Chi tiết",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = NeoNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${flashcards.size} thẻ",
                            style = MaterialTheme.typography.labelMedium,
                            color = NeoNavy.copy(alpha = 0.6f)
                        )
                    }

                    // Options Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Tùy chọn", tint = NeoNavy)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nhập từ CSV", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    importLauncher.launch("*/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xuất ra CSV", fontWeight = FontWeight.Bold) },
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

                // --- Study Buttons Section ---
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    // 0. HỌC TẤT CẢ (Luôn hiện nếu có thẻ)
                    if (flashcards.isNotEmpty()) {
                        StudyActionButton(
                            text = "HỌC TẤT CẢ (${flashcards.size})",
                            color = NeoWhite,
                            icon = Icons.Default.AllInclusive,
                            onClick = { onStudyClick(deckId, com.example.flashcard.StudyMode.ALL) }
                        )
                    }

                    // 1. ÔN TẬP ĐẾN HẠN
                    val dueCards = flashcards.filter { it.repetitions > 0 && it.nextReview <= System.currentTimeMillis() }
                    if (dueCards.isNotEmpty()) {
                        StudyActionButton(
                            text = "ÔN TẬP ĐẾN HẠN (${dueCards.size})",
                            color = NeoBackgroundPink,
                            icon = Icons.Default.Refresh,
                            onClick = { onStudyClick(deckId, com.example.flashcard.StudyMode.DUE) }
                        )
                    }

                    // 2. HỌC LẠI THẺ QUÊN
                    // Định nghĩa: repetitions = 0 nhưng đã từng có tương tác (lastModified > createdAt)
                    val forgottenCards = flashcards.filter { it.repetitions == 0 && it.lastModified > it.createdAt }
                    if (forgottenCards.isNotEmpty()) {
                        StudyActionButton(
                            text = "HỌC LẠI THẺ QUÊN (${forgottenCards.size})",
                            color = Color(0xFFEF4444),
                            textColor = NeoWhite,
                            icon = Icons.Default.Warning,
                            onClick = { onStudyClick(deckId, com.example.flashcard.StudyMode.FORGOTTEN) }
                        )
                    }

                    // 3. HỌC THẺ MỚI
                    // Định nghĩa: repetitions = 0 và chưa từng tương tác (lastModified <= createdAt)
                    val newCards = flashcards.filter { it.repetitions == 0 && it.lastModified <= it.createdAt }
                    if (newCards.isNotEmpty()) {
                        StudyActionButton(
                            text = "HỌC THẺ MỚI (${newCards.size})",
                            color = NeoBackgroundBlue,
                            icon = Icons.Default.PlayArrow,
                            onClick = { onStudyClick(deckId, com.example.flashcard.StudyMode.NEW) }
                        )
                    }
                }

                // --- List of cards ---
                if (flashcards.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Empty", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = NeoNavy.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chưa có thẻ nào ở đây!", fontWeight = FontWeight.Bold, color = NeoNavy)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(flashcards) { card ->
                            FlashcardItem(
                                card = card,
                                onEdit = { flashcardToEdit = card },
                                onDelete = { viewModel.deleteFlashcard(card) }
                            )
                        }
                    }
                }
            }

            // --- Floating Action Button ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .offset(x = 5.dp, y = 5.dp)
                        .background(NeoNavy, RoundedCornerShape(16.dp))
                )
                Surface(
                    onClick = { showAddDialog = true },
                    color = NeoWhite,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, NeoNavy),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Thêm thẻ", tint = NeoNavy, modifier = Modifier.size(32.dp))
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
private fun StudyActionButton(
    text: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color = NeoNavy,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .offset(x = 4.dp, y = 4.dp)
                .background(NeoNavy, RoundedCornerShape(100))
        )
        Surface(
            onClick = onClick,
            color = color,
            shape = RoundedCornerShape(100),
            border = BorderStroke(3.dp, NeoNavy),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = textColor)
                Spacer(Modifier.width(8.dp))
                Text(text, fontWeight = FontWeight.Black, fontSize = 15.sp, color = textColor)
            }
        }
    }
}

@Composable
fun FlashcardItem(
    card: Flashcard,
    onEdit: (Flashcard) -> Unit,
    onDelete: (Flashcard) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(NeoNavy, RoundedCornerShape(12.dp))
        )
        // Card Content
        Surface(
            color = NeoWhite,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(2.dp, NeoNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.front,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NeoNavy,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.back,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeoNavy.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row {
                    IconButton(onClick = { onEdit(card) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Sửa", tint = NeoNavy)
                    }
                    IconButton(onClick = { onDelete(card) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
