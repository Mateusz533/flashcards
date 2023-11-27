package pl.mateuszfrejlich.flashcards.util;

public record Flashcard(String frontText, String reverseText) {
    public int maxNumOfChars() {
        return 255;
    }

    public boolean isCorrect() {
        final boolean isBlank = (frontText.isBlank() || reverseText.isBlank());
        final boolean isOverloaded = (frontText.length() > maxNumOfChars() || reverseText.length() > maxNumOfChars());

        return !isBlank && !isOverloaded;
    }
}
