package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import vsu.org.ran.kgandg4.model.Model;

import java.net.URL;
import java.util.ResourceBundle;

public class ModelPanelController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox modelPanel;
    @FXML private Label modelInfoLabel;
    @FXML private Button loadModelButton;
    @FXML private Button loadTextureButton;
    @FXML private Button saveModelButton;

    // Превью текстуры
    @FXML private ImageView texturePreview;
    @FXML private Label textureInfoLabel;

    private GuiController guiController;
    private Image currentTexture = null;  // Храним текстуру отдельно

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== Инициализация ModelPanelController ===");

        // Настраиваем ImageView
        texturePreview.setFitWidth(280);
        texturePreview.setFitHeight(140);
        texturePreview.setPreserveRatio(true);
        texturePreview.setSmooth(true);
        texturePreview.setCache(true);

        // Устанавливаем стиль для отображения фона
        texturePreview.setStyle("-fx-background-color: #e9ecef; " +
                "-fx-border-color: #ced4da; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 4;");

        // Инициализируем пустое изображение
        clearTexturePreview();

        System.out.println("✓ ModelPanelController инициализирован");
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
        System.out.println("✓ GuiController установлен в ModelPanelController");
    }

    public void updateModelInfo() {
        System.out.println("Обновление информации о модели...");

        if (guiController != null && guiController.getMesh() != null) {
            Model mesh = guiController.getMesh();
            String info = "Модель загружена\n" +
                    "Вершин: " + mesh.vertices.size() +
                    "\nПолигонов: " + mesh.polygons.size() +
                    "\nНормалей: " + mesh.normals.size() +
                    "\nТекстур: " + mesh.textureVertices.size();
            modelInfoLabel.setText(info);
            System.out.println("Информация о модели обновлена");

            // Отображаем текущую текстуру (если есть)
            if (currentTexture != null) {
                updateTexturePreview(currentTexture);
            } else if (mesh.texture != null) {
                // Если в модели есть текстура, показываем ее
                checkAndUpdateTexture(mesh);
            }
        } else {
            System.out.println("Модель не загружена");
            modelInfoLabel.setText("Модель не загружена");

            // Показываем текущую текстуру (если есть)
            if (currentTexture != null) {
                updateTexturePreview(currentTexture);
            } else {
                clearTexturePreview();
            }
        }
    }

    private void checkAndUpdateTexture(Model mesh) {
        if (mesh.texture != null) {
            System.out.println("Текстура найдена в модели, проверяем состояние...");

            if (mesh.texture.isError()) {
                System.err.println("Текстура загружена с ошибкой: " + mesh.texture.getException());
                clearTexturePreview();
            } else if (mesh.texture.getWidth() > 0 && mesh.texture.getHeight() > 0) {
                // Текстура уже загружена
                System.out.println("Текстура уже загружена: " +
                        mesh.texture.getWidth() + "x" + mesh.texture.getHeight());
                updateTexturePreview(mesh.texture);
            } else {
                // Текстура все еще загружается, добавляем слушатель
                System.out.println("Текстура загружается...");
                mesh.texture.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() == 1.0) {
                        Platform.runLater(() -> {
                            if (!mesh.texture.isError() &&
                                    mesh.texture.getWidth() > 0 &&
                                    mesh.texture.getHeight() > 0) {
                                System.out.println("Текстура завершила загрузку: " +
                                        mesh.texture.getWidth() + "x" + mesh.texture.getHeight());
                                updateTexturePreview(mesh.texture);
                            } else {
                                System.err.println("Текстура не загрузилась корректно");
                                clearTexturePreview();
                            }
                        });
                    }
                });

                // Также проверяем через 500 мс на всякий случай
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        Platform.runLater(() -> {
                            if (mesh.texture.getWidth() > 0 && mesh.texture.getHeight() > 0) {
                                updateTexturePreview(mesh.texture);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } else {
            System.out.println("Текстура не найдена в модели");
            // Не очищаем, если есть текущая текстура
            if (currentTexture == null) {
                clearTexturePreview();
            }
        }
    }

    public void updateTexturePreview(Image texture) {
        System.out.println("Обновление превью текстуры...");

        if (texture != null && !texture.isError() &&
                texture.getWidth() > 0 && texture.getHeight() > 0) {

            System.out.println("Устанавливаем текстуру в ImageView: " +
                    texture.getWidth() + "x" + texture.getHeight());

            // Сохраняем текстуру
            currentTexture = texture;

            // Устанавливаем изображение
            texturePreview.setImage(texture);

            // Обновляем информацию
            String textureInfo = String.format("Текстура: %.0fx%.0f",
                    texture.getWidth(), texture.getHeight());
            textureInfoLabel.setText(textureInfo);

            System.out.println("Превью текстуры обновлено: " + textureInfo);
        } else {
            System.out.println("Текстура невалидна для отображения");
            clearTexturePreview();
        }
    }

    private void clearTexturePreview() {
        texturePreview.setImage(null);
        textureInfoLabel.setText("Текстура не загружена");
    }

    @FXML
    private void onLoadModelClick() {
        System.out.println("Нажата кнопка 'Загрузить модель'");
        if (guiController != null) {
            guiController.loadModel();
        }
    }

    @FXML
    private void onLoadTextureClick() {
        System.out.println("Нажата кнопка 'Загрузить текстуру'");
        if (guiController != null) {
            guiController.loadTexture();
        }
    }

    @FXML
    private void onSaveModelClick() {
        System.out.println("Нажата кнопка 'Сохранить модель'");
        if (guiController != null) {
            guiController.saveModel();
        }
    }

    public Parent getModelPanel() {
        return scrollPane;
    }

    // Геттер для текущей текстуры
    public Image getCurrentTexture() {
        return currentTexture;
    }
}