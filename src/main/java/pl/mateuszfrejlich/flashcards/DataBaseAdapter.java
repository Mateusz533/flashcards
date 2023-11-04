package pl.mateuszfrejlich.flashcards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataBaseAdapter {
    private final Map<String, List<Flashcard>> mockDB = new HashMap<>();

    public Stream<String> getSchemaNames() {
        return mockDB.keySet().stream();
    }

    public boolean createNewSchema(String name) {
        // TODO: create new schema in database
        mockDB.put(name, new ArrayList<>());
        return true;
    }

    public void updateSchema(String selectedCollectionName, Stream<Flashcard> stream) {
        // TODO: convert data and update database
        mockDB.replace(selectedCollectionName, stream.collect(Collectors.toList()));
    }

    public boolean deleteSchema(String name) {
        // name == null ?
        mockDB.remove(name);
        // TODO: delete selected schema from database
        return true;
    }

    public Stream<Flashcard> getPreparedCards(String schemaName) {
        // TODO: get data from schema
        return mockDB.get(schemaName).stream();
    }

    public Stream<Flashcard> getArchivedCards(String schemaName) {
        // TODO: get data from schema
        return new ArrayList<Flashcard>(0).stream();
    }

    public List<Stream<Flashcard>> getCardBox(String schemaName) {
        // TODO: get data from schema
        ArrayList<Stream<Flashcard>> streams = new ArrayList<>(5);
        for (int i = 0; i < 5; ++i)
            streams.add(new ArrayList<Flashcard>().stream());

        return streams;
    }
}
