package vsu.org.ran.kgandg4.model.models;

import java.util.ArrayList;
import java.util.Objects;

import math.matrix.Matrix4f;
import math.vector.Vector2f;
import math.vector.Vector3f;
import math.vector.Vector4f;

import vsu.org.ran.kgandg4.affineTransformations.AffineBuilder;
import vsu.org.ran.kgandg4.render_engine.GraphicConveyor;

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

    // todo: Можно добавить реализацию доставания TRS матрицы прямо отсюда

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

    public ArrayList<Vector3f> getOriginalVertices() {
        return new ArrayList<>(vertices); // Копия исходных вершин
    }

    public Matrix4f getTRS() {
        return GraphicConveyor.rotateScaleTranslate();
    }

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
