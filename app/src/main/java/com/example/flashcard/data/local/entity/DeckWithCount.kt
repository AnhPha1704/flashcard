package com.example.flashcard.data.local.entity

import androidx.room.Embedded

data class DeckWithCount(
    @Embedded val deck: Deck,
    val cardCount: Int
)
