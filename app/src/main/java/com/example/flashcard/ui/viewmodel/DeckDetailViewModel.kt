package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import com.example.flashcard.domain.util.CsvHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import javax.inject.Inject

@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _deck = MutableStateFlow<Deck?>(null)
    val deck: StateFlow<Deck?> = _deck.asStateFlow()

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards.asStateFlow()

    fun loadDeck(deckId: Int) {
        viewModelScope.launch {
            val deckData = repository.getDeckById(deckId)
            _deck.value = deckData
            
            repository.getFlashcardsByDeck(deckId).collect {
                _flashcards.value = it
            }
        }
    }

    fun upsertFlashcard(front: String, back: String, flashcard: Flashcard? = null) {
        viewModelScope.launch {
            val deckId = _deck.value?.id ?: return@launch
            if (flashcard == null) {
                repository.insertFlashcard(
                    Flashcard(deckId = deckId, front = front, back = back)
                )
            } else {
                repository.updateFlashcard(
                    flashcard.copy(front = front, back = back)
                )
            }
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
        }
    }

    fun importCsv(uri: Uri, context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val deckId = _deck.value?.id ?: run {
                onResult(false, "Không tìm thấy bộ thẻ!")
                return@launch
            }
            try {
                var importedCount = 0
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            var line = reader.readLine()
                            // Bỏ qua header nếu người dùng thêm vào (VD: có chứa từ 'front', 'back')
                            if (line != null && line.lowercase().contains("front") && line.lowercase().contains("back")) {
                                line = reader.readLine()
                            }
                            while (line != null) {
                                val parsed = CsvHelper.parseCsvLine(line)
                                if (parsed != null) {
                                    val (front, back) = parsed
                                    repository.insertFlashcard(
                                        Flashcard(deckId = deckId, front = front, back = back)
                                    )
                                    importedCount++
                                }
                                line = reader.readLine()
                            }
                        }
                    }
                }
                onResult(true, "Đã nhập thành công $importedCount thẻ!")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Lỗi khi đọc file: ${e.localizedMessage}")
            }
        }
    }

    fun exportCsv(uri: Uri, context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentCards = _flashcards.value
                var exportedCount = 0
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                            // Viết header trước
                            writer.write("Front,Back")
                            writer.newLine()
                            for (card in currentCards) {
                                writer.write(CsvHelper.toCsvLine(card.front, card.back))
                                writer.newLine()
                                exportedCount++
                            }
                        }
                    }
                }
                onResult(true, "Đã xuất thành công $exportedCount thẻ!")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false, "Lỗi khi xuất file: ${e.localizedMessage}")
            }
        }
    }
}
