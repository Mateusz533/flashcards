package pl.mateuszfrejlich.flashcards;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBox {
    private static final int[] SECTION_SIZES = new int[]{50, 70, 95, 130, 155};
    private static final float FIRST_SECTION_MIN_FILLING = 0.9F * SECTION_SIZES[1];
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

    public static List<Integer> sectionSizes() {
        return Arrays.stream(SECTION_SIZES).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Integer> sectionsFilling() {
        return cardSections.stream().map(Deque::size).toList();
    }

    public Flashcard popNextCard() {
        Deque<Flashcard> firstSection = cardSections.get(0);
        if (firstSection.size() < FIRST_SECTION_MIN_FILLING)
            return null;

        for (int i = 0, sectionsNumber = cardSections.size(); i < sectionsNumber; ++i) {
            Deque<Flashcard> currentSection = cardSections.get(i);
            Deque<Flashcard> nextSection = i + 1 < sectionsNumber ? cardSections.get(i + 1) : null;
            final boolean isNextSectionFull = nextSection != null && nextSection.size() == SECTION_SIZES[i + 1];

            if (!isNextSectionFull) {
                lentCard = currentSection.pollFirst();
                selectedSectionIndex = i;
                return lentCard;
            }
        }

        return null;
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
        int stackedFreeSpace = 0;
        for (int i = 0; i < cardSections.size(); ++i) {
            stackedFreeSpace += SECTION_SIZES[i] - cardSections.get(i).size();
            if (stackedFreeSpace < i + 1)
                return false;
        }

        Deque<Flashcard> firstSection = cardSections.get(0);
        firstSection.addLast(card);
        return true;
    }

    public List<Stream<Flashcard>> getSections() {
        return cardSections.stream().map(Collection::stream).collect(Collectors.toList());
    }
}
