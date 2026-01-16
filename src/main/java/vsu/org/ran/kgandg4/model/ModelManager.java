package vsu.org.ran.kgandg4.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import math.vector.Vector3f;

import vsu.org.ran.kgandg4.IO.ObjWriter;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.IO.objReader.ObjReader;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.model.models.Triangle;
import vsu.org.ran.kgandg4.normals.NormalCalculator;
import vsu.org.ran.kgandg4.triangulation.Triangulator;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class ModelManager {
    @Autowired
    private Triangulator triangulator;

    @Autowired
    private NormalCalculator normalCalculator;

    @Value("${model.default.threshold:0.3}")
    private float modelDefaultThreshold;

    private volatile boolean isProcessing = false;

    private ObservableList<Model> modelList = FXCollections.observableArrayList();
    private int nextId = 0;
    private final ReadOnlyObjectWrapper<Model> activeModelProperty = new ReadOnlyObjectWrapper<>();

    private final ObjectProperty<Integer> selectedVertexIndex = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> selectedPolygonIndex = new SimpleObjectProperty<>();
    private final ReadOnlyObjectWrapper<String> selectionInfoProperty = new ReadOnlyObjectWrapper<>("Ничего не выбрано");

    public Model loadModel(File file) throws IOException {
        String content = Files.readString(file.toPath());
        Model loadModel = ObjReader.read(content);
        loadModel.setName(file.getName());

        return this.addModel(loadModel, file.getName());
    }

    public Model addModel(Model model, String name) {
        TriangulatedModel triangulatedModel = triangulator.createTriangulatedModel(model);

        normalCalculator.calculateNormals(triangulatedModel);

        triangulatedModel.setId(nextId++);
        triangulatedModel.setVisible(true);
        triangulatedModel.setName(name != null ? name : "Модель " + String.valueOf(nextId));

        this.modelList.add(triangulatedModel);

        // Если это первая модель или нет активной модели, делаем ее активной
        if (modelList.size() == 1 || activeModelProperty.get() == null) {
            switchToModelById(triangulatedModel.getId());
        }

        return triangulatedModel;
    }


    public void removeModel(TriangulatedModel model) {
        if (modelList.size() <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнюю модель");
        }
        modelList.remove(model);
        if (activeModelProperty.get() != null && activeModelProperty.get().equals(model)) {
            switchToModel(0);
        }
    }

    /**
     * Удаляет модель по ID
     * @param id ID модели
     * @throws IllegalArgumentException если нельзя удалить последнюю модель
     */
    public void removeModel(int id) {
        if (modelList.size() <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнюю модель");
        }

        // Находим модель для удаления
        Model modelToRemove = null;
        int modelIndex = -1;
        for (int i = 0; i < modelList.size(); i++) {
            if (modelList.get(i).getId() == id) {
                modelToRemove = modelList.get(i);
                modelIndex = i;
                break;
            }
        }

        if (modelToRemove == null) {
            throw new IllegalArgumentException("Нет модели с таким ID: " + id);
        }

        // Проверяем, является ли модель активной
        boolean wasActive = activeModelProperty.get() != null &&
                activeModelProperty.get().getId() == id;

        // Если удаляемая модель активна, очищаем выбор
        if (wasActive) {
            clearSelection();

            // Находим новую модель для активации
            Model newActiveModel = null;
            if (modelIndex > 0) {
                // Если не первая, берем предыдущую
                newActiveModel = modelList.get(modelIndex - 1);
            } else {
                // Если первая, берем следующую
                newActiveModel = modelList.get(1);
            }

            // Устанавливаем новую активную модель
            activeModelProperty.set(newActiveModel);
        }

        // Удаляем модель из списка
        boolean removed = modelList.remove(modelToRemove);

        if (!removed) {
            throw new IllegalStateException("Не удалось удалить модель из списка");
        }

        // Обновляем информацию о выборе
        if (wasActive) {
            Model current = getCurrentModel();
            if (current != null) {
                selectionInfoProperty.set("Активна модель: " + current.getName());
            } else {
                selectionInfoProperty.set("Нет активной модели");
            }
        }
    }

    public void switchToModel(int index) {
        if (index >= 0 && index < modelList.size()) {
            Model newActive = modelList.get(index);
            activeModelProperty.set(newActive);
        }
    }

    public void switchToModelById(int id) {
        Model model = getModelById(id);
        activeModelProperty.set(model);
    }

    public Model switchToNextModel() {
        Model activeModel = activeModelProperty.get();
        if (activeModel == null || modelList.isEmpty()) return activeModel;

        int curIndex = modelList.indexOf(activeModel);
        int nextIndex = (curIndex + 1) % modelList.size();
        switchToModel(nextIndex);
        return activeModel;
    }

    public ObjectProperty<Model> activeModelProperty() {
        return activeModelProperty;
    }

    public Model getCurrentModel() {
        return activeModelProperty.get();
    }

    public Model getModelById(int id) {
        return modelList.stream()
                .filter(model -> model.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет модели с таким ID: " + id));
    }

    public ObservableList<Model> getModels() {
        return FXCollections.unmodifiableObservableList(this.modelList);
    }

    public void saveModel(File file) throws IOException {
        Model modelToSave = getCurrentModel();
        if (modelToSave == null) {
            throw new IllegalStateException("Нет активной модели для сохранения");
        }
        saveModel(modelToSave, file);
    }

    public void saveModel(Model model, File file) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("Модель не может быть null");
        }
        ObjWriter.write(model, file.toPath());
    }

    @Deprecated
    public void setCurrentModel(Model model) {
        if (model != null && modelList.contains(model)) {
            activeModelProperty.set(model);
        }
    }

    public void handleClickInModel(Vector3f pointInModelSpace) {
        TriangulatedModel activeModel = (TriangulatedModel) getCurrentModel();
        if (activeModel == null) {
            clearSelection();
            return;
        }
        float threshold = activeModel.calculateAutoThreshold(modelDefaultThreshold);

        // Ищем ближайшую вершину
        Integer vertexIndex = activeModel.findNearestVertex(pointInModelSpace, threshold);
        if (vertexIndex != null) {
            selectVertex(vertexIndex);
            return;
        }

        Integer polygonIndex = activeModel.findPolygonContainingPoint(pointInModelSpace);
        if (polygonIndex != null) {
            selectPolygon(polygonIndex);
            return;
        }

        // Ничего не нашли - очищаем выбор
        clearSelection();
    }

    public void selectVertex(int vertexIndex) {
        Model activeModel = getCurrentModel();
        if (activeModel != null && vertexIndex >= 0 && vertexIndex < activeModel.getVertices().size()) {
            selectedVertexIndex.set(vertexIndex);
            System.out.println("Выбрана вершина: " + selectedVertexIndex.get());
            selectedPolygonIndex.set(null);
            updateSelectionInfo();
        } else {
            clearSelection();
        }
    }

    public void selectPolygon(int polygonIndex) {
        Model activeModel = getCurrentModel();
        if (activeModel != null && polygonIndex >= 0 && polygonIndex < activeModel.getPolygons().size()) {
            selectedPolygonIndex.set(polygonIndex);
            System.out.println("Выбран полигон: " + selectedPolygonIndex.get());
            selectedVertexIndex.set(null);
            updateSelectionInfo();
        } else {
            clearSelection();
        }
    }

    public void clearSelection() {
        selectedVertexIndex.set(null);
        selectedPolygonIndex.set(null);
        updateSelectionInfo();
    }

    public boolean deleteSelected() {
        Model activeModel = getCurrentModel();
        if (activeModel == null) return false;

        if (selectedVertexIndex.get() != null) {
            int index = selectedVertexIndex.get();
            activeModel.removeVertex(index);
            clearSelection();
            return true;

        } else if (selectedPolygonIndex.get() != null) {
            int index = selectedPolygonIndex.get();
            activeModel.removePolygon(index);
            clearSelection();
            return true;
        }

        return false;
    }

    public boolean deleteAllFromCurrentModel() {
        Model activeModel = getCurrentModel();
        if (activeModel == null) {
            return false;
        }

        activeModel.deleteVertices();
        activeModel.deletePolygons();
        activeModel.deleteTextureVertices();
        activeModel.deleteNormals();

        clearSelection();
        return true;
    }

    /**
     * Удаляет вершину по индексу
     * @param index индекс вершины
     * @return true если вершина удалена, false если неверный индекс
     */
    public boolean deleteVertexByIndex(int index) {
        Model activeModel = getCurrentModel();
        if (activeModel == null) {
            return false;
        }

        if (index < 0 || index >= activeModel.getVertices().size()) {
            return false;
        }

        activeModel.removeVertex(index);

        // Если удалили выбранную вершину, сбрасываем выбор
        if (selectedVertexIndex.get() != null && selectedVertexIndex.get() == index) {
            clearSelection();
        }

        return true;
    }

    /**
     * Удаляет полигон по индексу
     * @param index индекс полигона
     * @return true если полигон удален, false если неверный индекс
     */
    public boolean deletePolygonByIndex(int index) {
        Model activeModel = getCurrentModel();
        if (activeModel == null) {
            return false;
        }

        if (index < 0 || index >= activeModel.getPolygons().size()) {
            return false;
        }

        activeModel.removePolygon(index);

        // Если удалили выбранный полигон, сбрасываем выбор
        if (selectedPolygonIndex.get() != null && selectedPolygonIndex.get() == index) {
            clearSelection();
        }

        return true;
    }

    /**
     * Удаляет текущую модель
     * @return true если модель удалена, false если модели не было
     */
    public boolean deleteCurrentModel() {
        System.out.println("DEBUG: deleteCurrentModel() called");

        Model currentModel = getCurrentModel();
        if (currentModel == null) {
            System.out.println("DEBUG: No current model to delete");
            return false;
        }

        int modelId = currentModel.getId();
        String modelName = currentModel.getName();

        System.out.println("DEBUG: Deleting model: " + modelName + " (ID: " + modelId + ")");

        // Используем существующий метод removeModel
        try {
            removeModel(modelId);
            System.out.println("DEBUG: Model deleted successfully");
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG: Failed to delete model: " + e.getMessage());
            return false;
        }
    }

    /**
     * Удаляет элемент по индексу (устаревший метод)
     * @deprecated Используйте {@link #deleteVertexByIndex(int)} или {@link #deletePolygonByIndex(int)}
     */
    @Deprecated
    public boolean deleteByIndex(int index) {
        Model activeModel = getCurrentModel();
        if (activeModel == null) {
            return false;
        }

        if (index >= 0 && index < activeModel.getVertices().size()) {
            return deleteVertexByIndex(index);
        }

        if (index >= 0 && index < activeModel.getPolygons().size()) {
            return deletePolygonByIndex(index);
        }

        return false;
    }

    private void updateSelectionInfo() {
        if (getCurrentModel() == null) {
            selectionInfoProperty.set("Нет активной модели");
            return;
        }

        if (selectedVertexIndex.get() != null) {
            Vector3f vertex = getSelectedVertex();
            if (vertex != null) {
                selectionInfoProperty.set(String.format("Вершина #%d (%.2f, %.2f, %.2f)",
                        selectedVertexIndex.get(), vertex.getX(), vertex.getY(), vertex.getZ()));
            } else {
                selectionInfoProperty.set("Вершина #" + selectedVertexIndex.get());
            }
        } else if (selectedPolygonIndex.get() != null) {
            Polygon polygon = getSelectedPolygon();
            if (polygon != null) {
                selectionInfoProperty.set(String.format("Полигон #%d (%d вершин)",
                        selectedPolygonIndex.get(), polygon.getVertexIndices().size()));
            } else {
                selectionInfoProperty.set("Полигон #" + selectedPolygonIndex.get());
            }
        } else {
            selectionInfoProperty.set("Ничего не выбрано");
        }
    }


    // === Геттеры ===

    public boolean isVertexSelected() {
        return selectedVertexIndex.get() != null;
    }

    public boolean isPolygonSelected() {
        return selectedPolygonIndex.get() != null;
    }

    public Integer getSelectedVertexIndex() {
        return selectedVertexIndex.get();
    }

    public Integer getSelectedPolygonIndex() {
        return selectedPolygonIndex.get();
    }

    public Vector3f getSelectedVertex() {
        Model activeModel = getCurrentModel();
        Integer index = selectedVertexIndex.get();
        if (activeModel != null && index != null && index < activeModel.getVertices().size()) {
            return activeModel.getVertices().get(index);
        }
        return null;
    }

    public Polygon getSelectedPolygon() {
        Model activeModel = getCurrentModel();
        Integer index = selectedPolygonIndex.get();
        if (activeModel != null && index != null && index < activeModel.getPolygons().size()) {
            return activeModel.getPolygons().get(index);
        }
        return null;
    }

    public ObjectProperty<Integer> selectedVertexIndexProperty() {
        return selectedVertexIndex;
    }

    public ObjectProperty<Integer> selectedPolygonIndexProperty() {
        return selectedPolygonIndex;
    }

    public ReadOnlyObjectProperty<String> selectionInfoProperty() {
        return selectionInfoProperty.getReadOnlyProperty();
    }
}