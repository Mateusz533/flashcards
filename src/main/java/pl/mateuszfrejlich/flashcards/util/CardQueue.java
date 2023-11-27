package pl.mateuszfrejlich.flashcards.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardQueue implements CardGroup {
    private final Deque<Flashcard> cards;
    private Flashcard lentCard = null;

    CardQueue(Stream<Flashcard> cards) {
        this.cards = cards.collect(Collectors.toCollection(ArrayDeque::new));
    }

    public int size() {
        return cards.size();
    }

    public Stream<Flashcard> getCards() {
        return cards.stream();
    }

    @Override
    public Flashcard getBorrowedCard() {
        return lentCard;
    }

    @Override
    public boolean addNewCard(Flashcard card) {
        if (card == null || !card.isCorrect())
            return false;

        cards.add(card);
        return true;
    }

    @Override
    public Flashcard borrowNextCard() {
        if (lentCard != null)
            return null;

        lentCard = cards.pollFirst();
        return lentCard;
    }

    @Override
    public Flashcard putCardBack(boolean isPassed) {
        if (lentCard == null)
            return null;

        Flashcard activeCard = lentCard;
        lentCard = null;

        if (!isPassed) {
            cards.addLast(activeCard);
            return null;
        } else
            return activeCard;
    }
}
