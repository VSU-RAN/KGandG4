package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.tools.ModelTools;

import java.net.URL;
import java.util.ResourceBundle;

public class EditPanelController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox editPanel;

    @FXML private Label modelStatsLabel;
    @FXML private Label selectionInfoLabel;

    @FXML private Button deleteSelectedButton;
    @FXML private Button deleteAllButton;
    @FXML private Button clearSelectionButton;

    @FXML private TextField selectedIndexField;
    @FXML private Button deleteByIndexButton;

    private GuiController guiController;

    private Integer selectedVertexIndex = null;
    private Integer selectedPolygonIndex = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateModelStats();
        updateSelectionInfo();
    }

    public void setGuiController(GuiController guiController) {
        this.guiController = guiController;
    }

    /**
     * Обработка клика по модели (вызывается из GuiController)
     */
    public void handleModelClick(double x, double y, double z) {
        if (guiController == null || guiController.getMesh() == null) {
            return;
        }

        Model model = guiController.getMesh();
        Vector3f clickPoint = new Vector3f((float)x, (float)y, (float)z);

        // Сначала ищем вершину
        selectedVertexIndex = ModelTools.findNearestVertex(model, clickPoint, 0.1f); // небольшой порог

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

        // Обновляем отображение модели
        if (guiController != null) {
            guiController.refreshModel();
        }
    }

    /**
     * Удаляет выбранный элемент (публичный для FXML)
     */
    @FXML
    public void deleteSelected() {
        if (guiController == null || guiController.getMesh() == null) {
            return;
        }

        Model model = guiController.getMesh();

        if (selectedVertexIndex != null) {
            ModelTools.removeVertex(model, selectedVertexIndex);
            selectedVertexIndex = null;

            // Показываем уведомление
            showAlert("Успех", "Вершина успешно удалена");

        } else if (selectedPolygonIndex != null) {
            ModelTools.removePolygon(model, selectedPolygonIndex);
            selectedPolygonIndex = null;

            // Показываем уведомление
            showAlert("Успех", "Полигон успешно удален");
        } else {
            showAlert("Предупреждение", "Не выбран элемент для удаления");
        }

        updateModelStats();
        updateSelectionInfo();

        // Обновляем отображение
        if (guiController != null) {
            guiController.refreshModel();

            // Обновляем информацию в панели моделей если она открыта
            if (guiController.getModelPanelController() != null) {
                guiController.getModelPanelController().updateModelInfo();
            }
        }
    }

    /**
     * Удаляет все вершины и полигоны (публичный для FXML)
     */
    @FXML
    public void deleteAll() {
        if (guiController == null || guiController.getMesh() == null) {
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Удаление всех элементов");
        confirmAlert.setContentText("Вы действительно хотите удалить ВСЕ вершины и полигоны?");

        ButtonType result = confirmAlert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
            Model model = guiController.getMesh();

            model.vertices.clear();
            model.polygons.clear();
            showAlert("Успех", "Все вершины и полигоны удалены");

            clearSelection();
            updateModelStats();

            // Обновляем отображение
            if (guiController != null) {
                guiController.refreshModel();

                if (guiController.getModelPanelController() != null) {
                    guiController.getModelPanelController().updateModelInfo();
                }
            }
        }
    }

    /**
     * Удаляет элемент по индексу из текстового поля (публичный для FXML)
     */
    @FXML
    public void deleteByIndex() {
        if (guiController == null || guiController.getMesh() == null) {
            return;
        }

        try {
            int index = Integer.parseInt(selectedIndexField.getText().trim());
            Model model = guiController.getMesh();

            // Пробуем сначала удалить вершину
            if (index >= 0 && index < model.vertices.size()) {
                ModelTools.removeVertex(model, index);
                showAlert("Успех", "Вершина #" + index + " удалена");
            }
            // Если не вершина, пробуем полигон
            else if (index >= 0 && index < model.polygons.size()) {
                ModelTools.removePolygon(model, index);
                showAlert("Успех", "Полигон #" + index + " удален");
            } else {
                showAlert("Ошибка", "Неверный индекс");
            }

            selectedIndexField.clear();
            updateModelStats();

            // Обновляем отображение
            if (guiController != null) {
                guiController.refreshModel();

                if (guiController.getModelPanelController() != null) {
                    guiController.getModelPanelController().updateModelInfo();
                }
            }

        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректный числовой индекс");
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
        if (guiController != null && guiController.getMesh() != null) {
            Model model = guiController.getMesh();
            String stats = String.format(
                    "Вершины: %d\nПолигоны: %d\nНормали: %d\nТекстуры: %d",
                    model.vertices.size(),
                    model.polygons.size(),
                    model.normals.size(),
                    model.textureVertices.size()
            );
            modelStatsLabel.setText(stats);
        } else {
            modelStatsLabel.setText("Модель не загружена");
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Parent getEditPanel() {
        return scrollPane;
    }
}