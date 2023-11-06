package pl.mateuszfrejlich.flashcards;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardQueue {
    private final Deque<Flashcard> cards;
    private Flashcard lentCard = null;

    CardQueue(Stream<Flashcard> cards) {
        this.cards = cards.collect(Collectors.toCollection(ArrayDeque::new));
    }

    int size() {
        return cards.size();
    }

    public Stream<Flashcard> getCards() {
        return cards.stream();
    }

    public void addCard(Flashcard card) {
        cards.add(card);
    }

    public Flashcard popNextCard() {
        lentCard = cards.pollFirst();
        return lentCard;
    }

    public void putBorrowedCard(boolean isPreserved) {
        if (lentCard == null)
            return;

        if (isPreserved)
            cards.addLast(lentCard);

        lentCard = null;
    }
}
