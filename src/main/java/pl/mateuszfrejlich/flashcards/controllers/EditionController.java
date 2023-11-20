package pl.mateuszfrejlich.flashcards.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import pl.mateuszfrejlich.flashcards.CardCollection;
import pl.mateuszfrejlich.flashcards.CollectionEditor;
import pl.mateuszfrejlich.flashcards.Flashcard;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EditionController {
    private CardCollection cardCollection;
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

    public void setup(CardCollection cardCollection) {
        this.cardCollection = cardCollection;
        Platform.runLater(() -> {
            pnForm.setText(cardCollection.getName());
            editor = cardCollection.createEditor();
            refreshWordList();
        });
    }

    @FXML
    void handleOK(ActionEvent ignoredEvent) {
        cardCollection.executeEdition(editor);
        pnForm.getScene().getWindow().hide();
    }

    @FXML
    void handleCancel(ActionEvent ignoredEvent) {
        pnForm.getScene().getWindow().hide();
    }

    @FXML
    void handleAddClicked(ActionEvent ignoredEvent) {
        Flashcard card = new Flashcard(tfFront.getText().trim(), tfReverse.getText().trim());
        final boolean updated = editor.addCard(card);

        if (updated)
            cbxItem.getItems().add(formatCardText(card));
        else
            handleError("Invalid data");
    }

    @FXML
    void handleUpdateClicked(ActionEvent ignoredEvent) {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();

        if (index == -1)
            handleError("No selected item!");
        else
            updateItem(index);
    }

    @FXML
    void handleDeleteClicked(ActionEvent ignoredEvent) {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();

        if (index == -1)
            handleError("No selected item!");
        else {
            editor.deleteCard(index);
            cbxItem.getItems().remove(index);
            fillTextFields();
        }
    }

    @FXML
    void handleItemSelected(ActionEvent ignoredEvent) {
        fillTextFields();
    }

    @FXML
    void handleSwapSidesClicked(ActionEvent ignoredEvent) {
        isSwapped = !isSwapped;
        refreshWordList();
    }

    private void fillTextFields() {
        final int index = cbxItem.getSelectionModel().getSelectedIndex();
        if (index == -1)
            return;

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
            clearTextFields();
        } else
            handleError("Invalid data!");
    }

    private String formatCardText(Flashcard card) {
        if (isSwapped)
            return card.reverseText() + '/' + card.frontText();
        else
            return card.frontText() + '/' + card.reverseText();
    }

    private void handleError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.show();
    }
}
