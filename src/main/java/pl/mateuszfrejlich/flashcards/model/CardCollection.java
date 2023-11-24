package pl.mateuszfrejlich.flashcards.model;

import java.util.List;
import java.util.stream.Stream;

public class CardCollection {
    private final String name;
    private final CardBox cardBox;
    private final CardQueue archivedCards;
    private CardQueue preparedCards;
    private Flashcard activeCard = null;
    private CardGroupChoice cardGroupChoice = CardGroupChoice.UNSELECTED;

    public CardCollection(String name,
                          Stream<Flashcard> preparedCards,
                          Stream<Flashcard> archivedCards,
                          List<Stream<Flashcard>> cardBoxSections) {
        this.name = name;
        this.preparedCards = new CardQueue(preparedCards);
        this.archivedCards = new CardQueue(archivedCards);
        cardBox = new CardBox(this.archivedCards, cardBoxSections);
    }

    public String getName() {
        return name;
    }

    public List<Stream<Flashcard>> getCardBoxSections() {
        return cardBox.getSections();
    }

    public Stream<Flashcard> getArchivedCards() {
        return archivedCards.getCards();
    }

    public Stream<Flashcard> getPreparedCards() {
        return preparedCards.getCards();
    }

    public Flashcard getActiveCard() {
        return activeCard;
    }

    public CardGroupChoice getCardGroupChoice() {
        return cardGroupChoice;
    }

    public void setCardGroupChoice(CardGroupChoice cardGroupChoice) {
        this.cardGroupChoice = cardGroupChoice;
    }

    public int numberOfPreparedCards() {
        return (preparedCards != null) ? preparedCards.size() : 0;
    }

    public int numberOfArchivedCards() {
        return archivedCards.size();
    }

    public List<Integer> boxSectionsFilling() {
        return cardBox.sectionsFilling();
    }

    public CollectionEditor createEditor() {
        return new CollectionEditor(preparedCards.getCards());
    }

    public void executeEdition(CollectionEditor editor) {
        Stream<Flashcard> cachedCards = editor.getCards();
        if (cachedCards != null)
            preparedCards = new CardQueue(cachedCards);
    }

    public void putBorrowedCard(boolean isPassed) {
        switch (cardGroupChoice) {
            case PREPARED -> {
                final boolean isPreserved = !isPassed || !cardBox.addNewCard(activeCard);
                preparedCards.putBorrowedCard(isPreserved);
            }
            case INBOX -> cardBox.putBorrowedCard(isPassed);
            case ARCHIVED -> archivedCards.putBorrowedCard(isPassed);
            case UNSELECTED -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardGroupChoice);
        }
        activeCard = null;
    }

    public boolean processNextCard() {
        switch (cardGroupChoice) {
            case PREPARED -> activeCard = preparedCards.popNextCard();
            case INBOX -> activeCard = cardBox.popNextCard();
            case ARCHIVED -> activeCard = archivedCards.popNextCard();
            case UNSELECTED -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardGroupChoice);
        }
        return activeCard != null;
    }
}
