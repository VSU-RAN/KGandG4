package vsu.org.ran.kgandg4.model.models;

import math.matrix.Matrix4f;
import math.vector.Vector2f;
import math.vector.Vector3f;

import java.util.ArrayList;
import java.util.Objects;

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
