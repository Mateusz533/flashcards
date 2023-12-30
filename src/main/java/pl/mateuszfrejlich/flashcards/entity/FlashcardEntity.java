package pl.mateuszfrejlich.flashcards.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString
public class FlashcardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String frontText;
    private String reverseText;

    public FlashcardEntity(Flashcard flashcard) {
        this.frontText = flashcard.frontText();
        this.reverseText = flashcard.reverseText();
    }

    public Flashcard toFlashcard() {
        return new Flashcard(frontText, reverseText);
    }
}
