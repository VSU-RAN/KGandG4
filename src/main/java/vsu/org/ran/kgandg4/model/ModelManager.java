package vsu.org.ran.kgandg4.model;

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

    private TriangulatedModel currentModel;

    public void loadModel(File file) throws IOException {
        String content = Files.readString(file.toPath());
        Model loadModel = ObjReader.read(content);

        this.currentModel = triangulator.createTriangulatedModel(loadModel);
        normalCalculator.calculateNormals(currentModel);
    }

    public void saveModel(Model model) {
        //todo: Сделать сохранение модели
    }

    public TriangulatedModel getCurrentModel() {
        return currentModel;
    }
}
