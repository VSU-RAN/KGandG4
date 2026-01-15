package vsu.org.ran.kgandg4.model;

import math.vector.Vector3f;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModelTools {

    /**
     * Удаляет вершину по индексу и все связанные с ней полигоны
     */
    public static void removeVertex(Model model, int vertexIndex) {
        if (model == null || vertexIndex < 0 || vertexIndex >= model.getVertices().size()) {
            return;
        }

        // Находим все полигоны, которые используют эту вершину
        Set<Integer> polygonsToRemove = new HashSet<>();
        for (int i = 0; i < model.getPolygons().size(); i++) {
            Polygon polygon = model.getPolygons().get(i);
            if (polygon.getVertexIndices().contains(vertexIndex)) {
                polygonsToRemove.add(i);
            }
        }

        // Удаляем полигоны (в обратном порядке чтобы индексы не сдвигались)
        List<Integer> sorted = new ArrayList<>(polygonsToRemove);
        sorted.sort((a, b) -> Integer.compare(b, a)); // сортируем по убыванию
        for (int polyIndex : sorted) {
            model.getPolygons().remove(polyIndex);
        }

        // Удаляем вершину
        model.getVertices().remove(vertexIndex);

        // Обновляем индексы вершин в оставшихся полигонах
        for (Polygon polygon : model.getPolygons()) {
            List<Integer> newIndices = new ArrayList<>();
            for (int idx : polygon.getVertexIndices()) {
                if (idx > vertexIndex) {
                    newIndices.add(idx - 1); // уменьшаем индекс
                } else if (idx < vertexIndex) {
                    newIndices.add(idx); // оставляем как есть
                }
                // idx == vertexIndex - уже удален, не добавляем
            }
            polygon.setVertexIndices(newIndices);
        }
        model.markTransformDirty();

        // Также нужно обновить нормали и текстуры если они есть
        updateNormalsAndTextures(model);
    }

    /**
     * Удаляет полигон по индексу
     */
    public static void removePolygon(Model model, int polygonIndex) {
        if (model == null || polygonIndex < 0 || polygonIndex >= model.getPolygons().size()) {
            return;
        }

        model.getPolygons().remove(polygonIndex);
        model.markTransformDirty();

        updateNormalsAndTextures(model);
    }

    /**
     * Находит ближайшую вершину к точке в мировых координатах
     */
    public static Integer findNearestVertex(Model model, Vector3f point, float threshold) {
        if (model == null || model.getVertices().isEmpty()) {
            return null;
        }

        int nearestIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < model.getVertices().size(); i++) {
            Vector3f vertex = model.getVertices().get(i);
            float distance = calculateDistance(vertex, point);

            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex >= 0 ? nearestIndex : null;
    }

    /**
     * Находит ближайший полигон к точке
     */
    public static Integer findNearestPolygon(Model model, Vector3f point, float threshold) {
        if (model == null || model.getPolygons().isEmpty()) {
            return null;
        }

        int nearestIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < model.getPolygons().size(); i++) {
            var polygon = model.getPolygons().get(i);
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
            if (vertexIndex >= 0 && vertexIndex < model.getPolygons().size()) {
                Vector3f vertex = model.getVertices().get(vertexIndex);
                x += vertex.getX();
                y += vertex.getY();
                z += vertex.getZ();
                count++;
            }
        }

        return count > 0 ? new Vector3f(x / count, y / count, z / count) : new Vector3f(0, 0, 0);
    }

    /**
     * Обновляет нормали и текстурные координаты после удаления элементов
     */
    private static void updateNormalsAndTextures(Model model) {
        // Здесь можно добавить перерасчет нормалей
        // Пока просто очищаем если они есть
        if (!model.getNormals().isEmpty()) {
            model.getNormals().clear();
        }
    }
}