package vsu.org.ran.kgandg4.triangulation;

import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.model.Polygon;
import vsu.org.ran.kgandg4.model.TriangulatedModel;

import java.util.ArrayList;
import java.util.List;


public interface Triangulator {
    /**
     * Триангулирует переданную модель
     * @param model - пользовательская модель
     */
    default void triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>(Math.max(model.vertices.size() - 2, 0));
        for (Polygon polygon : model.polygons) {
            List<Polygon> clipped = triangulatePolygon(model, polygon);
            newPolygons.addAll(clipped);
        }
        model.polygons = newPolygons;
    }

    default TriangulatedModel createTriangulatedModel(Model model) {
        TriangulatedModel triangulatedModel = new TriangulatedModel();
        triangulatedModel.vertices = new ArrayList<>(model.vertices);
        triangulatedModel.normals = new ArrayList<>(model.normals);
        triangulatedModel.textureVertices = new ArrayList<>(model.textureVertices);
        triangulatedModel.polygons = new ArrayList<>(model.polygons);
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }

    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
