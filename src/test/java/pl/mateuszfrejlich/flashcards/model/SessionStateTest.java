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
import static pl.mateuszfrejlich.flashcards.model.SessionState.*;

@ExtendWith(MockitoExtension.class)
class SessionStateTest {
    private static final Flashcard card1 = new Flashcard("a", "b");
    @Mock
    private ActiveCollectionChangeEventPublisher activeCollectionChangeEventPublisher;
    @Mock
    private CardStateChangeEventPublisher cardStateChangeEventPublisher;
    @Mock
    private CardGroupChoiceChangeEventPublisher cardGroupChoiceChangeEventPublisher;
    private SessionState state;

    private static CardCollection newNonEmptyCollection() {
        return new CardCollection("a", Stream.of(card1, card1), Stream.of(),
                List.of(Stream.of(), Stream.of(), Stream.of(), Stream.of(), Stream.of()));
    }

    @BeforeEach
    void init() {
        state = new SessionState(activeCollectionChangeEventPublisher, cardStateChangeEventPublisher,
                cardGroupChoiceChangeEventPublisher);
    }

    @Test
    void setActiveCollection_noGroupSelected_setProperly() {
        // given
        CardCollection anyCollection = newNonEmptyCollection();

        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
        assertNull(state.getActiveCollection());
        // when
        assertTrue(state.setActiveCollection(anyCollection));
        // then
        assertEquals(anyCollection, state.getActiveCollection());
    }

    @Test
    void setActiveCollection_cardAlreadyBorrowed_ignoreSetting() {
        // given
        CardCollection anyStartCollection = newNonEmptyCollection();
        CardCollection anyOtherCollection = newNonEmptyCollection();
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;
        state.setActiveCollection(anyStartCollection);
        state.setCardGroupChoice(anyChoice);
        state.getActiveCollection().borrowNextCard();

        assertEquals(anyStartCollection, state.getActiveCollection());
        assertNotEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
        assertNotNull(state.getActiveCollection().getBorrowedCard());
        // when
        assertFalse(state.setActiveCollection(null));
        assertFalse(state.setActiveCollection(anyOtherCollection));
        // then
        assertEquals(anyStartCollection, state.getActiveCollection());
    }

    @Test
    void setActiveCollection_anyGroupSelectedWithCardPutBack_setProperly() {
        // given
        CardCollection anyStartCollection = newNonEmptyCollection();
        CardCollection anyOtherCollection = newNonEmptyCollection();
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;
        state.setActiveCollection(anyStartCollection);
        state.setCardGroupChoice(anyChoice);

        assertEquals(anyStartCollection, state.getActiveCollection());
        assertNotEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
        assertNull(state.getActiveCollection().getBorrowedCard());
        // when
        assertTrue(state.setActiveCollection(anyOtherCollection));
        // then
        assertEquals(anyOtherCollection, state.getActiveCollection());
        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
    }

    @Test
    void setCardGroupChoice_noActiveCollection_ignoreSetting() {
        // given
        state.setActiveCollection(null);
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;

        assertNull(state.getActiveCollection());
        assertNotEquals(CardGroupChoice.UNSELECTED, anyChoice);
        // when
        assertFalse(state.setCardGroupChoice(anyChoice));
        // then
        assertEquals(CardGroupChoice.UNSELECTED, state.getCardGroupChoice());
    }

    @Test
    void setCardGroupChoice_cardAlreadyBorrowed_ignoreSetting() {
        // given
        CardCollection noEmptyCollection = newNonEmptyCollection();
        CardGroupChoice anyStartChoice = CardGroupChoice.PREPARED;
        CardGroupChoice differentChoice = CardGroupChoice.PREPARED;
        state.setActiveCollection(noEmptyCollection);
        state.setCardGroupChoice(anyStartChoice);
        noEmptyCollection.borrowNextCard();

        assertEquals(noEmptyCollection, state.getActiveCollection());
        assertEquals(anyStartChoice, state.getCardGroupChoice());
        assertNotNull(state.getActiveCollection().getBorrowedCard());
        // when
        assertFalse(state.setCardGroupChoice(differentChoice));
        // then
        assertEquals(anyStartChoice, state.getCardGroupChoice());
    }

    @Test
    void setCardGroupChoice_isActiveCollectionWithCardPutBack_setProperly() {
        // given
        CardCollection anyCollection = newNonEmptyCollection();
        CardGroupChoice anyChoice = CardGroupChoice.PREPARED;
        state.setActiveCollection(anyCollection);

        assertEquals(anyCollection, state.getActiveCollection());
        assertNull(state.getActiveCollection().getBorrowedCard());
        // when
        assertTrue(state.setCardGroupChoice(anyChoice));
        // then
        assertEquals(anyChoice, state.getCardGroupChoice());
    }
}