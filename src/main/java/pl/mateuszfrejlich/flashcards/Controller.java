package pl.mateuszfrejlich.flashcards;

public class Controller {
    private String selectedSchema = null;

    public boolean createSchema(String name, String path) {
        // TODO: validate name and path
        // TODO: create new schema in database
        return true;
    }

    public void deleteSchema() {
        System.out.println("Delete schema");
        // TODO: delete selected schema from database and from combobox
    }

    public void putCachedData() {
        // TODO: send cached data to database
    }

    public boolean cacheData(String front_side, String reversed_side) {
        // TODO: put data to cache
        return true;
    }
}
