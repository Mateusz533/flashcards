package pl.mateuszfrejlich.flashcards.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardQueue extends CardGroup {
    private final Deque<Flashcard> cards;

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
    public boolean addNewCard(Flashcard card) {
        if (!card.isCorrect())
            return false;

        cards.add(card);
        return true;
    }

    @Override
    public Flashcard popNextCard() {
        if (lentCard != null)
            return null;

        lentCard = cards.pollFirst();
        return lentCard;
    }

    @Override
    public void putBorrowedCard(boolean isPreserved) {
        if (lentCard == null)
            return;

        if (isPreserved)
            cards.addLast(lentCard);

        lentCard = null;
    }
}
