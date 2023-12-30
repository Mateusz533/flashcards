package pl.mateuszfrejlich.flashcards.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pl.mateuszfrejlich.flashcards.dao.DataBaseAdapter;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

@Service
public class CollectionsManager {
    @Autowired
    @Qualifier("jpa")
    private DataBaseAdapter dbAdapter;

    public void setupDBConnection() throws SQLException {
        dbAdapter.openConnection();
    }

    public Stream<String> getCollectionNames() {
        return dbAdapter.getCollectionNames();
    }

    public CardCollection getCollection(String name) {
        Stream<Flashcard> preparedCards = dbAdapter.getPreparedCards(name);
        Stream<Flashcard> archivedCards = dbAdapter.getArchivedCards(name);
        List<Stream<Flashcard>> cardBoxSections = dbAdapter.getCardBoxSections(name);

        if (preparedCards == null || archivedCards == null || cardBoxSections == null)
            return null;

        return new CardCollection(name, preparedCards, archivedCards, cardBoxSections);
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

        Stream<Flashcard> stream = !list.isEmpty() ? list.stream() : null;

        return dbAdapter.createNewCollection(name, stream);
    }

    public boolean updateCardsCollection(CardCollection cardCollection) {
        String name = cardCollection.getName();
        final boolean updatedPrep = dbAdapter.updatePreparedCardsCollection(name, cardCollection.getPreparedCards());
        final boolean updatedArch = dbAdapter.updateArchivedCardsCollection(name, cardCollection.getArchivedCards());
        final boolean updatedInbox = dbAdapter.updateInboxCardsCollection(name, cardCollection.getCardBoxSections());

        return updatedPrep && updatedArch && updatedInbox;
    }

    public boolean deleteCollection(String collectionName) {
        return dbAdapter.deleteCollection(collectionName);
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
        } catch (Exception e) {
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
