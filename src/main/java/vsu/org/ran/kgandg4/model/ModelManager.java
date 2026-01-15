package vsu.org.ran.kgandg4.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vsu.org.ran.kgandg4.IO.ObjWriter;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
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

    private ObservableList<TriangulatedModel> modelList = FXCollections.observableArrayList();
    private int nextId = 0;
    private final ReadOnlyObjectWrapper<TriangulatedModel> activeModelProperty = new ReadOnlyObjectWrapper<>();

    @PostConstruct
    public void init() {
    }

    public TriangulatedModel loadModel(File file) throws IOException {
        String content = Files.readString(file.toPath());
        Model loadModel = ObjReader.read(content);

        TriangulatedModel triangulatedModel = triangulator.createTriangulatedModel(loadModel);

        normalCalculator.calculateNormals(triangulatedModel);

        triangulatedModel.id = nextId++;
        triangulatedModel.name = file.getName();
        triangulatedModel.visible = true;

        this.modelList.add(triangulatedModel);

        // Если это первая модель или нет активной модели, делаем ее активной
        if (modelList.size() == 1 || activeModelProperty.get() == null) {
            switchToModelById(triangulatedModel.id);
        }
        return triangulatedModel;
    }

    public TriangulatedModel addModel() {
        TriangulatedModel model = new TriangulatedModel();
        model.id = nextId++;
        model.name = "Model " + model.id;
        model.visible = true;
        this.modelList.add(model);

        // Если это первая модель, делаем ее активной
        if (modelList.size() == 1 || activeModelProperty.get() == null) {
            switchToModelById(model.id);
        }

        return model;
    }

    public TriangulatedModel addModel(TriangulatedModel model) {
        model.id = nextId++;
        if (model.name == null) {
            model.name = "Model " + model.id;
        }
        model.visible = true;
        this.modelList.add(model);

        // Если это первая модель, делаем ее активной
        if (modelList.size() == 1 || activeModelProperty.get() == null) {
            switchToModelById(model.id);
        }

        return model;
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

        TriangulatedModel model = modelList.stream()
                .filter(m -> m.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет модели с таким ID: " + id));

        modelList.remove(model);
        if (activeModelProperty.get() != null && activeModelProperty.get().equals(model)) {
            switchToModel(0);
        }
    }

    public void switchToModel(int index) {
        if (index >= 0 && index < modelList.size()) {
            TriangulatedModel newActive = modelList.get(index);
            activeModelProperty.set(newActive);
        }
    }

    public void switchToModelById(int id) {
        TriangulatedModel model = getModelById(id);
        activeModelProperty.set(model);
    }

    public TriangulatedModel switchToNextModel() {
        TriangulatedModel activeModel = activeModelProperty.get();
        if (activeModel == null || modelList.isEmpty()) return activeModel;

        int curIndex = modelList.indexOf(activeModel);
        int nextIndex = (curIndex + 1) % modelList.size();
        switchToModel(nextIndex);
        return activeModel;
    }

    public ObjectProperty<TriangulatedModel> activeModelProperty() {
        return activeModelProperty;
    }

    public TriangulatedModel getActiveModel() {
        return activeModelProperty.get();
    }

    public TriangulatedModel getModelById(int id) {
        return modelList.stream()
                .filter(model -> model.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет модели с таким ID: " + id));
    }

    public ObservableList<TriangulatedModel> getModels() {
        return FXCollections.unmodifiableObservableList(this.modelList);
    }

    public void saveModel(File file) throws IOException {
        TriangulatedModel modelToSave = getActiveModel();
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

    public TriangulatedModel getCurrentModel() {
        return getActiveModel();
    }

    @Deprecated
    public void setCurrentModel(TriangulatedModel model) {
        if (model != null && modelList.contains(model)) {
            activeModelProperty.set(model);
        }
    }

    public boolean hasModels() {
        return !modelList.isEmpty();
    }

    public int getModelCount() {
        return modelList.size();
    }

    public void clearAllModels() {
        modelList.clear();
        activeModelProperty.set(null);
    }
}