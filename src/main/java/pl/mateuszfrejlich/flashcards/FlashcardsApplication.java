package pl.mateuszfrejlich.flashcards;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FlashcardsApplication {
    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }
}
