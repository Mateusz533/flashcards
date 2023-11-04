package pl.mateuszfrejlich.flashcards;

import java.util.stream.Stream;

public class Controller {
    private DataBaseAdapter dbAdapter = new DataBaseAdapter();
    private String selectedCollectionName = null;
    private CardQueue preparedCards = null;
    private CardBox cardBox = null;
    private CardQueue archivedCards = null;
    private Flashcard activeCard = null;
    private CardsChoice cardsChoice = CardsChoice.UNSELECTED;

    public Flashcard getActiveCard() {
        return activeCard;
    }

    public CardsChoice getCardChoice() {
        return cardsChoice;
    }

    public void setCardsChoice(CardsChoice cardsChoice) {
        this.cardsChoice = cardsChoice;
    }

    public boolean addNewCollection(String name) {
        return addNewCollection(name, null);
    }

    public boolean addNewCollection(String name, String path) {
        if (name.isBlank())
            return false;

        final boolean validName = name.chars().allMatch((int c) -> isValidChar((char) c));
        if (!validName)
            return false;

        if (path != null && path.isBlank())
            return false;

        // TODO: open and validate file

        final boolean created = dbAdapter.createNewSchema(name);
        if (!created)
            return false;

        // TODO: conditionally add data from file

        return true;
    }

    private boolean isValidChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == ' ';
    }

    public void selectCollection(String name) {
        selectedCollectionName = name;
        fetchData();
    }

    public boolean deleteCollection() {
        return deleteCollection(selectedCollectionName);
    }

    public boolean deleteCollection(String name) {
        return dbAdapter.deleteSchema(name);
    }

    public CardCache createCache() {
        return new CardCache(preparedCards.getCards().stream());
    }

    public void putCachedData(CardCache cache) {
        dbAdapter.updateSchema(selectedCollectionName, cache.getItems());
        fetchData();
    }

    public Stream<String> getCollectionNames() {
        return dbAdapter.getSchemaNames();
    }

    private void fetchData() {
        try {
            preparedCards = new CardQueue(dbAdapter.getPreparedCards(selectedCollectionName));
            archivedCards = new CardQueue(dbAdapter.getArchivedCards(selectedCollectionName));
            cardBox = new CardBox(archivedCards, dbAdapter.getCardBox(selectedCollectionName));
        } catch (Exception e) {
            System.out.println("Fetching data error: " + e.getMessage());
        }
    }

    public void putBorrowedCard(boolean isPassed) {
        switch (cardsChoice) {
            case PREPARED -> {
                final boolean success = isPassed ? cardBox.addNewCard(activeCard) : false;
                preparedCards.putBorrowedCard(!success);
            }
            case ARCHIVED -> archivedCards.putBorrowedCard(isPassed);
            case INBOX -> cardBox.putBorrowedCard(isPassed);
            case UNSELECTED -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardsChoice);
        }
        activeCard = null;
    }

    public void processNextCard() {
        switch (cardsChoice) {
            case PREPARED -> activeCard = preparedCards.popNextCard();
            case ARCHIVED -> activeCard = archivedCards.popNextCard();
            case INBOX -> activeCard = cardBox.popNextCard();
            case UNSELECTED -> {
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardsChoice);
        }
    }
}
