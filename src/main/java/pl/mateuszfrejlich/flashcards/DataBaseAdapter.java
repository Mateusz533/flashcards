package pl.mateuszfrejlich.flashcards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataBaseAdapter {
    private Map<String, List<Flashcard>> mockDB = new HashMap<>();

    public boolean createNewSchema(String name) {
        // TODO: create new schema in database
        mockDB.put(name, new ArrayList<>());
        return true;
    }

    public boolean deleteSchema(String name) {
        // name == null ?
        mockDB.remove(name);
        // TODO: delete selected schema from database
        return true;
    }

    public Stream<Flashcard> getDataFromSchema(String name) {
        // TODO: get data from schema
        return mockDB.get(name).stream();
    }

    public void updateSchema(String selectedCollectionName, Stream<Flashcard> stream) {
        // TODO: convert data and update database
        mockDB.replace(selectedCollectionName, stream.collect(Collectors.toList()));
    }

    public Stream<String> getSchemaNames() {
        return mockDB.keySet().stream();
    }
}
