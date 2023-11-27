package pl.mateuszfrejlich.flashcards.util;

import java.util.List;
import java.util.stream.Stream;

public class CardCollection implements CardGroup {
    private final String name;
    private final CardBox cardBox;
    private final CardQueue archivedCards;
    private CardQueue preparedCards;
    private CardGroupChoice cardGroupChoice = CardGroupChoice.UNSELECTED;

    public CardCollection(String name,
                          Stream<Flashcard> preparedCards,
                          Stream<Flashcard> archivedCards,
                          List<Stream<Flashcard>> cardBoxSections) {
        this.name = name;
        this.preparedCards = new CardQueue(preparedCards);
        this.archivedCards = new CardQueue(archivedCards);
        cardBox = new CardBox(cardBoxSections);
    }

    public String getName() {
        return name;
    }

    public List<Stream<Flashcard>> getCardBoxSections() {
        return cardBox.getSections();
    }

    public Stream<Flashcard> getArchivedCards() {
        return archivedCards.getCards();
    }

    public Stream<Flashcard> getPreparedCards() {
        return preparedCards.getCards();
    }

    public boolean setCardGroupChoice(CardGroupChoice cardGroupChoice) {
        final boolean noLentCard = getBorrowedCard() == null;
        if (noLentCard)
            this.cardGroupChoice = cardGroupChoice;

        return noLentCard;
    }

    public CardGroupChoice getCardGroupChoice() {
        return cardGroupChoice;
    }

    public int numberOfPreparedCards() {
        return (preparedCards != null) ? preparedCards.size() : 0;
    }

    public int numberOfArchivedCards() {
        return archivedCards.size();
    }

    public List<Integer> boxSectionsFilling() {
        return cardBox.sectionsFilling();
    }

    public CollectionEditor createEditor() {
        return new CollectionEditor(preparedCards.getCards());
    }

    public void executeEdition(CollectionEditor editor) {
        Stream<Flashcard> cachedCards = editor.getCards();
        if (cachedCards != null)
            preparedCards = new CardQueue(cachedCards);
    }

    @Override
    public Flashcard getBorrowedCard() {
        CardGroup activeGroup = activeGroup();
        return activeGroup != null ? activeGroup.getBorrowedCard() : null;
    }

    @Override
    public boolean addNewCard(Flashcard card) {
        return getBorrowedCard() == null && preparedCards.addNewCard(card);
    }

    @Override
    public Flashcard borrowNextCard() {
        CardGroup activeGroup = activeGroup();
        return activeGroup != null ? activeGroup.borrowNextCard() : null;
    }

    @Override
    public Flashcard putCardBack(boolean isPassed) {
        CardGroup activeGroup = activeGroup();
        if (activeGroup == null || activeGroup.getBorrowedCard() == null)
            return null;

        Flashcard returnedCard = activeGroup.putCardBack(isPassed);
        CardGroup followingGroup = followingGroup();
        if (returnedCard == null || followingGroup == null)
            return null;

        if (followingGroup.addNewCard(returnedCard))
            return null;

        if (activeGroup.addNewCard(returnedCard))
            return null;

        return returnedCard;
    }

    private CardGroup activeGroup() {
        return switch (cardGroupChoice) {
            case PREPARED -> preparedCards;
            case INBOX -> cardBox;
            case ARCHIVED -> archivedCards;
            case UNSELECTED -> null;
        };
    }

    private CardGroup followingGroup() {
        return switch (cardGroupChoice) {
            case PREPARED -> cardBox;
            case INBOX -> archivedCards;
            case ARCHIVED, UNSELECTED -> null;
        };
    }
}
