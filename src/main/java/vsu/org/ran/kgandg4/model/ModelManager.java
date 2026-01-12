package vsu.org.ran.kgandg4.model;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
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

    private Model currentModel;

    public void loadModel(File file) throws IOException {
        String content = Files.readString(file.toPath());
        this.currentModel = ObjReader.read(content);

        triangulator.triangulateModel(this.currentModel);
        normalCalculator.calculateNormals(currentModel);
    }

    public void saveModel(Model model) {
        //todo: Сделать сохранение модели
    }

    public Model getCurrentModel() {
        return currentModel;
    }
}
