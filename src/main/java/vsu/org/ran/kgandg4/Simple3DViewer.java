package vsu.org.ran.kgandg4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Simple3DViewer extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Исправлено: загружаем как VBox вместо BorderPane
        VBox viewport = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("gui.fxml")));

        Scene scene = new Scene(viewport);

        // Устанавливаем достаточно большой начальный размер
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        // Начальный размер с учетом будущей панели
        stage.setWidth(1600);
        stage.setHeight(900);

        viewport.prefWidthProperty().bind(scene.widthProperty());
        viewport.prefHeightProperty().bind(scene.heightProperty());

        stage.setTitle("Simple3DViewer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}