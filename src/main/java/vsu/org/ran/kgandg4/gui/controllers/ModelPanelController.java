package vsu.org.ran.kgandg4.gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import vsu.org.ran.kgandg4.IO.FileDialogService;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.gui.PanelManager;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.render_engine.Texture;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

@Component
public class ModelPanelController implements Initializable, PanelController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox modelPanel;

    // Элементы для управления списком моделей
    @FXML private ListView<TriangulatedModel> modelListView;
    @FXML private Label activeModelLabel;
    @FXML private Button switchModelButton;
    @FXML private Button nextModelButton;
    @FXML private CheckBox showAllCheckbox;

    // Элементы для информации о модели
    @FXML private Label modelInfoLabel;
    @FXML private Button loadModelButton;
    @FXML private Button loadTextureButton;
    @FXML private Button saveModelButton;
    @FXML private ImageView texturePreview;
    @FXML private Label textureInfoLabel;

    @Autowired
    private PanelManager panelManager;

    @Autowired
    private AlertService alertService;

    @Autowired
    private FileDialogService fileDialogService;

    @Autowired
    private Texture texture;

    @Autowired
    private ModelManager modelManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        texturePreview.setFitWidth(280);
        texturePreview.setFitHeight(140);
        texturePreview.setPreserveRatio(true);
        texturePreview.setSmooth(true);
        texturePreview.setCache(true);
        texturePreview.setStyle(ConstantsAndStyles.STYLE_TEXTURE_PREVIEW);

        // Инициализация ListView для моделей
        initModelListView();
        clearTexturePreview();

        // Инициализация связи с ModelManager
        if (modelManager != null) {
            initModelManager();
        }

        // Инициализация состояния кнопок
        updateButtonsState();

        // По умолчанию включаем "Показать все"
        if (showAllCheckbox != null) {
            showAllCheckbox.setSelected(true);
        }
    }

    private void initModelListView() {
        if (modelListView == null) {
            return;
        }

        // Настройка отображения моделей в списке
        modelListView.setCellFactory(lv -> new ListCell<TriangulatedModel>() {
            @Override
            protected void updateItem(TriangulatedModel model, boolean empty) {
                super.updateItem(model, empty);
                if (empty || model == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(null);
                } else {
                    String displayName = getModelDisplayName(model);

                    // Добавляем иконку для активной модели
                    StringBuilder text = new StringBuilder(displayName);
                    if (model.equals(modelManager.getActiveModel())) {
                        text.append(" ✓");
                    }

                    setText(text.toString());

                    // Стиль в зависимости от статуса
                    if (model.equals(modelManager.getActiveModel())) {
                        setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
                    } else {
                        setStyle(null);
                    }
                }
            }
        });

        // При выборе модели обновляем информацию
        modelListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newModel) -> {
                    if (newModel != null) {
                        // Если галка "Показать все" выключена, показываем только выбранную модель
                        if (showAllCheckbox != null && !showAllCheckbox.isSelected()) {
                            showOnlySelectedModel(newModel);
                        }
                        updateModelInfo(newModel);
                        updateButtonsState();
                    }
                }
        );
    }

    private void initModelManager() {
        if (modelListView == null || modelManager == null) {
            return;
        }

        modelListView.setItems(modelManager.getModels());

        // Слушатель изменения активной модели
        modelManager.activeModelProperty().addListener((obs, oldModel, newModel) -> {
            Platform.runLater(() -> {
                if (newModel != null) {
                    updateModelInfo(newModel);
                    activeModelLabel.setText(getModelDisplayName(newModel));
                    modelListView.getSelectionModel().select(newModel);
                    updateButtonsState();

                    // Если галка "Показать все" выключена, показываем только активную модель
                    if (showAllCheckbox != null && !showAllCheckbox.isSelected()) {
                        showOnlySelectedModel(newModel);
                    }

                    modelListView.refresh();
                }
            });
        });

        // Слушатель для обновления списка при изменении моделей
        modelManager.getModels().addListener((javafx.collections.ListChangeListener<TriangulatedModel>) change -> {
            Platform.runLater(() -> {
                modelListView.refresh();
                updateButtonsState();

                // Обновляем метку активной модели
                TriangulatedModel active = modelManager.getActiveModel();
                if (active != null) {
                    activeModelLabel.setText(getModelDisplayName(active));
                } else {
                    activeModelLabel.setText("Нет модели");
                }
            });
        });

        // Начальная инициализация - нет начальной модели
        // Активная модель будет установлена при загрузке первой модели
        updateButtonsState();
        activeModelLabel.setText("Нет модели");
        modelInfoLabel.setText(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
    }

    private void showOnlySelectedModel(TriangulatedModel selectedModel) {
        if (modelManager == null) return;

        for (TriangulatedModel model : modelManager.getModels()) {
            // Показываем только выбранную модель, остальные скрываем
            model.visible = model.equals(selectedModel);
        }

        // Обновляем отображение списка
        if (modelListView != null) {
            modelListView.refresh();
        }
    }

    private String getModelDisplayName(TriangulatedModel model) {
        if (model == null) return "Нет модели";
        return model.name != null ? model.name : "Model " + model.id;
    }

    private void updateModelInfo(TriangulatedModel model) {
        if (model == null) {
            modelInfoLabel.setText(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
            clearTexturePreview();
        } else {
            modelInfoLabel.setText(getDetailedModelInfo(model));
            if (texture.hasTexture()) {
                updateTexturePreview();
            } else {
                clearTexturePreview();
            }
        }
    }

    private String getDetailedModelInfo(TriangulatedModel model) {
        StringBuilder info = new StringBuilder();

        info.append(getModelDisplayName(model)).append("\n");
        info.append("Вершин: ").append(model.vertices.size())
                .append("\nПолигонов: ").append(model.polygons.size());

        return info.toString();
    }

    private void updateButtonsState() {
        if (modelManager == null) {
            return;
        }

        TriangulatedModel selected = modelListView != null ?
                modelListView.getSelectionModel().getSelectedItem() : null;

        boolean hasModels = !modelManager.getModels().isEmpty();
        boolean hasSelectedModel = selected != null;

        if (switchModelButton != null) {
            if (hasSelectedModel) {
                boolean isActive = selected.equals(modelManager.getActiveModel());
                switchModelButton.setDisable(isActive);
                switchModelButton.setText(isActive ? "Активна" : "Сделать активной");
            } else {
                switchModelButton.setDisable(true);
                switchModelButton.setText("Сделать активной");
            }
        }

        if (nextModelButton != null) {
            nextModelButton.setDisable(!hasModels);
        }

        if (saveModelButton != null) {
            saveModelButton.setDisable(!hasModels);
        }

        if (loadModelButton != null) {
            // Кнопка загрузки всегда активна
            loadModelButton.setDisable(false);
        }
    }

    @FXML
    private void onShowAllCheckboxChanged() {
        if (showAllCheckbox != null && modelManager != null) {
            boolean showAll = showAllCheckbox.isSelected();

            if (showAll) {
                // Включаем галку "Показать все" - показываем все модели
                for (TriangulatedModel model : modelManager.getModels()) {
                    model.visible = true;
                }
                System.out.println("Все модели показаны");
            } else {
                // Выключаем галку "Показать все" - скрываем все модели
                for (TriangulatedModel model : modelManager.getModels()) {
                    model.visible = false;
                }
                System.out.println("Все модели скрыты");

                // Но если есть выбранная модель - показываем ее
                TriangulatedModel selected = modelListView != null ?
                        modelListView.getSelectionModel().getSelectedItem() : null;
                if (selected != null) {
                    selected.visible = true;
                    System.out.println("Выбранная модель показана: " + getModelDisplayName(selected));
                }
            }

            // Обновляем отображение
            if (modelListView != null) {
                modelListView.refresh();
            }
        }
    }

    // ===== Обработчики для кнопок управления моделями =====

    @FXML
    private void onSwitchModelClick() {
        TriangulatedModel selected = modelListView != null ?
                modelListView.getSelectionModel().getSelectedItem() : null;
        if (selected != null && modelManager != null) {
            try {
                modelManager.switchToModelById(selected.id);
                alertService.showInfo("Успех", "Активная модель изменена: " + getModelDisplayName(selected));

                // Если галка "Показать все" выключена, показываем только активную модель
                if (showAllCheckbox != null && !showAllCheckbox.isSelected()) {
                    showOnlySelectedModel(selected);
                }
            } catch (Exception e) {
                alertService.showError("Ошибка", "Не удалось переключить модель: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onNextModelClick() {
        if (modelManager != null) {
            try {
                // Проверяем, есть ли вообще модели
                if (modelManager.getModels().isEmpty()) {
                    alertService.showInfo("Информация", "Нет загруженных моделей");
                    return;
                }

                TriangulatedModel previousModel = modelManager.getActiveModel();
                TriangulatedModel nextModel = modelManager.switchToNextModel();

                if (previousModel != null && !previousModel.equals(nextModel)) {
                    alertService.showInfo("Успех", "Переключено на модель: " + getModelDisplayName(nextModel));

                    // Если галка "Показать все" выключена, показываем только активную модель
                    if (showAllCheckbox != null && !showAllCheckbox.isSelected()) {
                        showOnlySelectedModel(nextModel);
                    }
                }
            } catch (Exception e) {
                alertService.showError("Ошибка", "Не удалось переключить модель: " + e.getMessage());
            }
        }
    }

    // ===== Существующие обработчики загрузки/сохранения =====

    private void loadModel(File file) {
        try {
            // Просто вызываем метод, который возвращает void
            modelManager.loadModel(file);
            onModelLoadSuccess(file);
        } catch (IOException e) {
            onModelLoadError(file, e);
        } catch (Exception e) {
            onUnexpectedError("загрузки модели", file, e);
        }
    }

    private void onModelLoadSuccess(File file) {
        Platform.runLater(() -> {
            TriangulatedModel loadedModel = modelManager.getActiveModel();

            // Если галка "Показать все" выключена, показываем только загруженную модель
            if (showAllCheckbox != null && !showAllCheckbox.isSelected() && loadedModel != null) {
                showOnlySelectedModel(loadedModel);
            }

            updateModelInfo(loadedModel);
            updateButtonsState();
            alertService.showInfo("Успех", "Модель успешно загружена: " + file.getName());
        });
    }

    private void saveModel(File file) {
        try {
            modelManager.saveModel(file);
            onModelSaveSuccess(file);
        } catch (IOException e) {
            onModelSaveError(file, e);
        } catch (IllegalArgumentException e) {
            onValidationError("сохранения модели", e);
        } catch (Exception e) {
            onUnexpectedError("сохранения модели", file, e);
        }
    }

    private void loadTexture(File file) {
        if (texture == null || alertService == null) {
            return;
        }

        textureInfoLabel.setText("Загрузка...");
        texturePreview.setImage(null);

        CompletableFuture<Void> loadFuture = texture.loadFromFile(file.toURI().toString());

        loadFuture.thenAcceptAsync(result -> {
            Platform.runLater(() -> {
                updateTexturePreview();
                updateModelInfo(modelManager.getActiveModel());
                alertService.showInfo("Успех", "Текстура загружена: " + file.getName());
            });
        }).exceptionallyAsync(error -> {
            Platform.runLater(() -> {
                onTextureLoadError(file, error instanceof Exception ?
                        (Exception) error : new Exception(error));
            });
            return null;
        });
    }

    public void updateTexturePreview() {
        if (texturePreview == null || texture == null) {
            return;
        }

        try {
            texturePreview.setImage(null);
            if (texture.hasTexture()) {
                texturePreview.setImage(texture.getTexture());
                textureInfoLabel.setText(texture.toString());
            } else {
                clearTexturePreview();
            }
        } catch (Exception e) {
            texturePreview.setImage(null);
            textureInfoLabel.setText("Ошибка загрузки текстуры");
        }
    }

    private void clearTexturePreview() {
        if (texturePreview != null) {
            texturePreview.setImage(null);
        }
        if (textureInfoLabel != null) {
            textureInfoLabel.setText(ConstantsAndStyles.DEFAULT_TEXTURE_TEXT);
        }
    }

    @FXML
    private void onLoadModelClick() {
        if (fileDialogService != null && panelManager != null) {
            fileDialogService.showOpenModelDialog(panelManager.getMainWindow())
                    .ifPresent(this::loadModel);
        }
    }

    @FXML
    private void onLoadTextureClick() {
        if (fileDialogService != null && panelManager != null) {
            fileDialogService.showOpenTextureDialog(panelManager.getMainWindow())
                    .ifPresent(this::loadTexture);
        }
    }

    @FXML
    private void onSaveModelClick() {
        if (fileDialogService != null && panelManager != null) {
            fileDialogService.showSaveModelDialog(panelManager.getMainWindow())
                    .ifPresent(this::saveModel);
        }
    }

    public Parent getModelPanel() {
        return scrollPane;
    }

    // === Обработчики успеха ===

    private void onModelSaveSuccess(File file) {
        Platform.runLater(() -> {
            alertService.showInfo("Успех", "Модель успешно сохранена: " + file.getName());
        });
    }

    // === Обработчики ошибок ===

    private void onModelLoadError(File file, IOException e) {
        Platform.runLater(() -> {
            alertService.showError("Ошибка загрузки",
                    "Не удалось загрузить модель " + file.getName() + ":\n" + e.getMessage());
        });
    }

    private void onModelSaveError(File file, IOException e) {
        Platform.runLater(() -> {
            alertService.showError("Ошибка сохранения",
                    "Не удалось сохранить модель " + file.getName() + ":\n" + e.getMessage());
        });
    }

    private void onTextureLoadError(File file, Exception e) {
        Platform.runLater(() -> {
            alertService.showError("Ошибка",
                    "Не удалось загрузить текстуру " + file.getName() + ":\n" + e.getMessage());
        });
    }

    private void onValidationError(String operation, IllegalArgumentException e) {
        Platform.runLater(() -> {
            alertService.showError("Ошибка " + operation, e.getMessage());
        });
    }

    private void onUnexpectedError(String operation, File file, Exception e) {
        Platform.runLater(() -> {
            alertService.showError("Ошибка",
                    "Произошла непредвиденная ошибка " + operation +
                            (file != null ? " файла " + file.getName() : "") + ":\n" + e.getMessage());
        });
        e.printStackTrace();
    }

    @Override
    public void onPanelShow() {
        Platform.runLater(() -> {
            TriangulatedModel activeModel = modelManager != null ?
                    modelManager.getActiveModel() : null;
            updateModelInfo(activeModel);
            updateButtonsState();

            if (modelListView != null) {
                modelListView.refresh();
            }
        });
    }
}