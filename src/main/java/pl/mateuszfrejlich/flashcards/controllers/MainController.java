package pl.mateuszfrejlich.flashcards.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.springframework.stereotype.Component;
import pl.mateuszfrejlich.flashcards.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MainController {
    private CardCollection activeCollection = null;
    private CollectionsManager collectionsManager;
    private CardState cardState = CardState.ABSENT;

    @FXML
    private ComboBox<String> cbxCollection;

    @FXML
    private Button btnPrepared;

    @FXML
    private Button btnArchived;

    @FXML
    private Button btnPassed;

    @FXML
    private Button btnFailed;

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
    private Label lbWord;

    @FXML
    private GridPane pnOptions;

    @FXML
    private GridPane pnBox;

    @FXML
    private GridPane pnFlashcard;

    @FXML
    private GridPane pnButtons;

    @FXML
    private GridPane pnUnboxedCards;

    public void setup(Stage stage) {
        stage.setOnCloseRequest(this::handleOnCloseRequest);

        Platform.runLater(() -> {
            try {
                collectionsManager = new CollectionsManager();
            } catch (Exception e) {
                handleError(e.getMessage());
                pnOptions.getScene().getWindow().hide();
            }
            setCardState(CardState.ABSENT);
            refreshCollectionList();
        });
    }

    @FXML
    void handleOnCloseRequest(WindowEvent event) {
        if (activeCollection == null)
            return;

        if (activeCollection.getActiveCard() != null)
            activeCollection.putBorrowedCard(false);

        activeCollection.saveChanges();
    }

    @FXML
    void handleNewClicked(ActionEvent ignoredEvent) {
        openCreationDialog();
        cbxCollection.getItems().clear();
        refreshCollectionList();
    }

    @FXML
    void handleEditClicked(ActionEvent ignoredEvent) {
        if (activeCollection == null)
            handleError("No collection selected!");
        else
            openEditionDialog();
    }

    @FXML
    void handleDeleteClicked(ActionEvent ignoredEvent) {
        if (activeCollection == null) {
            handleError("No collection selected!");
            return;
        }

        String collectionName = activeCollection.getName();
        String msg = "Are you sure you want to permanently delete collection '" + collectionName + "'?";
        ButtonType btnYes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", btnYes, btnNo);
        alert.setTitle("Confirm deletion");
        alert.setHeaderText(msg);
        Optional<ButtonType> result = alert.showAndWait();

        if (result.orElse(btnYes) == btnYes)
            deleteCollection(collectionName);
    }

    @FXML
    void handleCollectionChanged(ActionEvent ignoredEvent) {
        if (activeCollection != null)
            activeCollection.saveChanges();

        String selectedCollection = cbxCollection.getValue();
        chooseCardContainer(CardGroupChoice.UNSELECTED);

        if (selectedCollection != null) {
            activeCollection = collectionsManager.getCollection(selectedCollection);
            setCardState(CardState.TO_DRAW);
        } else {
            activeCollection = null;
            setCardState(CardState.ABSENT);
        }
    }

    @FXML
    void handleCardClicked(MouseEvent ignoredEvent) {
        switch (cardState) {
            case TO_DRAW -> setCardState(CardState.REVERSED);
            case REVERSED -> setCardState(CardState.FACE_UP);
            case FACE_UP, ABSENT -> {
                // From that state it is changed by different action
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardState);
        }
    }

    @FXML
    void handlePreparedClicked(ActionEvent ignoredEvent) {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.PREPARED);
    }

    @FXML
    void handleArchivedClicked(ActionEvent ignoredEvent) {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.ARCHIVED);
    }

    @FXML
    void handleBoxClicked(MouseEvent ignoredEvent) {
        if (cardState == CardState.TO_DRAW)
            chooseCardContainer(CardGroupChoice.INBOX);
    }

    @FXML
    void handlePassedClicked(ActionEvent ignoredEvent) {
        activeCollection.putBorrowedCard(true);
        setCardState(CardState.TO_DRAW);
    }

    @FXML
    void handleFailedClicked(ActionEvent ignoredEvent) {
        activeCollection.putBorrowedCard(false);
        setCardState(CardState.TO_DRAW);
    }

    private void deleteCollection(String collectionName) {
        final boolean deleted = collectionsManager.deleteCollection(collectionName);
        if (!deleted) {
            handleError("Request to database has caused an error! Collection wasn't delete properly!");
            return;
        }

        activeCollection = null;
        chooseCardContainer(CardGroupChoice.UNSELECTED);
        setCardState(CardState.ABSENT);
        cbxCollection.getItems().remove(cbxCollection.getValue());
    }

    private void setCardState(CardState state) {
        switch (state) {
            case REVERSED -> {
                activeCollection.processNextCard();
                Flashcard activeCard = activeCollection.getActiveCard();
                if (activeCard == null)
                    return;

                setCardText(activeCard.reverseText());
                setEnabledStates(false, false);
            }
            case FACE_UP -> {
                setCardText(activeCollection.getActiveCard().frontText());
                setEnabledStates(false, true);
            }
            case TO_DRAW -> {
                setCardText("Get next");
                setEnabledStates(true, false);
                refreshGroupView();
            }
            case ABSENT -> {
                setCardText("Select collection");
                pnOptions.setDisable(false);
                pnFlashcard.setDisable(true);
                pnBox.setDisable(true);
                pnUnboxedCards.setDisable(true);
                pnButtons.setDisable(true);
                refreshGroupView();
            }
            default -> throw new IllegalStateException("Unexpected value: " + state);
        }
        cardState = state;
    }

    private void chooseCardContainer(CardGroupChoice choice) {
        if (activeCollection != null) {
            if (activeCollection.getCardGroupChoice() == choice)
                return;
            activeCollection.setCardGroupChoice(choice);
        }

        String colorSelected = "#9e9";
        String colorNormal = "#eee";
        setButtonColor(btnPrepared, (choice == CardGroupChoice.PREPARED) ? colorSelected : colorNormal);
        setButtonColor(btnArchived, (choice == CardGroupChoice.ARCHIVED) ? colorSelected : colorNormal);
        pnBox.setOpacity(choice == CardGroupChoice.INBOX ? 1.0 : 0.5);

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
                btnPassed.setText("PRESERVE");
                btnFailed.setText("REMOVE");
            }
            default -> throw new IllegalStateException("Unexpected value: " + choice);
        }
    }

    private void setEnabledStates(boolean cardGroupChoice, boolean cardRedirection) {
        pnOptions.setDisable(!cardGroupChoice);
        pnBox.setDisable(!cardGroupChoice);
        pnUnboxedCards.setDisable(!cardGroupChoice);
        pnButtons.setDisable(!cardRedirection);
        pnFlashcard.setDisable(!cardGroupChoice && cardRedirection);
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

    private void refreshCollectionList() {
        Stream<String> names = collectionsManager.getCollectionNames();
        if (names != null) {
            cbxCollection.getItems().clear();
            cbxCollection.setItems(FXCollections.observableList(names.collect(Collectors.toList())));
        }
    }

    private void refreshGroupView() {
        List<Label> sectionLabels = List.of(lbSection1, lbSection2, lbSection3, lbSection4, lbSection5);
        List<Integer> sectionSizes = CardBox.sectionSizes();
        final int numberOfPreparedCards;
        final int numberOfArchivedCards;
        List<Integer> sectionsFilling;

        if (activeCollection == null) {
            numberOfPreparedCards = 0;
            numberOfArchivedCards = 0;
            sectionsFilling = List.of(0, 0, 0, 0, 0);
        } else {
            numberOfPreparedCards = activeCollection.numberOfPreparedCards();
            numberOfArchivedCards = activeCollection.numberOfArchivedCards();
            sectionsFilling = activeCollection.boxSectionsFilling();
        }

        if (sectionLabels.size() != sectionsFilling.size()) {
            handleError("Incorrect card box data!");
            return;
        }

        btnPrepared.setText(String.valueOf(numberOfPreparedCards));
        btnArchived.setText(String.valueOf(numberOfArchivedCards));

        for (int i = 0; i < sectionLabels.size(); ++i) {
            String text = String.valueOf(sectionsFilling.get(i)) + '/' + sectionSizes.get(i);
            sectionLabels.get(i).setText(text);
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
            FXMLLoader fxmlLoader = new FXMLLoader(CreationController.class.getResource("/creation-dialog.fxml"));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Creation dialog");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(pnOptions.getScene().getWindow());

            CreationController controller = fxmlLoader.getController();
            controller.setup(collectionsManager);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void openEditionDialog() {
        try {
            Stage stage = new Stage();
            FXMLLoader fxmlLoader = new FXMLLoader(EditionController.class.getResource("/edition-dialog.fxml"));
            Parent root = fxmlLoader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Edition dialog");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(pnOptions.getScene().getWindow());

            EditionController controller = fxmlLoader.getController();
            controller.setup(activeCollection);
            stage.showAndWait();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
