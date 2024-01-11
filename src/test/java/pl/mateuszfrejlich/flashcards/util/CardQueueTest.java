package pl.mateuszfrejlich.flashcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CardQueueTest {

    private static final Flashcard card1 = new Flashcard("a", "b");
    private static final Flashcard card2 = new Flashcard("c", "d");
    private static final Flashcard card3 = new Flashcard("e", "f");
    private static CardQueue cardQueue;

    @BeforeEach
    void init() {
        cardQueue = new CardQueue(Stream.of(card1, card2));
    }

    @Test
    void constructorCardQueue_nullStream_throwException() {
        assertThrows(NullPointerException.class, () -> new CardQueue(null));
    }

    @Test
    void getBorrowedCard_isNotBorrowed_getNull() {
        // given
        assertNull(cardQueue.putCardBack(true));
        // when
        Flashcard receivedCard = cardQueue.getBorrowedCard();
        // then
        assertNull(receivedCard);
    }

    @Test
    void getBorrowedCard_isBorrowed_getProperly() {
        // given
        assertEquals(card1, cardQueue.borrowNextCard());
        // when
        Flashcard receivedCard = cardQueue.getBorrowedCard();
        // then
        assertEquals(card1, receivedCard);
    }

    @Test
    void addNewCard_nonNullCard_addProperly() {
        // given
        assertEquals(2, cardQueue.size());
        // when
        assertTrue(cardQueue.addNewCard(card3));
        // then
        assertEquals(3, cardQueue.size());
        Flashcard lastCard = cardQueue.getCards().skip(2).toList().get(0);
        assertEquals(card3, lastCard);
    }

    @Test
    void addNewCard_nullCard_ignoreAddition() {
        // given
        final int startSize = cardQueue.size();
        // when
        assertFalse(cardQueue.addNewCard(null));
        // then
        assertEquals(startSize, cardQueue.size());
    }

    @Test
    void borrowNextCard_emptyQueue_ignoreAction() {
        // given
        CardQueue cardQueue = new CardQueue(Stream.of());

        assertNull(cardQueue.getBorrowedCard());
        // when
        assertNull(cardQueue.borrowNextCard());
        // then
        assertNull(cardQueue.getBorrowedCard());
    }

    @Test
    void borrowNextCard_cardAlreadyBorrowed_ignoreAction() {
        // given
        cardQueue.borrowNextCard();

        assertEquals(card1, cardQueue.getBorrowedCard());
        // when
        assertNull(cardQueue.borrowNextCard());
        // then
        assertEquals(card1, cardQueue.getBorrowedCard());
    }

    @Test
    void borrowNextCard_noCardAlreadyBorrowed_borrowProperly() {
        // given
        CardQueue cardQueue = new CardQueue(Stream.of(card1, card2));

        assertNull(cardQueue.getBorrowedCard());
        // when
        assertEquals(card1, cardQueue.borrowNextCard());
        // then
        assertEquals(card1, cardQueue.getBorrowedCard());
    }

    @Test
    void putCardBack_noCardAlreadyBorrowed_ignoreAction() {
        // given
        assertEquals(2, cardQueue.size());
        assertNull(cardQueue.getBorrowedCard());
        // when
        assertNull(cardQueue.putCardBack(true));
        assertNull(cardQueue.putCardBack(false));
        // then
        assertEquals(2, cardQueue.size());
    }

    @Test
    void putCardBack_cardIsPassed_popThatCard() {
        // given
        cardQueue.borrowNextCard();

        assertEquals(1, cardQueue.size());
        assertEquals(card1, cardQueue.getBorrowedCard());
        // when
        assertEquals(card1, cardQueue.putCardBack(true));
        // then
        assertEquals(1, cardQueue.size());
    }

    @Test
    void putCardBack_cardIsNotPassed_moveThatCardToEnd() {
        // given
        CardQueue cardQueue = new CardQueue(Stream.of(card1, card2));
        cardQueue.borrowNextCard();

        assertEquals(1, cardQueue.size());
        assertEquals(card1, cardQueue.getBorrowedCard());
        // when
        assertNull(cardQueue.putCardBack(false));
        // then
        assertEquals(2, cardQueue.size());
        assertEquals(card2, cardQueue.borrowNextCard());
    }
}