package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.MaterialSettings;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

import java.util.List;

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
    private Zbuffer zbuffer;

    @Autowired
    private Lightning lightning;

    @Autowired
    private MaterialSettings materialSettings;

    private boolean textureModeEnabled = false;

    public void setTextureEnabled(boolean enabled) throws IllegalStateException {
        if (enabled) {
            boolean hasVisibleModelWithLoadedTexture = modelManager.getModels().stream().filter(Model::isVisible).anyMatch(Model::hasTexture);
            if (!hasVisibleModelWithLoadedTexture) {
                throw new IllegalStateException(
                        "Невозможно включить режим текстур.\n\n" +
                                "Нет видимых моделей с загруженными текстурами.\n" +
                                "1. Загрузите текстуру для хотя бы одной видимой модели\n" +
                                "2. Попробуйте снова"
                );
            }
            this.textureModeEnabled = true;
        } else {
            this.textureModeEnabled = false;
        }
        renderContext.setTextureEnabled(enabled);
    }

    public boolean isTextureEnabled() {
        return textureModeEnabled;
    }


    public Lightning getLightning() {
        return lightning;
    }

    public Zbuffer getZbuffer() {
        return zbuffer;
    }

    public Camera getActiveCamera() {
        return cameraManager.getActiveCamera();
    }

    public Color getLightColor() {
        return lightning.getLightColor();
    }

    public void setLightColor(Color color) {
        lightning.setLightColor(color);
    }

    public float getLightIntensity() {
        return lightning.getIntensity();
    }

    public void setLightIntensity(float intensity) {
        lightning.setIntensity(intensity);
    }

    public Color getMaterialColor() {
       return modelManager.getMaterialColor();
    }

    public void setMaterialColor(Color color) {
        modelManager.setActiveModelColor(color);
    }


    public void setLightEnabled(boolean enabled) {
        renderContext.setLightEnabled(enabled);
    }

    public boolean isLightEnabled() {
        return renderContext.isLightEnabled();
    }

    public void setWireframeEnabled(boolean enabled) {
        renderContext.setWireframeEnabled(enabled);
    }

    public boolean isWireframeEnabled() {
        return renderContext.isWireframeEnabled();
    }

    public void setWireframeOnlyEnabled(boolean enabled) {
        renderContext.setWireframeOnlyEnabled(enabled);
    }

    public boolean isWireframeOnlyEnabled() {
        return renderContext.isWireframeOnlyEnabled();
    }


    public void renderFrame(GraphicsContext gc, int width, int height) {
        Camera camera = cameraManager.getActiveCamera();

        camera.setAspectRatio((float) width/height);

        zbuffer.resize(width, height);
        zbuffer.clear();

        gc.clearRect(0, 0, width, height);

        List<TriangulatedModel> allModels = modelManager.getModels().stream().map(model -> (TriangulatedModel) model).toList();

        renderContext.setup(
                gc,
                camera,
                allModels,
                zbuffer,
                lightning,
                width,
                height
        );

        RenderEngine.render(renderContext);
    }
}
