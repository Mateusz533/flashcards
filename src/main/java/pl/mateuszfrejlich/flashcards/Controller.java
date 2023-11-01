package pl.mateuszfrejlich.flashcards;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Controller {
    private String selectedCollectionName;
    private DataBaseAdapter dbAdapter = new DataBaseAdapter();
    private List<Flashcard> activeCollection;

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

    public void putCachedData() {
        dbAdapter.updateSchema(selectedCollectionName, activeCollection.stream());
    }

    public boolean addToCache(Flashcard card) {
        if (card.isCorrect()) {
            activeCollection.add(card);
            return true;
        } else
            return false;
    }

    public boolean updateCache(int index, Flashcard card) {
        if (card.isCorrect()) {
            activeCollection.set(index, card);
            return true;
        } else
            return false;
    }

    public void deleteFromCache(int index) {
        activeCollection.remove(index);
    }

    public void clearCache() {
        activeCollection.clear();
        fetchData();
    }

    public Stream<Flashcard> getItems() {
        return activeCollection.stream();
    }

    public Stream<String> getCollectionNames(){
        return dbAdapter.getSchemaNames();
    }
    private void fetchData() {
        try {
            Stream<Flashcard> stream = dbAdapter.getDataFromSchema(selectedCollectionName);
            activeCollection = stream.collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Fetching data error: " + e.getMessage());
        }
    }
}
