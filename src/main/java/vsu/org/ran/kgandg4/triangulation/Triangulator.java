package vsu.org.ran.kgandg4.triangulation;

import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;

import java.util.ArrayList;
import java.util.List;


public interface Triangulator {
    /**
     * Триангулирует переданную модель
     * @param model - пользовательская модель
     */
    default void triangulateModel(Model model) {
        ArrayList<Polygon> newPolygons = new ArrayList<>(Math.max(model.getVertices().size() - 2, 0));
        for (Polygon polygon : model.getPolygons()) {
            List<Polygon> clipped = triangulatePolygon(model, polygon);
            newPolygons.addAll(clipped);
        }
        model.setPolygons(newPolygons);
    }

    default TriangulatedModel createTriangulatedModel(Model model) {
        TriangulatedModel triangulatedModel = new TriangulatedModel();
        triangulatedModel.setVertices(new ArrayList<>(model.getVertices()));
        triangulatedModel.setNormals(new ArrayList<>(model.getNormals()));
        triangulatedModel.setTextureVertices(new ArrayList<>(model.getTextureVertices()));
        triangulatedModel.setPolygons(new ArrayList<>(model.getPolygons()));
        triangulateModel(triangulatedModel);
        return triangulatedModel;
    }

    List<Polygon> triangulatePolygon(Model model, Polygon polygon);
}
