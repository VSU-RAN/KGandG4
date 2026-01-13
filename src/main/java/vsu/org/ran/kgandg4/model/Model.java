package vsu.org.ran.kgandg4.model;

import javafx.scene.image.Image;
import vsu.org.ran.kgandg4.math.Vector2f;
import vsu.org.ran.kgandg4.math.Vector3f;

import java.util.ArrayList;

public class Model {
    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    public Image texture;

    public String getTextureStatus() {
        if (texture == null) {
            return "null";
        } else if (texture.isError()) {
            return "error: " + texture.getException().getMessage();
        } else if (texture.getWidth() <= 0 || texture.getHeight() <= 0) {
            return "loading... progress: " + (texture.getProgress() * 100) + "%";
        } else {
            return String.format("%.0fx%.0f", texture.getWidth(), texture.getHeight());
        }
    }
}