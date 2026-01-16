package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

import math.matrix.Matrix4f;
import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

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
    private Texture texture;

    @Autowired
    private Zbuffer zbuffer;

    @Autowired
    private Lightning lightning;

    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public Texture getTexture() {
        return texture;
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

    public Color getMaterialColor() {
        return texture.getMaterialColor();
    }

    public void setMaterialColor(Color color) {
        texture.setMaterialColor(color);

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

    public void setTextureEnabled(boolean enabled) throws IllegalStateException {
        renderContext.setTextureEnabled(enabled);
    }

    public boolean isTextureEnabled() {
        return renderContext.isTextureEnabled();
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


    public void renderFrame(GraphicsContext gc, int width, int height) {
        Camera camera = cameraManager.getActiveCamera();

        camera.setAspectRatio((float) width/height);

        zbuffer.resize(width, height);
        zbuffer.clear();

        gc.clearRect(0, 0, width, height);

        for (Model model: modelManager.getModels()) {
            if (model.isVisible()) {
                renderContext.setup(
                        gc,
                        camera,
                        (TriangulatedModel) model,
                        texture,
                        zbuffer,
                        lightning,
                        width,
                        height
                );

                // Рендерим модель
                RenderEngine.render(renderContext);
            }
        }
    }
}
