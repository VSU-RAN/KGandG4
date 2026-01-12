package vsu.org.ran.kgandg4;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.render_engine.CameraManager;
import vsu.org.ran.kgandg4.render_engine.RenderEngine;
import vsu.org.ran.kgandg4.render_engine.RenderLoopService;

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
        renderLoopService.startRenderLoop(this::renderFrame);
    }

    private void renderFrame() {
        if (modelManager.getCurrentModel() != null) {
            try {
                double width = canvas.getWidth();
                double height = canvas.getHeight();

                cameraManager.getActiveCamera().setAspectRatio((float) (width / height));
                canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

                RenderEngine.render(
                        canvas.getGraphicsContext2D(),
                        cameraManager.getActiveCamera(),
                        modelManager.getCurrentModel(),
                        (int) width,
                        (int) height
                );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//                RenderEngine.testZBuffer(canvas.getGraphicsContext2D(), (int) width, (int) height);
        }
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