package pl.mateuszfrejlich.flashcards.dao;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Repository;
import pl.mateuszfrejlich.flashcards.entity.CardCollectionEntity;
import pl.mateuszfrejlich.flashcards.entity.FlashcardEntity;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Repository("jpa")
public class JpaAdapter implements DataBaseAdapter {
    @Autowired
    private FlashcardsRepository flashcardsRepository;

    @Override
    public void openConnection() throws SQLException {
        try {
            flashcardsRepository.flush();
        } catch (Exception e) {
            throw new SQLException("Failed to connect with database!");
        }
    }

    @Override
    public Stream<String> getCollectionNames() {
        return flashcardsRepository.findAll().stream().map(CardCollectionEntity::getName);
    }

    @Override
    public Stream<Flashcard> getPreparedCards(String collectionName) {
        return getCards(collectionName, c -> Stream.of(c.getPre())).findAny().orElse(null);
    }

    @Override
    public Stream<Flashcard> getArchivedCards(String collectionName) {
        return getCards(collectionName, c -> Stream.of(c.getArch())).findAny().orElse(null);
    }

    @Override
    public List<Stream<Flashcard>> getCardBoxSections(String collectionName) {
        return getCards(collectionName,
                c -> Stream.of(c.getSec1(), c.getSec2(), c.getSec3(), c.getSec4(), c.getSec5())
        ).toList();
    }

    @Override
    public boolean updatePreparedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        return updateCardsCollection(selectedCollectionName, List.of(stream), c -> List.of(c.getPre()));
    }

    @Override
    public boolean updateArchivedCardsCollection(String selectedCollectionName, Stream<Flashcard> stream) {
        return updateCardsCollection(selectedCollectionName, List.of(stream), c -> List.of(c.getArch()));
    }

    @Override
    public boolean updateInboxCardsCollection(String selectedCollectionName, List<Stream<Flashcard>> sections) {
        return updateCardsCollection(selectedCollectionName, sections,
                c -> List.of(c.getSec1(), c.getSec2(), c.getSec3(), c.getSec4(), c.getSec5()));
    }

    @Override
    public boolean createNewCollection(String name, Stream<Flashcard> initialData) {
        try {
            CardCollectionEntity collection = new CardCollectionEntity(name);
            if (initialData != null)
                collection.getPre().addAll(initialData.map(FlashcardEntity::new).toList());
            flashcardsRepository.saveAndFlush(collection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteCollection(String name) {
        try {
            flashcardsRepository.deleteById(name);
            flashcardsRepository.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    private Stream<Stream<Flashcard>> getCards(String name, Function<CardCollectionEntity,
            Stream<List<FlashcardEntity>>> getter) {
        try {
            CardCollectionEntity collection = flashcardsRepository.findById(name).orElseThrow();
            return getter.apply(collection).map(sec -> sec.stream().map(FlashcardEntity::toFlashcard));
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    private boolean updateCardsCollection(String name, List<Stream<Flashcard>> data,
                                          Function<CardCollectionEntity, List<List<FlashcardEntity>>> getter) {
        try {
            CardCollectionEntity collection = flashcardsRepository.findById(name).orElseThrow();
            List<List<FlashcardEntity>> objects = getter.apply(collection);
            if (objects.size() != data.size())
                return false;

            StreamUtils.zip(objects.stream(), data.stream(), Pair::of).forEach(p -> {
                p.getFirst().clear();
                p.getFirst().addAll(p.getSecond().map(FlashcardEntity::new).toList());
            });
            flashcardsRepository.saveAndFlush(collection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
