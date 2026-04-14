package com.example.flashcard.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcard.data.local.dao.DeckDao
import com.example.flashcard.data.local.dao.FlashcardDao
import com.example.flashcard.data.local.entity.Deck
import com.example.flashcard.data.local.entity.Flashcard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var deckDao: DeckDao
    private lateinit var flashcardDao: FlashcardDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        deckDao = db.deckDao()
        flashcardDao = db.flashcardDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeDeckAndReadInList() = runBlocking {
        val deck = Deck(name = "Test Deck", description = "Test Description")
        val deckId = deckDao.insertDeck(deck).toInt()
        
        val allDecks = deckDao.getAllDecks().first()
        assertEquals(allDecks[0].name, "Test Deck")
        assertEquals(allDecks[0].id, deckId)
    }

    @Test
    @Throws(Exception::class)
    fun insertFlashcardAndVerifyRelation() = runBlocking {
        val deck = Deck(name = "Language", description = "English")
        val deckId = deckDao.insertDeck(deck).toInt()

        val flashcard = Flashcard(
            deckId = deckId,
            front = "Hello",
            back = "Xin chào"
        )
        flashcardDao.insertFlashcard(flashcard)

        val cards = flashcardDao.getFlashcardsByDeck(deckId).first()
        assertEquals(cards.size, 1)
        assertEquals(cards[0].front, "Hello")
        assertEquals(cards[0].deckId, deckId)
    }

    @Test
    @Throws(Exception::class)
    fun deleteDeckAndVerifyFlashcardsDeleted() = runBlocking {
        val deck = Deck(name = "To be deleted", description = null)
        val deckId = deckDao.insertDeck(deck).toInt()

        val flashcard = Flashcard(deckId = deckId, front = "Q", back = "A")
        flashcardDao.insertFlashcard(flashcard)

        // Delete the deck
        val deckToDelete = deckDao.getDeckById(deckId)
        if (deckToDelete != null) {
            deckDao.deleteDeck(deckToDelete)
        }

        val allCards = flashcardDao.getFlashcardsByDeck(deckId).first()
        assertTrue("Flashcards should be deleted due to Cascade", allCards.isEmpty())
    }
}
