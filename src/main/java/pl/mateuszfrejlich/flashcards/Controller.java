package pl.mateuszfrejlich.flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class Controller {
    private DataBaseAdapter dbAdapter = new DataBaseAdapter();
    private String selectedCollectionName = null;
    private CardQueue preparedCards = null;
    private CardBox cardBox = null;
    private CardQueue archivedCards = null;
    private Flashcard activeCard = null;
    private CardsChoice cardsChoice = CardsChoice.UNSELECTED;

    public int preparedCardsNumber() {
        return (preparedCards != null) ? preparedCards.size() : 0;
    }

    public int archivedCardsNumber() {
        return (archivedCards != null) ? archivedCards.size() : 0;
    }

    public Flashcard getActiveCard() {
        return activeCard;
    }

    public CardsChoice getCardChoice() {
        return cardsChoice;
    }

    public void setCardsChoice(CardsChoice cardsChoice) {
        if (selectedCollectionName != null)
            this.cardsChoice = cardsChoice;
    }

    public boolean addNewCollection(String name) {
        return addNewCollection(name, null);
    }

    public boolean addNewCollection(String name, String path) {
        if (!isValidName(name)) return false;

        ArrayList<Flashcard> list = new ArrayList<>();

        if (path != null) {
            final boolean isValidFile = getDataFromFile(path, list);
            if (!isValidFile)
                return false;
        }

        final boolean isCreated = dbAdapter.createNewSchema(name, list.stream());
        return isCreated;
    }

    public void selectCollection(String name) {
        selectedCollectionName = name;
        fetchData();
    }

    public boolean deleteCollection() {
        if (!dbAdapter.deleteSchema(selectedCollectionName))
            return false;

        selectedCollectionName = null;
        preparedCards = null;
        cardBox = null;
        archivedCards = null;
        activeCard = null;
        cardsChoice = CardsChoice.UNSELECTED;
        return true;
    }

    public CardCache createCache() {
        return new CardCache(preparedCards.getCards());
    }

    public void putCachedData(CardCache cache) {
        dbAdapter.updateSchema(selectedCollectionName, cache.getItems());
        fetchData();
    }

    public Stream<String> getCollectionNames() {
        return dbAdapter.getSchemaNames();
    }

    public void putBorrowedCard(boolean isPassed) {
        switch (cardsChoice) {
            case PREPARED -> {
                final boolean isRetrieved = !isPassed || !cardBox.addNewCard(activeCard);
                preparedCards.putBorrowedCard(isRetrieved);
                if (!isRetrieved)
                    dbAdapter.updateSchema(selectedCollectionName, preparedCards.getCards());
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

    private void fetchData() {
        try {
            preparedCards = new CardQueue(dbAdapter.getPreparedCards(selectedCollectionName));
            archivedCards = new CardQueue(dbAdapter.getArchivedCards(selectedCollectionName));
            cardBox = new CardBox(archivedCards, dbAdapter.getCardBox(selectedCollectionName));
        } catch (Exception e) {
            System.out.println("Fetching data error: " + e.getMessage());
        }
    }

    private boolean getDataFromFile(String path, ArrayList<Flashcard> list) {
        try {
            File file = new File(path);
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] sentences = line.trim().split("\\|");
                Flashcard nextCard = new Flashcard(sentences[0].trim(), sentences[1].trim());
                if (!nextCard.isCorrect())
                    throw new Exception();
                else
                    list.add(nextCard);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            return false;
        } catch (Exception e) {
            System.out.println("Invalid file data!");
            return false;
        }

        return true;
    }

    private boolean isValidName(String name) {
        if (name.isBlank())
            return false;

        return name.chars().allMatch((int c) -> isValidChar((char) c));
    }

    private boolean isValidChar(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_' || c == ' ';
    }
}
