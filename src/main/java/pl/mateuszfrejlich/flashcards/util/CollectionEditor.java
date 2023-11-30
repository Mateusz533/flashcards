package pl.mateuszfrejlich.flashcards.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public boolean addCard(Flashcard newCard) {
        if (!newCard.isCorrect())
            return false;

        if (items.stream().anyMatch(card -> card.frontText().equals(newCard.frontText())))
            return false;

        items.add(newCard);
        return true;
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

    public void shuffleCards() {
        Collections.shuffle(items);
    }

    public void sortCards(ComparatorKey key) {
        switch (key) {
            case FRONT_TEXT -> items.sort(Comparator.comparing(Flashcard::frontText));
            case REVERSE_TEXT -> items.sort(Comparator.comparing(Flashcard::reverseText));
        }
    }

    public enum ComparatorKey {
        FRONT_TEXT,
        REVERSE_TEXT,
    }
}
