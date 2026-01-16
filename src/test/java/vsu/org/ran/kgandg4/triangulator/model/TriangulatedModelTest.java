package vsu.org.ran.kgandg4.triangulator.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.model.models.Triangle;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;

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
        validModel.getVertices().addAll(Arrays.asList(v1, v2, v3, v4));
        validModel.getPolygons().add(new Polygon(Arrays.asList(0, 1, 2)));
        validModel.getPolygons().add(new Polygon(Arrays.asList(1, 2, 3)));

        invalidModel = new TriangulatedModel();
        invalidModel.getVertices().addAll(Arrays.asList(v1, v2, v3, v4));
        invalidModel.getPolygons().add(new Polygon(Arrays.asList(0, 1, 2, 3)));
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
    public void testGetTrianglesInvalidModelFourVerticesPolygon() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> invalidModel.getTriangles());
    }
}
