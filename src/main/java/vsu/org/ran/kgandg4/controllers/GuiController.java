package vsu.org.ran.kgandg4.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.normals.FaceNormalCalculator;
import vsu.org.ran.kgandg4.normals.NormalCalculator;
import vsu.org.ran.kgandg4.objTools.ObjReader;
import vsu.org.ran.kgandg4.objTools.ObjWriter;
import vsu.org.ran.kgandg4.render_engine.CameraManager;
import vsu.org.ran.kgandg4.render_engine.RenderEngine;
import vsu.org.ran.kgandg4.triangulation.SimpleTriangulator;
import vsu.org.ran.kgandg4.triangulation.Triangulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiController {
    @FXML private VBox mainContainer;
    @FXML private SplitPane mainSplitPane;
    @FXML private AnchorPane canvasContainer;
    @FXML private VBox rightPanelContainer;
    @FXML private Canvas canvas;

    @FXML private Button modelButton;
    @FXML private Button cameraButton;
    @FXML private Button renderButton;
    @FXML private Button editButton;
    @FXML private Button transformButton;
    @FXML private Button resetButton;

    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuCameraPanel;
    @FXML private MenuItem menuModelPanel;
    @FXML private MenuItem menuCameraTools;
    @FXML private MenuItem menuModelTools;
    @FXML private MenuItem menuRenderSettings;
    @FXML private MenuItem menuEditPanel;
    @FXML private MenuItem menuTransformPanel;
    @FXML private MenuItem menuResetView;
    @FXML private MenuItem menuExit;

    private Parent cameraPanelContainer;
    private Parent modelPanelContainer;
    private Parent renderPanelContainer;
    private Parent editPanelContainer;
    private Parent transformPanelContainer;

    private CameraPanelController cameraPanelController;
    private ModelPanelController modelPanelController;
    private RenderPanelController renderPanelController;
    private EditPanelController editPanelController;
    private TransformPanelController transformPanelController;

    private Model mesh = null;
    private Image loadedTexture = null;
    private CameraManager cameraManager;
    private Triangulator triangulator;
    private NormalCalculator normalCalculator;
    private Timeline timeline;

    private boolean isCameraPanelOpen = false;
    private boolean isModelPanelOpen = false;
    private boolean isRenderPanelOpen = false;
    private boolean isEditPanelOpen = false;
    private boolean isTransformPanelOpen = false;

    // Настройки отображения
    private boolean showWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;

    // Настройки рендеринга
    private String renderMode = "solid";
    private Color faceColor = Color.web("#4a90e2");
    private Color wireframeColor = Color.BLACK;
    private Color lightColor = Color.WHITE;

    // Управление камерой с клавиатуры
    private boolean shiftPressed = false;
    private static final float PAN_SPEED = 0.5f;
    private static final float ORBIT_SPEED = 0.05f;
    private static final float ZOOM_SPEED = 0.5f;
    private float lightIntensity = 1.0f;

    private final String BUTTON_ACTIVE_STYLE =
            "-fx-background-color: #007bff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 500;" +
                    "-fx-padding: 6 14 6 14;" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: #0056b3;";

    private final String BUTTON_NORMAL_STYLE =
            "-fx-background-color: transparent;" +
                    "-fx-text-fill: #495057;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 500;" +
                    "-fx-padding: 6 14 6 14;" +
                    "-fx-border-radius: 4;" +
                    "-fx-background-radius: 4;" +
                    "-fx-cursor: hand;" +
                    "-fx-border-color: transparent;";

    @FXML
    private void initialize() {

        // Назначаем обработчики для MenuItems
        menuOpen.setOnAction(event -> loadModel());
        menuCameraPanel.setOnAction(event -> onToggleCameraPanelClick());
        menuModelPanel.setOnAction(event -> onToggleModelPanelClick());
        menuCameraTools.setOnAction(event -> onCameraToolsClick());
        menuModelTools.setOnAction(event -> onModelToolsClick());
        menuRenderSettings.setOnAction(event -> onToggleRenderPanelClick());
        menuEditPanel.setOnAction(event -> onToggleEditPanelClick());
        menuTransformPanel.setOnAction(event -> onToggleTransformPanelClick());
        menuResetView.setOnAction(event -> onResetViewClick());
        menuExit.setOnAction(event -> onExitClick());

        // Важно! Canvas должен быть привязан к размерам контейнера
        if (canvasContainer != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty());
            canvas.heightProperty().bind(canvasContainer.heightProperty());
        }

        // Настраиваем обработку кликов по канвасу
        setupCanvasClickHandler();

        // Настраиваем обработку клавиатуры для камеры
        setupKeyboardHandlers();

        cameraManager = new CameraManager(canvas.getWidth(), canvas.getHeight());
        triangulator = new SimpleTriangulator();
        normalCalculator = new FaceNormalCalculator();

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(50), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            if (cameraManager.getActiveCamera() != null) {
                cameraManager.getActiveCamera().setAspectRatio((float) (width / height));
            }

            if (mesh != null) {
                try {
                    RenderEngine.render(canvas.getGraphicsContext2D(),
                            cameraManager.getActiveCamera(), mesh,
                            (int) width, (int) height);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void setupCanvasClickHandler() {
        canvas.setOnMouseClicked(event -> {
            if (mesh != null && editPanelController != null && isEditPanelOpen) {
                // Преобразуем координаты клика в нормализованные координаты модели
                double x = (event.getX() / canvas.getWidth()) * 2 - 1;
                double y = -((event.getY() / canvas.getHeight()) * 2 - 1);
                double z = 0; // Для простоты пока используем 0

                editPanelController.handleModelClick(x, y, z);
            }
        });
    }

    private void setupKeyboardHandlers() {
        // Обработка клавиш в самой сцене
        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    KeyCode code = event.getCode();

                    if (code == KeyCode.SHIFT) {
                        shiftPressed = true;
                    }

                    handleCameraKeyPress(code, event.isShiftDown());
                });

                newScene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                    KeyCode code = event.getCode();

                    if (code == KeyCode.SHIFT) {
                        shiftPressed = false;
                    }
                });

                // Фокус на Canvas для гарантии получения событий
                canvas.setFocusTraversable(true);
                canvas.requestFocus();
            }
        });

        // Также обрабатываем клик по канвасу для возврата фокуса
        canvas.setOnMouseClicked(event -> {
            canvas.requestFocus();

            // Обработка клика для редактирования
            if (mesh != null && editPanelController != null && isEditPanelOpen) {
                double x = (event.getX() / canvas.getWidth()) * 2 - 1;
                double y = -((event.getY() / canvas.getHeight()) * 2 - 1);
                double z = 0;
                editPanelController.handleModelClick(x, y, z);
            }
        });
    }

    private void handleCameraKeyPress(KeyCode code, boolean shiftDown) {
        if (cameraManager == null || cameraManager.getActiveCamera() == null) {
            return;
        }

        vsu.org.ran.kgandg4.render_engine.Camera activeCamera = cameraManager.getActiveCamera();

        switch (code) {
            case W:
                if (shiftPressed) {
                    // Shift + W = Pan вперед
                    activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, 0, PAN_SPEED));
                } else {
                    // W = Orbit вверх
                    activeCamera.orbit(0, ORBIT_SPEED);
                }
                break;

            case S:
                if (shiftPressed) {
                    // Shift + S = Pan назад
                    activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, 0, -PAN_SPEED));
                } else {
                    // S = Orbit вниз
                    activeCamera.orbit(0, -ORBIT_SPEED);
                }
                break;

            case A:
                if (shiftPressed) {
                    // Shift + A = Pan влево
                    activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(-PAN_SPEED, 0, 0));
                } else {
                    // A = Orbit влево
                    activeCamera.orbit(ORBIT_SPEED, 0);
                }
                break;

            case D:
                if (shiftPressed) {
                    // Shift + D = Pan вправо
                    activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(PAN_SPEED, 0, 0));
                } else {
                    // D = Orbit вправо
                    activeCamera.orbit(-ORBIT_SPEED, 0);
                }
                break;

            case UP:
                // Стрелка вверх = Pan вперед
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, 0, PAN_SPEED));
                break;

            case DOWN:
                // Стрелка вниз = Pan назад
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, 0, -PAN_SPEED));
                break;

            case LEFT:
                // Стрелка влево = Pan влево
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(-PAN_SPEED, 0, 0));
                break;

            case RIGHT:
                // Стрелка вправо = Pan вправо
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(PAN_SPEED, 0, 0));
                break;

            case Q:
                // Q = Pan вверх
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, PAN_SPEED, 0));
                break;

            case E:
                // E = Pan вниз
                activeCamera.movePositionAndTarget(new javax.vecmath.Vector3f(0, -PAN_SPEED, 0));
                break;

            case EQUALS:
            case ADD:
                // + или = = Zoom In
                activeCamera.zoom(-ZOOM_SPEED);
                break;

            case MINUS:
            case SUBTRACT:
                // - = Zoom Out
                activeCamera.zoom(ZOOM_SPEED);
                break;

            case DIGIT1:
            case NUMPAD1:
                // Переключение на камеру 1
                if (cameraManager.getCameras().size() > 0) {
                    cameraManager.switchToCameraById(0);
                }
                break;

            case DIGIT2:
            case NUMPAD2:
                // Переключение на камеру 2
                if (cameraManager.getCameras().size() > 1) {
                    cameraManager.switchToCameraById(1);
                }
                break;

            case C:
                // Переключение на следующую камеру
                if (!shiftDown) {
                    cameraManager.switchToNextCamera();
                }
                break;
        }
    }

    private void loadCameraPanel() {

        try {
            java.net.URL resourceUrl = getClass().getResource("/camera-panel.fxml");
            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("camera-panel.fxml");
            }
            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/camera-panel.fxml");
            }
            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/controllers/camera-panel.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл camera-panel.fxml. Проверьте путь в проекте.");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            cameraPanelContainer = loader.load();
            cameraPanelController = loader.getController();

            if (cameraPanelController != null) {
                cameraPanelController.setCameraManager(cameraManager);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель камер: " + e.getMessage());
        }
    }

    private void loadModelPanel() {

        try {
            java.net.URL resourceUrl = getClass().getResource("model-panel.fxml");

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/model-panel.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл model-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            modelPanelContainer = loader.load();

            modelPanelController = loader.getController();

            if (modelPanelController != null) {
                modelPanelController.setGuiController(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель моделей: " + e.getMessage());
        }
    }

    private void loadRenderPanel() {

        try {
            java.net.URL resourceUrl = getClass().getResource("render-panel.fxml");

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/render-panel.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл render-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            renderPanelContainer = loader.load();

            renderPanelController = loader.getController();

            if (renderPanelController != null) {
                renderPanelController.setGuiController(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель рендеринга: " + e.getMessage());
        }
    }

    private void loadEditPanel() {

        try {
            java.net.URL resourceUrl = getClass().getResource("edit-panel.fxml");

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/edit-panel.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл edit-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            editPanelContainer = loader.load();

            editPanelController = loader.getController();

            if (editPanelController != null) {
                editPanelController.setGuiController(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель редактирования: " + e.getMessage());
        }
    }

    private void loadTransformPanel() {

        try {
            java.net.URL resourceUrl = getClass().getResource("transform-panel.fxml");

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/transform-panel.fxml");
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл transform-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            transformPanelContainer = loader.load();
            transformPanelController = loader.getController();

            if (transformPanelController != null) {
                transformPanelController.setGuiController(this);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель трансформаций: " + e.getMessage());
        }
    }

    public void setLightIntensity(float intensity) {
        this.lightIntensity = intensity;
    }

    public void setLightColor(Color color) {
        this.lightColor = color;
    }

    public float getLightIntensity() {
        return lightIntensity;
    }

    public Color getLightColor() {
        return lightColor;
    }

    @FXML
    private void onToggleCameraPanelClick() {
        if (isModelPanelOpen) closeModelPanel();
        if (isRenderPanelOpen) closeRenderPanel();
        if (isEditPanelOpen) closeEditPanel();
        if (isTransformPanelOpen) closeTransformPanel();

        togglePanel(cameraPanelContainer, "camera", "камер");
    }

    @FXML
    private void onToggleModelPanelClick() {
        if (isCameraPanelOpen) closeCameraPanel();
        if (isRenderPanelOpen) closeRenderPanel();
        if (isEditPanelOpen) closeEditPanel();
        if (isTransformPanelOpen) closeTransformPanel();

        togglePanel(modelPanelContainer, "model", "моделей");
    }

    @FXML
    private void onToggleRenderPanelClick() {
        if (isCameraPanelOpen) closeCameraPanel();
        if (isModelPanelOpen) closeModelPanel();
        if (isEditPanelOpen) closeEditPanel();
        if (isTransformPanelOpen) closeTransformPanel();

        togglePanel(renderPanelContainer, "render", "рендеринга");
    }

    @FXML
    private void onToggleEditPanelClick() {
        if (isCameraPanelOpen) closeCameraPanel();
        if (isModelPanelOpen) closeModelPanel();
        if (isRenderPanelOpen) closeRenderPanel();
        if (isTransformPanelOpen) closeTransformPanel();

        togglePanel(editPanelContainer, "edit", "редактирования");
    }

    @FXML
    private void onToggleTransformPanelClick() {
        if (isCameraPanelOpen) closeCameraPanel();
        if (isModelPanelOpen) closeModelPanel();
        if (isRenderPanelOpen) closeRenderPanel();
        if (isEditPanelOpen) closeEditPanel();

        togglePanel(transformPanelContainer, "transform", "трансформаций");
    }

    private void togglePanel(Parent panelContainer, String panelType, String panelName) {
        boolean panelExists = (panelContainer != null &&
                rightPanelContainer.getChildren().contains(panelContainer));
        if (panelExists) {
            closePanel(panelContainer, panelType, panelName);
        } else {
            openPanel(panelContainer, panelType, panelName);
        }
    }

    private void openPanel(Parent panelContainer, String panelType, String panelName) {
        if (panelContainer == null) {
            if (panelType.equals("camera")) {
                loadCameraPanel();
                panelContainer = cameraPanelContainer;
            } else if (panelType.equals("model")) {
                loadModelPanel();
                panelContainer = modelPanelContainer;
            } else if (panelType.equals("render")) {
                loadRenderPanel();
                panelContainer = renderPanelContainer;
            } else if (panelType.equals("edit")) {
                loadEditPanel();
                panelContainer = editPanelContainer;
            } else if (panelType.equals("transform")) {
                loadTransformPanel();
                panelContainer = transformPanelContainer;
            }

            if (panelContainer == null) {
                return;
            }
        }

        panelContainer.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #ced4da;" +
                        "-fx-border-width: 0 0 0 1;" +
                        "-fx-padding: 16;"
        );

        if (panelContainer instanceof Region) {
            Region region = (Region) panelContainer;
            region.setPrefWidth(320);
            region.setMinWidth(300);
            region.setMaxWidth(350);
        }

        rightPanelContainer.getChildren().clear();
        rightPanelContainer.getChildren().add(panelContainer);

        rightPanelContainer.setVisible(true);
        rightPanelContainer.setManaged(true);

        mainSplitPane.setDividerPositions(0.75);

        canvas.requestFocus();

        if (panelType.equals("camera")) {
            isCameraPanelOpen = true;
        } else if (panelType.equals("model")) {
            isModelPanelOpen = true;
        } else if (panelType.equals("render")) {
            isRenderPanelOpen = true;
        } else if (panelType.equals("edit")) {
            isEditPanelOpen = true;
        } else if (panelType.equals("transform")) {
            isTransformPanelOpen = true;
        }
        updateButtonStyles();

        if (panelType.equals("model") && modelPanelController != null) {
            modelPanelController.updateModelInfo();
        }

        if (panelType.equals("render") && renderPanelController != null) {
            renderPanelController.updateSettings(renderMode, faceColor);
        }

        Platform.runLater(() -> {
        });
    }

    private void closePanel(Parent panelContainer, String panelType, String panelName) {
        rightPanelContainer.getChildren().remove(panelContainer);
        rightPanelContainer.setVisible(false);
        rightPanelContainer.setManaged(false);

        mainSplitPane.setDividerPositions(1.0);
        canvas.requestFocus();

        if (panelType.equals("camera")) {
            isCameraPanelOpen = false;
        } else if (panelType.equals("model")) {
            isModelPanelOpen = false;
        } else if (panelType.equals("render")) {
            isRenderPanelOpen = false;
        } else if (panelType.equals("edit")) {
            isEditPanelOpen = false;
        } else if (panelType.equals("transform")) {
            isTransformPanelOpen = false;
        }
        updateButtonStyles();
    }

    private void closeCameraPanel() {
        if (cameraPanelContainer != null && rightPanelContainer.getChildren().contains(cameraPanelContainer)) {
            closePanel(cameraPanelContainer, "camera", "камер");
        }
    }

    private void closeModelPanel() {
        if (modelPanelContainer != null && rightPanelContainer.getChildren().contains(modelPanelContainer)) {
            closePanel(modelPanelContainer, "model", "моделей");
        }
    }

    private void closeRenderPanel() {
        if (renderPanelContainer != null && rightPanelContainer.getChildren().contains(renderPanelContainer)) {
            closePanel(renderPanelContainer, "render", "рендеринга");
        }
    }

    private void closeEditPanel() {
        if (editPanelContainer != null && rightPanelContainer.getChildren().contains(editPanelContainer)) {
            closePanel(editPanelContainer, "edit", "редактирования");
        }
    }

    private void closeTransformPanel() {
        if (transformPanelContainer != null && rightPanelContainer.getChildren().contains(transformPanelContainer)) {
            closePanel(transformPanelContainer, "transform", "трансформаций");
        }
    }

    private void updateButtonStyles() {
        if (modelButton != null) {
            modelButton.setStyle(isModelPanelOpen ? BUTTON_ACTIVE_STYLE : BUTTON_NORMAL_STYLE);
        }
        if (cameraButton != null) {
            cameraButton.setStyle(isCameraPanelOpen ? BUTTON_ACTIVE_STYLE : BUTTON_NORMAL_STYLE);
        }
        if (renderButton != null) {
            renderButton.setStyle(isRenderPanelOpen ? BUTTON_ACTIVE_STYLE : BUTTON_NORMAL_STYLE);
        }
        if (editButton != null) {
            editButton.setStyle(isEditPanelOpen ? BUTTON_ACTIVE_STYLE : BUTTON_NORMAL_STYLE);
        }
        if (transformButton != null) {
            transformButton.setStyle(isTransformPanelOpen ? BUTTON_ACTIVE_STYLE : BUTTON_NORMAL_STYLE);
        }
    }

    @FXML
    private void onModelPanelButtonClick() {
        onToggleModelPanelClick();
    }

    @FXML
    private void onCameraPanelButtonClick() {
        onToggleCameraPanelClick();
    }

    @FXML
    private void onRenderPanelButtonClick() {
        onToggleRenderPanelClick();
    }

    @FXML
    private void onEditPanelButtonClick() {
        onToggleEditPanelClick();
    }

    @FXML
    private void onTransformPanelButtonClick() {
        onToggleTransformPanelClick();
    }

    @FXML
    private void onResetViewButtonClick() {
        onResetViewClick();
    }

    public void loadModel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Модель (*.obj)", "*.obj"));
        fileChooser.setTitle("Загрузить модель");

        File file = fileChooser.showOpenDialog((Stage) mainContainer.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            mesh = ObjReader.read(fileContent);

            int polygonsWithTexture = 0;
            for (var polygon : mesh.polygons) {
                if (polygon.getTextureVertexIndices().size() > 0) {
                    polygonsWithTexture++;
                }
            }
            normalizeModel(mesh);

            triangulator.triangulateModel(mesh);
            normalCalculator.calculateNormals(mesh);

            showInfoDialog("Успех", "Модель успешно загружена: " + file.getName());

            if (modelPanelController != null) {
                modelPanelController.updateModelInfo();
            }

            if (editPanelController != null) {
                editPanelController.updateModelStats();
            }

        } catch (IOException exception) {
            showErrorDialog("Ошибка загрузки модели", exception.getMessage());
        } catch (Exception e) {
            showErrorDialog("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadTexture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"),
                new FileChooser.ExtensionFilter("Все файлы", "*.*")
        );
        fileChooser.setTitle("Загрузить текстуру");

        File file = fileChooser.showOpenDialog((Stage) mainContainer.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String filePath = file.toURI().toString();
            // Загружаем текстуру с фоновой загрузкой
            Image textureImage = new Image(filePath, true);

            // Добавляем слушатель для отслеживания загрузки
            textureImage.errorProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> {
                        showErrorDialog("Ошибка", "Не удалось загрузить текстуру: " +
                                textureImage.getException().getMessage());
                    });
                }
            });

            textureImage.progressProperty().addListener((obs, oldVal, newVal) -> {
                // Когда загрузка завершена
                if (newVal.doubleValue() == 1.0 && !textureImage.isError()) {
                    Platform.runLater(() -> {

                        // Прямое обновление превью в ModelPanelController
                        if (modelPanelController != null) {
                            modelPanelController.updateTexturePreview(textureImage);
                        }

                        showInfoDialog("Успех", "Текстура загружена: " + file.getName());
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Ошибка загрузки текстуры", e.getMessage());
        }
    }

    public void updateTexturePreview() {
        if (modelPanelController != null && loadedTexture != null) {
            modelPanelController.updateTexturePreview(loadedTexture);
        }
    }

    public void saveModel() {
        if (mesh == null) {
            showErrorDialog("Ошибка", "Нет модели для сохранения!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Модель (*.obj)", "*.obj"));
        fileChooser.setTitle("Сохранить модель");
        fileChooser.setInitialFileName("model.obj");

        File file = fileChooser.showSaveDialog((Stage) mainContainer.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            // Проверяем, нужно ли сохранять с трансформациями
            Model modelToSave = mesh;

            if (transformPanelController != null && !transformPanelController.isSaveOriginal()) {
                // Если выбран режим "С трансформациями", применяем трансформации
                modelToSave = getTransformedModel();
            }

            // Используем ObjWriter для сохранения
            ObjWriter.write(modelToSave, file.toPath());

            showInfoDialog("Успех", "Модель успешно сохранена: " + file.getName());
        } catch (IOException e) {
            showErrorDialog("Ошибка сохранения", "Не удалось сохранить файл: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showErrorDialog("Ошибка", "Произошла ошибка при сохранении: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Model getTransformedModel() {
        if (mesh == null) return null;

        // Создаем копию модели
        Model transformed = new Model();

        // Получаем параметры трансформаций из TransformPanelController
        float tx = 0, ty = 0, tz = 0;
        float rx = 0, ry = 0, rz = 0;
        float sx = 1, sy = 1, sz = 1;

        if (transformPanelController != null) {
            // Здесь нужно получить значения из слайдеров TransformPanelController
        }

        // Применяем трансформации к каждой вершине
        for (Vector3f vertex : mesh.vertices) {
            // Сначала масштабирование
            float x = vertex.getX() * sx;
            float y = vertex.getY() * sy;
            float z = vertex.getZ() * sz;

            // Затем поворот
            // TODO: Реализовать поворот

            // Затем перемещение
            x += tx;
            y += ty;
            z += tz;

            transformed.vertices.add(new Vector3f(x, y, z));
        }

        // Копируем остальные данные без изменений
        transformed.textureVertices.addAll(mesh.textureVertices);
        transformed.normals.addAll(mesh.normals);
        transformed.polygons.addAll(mesh.polygons);

        return transformed;
    }

    // Методы для управления настройками отображения (обновлены)
    public void updateDisplaySettings(boolean wireframe, boolean texture, boolean lighting) {
        this.showWireframe = wireframe;
        this.useTexture = texture;
        this.useLighting = lighting;
    }

    public void setRenderMode(String mode) {
        this.renderMode = mode.toLowerCase();
    }

    public void setFaceColor(Color color) {
        this.faceColor = color;
    }

    public void setWireframeColor(Color color) {
        this.wireframeColor = color;
    }

    @FXML
    private void onResetViewClick() {
        if (cameraManager != null && cameraManager.getActiveCamera() != null) {
            cameraManager.getActiveCamera().setPosition(new javax.vecmath.Vector3f(0, 0, 20));
            cameraManager.getActiveCamera().setTarget(new javax.vecmath.Vector3f(0, 0, 0));
            showInfoDialog("Сброс вида", "Камера сброшена к начальному положению.");
        }
    }

    @FXML
    private void onCameraToolsClick() {
        showInfoDialog("Инструменты камер", "Диалог инструментов камер еще не реализован.");
    }

    @FXML
    private void onModelToolsClick() {
        showInfoDialog("Инструменты моделей", "Диалог инструментов моделей еще не реализован.");
    }

    @FXML
    private void onExitClick() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    private void normalizeModel(Model mesh) {
        if (mesh.vertices.isEmpty()) return;

        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

        for (Vector3f vertex : mesh.vertices) {
            minX = Math.min(minX, vertex.getX());
            maxX = Math.max(maxX, vertex.getX());
            minY = Math.min(minY, vertex.getY());
            maxY = Math.max(maxY, vertex.getY());
            minZ = Math.min(minZ, vertex.getZ());
            maxZ = Math.max(maxZ, vertex.getZ());
        }

        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float centerZ = (minZ + maxZ) / 2;

        float sizeX = maxX - minX;
        float sizeY = maxY - minY;
        float sizeZ = maxZ - minZ;
        float maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        if (maxSize < 0.0001f) return;

        float scale = 2.0f / maxSize;

        for (int i = 0; i < mesh.vertices.size(); i++) {
            Vector3f vertex = mesh.vertices.get(i);
            float x = (vertex.getX() - centerX) * scale;
            float y = (vertex.getY() - centerY) * scale;
            float z = (vertex.getZ() - centerZ) * scale;
            mesh.vertices.set(i, new Vector3f(x, y, z));
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void refreshModel() {
        if (mesh != null) {
            if (modelPanelController != null) {
                modelPanelController.updateModelInfo();
            }

            if (editPanelController != null) {
                editPanelController.updateModelStats();
            }
        }
    }

    public Model getMesh() {
        return mesh;
    }

    public void setMesh(Model mesh) {
        this.mesh = mesh;
    }

    public ModelPanelController getModelPanelController() {
        return modelPanelController;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    // Геттеры для настроек отображения (обновлены)
    public boolean isShowWireframe() {
        return showWireframe;
    }

    public boolean isUseTexture() {
        return useTexture;
    }

    public boolean isUseLighting() {
        return useLighting;
    }

    public String getRenderMode() {
        return renderMode;
    }

    public Color getFaceColor() {
        return faceColor;
    }

    public Color getWireframeColor() {
        return wireframeColor;
    }
}