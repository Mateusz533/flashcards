package pl.mateuszfrejlich.flashcards;

public class Flashcard {
    private final String frontText;
    private final String reverseText;

    public Flashcard(String frontText, String reverseText) {
        this.frontText = frontText;
        this.reverseText = reverseText;
    }

    public String getFrontText() {
        return frontText;
    }

    public String getReverseText() {
        return reverseText;
    }

    public boolean isCorrect() {
        return !(frontText.isBlank() || reverseText.isBlank());
    }
}
