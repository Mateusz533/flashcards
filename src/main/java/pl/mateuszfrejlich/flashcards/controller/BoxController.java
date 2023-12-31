package pl.mateuszfrejlich.flashcards.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.util.CardBox;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.util.CardState;

import java.util.List;

@Controller
public class BoxController {
    @Autowired
    private SessionState sessionState;
    @FXML
    private Label lbSection1;
    @FXML
    private Label lbSection2;
    @FXML
    private Label lbSection3;
    @FXML
    private Label lbSection4;
    @FXML
    private Label lbSection5;
    @FXML
    private GridPane pnBox;

    @FXML
    void handleBoxClicked(MouseEvent ignoredEvent) {
        if (sessionState.getCardState() == CardState.TO_DRAW)
            sessionState.setCardGroupChoice(CardGroupChoice.INBOX);
    }

    @EventListener
    public void handleEvent(SessionState.ActiveCollectionChangeEvent event) {
        refreshView();
    }

    @EventListener
    public void handleEvent(SessionState.CardStateChangeEvent event) {
        CardState cardState = sessionState.getCardState();
        switch (cardState) {
            case REVERSED, FACE_UP -> pnBox.setDisable(true);
            case TO_DRAW -> {
                pnBox.setDisable(false);
                refreshView();
            }
            case ABSENT -> {
                pnBox.setDisable(true);
                refreshView();
            }
        }
    }

    @EventListener
    public void handleEvent(SessionState.CardGroupChoiceChangeEvent event) {
        CardGroupChoice choice = sessionState.getCardGroupChoice();
        pnBox.setOpacity(choice == CardGroupChoice.INBOX ? 1.0 : 0.5);
    }

    private void refreshView() {
        List<Integer> sectionFillings;

        if (sessionState.hasActiveCollection()) {
            CardCollection activeCollection = sessionState.getActiveCollection();
            sectionFillings = activeCollection.boxSectionsFilling();
        } else
            sectionFillings = List.of(0, 0, 0, 0, 0);

        setDisplayedSectionSizes(sectionFillings);
    }

    private void setDisplayedSectionSizes(List<Integer> sectionFillings) {
        List<Label> sectionLabels = List.of(lbSection1, lbSection2, lbSection3, lbSection4, lbSection5);
        List<Integer> sectionSizes = CardBox.sectionSizes();

        if (sectionLabels.size() != sectionFillings.size()) {
            ErrorHandler.handleError("Incorrect card box data!");
            return;
        }

        for (int i = 0; i < sectionLabels.size(); ++i) {
            String text = String.valueOf(sectionFillings.get(i)) + '/' + sectionSizes.get(i);
            sectionLabels.get(i).setText(text);
        }
    }
}
