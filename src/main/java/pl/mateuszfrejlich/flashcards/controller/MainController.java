package pl.mateuszfrejlich.flashcards.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.service.CollectionsManager;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.util.CardState;

@Controller
public class MainController {
    @Autowired
    private CollectionsManager collectionsManager;
    @Autowired
    private SessionState sessionState;
    @Autowired
    private OptionsController optionsController;
    @FXML
    private Button btnPrepared;
    @FXML
    private Button btnArchived;
    @FXML
    private Button btnPassed;
    @FXML
    private Button btnFailed;
    @FXML
    private Label lbWord;
    @FXML
    private GridPane pnFlashcard;
    @FXML
    private GridPane pnButtons;
    @FXML
    private GridPane pnUnboxedCards;

    public void start(Stage stage) {
        stage.setOnCloseRequest(this::handleOnCloseRequest);

        Platform.runLater(() -> {
            try {
                collectionsManager.setupDBConnection();
                sessionState.setCardState(CardState.ABSENT);
                sessionState.setCardGroupChoice(CardGroupChoice.UNSELECTED);
                optionsController.start();
            } catch (Exception e) {
                ErrorHandler.handleError(e.getMessage());
                pnFlashcard.getScene().getWindow().hide();
            }
        });
    }

    @FXML
    void handleOnCloseRequest(WindowEvent event) {
        if (!sessionState.hasActiveCollection())
            return;

        CardCollection activeCollection = sessionState.getActiveCollection();
        activeCollection.putCardBack(false);
        collectionsManager.updateCardsCollection(activeCollection);
    }

    @FXML
    void handleCardClicked(MouseEvent ignoredEvent) {
        if (sessionState.getCardGroupChoice() == CardGroupChoice.UNSELECTED)
            return;

        switch (sessionState.getCardState()) {
            case TO_DRAW -> {
                if (sessionState.getActiveCollection().borrowNextCard() != null)
                    sessionState.setCardState(CardState.REVERSED);
            }
            case REVERSED -> sessionState.setCardState(CardState.FACE_UP);
            case FACE_UP, ABSENT -> {
                // From that state it is changed by different action
            }
        }
    }

    @FXML
    void handlePreparedClicked(ActionEvent ignoredEvent) {
        tryChooseCardGroup(CardGroupChoice.PREPARED);
    }

    @FXML
    void handleArchivedClicked(ActionEvent ignoredEvent) {
        tryChooseCardGroup(CardGroupChoice.ARCHIVED);
    }

    @FXML
    void handlePassedClicked(ActionEvent ignoredEvent) {
        tryRedirectCard(true);
    }

    @FXML
    void handleFailedClicked(ActionEvent ignoredEvent) {
        tryRedirectCard(false);
    }

    @EventListener
    public void handleEvent(SessionState.ActiveCollectionChangeEvent event) {
        refreshGroupView();
    }

    @EventListener
    public void handleEvent(SessionState.CardStateChangeEvent event) {
        CardState cardState = sessionState.getCardState();
        switch (cardState) {
            case REVERSED -> {
                setCardText(sessionState.getActiveCollection().getBorrowedCard().reverseText());
                setEnabledStates(false, false, true);
            }
            case FACE_UP -> {
                setCardText(sessionState.getActiveCollection().getBorrowedCard().frontText());
                setEnabledStates(false, true, false);
            }
            case TO_DRAW -> {
                setCardText("Get next");
                setEnabledStates(true, false, true);
                refreshGroupView();
            }
            case ABSENT -> {
                setCardText("Select collection");
                setEnabledStates(false, false, false);
                refreshGroupView();
            }
        }
    }

    @EventListener
    public void handleEvent(SessionState.CardGroupChoiceChangeEvent event) {
        CardGroupChoice choice = sessionState.getCardGroupChoice();

        String colorSelected = "#9e9";
        String colorNormal = "#eee";
        setButtonColor(btnPrepared, (choice == CardGroupChoice.PREPARED) ? colorSelected : colorNormal);
        setButtonColor(btnArchived, (choice == CardGroupChoice.ARCHIVED) ? colorSelected : colorNormal);

        switch (choice) {
            case PREPARED -> {
                btnPassed.setText("REMEMBERED");
                btnFailed.setText("KEEP");
            }
            case INBOX, UNSELECTED -> {
                btnPassed.setText("PASSED");
                btnFailed.setText("FAILED");
            }
            case ARCHIVED -> {
                btnPassed.setText("REMOVE");
                btnFailed.setText("PRESERVE");
            }
        }
    }

    private void tryChooseCardGroup(CardGroupChoice choice) {
        if (sessionState.getCardState() == CardState.TO_DRAW)
            sessionState.setCardGroupChoice(choice);
    }

    private void tryRedirectCard(boolean isPassed) {
        if (sessionState.getCardState() == CardState.FACE_UP) {
            sessionState.getActiveCollection().putCardBack(isPassed);
            sessionState.setCardState(CardState.TO_DRAW);
        }
    }

    private void setEnabledStates(boolean cardGroupChoice, boolean cardRedirection, boolean cardMove) {
        pnUnboxedCards.setDisable(!cardGroupChoice);
        pnButtons.setDisable(!cardRedirection);
        pnFlashcard.setDisable(!cardMove);
    }

    private void setButtonColor(Button button, String color) {
        BackgroundFill bg = button.getBackground().getFills().get(0);
        button.setStyle("{}");
        BackgroundFill fill = new BackgroundFill(Color.valueOf(color), bg.getRadii(), bg.getInsets());
        button.setBackground(new Background(fill));
    }

    private void setCardText(String text) {
        lbWord.setText(text);
    }

    private void refreshGroupView() {
        CardCollection activeCollection = sessionState.getActiveCollection();
        final int numberOfPreparedCards = activeCollection != null ? activeCollection.numberOfPreparedCards() : 0;
        final int numberOfArchivedCards = activeCollection != null ? activeCollection.numberOfArchivedCards() : 0;

        btnPrepared.setText(String.valueOf(numberOfPreparedCards));
        btnArchived.setText(String.valueOf(numberOfArchivedCards));
    }
}
