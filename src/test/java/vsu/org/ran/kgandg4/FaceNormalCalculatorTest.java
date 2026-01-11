package vsu.org.ran.kgandg4;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.model.Polygon;
import vsu.org.ran.kgandg4.normals.FaceNormalCalculator;

import math.vector.Vector3f; // вместо // import vsu.org.ran.kgandg4.math.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FaceNormalCalculatorTest {

    @Test
    void testCalculateTriangleNormal_XYPlane() {
        Vector3f v0 = new Vector3f(0, 0, 0);
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(0, 1, 0);

        FaceNormalCalculator calculator = new FaceNormalCalculator();

        Vector3f normal = calculator.calculateTriangleNormal(v0, v1, v2);

        assertEquals(1.0f, normal.length(), 0.001f, "Нормаль должна быть единичной длины");
        assertEquals(0.0f, normal.getX(), 0.001f, "X компонента должна быть 0");
        assertEquals(0.0f, normal.getY(), 0.001f, "Y компонента должна быть 0");

        float zComponent = normal.getZ();
        assertTrue(Math.abs(zComponent) == 1.0f, "Z компонента должна быть +1 или -1");
    }

    @Test
    void testCalculateTriangleNormal_XZPlane() {
        Vector3f v0 = new Vector3f(0, 0, 0);
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(0, 0, 1);

        FaceNormalCalculator calculator = new FaceNormalCalculator();
        Vector3f normal = calculator.calculateTriangleNormal(v0, v1, v2);

        assertEquals(1.0f, normal.length(), 0.001f);
        assertEquals(0.0f, normal.getX(), 0.001f);
        assertEquals(0.0f, normal.getZ(), 0.001f);

        float yComponent = normal.getY();
        assertTrue(Math.abs(yComponent) == 1.0f);
    }

    @Test
    void testCalculateTriangleNormal_YZPlane() {
        Vector3f v0 = new Vector3f(0, 0, 0);
        Vector3f v1 = new Vector3f(0, 1, 0);
        Vector3f v2 = new Vector3f(0, 0, 1);

        FaceNormalCalculator calculator = new FaceNormalCalculator();
        Vector3f normal = calculator.calculateTriangleNormal(v0, v1, v2);

        assertEquals(1.0f, normal.length(), 0.001f);
        assertEquals(0.0f, normal.getY(), 0.001f);
        assertEquals(0.0f, normal.getZ(), 0.001f);

        float xComponent = normal.getX();
        assertTrue(Math.abs(xComponent) == 1.0f);
    }

    @Test
    void testCalculateTriangleNormal_Degenerate() {
        Vector3f v0 = new Vector3f(0, 0, 0);
        Vector3f v1 = new Vector3f(1, 0, 0);
        Vector3f v2 = new Vector3f(2, 0, 0);

        FaceNormalCalculator calculator = new FaceNormalCalculator();
        Vector3f normal = calculator.calculateTriangleNormal(v0, v1, v2);
        assertNotNull(normal);
    }

    @Test
    void testCalculateNormals_FullModel() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(0, 1, 0));
        model.vertices.add(new Vector3f(0, 0, 1));

        Polygon face1 = new Polygon();
        face1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Polygon face2 = new Polygon();
        face2.setVertexIndices(new ArrayList<>(Arrays.asList(0, 2, 3)));

        Polygon face3 = new Polygon();
        face3.setVertexIndices(new ArrayList<>(Arrays.asList(0, 3, 1)));

        Polygon face4 = new Polygon();
        face4.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3)));

        model.polygons.add(face1);
        model.polygons.add(face2);
        model.polygons.add(face3);
        model.polygons.add(face4);

        FaceNormalCalculator calculator = new FaceNormalCalculator();
        calculator.calculateNormals(model);

        assertEquals(4, model.normals.size(), "Должно быть 4 нормали (по одной на грань)");

        for (int i = 0; i < model.normals.size(); i++) {
            Vector3f normal = model.normals.get(i);
            assertEquals(1.0f, normal.length(), 0.001f,
                    "Нормаль #" + i + " должна быть единичной. Длина: " + normal.length());
        }

        for (Polygon polygon : model.polygons) {
            assertNotNull(polygon.getNormalIndices(), "Полигон должен иметь индексы нормалей");
            assertEquals(3, polygon.getNormalIndices().size(), "Должно быть 3 индекса нормалей");

            List<Integer> normalIndices = polygon.getNormalIndices();
            assertEquals(normalIndices.get(0), normalIndices.get(1));
            assertEquals(normalIndices.get(1), normalIndices.get(2));
        }
    }

    @Test
    void testCalculateNormals_NonTriangleThrowsException() {
        Model model = new Model();
        model.vertices.add(new Vector3f(0, 0, 0));
        model.vertices.add(new Vector3f(1, 0, 0));
        model.vertices.add(new Vector3f(1, 1, 0));
        model.vertices.add(new Vector3f(0, 1, 0));

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        model.polygons.add(quad);

        FaceNormalCalculator calculator = new FaceNormalCalculator();

        assertThrows(IllegalArgumentException.class, () -> {
            calculator.calculateNormals(model);
        }, "Должно выбросить исключение для нетреугольного полигона");
    }
}