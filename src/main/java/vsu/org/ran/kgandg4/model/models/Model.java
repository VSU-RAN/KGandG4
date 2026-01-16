package vsu.org.ran.kgandg4.model.models;

import math.matrix.Matrix4f;
import math.vector.Vector2f;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

import java.util.*;


public class Model {
    protected int id;
    protected String name;
    protected boolean visible = true;

    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Vector3f rotation = new Vector3f(0, 0, 0); // в градусах
    protected Vector3f scale = new Vector3f(1, 1, 1);

    private Matrix4f cachedTransformMatrix;
    private boolean isTransformDirty = true;

    protected ArrayList<Vector3f> vertices = new ArrayList<>();
    protected ArrayList<Vector2f> textureVertices = new ArrayList<>();
    protected ArrayList<Vector3f> normals = new ArrayList<>();
    protected ArrayList<Polygon> polygons = new ArrayList<>();



    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void markTransformDirty() {
        this.isTransformDirty = true;
    }

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public void setNormals(ArrayList<Vector3f> normals) {
        this.normals = normals;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public Vector3f getPosition() {
        return new Vector3f(position.getX(), position.getY(), position.getZ());
    }

    public void setPosition(Vector3f position) {
        this.position = new Vector3f(position.getX(), position.getY(), position.getZ());
        this.isTransformDirty = true;
    }

    public Vector3f getRotation() {
        return new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ());
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = new Vector3f(rotation.getX(), rotation.getY(), rotation.getZ());
        this.isTransformDirty = true;
    }

    public Vector3f getScale() {
        return new Vector3f(scale.getX(), scale.getY(), scale.getZ());
    }

    public void setScale(Vector3f scale) {
        this.scale = new Vector3f(scale.getX(), scale.getY(), scale.getZ());
        this.isTransformDirty = true;
    }

    public void deleteVertices() {
        this.vertices.clear();
        this.isTransformDirty = true;
    }

    public void deletePolygons() {
        this.polygons.clear();
        this.isTransformDirty = true;
    }

    public void deleteTextureVertices() {
        this.textureVertices.clear();
        this.isTransformDirty = true;
    }

    public void deleteNormals() {
        this.normals.clear();
        this.isTransformDirty = true;
    }

    public void removeVertex(int vertexIndex) {
        if (vertexIndex < 0 || vertexIndex >= vertices.size()) {
            return;
        }

        // 1. Находим все полигоны, которые используют эту вершину
        Set<Integer> polygonsToRemove = new HashSet<>();
        Set<Integer> textureVerticesToRemove = new HashSet<>();
        Set<Integer> normalsToRemove = new HashSet<>();

        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if (polygon.getVertexIndices().contains(vertexIndex)) {
                polygonsToRemove.add(i);

                // Собираем индексы текстурных координат и нормалей, которые нужно удалить
                if (polygon.getTextureVertexIndices() != null) {
                    for (int texIdx : polygon.getTextureVertexIndices()) {
                        if (texIdx >= 0) textureVerticesToRemove.add(texIdx);
                    }
                }
                if (polygon.getNormalIndices() != null) {
                    for (int normIdx : polygon.getNormalIndices()) {
                        if (normIdx >= 0) normalsToRemove.add(normIdx);
                    }
                }
            }
        }

        // 2. Удаляем полигоны (в обратном порядке чтобы индексы не сдвигались)
        List<Integer> sortedPolygons = new ArrayList<>(polygonsToRemove);
        sortedPolygons.sort((a, b) -> Integer.compare(b, a));
        for (int polyIndex : sortedPolygons) {
            polygons.remove(polyIndex);
        }

        // 3. Удаляем вершину
        vertices.remove(vertexIndex);

        // 4. Удаляем неиспользуемые текстурные координаты и нормали
        removeUnusedTextureVertices(textureVerticesToRemove);
        removeUnusedNormals(normalsToRemove);

        // 5. Обновляем индексы вершин в оставшихся полигонах
        for (Polygon polygon : polygons) {
            updatePolygonIndicesAfterVertexRemoval(polygon, vertexIndex);
        }

