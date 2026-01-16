package vsu.org.ran.kgandg4.model.models;

import math.vector.Vector2f;
import math.vector.Vector3f;

public class Triangle extends Polygon {

    public Triangle(Polygon polygon) {
        if (polygon.getVertexIndices().size() != 3) {
            throw new IllegalArgumentException("Треугольник должен иметь 3 вершины");
        }
        this.vertexIndices = polygon.getVertexIndices();
        this.textureVertexIndices = polygon.getTextureVertexIndices();
        this.normalIndices = polygon.getNormalIndices();
    }

    public Vector3f[] getWorldVertices(Model model) {
        Vector3f[] vertices = new Vector3f[3];
        for (int i = 0; i < 3; i++) {
            vertices[i] = model.vertices.get(vertexIndices.get(i));
        }
        return vertices;
    }

    public Vector3f getWorldVertex(int index, Model model) {
        return model.vertices.get(vertexIndices.get(index));
    }

    public Vector2f getTextureCord(int index, Model model) {
        // Безопасный доступ с проверкой всех границ
        if (model == null || model.textureVertices == null) {
            return getDefaultTextureCoord(index);
        }

        if (textureVertexIndices == null || textureVertexIndices.isEmpty()) {
            return getDefaultTextureCoord(index);
        }

        if (index < 0 || index >= textureVertexIndices.size()) {
            System.err.println("Triangle.getTextureCord: индекс " + index +
                    " вне границ textureVertexIndices");
            return getDefaultTextureCoord(index);
        }

        int texIdx = textureVertexIndices.get(index);

        if (texIdx < 0 || texIdx >= model.textureVertices.size()) {
            System.err.println("Triangle.getTextureCord: индекс текстуры " + texIdx +
                    " вне границ массива текстур (" +
                    model.textureVertices.size() + ")");
            return getDefaultTextureCoord(index);
        }

        return model.textureVertices.get(texIdx);
    }

    /**
     * Возвращает дефолтные текстурные координаты
     */
    private Vector2f getDefaultTextureCoord(int index) {
        switch (index) {
            case 0: return new Vector2f(0, 0);
            case 1: return new Vector2f(1, 0);
            case 2: return new Vector2f(0, 1);
            default: return new Vector2f(0, 0);
        }
    }

    public Vector3f getNormal(int index, Model model) {
        // Безопасный доступ
        if (model == null || model.normals == null) {
            return null;
        }

        if (normalIndices == null || normalIndices.isEmpty()) {
            return null;
        }

        if (index < 0 || index >= normalIndices.size()) {
            return null;
        }

        int normIdx = normalIndices.get(index);
        if (normIdx < 0 || normIdx >= model.normals.size()) {
            return null;
        }

        return model.normals.get(normIdx);
    }

    public boolean isInsideTriangle(Vector3f point, Model model) {
        Vector3f[] vertices = getWorldVertices(model);
        return isPointInTriangle(point, vertices[0], vertices[1], vertices[2]);
    }

    private boolean isPointInTriangle(Vector3f p, Vector3f a, Vector3f b, Vector3f c) {
        Vector3f v0 = b.subtract(a);
        Vector3f v1 = c.subtract(a);
        Vector3f v2 = p.subtract(a);

        float dot00 = Vector3f.dotProduct(v0, v0);
        float dot01 = Vector3f.dotProduct(v0, v1);
        float dot02 = Vector3f.dotProduct(v0, v2);
        float dot11 = Vector3f.dotProduct(v1, v1);
        float dot12 = Vector3f.dotProduct(v1, v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v < 1);
    }
}