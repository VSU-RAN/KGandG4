package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import math.matrix.Matrix4f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;

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

    /// Состояние рендеринга ///
    private GraphicsContext graphicsContext;
    private Camera camera;
    private TriangulatedModel model;
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
        this.mode = new RenderMode(defaultEnableWireframe, defaultEnableTexture, defaultEnableLighting);
        this.wireframeColor = Color.web(defaultWireframeColor);
    }

    public void setup(
            GraphicsContext gc,
            Camera camera,
            TriangulatedModel model,
            Texture texture,
            Zbuffer zbuffer,
            Lightning lightning,
            int width,
            int height
    ) {
        this.graphicsContext = gc;
        this.camera = camera;
        this.model = model;
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
        Matrix4f modelMatrix = model.getCachedTransformMatrix();  //  = rotateScaleTranslate();
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



    /// Геттеры для состояния рендеринга ///
    public GraphicsContext getGraphicsContext() { return graphicsContext; }
    public TriangulatedModel getModel() { return model; }
    public Texture getTexture() { return texture; }
    public Zbuffer getZbuffer() { return zbuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Lightning getLightning() {return lightning;}



    /// ГЕТТЕРЫ ДЛЯ НАСТРОЕК И ВЫЧИСЛЯЕМЫХ ПАРАМЕТРОВ ///
    public RenderMode getMode() { return mode; }
    public Color getWireframeColor() { return wireframeColor; }


    /// УПРАВЛЕНИЕ РЕЖИМАМИ РЕНДЕРИНГА ///
    public void setWireframeEnabled(boolean enabled) {
        updateRenderMode(enabled, mode.isTexture(), mode.isLighting());
    }

    public void setTextureEnabled(boolean enabled) {
        updateRenderMode(mode.isWireframe(), enabled, mode.isLighting());
    }

    public void setLightEnabled(boolean enabled) {
        updateRenderMode(mode.isWireframe(), mode.isTexture(), enabled);
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

    private void updateRenderMode(boolean wireframe, boolean texture, boolean lighting) throws IllegalStateException {
        this.mode = new RenderMode(wireframe, texture, lighting);

        if (this.texture != null) {
            this.texture.enableTexture(texture);
        }

        if (this.lightning != null) {
            this.lightning.setEnabled(lighting);
        }
    }

}

