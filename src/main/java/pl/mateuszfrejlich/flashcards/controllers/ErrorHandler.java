package pl.mateuszfrejlich.flashcards.controllers;

import javafx.scene.control.Alert;

class ErrorHandler {
    static void handleError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        alert.show();
    }
}