package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

@Component
public class MaterialSettings {
    @Value("${material.default.color:#4A90E2}")
    private String defaultColorString;

    private Color defaultMaterialColor;

    @PostConstruct
    public void init() {
        this.defaultMaterialColor = Color.web(defaultColorString);
    }

    public Color getDefaultMaterialColor() {
        return defaultMaterialColor;
    }

    public Texture createDefaultTexture() {
        return new Texture(defaultMaterialColor);
    }
}
