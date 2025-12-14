package vsu.org.ran.kgandg4.model;

import java.util.ArrayList;
import java.util.List;

public class TriangulatedModel extends Model {
    public List<Triangle> getTriangles() {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() > 3)
                throw new IllegalArgumentException("Model is not good triangulated!");
            triangles.add(new Triangle(
                    vertices.get(polygon.getVertexIndices().get(0)),
                    vertices.get(polygon.getVertexIndices().get(1)),
                    vertices.get(polygon.getVertexIndices().get(2)
                    )));
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
}
