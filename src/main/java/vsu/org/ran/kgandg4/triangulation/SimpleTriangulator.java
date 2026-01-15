package vsu.org.ran.kgandg4.triangulation;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.triangulation.utils.PolygonUtil;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class SimpleTriangulator implements Triangulator {
    @Override
    public List<Polygon> triangulatePolygon(Model model, Polygon polygon) {
        // получаю вершины полигона в виде точек
        ArrayList<Integer> verticesIndexes = polygon.getVertexIndices();
        int indexOfVertexInPolygon = 0;
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        for (Integer vertexIndex : verticesIndexes) {
            if (indexOfVertexInPolygon < polygon.getTextureVertexIndices().size())
                textureIndexesMap.put(vertexIndex, polygon.getTextureVertexIndices().get(indexOfVertexInPolygon));
            if (indexOfVertexInPolygon < polygon.getNormalIndices().size())
                normalsIndexesMap.put(vertexIndex, polygon.getNormalIndices().get(indexOfVertexInPolygon));
            indexOfVertexInPolygon++;
        }

        // начинаю обработку вершин и создание новых полигонов
        List<Polygon> newPolygons = new ArrayList<>();
        int n = verticesIndexes.size();
        int firstVertexIndex = 0;
        int secondVertexIndex = 1;
        int thirdVertexIndex = 2;
        while (thirdVertexIndex < n) {
            Polygon newPolygon = PolygonUtil.createNewPolygon(List.of(
                    verticesIndexes.get(firstVertexIndex),
                    verticesIndexes.get(secondVertexIndex),
                    verticesIndexes.get(thirdVertexIndex)
            ), textureIndexesMap, normalsIndexesMap);
            newPolygons.add(newPolygon);
            secondVertexIndex++;
            thirdVertexIndex++;
        }
        return newPolygons;
    }
}
