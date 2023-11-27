package pl.mateuszfrejlich.flashcards.util;

public interface CardGroup {

    Flashcard getBorrowedCard();

    boolean addNewCard(Flashcard card);

    /**
     * Only one card can be borrowed at one time
     *
     * @return null if borrowing is forbidden in current group state
     */
    Flashcard borrowNextCard();

    /**
     * Unless card is already borrowed, do nothing
     *
     * @param isPassed true if borrowed card go to the next level of processing
     * @return a card leaving the group in this step, or null if all of them stay
     */
    Flashcard putCardBack(boolean isPassed);
}
