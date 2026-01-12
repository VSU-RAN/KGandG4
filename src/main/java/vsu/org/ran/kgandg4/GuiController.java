package vsu.org.ran.kgandg4;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import vsu.org.ran.kgandg4.controllers.ModelPanelController;
import vsu.org.ran.kgandg4.controllers.RenderPanelController;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.math.Vector3f;
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
    @FXML private VBox mainContainer;
    @FXML private SplitPane mainSplitPane;
    @FXML private AnchorPane canvasContainer;
    @FXML private VBox rightPanelContainer;
    @FXML private Canvas canvas;

    @FXML private Button modelButton;
    @FXML private Button cameraButton;
    @FXML private Button renderButton;
    @FXML private Button resetButton;

    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuCameraPanel;
    @FXML private MenuItem menuModelPanel;
    @FXML private MenuItem menuCameraTools;
    @FXML private MenuItem menuModelTools;
    @FXML private MenuItem menuRenderSettings;
    @FXML private MenuItem menuResetView;
    @FXML private MenuItem menuExit;

    private Parent cameraPanelContainer;
    private Parent modelPanelContainer;
    private Parent renderPanelContainer;
    private CameraPanelController cameraPanelController;
    private ModelPanelController modelPanelController;
    private RenderPanelController renderPanelController;

    private Model mesh = null;
    private CameraManager cameraManager;
    private Triangulator triangulator;
    private NormalCalculator normalCalculator;
    private Timeline timeline;

    private boolean isCameraPanelOpen = false;
    private boolean isModelPanelOpen = false;
    private boolean isRenderPanelOpen = false;

    // Настройки отображения
    private boolean showWireframe = false;
    private boolean showFaces = true;
    private boolean showNormals = false;
    private boolean showTexture = false;

    // Настройки рендеринга
    private String renderMode = "solid"; // "solid", "texture"
    private Color faceColor = Color.web("#4a90e2");
    private Color wireframeColor = Color.BLACK;

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
        System.out.println("=== Инициализация GuiController ===");

        // Назначаем обработчики для MenuItems
        menuOpen.setOnAction(event -> loadModel());
        menuCameraPanel.setOnAction(event -> onToggleCameraPanelClick());
        menuModelPanel.setOnAction(event -> onToggleModelPanelClick());
        menuCameraTools.setOnAction(event -> onCameraToolsClick());
        menuModelTools.setOnAction(event -> onModelToolsClick());
        menuRenderSettings.setOnAction(event -> onToggleRenderPanelClick());
        menuResetView.setOnAction(event -> onResetViewClick());
        menuExit.setOnAction(event -> onExitClick());

        // Важно! Canvas должен быть привязан к размерам контейнера
        if (canvasContainer != null) {
            canvas.widthProperty().bind(canvasContainer.widthProperty());
            canvas.heightProperty().bind(canvasContainer.heightProperty());
            System.out.println("Canvas привязан к контейнеру: " + canvasContainer);
        }

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
                    // TODO: Передать настройки отображения в RenderEngine
                    // RenderEngine.render(canvas.getGraphicsContext2D(),
                    //     cameraManager.getActiveCamera(), mesh,
                    //     (int) width, (int) height,
                    //     showWireframe, showFaces, showNormals, showTexture,
                    //     renderMode, faceColor, wireframeColor);

                    RenderEngine.render(canvas.getGraphicsContext2D(),
                            cameraManager.getActiveCamera(), mesh,
                            (int) width, (int) height);

                } catch (Exception e) {
                    System.err.println("Ошибка рендеринга: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();
    }

    private void loadCameraPanel() {
        System.out.println("=== Загрузка панели камер из FXML ===");

        try {
            java.net.URL resourceUrl = getClass().getResource("camera-panel.fxml");
            System.out.println("URL ресурса: " + resourceUrl);

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/camera-panel.fxml");
                System.out.println("URL ресурса (второй попыткой): " + resourceUrl);
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл camera-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            cameraPanelContainer = loader.load();
            System.out.println("FXML загружен успешно!");

            cameraPanelController = loader.getController();
            System.out.println("Контроллер получен: " + (cameraPanelController != null));

            if (cameraPanelController != null) {
                cameraPanelController.setCameraManager(cameraManager);
                cameraPanelController.setScene(mainContainer.getScene());
                System.out.println("✓ Панель камер успешно загружена из FXML!");
            }

        } catch (Exception e) {
            System.err.println("✗ Ошибка загрузки панели камер из FXML:");
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель камер: " + e.getMessage());
        }
    }

    private void loadModelPanel() {
        System.out.println("=== Загрузка панели моделей из FXML ===");

        try {
            java.net.URL resourceUrl = getClass().getResource("model-panel.fxml");
            System.out.println("URL ресурса: " + resourceUrl);

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/model-panel.fxml");
                System.out.println("URL ресурса (второй попыткой): " + resourceUrl);
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл model-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            modelPanelContainer = loader.load();
            System.out.println("FXML загружен успешно!");

            modelPanelController = loader.getController();
            System.out.println("Контроллер панели моделей получен: " + (modelPanelController != null));

            if (modelPanelController != null) {
                modelPanelController.setGuiController(this);
                System.out.println("✓ Панель моделей успешно загружена из FXML!");
            }

        } catch (Exception e) {
            System.err.println("✗ Ошибка загрузки панели моделей из FXML:");
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель моделей: " + e.getMessage());
        }
    }

    private void loadRenderPanel() {
        System.out.println("=== Загрузка панели рендеринга из FXML ===");

        try {
            java.net.URL resourceUrl = getClass().getResource("render-panel.fxml");
            System.out.println("URL ресурса: " + resourceUrl);

            if (resourceUrl == null) {
                resourceUrl = getClass().getResource("/vsu/org/ran/kgandg4/render-panel.fxml");
                System.out.println("URL ресурса (второй попыткой): " + resourceUrl);
            }

            if (resourceUrl == null) {
                throw new IOException("Не найден файл render-panel.fxml");
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            renderPanelContainer = loader.load();
            System.out.println("FXML загружен успешно!");

            renderPanelController = loader.getController();
            System.out.println("Контроллер панели рендеринга получен: " + (renderPanelController != null));

            if (renderPanelController != null) {
                renderPanelController.setGuiController(this);
                System.out.println("✓ Панель рендеринга успешно загружена из FXML!");
            }

        } catch (Exception e) {
            System.err.println("✗ Ошибка загрузки панели рендеринга из FXML:");
            e.printStackTrace();
            showErrorDialog("Ошибка", "Не удалось загрузить панель рендеринга: " + e.getMessage());
        }
    }

    @FXML
    private void onToggleCameraPanelClick() {
        System.out.println("=== Нажато: Панель камер ===");

        // Закрываем другие панели
        if (isModelPanelOpen) closeModelPanel();
        if (isRenderPanelOpen) closeRenderPanel();

        togglePanel(cameraPanelContainer, "camera", "камер");
    }

    @FXML
    private void onToggleModelPanelClick() {
        System.out.println("=== Нажато: Панель моделей ===");

        // Закрываем другие панели
        if (isCameraPanelOpen) closeCameraPanel();
        if (isRenderPanelOpen) closeRenderPanel();

        togglePanel(modelPanelContainer, "model", "моделей");
    }

    @FXML
    private void onToggleRenderPanelClick() {
        System.out.println("=== Нажато: Панель рендеринга ===");

        // Закрываем другие панели
        if (isCameraPanelOpen) closeCameraPanel();
        if (isModelPanelOpen) closeModelPanel();

        togglePanel(renderPanelContainer, "render", "рендеринга");
    }

    private void togglePanel(Parent panelContainer, String panelType, String panelName) {
        System.out.println("  Детей в правой панели: " + rightPanelContainer.getChildren().size());

        boolean panelExists = (panelContainer != null &&
                rightPanelContainer.getChildren().contains(panelContainer));

        System.out.println("  Панель " + panelName + " найдена? " + panelExists);

        if (panelExists) {
            closePanel(panelContainer, panelType, panelName);
        } else {
            openPanel(panelContainer, panelType, panelName);
        }
    }

    private void openPanel(Parent panelContainer, String panelType, String panelName) {
        // Загружаем панель если она еще не загружена
        if (panelContainer == null) {
            System.out.println("Панель " + panelName + " не загружена, загружаем...");
            if (panelType.equals("camera")) {
                loadCameraPanel();
                panelContainer = cameraPanelContainer;
            } else if (panelType.equals("model")) {
                loadModelPanel();
                panelContainer = modelPanelContainer;
            } else if (panelType.equals("render")) {
                loadRenderPanel();
                panelContainer = renderPanelContainer;
            }

            if (panelContainer == null) {
                System.err.println("Не удалось загрузить панель " + panelName + "!");
                return;
            }
        }

        // Устанавливаем нормальные стили
        panelContainer.setStyle(
                "-fx-background-color: #ffffff;" +  // Белый фон
                        "-fx-border-color: #ced4da;" +      // Серая рамка
                        "-fx-border-width: 0 0 0 1;" +      // Только левая рамка
                        "-fx-padding: 16;"
        );

        // Устанавливаем фиксированную ширину
        if (panelContainer instanceof Region) {
            Region region = (Region) panelContainer;
            region.setPrefWidth(320);
            region.setMinWidth(300);
            region.setMaxWidth(350);
            System.out.println("✓ Ширина панели установлена: pref=320, min=300, max=350");
        }

        // Очищаем правую панель и добавляем нужную панель
        rightPanelContainer.getChildren().clear();
        rightPanelContainer.getChildren().add(panelContainer);

        // Показываем правую панель
        rightPanelContainer.setVisible(true);
        rightPanelContainer.setManaged(true);

        // Устанавливаем разделитель
        mainSplitPane.setDividerPositions(0.75);

        // Устанавливаем флаг и обновляем кнопки
        if (panelType.equals("camera")) {
            isCameraPanelOpen = true;
        } else if (panelType.equals("model")) {
            isModelPanelOpen = true;
        } else if (panelType.equals("render")) {
            isRenderPanelOpen = true;
        }
        updateButtonStyles();

        // Если это панель моделей, обновляем информацию о модели
        if (panelType.equals("model") && modelPanelController != null) {
            modelPanelController.updateModelInfo();
        }

        // Если это панель рендеринга, передаем текущие настройки
        if (panelType.equals("render") && renderPanelController != null) {
            renderPanelController.updateSettings(renderMode, faceColor);
        }

        System.out.println("✓ Панель " + panelName + " добавлена в правую панель");
        System.out.println("✓ SplitPane установлен на 75%/25%");

        // Проверяем
        javafx.application.Platform.runLater(() -> {
            System.out.println("После добавления:");
            System.out.println("  Размер SplitPane: " + mainSplitPane.getWidth() + "x" + mainSplitPane.getHeight());
            System.out.println("  Размер правой панели: " + rightPanelContainer.getWidth() + "x" + rightPanelContainer.getHeight());
        });
    }

    private void closePanel(Parent panelContainer, String panelType, String panelName) {
        // Удаляем панель из правого контейнера
        rightPanelContainer.getChildren().remove(panelContainer);
        System.out.println("✓ Панель " + panelName + " удалена из правой панели");

        // Скрываем правую панель
        rightPanelContainer.setVisible(false);
        rightPanelContainer.setManaged(false);

        // Устанавливаем разделитель на 1.0 (Canvas занимает всю ширину)
        mainSplitPane.setDividerPositions(1.0);
        System.out.println("✓ SplitPane установлен на полную ширину Canvas");

        // Сбрасываем флаг и обновляем кнопки
        if (panelType.equals("camera")) {
            isCameraPanelOpen = false;
        } else if (panelType.equals("model")) {
            isModelPanelOpen = false;
        } else if (panelType.equals("render")) {
            isRenderPanelOpen = false;
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
        System.out.println("✓ Стили кнопок обновлены: Модель=" + isModelPanelOpen +
                ", Камеры=" + isCameraPanelOpen +
                ", Рендеринг=" + isRenderPanelOpen);
    }

    @FXML
    private void onModelPanelButtonClick() {
        System.out.println("Нажато: Кнопка Модель");
        onToggleModelPanelClick();
    }

    @FXML
    private void onCameraPanelButtonClick() {
        System.out.println("Нажато: Кнопка Камеры");
        onToggleCameraPanelClick();
    }

    @FXML
    private void onRenderPanelButtonClick() {
        System.out.println("Нажато: Кнопка Рендеринг");
        onToggleRenderPanelClick();
    }

    @FXML
    private void onResetViewButtonClick() {
        System.out.println("Нажато: Кнопка Сбросить вид");
        onResetViewClick();
    }

    // Метод для загрузки модели
    public void loadModel() {
        System.out.println("Загрузка модели...");

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

            System.out.println("Модель загружена:");
            System.out.println("  Вершин: " + mesh.vertices.size());
            System.out.println("  Полигонов: " + mesh.polygons.size());
            System.out.println("  Нормалей: " + mesh.normals.size());
            System.out.println("  Текстурных координат: " + mesh.textureVertices.size());

            // Проверим, есть ли у полигонов текстурные координаты
            int polygonsWithTexture = 0;
            for (var polygon : mesh.polygons) {
                if (polygon.getTextureVertexIndices().size() > 0) {
                    polygonsWithTexture++;
                }
            }
            System.out.println("  Полигонов с текстурными координатами: " + polygonsWithTexture + " из " + mesh.polygons.size());

            // Нормализуем модель
            normalizeModel(mesh);

            triangulator.triangulateModel(mesh);
            normalCalculator.calculateNormals(mesh);

            System.out.println("✓ Модель триангулирована. Полигонов: " + mesh.polygons.size());

            showInfoDialog("Успех", "Модель успешно загружена: " + file.getName());

            // Обновляем информацию в панели моделей, если она открыта
            if (modelPanelController != null) {
                modelPanelController.updateModelInfo();
            }

        } catch (IOException exception) {
            showErrorDialog("Ошибка загрузки модели", exception.getMessage());
        } catch (Exception e) {
            showErrorDialog("Ошибка", e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для загрузки текстуры
    public void loadTexture() {
        System.out.println("Загрузка текстуры...");

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
            Image textureImage = new Image(file.toURI().toString());

            if (mesh != null) {
                mesh.texture = textureImage;
                System.out.println("Текстура сохранена в модель: " +
                        textureImage.getWidth() + "x" + textureImage.getHeight());
            }

            System.out.println("✓ Текстура загружена: " + file.getAbsolutePath());
            showInfoDialog("Успех", "Текстура загружена: " + file.getName());

            // Обновляем информацию в панели моделей
            if (modelPanelController != null) {
                modelPanelController.updateModelInfo();
            }

        } catch (Exception e) {
            showErrorDialog("Ошибка загрузки текстуры", e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для сохранения модели
    public void saveModel() {
        if (mesh == null) {
            showErrorDialog("Ошибка", "Нет модели для сохранения!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Модель (*.obj)", "*.obj"));
        fileChooser.setTitle("Сохранить модель");

        File file = fileChooser.showSaveDialog((Stage) mainContainer.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            // TODO: Реализовать сохранение модели в OBJ формат
            showInfoDialog("Успех", "Модель сохранена: " + file.getName());
        } catch (Exception e) {
            showErrorDialog("Ошибка сохранения", e.getMessage());
        }
    }

    // Методы для управления настройками отображения
    public void updateDisplaySettings(boolean wireframe, boolean faces, boolean normals, boolean texture) {
        this.showWireframe = wireframe;
        this.showFaces = faces;
        this.showNormals = normals;
        this.showTexture = texture;
        System.out.println("Настройки отображения обновлены:");
        System.out.println("  Каркас: " + showWireframe);
        System.out.println("  Грани: " + showFaces);
        System.out.println("  Нормали: " + showNormals);
        System.out.println("  Текстура: " + showTexture);
    }

    public void setRenderMode(String mode) {
        this.renderMode = mode.toLowerCase();
        System.out.println("Режим рендеринга установлен: " + renderMode);

        // Автоматически настраиваем соответствующие чекбоксы
        if (mode.equalsIgnoreCase("texture")) {
            showTexture = true;
        } else if (mode.equalsIgnoreCase("solid")) {
            showFaces = true;
        }
    }

    public void setFaceColor(Color color) {
        this.faceColor = color;
        System.out.println("Цвет граней установлен: " + faceColor);
    }

    public void setWireframeColor(Color color) {
        this.wireframeColor = color;
        System.out.println("Цвет каркаса установлен: " + wireframeColor);
    }

    @FXML
    private void onResetViewClick() {
        System.out.println("Нажато: Сбросить вид");
        if (cameraManager != null && cameraManager.getActiveCamera() != null) {
            cameraManager.getActiveCamera().setPosition(new javax.vecmath.Vector3f(0, 0, 20));
            cameraManager.getActiveCamera().setTarget(new javax.vecmath.Vector3f(0, 0, 0));
            showInfoDialog("Сброс вида", "Камера сброшена к начальному положению.");
        }
    }

    @FXML
    private void onCameraToolsClick() {
        System.out.println("Нажато: Инструменты камер");
        showInfoDialog("Инструменты камер", "Диалог инструментов камер еще не реализован.");
    }

    @FXML
    private void onModelToolsClick() {
        System.out.println("Нажато: Инструменты моделей");
        showInfoDialog("Инструменты моделей", "Диалог инструментов моделей еще не реализован.");
    }

    @FXML
    private void onExitClick() {
        System.out.println("Нажато: Выход");
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

    // Геттер для модели (для ModelPanelController)
    public Model getMesh() {
        return mesh;
    }

    // Сеттер для модели (для ModelPanelController)
    public void setMesh(Model mesh) {
        this.mesh = mesh;
    }

    // Геттеры для настроек отображения
    public boolean isShowWireframe() {
        return showWireframe;
    }

    public boolean isShowFaces() {
        return showFaces;
    }

    public boolean isShowNormals() {
        return showNormals;
    }

    public boolean isShowTexture() {
        return showTexture;
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