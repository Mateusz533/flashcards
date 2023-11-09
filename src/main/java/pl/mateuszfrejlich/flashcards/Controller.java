package pl.mateuszfrejlich.flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Controller {
    private final DataBaseAdapter dbAdapter;
    private String selectedCollectionName = null;
    private CardQueue preparedCards = null;
    private CardBox cardBox = null;
    private CardQueue archivedCards = null;
    private Flashcard activeCard = null;
    private CardGroupChoice cardGroupChoice = CardGroupChoice.UNSELECTED;

    public Controller() throws SQLException {
        this.dbAdapter = new DataBaseAdapter();
    }

    public int preparedCardsNumber() {
        return (preparedCards != null) ? preparedCards.size() : 0;
    }

    public int archivedCardsNumber() {
        return (archivedCards != null) ? archivedCards.size() : 0;
    }

    public Flashcard getActiveCard() {
        return activeCard;
    }

    public CardGroupChoice getCardGroupChoice() {
        return cardGroupChoice;
    }

    public void setCardGroupChoice(CardGroupChoice cardGroupChoice) {
        if (selectedCollectionName != null)
            this.cardGroupChoice = cardGroupChoice;
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

        return dbAdapter.createNewCollection(name, list.stream());
    }

    public void selectCollection(String name) {
        selectedCollectionName = name;
        fetchData();
    }

    public boolean deleteCollection() {
        if (!dbAdapter.deleteCollection(selectedCollectionName))
            return false;

        selectedCollectionName = null;
        preparedCards = null;
        cardBox = null;
        archivedCards = null;
        activeCard = null;
        cardGroupChoice = CardGroupChoice.UNSELECTED;
        return true;
    }

    public CollectionEditor createEditor() {
        return new CollectionEditor(preparedCards.getCards());
    }

    public void executeEdition(CollectionEditor editor) {
        Stream<Flashcard> cachedCards = editor.getCards();
        if (cachedCards != null)
            preparedCards = new CardQueue(cachedCards);
    }

    public Stream<String> getCollectionNames() {
        return dbAdapter.getCollectionNames();
    }

    public void putBorrowedCard(boolean isPassed) {
        switch (cardGroupChoice) {
            case PREPARED -> {
                final boolean isRetrieved = !isPassed || !cardBox.addNewCard(activeCard);
                preparedCards.putBorrowedCard(isRetrieved);
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
        if (selectedCollectionName == null)
            return;

        dbAdapter.updatePreparedCardsCollection(selectedCollectionName, preparedCards.getCards());
        dbAdapter.updateArchivedCardsCollection(selectedCollectionName, archivedCards.getCards());
        dbAdapter.updateInboxCardsCollection(selectedCollectionName, cardBox.getSections());
    }

    private void fetchData() {
        Stream<Flashcard> preparedCards = dbAdapter.getPreparedCards(selectedCollectionName);
        Stream<Flashcard> archivedCards = dbAdapter.getArchivedCards(selectedCollectionName);
        List<Stream<Flashcard>> cardBoxSections = dbAdapter.getCardBoxSections(selectedCollectionName);

        if (preparedCards == null || archivedCards == null || cardBoxSections == null) {
            System.out.println("Fetching data error!");
            return;
        }

        this.preparedCards = new CardQueue(preparedCards);
        this.archivedCards = new CardQueue(archivedCards);
        cardBox = new CardBox(this.archivedCards, cardBoxSections);
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
