package pl.mateuszfrejlich.flashcards.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CollectionEditor;
import pl.mateuszfrejlich.flashcards.util.Flashcard;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.mateuszfrejlich.flashcards.util.CollectionEditor.*;

@Controller
public class EditionController implements OptionsController.DialogController {
    @Autowired
    private SessionState sessionState;
    private CollectionEditor editor;
    private boolean isSwapped = false;
    @FXML
    private TitledPane pnForm;
    @FXML
    private ComboBox<String> cbxItem;
    @FXML
    private TextField tfFront;
    @FXML
    private TextField tfReverse;

    @Override
    public void start() {
        Platform.runLater(() -> {
            CardCollection activeCollection = sessionState.getActiveCollection();
            pnForm.setText(activeCollection.getName());
            editor = activeCollection.createEditor();
            refreshWordList();
        });
    }

    @FXML
    void handleOK(ActionEvent ignoredEvent) {
        CardCollection activeCollection = sessionState.getActiveCollection();
        activeCollection.executeEdition(editor);
        sessionState.setActiveCollection(activeCollection);
        pnForm.getScene().getWindow().hide();
    }

    @FXML
    void handleCancel(ActionEvent ignoredEvent) {
        pnForm.getScene().getWindow().hide();
    }

    @FXML
    void handleAddClicked(ActionEvent ignoredEvent) {
        Flashcard card = new Flashcard(tfFront.getText().trim(), tfReverse.getText().trim());

        if (!card.isCorrect()) {
            String msg1 = "Each side must contain between 1 and " + Flashcard.maxNumOfChars() + " characters!\n";
            String msg2 = "Sing ' is forbidden, try to use \" instead.";
            ErrorHandler.handleError(msg1 + msg2);
            return;
        }

        final boolean updated = editor.addCard(card);

        if (updated) {
            cbxItem.getItems().add(formatCardText(card));
            cbxItem.getSelectionModel().selectLast();
        } else
            ErrorHandler.handleError("A card with the same front side already exists!");
    }

    @FXML
    void handleUpdateClicked(ActionEvent ignoredEvent) {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();

        if (index == -1)
            ErrorHandler.handleError("No selected item!");
        else
            updateItem(index);
    }

    @FXML
    void handleDeleteClicked(ActionEvent ignoredEvent) {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();

        if (index == -1)
            ErrorHandler.handleError("No selected item!");
        else {
            editor.deleteCard(index);
            cbxItem.getItems().remove(index);
        }
    }

    @FXML
    void handleShuffleClicked(ActionEvent ignoredEvent) {
        editor.shuffleCards();
        refreshWordList();
    }

    @FXML
    void handleItemSelected(ActionEvent ignoredEvent) {
        fillTextFields();
    }

    @FXML
    void handleSortClicked(ActionEvent ignoredEvent){
        final ComparatorKey key = isSwapped ? ComparatorKey.REVERSE_TEXT : ComparatorKey.FRONT_TEXT;
        editor.sortCards(key);
        refreshWordList();
    }

    @FXML
    void handleSwapSidesClicked(ActionEvent ignoredEvent) {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();
        isSwapped = !isSwapped;
        refreshWordList();
        cbxItem.getSelectionModel().select(index);
    }

    private void fillTextFields() {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            clearTextFields();
            return;
        }

        Flashcard card = editor.getCard(index);
        tfFront.setText(card.frontText());
        tfReverse.setText(card.reverseText());
    }

    private void clearTextFields() {
        tfFront.setText("");
        tfReverse.setText("");
    }

    private void refreshWordList() {
        Stream<Flashcard> cards = editor.getCards();
        List<String> items = cards.map(this::formatCardText).collect(Collectors.toList());
        cbxItem.getItems().clear();
        cbxItem.setItems(FXCollections.observableList(items));
    }

    private void updateItem(int index) {
        Flashcard card = new Flashcard(tfFront.getText().trim(), tfReverse.getText().trim());
        final boolean updated = editor.updateCard(index, card);

        if (updated) {
            cbxItem.getItems().remove(index);
            cbxItem.getItems().add(index, formatCardText(card));
            cbxItem.getSelectionModel().select(index);
        } else {
            String msg1 = "Each side must contain between 1 and " + Flashcard.maxNumOfChars() + " characters!\n";
            String msg2 = "Symbol ' is forbidden, try to use \" instead.";
            ErrorHandler.handleError(msg1 + msg2);
        }
    }

    private String formatCardText(Flashcard card) {
        if (isSwapped)
            return card.reverseText() + " - " + card.frontText();
        else
            return card.frontText() + " - " + card.reverseText();
    }
}
