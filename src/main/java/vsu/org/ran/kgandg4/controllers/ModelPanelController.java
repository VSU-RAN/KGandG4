package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import vsu.org.ran.kgandg4.GuiController;
import vsu.org.ran.kgandg4.model.Model;

import java.net.URL;
import java.util.ResourceBundle;

public class ModelPanelController implements Initializable {

    @FXML private VBox modelPanel;
    @FXML private Label modelInfoLabel;
    @FXML private Button loadModelButton;
    @FXML private Button loadTextureButton;
    @FXML private Button saveModelButton;

    // Чекбоксы для отображения элементов
    @FXML private CheckBox showWireframeCheck;
    @FXML private CheckBox showFacesCheck;
    @FXML private CheckBox showNormalsCheck;
    @FXML private CheckBox showTextureCheck;

    // Превью текстуры
    @FXML private ImageView texturePreview;
    @FXML private Label textureInfoLabel;

    private GuiController guiController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Назначаем обработчики для чекбоксов
        setupEventHandlers();

        updateModelInfo();
        updateTexturePreview(null);
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
    }

    private void setupEventHandlers() {
        // При изменении чекбоксов
        showWireframeCheck.setOnAction(e -> onDisplaySettingsChanged());
        showFacesCheck.setOnAction(e -> onDisplaySettingsChanged());
        showNormalsCheck.setOnAction(e -> onDisplaySettingsChanged());
        showTextureCheck.setOnAction(e -> onDisplaySettingsChanged());
    }

    private void onDisplaySettingsChanged() {
        System.out.println("Настройки отображения изменены:");
        System.out.println("  Каркас: " + showWireframeCheck.isSelected());
        System.out.println("  Грани: " + showFacesCheck.isSelected());
        System.out.println("  Нормали: " + showNormalsCheck.isSelected());
        System.out.println("  Текстура: " + showTextureCheck.isSelected());

        // Отправляем настройки в GuiController
        if (guiController != null) {
            guiController.updateDisplaySettings(
                    showWireframeCheck.isSelected(),
                    showFacesCheck.isSelected(),
                    showNormalsCheck.isSelected(),
                    showTextureCheck.isSelected()
            );
        }
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

            // Если есть текстура, обновляем превью
            if (mesh.texture != null) {
                updateTexturePreview(mesh.texture);
            } else {
                updateTexturePreview(null);
            }
        } else {
            modelInfoLabel.setText("Модель не загружена");
            updateTexturePreview(null);
        }
    }

    private void updateTexturePreview(javafx.scene.image.Image texture) {
        if (texture != null) {
            texturePreview.setImage(texture);
            texturePreview.setFitWidth(280);
            texturePreview.setFitHeight(140);
            texturePreview.setPreserveRatio(true);
            textureInfoLabel.setText(
                    "Текстура: " + (int)texture.getWidth() + "x" + (int)texture.getHeight()
            );
        } else {
            texturePreview.setImage(null);
            textureInfoLabel.setText("Текстура не загружена");
        }
    }

    @FXML
    private void onLoadModelClick() {
        if (guiController != null) {
            guiController.loadModel();
            updateModelInfo();
        }
    }

    @FXML
    private void onLoadTextureClick() {
        if (guiController != null) {
            guiController.loadTexture();
            updateModelInfo();
        }
    }

    @FXML
    private void onSaveModelClick() {
        if (guiController != null) {
            guiController.saveModel();
        }
    }

    public VBox getModelPanel() {
        return modelPanel;
    }
}