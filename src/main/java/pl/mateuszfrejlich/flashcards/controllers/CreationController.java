package pl.mateuszfrejlich.flashcards.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import pl.mateuszfrejlich.flashcards.CollectionsManager;

import java.io.File;

public class CreationController {

    private CollectionsManager collectionsManager;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfPath;
    @FXML
    private Label lbPath;
    @FXML
    private Button btnPath;
    @FXML
    private CheckBox cbEmpty;

    public void setup(CollectionsManager collectionsManager) {
        this.collectionsManager = collectionsManager;
    }

    @FXML
    void handleOK(ActionEvent event) {
        final boolean created = addNewCollection();

        if (created)
            cbEmpty.getScene().getWindow().hide();
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid data!");
            alert.show();
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        cbEmpty.getScene().getWindow().hide();
    }

    @FXML
    void handlePathClicked(ActionEvent event) {
        useFileChoiceDialog();
    }

    @FXML
    void handleCheckChanged(ActionEvent event) {
        setEnabledPathInput(!cbEmpty.isSelected());
    }

    private void useFileChoiceDialog() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(".csv, .txt", "*.txt", "*.csv");
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(null);
        try {
            if (file.canRead())
                tfPath.setText(file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void setEnabledPathInput(boolean enabled) {
        lbPath.setDisable(!enabled);
        tfPath.setDisable(!enabled);
        btnPath.setDisable(!enabled);
    }

    private boolean addNewCollection() {
        if (tfPath.isDisable())
            return collectionsManager.addNewCollection(tfName.getText().trim());
        else
            return collectionsManager.addNewCollection(tfName.getText().trim(), tfPath.getText().trim());
    }

}
