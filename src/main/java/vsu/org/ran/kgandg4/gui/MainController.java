package vsu.org.ran.kgandg4.gui;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;

import math.matrix.Matrix4f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.gui.controllers.*;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.GraphicConveyor;
import vsu.org.ran.kgandg4.render_engine.render.RenderContext;
import vsu.org.ran.kgandg4.render_engine.render.RenderLoopService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.render_engine.render.Scene;

@Component
public class MainController {
    @FXML private VBox mainContainer;
    @FXML private SplitPane mainSplitPane;
    @FXML private VBox rightPanelContainer;
    @FXML private Canvas canvas;

    @FXML private Button modelButton;
    @FXML private Button cameraButton;
    @FXML private Button renderButton;
    @FXML private Button editButton;
    @FXML private Button transformButton;
    @FXML private Button resetButton;

    @FXML private MenuItem menuCameraPanel;
    @FXML private MenuItem menuModelPanel;
    @FXML private MenuItem menuCameraTools;
    @FXML private MenuItem menuModelTools;
    @FXML private MenuItem menuRenderSettings;
    @FXML private MenuItem menuEditPanel;
    @FXML private MenuItem menuTransformPanel;
    @FXML private MenuItem menuResetView;
    @FXML private MenuItem menuExit;


    @Autowired
    private CameraManager cameraManager;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private RenderLoopService renderLoopService;

    @Autowired
    private PanelManager panelManager;

    @Autowired
    private KeyboardAndMouseController keyboardAndMouseController;

    @Autowired
    private AlertService alertService;

    @Autowired
    private Scene scene;


    @Autowired
    private TransformPanelController transformPanelController;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            panelManager.initializeMainWindow(mainContainer.getScene().getWindow());
            keyboardAndMouseController.attachToScene(mainContainer.getScene());
        });

        panelManager.preloadAll();

        setupMenuHandlers();

        setupCanvas();
        setupEventHandlers();
        setupCanvasClickHandler();

        setupTransformPanel();  // Настройка TransformPanelController

        startRenderLoop();
    }


    private void setupMenuHandlers() {
        menuCameraPanel.setOnAction(event -> togglePanel("camera"));
        menuModelPanel.setOnAction(event -> togglePanel("model"));
        menuCameraTools.setOnAction(event -> onCameraToolsClick());
        menuModelTools.setOnAction(event -> onModelToolsClick());
        menuRenderSettings.setOnAction(event -> togglePanel("render"));
        menuEditPanel.setOnAction(event -> togglePanel("edit"));
        menuTransformPanel.setOnAction(event -> togglePanel("transform"));
        menuResetView.setOnAction(event -> onResetViewClick());
        menuExit.setOnAction(event -> onExitClick());
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
        keyboardAndMouseController.attachToCanvas(canvas);

        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                keyboardAndMouseController.attachToScene(newScene);
            }
        });

        mainContainer.setOnMouseClicked(event -> {
            mainContainer.requestFocus();
        });
    }



    private void setupCanvasClickHandler() {
        canvas.setOnMouseClicked(event -> {
            if (modelManager.getCurrentModel() != null) {
                if (panelManager.isPanelOpen("edit")) {
                    EditPanelController editController = panelManager.getController("edit", EditPanelController.class);

                    float screenX = (float)event.getX();
                    float screenY = (float)event.getY();

                    float z = scene.getZbuffer().readDepth(screenX, screenY);

                    if (z >= 0.999f) {
                        System.out.println("  -> Empty click");
                        editController.clearSelection();
                        return;
                    }

                    Camera camera = scene.getActiveCamera();
                    int width = (int)canvas.getWidth();
                    int height = (int)canvas.getHeight();

                    Matrix4f viewMatrix = camera.getViewMatrix();
                    Matrix4f projectionMatrix = camera.getProjectionMatrix();
                    Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
                    float near = camera.getNearPlane();
                    float far = camera.getFarPlane();

                    Vector3f modelCoords = GraphicConveyor.screenToModel(
                            screenX, screenY, z,
                            width, height,
                            viewMatrix, projectionMatrix, modelMatrix,
                            near, far
                    );

                    if (modelCoords != null) {
                        editController.handleModelClick(modelCoords);
                    }
                }
            }
        });
    }


    private void setupTransformPanel() {
//         Связка с текущей моделью
//        transformPanelController.setCurrentModel(modelManager.getCurrentModel());

        // Установка callback для перерисовки
        transformPanelController.setOnTransformChanged(() -> {
            // RenderLoopService работает в цикле, поэтому изменения
            // автоматически подхватятся в следующем кадре
            System.out.println("Transformation changed - will be rendered in next frame");
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



    private void togglePanel(String panelId) {
        panelManager.togglePanel(panelId, rightPanelContainer);

        if (panelManager.isPanelOpen(panelId)) {
            mainSplitPane.setDividerPositions(0.75);

            if (panelId.equals("transform")) {
                if (modelManager != null) {
                    Model currentModel = modelManager.getCurrentModel();
                    transformPanelController.setCurrentModel(currentModel);

                    if (currentModel == null) {
                        System.out.println("Warning: No model selected for transformation");
                    } else {
                        System.out.println("Transform panel opened for model: " + currentModel.getName());
                    }
                }
            }
        } else {
            mainSplitPane.setDividerPositions(1.0);
        }

        canvas.requestFocus();

        updateButtonStyles();
    }


    private void updateButtonStyles() {
        modelButton.setStyle(panelManager.isPanelOpen("model") ? ConstantsAndStyles.ACTIVE : ConstantsAndStyles.NORMAL);
        cameraButton.setStyle(panelManager.isPanelOpen("camera") ? ConstantsAndStyles.ACTIVE : ConstantsAndStyles.NORMAL);
        renderButton.setStyle(panelManager.isPanelOpen("render") ? ConstantsAndStyles.ACTIVE : ConstantsAndStyles.NORMAL);
        editButton.setStyle(panelManager.isPanelOpen("edit") ? ConstantsAndStyles.ACTIVE : ConstantsAndStyles.NORMAL);
        transformButton.setStyle(panelManager.isPanelOpen("transform") ? ConstantsAndStyles.ACTIVE : ConstantsAndStyles.NORMAL);
    }


    @FXML
    private void onModelPanelButtonClick() {
        togglePanel("model");
    }

    @FXML
    private void onCameraPanelButtonClick() {
        togglePanel("camera");
    }

    @FXML
    private void onRenderPanelButtonClick() {
        togglePanel("render");
    }

    @FXML
    private void onEditPanelButtonClick() {
        togglePanel("edit");
    }

    @FXML
    private void onTransformPanelButtonClick() {
        togglePanel("transform");
    }

    @FXML
    private void onResetViewButtonClick() {
        onResetViewClick();
    }

    @FXML
    private void onResetViewClick() {
        if (cameraManager != null && cameraManager.getActiveCamera() != null) {
            cameraManager.getActiveCamera().setPosition(new Vector3f(0, 0, 20));
            cameraManager.getActiveCamera().setTarget(new Vector3f(0, 0, 0));
            alertService.showInfo("Сброс вида", "Камера сброшена к начальному положению.");
        }
    }

    @FXML
    private void onCameraToolsClick() {
        alertService.showInfo("Инструменты камер", "Диалог инструментов камер еще не реализован.");
    }

    @FXML
    private void onModelToolsClick() {
        alertService.showInfo("Инструменты моделей", "Диалог инструментов моделей еще не реализован.");
    }

    @FXML
    private void onExitClick() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }


}