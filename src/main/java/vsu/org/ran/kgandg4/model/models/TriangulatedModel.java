package vsu.org.ran.kgandg4.model.models;

import java.util.List;
import java.util.ArrayList;

import math.vector.Vector3f;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;

public class TriangulatedModel extends Model {

    public TriangulatedModel() {
        super(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
    }

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

    public Integer findPolygonContainingPoint(Vector3f point) {
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);

            if (polygon.getVertexIndices().size() != 3) {
                continue;
            }

            Triangle triangle = new Triangle(polygon);

            if (triangle.isInsideTriangle(point, this)) {
                return i;
            }
        }

        return null;
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