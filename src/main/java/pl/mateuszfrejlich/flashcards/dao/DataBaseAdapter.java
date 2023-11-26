package pl.mateuszfrejlich.flashcards.dao;

import org.springframework.stereotype.Repository;
import pl.mateuszfrejlich.flashcards.model.Flashcard;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface DataBaseAdapter {

    void openConnection() throws SQLException;

    Stream<String> getCollectionNames();

    Stream<Flashcard> getPreparedCards(String collectionName);

    Stream<Flashcard> getArchivedCards(String collectionName);

    List<Stream<Flashcard>> getCardBoxSections(String collectionName);

    boolean updatePreparedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream);

    boolean updateArchivedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream);

    boolean updateInboxCardsCollection(String selectedCollectionName, List<Stream<Flashcard>> sections);

    boolean createNewCollection(String name, Stream<Flashcard> initialData);

    boolean deleteCollection(String name);
}
