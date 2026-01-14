package vsu.org.ran.kgandg4.model;

import math.vector.Vector2f;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.model.models.Polygon;

import java.util.ArrayList;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();

    @Override
    public String toString() {
        return "Модель загружена" +
                "\nВершин:" + vertices +
                "\ntextureVertices=" + textureVertices +
                "\nНормалей:" + normals +
                "\nТекстур:" + polygons;
    }
}
