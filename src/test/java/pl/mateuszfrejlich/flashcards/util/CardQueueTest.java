package pl.mateuszfrejlich.flashcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CardQueueTest {

    private static final Flashcard card1 = new Flashcard("a", "b");
    private static final Flashcard card2 = new Flashcard("c", "d");
    private static CardQueue cardQueue;

    @BeforeEach
    void init() {
        cardQueue = new CardQueue(Stream.of(card1, card2));
    }

    @Test
    void getBorrowedCard() {
        // given
        assertNull(cardQueue.getBorrowedCard());
        // when
        cardQueue.borrowNextCard();
        // then
        assertNotNull(cardQueue.getBorrowedCard());
        // when
        cardQueue.putCardBack(true);
        // then
        assertNull(cardQueue.getBorrowedCard());
    }

    @Test
    void addNewCard() {
        // given
        Flashcard card = new Flashcard("e", "f");
        // when
        assertTrue(cardQueue.addNewCard(card));
        // then
        Flashcard lastCard = cardQueue.getCards().skip(2).toList().get(0);
        assertEquals(card, lastCard);
        // given-then
        assertFalse(cardQueue.addNewCard(null));
    }

    @Test
    void borrowNextCard() {
        // when
        assertEquals(card1, cardQueue.borrowNextCard());
        // then
        assertEquals(card1, cardQueue.getBorrowedCard());
        assertNull(cardQueue.borrowNextCard());
        // when
        cardQueue.putCardBack(true);
        // then
        assertEquals(card2, cardQueue.borrowNextCard());
        // when
        cardQueue.putCardBack(true);
        // then
        assertEquals(0, cardQueue.size());
        assertNull(cardQueue.borrowNextCard());
    }

    @Test
    void putCardBack() {
        // given
        assertEquals(2, cardQueue.size());
        assertNull(cardQueue.getBorrowedCard());
        // then
        assertNull(cardQueue.putCardBack(true));
        assertNull(cardQueue.putCardBack(false));
        assertEquals(2, cardQueue.size());
        // given
        assertEquals(2, cardQueue.size());
        // when
        cardQueue.borrowNextCard();
        // then
        assertEquals(card1, cardQueue.putCardBack(true));
        assertEquals(1, cardQueue.size());
        // given
        assertEquals(1, cardQueue.size());
        // when
        cardQueue.borrowNextCard();
        // then
        assertNull(cardQueue.putCardBack(false));
        assertEquals(1, cardQueue.size());
    }
}