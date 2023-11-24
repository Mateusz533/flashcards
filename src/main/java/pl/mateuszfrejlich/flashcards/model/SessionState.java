package pl.mateuszfrejlich.flashcards.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SessionState {
    private CardCollection activeCollection = null;
    private CardState cardState = CardState.ABSENT;
    private CardGroupChoice cardGroupChoice = CardGroupChoice.UNSELECTED;
    @Autowired
    private ActiveCollectionChangeEventPublisher activeCollectionChangeEventPublisher;
    @Autowired
    private CardStateChangeEventPublisher cardStateChangeEventPublisher;
    @Autowired
    private CardGroupChoiceChangeEventPublisher cardGroupChoiceChangeEventPublisher;

    public boolean hasActiveCollection() {
        return activeCollection != null;
    }

    public CardCollection getActiveCollection() {
        return activeCollection;
    }

    public void setActiveCollection(CardCollection activeCollection) {
        this.activeCollection = activeCollection;
        activeCollectionChangeEventPublisher.publishEvent();
    }

    public CardState getCardState() {
        return cardState;
    }

    public void setCardState(CardState cardState) {
        this.cardState = cardState;
        cardStateChangeEventPublisher.publishEvent();
    }

    public CardGroupChoice getCardGroupChoice() {
        return cardGroupChoice;
    }

    public void setCardGroupChoice(CardGroupChoice cardGroupChoice) {
        this.cardGroupChoice = cardGroupChoice;
        if (activeCollection != null)
            activeCollection.setCardGroupChoice(cardGroupChoice);
        cardGroupChoiceChangeEventPublisher.publishEvent();
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
