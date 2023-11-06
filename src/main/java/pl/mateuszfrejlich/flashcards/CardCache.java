package pl.mateuszfrejlich.flashcards;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CardCache {
    private final List<Flashcard> items;

    public CardCache(Stream<Flashcard> items) {
        this.items = items.collect(Collectors.toCollection(ArrayList::new));
    }

    public Stream<Flashcard> getItems() {
        return items == null ? null : items.stream();
    }

    public Flashcard getItem(int index) {
        return items.get(index);
    }

    public boolean addItem(Flashcard card) {
        if (card.isCorrect()) {
            items.add(card);
            return true;
        } else
            return false;
    }

    public boolean updateItem(int index, Flashcard card) {
        if (card.isCorrect()) {
            items.set(index, card);
            return true;
        } else
            return false;
    }

    public void deleteItem(int index) {
        items.remove(index);
    }
}
