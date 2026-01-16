package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import math.matrix.Matrix4f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;

import java.util.List;

public class RenderContext {
    private final GraphicsContext graphicsContext;
    private final Camera camera;
    private final List<TriangulatedModel> allModels;
    private final Zbuffer zbuffer;
    private final Lightning lightning;
    private final int width;
    private final int height;
    private final RenderMode renderMode;
    private final Color wireframeColor;
    private final Scene scene;

    /// Настройки рендеринга ///
    private Vector3f cameraDirection;
    private List<TriangulatedModel> visibleModels;


    public RenderContext(
            GraphicsContext graphicsContext,
            Camera camera,
            List<TriangulatedModel> allModels,
            Zbuffer zbuffer,
            Lightning lightning,
            int width,
            int height,
            RenderMode renderMode,
            Color wireframeColor,
            Scene scene
    ) {
        this.graphicsContext = graphicsContext;
        this.camera = camera;
        this.allModels = allModels;
        this.zbuffer = zbuffer;
        this.lightning = lightning;
        this.width = width;
        this.height = height;
        this.renderMode = renderMode;
        this.wireframeColor = wireframeColor;
        this.scene = scene;
    }

    public Matrix4f getPVMMatrixForModel(Model model) {
        Matrix4f modelMatrix = model.getCachedTransformMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f pvmMatrix = new Matrix4f(projectionMatrix.copy());
        pvmMatrix.multiplyV(viewMatrix);
        pvmMatrix.multiplyV(modelMatrix);

        return pvmMatrix;
    }

    public Vector3f getCameraDirectionNormalized() {
        if (cameraDirection == null) {
            cameraDirection = camera.getDirection().normalized();
        }
        return cameraDirection;
    }

    public Camera getCamera() {
        return camera;
    }


    /// Геттеры для состояния рендеринга ///
    public GraphicsContext getGraphicsContext() { return graphicsContext; }

    public Scene getScene() {
        return scene;
    }

    public List<TriangulatedModel> getVisibleModels() {
        if (visibleModels == null) {
            visibleModels = allModels.stream().filter(Model::isVisible).toList();
        }
        return visibleModels;
    }

    public Zbuffer getZbuffer() { return zbuffer; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Lightning getLightning() {return lightning;}
    public Color getWireframeColor() { return wireframeColor; }
    public RenderMode getMode() {return renderMode;}
}

