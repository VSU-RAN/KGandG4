package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.render_engine.render.Scene;

@FunctionalInterface
public interface ColorProvider {
    Color getColor(float u, float v);

    static ColorProvider forModel(TriangulatedModel model, Scene scene) {
        Texture texture = model.getTexture();
        boolean isActive = model.isActive();

        if (isActive) {
            if (scene.isTextureEnabled() && texture.hasTexture()) {
                return texture::getTextureColor;
            }
            else {
                Color color = texture.getMaterialColor();
                return (u, v) -> color;
            }
        }
        else {
            Color inactiveColor = scene.getInactiveColor();
            if (scene.isTextureEnabled() && texture.hasTexture()) {
                return (u, v) -> desaturate(texture.getTextureColor(u, v), inactiveColor);
            } else {
                return (u, v) -> inactiveColor;
            }
        }
    }
    private static Color desaturate(Color original, Color tint) {
        return new Color(
                original.getRed() * tint.getRed(),
                original.getGreen() * tint.getGreen(),
                original.getBlue() * tint.getBlue(),
                original.getOpacity() * tint.getOpacity()
        );
    }
}
