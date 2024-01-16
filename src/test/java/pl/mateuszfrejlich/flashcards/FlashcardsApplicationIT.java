package pl.mateuszfrejlich.flashcards;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import pl.mateuszfrejlich.flashcards.controller.BoxController;
import pl.mateuszfrejlich.flashcards.controller.MainController;
import pl.mateuszfrejlich.flashcards.controller.OptionsController;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FlashcardsApplication.class)
@TestPropertySource(locations = "classpath:application-integration-test.properties")
@MockBeans({@MockBean(MainController.class), @MockBean(OptionsController.class), @MockBean(BoxController.class)})
class FlashcardsApplicationIT {
    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
            SpringApplicationBuilder builder = new SpringApplicationBuilder(FlashcardsApplication.class);
            ConfigurableApplicationContext context = builder.run();
            context.close();
        });
    }
}
