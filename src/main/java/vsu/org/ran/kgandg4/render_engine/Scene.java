package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.canvas.GraphicsContext;
import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.render_engine.render.RenderContext;
import vsu.org.ran.kgandg4.render_engine.render.RenderEngine;

@Component
public class Scene {
    @Autowired
    private RenderContext renderContext;

    @Autowired
    private ModelManager modelManager;

    @Autowired
    private CameraManager cameraManager;

    @Autowired
    private RenderEngine renderEngine;

    @Autowired
    private Texture defaultTexture;

    @Autowired
    private Zbuffer zbuffer;

    public void renderFrame(GraphicsContext gc, int width, int height) {
        Camera camera = cameraManager.getActiveCamera();
        if (camera != null) {
            camera.setAspectRatio((float) width/height);
        }

        if (zbuffer != null) {
            zbuffer.resize(width, height);
            zbuffer.clear();
        }

        gc.clearRect(0, 0, width, height);

        // Рендерим все видимые модели
        for (TriangulatedModel model : modelManager.getModels()) {
            if (model.visible) {
                try {
                    // Используем существующий метод create (без матрицы трансформации)
                    renderContext.create(
                            gc,
                            camera,
                            model,
                            defaultTexture,
                            zbuffer,
                            width,
                            height
                    );

                    // Рендерим модель
                    RenderEngine.render(renderContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getVisibleModelCount() {
        if (modelManager == null) return 0;

        int count = 0;
        for (TriangulatedModel model : modelManager.getModels()) {
            if (model.visible) count++;
        }
        return count;
    }

    public TriangulatedModel getActiveModel() {
        return modelManager != null ? modelManager.getActiveModel() : null;
    }

    public boolean hasVisibleModels() {
        if (modelManager == null) return false;

        for (TriangulatedModel model : modelManager.getModels()) {
            if (model.visible) return true;
        }
        return false;
    }
}