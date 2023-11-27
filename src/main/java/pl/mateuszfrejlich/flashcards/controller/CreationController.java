package pl.mateuszfrejlich.flashcards.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import pl.mateuszfrejlich.flashcards.service.CollectionsManager;

import java.io.File;

@Controller
public class CreationController implements OptionsController.DialogController {
    @Autowired
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

    @Override
    public void start() {
        // nothing needed to do
    }

    @FXML
    void handleOK(ActionEvent ignoredEvent) {
        final boolean created = addNewCollection();

        if (created)
            cbEmpty.getScene().getWindow().hide();
        else
            ErrorHandler.handleError("Invalid data!");
    }

    @FXML
    void handleCancel(ActionEvent ignoredEvent) {
        cbEmpty.getScene().getWindow().hide();
    }

    @FXML
    void handlePathClicked(ActionEvent ignoredEvent) {
        useFileChoiceDialog();
    }

    @FXML
    void handleCheckChanged(ActionEvent ignoredEvent) {
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
