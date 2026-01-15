package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.paint.Color;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import static math.vector.Vector3f.dotProduct;

@Component
public class Lightning {
    @Value("${render.default.light_color:#FFFFFF}")
    private String defaultLightColor;
    private Color lightColor;

    @Value("${render.default.enable_lighting:true}")
    private boolean enabled;

    @Value("${render.default.lightning.intensity:0.9}")
    private float intensity;

    @PostConstruct
    public void init() {
        this.lightColor = Color.web(defaultLightColor);
    }

    public Color calculateLightning(Color baseColor, Vector3f normal, Vector3f rayLight) {
        if (!enabled) {
            return baseColor;
        }

        float l = Math.max(0, -dotProduct(normal, rayLight));

        float factor = (1 - intensity) + (intensity * l);

        float r = (float) (baseColor.getRed() * factor);
        float b = (float) (baseColor.getBlue() * factor);
        float g = (float) (baseColor.getGreen() * factor);

        Color colorWithIntensity = new Color(r, g, b, baseColor.getOpacity());

        return applyToColor(colorWithIntensity);
    }


    public Color applyToColor(Color baseColor) {
        float r = (float) (baseColor.getRed() * lightColor.getRed());
        float g = (float) (baseColor.getGreen() * lightColor.getGreen());
        float b = (float) (baseColor.getBlue() * lightColor.getBlue());

        r = Math.min(1.0f, Math.max(0.0f, r));
        g = Math.min(1.0f, Math.max(0.0f, g));
        b = Math.min(1.0f, Math.max(0.0f, b));

        return new Color(r, g, b, baseColor.getOpacity());
    }

    public Color getLightColor() {
        return lightColor;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setLightColor(Color lightColor) {
        this.lightColor = lightColor;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIntensity(float intensity) {
        this.intensity = Math.max(0, Math.min(1, intensity));
    }
}
