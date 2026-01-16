package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import math.matrix.Matrix4f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;

import java.util.Collections;
import java.util.List;

import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.rotateScaleTranslate;

@Component
public class RenderContext {
    /// Конфигурационные значения ///

    @Value("${render.default.wireframe_color:#FF0000}")
    private String defaultWireframeColor;
    @Value("${render.default.enable_lighting:true}")
    private boolean defaultEnableLighting;
    @Value("${render.default.enable_wireframe:true}")
    private boolean defaultEnableWireframe;
    @Value("${render.default.enable_texture:true}")
    private boolean defaultEnableTexture;
    @Value("${render.default.enable_only_wireframe:false}")
    private boolean defaultOnlyEnableWireframe;

    /// Состояние рендеринга ///
    private GraphicsContext graphicsContext;
    private Camera camera;
    private List<TriangulatedModel> models;
    private Texture texture;
    private Zbuffer zbuffer;
    private Lightning lightning;
    private int width;
    private int height;

    /// Настройки рендеринга ///
    private RenderMode mode;
    private Color wireframeColor;
    private Matrix4f pvmMatrix;
    private Vector3f cameraDirectionNormalized;

    @PostConstruct
    private void init() {
        this.mode = new RenderMode(defaultEnableWireframe, defaultEnableTexture, defaultEnableLighting, defaultOnlyEnableWireframe);
        this.wireframeColor = Color.web(defaultWireframeColor);
    }


    public void setup(
            GraphicsContext gc,
            Camera camera,
            List<TriangulatedModel> models,
            Texture texture,
            Zbuffer zbuffer,
            Lightning lightning,
            int width,
            int height
    ) {
        this.graphicsContext = gc;
        this.camera = camera;
        this.models = models;
        this.texture = texture;
        this.zbuffer = zbuffer;
        this.lightning = lightning;
        this.width = width;
        this.height = height;

        if (camera != null) {
            camera.setAspectRatio((float) width / height);
            this.cameraDirectionNormalized = camera.getDirection().normalized();
        }
        this.camera = camera;

        calculatePVMMatrix();
    }

    private void calculatePVMMatrix() {
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // PVM матрица как в методичке
        this.pvmMatrix = new Matrix4f(projectionMatrix.copy());
        pvmMatrix.multiplyV(viewMatrix);
        pvmMatrix.multiplyV(modelMatrix);
    }

    public Matrix4f getPVMMatrix() {
        calculatePVMMatrix();
        return this.pvmMatrix;
    }

    public Vector3f getCameraDirectionNormalized() {
        if (cameraDirectionNormalized == null && camera != null) {
            cameraDirectionNormalized = camera.getDirection().normalized();
        }
        return cameraDirectionNormalized;
    }

    public Camera getCamera() {
        return camera;
    }


    /// Геттеры для состояния рендеринга ///
    public GraphicsContext getGraphicsContext() { return graphicsContext; }

    public List<TriangulatedModel> getAllModels() {
        return Collections.unmodifiableList(models);
    }

    public List<TriangulatedModel> getVisibleModels() {
        return models.stream().filter(Model::isVisible).toList();
    }


    public Texture getTexture() { return texture; }
    public Zbuffer getZbuffer() { return zbuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Lightning getLightning() {return lightning;}



    /// ГЕТТЕРЫ ДЛЯ НАСТРОЕК И ВЫЧИСЛЯЕМЫХ ПАРАМЕТРОВ ///
    public RenderMode getMode() { return mode; }
    public Color getWireframeColor() { return wireframeColor; }


    /// УПРАВЛЕНИЕ РЕЖИМАМИ РЕНДЕРИНГА ///
    public void setWireframeEnabled(boolean enabled) throws IllegalStateException {
        if (enabled && mode.isOnlyWireframe()) {
            updateRenderMode(false, false, false, false);
        }
        updateRenderMode(enabled, mode.isTexture(), mode.isLighting(), false);
    }

    public void setTextureEnabled(boolean enabled) throws IllegalStateException {
        if (enabled && mode.isOnlyWireframe()) {
            updateRenderMode(false, false, false, false);
        }
        updateRenderMode(mode.isWireframe(), enabled, mode.isLighting(), false);
    }

    public void setLightEnabled(boolean enabled) throws IllegalStateException {
        if (enabled && mode.isOnlyWireframe()) {
            updateRenderMode(false, false, false, false);
        }
        updateRenderMode(mode.isWireframe(), mode.isTexture(), enabled, false);
    }

    public void setWireframeOnlyEnabled(boolean enabled) throws IllegalStateException {
        if (enabled) {
            updateRenderMode(false, false, false, true);
        } else {
            updateRenderMode(defaultEnableWireframe, defaultEnableTexture, defaultEnableLighting, false);
        }
    }

    public boolean isWireframeEnabled() {
        return mode.isWireframe();
    }

    public boolean isTextureEnabled() {
        return mode.isTexture();
    }

    public boolean isLightEnabled() {
        return mode.isLighting();
    }

    public boolean isWireframeOnlyEnabled() {return mode.isOnlyWireframe();}

    private void updateRenderMode(boolean wireframe, boolean texture, boolean lighting, boolean onlyWireframe) throws IllegalStateException {
        this.mode = new RenderMode(wireframe, texture, lighting, onlyWireframe);

        if (this.texture != null && texture) {
            try {
                this.texture.enableTexture(texture);
            } catch (IllegalStateException e) {
                this.mode = new RenderMode(wireframe, false, lighting, onlyWireframe);
                throw e;
            }
        } else if (this.texture != null) {
            this.texture.enableTexture(false);
        }

        if (this.lightning != null) {
            this.lightning.setEnabled(lighting);
        }
    }

}

