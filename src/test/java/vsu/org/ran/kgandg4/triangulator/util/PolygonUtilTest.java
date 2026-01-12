package vsu.org.ran.kgandg4.triangulator.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.triangulation.utils.PolygonUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PolygonUtilTest {


    @Test
    public void testCreateNewPolygon() {
        List<Integer> vertexIndexes = Arrays.asList(0, 1, 2);
        Map<Integer, Integer> textureIndexesMap = new HashMap<>();
        textureIndexesMap.put(0, 10);
        textureIndexesMap.put(1, 11);
        textureIndexesMap.put(2, 12);

        Map<Integer, Integer> normalsIndexesMap = new HashMap<>();
        normalsIndexesMap.put(0, 100);
        normalsIndexesMap.put(1, 101);
        normalsIndexesMap.put(2, 102);
        Polygon newPolygon = PolygonUtil.createNewPolygon(vertexIndexes, textureIndexesMap, normalsIndexesMap);


        Assertions.assertEquals(vertexIndexes, newPolygon.getVertexIndices());

        List<Integer> expectedTextureIndices = Arrays.asList(10, 11, 12);
        Assertions.assertEquals(expectedTextureIndices, newPolygon.getTextureVertexIndices());

        List<Integer> expectedNormalIndices = Arrays.asList(100, 101, 102);
        Assertions.assertEquals(expectedNormalIndices, newPolygon.getNormalIndices());
    }


    @Test
    public void testDeepCopyOfPolygon_ShouldCreateIndependentCopy() {
        List<Integer> originalVertices = Arrays.asList(0, 1, 2);
        List<Integer> originalTextures = Arrays.asList(10, 11, 12);
        List<Integer> originalNormals = Arrays.asList(100, 101, 102);
        Polygon originalPolygon = new Polygon(originalVertices, originalTextures, originalNormals);
        Polygon copiedPolygon = PolygonUtil.deepCopyOfPolygon(originalPolygon);

        Assertions.assertEquals(originalVertices, copiedPolygon.getVertexIndices());
        Assertions.assertEquals(originalTextures, copiedPolygon.getTextureVertexIndices());
        Assertions.assertEquals(originalNormals, copiedPolygon.getNormalIndices());

        originalVertices.set(0, 999);
        originalPolygon.setVertexIndices(originalVertices);

        Assertions.assertNotEquals(999, copiedPolygon.getVertexIndices().get(0));
        Assertions.assertEquals(0, copiedPolygon.getVertexIndices().get(0));
    }
}

