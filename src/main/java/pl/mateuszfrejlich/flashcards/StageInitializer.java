package pl.mateuszfrejlich.flashcards;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import pl.mateuszfrejlich.flashcards.controller.MainController;

import java.io.IOException;
import java.net.URL;

import static pl.mateuszfrejlich.flashcards.MainApplication.StageReadyEvent;

@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {
    private final ApplicationContext applicationContext;
    @Value("classpath:/main-view.fxml")
    private Resource resource;

    public StageInitializer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            MainController controller = (MainController) initStage(stage, resource.getURL());

            stage.setWidth(800);
            stage.setHeight(600);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setTitle("Flashcards");

            controller.start(stage);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            Platform.exit();
        }
    }

    /**
     * @return bean of a controller assigned to the fxml from given url
     */
    public Object initStage(Stage stage, URL url) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        Parent parent = fxmlLoader.load();
        stage.setScene(new Scene(parent));

        return fxmlLoader.getController();
    }
}
