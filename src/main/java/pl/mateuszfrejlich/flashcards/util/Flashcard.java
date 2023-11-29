package pl.mateuszfrejlich.flashcards.util;

public record Flashcard(String frontText, String reverseText) {
    public static int maxNumOfChars() {
        return 255;
    }

    public boolean isCorrect() {
        final boolean isBlank = (frontText.isBlank() || reverseText.isBlank());
        final boolean isOverloaded = (frontText.length() > maxNumOfChars() || reverseText.length() > maxNumOfChars());
        final boolean hasInvalidChars = (frontText.contains("'") || reverseText.contains("'"));

        return !isBlank && !isOverloaded && !hasInvalidChars;
    }
}
