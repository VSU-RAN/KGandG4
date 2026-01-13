package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateModelInfo();
        updateTexturePreview(null);
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

    public Parent getModelPanel() {
        return scrollPane;
    }
}