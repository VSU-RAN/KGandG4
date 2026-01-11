package vsu.org.ran.kgandg4.triangulator.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.Polygon;
import vsu.org.ran.kgandg4.model.Triangle;
import vsu.org.ran.kgandg4.model.TriangulatedModel;

import math.vector.Vector3f; // вместо // import vsu.org.ran.kgandg4.math.Vector3f;

import java.util.Arrays;
import java.util.List;

public class TriangulatedModelTest {
    private TriangulatedModel validModel;
    private TriangulatedModel invalidModel;

    private final Vector3f v1 = new Vector3f(0, 0, 0);
    private final Vector3f v2 = new Vector3f(1, 0, 0);
    private final Vector3f v3 = new Vector3f(0, 1, 0);
    private final Vector3f v4 = new Vector3f(1, 1, 0);


    @BeforeEach
    public void setUp() {
        validModel = new TriangulatedModel();
        validModel.vertices.addAll(Arrays.asList(v1, v2, v3, v4));
        validModel.polygons.add(new Polygon(Arrays.asList(0, 1, 2)));
        validModel.polygons.add(new Polygon(Arrays.asList(1, 2, 3)));

        invalidModel = new TriangulatedModel();
        invalidModel.vertices.addAll(Arrays.asList(v1, v2, v3, v4));
        invalidModel.polygons.add(new Polygon(Arrays.asList(0, 1, 2, 3)));
    }

    @Test
    public void testIsValidValidModel() {
        boolean result = validModel.isValid();
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsValidInvalidModelFourVerticesPolygon() {
        boolean result = invalidModel.isValid();
        Assertions.assertFalse(result);
    }

    @Test
    public void testIsValidEmptyModel() {
        TriangulatedModel emptyModel = new TriangulatedModel();
        boolean result = emptyModel.isValid();
        Assertions.assertTrue(result);
    }

    @Test
    public void testGetTrianglesValidModel() {
        List<Triangle> triangles = validModel.getTriangles();

        Assertions.assertNotNull(triangles);
        Assertions.assertEquals(2, triangles.size());

        Triangle t1 = triangles.get(0);
        Assertions.assertEquals(v1, t1.getPointByIndex(0));
        Assertions.assertEquals(v2, t1.getPointByIndex(1));
        Assertions.assertEquals(v3, t1.getPointByIndex(2));

        Triangle t2 = triangles.get(1);
        Assertions.assertEquals(v2, t2.getPointByIndex(0));
        Assertions.assertEquals(v3, t2.getPointByIndex(1));
        Assertions.assertEquals(v4, t2.getPointByIndex(2));
    }

    @Test
    public void testGetTrianglesInvalidModelFourVerticesPolygon() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> invalidModel.getTriangles());
    }
}
