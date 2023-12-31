package pl.mateuszfrejlich.flashcards.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.util.CardState;

@Component
@RequiredArgsConstructor
public class SessionState {
    private final ActiveCollectionChangeEventPublisher activeCollectionChangeEventPublisher;
    private final CardStateChangeEventPublisher cardStateChangeEventPublisher;
    private final CardGroupChoiceChangeEventPublisher cardGroupChoiceChangeEventPublisher;
    @Getter
    private CardCollection activeCollection = null;
    @Getter
    private CardState cardState = CardState.ABSENT;

    public boolean hasActiveCollection() {
        return activeCollection != null;
    }

    public boolean setActiveCollection(CardCollection activeCollection) {
        if (this.activeCollection != null && !setCardGroupChoice(CardGroupChoice.UNSELECTED))
            return false;

        this.activeCollection = activeCollection;
        activeCollectionChangeEventPublisher.publishEvent();
        return true;
    }

    public void setCardState(CardState cardState) {
        this.cardState = cardState;
        cardStateChangeEventPublisher.publishEvent();
    }

    public CardGroupChoice getCardGroupChoice() {
        return activeCollection != null ? activeCollection.getCardGroupChoice() : CardGroupChoice.UNSELECTED;
    }

    public boolean setCardGroupChoice(CardGroupChoice cardGroupChoice) {
        final boolean setProperly = activeCollection != null && activeCollection.setCardGroupChoice(cardGroupChoice);
        if (setProperly)
            cardGroupChoiceChangeEventPublisher.publishEvent();

        return setProperly;
    }

    public static class ActiveCollectionChangeEvent extends ApplicationEvent {
        public ActiveCollectionChangeEvent(Object source) {
            super(source);
        }
    }

    public static class CardStateChangeEvent extends ApplicationEvent {
        public CardStateChangeEvent(Object source) {
            super(source);
        }
    }

    public static class CardGroupChoiceChangeEvent extends ApplicationEvent {
        public CardGroupChoiceChangeEvent(Object source) {
            super(source);
        }
    }

    @Component
    public static class ChangeEventPublisher {
        @Autowired
        private ApplicationEventPublisher applicationEventPublisher;

        protected void publishEvent(ApplicationEvent event) {
            applicationEventPublisher.publishEvent(event);
        }
    }

    @Component
    public static class ActiveCollectionChangeEventPublisher extends ChangeEventPublisher {
        public void publishEvent() {
            super.publishEvent(new ActiveCollectionChangeEvent(this));
        }
    }

    @Component
    public static class CardStateChangeEventPublisher extends ChangeEventPublisher {
        public void publishEvent() {
            super.publishEvent(new CardStateChangeEvent(this));
        }
    }

    @Component
    public static class CardGroupChoiceChangeEventPublisher extends ChangeEventPublisher {
        public void publishEvent() {
            super.publishEvent(new CardGroupChoiceChangeEvent(this));
        }
    }
}
