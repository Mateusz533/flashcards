package pl.mateuszfrejlich.flashcards;

import java.util.ArrayList;
import java.util.Iterator;

public class Controller {
    private String selectedSchema = null;

    public boolean addNewCollection(String name) {
        return addNewCollection(name, null);
    }

    public boolean addNewCollection(String name, String path) {
        // TODO: validate name and path
        if (name.isBlank() || (path != null && path.isBlank()))
            return false;

        // TODO: create new schema in database
        final boolean created = true;
        if (!created)
            return false;

        return true;
    }

    public void deleteSchema() {
        System.out.println("Delete schema");
        // TODO: delete selected schema from database and from combo-box
    }

    public void putCachedData() {
        // TODO: send cached data to database
    }

    public boolean addToCache(Flashcard card) {
        // TODO: put data to cache
        if (!card.isCorrect())
            return false;

        return true;
    }

    public boolean updateCache(int index, Flashcard card) {
        // TODO: put data to cache
        if (!card.isCorrect())
            return false;

        return true;
    }

    public void deleteFromCache(int index) {
        // TODO: remove data from cache
    }

    public void clearCache() {
        // TODO: clear cache
    }

    public Iterator<Flashcard> getItems(String collectionName) {
        // TODO: get items from cache instead
        ArrayList<Flashcard> arrayList = new ArrayList<>();
        arrayList.add(new Flashcard("<front text>", "<reverse text>"));
        return arrayList.iterator();
    }
}
