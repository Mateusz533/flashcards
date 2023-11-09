package pl.mateuszfrejlich.flashcards;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBox {
    private static final int[] SECTION_SIZES = new int[]{50, 70, 95, 130, 155};
    private static final float RESERVE_FOR_RETURNING_CARDS = 0.1F * SECTION_SIZES[1];
    private final List<Deque<Flashcard>> cardSections;
    private final CardQueue archive;
    private Flashcard lentCard = null;
    private int selectedSectionIndex = 0;

    public CardBox(CardQueue archive, List<Stream<Flashcard>> sections) {
        if (sections.size() != SECTION_SIZES.length)
            throw new IllegalArgumentException("Required list of size: 5 !!!");
        this.archive = archive;
        cardSections = new ArrayList<>(SECTION_SIZES.length);
        for (Stream<Flashcard> stream : sections) {
            ArrayDeque<Flashcard> section = stream.collect(Collectors.toCollection(ArrayDeque::new));
            cardSections.add(section);
        }
    }

    public Flashcard popNextCard() {
        final boolean firstSectionFull = cardSections.get(0).size() == SECTION_SIZES[0];

        for (int i = 0, size = cardSections.size(); i < size; ++i) {
            Deque<Flashcard> section = cardSections.get(i);
            Deque<Flashcard> nextSection = i + 1 < size ? cardSections.get(i + 1) : null;

            if (section.isEmpty())
                return null;
            if (i != 0 && firstSectionFull)
                return null;
            if (nextSection != null && nextSection.size() == SECTION_SIZES[i + 1])
                continue;

            lentCard = section.pollFirst();
            selectedSectionIndex = i;
        }

        return lentCard;
    }

    public void putBorrowedCard(boolean isPassed) {
        if (lentCard == null)
            return;

        if (!isPassed) {
            cardSections.get(0).addLast(lentCard);
        } else if (selectedSectionIndex + 1 == SECTION_SIZES.length) {
            archive.addCard(lentCard);
        } else {
            cardSections.get(selectedSectionIndex + 1).addLast(lentCard);
        }

        lentCard = null;
    }

    public boolean addNewCard(Flashcard card) {
        Deque<Flashcard> firstSection = cardSections.get(0);
        final boolean enoughSpareSpace = firstSection.size() < SECTION_SIZES[0] - RESERVE_FOR_RETURNING_CARDS;

        if (enoughSpareSpace)
            firstSection.addLast(card);

        return enoughSpareSpace;
    }

    public List<Stream<Flashcard>> getSections() {
        return cardSections.stream().map(Collection::stream).collect(Collectors.toList());
    }
}
