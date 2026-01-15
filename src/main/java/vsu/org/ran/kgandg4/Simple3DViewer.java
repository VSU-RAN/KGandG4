package vsu.org.ran.kgandg4;

import java.io.IOException;
import java.util.Objects;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.application.Application;

import vsu.org.ran.kgandg4.config.AppConfig;
import vsu.org.ran.kgandg4.dependecyIndjection.DIContainer;

public class Simple3DViewer extends Application {
    private static DIContainer diContainer;

    @Override
    public void init() throws Exception {
        // Инициализируем DI контейнер
        diContainer = new DIContainer();
        diContainer.initialize();
    }

    public static DIContainer getDiContainer() {
        return diContainer;
    }

    @Override
    public void start(Stage stage) throws IOException {
        AppConfig config = diContainer.getBean(AppConfig.class);

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("gui.fxml")));
        fxmlLoader.setControllerFactory(type -> diContainer.getBean(type));

        VBox viewport = fxmlLoader.load();

        Scene scene = new Scene(viewport);

        stage.setMinWidth(config.getWindowWidth());
        stage.setMinHeight(config.getWindowHeight());
        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        stage.setTitle(config.getApplicationName());
        stage.setScene(scene);
        stage.show();


        viewport.requestFocus();
    }
}