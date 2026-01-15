package vsu.org.ran.kgandg4.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vsu.org.ran.kgandg4.IO.ObjWriter;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.normals.NormalCalculator;
import vsu.org.ran.kgandg4.IO.objReader.ObjReader;
import vsu.org.ran.kgandg4.triangulation.Triangulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class ModelManager {
    @Autowired
    private Triangulator triangulator;

    @Autowired
    private NormalCalculator normalCalculator;

    private ObservableList<Model> modelList = FXCollections.observableArrayList();
    private int nextId = 0;
    private final ReadOnlyObjectWrapper<Model> activeModelProperty = new ReadOnlyObjectWrapper<>();

    public Model loadModel(File file) throws IOException {
        String content = Files.readString(file.toPath());
        Model loadModel = ObjReader.read(content);
        loadModel.setName(file.getName());

        return this.addModel(loadModel);
    }

    public Model addModel(Model model) {
        TriangulatedModel triangulatedModel = triangulator.createTriangulatedModel(model);

        normalCalculator.calculateNormals(triangulatedModel);

        triangulatedModel.setId(nextId++);
        triangulatedModel.setVisible(true);

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

    public void removeModel(int id) {
        if (modelList.size() <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнюю модель");
        }

        Model model = this.getModelById(id);

        modelList.remove(model);
        if (activeModelProperty.get() != null && activeModelProperty.get().equals(model)) {
            switchToModel(0);
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
}