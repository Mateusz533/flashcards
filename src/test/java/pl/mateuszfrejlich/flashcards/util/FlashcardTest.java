package pl.mateuszfrejlich.flashcards.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlashcardTest {

    private static final String exampleCorrectWord = "test";

    private static boolean isSymmetricallyCorrect(String otherWord) {
        Flashcard card1 = new Flashcard(exampleCorrectWord, otherWord);
        Flashcard card2 = new Flashcard(otherWord, exampleCorrectWord);
        assertEquals(card1.isCorrect(), card2.isCorrect());

        return card1.isCorrect() && card2.isCorrect();
    }

    @Test
    void isCorrect() {
        assertTrue(isSymmetricallyCorrect(exampleCorrectWord));
        assertTrue(isSymmetricallyCorrect("a"));
        assertTrue(isSymmetricallyCorrect(" TeSt "));
        assertTrue(isSymmetricallyCorrect("\"a\""));
        assertTrue(isSymmetricallyCorrect("~!@#$%^&*()_+=-09887654321`\\|[]{};:<>,./?%"));
        assertTrue(isSymmetricallyCorrect("a".repeat(255)));

        assertFalse(isSymmetricallyCorrect("a".repeat(256)));
        assertFalse(isSymmetricallyCorrect("\n"));
        assertFalse(isSymmetricallyCorrect("     "));
        assertFalse(isSymmetricallyCorrect("'a'"));
        assertFalse(isSymmetricallyCorrect(""));
    }
}