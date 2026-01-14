package vsu.org.ran.kgandg4.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

@Component
public class ModelPanelController implements Initializable, PanelController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox modelPanel;
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

        clearTexturePreview();
    }

    private void loadModel(File file) {
        try {
            modelManager.loadModel(file);
            onModelLoadSuccess(file);
        } catch (IOException e) {
            onModelLoadError(file, e);
        } catch (Exception e) {
            onUnexpectedError("загрузки модели", file, e);
        }
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
        try {
            texture.loadFromFile(file.toURI().toString());
            onTextureLoadSuccess(file);
        } catch (Exception e) {
            onTextureLoadError(file, e);
        }
    }


    public void updateModelInfo() {
        if (modelManager.getCurrentModel() != null) {
            TriangulatedModel currentModel = modelManager.getCurrentModel();

            modelInfoLabel.setText(currentModel.toString());

            if (texture.hasTexture()) {
                updateTexturePreview();
            }
        } else {
            modelInfoLabel.setText(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
            if (texture.hasTexture()) {
                updateTexturePreview();
            } else {
                clearTexturePreview();
            }
        }
    }

    public void updateTexturePreview() {
        texturePreview.setImage(this.texture.getTexture());

        textureInfoLabel.setText(this.texture.toString());
    }

    private void clearTexturePreview() {
        texturePreview.setImage(null);
        textureInfoLabel.setText(ConstantsAndStyles.DEFAULT_TEXTURE_TEXT);
    }


    @FXML
    private void onLoadModelClick() {
        fileDialogService.showOpenModelDialog(panelManager.getMainWindow()).ifPresent(this::loadModel);
    }

    @FXML
    private void onLoadTextureClick() {
        fileDialogService.showOpenTextureDialog(panelManager.getMainWindow()).ifPresent(this::loadTexture);
    }

    @FXML
    private void onSaveModelClick() {
       fileDialogService.showSaveModelDialog(panelManager.getMainWindow()).ifPresent(this::saveModel);
    }

    public Parent getModelPanel() {
        return scrollPane;
    }


    // === ОБРАБОТЧИКИ УСПЕХА ===

    private void onModelLoadSuccess(File file) {
        updateModelInfo();
        alertService.showInfo("Успех", "Модель успешно загружена: " + file.getName());
    }

    private void onModelSaveSuccess(File file) {
        alertService.showInfo("Успех", "Модель успешно сохранена: " + file.getName());
    }

    private void onTextureLoadSuccess(File file) {
        updateModelInfo();
        alertService.showInfo("Успех", "Текстура загружена: " + file.getName());
    }

    // === ОБРАБОТЧИКИ ОШИБОК ===

    private void onModelLoadError(File file, IOException e) {
        alertService.showError("Ошибка загрузки",
                "Не удалось загрузить модель " + file.getName() + ":\n" + e.getMessage());
    }

    private void onModelSaveError(File file, IOException e) {
        alertService.showError("Ошибка сохранения",
                "Не удалось сохранить модель " + file.getName() + ":\n" + e.getMessage());
    }

    private void onTextureLoadError(File file, Exception e) {
        alertService.showError("Ошибка",
                "Не удалось загрузить текстуру " + file.getName() + ":\n" + e.getMessage());
    }

    private void onValidationError(String operation, IllegalArgumentException e) {
        alertService.showError("Ошибка " + operation, e.getMessage());
    }

    private void onUnexpectedError(String operation, File file, Exception e) {
        alertService.showError("Ошибка",
                "Произошла непредвиденная ошибка " + operation +
                        (file != null ? " файла " + file.getName() : "") + ":\n" + e.getMessage());
        e.printStackTrace();
    }

    @Override
    public void onPanelShow() {
        updateModelInfo();
    }


}