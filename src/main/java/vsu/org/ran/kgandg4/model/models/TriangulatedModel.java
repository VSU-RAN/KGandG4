package vsu.org.ran.kgandg4.model.models;


import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel extends Model {
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