package pl.mateuszfrejlich.flashcards;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class FlashcardsApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(FlashcardsApplication.class).headless(false).web(WebApplicationType.NONE).run(args);
        MainWindow window = ctx.getBean(MainWindow.class);
        window.setTitle("Flashcards");
        window.setSize(800, 600);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
