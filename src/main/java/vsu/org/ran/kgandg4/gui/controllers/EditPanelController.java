package vsu.org.ran.kgandg4.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.Initializable;

import vsu.org.ran.kgandg4.model.ModelManager;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.model.modelEdit.ModelTools;
import vsu.org.ran.kgandg4.model.models.Model;


@Component
public class EditPanelController implements Initializable, PanelController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox editPanel;

    @FXML private Label modelStatsLabel;
    @FXML private Label selectionInfoLabel;

    @FXML private Button deleteSelectedButton;
    @FXML private Button deleteAllButton;
    @FXML private Button clearSelectionButton;

    @FXML private TextField selectedIndexField;
    @FXML private Button deleteByIndexButton;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private AlertService alertService;

    private Integer selectedVertexIndex = null;
    private Integer selectedPolygonIndex = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateModelStats();
        updateSelectionInfo();
    }

    /**
     * Обработка клика по модели (вызывается из GuiController)
     */
    public void handleModelClick(double x, double y, double z) {
        if (modelManager.getCurrentModel() == null) {
            return;
        }

        Model model = modelManager.getCurrentModel();


         Vector3f clickPoint = new Vector3f((float)x, (float)y, (float)z);
         selectedVertexIndex = ModelTools.findNearestVertex(model, clickPoint, 0.1f);

        if (selectedVertexIndex != null) {
            selectedPolygonIndex = null;
        } else {
            // Если не нашли вершину, ищем полигон
            selectedPolygonIndex = ModelTools.findNearestPolygon(model, clickPoint, 0.1f);
            if (selectedPolygonIndex != null) {
                selectedVertexIndex = null;
            }
        }

        updateSelectionInfo();
        updateModelStats();
    }

    /**
     * Удаляет выбранный элемент (публичный для FXML)
     */
    @FXML
    public void deleteSelected() {
        if (modelManager.getCurrentModel() == null) {
            return;
        }

        Model model = modelManager.getCurrentModel();

        if (selectedVertexIndex != null) {
            try {
                ModelTools.removeVertex(model, selectedVertexIndex);
                alertService.showInfo("Успех", "Вершина успешно удалена");
                selectedVertexIndex = null;
            } catch (Exception e) {
                alertService.showError("Ошибка", "Не удалось удалить вершину: " + e.getMessage());
            }

        } else if (selectedPolygonIndex != null) {
            try {
                ModelTools.removePolygon(model, selectedPolygonIndex);
                alertService.showInfo("Успех", "Полигон успешно удален");
                selectedPolygonIndex = null;
            } catch (Exception e) {
                alertService.showError("Ошибка", "Не удалось удалить полигон: " + e.getMessage());
            }
        } else {
            alertService.showInfo("Предупреждение", "Не выбран элемент для удаления");
        }

        updateModelStats();
        updateSelectionInfo();
    }

    /**
     * Удаляет все вершины и полигоны (публичный для FXML)
     */
    @FXML
    public void deleteAll() {
        if (modelManager.getCurrentModel() == null) {
            alertService.showInfo("Предупреждение", "Нет активной модели");
            return;
        }

        boolean confirmed = alertService.showConfirmation("Подтверждение", "Вы действительно хотите удалить ВСЕ вершины и полигоны?");

        if (confirmed) {
            try {
                Model model = modelManager.getCurrentModel();

                model.deleteVertices();
                model.deletePolygons();
                model.deleteTextureVertices();
                model.deleteNormals();

                alertService.showInfo("Успех", "Все вершины и полигоны удалены");

                clearSelection();
                updateModelStats();
            } catch (Exception e) {
                alertService.showError("Ошибка", "Не удалось очистить модель: " + e.getMessage());
            }
        }

        updateModelStats();
        updateSelectionInfo();
    }

    /**
     * Удаляет элемент по индексу из текстового поля (публичный для FXML)
     */
    @FXML
    public void deleteByIndex() {
        if (modelManager.getCurrentModel() == null) {
            return;
        }

        try {
            int index = Integer.parseInt(selectedIndexField.getText().trim());
            Model model = modelManager.getCurrentModel();

            // Пробуем сначала удалить вершину
            if (index >= 0 && index < model.getVertices().size()) {
                ModelTools.removeVertex(model, index);
                alertService.showInfo("Успех", "Вершина #" + index + " удалена");
            }
            // Если не вершина, пробуем полигон
            else if (index >= 0 && index < model.getPolygons().size()) {
                ModelTools.removePolygon(model, index);
                alertService.showInfo("Успех", "Полигон #" + index + " удален");
            } else {
                alertService.showError("Ошибка", "Неверный индекс");
            }

            selectedIndexField.clear();

            updateModelStats();
            updateSelectionInfo();
        } catch (NumberFormatException e) {
            alertService.showError("Ошибка", "Введите корректный числовой индекс");
        }
    }

    /**
     * Очищает текущий выбор (публичный для FXML)
     */
    @FXML
    public void clearSelection() {
        selectedVertexIndex = null;
        selectedPolygonIndex = null;
        updateSelectionInfo();
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

    /**
     * Обновляет информацию о выборе
     */
    private void updateSelectionInfo() {
        if (selectedVertexIndex != null) {
            selectionInfoLabel.setText("Выбрана вершина: #" + selectedVertexIndex);
            selectionInfoLabel.setStyle("-fx-text-fill: #007bff; -fx-font-weight: bold;");
        } else if (selectedPolygonIndex != null) {
            selectionInfoLabel.setText("Выбран полигон: #" + selectedPolygonIndex);
            selectionInfoLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            selectionInfoLabel.setText("Ничего не выбрано");
            selectionInfoLabel.setStyle("-fx-text-fill: #495057;");
        }
    }

    @Override
    public void onPanelShow() {
        updateModelStats();
    }
}