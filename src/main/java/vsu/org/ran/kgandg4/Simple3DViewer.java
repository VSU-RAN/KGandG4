package vsu.org.ran.kgandg4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import vsu.org.ran.kgandg4.config.AppConfig;
import vsu.org.ran.kgandg4.dependecyIndjection.DIContainer;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import java.io.IOException;
import java.util.Objects;

public class Simple3DViewer extends Application {
    private static DIContainer diContainer;

    @Override
    public void init() throws Exception {
        // Инициализируем DI контейнер
        diContainer = new DIContainer();
        diContainer.initialize();
    }

    @Override
    public void start(Stage stage) throws IOException {
        AppConfig config = diContainer.getBean(AppConfig.class);

        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("gui.fxml")));
        fxmlLoader.setControllerFactory(type -> diContainer.getBean(type));


        BorderPane viewport = fxmlLoader.load();

        Scene scene = new Scene(viewport);

        stage.setMinWidth(config.getWindowWidth());
        stage.setMinHeight(config.getWindowHeight());
        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        stage.setTitle(config.getApplicationName());
        stage.setScene(scene);
        stage.show();
    }
}