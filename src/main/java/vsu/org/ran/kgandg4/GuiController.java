package vsu.org.ran.kgandg4;


import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.model.Polygon;
import vsu.org.ran.kgandg4.normals.FaceNormalCalculator;
import vsu.org.ran.kgandg4.normals.NormalCalculator;
import vsu.org.ran.kgandg4.objReader.ObjReader;
import vsu.org.ran.kgandg4.render_engine.CameraManager;
import vsu.org.ran.kgandg4.render_engine.RenderEngine;
import vsu.org.ran.kgandg4.triangulation.SimpleTriangulator;
import vsu.org.ran.kgandg4.triangulation.Triangulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiController {
    @FXML private BorderPane rootPane;

    @FXML
    private Canvas canvas;

    @FXML
    private CameraPanelController cameraPanelController;

    private Model mesh = null;

    private CameraManager cameraManager;

    private Triangulator triangulator;

    private NormalCalculator normalCalculator;

    private Timeline timeline;

    @FXML
    private void initialize() {
        AnchorPane canvasContainer = (AnchorPane) canvas.getParent();
        if (canvasContainer != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty());
            canvas.heightProperty().bind(canvasContainer.heightProperty());
        }
        rootPane.setOnMouseClicked(event -> {
            rootPane.requestFocus();
        });

        cameraManager = new CameraManager(canvas.getWidth(), canvas.getHeight());

        triangulator = new SimpleTriangulator();
//        triangulator = new EarCuttingTriangulator();

        normalCalculator = new FaceNormalCalculator();

        cameraPanelController.setCameraManager(cameraManager);
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                cameraPanelController.setScene(newScene);
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(50), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            cameraManager.getActiveCamera().setAspectRatio((float) (width / height));

            if (mesh != null) {
                try {
                    RenderEngine.render(canvas.getGraphicsContext2D(), cameraManager.getActiveCamera(), mesh, (int) width, (int) height);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                RenderEngine.testZBuffer(canvas.getGraphicsContext2D(), (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        //Для более удобной работы, сразу папка с моделями
        File projectDir = new File(System.getProperty("user.dir"));
        File modelsDir = new File(projectDir, "3DModels");
        fileChooser.setInitialDirectory(modelsDir);


        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);
            triangulator.triangulateModel(mesh);
            normalCalculator.calculateNormals(mesh);

            // todo: обработка ошибок
        } catch (IOException exception) {

        }
    }
}