package vsu.org.ran.kgandg4.render_engine.render;

import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.render_engine.*;
import vsu.org.ran.kgandg4.model.ModelManager;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Scene {
    /// Конфигурационные значения ///
    @Value("${render.default.wireframe_color:#FF0000}")
    private String defaultWireframeColor;

    @Value("${render.default.inactive_color:#FF0000}")
    private String defaultInactiveColor;

    @Value("${render.default.enable_lighting:true}")
    private boolean defaultEnableLighting;
    @Value("${render.default.enable_wireframe:true}")
    private boolean defaultEnableWireframe;
    @Value("${render.default.enable_texture:true}")
    private boolean defaultEnableTexture;
    @Value("${render.default.enable_only_wireframe:false}")
    private boolean defaultOnlyEnableWireframe;

    private RenderMode renderMode;
    private Color wireframeColor;
    private Color inactiveColor;

    @Autowired private ModelManager modelManager;
    @Autowired private CameraManager cameraManager;
    @Autowired private Zbuffer zbuffer;
    @Autowired private Lightning lightning;


    @PostConstruct
    public void init() {
        this.wireframeColor = Color.web(defaultWireframeColor);
        this.inactiveColor = Color.web(defaultInactiveColor);

        this.renderMode = new RenderMode(
                defaultEnableWireframe,
                defaultEnableTexture,
                defaultEnableLighting,
                defaultOnlyEnableWireframe
        );
        lightning.setEnabled(renderMode.isLighting());
    }

    public void setTextureEnabled(boolean enabled) throws IllegalStateException {
        if (enabled && !canEnableTextureMode()) {
            throw new IllegalStateException(
                    "Невозможно включить режим текстур.\n\n" +
                            "Нет видимых моделей с загруженными текстурами.\n" +
                            "1. Загрузите текстуру для хотя бы одной видимой модели\n" +
                            "2. Попробуйте снова"
            );
        }
        this.renderMode = new RenderMode(
                renderMode.isWireframe(),
                enabled,
                renderMode.isLighting(),
                renderMode.isOnlyWireframe()
        );
    }

    public boolean isTextureEnabled() {
        return renderMode.isTexture();
    }

    public boolean canEnableTextureMode() {
        return modelManager.getModels().stream().filter(Model::isVisible).anyMatch(Model::hasTexture);
    }

    public void setWireframeEnabled(boolean enabled) {
        if (enabled && renderMode.isOnlyWireframe()) {
            setWireframeOnlyEnabled(false);
        }
        this.renderMode = new RenderMode(
                enabled,
                renderMode.isTexture(),
                renderMode.isLighting(),
                false
        );
    }

    public void setWireframeOnlyEnabled(boolean enabled) {
        if (enabled) {
            this.renderMode = new RenderMode(
                    false,
                    false,
                    false,
                    true
            );
        } else {
            this.renderMode = new RenderMode(
                    defaultEnableWireframe,
                    defaultEnableTexture,
                    defaultEnableLighting,
                    false
            );
        }
    }

    public void setLightEnabled(boolean enabled) {
        this.renderMode = new RenderMode(
                renderMode.isWireframe(),
                renderMode.isTexture(),
                enabled,
                renderMode.isOnlyWireframe()
        );

        if (lightning != null) {
            lightning.setEnabled(enabled);
        }
    }

    public boolean isLightEnabled() {
        return renderMode.isLighting();
    }

    public boolean isWireframeOnlyEnabled() {
        return renderMode.isOnlyWireframe();
    }

    public boolean isWireframeEnabled() {
        return renderMode.isWireframe();
    }

    public ColorProvider getColorProviderFor(TriangulatedModel model) {
        return ColorProvider.forModel(model, this);
    }



    public Color getWireframeColor() {
        return wireframeColor;
    }

    public void setWireframeColor(Color color) {
        this.wireframeColor = color;
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }


    public Color getMaterialColor() {
        return modelManager.getMaterialColor();
    }

    public void setMaterialColor(Color color) {
        modelManager.setActiveModelColor(color);
    }


    public Lightning getLightning() {
        return lightning;
    }

    public Zbuffer getZbuffer() {
        return zbuffer;
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

    public float getAmbientStrength() {
        return lightning.getAmbientStrength();
    }

    public void setAmbientStrength(float strength) {
        lightning.setAmbientStrength(strength);
    }


    public void renderFrame(GraphicsContext gc, int width, int height) {
        Camera camera = cameraManager.getActiveCamera();

        camera.setAspectRatio((float) width/height);

        zbuffer.resize(width, height);
        zbuffer.clear();

        gc.clearRect(0, 0, width, height);

        List<TriangulatedModel> visibleModels = modelManager.getModels().stream()
                .filter(Model::isVisible)
                .filter(m -> m instanceof TriangulatedModel)
                .map(m -> (TriangulatedModel) m)
                .collect(Collectors.toList());

        RenderContext renderContext = new RenderContext(
                gc,
                camera,
                visibleModels,
                zbuffer,
                lightning,
                width,
                height,
                renderMode,
                wireframeColor,
                this
        );

        RenderEngine.render(renderContext);
    }
}
