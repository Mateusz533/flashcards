package pl.mateuszfrejlich.flashcards.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardBox implements CardGroup {
    private static final List<Integer> SECTION_SIZES = List.of(50, 70, 95, 130, 155);
    private static final int FIRST_SECTION_MIN_FILLING = (int) (0.8 * SECTION_SIZES.get(0));
    private final List<Deque<Flashcard>> cardSections;
    private int selectedSectionIndex = 0;
    private Flashcard lentCard = null;

    public CardBox(List<Stream<Flashcard>> sections) {
        if (sections.size() != SECTION_SIZES.size())
            throw new IllegalArgumentException("Required list of size: 5 !!!");
        cardSections = new ArrayList<>(SECTION_SIZES.size());
        for (Stream<Flashcard> stream : sections) {
            ArrayDeque<Flashcard> section = stream.collect(Collectors.toCollection(ArrayDeque::new));
            cardSections.add(section);
        }
        if (deadlockOccurrence(0))
            throw new IllegalArgumentException("Provided cards distribution causes deadlock!");
    }

    public static List<Integer> sectionSizes() {
        return SECTION_SIZES;
    }

    public List<Integer> sectionsFilling() {
        return cardSections.stream().map(Deque::size).toList();
    }

    public List<Stream<Flashcard>> getSections() {
        return cardSections.stream().map(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public Flashcard getBorrowedCard() {
        return lentCard;
    }

    @Override
    public boolean addNewCard(Flashcard card) {
        if (card == null || deadlockOccurrence(1))
            return false;

        Deque<Flashcard> firstSection = cardSections.get(0);
        firstSection.addLast(card);
        return true;
    }

    @Override
    public Flashcard borrowNextCard() {
        if (lentCard != null)
            return null;

        Deque<Flashcard> firstSection = cardSections.get(0);
        if (firstSection.size() <= FIRST_SECTION_MIN_FILLING)
            return null;

        for (int i = 0, sectionsNumber = cardSections.size(); i < sectionsNumber; ++i) {
            Deque<Flashcard> currentSection = cardSections.get(i);
            Deque<Flashcard> nextSection = i + 1 < sectionsNumber ? cardSections.get(i + 1) : null;
            final boolean isNextSectionFull = nextSection != null && nextSection.size() == SECTION_SIZES.get(i + 1);

            if (!isNextSectionFull) {
                lentCard = currentSection.pollFirst();
                selectedSectionIndex = i;
                return lentCard;
            }
        }

        return null;
    }

    @Override
    public Flashcard putCardBack(boolean isPassed) {
        if (lentCard == null)
            return null;

        Flashcard activeCard = lentCard;
        lentCard = null;

        if (!isPassed)
            cardSections.get(0).addLast(activeCard);
        else if (selectedSectionIndex + 1 < SECTION_SIZES.size())
            cardSections.get(selectedSectionIndex + 1).addLast(activeCard);
        else
            return activeCard;

        return null;
    }

    private boolean deadlockOccurrence(int numberOfAddedCards) {
        int stackedFreeSpace = 0;
        for (int i = 0; i < cardSections.size(); ++i) {
            stackedFreeSpace += SECTION_SIZES.get(i) - cardSections.get(i).size();
            if (stackedFreeSpace < i + numberOfAddedCards)
                return true;
        }
        return false;
    }
}
