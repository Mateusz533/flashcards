package pl.mateuszfrejlich.flashcards.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CardBoxTest {

    private static final Flashcard card1 = new Flashcard("a", "b");
    private static final Flashcard card2 = new Flashcard("c", "d");

    @Test
    void cardBoxTest() {
        // given
        final int firstSectionSize = CardBox.sectionSizes().get(0);
        final int secondSectionSize = CardBox.sectionSizes().get(1);
        List<Stream<Flashcard>> deadlockedSections = List.of(createStream(firstSectionSize),
                createStream(secondSectionSize), createStream(0), createStream(0), createStream(0));
        // then
        assertThrows(IllegalArgumentException.class, () -> {
            new CardBox(deadlockedSections);
        });
    }

    @Test
    void getBorrowedCard() {
        // given
        CardBox cardBox = new CardBox(List.of(createStream(41), createStream(0), createStream(0),
                createStream(0), createStream(0)));
        // then
        assertNull(cardBox.getBorrowedCard());
        // when
        cardBox.borrowNextCard();
        // then
        assertNotNull(cardBox.getBorrowedCard());
        // when
        cardBox.putCardBack(true);
        // then
        assertNull(cardBox.getBorrowedCard());
    }

    @Test
    void addNewCard() {
        // given
        CardBox cardBox = new CardBox(List.of(Stream.of(card1, card2), createStream(0), createStream(0),
                createStream(0), createStream(0)));
        Flashcard card = new Flashcard("e", "f");
        // when
        assertTrue(card.isCorrect());
        // then
        assertTrue(cardBox.addNewCard(card));
        // when
        Flashcard lastCard = cardBox.getSections().get(0).skip(2).toList().get(0);
        // then
        assertEquals(lastCard, card);
        // given-then
        assertFalse(cardBox.addNewCard(null));
        // given
        final int firstSectionSize = CardBox.sectionSizes().get(0);
        CardBox partialFullCardBox = new CardBox(List.of(createStream(firstSectionSize), createStream(0),
                createStream(0), createStream(0), createStream(0)));
        // then
        assertFalse(partialFullCardBox.addNewCard(card));
    }

    @Test
    void borrowNextCard() {
        // given
        CardBox littleFilledBox = new CardBox(List.of(createStream(40), createStream(0), createStream(0),
                createStream(0), createStream(0)));
        // then
        assertNull(littleFilledBox.borrowNextCard());
        // given
        List<Flashcard> firstSection = new ArrayList<>(List.of(card1, card2));
        firstSection.addAll(createStream(40).toList());
        CardBox cardBox = new CardBox(List.of(firstSection.stream(), createStream(0), createStream(0),
                createStream(0), createStream(0)));
        // when
        assertEquals(card1, cardBox.borrowNextCard());
        // then
        assertNull(cardBox.borrowNextCard());
        // when
        cardBox.putCardBack(true);
        // then
        assertEquals(card2, cardBox.borrowNextCard());
        // when
        cardBox.putCardBack(true);
        // then
        assertEquals(40, cardBox.sectionsFilling().get(0));
        assertNull(cardBox.borrowNextCard());
    }

    @Test
    void putCardBack() {
        // given
        List<Flashcard> firstSection = new ArrayList<>(List.of(card1, card2));
        firstSection.addAll(createStream(40).toList());
        CardBox cardBox = new CardBox(List.of(firstSection.stream(), createStream(0), createStream(0),
                createStream(0), createStream(0)));
        assertEquals(cardBox.sectionsFilling().get(0), 42);
        // when
        assertNull(cardBox.putCardBack(true));
        assertNull(cardBox.putCardBack(false));
        // then
        assertEquals(42, cardBox.sectionsFilling().get(0));
        // when
        cardBox.borrowNextCard();
        // then
        assertNull(cardBox.putCardBack(true));
        assertEquals(41, cardBox.sectionsFilling().get(0));
        // when
        cardBox.borrowNextCard();
        // then
        assertNull(cardBox.putCardBack(false));
        assertEquals(41, cardBox.sectionsFilling().get(0));
        // given
        List<Stream<Flashcard>> streams = CardBox.sectionSizes().stream().map(this::createStream)
                .collect(Collectors.toCollection(ArrayList::new));
        streams.set(0, streams.get(0).skip(CardBox.sectionSizes().size() - 1));
        List<Flashcard> list5 = new ArrayList<>(streams.get(CardBox.sectionSizes().size() - 1).toList());
        list5.set(0, card2);
        streams.set(CardBox.sectionSizes().size() - 1, list5.stream());
        CardBox box = new CardBox(streams);
        // when
        assertEquals(card2, box.borrowNextCard());
        // then
        assertNotNull(box.putCardBack(true));
    }

    public Stream<Flashcard> createStream(int size) {
        List<Flashcard> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            list.add(card1);

        return list.stream();
    }
}