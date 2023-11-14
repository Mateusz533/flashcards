package pl.mateuszfrejlich.flashcards;

import java.util.List;
import java.util.stream.Stream;

public class CardCollection {
    private final DataBaseAdapter dbAdapter;
    private final String name;
    private CardQueue preparedCards = null;
    private CardBox cardBox = null;
    private CardQueue archivedCards = null;
    private Flashcard activeCard = null;
    private CardGroupChoice cardGroupChoice = CardGroupChoice.UNSELECTED;

    public CardCollection(DataBaseAdapter dbAdapter, String name) {
        this.dbAdapter = dbAdapter;
        this.name = name;
        fetchData();
    }

    public String getName() {
        return name;
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

    public List<Integer> boxSectionsFilling() {
        return (cardBox != null) ? cardBox.sectionsFilling() : List.of(0, 0, 0, 0, 0);
    }

    public int numberOfArchivedCards() {
        return (archivedCards != null) ? archivedCards.size() : 0;
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

    public void processNextCard() {
        switch (cardGroupChoice) {
            case PREPARED -> activeCard = preparedCards.popNextCard();
            case INBOX -> activeCard = cardBox.popNextCard();
            case ARCHIVED -> activeCard = archivedCards.popNextCard();
            case UNSELECTED -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardGroupChoice);
        }
    }

    public void saveChanges() {
        dbAdapter.updatePreparedCardsCollection(name, preparedCards.getCards());
        dbAdapter.updateArchivedCardsCollection(name, archivedCards.getCards());
        dbAdapter.updateInboxCardsCollection(name, cardBox.getSections());
    }

    private void fetchData() {
        Stream<Flashcard> preparedCards = dbAdapter.getPreparedCards(name);
        Stream<Flashcard> archivedCards = dbAdapter.getArchivedCards(name);
        List<Stream<Flashcard>> cardBoxSections = dbAdapter.getCardBoxSections(name);

        if (preparedCards == null || archivedCards == null || cardBoxSections == null) {
            System.out.println("Fetching data error!");
            return;
        }

        this.preparedCards = new CardQueue(preparedCards);
        this.archivedCards = new CardQueue(archivedCards);
        cardBox = new CardBox(this.archivedCards, cardBoxSections);
    }
}
