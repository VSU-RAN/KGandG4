package vsu.org.ran.kgandg4.triangulation.utils;


import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

import math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PolygonUtil {
    public static Polygon createNewPolygon(
            List<Integer> vertexIndexes,
            Map<Integer, Integer> textureIndexesMap,
            Map<Integer, Integer> normalsIndexesMap
    ) {
        Polygon polygon = new Polygon(vertexIndexes);
        List<Integer> textureIndices = new ArrayList<>();
        List<Integer> normalsIndices = new ArrayList<>();
        vertexIndexes.forEach(index -> {
            textureIndices.add(textureIndexesMap.get(index));
            normalsIndices.add(normalsIndexesMap.get(index));
        });
        polygon.setTextureVertexIndices(textureIndices);
        polygon.setNormalIndices(normalsIndices);
        return polygon;
    }

    public static Polygon deepCopyOfPolygon(Polygon polygon) {
        List<Integer> verticesIndexes = new ArrayList<>(polygon.getVertexIndices());
        List<Integer> normalIndexes = new ArrayList<>(polygon.getNormalIndices());
        List<Integer> textureVerticesIndexes = new ArrayList<>(polygon.getTextureVertexIndices());
        return new Polygon(verticesIndexes, textureVerticesIndexes, normalIndexes);
    }

    public static float calcTrianglePolygonSquare(Polygon polygon, Model model) {
        List<Vector3f> vertices = polygon.getVertexIndices().stream().map(model.vertices::get).toList();
        if (vertices.size() != 3)
            throw new IllegalArgumentException("Method works only with triangles. Cnt given polygon vertices: "
                    + vertices.size());
        Vector3f first = new Vector3f(
                vertices.get(0).getX() - vertices.get(1).getX(),
                vertices.get(0).getY() - vertices.get(1).getY(),
                vertices.get(0).getZ() - vertices.get(1).getZ()
        );
        Vector3f second = new Vector3f(
                vertices.get(2).getX() - vertices.get(1).getX(),
                vertices.get(2).getY() - vertices.get(1).getY(),
                vertices.get(2).getZ() - vertices.get(1).getZ()
        );
        return (float) (0.5 * Math.sqrt(
                Math.pow(first.getY() * second.getZ() - second.getY() * first.getZ(), 2) +
                        Math.pow(first.getZ() * second.getX() - second.getZ() * first.getX(), 2) +
                        Math.pow(first.getX() * second.getY() - second.getX() * first.getY(), 2)
        ));
    }
}
