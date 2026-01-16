package vsu.org.ran.kgandg4.model.models;

import javafx.scene.paint.Color;

import java.util.Objects;
import java.util.ArrayList;


import math.matrix.Matrix4f;
import math.vector.Vector2f;
import math.vector.Vector3f;
import math.vector.Vector4f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.gui.ConstantsAndStyles;
import vsu.org.ran.kgandg4.render_engine.Texture;

import vsu.org.ran.kgandg4.affineTransformations.AffineBuilder;
import vsu.org.ran.kgandg4.render_engine.GraphicConveyor;
import java.util.*;

public class Model {
    protected int id;
    protected String name;
    protected boolean visible = true;
    protected boolean active = false;
    protected Texture texture;


    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Vector3f rotation = new Vector3f(0, 0, 0); // в градусах
    protected Vector3f scale = new Vector3f(1, 1, 1);

    private Matrix4f cachedTransformMatrix;
    private boolean isTransformDirty = true;

    protected ArrayList<Vector3f> vertices = new ArrayList<>();
    protected ArrayList<Vector2f> textureVertices = new ArrayList<>();
    protected ArrayList<Vector3f> normals = new ArrayList<>();
    protected ArrayList<Polygon> polygons = new ArrayList<>();

    public Model() {
        this(ConstantsAndStyles.DEFAULT_MODEL_TEXT);
    }

    protected Model(String name) {
        this.name = name;
        this.texture = null;
    }

