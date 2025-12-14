package vsu.org.ran.kgandg4.math;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MathUtil {
    public static double[] solveByKramer(double a, double b, double c, double d, double v1, double v2) {
        double deltaMain = calcDetermination(
                a, b,
                c, d
        );
        if (deltaMain == 0) return new double[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        double delta1 = calcDetermination(
                v1, b,
                v2, d);
        double delta2 = calcDetermination(
                a, v1,
                c, v2
        );
        return new double[]{delta1 / deltaMain, delta2 / deltaMain};
    }

    private static double calcDetermination(double a11, double a12, double a21, double a22) {
        return a11 * a22 - a12 * a21;
    }

    public static double calcSquareByGeroneByVertices(double x0, double y0, double x1, double y1, double x2, double y2) {
        double AB = sqrt(pow(x0 - x1, 2) + pow(y0 - y1, 2));
        double AC = sqrt(pow(x0 - x2, 2) + pow(y0 - y2, 2));
        double BC = sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));

        double semiPerimeter = (AB + AC + BC) / 2;
        return sqrt(semiPerimeter * (semiPerimeter - AB) * (semiPerimeter - AC) * (semiPerimeter - BC));
    }

    public static Vector3f cross(Vector3f u, Vector3f v) {
        return new Vector3f(
                u.getY() * v.getZ() - u.getZ() * v.getY(),
                u.getZ() * v.getX() - u.getX() * v.getZ(),
                u.getX() * v.getY() - u.getY() * v.getX()
        );
    }
}
