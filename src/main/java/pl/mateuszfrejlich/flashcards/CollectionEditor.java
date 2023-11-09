package pl.mateuszfrejlich.flashcards;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionEditor {
    private final List<Flashcard> items;

    public CollectionEditor(Stream<Flashcard> items) {
        this.items = items.collect(Collectors.toCollection(ArrayList::new));
    }

    public Stream<Flashcard> getCards() {
        return items == null ? null : items.stream();
    }

    public Flashcard getCard(int index) {
        return items.get(index);
    }

    public boolean addCard(Flashcard card) {
        if (card.isCorrect()) {
            items.add(card);
            return true;
        } else
            return false;
    }

    public boolean updateCard(int index, Flashcard card) {
        if (card.isCorrect()) {
            items.set(index, card);
            return true;
        } else
            return false;
    }

    public void deleteCard(int index) {
        items.remove(index);
    }
}
