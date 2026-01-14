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
    /// Конфигурационные значения ///
    @Value("${render.default.ambient_light:0.9}")
    private float defaultAmbientLight;

    @Value("${render.default.wireframe_color:#FF0000}")
    private String defaultWireframeColor;

    @Value("${render.default.light_color:#FFFFFF}")
    private String defaultLightColor;

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
    private int width;
    private int height;

    /// Настройки рендеринга ///
    private RenderMode mode;
    private float ambientLight;
    private Color wireframeColor;
    private Color lightColor;


    private Matrix4f pvmMatrix;
    private Vector3f cameraDirectionNormalized;

    @PostConstruct
    private void init() {
        this.mode = new RenderMode(defaultEnableWireframe, defaultEnableTexture, defaultEnableLighting);
        this.ambientLight = defaultAmbientLight;
        this.wireframeColor = Color.web(defaultWireframeColor);
        this.lightColor = Color.web(defaultLightColor);
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
    public TriangulatedModel getModel() { return model; }
    public Texture getTexture() { return texture; }
    public Zbuffer getZbuffer() { return zbuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Matrix4f getPVMMatrix() {
        calculatePVMMatrix();
        return this.pvmMatrix;
    }

    public RenderMode getMode() { return mode; }
    public float getAmbientLight() { return ambientLight; }
    public Color getLightColor() {return lightColor;}
    public Color getWireframeColor() { return wireframeColor; }
    public Color getMaterialColor() { return texture.getMaterialColor();}

    public boolean shouldDrawWireframe() {return mode.isWireframe();}
    public boolean shouldUseTexture() {return mode.isTexture() && texture != null && texture.hasTexture();}
    public boolean shouldUseLighting() {return mode.isLighting();}


    public void setCamera(Camera camera) {
        this.camera = camera;
        this.cameraDirectionNormalized = null;
        this.pvmMatrix = null;
    }

    public void setModel(TriangulatedModel model) { this.model = model; }
    public void setTexture(Texture texture) { this.texture = texture; }
    public void setAmbientLight(float ambientLight) { this.ambientLight = ambientLight; }
    public void setLightColor(Color lightColor) {this.lightColor = lightColor;}
    public void setWireframeColor(Color wireframeColor) { this.wireframeColor = wireframeColor; }

    public void setDrawWireframe(boolean drawWireframe) {
        this.mode = new RenderMode(drawWireframe, mode.isTexture(), mode.isLighting());
    }

    public void setUseTexture(boolean useTexture) {
        this.mode = new RenderMode(mode.isWireframe(), useTexture, mode.isLighting());
        if (texture != null) {
            texture.enableTexture(useTexture);
        }
    }

    public void setUseLighting(boolean useLighting) {
        this.mode = new RenderMode(mode.isWireframe(), mode.isTexture(), useLighting);
    }

}

