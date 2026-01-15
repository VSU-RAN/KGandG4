package vsu.org.ran.kgandg4.model.models;

import java.util.List;
import java.util.ArrayList;

public class TriangulatedModel extends Model {
    public List<Triangle> getTriangles() {
        List<Triangle> triangles = new ArrayList<>();
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices().size() != 3)
                throw new IllegalArgumentException("Model is not good triangulated!");
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
        return super.toString();
    }
}
