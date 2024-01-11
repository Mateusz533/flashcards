package pl.mateuszfrejlich.flashcards.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CardBoxTest {

    private static final Flashcard card1 = new Flashcard("a", "b");
    private static final Flashcard card2 = new Flashcard("c", "d");
    private static final Flashcard card3 = new Flashcard("e", "f");
    private static final int minFirstSectionFilling = 40;
    private static CardBox cardBox;

    private static Stream<Flashcard> streamOfCard1(int size) {
        List<Flashcard> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            list.add(card1);

        return list.stream();
    }

    private static List<Stream<Flashcard>> listWithOnlyFirstSectionNonEmpty(Stream<Flashcard> cards) {
        return List.of(cards, streamOfCard1(0), streamOfCard1(0), streamOfCard1(0), streamOfCard1(0));
    }

    private static List<Stream<Flashcard>> listWithOnlyFirstSectionNonEmpty(int size) {
        return listWithOnlyFirstSectionNonEmpty(streamOfCard1(size));
    }

    @BeforeEach
    void init() {
        Stream<Flashcard> firstSection = Stream.concat(Stream.of(card1, card2), streamOfCard1(minFirstSectionFilling));
        cardBox = new CardBox(listWithOnlyFirstSectionNonEmpty(firstSection));
    }

    @Test
    void constructorCardBox() {
        // given
        final int firstSectionSize = CardBox.sectionSizes().get(0);
        final int secondSectionSize = CardBox.sectionSizes().get(1);
        List<Stream<Flashcard>> deadlockedSections = List.of(streamOfCard1(firstSectionSize),
                streamOfCard1(secondSectionSize), streamOfCard1(0), streamOfCard1(0), streamOfCard1(0));
        // when-then
        assertThrows(IllegalArgumentException.class, () -> new CardBox(deadlockedSections));
    }

    @Test
    void getBorrowedCard_isNotBorrowed_getNull() {
        // given
        assertNull(cardBox.putCardBack(true));
        // when
        Flashcard receivedCard = cardBox.getBorrowedCard();
        // then
        assertNull(receivedCard);
    }

    @Test
    void getBorrowedCard_isBorrowed_getProperly() {
        // given
        CardBox cardBox = new CardBox(listWithOnlyFirstSectionNonEmpty(minFirstSectionFilling + 1));

        assertEquals(card1, cardBox.borrowNextCard());
        // when
        Flashcard receivedCard = cardBox.getBorrowedCard();
        // then
        assertEquals(card1, receivedCard);
    }

    @Test
    void addNewCard_nonNullCard_addProperly() {
        // given
        final int startFilling = cardBox.sectionsFilling().get(0);
        // when
        assertTrue(cardBox.addNewCard(card3));
        // then
        assertEquals(startFilling + 1, cardBox.sectionsFilling().get(0));
        Flashcard lastCard = cardBox.getSections().get(0).skip(startFilling).toList().get(0);
        assertEquals(card3, lastCard);
    }

    @Test
    void addNewCard_nullCard_ignoreAddition() {
        // given
        final int startFilling = cardBox.sectionsFilling().get(0);
        // when
        assertFalse(cardBox.addNewCard(null));
        // then
        assertEquals(startFilling, cardBox.sectionsFilling().get(0));
    }

    @Test
    void addNewCard_firstSectionFull_ignoreAddition() {
        // given
        final int firstSectionSize = CardBox.sectionSizes().get(0);
        CardBox partialFullCardBox = new CardBox(listWithOnlyFirstSectionNonEmpty(firstSectionSize));
        final int firstSectionFilling = partialFullCardBox.sectionsFilling().get(0);

        assertEquals(firstSectionSize, firstSectionFilling);
        // when
        assertFalse(partialFullCardBox.addNewCard(card3));
        // then
        assertEquals(firstSectionSize, partialFullCardBox.sectionsFilling().get(0));
    }

    @Test
    void borrowNextCard_tooLittleBoxFilling_ignoreAction() {
        // given
        CardBox littleFilledBox = new CardBox(listWithOnlyFirstSectionNonEmpty(minFirstSectionFilling));

        assertNull(littleFilledBox.getBorrowedCard());
        // when
        assertNull(littleFilledBox.borrowNextCard());
        // then
        assertNull(littleFilledBox.getBorrowedCard());
    }

    @Test
    void borrowNextCard_enoughBoxFilling_borrowProperly() {
        // given
        Stream<Flashcard> firstSection = Stream.concat(Stream.of(card2), streamOfCard1(minFirstSectionFilling));
        CardBox cardBox = new CardBox(listWithOnlyFirstSectionNonEmpty(firstSection));

        assertNull(cardBox.getBorrowedCard());
        // when
        assertEquals(card2, cardBox.borrowNextCard());
        // then
        assertEquals(card2, cardBox.getBorrowedCard());
    }

    @Test
    void borrowNextCard_cardAlreadyBorrowed_ignoreAction() {
        // given
        cardBox.borrowNextCard();

        assertEquals(card1, cardBox.getBorrowedCard());
        // when
        assertNull(cardBox.borrowNextCard());
        // then
        assertEquals(card1, cardBox.getBorrowedCard());
    }

    @Test
    void borrowNextCard_noCardAlreadyBorrowed_borrowProperly() {
        // given
        assertEquals(card1, cardBox.getSections().get(0).findFirst().orElse(null));
        assertNull(cardBox.getBorrowedCard());
        // when
        assertEquals(card1, cardBox.borrowNextCard());
        // then
        assertEquals(card1, cardBox.getBorrowedCard());
    }

    @Test
    void putCardBack_noCardAlreadyBorrowed_ignoreAction() {
        // given
        final int startFilling = cardBox.sectionsFilling().get(0);

        assertEquals(startFilling, cardBox.sectionsFilling().get(0));
        assertNull(cardBox.getBorrowedCard());
        // when
        assertNull(cardBox.putCardBack(true));
        assertNull(cardBox.putCardBack(false));
        // then
        assertEquals(startFilling, cardBox.sectionsFilling().get(0));
    }

    @Test
    void putCardBack_cardIsPassed_moveThatCardToNextSection() {
        // given
        final int startFilling = cardBox.sectionsFilling().get(0);
        cardBox.borrowNextCard();

        assertEquals(card1, cardBox.getBorrowedCard());
        assertEquals(startFilling - 1, cardBox.sectionsFilling().get(0));
        assertEquals(0, cardBox.sectionsFilling().get(1));
        // when
        assertNull(cardBox.putCardBack(true));
        // then
        assertEquals(startFilling - 1, cardBox.sectionsFilling().get(0));
        assertEquals(1, cardBox.sectionsFilling().get(1));
    }

    @Test
    void putCardBack_cardIsNotPassed_moveThatCardToEnd() {
        // given
        Stream<Flashcard> firstSection = Stream.concat(Stream.of(card1, card2), streamOfCard1(minFirstSectionFilling));
        CardBox cardBox = new CardBox(listWithOnlyFirstSectionNonEmpty(firstSection));
        final int startFilling = cardBox.sectionsFilling().get(0);
        cardBox.borrowNextCard();

        assertEquals(startFilling - 1, cardBox.sectionsFilling().get(0));
        assertEquals(card1, cardBox.getBorrowedCard());
        // when
        assertNull(cardBox.putCardBack(false));
        // then
        assertEquals(startFilling, cardBox.sectionsFilling().get(0));
        assertEquals(card2, cardBox.borrowNextCard());
    }

    @Test
    void putCardBack_cardInLastSectionIsPassed_popThatCard() {
        // given
        List<Integer> sectionSizes = CardBox.sectionSizes();
        final int lastSectionIndex = sectionSizes.size() - 1;
        final int lastSectionSize = sectionSizes.get(lastSectionIndex);
        List<Stream<Flashcard>> sections = List.of(
                streamOfCard1(sectionSizes.get(0) - lastSectionIndex),
                streamOfCard1(sectionSizes.get(1)),
                streamOfCard1(sectionSizes.get(2)),
                streamOfCard1(sectionSizes.get(3)),
                Stream.concat(Stream.of(card2), streamOfCard1(lastSectionSize - 1))
        );
        CardBox cardBox = new CardBox(sections);
        final int numOfCards = cardBox.sectionsFilling().stream().reduce(0, Integer::sum);

        assertEquals(card2, cardBox.borrowNextCard());
        // when
        assertEquals(card2, cardBox.putCardBack(true));
        // then
        assertEquals(numOfCards - 1, cardBox.sectionsFilling().stream().reduce(0, Integer::sum));
    }
}