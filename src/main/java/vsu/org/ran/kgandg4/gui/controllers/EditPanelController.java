package vsu.org.ran.kgandg4.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.Initializable;

import vsu.org.ran.kgandg4.model.ModelManager;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;
import vsu.org.ran.kgandg4.model.models.Model;

@Component
public class EditPanelController implements Initializable, PanelController {

    @FXML private Label modelStatsLabel;
    @FXML private Label selectionInfoLabel;
    @FXML private TextField selectedIndexField;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private AlertService alertService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateModelStats();

        selectionInfoLabel.textProperty().bind(modelManager.selectionInfoProperty());

        modelManager.selectedVertexIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectionStyle();
        });
        modelManager.selectedPolygonIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateSelectionStyle();
        });

        modelManager.activeModelProperty().addListener((obs, oldModel, newModel) -> {
            updateModelStats();
            updateSelectionStyle();
        });
    }

    private void updateSelectionStyle() {
        if (modelManager.isVertexSelected()) {
            selectionInfoLabel.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
        } else if (modelManager.isPolygonSelected()) {
            selectionInfoLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            selectionInfoLabel.setStyle("-fx-text-fill: #495057;");
        }
    }

    /**
     * Обработка клика по модели
     */
    public void handleModelClick(Vector3f clickPoint) {
        modelManager.handleClickInModel(clickPoint);
        updateModelStats();
    }

    /**
     * Удаляет выбранный элемент
     */
    @FXML
    public void deleteSelected() {
        boolean deleted = modelManager.deleteSelected();

        if (deleted) {
            alertService.showInfo("Успех", "Элемент удален");
        } else {
            alertService.showInfo("Предупреждение", "Не выбран элемент для удаления");
        }

        updateModelStats();
    }

    /**
     * Удаляет все вершины и полигоны
     */
    @FXML
    public void deleteAll() {
        if (modelManager.getCurrentModel() == null) {
            alertService.showInfo("Предупреждение", "Нет активной модели");
            return;
        }

        boolean confirmed = alertService.showConfirmation("Подтверждение",
                "Вы действительно хотите удалить ВСЕ вершины и полигоны?");

        if (confirmed) {
            boolean deleted = modelManager.deleteAllFromCurrentModel();

            if (deleted) {
                alertService.showInfo("Успех", "Все вершины и полигоны удалены");
            } else {
                alertService.showError("Ошибка", "Не удалось очистить модель");
            }
        }

        updateModelStats();
    }

    /**
     * Удаляет элемент по индексу
     */
    @FXML
    public void deleteByIndex() {
        try {
            int index = Integer.parseInt(selectedIndexField.getText().trim());
            boolean deleted = modelManager.deleteByIndex(index);

            if (deleted) {
                alertService.showInfo("Успех", "Элемент #" + index + " удален");
            } else {
                alertService.showError("Ошибка", "Неверный индекс");
            }

            selectedIndexField.clear();
            updateModelStats();

        } catch (NumberFormatException e) {
            alertService.showError("Ошибка", "Введите корректный числовой индекс");
        } catch (Exception e) {
            alertService.showError("Ошибка", "Не удалось удалить элемент: " + e.getMessage());
        }
    }

    /**
     * Очищает текущий выбор
     */
    @FXML
    public void clearSelection() {
        modelManager.clearSelection();
    }

    /**
     * Обновляет статистику модели
     */
    public void updateModelStats() {
        if (modelManager.getCurrentModel() != null) {
            Model currentModel = modelManager.getCurrentModel();
            modelStatsLabel.setText(currentModel.toString());
        } else {
            modelStatsLabel.setText(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
        }
    }

    @Override
    public void onPanelShow() {
        updateModelStats();
    }
}