package pl.mateuszfrejlich.flashcards.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.StageInitializer;
import pl.mateuszfrejlich.flashcards.model.CardCollection;
import pl.mateuszfrejlich.flashcards.model.CardGroupChoice;
import pl.mateuszfrejlich.flashcards.model.CardState;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.service.CollectionsManager;

import java.net.URL;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class OptionsController {
    @Autowired
    private CollectionsManager collectionsManager;
    @Autowired
    private SessionState sessionState;
    @Autowired
    private StageInitializer stageInitializer;
    @FXML
    private ComboBox<String> cbxCollection;
    @FXML
    private GridPane pnOptions;

    public void start() {
        Platform.runLater(this::refreshCollectionList);
    }

    @FXML
    void handleNewClicked(ActionEvent ignoredEvent) {
        openCreationDialog();
        cbxCollection.getItems().clear();
        refreshCollectionList();
    }

    @FXML
    void handleEditClicked(ActionEvent ignoredEvent) {
        if (!sessionState.hasActiveCollection())
            handleError("No collection selected!");
        else
            openEditionDialog();
    }

    @FXML
    void handleDeleteClicked(ActionEvent ignoredEvent) {
        if (!sessionState.hasActiveCollection()) {
            handleError("No collection selected!");
            return;
        }

        String collectionName = sessionState.getActiveCollection().getName();
        if (requestDeleteConfirmation(collectionName))
            deleteCollection(collectionName);
    }

    @FXML
    void handleCollectionChanged(ActionEvent ignoredEvent) {
        if (sessionState.hasActiveCollection())
            collectionsManager.updateCardsCollection(sessionState.getActiveCollection());

        String selectedCollectionName = cbxCollection.getValue();
        sessionState.setCardGroupChoice(CardGroupChoice.UNSELECTED);

        if (selectedCollectionName == null) {
            sessionState.setActiveCollection(null);
            sessionState.setCardState(CardState.ABSENT);
            return;
        }

        CardCollection newActiveCollection = collectionsManager.getCollection(selectedCollectionName);
        sessionState.setActiveCollection(newActiveCollection);
        if (newActiveCollection != null)
            sessionState.setCardState(CardState.TO_DRAW);
        else {
            handleError("Error with fetching data of collection '" + selectedCollectionName + "'!");
            sessionState.setCardState(CardState.ABSENT);
        }
    }

    @EventListener
    public void handleEvent(SessionState.CardStateChangeEvent event) {
        switch (sessionState.getCardState()) {
            case TO_DRAW, ABSENT -> pnOptions.setDisable(false);
            case REVERSED, FACE_UP -> pnOptions.setDisable(true);
        }
    }

    private boolean requestDeleteConfirmation(String collectionName) {
        String msg = "Are you sure you want to permanently delete collection '" + collectionName + "'?";
        ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", btnYes, btnNo);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();

        return result.orElse(btnYes) == btnYes;
    }

    private void deleteCollection(String collectionName) {
        final boolean deleted = collectionsManager.deleteCollection(collectionName);
        if (!deleted) {
            handleError("Request to database has caused an error! Collection wasn't delete properly!");
            return;
        }

        sessionState.setActiveCollection(null);
        sessionState.setCardGroupChoice(CardGroupChoice.UNSELECTED);
        sessionState.setCardState(CardState.ABSENT);
        cbxCollection.getItems().remove(cbxCollection.getValue());
    }

    private void refreshCollectionList() {
        Stream<String> names = collectionsManager.getCollectionNames();
        if (names != null) {
            cbxCollection.getItems().clear();
            cbxCollection.setItems(FXCollections.observableList(names.collect(Collectors.toList())));
        }
    }

    private void handleError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.show();
    }

    private void openCreationDialog() {
        try {
            Stage stage = new Stage();
            URL url = CreationController.class.getResource("/creation-dialog.fxml");
            CreationController ignoredController = (CreationController) stageInitializer.initStage(stage, url);
            openDialog(stage, "Creation dialog");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void openEditionDialog() {
        try {
            Stage stage = new Stage();
            URL url = EditionController.class.getResource("/edition-dialog.fxml");
            EditionController controller = (EditionController) stageInitializer.initStage(stage, url);
            controller.start();
            openDialog(stage, "Edition dialog");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void openDialog(Stage stage, String title) {
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(pnOptions.getScene().getWindow());
        stage.showAndWait();
    }
}
