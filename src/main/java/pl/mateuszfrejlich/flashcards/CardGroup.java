package pl.mateuszfrejlich.flashcards;

public abstract class CardGroup {
    protected Flashcard lentCard = null;

    public abstract boolean addNewCard(Flashcard card);

    public abstract Flashcard popNextCard();

    public abstract void putBorrowedCard(boolean isPreserved);
}
