package com.example.flashcard.data.local.entity

import androidx.room.Embedded

data class DeckWithCount(
    @Embedded val deck: Deck,
    val totalCount: Int,
    val dueCount: Int = 0,
    val newCount: Int = 0,
    val forgottenCount: Int = 0
)
