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
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
    }

    public void updateModelInfo() {
        if (guiController != null && guiController.getMesh() != null) {
            Model mesh = guiController.getMesh();
            String info = "Модель загружена\n" +
                    "Вершин: " + mesh.vertices.size() +
                    "\nПолигонов: " + mesh.polygons.size() +
                    "\nНормалей: " + mesh.normals.size() +
                    "\nТекстур: " + mesh.textureVertices.size();
            modelInfoLabel.setText(info);
            // Отображаем текущую текстуру (если есть)
            if (currentTexture != null) {
                updateTexturePreview(currentTexture);
            } else if (mesh.texture != null) {
                // Если в модели есть текстура, показываем ее
                checkAndUpdateTexture(mesh);
            }
        } else {
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
            if (mesh.texture.isError()) {
               clearTexturePreview();
            } else if (mesh.texture.getWidth() > 0 && mesh.texture.getHeight() > 0) {
                // Текстура уже загружена
                updateTexturePreview(mesh.texture);
            } else {
                // Текстура все еще загружается, добавляем слушатель
                mesh.texture.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() == 1.0) {
                        Platform.runLater(() -> {
                            if (!mesh.texture.isError() &&
                                    mesh.texture.getWidth() > 0 &&
                                    mesh.texture.getHeight() > 0) {
                            } else {
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
            // Не очищаем, если есть текущая текстура
            if (currentTexture == null) {
                clearTexturePreview();
            }
        }
    }

    public void updateTexturePreview(Image texture) {
        if (texture != null && !texture.isError() &&
                texture.getWidth() > 0 && texture.getHeight() > 0) {

            // Сохраняем текстуру
            currentTexture = texture;

            // Устанавливаем изображение
            texturePreview.setImage(texture);

            // Обновляем информацию
            String textureInfo = String.format("Текстура: %.0fx%.0f",
                    texture.getWidth(), texture.getHeight());
            textureInfoLabel.setText(textureInfo);

        } else {
            clearTexturePreview();
        }
    }

    private void clearTexturePreview() {
        texturePreview.setImage(null);
        textureInfoLabel.setText("Текстура не загружена");
    }

    @FXML
    private void onLoadModelClick() {
        if (guiController != null) {
            guiController.loadModel();
        }
    }

    @FXML
    private void onLoadTextureClick() {
        if (guiController != null) {
            guiController.loadTexture();
        }
    }

    @FXML
    private void onSaveModelClick() {
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