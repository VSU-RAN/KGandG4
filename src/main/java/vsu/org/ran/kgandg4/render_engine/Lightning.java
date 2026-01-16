package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.paint.Color;

import math.vector.Vector3f;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;

import static math.vector.Vector3f.dotProduct;
import static math.vector.Vector3f.normalized;

@Component
public class Lightning {
    @Value("${render.default.light_color:#FFFFFF}")
    private String defaultLightColor;
    private Color lightColor;

    @Value("${render.default.enable_lighting:true}")
    private boolean enabled;

    @Value("${render.default.lightning.intensity:0.9}")
    private float intensity;

    @Value("${render.default.lightning.ambient_strength:0.3}")
    private float ambientStrength;

    @PostConstruct
    public void init() {
        this.lightColor = Color.web(defaultLightColor);
    }

    public Color calculateLightning(Color baseColor, Vector3f normal, Vector3f rayLight) {
        if (!enabled) {
            return baseColor;
        }

        Vector3f N = normalized(normal);
        Vector3f L = normalized(rayLight);

        //diffuse = max(0, N·L)
        float diffuse = Math.max(0.0f, -dotProduct(N, L));


        //  lighting = ambient + diffuse * intensity
        float lighting = ambientStrength + (diffuse * intensity);

        lighting = Math.min(1.0f, Math.max(0.0f, lighting));

        Color illuminated = applyToColor(baseColor, lighting);

        if (!lightColor.equals(Color.WHITE)) {
            illuminated = multiplyColors(illuminated, lightColor);
        }

        return illuminated;
    }

    private Color applyToColor(Color color, float lighting) {
        return new Color(
                (float) (color.getRed() * lighting),
                (float) (color.getGreen() * lighting),
                (float) (color.getBlue() * lighting),
                (float) color.getOpacity()
        );
    }

    private Color multiplyColors(Color a, Color b) {
        return new Color(
                a.getRed() * b.getRed(),
                a.getGreen() * b.getGreen(),
                a.getBlue() * b.getBlue(),
                a.getOpacity() * b.getOpacity()
        );
    }

    public float getAmbientStrength() {
        return ambientStrength;
    }

    public void setAmbientStrength(float ambientStrength) {
        this.ambientStrength = Math.max(0.05f, Math.min(0.5f, ambientStrength));
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0, Math.min(1, intensity));
    }

    public Color getLightColor() {
        return lightColor;
    }

    public void setLightColor(Color lightColor) {
        this.lightColor = lightColor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
