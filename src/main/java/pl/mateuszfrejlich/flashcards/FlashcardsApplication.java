package pl.mateuszfrejlich.flashcards;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

@SpringBootApplication
public class FlashcardsApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(FlashcardsApplication.class).headless(false).web(WebApplicationType.NONE).run(args);
        EventQueue.invokeLater(() -> {
            MainWindow window = ctx.getBean(MainWindow.class);
        });
    }
}
