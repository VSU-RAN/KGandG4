package vsu.org.ran.kgandg4.model.models;

import math.vector.Vector2f;
import math.vector.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel extends Model {
    public int id;
    public String name;
    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f rotation = new Vector3f(0, 0, 0); // в градусах
    public Vector3f scale = new Vector3f(1, 1, 1);

    // Показывать/скрывать модель
    public boolean visible = true;

    public List<Triangle> getTriangles() {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() != 3)
                throw new IllegalArgumentException("Модель плохо триангулирована!");
            triangles.add(new Triangle(polygon));
        }
        return triangles;
    }

    public boolean isValid() {
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() > 3)
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return (name != null ? name + "\n" : "") +
                "Вершин: " + vertices.size() +
                "\nТекстурных вершин: " + textureVertices.size() +
                "\nНормалей: " + normals.size() +
                "\nПолигонов: " + polygons.size();
    }
}