package pl.mateuszfrejlich.flashcards.controller;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.service.StageInitializer;
import pl.mateuszfrejlich.flashcards.model.SessionState;
import pl.mateuszfrejlich.flashcards.service.CollectionsManager;
import pl.mateuszfrejlich.flashcards.util.CardCollection;
import pl.mateuszfrejlich.flashcards.util.CardState;

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
    @Value("classpath:/creation-dialog.fxml")
    private Resource creationDialogResource;
    @Value("classpath:/edition-dialog.fxml")
    private Resource editionDialogResource;
    @FXML
    private ComboBox<String> cbxCollection;
    @FXML
    private GridPane pnOptions;

    public void start() {
        Platform.runLater(this::refreshCollectionList);
    }

    @FXML
    void handleNewClicked(ActionEvent ignoredEvent) {
        openDialog(creationDialogResource, CreationController.class,
                "Creation dialog", 400, 200);
        cbxCollection.getItems().clear();
        refreshCollectionList();
    }

    @FXML
    void handleEditClicked(ActionEvent ignoredEvent) {
        if (!sessionState.hasActiveCollection())
            ErrorHandler.handleError("No collection selected!");
        else
            openDialog(editionDialogResource, EditionController.class,
                    "Edition dialog", 500, 300);
    }

    @FXML
    void handleDeleteClicked(ActionEvent ignoredEvent) {
        if (!sessionState.hasActiveCollection()) {
            ErrorHandler.handleError("No collection selected!");
            return;
        }

        String collectionName = sessionState.getActiveCollection().getName();
        if (requestDeleteConfirmation(collectionName))
            deleteCollection(collectionName);
    }

    @FXML
    void handleCollectionSelected(ActionEvent ignoredEvent) {
        if (sessionState.hasActiveCollection())
            saveActiveCollection();

        String selectedCollectionName = cbxCollection.getValue();
        changeActiveCollection(selectedCollectionName);
    }

    @EventListener
    public void handleEvent(SessionState.CardStateChangeEvent event) {
        switch (sessionState.getCardState()) {
            case TO_DRAW, ABSENT -> pnOptions.setDisable(false);
            case REVERSED, FACE_UP -> pnOptions.setDisable(true);
        }
    }

    private void saveActiveCollection() {
        if (!collectionsManager.updateCardsCollection(sessionState.getActiveCollection()))
            ErrorHandler.handleError("Error with database connection! Data has NOT been saved!");
    }

    private void changeActiveCollection(String name) {
        CardCollection newActiveCollection = name != null ? collectionsManager.getCollection(name) : null;

        if (sessionState.setActiveCollection(newActiveCollection))
            sessionState.setCardState(newActiveCollection != null ? CardState.TO_DRAW : CardState.ABSENT);
        else
            ErrorHandler.handleError("Unable to change active collection! Put back the card first!");

        if (name != null && newActiveCollection == null)
            ErrorHandler.handleError("Error with fetching data of collection '" + name + "'!");
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
            ErrorHandler.handleError("Request to database has caused an error! Collection wasn't delete properly!");
            return;
        }

        sessionState.setActiveCollection(null);
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

    private void openDialog(Resource resource, Class<? extends DialogController> aClass, String title,
                            double minWidth, double minHeight) {
        try {
            Stage stage = new Stage();
            DialogController controller = aClass.cast(stageInitializer.initStage(stage, resource.getURL()));
            stage.setTitle(title);
            stage.setMinWidth(minWidth);
            stage.setMinHeight(minHeight);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(pnOptions.getScene().getWindow());
            controller.start();
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    interface DialogController {
        void start();
    }
}
