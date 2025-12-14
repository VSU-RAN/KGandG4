package vsu.org.ran.kgandg4.triangulator.math;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vsu.org.ran.kgandg4.math.MathUtil;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.Triangle;
import vsu.org.ran.kgandg4.triangulation.utils.Constants;

public class MathUtilTest {
    private Triangle triangle = new Triangle(
            new Vector3f(0, 0, 0),
            new Vector3f(0, 50, 0),
            new Vector3f(50, 0, 0)
    );

    @Test
    public void insideTriangleTest() {
        Assertions.assertTrue(triangle.isInsideTriangle(new Vector3f(10, 10, 0)));
        Assertions.assertTrue(triangle.isInsideTriangle(new Vector3f(3, 40, 0)));
        Assertions.assertFalse(triangle.isInsideTriangle(new Vector3f(-10, 10, 0)));
        Assertions.assertFalse(triangle.isInsideTriangle(new Vector3f(10, -10, 0)));
    }

    @Test
    public void testStandardRightTriangle() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 3.0, y1 = 0.0;
        double x2 = 0.0, y2 = 4.0;
        double expectedArea = 6.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, Constants.EPS);
    }

    @Test
    public void testArbitraryTriangle() {
        double x0 = 1.0, y0 = 1.0;
        double x1 = 4.0, y1 = 5.0;
        double x2 = 6.0, y2 = 2.0;
        double expectedArea = 8.5;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, Constants.EPS);
    }

    @Test
    public void testCollinearPoints() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 1.0, y1 = 0.0;
        double x2 = 5.0, y2 = 0.0;
        double expectedArea = 0.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, Constants.EPS);
    }

    @Test
    public void testAllPointsSame() {
        double x0 = 1.0, y0 = 1.0;
        double x1 = 1.0, y1 = 1.0;
        double x2 = 1.0, y2 = 1.0;
        double expectedArea = 0.0;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, Constants.EPS);
    }

    @Test
    public void testFractionalCoordinates() {
        double x0 = 0.0, y0 = 0.0;
        double x1 = 2.5, y1 = 0.0;
        double x2 = 0.0, y2 = 3.0;
        double expectedArea = 3.75;
        double actualArea = MathUtil.calcSquareByGeroneByVertices(x0, y0, x1, y1, x2, y2);
        Assertions.assertEquals(expectedArea, actualArea, Constants.EPS);
    }
}
