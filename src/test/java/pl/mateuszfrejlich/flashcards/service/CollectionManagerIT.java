package pl.mateuszfrejlich.flashcards.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mateuszfrejlich.flashcards.FlashcardsApplication;
import pl.mateuszfrejlich.flashcards.controller.BoxController;
import pl.mateuszfrejlich.flashcards.controller.MainController;
import pl.mateuszfrejlich.flashcards.controller.OptionsController;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.service.CollectionsManager;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.util.CardState;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlashcardsApplication.class)
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@MockBeans({@MockBean(MainController.class), @MockBean(OptionsController.class), @MockBean(BoxController.class)})
class CollectionManagerIT {
    private static final String exampleName = "test_collection 1";
    private static final Flashcard card1 = new Flashcard("a", "b");
    @Autowired
    private CollectionsManager collectionsManager;

    @BeforeEach
    void setUp() {
        assertDoesNotThrow(() -> collectionsManager.setupDBConnection());
    }

    @AfterEach
    void cleanUP() {
        assertTrue(collectionsManager.getCollectionNames().allMatch(name -> collectionsManager.deleteCollection(name)));
    }

    @Test
    void addCollection_incorrectName_ignoreAction() {
        // given
        assertEquals(0, collectionsManager.getCollectionNames().count());
        // when
        assertFalse(collectionsManager.addNewCollection(""));
        assertFalse(collectionsManager.addNewCollection(" "));
        assertFalse(collectionsManager.addNewCollection("\n"));
        assertFalse(collectionsManager.addNewCollection("!"));
        assertFalse(collectionsManager.addNewCollection("''"));
        assertFalse(collectionsManager.addNewCollection("``"));
        assertFalse(collectionsManager.addNewCollection("?<:}{^&!%}"));
        // then
        assertEquals(0, collectionsManager.getCollectionNames().count());
        assertNull(collectionsManager.getCollection(exampleName));
    }

    @Test
    void addCollection_correctName_addProperly() {
        // given
        assertEquals(0, collectionsManager.getCollectionNames().count());
        // when
        assertTrue(collectionsManager.addNewCollection(exampleName));
        // then
        List<String> names = collectionsManager.getCollectionNames().toList();
        assertEquals(1, names.size());
        assertEquals(exampleName, names.get(0));
        assertNotNull(collectionsManager.getCollection(exampleName));
    }

    @Test
    void updateCollection_addOneCard_updateProperly() {
        // given
        collectionsManager.addNewCollection(exampleName);

        assertEquals(1, collectionsManager.getCollectionNames().count());
        assertNotNull(collectionsManager.getCollection(exampleName));
        assertEquals(0, collectionsManager.getCollection(exampleName).getPreparedCards().count());
        // when
        CardCollection collection = collectionsManager.getCollection(exampleName);
        assertTrue(collection.addNewCard(card1));
        assertTrue(collectionsManager.updateCardsCollection(collection));
        // then
        assertEquals(1, collectionsManager.getCollectionNames().count());
        CardCollection updatedCollection = collectionsManager.getCollection(exampleName);
        assertNotNull(updatedCollection);
        assertEquals(1, updatedCollection.getPreparedCards().count());
        assertEquals(card1, updatedCollection.getPreparedCards().findFirst().orElse(null));
    }

    @Test
    void updateCollection_updateUnregisteredCollection_ignoreAction() {
        // given
        CardCollection collection = new CardCollection("test", Stream.empty(), Stream.empty(), List.of(
                Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty(), Stream.empty()));

        assertEquals(0, collectionsManager.getCollectionNames().count());
        // when
        assertFalse(collectionsManager.updateCardsCollection(collection));
        // then
        assertEquals(0, collectionsManager.getCollectionNames().count());
        assertNull(collectionsManager.getCollection(collection.getName()));
    }

    @Test
    void deleteCollection_deleteUnregisteredCollection_ignoreAction() {
        // given
        String differentName = "different_name 0";
        collectionsManager.addNewCollection(exampleName);

        assertNotEquals(exampleName, differentName);
        assertEquals(1, collectionsManager.getCollectionNames().count());
        assertNull(collectionsManager.getCollection(differentName));
        // when
        assertFalse(collectionsManager.deleteCollection(differentName));
        // then
        assertEquals(1, collectionsManager.getCollectionNames().count());
        assertNull(collectionsManager.getCollection(differentName));
    }
}
