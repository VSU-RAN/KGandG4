package vsu.org.ran.kgandg4;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.render_engine.render.RenderLoopService;

import java.io.File;
import java.io.IOException;

@Component
public class GuiController {
    @FXML private BorderPane rootPane;
    @FXML private Canvas canvas;
    @FXML private CameraPanelController cameraPanelController;

    @Autowired
    private CameraManager cameraManager;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private FileDialogService fileDialogService;

    @Autowired
    private RenderLoopService renderLoopService;



    @FXML
    private void initialize() {
        setupCanvas();
        setupEventHandlers();
        startRenderLoop();
    }

    private void setupCanvas() {
        AnchorPane canvasContainer = (AnchorPane) canvas.getParent();
        if (canvasContainer != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty());
            canvas.heightProperty().bind(canvasContainer.heightProperty());

            canvasContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    updateRenderSize();
                }
            });

            canvasContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() > 0) {
                    updateRenderSize();
                }
            });
        }
    }

    private void setupEventHandlers() {
        rootPane.setOnMouseClicked(event -> {
            rootPane.requestFocus();
        });

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                cameraPanelController.setScene(newScene);
            }
        });
    }

    private void startRenderLoop() {
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> {
                    renderLoopService.startRenderLoop(
                            canvas.getGraphicsContext2D(),
                            (int) canvas.getWidth(),
                            (int) canvas.getHeight()
                    );
                });
            }
        });
    }

    private void updateRenderSize() {
        Platform.runLater(() -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            if (width > 0 && height > 0) {
                if (renderLoopService.isRunning()) {
                    renderLoopService.updateSize((int) width, (int) height);
                } else {
                    renderLoopService.startRenderLoop(
                            canvas.getGraphicsContext2D(),
                            (int) width,
                            (int) height
                    );
                }
            }
        });
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        fileDialogService.showOpenModelDialog(canvas.getScene().getWindow()).ifPresent(this::loadModel);
            // todo: обработка ошибок
    }

    private void loadModel(File file) {
        try {
            modelManager.loadModel(file);
            //todo: Сделать что то такое
            // showMessage("Model loaded: " + file.getName());

        } catch (IOException e) {
            //todo: Сделать что то такое
            //   showError("Failed to load model",
            //   Cannot load " + file.getName() + ": " + e.getMessage());
        }
    }
}