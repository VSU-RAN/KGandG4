package vsu.org.ran.kgandg4.normals;

import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

import java.util.ArrayList;
import java.util.List;

@Component
public class FaceNormalCalculator implements NormalCalculator{
    @Override
    public void calculateNormals(Model model) {
        model.normals.clear();

        for (Polygon polygon : model.polygons) {
            ArrayList<Integer> vertexIndeces = polygon.getVertexIndices();

            if (vertexIndeces.size() != 3) {
                throw new IllegalArgumentException(
                        "Полигон не является треугольником после триангуляции"
                );
            }

            Vector3f v0 = model.vertices.get(vertexIndeces.get(0));
            Vector3f v1 = model.vertices.get(vertexIndeces.get(1));
            Vector3f v2 = model.vertices.get(vertexIndeces.get(2));

            Vector3f normal  = calculateTriangleNormal(v0, v1, v2);

            model.normals.add(normal);

            int normalIndex = model.normals.size() - 1;


            polygon.setNormalIndices(List.of(normalIndex, normalIndex, normalIndex));
        }
    }

    public Vector3f calculateTriangleNormal(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f u = v1.subtract(v0);
        Vector3f v = v2.subtract(v0);

        Vector3f n = Vector3f.crossProduct(u, v);
        n.normalize();

        return n;
    }
}
