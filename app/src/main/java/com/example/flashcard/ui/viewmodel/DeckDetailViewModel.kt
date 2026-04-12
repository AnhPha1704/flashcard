package com.example.flashcard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import com.example.flashcard.domain.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
}