        markTransformDirty();
    }

    /**
     * Обновляет индексы вершин, текстурных координат и нормалей в полигоне после удаления вершины
     */
    private void updatePolygonIndicesAfterVertexRemoval(Polygon polygon, int removedVertexIndex) {
        // Обновляем индексы вершин
        List<Integer> newVertexIndices = new ArrayList<>();
        for (int idx : polygon.getVertexIndices()) {
            if (idx > removedVertexIndex) {
                newVertexIndices.add(idx - 1);
            } else if (idx < removedVertexIndex) {
                newVertexIndices.add(idx);
            }
            // idx == removedVertexIndex не добавляем
        }
        polygon.setVertexIndices(newVertexIndices);

        // Обновляем индексы текстурных координат (если есть)
        if (polygon.getTextureVertexIndices() != null) {
            // Здесь нужно обновлять аналогично, если удаляли текстурные координаты
            // Но обычно текстурные координаты удаляются отдельно
        }

        // Обновляем индексы нормалей (если есть)
        if (polygon.getNormalIndices() != null) {
            // Аналогично для нормалей
        }
    }

    public void removePolygon(int polygonIndex) {
        if (polygonIndex < 0 || polygonIndex >= polygons.size()) {
            return;
        }

        // 1. Получаем полигон, который будем удалять
        Polygon polygonToRemove = polygons.get(polygonIndex);

        // 2. Собираем все вершины, которые используются ТОЛЬКО в этом полигоне
        Set<Integer> potentiallyUnusedVertices = new HashSet<>();
        Set<Integer> potentiallyUnusedTextureVertices = new HashSet<>();
        Set<Integer> potentiallyUnusedNormals = new HashSet<>();

        // Получаем индексы из удаляемого полигона
        if (polygonToRemove.getVertexIndices() != null) {
            potentiallyUnusedVertices.addAll(polygonToRemove.getVertexIndices());
        }
        if (polygonToRemove.getTextureVertexIndices() != null) {
            potentiallyUnusedTextureVertices.addAll(polygonToRemove.getTextureVertexIndices());
        }
        if (polygonToRemove.getNormalIndices() != null) {
            potentiallyUnusedNormals.addAll(polygonToRemove.getNormalIndices());
        }

        // 3. Удаляем полигон
        polygons.remove(polygonIndex);

        // 4. Проверяем, используются ли еще вершины/текстуры/нормали в других полигонах
        for (Polygon polygon : polygons) {
            if (polygon.getVertexIndices() != null) {
                potentiallyUnusedVertices.removeAll(polygon.getVertexIndices());
            }
            if (polygon.getTextureVertexIndices() != null) {
                potentiallyUnusedTextureVertices.removeAll(polygon.getTextureVertexIndices());
            }
            if (polygon.getNormalIndices() != null) {
                potentiallyUnusedNormals.removeAll(polygon.getNormalIndices());
            }
        }

        // 5. Удаляем полностью неиспользуемые вершины
        // (осторожно: не всегда нужно удалять вершины при удалении полигона)
        // Если хотите сохранять геометрию, пропустите этот шаг
        if (!potentiallyUnusedVertices.isEmpty()) {
            removeUnusedVertices(potentiallyUnusedVertices);
        }

        // 6. Удаляем неиспользуемые текстурные координаты и нормали
        removeUnusedTextureVertices(potentiallyUnusedTextureVertices);
        removeUnusedNormals(potentiallyUnusedNormals);

        // 7. Обновляем индексы в оставшихся полигонах после удаления элементов
        updatePolygonIndicesAfterRemoval();

        markTransformDirty();
    }

    /**
     * Удаляет набор вершин и обновляет индексы
     */
    private void removeUnusedVertices(Set<Integer> verticesToRemove) {
        if (verticesToRemove.isEmpty()) return;

        // Сортируем индексы по убыванию для безопасного удаления
        List<Integer> sortedIndices = new ArrayList<>(verticesToRemove);
        sortedIndices.sort((a, b) -> Integer.compare(b, a)); // по убыванию

        // Удаляем вершины
        for (int vertexIndex : sortedIndices) {
            if (vertexIndex >= 0 && vertexIndex < vertices.size()) {
                vertices.remove(vertexIndex);
            }
        }

        // После удаления вершин нужно обновить ВСЕ индексы во ВСЕХ полигонах
        for (Polygon polygon : polygons) {
            updatePolygonVertexIndicesAfterMultipleRemovals(polygon, sortedIndices);
        }
    }

    /**
     * Обновляет индексы вершин в полигоне после удаления нескольких вершин
     */
    private void updatePolygonVertexIndicesAfterMultipleRemovals(Polygon polygon, List<Integer> removedIndicesSortedDesc) {
        List<Integer> newIndices = new ArrayList<>();

        for (int originalIdx : polygon.getVertexIndices()) {
            int adjustedIdx = originalIdx;

            // Для каждого удаленного индекса (от большего к меньшему)
            for (int removedIdx : removedIndicesSortedDesc) {
                if (originalIdx > removedIdx) {
                    adjustedIdx--; // уменьшаем индекс
                } else if (originalIdx == removedIdx) {
                    adjustedIdx = -1; // помечаем как удаленный
                    break;
                }
            }

            if (adjustedIdx >= 0) {
                newIndices.add(adjustedIdx);
            }
        }

        polygon.setVertexIndices(newIndices);
    }

    /**
     * Удаляет неиспользуемые текстурные координаты и обновляет индексы
     */
    private void removeUnusedTextureVertices(Set<Integer> indicesToRemove) {
        if (indicesToRemove.isEmpty() || textureVertices.isEmpty()) return;

        // Создаем маппинг старых индексов на новые
        int[] indexMapping = new int[textureVertices.size()];
        List<Vector2f> newTextureVertices = new ArrayList<>();

        int newIndex = 0;
        for (int i = 0; i < textureVertices.size(); i++) {
            if (!indicesToRemove.contains(i)) {
                newTextureVertices.add(textureVertices.get(i));
                indexMapping[i] = newIndex++;
            } else {
                indexMapping[i] = -1; // помечаем как удаленный
            }
        }

        // Обновляем хранилище
        textureVertices = new ArrayList<>(newTextureVertices);

        // Обновляем индексы в полигонах
        for (Polygon polygon : polygons) {
            if (polygon.getTextureVertexIndices() != null) {
                List<Integer> newIndices = new ArrayList<>();
                for (int texIdx : polygon.getTextureVertexIndices()) {
                    if (texIdx >= 0 && texIdx < indexMapping.length && indexMapping[texIdx] != -1) {
                        newIndices.add(indexMapping[texIdx]);
                    }
                }
                polygon.setTextureVertexIndices(newIndices);
            }
        }
    }

    /**
     * Удаляет неиспользуемые нормали и обновляет индексы
     */
    private void removeUnusedNormals(Set<Integer> indicesToRemove) {
        if (indicesToRemove.isEmpty() || normals.isEmpty()) return;

        // Создаем маппинг старых индексов на новые
        int[] indexMapping = new int[normals.size()];
        List<Vector3f> newNormals = new ArrayList<>();

        int newIndex = 0;
        for (int i = 0; i < normals.size(); i++) {
            if (!indicesToRemove.contains(i)) {
                newNormals.add(normals.get(i));
                indexMapping[i] = newIndex++;
            } else {
                indexMapping[i] = -1; // помечаем как удаленный
            }
        }

        // Обновляем хранилище
        normals = new ArrayList<>(newNormals);

        // Обновляем индексы в полигонах
        for (Polygon polygon : polygons) {
            if (polygon.getNormalIndices() != null) {
                List<Integer> newIndices = new ArrayList<>();
                for (int normIdx : polygon.getNormalIndices()) {
                    if (normIdx >= 0 && normIdx < indexMapping.length && indexMapping[normIdx] != -1) {
                        newIndices.add(indexMapping[normIdx]);
                    }
                }
                polygon.setNormalIndices(newIndices);
            }
        }
    }

    private void updatePolygonIndicesAfterRemoval() {
        // Эта функция может быть пустой, если мы правильно обновляем индексы в других методах
        // или может содержать дополнительную логику валидации
    }

    public Integer findNearestVertex(Vector3f point, float threshold) {
        if (vertices.isEmpty()) {
            return null;
        }

        int nearestIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            float distance = calculateDistance(vertex, point);

            if (distance < minDistance && distance < threshold) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex >= 0 ? nearestIndex : null;
    }

    public Integer findPolygonContainingPoint(Vector3f point) {
        return null;
    }


    public float calculateSize() {
        if (vertices.isEmpty()) return 0.0f;

        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

        for (Vector3f vertex : vertices) {
            min = new Vector3f(
                    Math.min(min.getX(), vertex.getX()),
                    Math.min(min.getY(), vertex.getY()),
                    Math.min(min.getZ(), vertex.getZ())
            );
            max = new Vector3f(
                    Math.max(max.getX(), vertex.getX()),
                    Math.max(max.getY(), vertex.getY()),
                    Math.max(max.getZ(), vertex.getZ())
            );
        }

        float dx = max.getX() - min.getX();
        float dy = max.getY() - min.getY();
        float dz = max.getZ() - min.getZ();

        return (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    public float calculateAutoThreshold(float modelDefaultThreshold) {
        float size = calculateSize();
        if (size < 0.001f) return modelDefaultThreshold;
        return size * modelDefaultThreshold;
    }

    private float calculateDistance(Vector3f v1, Vector3f v2) {
        float dx = v1.getX() - v2.getX();
        float dy = v1.getY() - v2.getY();
        float dz = v1.getZ() - v2.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }


    private void clearNormalsAndTexturesIfNeeded() {
        if (!normals.isEmpty()) {
            normals.clear();
        }
        if (!textureVertices.isEmpty()) {
            textureVertices.clear();
        }
    }


    //todo: Можно добавить реализацию доставания TRS матрицы прямо отсюда


    @Override
    public String toString() {
        return "Модель загружена:" +
                "\nВершин: " + vertices.size() +
                "\nТекстурных вершин: " + textureVertices.size() +
                "\nНормалей: " + normals.size() +
                "\nПолигонов: " + polygons.size();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Model model = (Model) object;
        return id == model.id && Objects.equals(name, model.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
