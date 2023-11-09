package pl.mateuszfrejlich.flashcards;

public record Flashcard(String frontText, String reverseText) {

    public boolean isCorrect() {
        return !(frontText.isBlank() || reverseText.isBlank());
    }
}
