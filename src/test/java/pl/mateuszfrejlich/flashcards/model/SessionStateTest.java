package pl.mateuszfrejlich.flashcards.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SessionStateTest {
    private static final Flashcard card1 = new Flashcard("a", "b");
    @Mock
    private SessionState.ActiveCollectionChangeEventPublisher activeCollectionChangeEventPublisher;
    @Mock
    private SessionState.CardStateChangeEventPublisher cardStateChangeEventPublisher;
    @Mock
    private SessionState.CardGroupChoiceChangeEventPublisher cardGroupChoiceChangeEventPublisher;
    private SessionState state;

    @BeforeEach
    void init() {
        state = new SessionState(activeCollectionChangeEventPublisher, cardStateChangeEventPublisher,
                cardGroupChoiceChangeEventPublisher);
    }

    @Test
    void setActiveCollection() {
        // given
        CardCollection anyCollection = new CardCollection("a", Stream.of(card1, card1), Stream.of(),
                List.of(Stream.of(), Stream.of(), Stream.of(), Stream.of(), Stream.of()));
        assertNull(state.getActiveCollection());
        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
        // when
        assertTrue(state.setActiveCollection(anyCollection));
        // then
        assertEquals(anyCollection, state.getActiveCollection());
        // given
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;
        assertNotEquals(CardGroupChoice.UNSELECTED, anyChoice);
        // when
        assertTrue(state.setCardGroupChoice(anyChoice));
        assertNotNull(anyCollection.borrowNextCard());
        // then
        assertFalse(state.setActiveCollection(null));
        assertFalse(state.setActiveCollection(anyCollection));
        // when
        anyCollection.putCardBack(false);
        // then
        assertTrue(state.setActiveCollection(null));
        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
        assertTrue(state.setActiveCollection(anyCollection));
        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
    }

    @Test
    void setCardGroupChoice() {
        // given
        assertTrue(state.setActiveCollection(null));
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;
        // then
        assertFalse(state.setCardGroupChoice(anyChoice));
        // given
        CardCollection anyCollection = new CardCollection("a", Stream.of(card1, card1), Stream.of(),
                List.of(Stream.of(), Stream.of(), Stream.of(), Stream.of(), Stream.of()));
        assertTrue(state.setActiveCollection(anyCollection));
        // when
        assertNull(anyCollection.getBorrowedCard());
        // then
        assertTrue(state.setCardGroupChoice(anyChoice));
        // given
        assertNotNull(state.getActiveCollection());
        // when
        assertNotNull(anyCollection.borrowNextCard());
        // then
        assertFalse(state.setCardGroupChoice(anyChoice));
    }
}