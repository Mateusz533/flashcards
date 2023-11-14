package pl.mateuszfrejlich.flashcards;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class CollectionsManager {
    private final DataBaseAdapter dbAdapter;

    public CollectionsManager() throws SQLException {
        this.dbAdapter = new DataBaseAdapter();
    }

    public Stream<String> getCollectionNames() {
        return dbAdapter.getCollectionNames();
    }

    public CardCollection getCollection(String name) {
        return new CardCollection(dbAdapter, name);
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

    public boolean deleteCollection(String collectionName) {
        if (!dbAdapter.deleteCollection(collectionName))
            return false;

        return true;
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