    protected Model(String name, Texture texture) {
        this.name = name;
        this.texture = texture;
    }
    public Model() {
        this.cachedTransformMatrix = Matrix4f.identityMatrix();
        this.isTransformDirty = false;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public boolean hasTexture() {
        return texture != null && texture.hasTexture();
    }

    public Color getColor(float u, float v, boolean useTexture) {
        return texture.getColor(u, v, useTexture);
    }

    public void setMaterialColor(Color color) {
        texture.setMaterialColor(color);
    }

    public Matrix4f getModelMatrix() {
        return Matrix4f.identityMatrix();
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

        // 1. Находим все полигоны (треугольники), которые используют эту вершину
        Set<Integer> polygonsToRemove = new HashSet<>();
        for (int i = 0; i < polygons.size(); i++) {
            Polygon polygon = polygons.get(i);
            if (polygon.getVertexIndices().contains(vertexIndex)) {
                polygonsToRemove.add(i);
            }
        }

        // 2. Удаляем полигоны (в обратном порядке чтобы индексы не сдвигались)
        List<Integer> sorted = new ArrayList<>(polygonsToRemove);
        sorted.sort((a, b) -> Integer.compare(b, a));
        for (int polyIndex : sorted) {
            polygons.remove(polyIndex);
        }

        // 3. Удаляем вершину
        vertices.remove(vertexIndex);

        // 4. Обновляем индексы вершин в оставшихся полигонах
        for (Polygon polygon : polygons) {
            updateVertexIndicesAfterRemoval(polygon, vertexIndex);
        }

        // 5. Очищаем неиспользуемые текстурные координаты и нормали
        clearUnusedTextureVerticesAndNormals();

        markTransformDirty();
    }

    /**
     * Обновляет индексы вершин в полигоне после удаления вершины
     */
    private void updateVertexIndicesAfterRemoval(Polygon polygon, int removedVertexIndex) {
        List<Integer> newIndices = new ArrayList<>();
        for (int idx : polygon.getVertexIndices()) {
            if (idx > removedVertexIndex) {
                newIndices.add(idx - 1); // уменьшаем индекс
            } else if (idx < removedVertexIndex) {
                newIndices.add(idx); // оставляем как есть
            }
            // idx == removedVertexIndex не добавляем
        }
        polygon.setVertexIndices(newIndices);

        // Индексы текстур и нормалей не меняем - они относятся к отдельным массивам
    }

    public void removePolygon(int polygonIndex) {
        if (polygonIndex < 0 || polygonIndex >= polygons.size()) {
            return;
        }

        // 1. Получаем удаляемый полигон для сбора информации
        Polygon removedPolygon = polygons.get(polygonIndex);

        // 2. Удаляем полигон
        polygons.remove(polygonIndex);

        // 3. Очищаем неиспользуемые текстурные координаты и нормали
        clearUnusedTextureVerticesAndNormals();

        // ВАЖНО: Не удаляем вершины! Они могут использоваться другими полигонами

        markTransformDirty();
    }

    /**
     * Очищает текстурные координаты и нормали, которые больше нигде не используются
     */
    private void clearUnusedTextureVerticesAndNormals() {
        // Для текстурных координат
        if (!textureVertices.isEmpty()) {
            Set<Integer> usedTextureIndices = new HashSet<>();
            for (Polygon polygon : polygons) {
                if (polygon.getTextureVertexIndices() != null) {
                    for (int texIdx : polygon.getTextureVertexIndices()) {
                        if (texIdx >= 0 && texIdx < textureVertices.size()) {
                            usedTextureIndices.add(texIdx);
                        }
                    }
                }
            }

            // Если есть текстурные координаты, но ни одна не используется
            if (usedTextureIndices.isEmpty()) {
                textureVertices.clear();
                // Очищаем индексы в полигонах
                for (Polygon polygon : polygons) {
                    polygon.setTextureVertexIndices(new ArrayList<>());
                }
            }
        }

        // Для нормалей
        if (!normals.isEmpty()) {
            Set<Integer> usedNormalIndices = new HashSet<>();
            for (Polygon polygon : polygons) {
                if (polygon.getNormalIndices() != null) {
                    for (int normIdx : polygon.getNormalIndices()) {
                        if (normIdx >= 0 && normIdx < normals.size()) {
                            usedNormalIndices.add(normIdx);
                        }
                    }
                }
            }

            // Если есть нормали, но ни одна не используется
            if (usedNormalIndices.isEmpty()) {
                normals.clear();
                // Очищаем индексы в полигонах
                for (Polygon polygon : polygons) {
                    polygon.setNormalIndices(new ArrayList<>());
                }
            }
        }
    }

    public Integer findNearestVertex(Vector3f point, float threshold) {
        if (vertices.isEmpty()) {
            return null;
        }

        int nearestIndex = -1;
        float minDistance = Float.MAX_VALUE;
        float thresholdSquared = threshold * threshold;

        for (int i = 0; i < vertices.size(); i++) {
            Vector3f vertex = vertices.get(i);
            float distanceSquared = calculateDistanceSquared(vertex, point);

            if (distanceSquared < minDistance && distanceSquared < thresholdSquared) {
                minDistance = distanceSquared;
                nearestIndex = i;
            }
        }

        return nearestIndex >= 0 ? nearestIndex : null;
    }

    /**
     * Быстрое вычисление квадрата расстояния (без извлечения корня)
     */
    private float calculateDistanceSquared(Vector3f v1, Vector3f v2) {
        float dx = v1.getX() - v2.getX();
        float dy = v1.getY() - v2.getY();
        float dz = v1.getZ() - v2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public Integer findPolygonContainingPoint(Vector3f point) {
        return null; // Для базового Model не реализовано
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

        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public float calculateAutoThreshold(float modelDefaultThreshold) {
        float size = calculateSize();
        if (size < 0.001f) return modelDefaultThreshold;
        return size * modelDefaultThreshold;
    }

    /**
     * Метод для обратной совместимости
     */
    private float calculateDistance(Vector3f v1, Vector3f v2) {
        return (float) Math.sqrt(calculateDistanceSquared(v1, v2));
    }

    /**
     * Очищает нормали и текстурные координаты, если они пустые или не используются
     * @deprecated Используйте {@link #clearUnusedTextureVerticesAndNormals()}
     */
    @Deprecated
    private void clearNormalsAndTexturesIfNeeded() {
        clearUnusedTextureVerticesAndNormals();
    }

    /**
     * Валидирует модель - проверяет все индексы на корректность
     */
    public boolean validate() {
        System.out.println("=== Валидация модели '" + name + "' ===");
        System.out.println("Вершин: " + vertices.size());
        System.out.println("Текстурных координат: " + textureVertices.size());
        System.out.println("Нормалей: " + normals.size());
        System.out.println("Полигонов: " + polygons.size());

        boolean isValid = true;

        // Проверяем все полигоны
        for (int i = 0; i < polygons.size(); i++) {
            Polygon poly = polygons.get(i);

            // Проверяем индексы вершин
            for (int vertIdx : poly.getVertexIndices()) {
                if (vertIdx < 0 || vertIdx >= vertices.size()) {
                    System.err.println("  ОШИБКА в полигоне " + i +
                            ": индекс вершины " + vertIdx + " вне границ!");
                    isValid = false;
                }
            }

            // Проверяем индексы текстурных координат
            if (poly.getTextureVertexIndices() != null && !poly.getTextureVertexIndices().isEmpty()) {
                for (int texIdx : poly.getTextureVertexIndices()) {
                    if (texIdx < 0 || texIdx >= textureVertices.size()) {
                        System.err.println("  ОШИБКА в полигоне " + i +
                                ": индекс текстуры " + texIdx + " вне границ!");
                        isValid = false;
                    }
                }
            }

            // Проверяем индексы нормалей
            if (poly.getNormalIndices() != null && !poly.getNormalIndices().isEmpty()) {
                for (int normIdx : poly.getNormalIndices()) {
                    if (normIdx < 0 || normIdx >= normals.size()) {
                        System.err.println("  ОШИБКА в полигоне " + i +
                                ": индекс нормали " + normIdx + " вне границ!");
                        isValid = false;
                    }
                }
            }
        }

        if (isValid) {
            System.out.println("Модель валидна ✓");
        } else {
            System.err.println("Модель содержит ошибки ✗");
        }

        return isValid;
    }

    /**
     * Метод построения матрицы модели: M = TRS.
     * @return матрица модели
     */
    public Matrix4f getCachedTransformMatrix() {
        if (isTransformDirty) {
            updateTransformMatrix();
        }
        return new Matrix4f(cachedTransformMatrix.copy());
    }

    /**
     * Метод построения матрицы модели: M = TRS.
     */
    private void updateTransformMatrix() {
        // Матрица трансформации
        AffineBuilder affineBuilder = new AffineBuilder();
        // 1. Масштабирование
        if (scale.getX() != 1.0f || scale.getY() != 1.0f || scale.getZ() != 1.0f) {
            affineBuilder.scale(scale.getX(), scale.getY(), scale.getZ());
        }
        // 2. Вращение (в градусах, нужно преобразовать в радианы)
        // Вращение через кватернионы (как в GraphicConveyor)
        if (rotation.getX() != 0.0f) {
            affineBuilder.rotateXQuat((float) Math.toRadians(rotation.getX()));
        }
        if (rotation.getY() != 0.0f) {
            affineBuilder.rotateYQuat((float) Math.toRadians(rotation.getY()));
        }
        if (rotation.getZ() != 0.0f) {
            affineBuilder.rotateZQuat((float) Math.toRadians(rotation.getZ()));
        }
        // 3. Перемещение
        if (position.getX() != 0.0f || position.getY() != 0.0f || position.getZ() != 0.0f) {
            affineBuilder.translate(position.getX(), position.getY(), position.getZ());
        }

        cachedTransformMatrix = affineBuilder.buildMatrix();
        isTransformDirty = false;
    }

    // Метод для получения трансформированных вершин (для сохранения)
    public ArrayList<Vector3f> getTransformedVertices() {
        Matrix4f transformMatrix = getCachedTransformMatrix();
        ArrayList<Vector3f> transformedVertices = new ArrayList<>();
        for (Vector3f vertex : vertices) {
            // Точка с w=1 для преобразования
            Vector4f point = new Vector4f(vertex);

            // Применяем трансформацию
            Vector4f transformed = transformMatrix.transformed(point);
            transformed.dehomogenize();

            transformedVertices.add(new Vector3f(
                    transformed.getX(),
                    transformed.getY(),
                    transformed.getZ()
            ));
        }

        return transformedVertices;
    }

    @Override
    public String toString() {
        return "Модель '" + name + "':" +
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
