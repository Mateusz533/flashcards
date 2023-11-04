package pl.mateuszfrejlich.flashcards;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBox {
    final private static int[] sectionSizes = new int[]{50, 70, 95, 130, 155};
    private List<Deque<Flashcard>> cardSections;
    private Flashcard lentCard = null;
    private int selectedSectionIndex = 0;
    private CardQueue archive;

    public CardBox(CardQueue archive, List<Stream<Flashcard>> data) {
        if (data.size() != sectionSizes.length)
            throw new IllegalArgumentException("Required list of size: 5 !!!");
        this.archive = archive;
        cardSections = new ArrayList<>(sectionSizes.length);
        for (Stream<Flashcard> stream : data) {
            ArrayDeque<Flashcard> section = stream.collect(Collectors.toCollection(ArrayDeque::new));
            cardSections.add(section);
        }
    }

    public Flashcard popNextCard() {
        final boolean firstSectionFull = cardSections.get(0).size() == sectionSizes[0];

        for (int i = 0, size = cardSections.size(); i < size; ++i) {
            Deque<Flashcard> section = cardSections.get(i);
            Deque<Flashcard> nextSection = i + 1 < size ? cardSections.get(i + 1) : null;

            if (section.isEmpty())
                return null;
            if (i != 0 && firstSectionFull)
                return null;
            if (nextSection != null && nextSection.size() == sectionSizes[i + 1])
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
        } else if (selectedSectionIndex + 1 == sectionSizes.length) {
            archive.addCard(lentCard);
        } else {
            cardSections.get(selectedSectionIndex + 1).addLast(lentCard);
        }

        lentCard = null;
    }

    public boolean addNewCard(Flashcard card) {
        Deque<Flashcard> firstSection = cardSections.get(0);
        boolean enoughSpareSpace = firstSection.size() < 0.9 * sectionSizes[0];

        if (enoughSpareSpace)
            firstSection.addLast(card);

        return enoughSpareSpace;
    }
}
