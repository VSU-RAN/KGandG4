package vsu.org.ran.kgandg4.model;

import math.vector.Vector3f;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

public class ModelTools {
    public static Integer findNearestPolygon(Model model, Vector3f point, float threshold) {
        if (model == null || model.getPolygons().isEmpty()) {
            return null;
        }

        int nearestIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon polygon = model.getPolygons().get(i);
            Vector3f center = calculatePolygonCenter(model, polygon);

            float distance = calculateDistance(center, point);
            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex >= 0 ? nearestIndex : null;
    }

    /**
     * Вычисляет расстояние между двумя точками
     */
    private static float calculateDistance(Vector3f v1, Vector3f v2) {
        float dx = v1.getX() - v2.getX();
        float dy = v1.getY() - v2.getY();
        float dz = v1.getZ() - v2.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Вычисляет центр полигона
     */
    private static Vector3f calculatePolygonCenter(Model model, Polygon polygon) {
        if (polygon.getVertexIndices().isEmpty()) {
            return new Vector3f(0, 0, 0);
        }

        float x = 0, y = 0, z = 0;
        int count = 0;

        for (int vertexIndex : polygon.getVertexIndices()) {
            if (vertexIndex >= 0 && vertexIndex < model.getVertices().size()) {
                Vector3f vertex = model.getVertices().get(vertexIndex);
                x += vertex.getX();
                y += vertex.getY();
                z += vertex.getZ();
                count++;
            }
        }

        return count > 0 ? new Vector3f(x / count, y / count, z / count) : new Vector3f(0, 0, 0);
    }
}
