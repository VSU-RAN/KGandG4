package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import math.matrix.Matrix4f;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;

import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.rotateScaleTranslate;

@Component
public class RenderContext {
    public enum RenderMode {
        WIREFRAME, SOLID, TEXTURED, WIREFRAME_OVERLAY
    }

    @Value("${render.default.mode:TEXTURED}")
    private String defaultMode;

    @Value("${render.default.ambient_light:0.9}")
    private float defaultAmbientLight;

    @Value("${render.default.enable_lighting:true}")
    private boolean defaultEnableLighting;

    @Value("${render.default.wireframe_color:#FF0000}")
    private String defaultWireframeColor;


    private GraphicsContext graphicsContext;
    private Camera camera;
    private TriangulatedModel model;
    private Texture texture;
    private Zbuffer zbuffer;
    private int width;
    private int height;

    // === Текущие настройки ===
    private RenderMode mode;
    private float ambientLight;
    private boolean enableLighting;
    private Color wireframeColor;


    private Matrix4f pvmMatrix;
    private Vector3f cameraDirectionNormalized;

    @PostConstruct
    private void init() {
        this.mode = RenderMode.valueOf(defaultMode.toUpperCase());
        this.ambientLight = defaultAmbientLight;
        this.enableLighting = defaultEnableLighting;
        this.wireframeColor = Color.web(defaultWireframeColor);
    }


    public void create(
            GraphicsContext gc,
            Camera camera,
            TriangulatedModel model,
            Texture texture,
            Zbuffer zbuffer,
            int width,
            int height
    ) {
        this.graphicsContext = gc;
        this.camera = camera;
        this.model = model;
        this.texture = texture;
        this.zbuffer = zbuffer;
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

    public Vector3f getCameraDirectionNormalized() {
        if (cameraDirectionNormalized == null && camera != null) {
            cameraDirectionNormalized = camera.getDirection().normalized();
        }
        return cameraDirectionNormalized;
    }

    public GraphicsContext getGraphicsContext() { return graphicsContext; }
    public Camera getCamera() { return camera; }
    public TriangulatedModel getModel() { return model; }
    public Texture getTexture() { return texture; }
    public Zbuffer getZbuffer() { return zbuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public RenderMode getMode() { return mode; }
    public float getAmbientLight() { return ambientLight; }
    public boolean isEnableLighting() { return enableLighting; }
    public Color getWireframeColor() { return wireframeColor; }
    public Matrix4f getPVMMatrix() {
        calculatePVMMatrix();
        return this.pvmMatrix;
    }

    public void setGraphicsContext(GraphicsContext gc) { this.graphicsContext = gc; }

    public void setCamera(Camera camera) {
        this.camera = camera;
        this.pvmMatrix = null;
    }

    public void setModel(TriangulatedModel model) { this.model = model; }
    public void setTexture(Texture texture) { this.texture = texture; }
    public void setZbuffer(Zbuffer zbuffer) { this.zbuffer = zbuffer; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }

    // === Сеттеры для настроек ===
    public void setMode(RenderMode mode) { this.mode = mode; }
    public void setAmbientLight(float ambientLight) { this.ambientLight = ambientLight; }
    public void setEnableLighting(boolean enableLighting) { this.enableLighting = enableLighting; }
    public void setWireframeColor(Color wireframeColor) { this.wireframeColor = wireframeColor; }

    private static void printMatrix(Matrix4f matrix) {
        System.out.printf("[%.4f, %.4f, %.4f, %.4f]\n",
                matrix.getElement(0), matrix.getElement(1), matrix.getElement(2), matrix.getElement(3));
        System.out.printf("[%.4f, %.4f, %.4f, %.4f]\n",
                matrix.getElement(4), matrix.getElement(5), matrix.getElement(6), matrix.getElement(7));
        System.out.printf("[%.4f, %.4f, %.4f, %.4f]\n",
                matrix.getElement(8), matrix.getElement(9), matrix.getElement(10), matrix.getElement(11)); // Исправлено
        System.out.printf("[%.4f, %.4f, %.4f, %.4f]\n",
                matrix.getElement(12), matrix.getElement(13), matrix.getElement(14), matrix.getElement(15)); // Исправлено
    }
}

