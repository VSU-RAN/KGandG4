package vsu.org.ran.kgandg4.rasterization;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.render_engine.ColorProvider;
import vsu.org.ran.kgandg4.render_engine.Lightning;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;
import vsu.org.ran.kgandg4.render_engine.render.RenderMode;

import static java.lang.Math.*;
import static math.vector.Vector3f.dotProduct;

public class Rasterization {
    public static void drawTriangle(PixelWriter pw, Zbuffer zbuffer, Texture texture,
                                    Vector3f ray, Lightning lightning, ColorProvider colorProvider,
                                    int x0, int y0, float z0, float u0, float v0, Vector3f n0,
                                    int x1, int y1, float z1, float u1, float v1, Vector3f n1,
                                    int x2, int y2, float z2, float u2, float v2, Vector3f n2) {
            int tmp;
            float tmpF;
            Vector3f tmpVec;

            if (y0 > y1) {
                tmp = y1; y1 = y0; y0 = tmp;
                tmp = x1; x1 = x0; x0 = tmp;

                tmpF = z1; z1 = z0; z0 = tmpF;
                tmpF = u1; u1 = u0; u0 = tmpF;
                tmpF = v1; v1 = v0; v0 = tmpF;

                tmpVec = n1;
                n1 = n0;
                n0 = tmpVec;
            }
            if (y1 > y2) {
                tmp = y2; y2 = y1; y1 = tmp;
                tmp = x2; x2 = x1; x1 = tmp;

                tmpF = z2; z2 = z1; z1 = tmpF;
                tmpF = u2; u2 = u1; u1 = tmpF;
                tmpF = v2; v2 = v1; v1 = tmpF;

                tmpVec = n2;
                n2 = n1;
                n1 = tmpVec;
            }
            if (y0 > y1) {
                tmp = y1; y1 = y0; y0 = tmp;
                tmp = x1; x1 = x0; x0 = tmp;

                tmpF = z1; z1 = z0; z0 = tmpF;
                tmpF = u1; u1 = u0; u0 = tmpF;
                tmpF = v1; v1 = v0; v0 = tmpF;

                tmpVec = n1;
                n1 = n0;
                n0 = tmpVec;
            }

        float u0OverZ = u0 / z0;
        float v0OverZ = v0 / z0;
        float u1OverZ = u1 / z1;
        float v1OverZ = v1 / z1;
        float u2OverZ = u2 / z2;
        float v2OverZ = v2 / z2;

        int minX = min(x0, min(x1, x2));
        int maxX = max(x0, max(x1, x2));


        if (y0 == y1 && y1 == y2) {
            for (int x = minX; x <= max(x0, max(x1, x2)); x++) {
                double[] barycentric = findBarycentricCords(x, y0, x0, y0, x1, y1, x2, y2);
                drawPixel(pw, zbuffer, texture, ray, lightning, colorProvider,
                        x, y0,
                        barycentric[0], barycentric[1], barycentric[2],
                        z0, z1, z2,
                        u0OverZ, u1OverZ, u2OverZ,
                        v0OverZ, v1OverZ, v2OverZ,
                        n0, n1, n2);
            }
        }

        double dxEvenlyDistributedLong = y2 - y0 != 0 ? (double) (x2 - x0) / (y2 - y0) : 0;
        double dxEvenlyDistributedShort1 = y1 - y0 != 0 ? (double) (x1 - x0) / (y1 - y0) : 0;
        double dxEvenlyDistributedShort2 = y2 - y1 != 0 ? (double) (x2 - x1) / (y2 - y1) : 0;

        double longSide = x0;
        double shortSide = x0;

        double nextLongSide = longSide;
        double nextShortSide = shortSide;

        for (int y = y0; y <= y1; y++) {
            nextLongSide = longSide + dxEvenlyDistributedLong;
            nextShortSide = shortSide + dxEvenlyDistributedShort1;

            int xStart = (int) Math.floor(min(shortSide, longSide));
            int xEnd = (int) Math.ceil(max(shortSide, longSide));
            if (y != y1) {
                if (longSide > shortSide) {
                    if (longSide + 1 < nextLongSide && longSide + 1 < nextShortSide) {
                        xEnd = (int) Math.floor(min(nextLongSide, nextShortSide));
                    } else if (shortSide - 1 > nextLongSide && shortSide - 1 > nextShortSide) {
                        xStart = (int) Math.ceil(max(nextLongSide, nextShortSide));
                    }
                } else {
                    if (shortSide + 1 < nextLongSide && shortSide + 1 < nextShortSide) {
                        xEnd = (int) Math.floor(min(nextLongSide, nextShortSide));
                    } else if (longSide - 1 > nextLongSide && longSide - 1 > nextShortSide) {
                        xStart = (int) Math.ceil(max(nextLongSide, nextShortSide));
                    }
                }
            }
            xStart = max(xStart, minX);
            xEnd = min(xEnd, maxX);
            for (int x = xStart; x <= xEnd; x++) {
                double[] barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                drawPixel(pw, zbuffer, texture, ray, lightning, colorProvider,
                        x, y,
                        barycentric[0], barycentric[1], barycentric[2],
                        z0, z1, z2,
                        u0OverZ, u1OverZ, u2OverZ,
                        v0OverZ, v1OverZ, v2OverZ,
                        n0, n1, n2);
            }

            longSide = nextLongSide;
            shortSide = nextShortSide;
        }
        shortSide = x1;
        longSide -= dxEvenlyDistributedLong;
        for (int y = y1; y <= y2; y++) {
            nextLongSide = longSide + dxEvenlyDistributedLong;
            nextShortSide = shortSide + dxEvenlyDistributedShort2;

            int xStart = (int) Math.floor(min(shortSide, longSide));
            int xEnd = (int) Math.ceil(max(shortSide, longSide));

            if (y != y2) {
                if (longSide > shortSide) {
                    if (longSide + 1 < nextLongSide && longSide + 1 < nextShortSide) {
                        xEnd = (int) Math.floor(min(nextLongSide, nextShortSide));
                    } else if (shortSide - 1 > nextLongSide && shortSide - 1 > nextShortSide) {
                        xStart = (int) Math.ceil(max(nextLongSide, nextShortSide));
                    }
                } else {
                    if (shortSide + 1 < nextLongSide && shortSide + 1 < nextShortSide) {
                        xEnd = (int) Math.floor(min(nextLongSide, nextShortSide));
                    } else if (longSide - 1 > nextLongSide && longSide - 1 > nextShortSide) {
                        xStart = (int) Math.ceil(max(nextLongSide, nextShortSide));
                    }
                }
            }
            xStart = max(xStart, minX);
            xEnd = min(xEnd, maxX);
            for (int x = xStart; x <= xEnd; x++) {
                double[] barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                drawPixel(pw, zbuffer, texture, ray, lightning, colorProvider,
                        x, y,
                        barycentric[0], barycentric[1], barycentric[2],
                        z0, z1, z2,
                        u0OverZ, u1OverZ, u2OverZ,
                        v0OverZ, v1OverZ, v2OverZ,
                        n0, n1, n2);
            }

            longSide = nextLongSide;
            shortSide = nextShortSide;
        }
    }

    private static void drawPixel(
            PixelWriter pw, Zbuffer zbuffer, Texture texture,
            Vector3f ray, Lightning lightning, ColorProvider colorProvider,
            int x, int y,
            double alpha, double beta, double gamma,
            float z0, float z1, float z2,
            float u0OverZ, float u1OverZ, float u2OverZ,
            float v0OverZ, float v1OverZ, float v2OverZ,
            Vector3f n0, Vector3f n1, Vector3f n2) {

        if (alpha >= -0.0001 && beta >= -0.0001 && gamma >= -0.0001) {
            float oneOverZ = (float)(alpha * 1.0f / z0 + beta * 1.0f / z1 + gamma * 1.0f / z2);
            float z_interpolated = 1.0f / oneOverZ;

            if (zbuffer.testPointAndSet(x, y, z_interpolated)) {
                // Перспективно-корректная интерполяция
                float uOverZ = (float) (alpha * u0OverZ + beta * u1OverZ + gamma * u2OverZ);
                float vOverZ = (float) (alpha * v0OverZ + beta * v1OverZ + gamma * v2OverZ);

                float u = uOverZ * z_interpolated;
                float v = vOverZ * z_interpolated;

                // Получаем цвет
                Color color = colorProvider.getColor(u, v);

                Vector3f n = new Vector3f(
                        (float) (alpha * n0.getX() + beta * n1.getX() + gamma * n2.getX()),
                        (float) (alpha * n0.getY() + beta * n1.getY() + gamma * n2.getY()),
                        (float) (alpha * n0.getZ() + beta * n1.getZ() + gamma * n2.getZ())
                );
                n.normalize();

                // Применяем освещение (Lightning возвращает исходный цвет или преобразованный)
                color = lightning.calculateLightning(color, n, ray);

                pw.setColor(x, y, color);
            }
        }
    }

    public static void drawLine(PixelWriter pixelWriter, Zbuffer zbuffer, Color color,
                                double x1, double y1, double z1,
                                double x2, double y2, double z2) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        if (Math.abs(dx) > Math.abs(dy)) {
            if (x1 > x2) {
                double tmpX = x2; x2 = x1; x1 = tmpX;
                double tmpY = y2; y2 = y1; y1 = tmpY;
                double tmpZ = z2; z2 = z1; z1 = tmpZ;
            }

            double slope = dy / dx;
            double zSlope = dz / dx;

            double y = y1;
            double z = z1;

            int startX = (int)Math.round(x1);
            int endX = (int)Math.round(x2);

            for (int x = startX; x <= endX; x++) {
                double currentZ = z;

                float wireframeZ = (float)currentZ - 0.01f;

                if (zbuffer.testPointAndSet(x, (int)Math.round(y), wireframeZ)) {
                    pixelWriter.setColor(x, (int)Math.round(y), color);
                }

                y += slope;
                z += zSlope;
            }
        } else {
            if (y1 > y2) {
                double tmpX = x2; x2 = x1; x1 = tmpX;
                double tmpY = y2; y2 = y1; y1 = tmpY;
                double tmpZ = z2; z2 = z1; z1 = tmpZ;
            }

            double slope = dx / dy;
            double zSlope = dz / dy;

            double x = x1;
            double z = z1;

            int startY = (int)Math.round(y1);
            int endY = (int)Math.round(y2);

            for (int y = startY; y <= endY; y++) {
                double currentZ = z;
                float wireframeZ = (float)currentZ - 0.01f;

                if (zbuffer.testPointAndSet((int)Math.round(x), y, wireframeZ)) {
                    pixelWriter.setColor((int)Math.round(x), y, color);
                }

                x += slope;
                z += zSlope;
            }
        }
    }

    /**
     * Wireframe треугольник с Z-буфером
     */
    public static void drawWireFrameTriangle(PixelWriter pixelWriter, Zbuffer zbuffer, Color color,
                                             double x0, double y0, double z0,
                                             double x1, double y1, double z1,
                                             double x2, double y2, double z2) {
        drawLine(pixelWriter, zbuffer, color, x0, y0, z0, x1, y1, z1);
        drawLine(pixelWriter, zbuffer, color, x0, y0, z0, x2, y2, z2);
        drawLine(pixelWriter, zbuffer, color, x2, y2, z2, x1, y1, z1);
    }

    private static int findThirdOrderDeterminant(
            int a00, int a01, int a02,
            int a10, int a11, int a12,
            int a20, int a21, int a22
    ) {
        return ((a00 * a11 * a22) + (a10 * a21 * a02) + (a01 * a12 * a20)) - ((a02 * a11 * a20) + (a01 * a10 * a22) + (a12 * a21 * a00));
    }

    private static double[] findBarycentricCords(int xCur, int yCur, int x0, int y0, int x1, int y1, int x2, int y2) {
        int mainDet = findThirdOrderDeterminant(
                x0, x1, x2,
                y0, y1, y2,
                1, 1, 1
        );
        if (mainDet == 0) return new double[]{0, 0, 0};

        int detForAlpha = findThirdOrderDeterminant(
                xCur, x1, x2,
                yCur, y1, y2,
                1, 1, 1
        );
        int detForBeta = findThirdOrderDeterminant(
                x0, xCur, x2,
                y0, yCur, y2,
                1, 1, 1
        );
        int detForLambda = findThirdOrderDeterminant(
                x0, x1, xCur,
                y0, y1, yCur,
                1, 1, 1
        );
        return new double[]{(double) detForAlpha / mainDet, (double) detForBeta / mainDet, (double) detForLambda / mainDet};
    }















//
//    public static Map<Integer, List<Integer>> myBresenhamOneY(int x0, int y0, int x1, int y1) {
//        Map<Integer, List<Integer>> points = new HashMap<>();
//        int dx = abs(x0 - x1);
//        int dy = abs(y0 - y1);
//
//        int x = x0, y = y0;
//        int error = 0;
//        int stepX = 1;
//        int stepY = 1;
//        if (x1 - x0 < 0) {
//            stepX *= -1;
//        }
//        if (y1 - y0 < 0) {
//            stepY *= -1;
//        }
//        if (dx > dy) {
//            // тогда я меняю на каждой итерации x, а для y коплю ошибки
//            // тогда я могу равномерно разделить подъем, когда иду по y. То есть на каждом шаге по x надо на какое то нецелое
//            // число, которое меньше нуля (dx > dy) менять y. Но это не целое. Поэтому я умножу все на dx,а потом из за
//            // того, что 0.5 - ошибка при которой я должен закрасить клетку сверху - я буду умножать еще на два
//            for (int i = 0; i <= dx; i++) {
//                if (points.containsKey(y)) {
//                    points.get(y).add(x);
//                } else {
//                    points.put(y, new ArrayList<>());
//                    points.get(y).add(x);
//                }
//                error += 2 * dy;
//                if (error > dx) {
//                    y += stepY;
//                    error = -(2 * dx - error);
//                }
//                x += stepX;
//            }
//        } else {
//            // тогда я меняю на каждой итерации y, а для x коплю ошибки
//            for (int i = 0; i <= dy; i++) {
//                points.put(y, new ArrayList<>());
//                points.get(y).add(x);
//                error += 2 * dx;
//                if (error > dy) {
//                    x += stepX;
//                    error = -(2 * dy - error);
//                }
//                y += stepY;
//            }
//        }
//        return points;
//    }
//
//    public static Map<Integer, List<Integer>> myBresenhamOneX(int x0, int y0, int x1, int y1) {
//        Map<Integer, List<Integer>> points = new HashMap<>();
//        int dx = abs(x0 - x1);
//        int dy = abs(y0 - y1);
//
//        int x = x0, y = y0;
//        int error = 0;
//        int stepX = 1;
//        int stepY = 1;
//        if (x1 - x0 < 0) {
//            stepX *= -1;
//        }
//        if (y1 - y0 < 0) {
//            stepY *= -1;
//        }
//        if (dx > dy) {
//            // тогда я меняю на каждой итерации x, а для y коплю ошибки
//            // тогда я могу равномерно разделить подъем, когда иду по y. То есть на каждом шаге по x надо на какое то нецелое
//            // число, которое меньше нуля (dx > dy) менять y. Но это не целое. Поэтому я умножу все на dx,а потом из за
//            // того, что 0.5 - ошибка при которой я должен закрасить клетку сверху - я буду умножать еще на два
//            for (int i = 0; i <= dx; i++) {
////                pw.setColor(x, y, Color.BLACK);
//                points.put(x, new ArrayList<>());
//                points.get(x).add(y);
//                error += 2 * dy;
//                if (error > dx) {
//                    y += stepY;
//                    error = -(2 * dx - error);
//                }
//                x += stepX;
//            }
//        } else {
//            // тогда я меняю на каждой итерации y, а для x коплю ошибки
//            for (int i = 0; i <= dy; i++) {
////                pw.setColor(x, y, Color.BLACK);
//                if (points.containsKey(x)) {
//                    points.get(x).add(y);
//                } else {
//                    points.put(x, new ArrayList<>());
//                    points.get(x).add(y);
//                }
//                error += 2 * dx;
//                if (error > dy) {
//                    x += stepX;
//                    error = -(2 * dy - error);
//                }
//                y += stepY;
//            }
//        }
//        return points;
//    }

//    /**
//     * Метод для растеризации треугольника с использованием идеи scanline и нахождения границ через алгоритм
//     * Брезенхейма.
//     */
//    public static void drawTriangleBresenham(PixelWriter pw, int x0, int y0, int x1, int y1, int x2, int y2) {
//        if (max(y0, max(y1, y2)) - min(y0, max(y1, y2)) > max(x0, max(x1, x2)) - min(x0, max(x1, x2))) {
//            int tmp;
//            if (y0 > y1) {
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//            }
//            if (y1 > y2) {
//                tmp = y2;
//                y2 = y1;
//                y1 = tmp;
//
//                tmp = x2;
//                x2 = x1;
//                x1 = tmp;
//            }
//            if (y0 > y1) {
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//            }
//
//            Map<Integer, List<Integer>> lineAC = myBresenhamOneY(x0, y0, x2, y2);
//            Map<Integer, List<Integer>> lineAB = myBresenhamOneY(x0, y0, x1, y1);
//            Map<Integer, List<Integer>> lineBC = myBresenhamOneY(x1, y1, x2, y2);
//
//            for (int y = y0; y <= y2; y++) {
//                int borderFirst = lineAC.get(y).get(lineAC.get(y).size() - 1);
//                int borderLast;
//                if (lineAB.containsKey(y)) {
//                    borderLast = lineAB.get(y).get(lineAB.get(y).size() - 1);
//                } else {
//                    borderLast = lineBC.get(y).get(lineBC.get(y).size() - 1);
//                }
//                for (int x = min(borderFirst, borderLast); x <= max(borderFirst, borderLast); x++) {
//
//                    pw.setColor(x, y, Color.BLACK);
//                }
//            }
//        } else {
//            int tmp;
//            if (x0 > x1) {
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//            }
//            if (x1 > x2) {
//                tmp = x2;
//                x2 = x1;
//                x1 = tmp;
//
//                tmp = y2;
//                y2 = y1;
//                y1 = tmp;
//            }
//            if (x0 > x1) {
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//            }
//
//            Map<Integer, List<Integer>> lineAC = myBresenhamOneX(x0, y0, x2, y2);
//            Map<Integer, List<Integer>> lineAB = myBresenhamOneX(x0, y0, x1, y1);
//            Map<Integer, List<Integer>> lineBC = myBresenhamOneX(x1, y1, x2, y2);
//
//            for (int x = x0; x <= x2; x++) {
//                int borderFirst = lineAC.get(x).get(lineAC.get(x).size() - 1);
//                int borderLast;
//                if (lineAB.containsKey(x)) {
//                    borderLast = lineAB.get(x).get(lineAB.get(x).size() - 1);
//                } else {
//                    borderLast = lineBC.get(x).get(lineBC.get(x).size() - 1);
//                }
//                for (int y = min(borderFirst, borderLast); y <= max(borderFirst, borderLast); y++) {
//                    pw.setColor(x, y, Color.BLACK);
//                }
//            }
//        }
//    }
//
//    public static void drawLineBresenham(PixelWriter pixelWriter, int x0, int y0, int x1, int y1) {
//        if (abs(x1 - x0) > abs(y1 - y0)) {
//            if (x1 < x0) {
//                int tmp = x0;
//                x0 = x1;
//                x1 = tmp;
//
//                tmp = y0;
//                y0 = y1;
//                y1 = tmp;
//            }
//            BorderIterator iterator = new BresenhamBorderIterator(x0, y0, x1, y1, true);
//            while (iterator.hasNext()) {
//                int x = iterator.getX();
//                int y = iterator.getY();
//                pixelWriter.setColor(x, y, Color.BLACK);
//                iterator.next();
//            }
//        } else {
//            if (y1 < y0) {
//                int tmp = y0;
//                y0 = y1;
//                y1 = tmp;
//
//                tmp = x0;
//                x0 = x1;
//                x1 = tmp;
//            }
//            BorderIterator iterator = new BresenhamBorderIterator(x0, y0, x1, y1, false);
//            while (iterator.hasNext()) {
//                int x = iterator.getX();
//                int y = iterator.getY();
//                pixelWriter.setColor(x, y, Color.BLACK);
//                iterator.next();
//            }
//        }
//    }

//    /**
//     * Метод для растеризации треугольника через scanline и алгоритм Брезенхейма для нахождения границ,
//     * реализованный в виде итератора по границам. Позволяет закрашивать треугольник тремя цветами с
//     * интерполяцией между вершинами. Использует барицентрические координаты. Работает медленнее заполнения
//     * одним цветом.
//     */
//    public static void drawInterpolatedTriangleByIterator(
//            final PixelWriter pixelWriter,
//            int x0, int y0,
//            int x1, int y1,
//            int x2, int y2,
//            Color color0,
//            Color color1,
//            Color color2
//    ) {
//        int tmp;
//        Color tmpColor;
//        if (y0 > y1) {
//            tmp = y1;
//            y1 = y0;
//            y0 = tmp;
//
//            tmp = x1;
//            x1 = x0;
//            x0 = tmp;
//
//            tmpColor = color0;
//            color0 = color1;
//            color1 = tmpColor;
//        }
//        if (y1 > y2) {
//            tmp = y2;
//            y2 = y1;
//            y1 = tmp;
//
//            tmp = x2;
//            x2 = x1;
//            x1 = tmp;
//
//            tmpColor = color1;
//            color1 = color2;
//            color2 = tmpColor;
//        }
//        if (y0 > y1) {
//            tmp = y1;
//            y1 = y0;
//            y0 = tmp;
//
//            tmp = x1;
//            x1 = x0;
//            x0 = tmp;
//
//            tmpColor = color0;
//            color0 = color1;
//            color1 = tmpColor;
//        }
//        BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, true);
//        BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, true);
//        BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, true);
//
//        double[] barycentric;
//        while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
//            int y = borderIterator1.getY();
//            int xLineStart = borderIterator1.getX();
//            int xLineEnd = borderIterator2.getX();
//            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
//                barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
//                Color color = createColorFromBarycentric(barycentric, color0, color1, color2);
//                pixelWriter.setColor(x, y, color);
//            }
//            borderIterator1.next();
//            borderIterator2.next();
//        }
//
//        borderIterator3.next();
//        while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
//            int y = borderIterator2.getY();
//            int xLineStart = borderIterator2.getX();
//            int xLineEnd = borderIterator3.getX();
//            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
//                barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
//                Color color = createColorFromBarycentric(barycentric, color0, color1, color2);
//                pixelWriter.setColor(x, y, color);
//            }
//            borderIterator2.next();
//            borderIterator3.next();
//        }
//    }
//
//    public static void drawTriangleBresenhamByIterator(PixelWriter pw, int x0, int y0, int x1, int y1, int x2, int y2) {
//        if (max(y0, max(y1, y2)) - min(y0, max(y1, y2)) >= max(x0, max(x1, x2)) - min(x0, max(x1, x2))) {
//            int tmp;
//            if (y0 > y1) {
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//            }
//            if (y1 > y2) {
//                tmp = y2;
//                y2 = y1;
//                y1 = tmp;
//
//                tmp = x2;
//                x2 = x1;
//                x1 = tmp;
//            }
//            if (y0 > y1) {
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//            }
//            Map<Integer, Integer> it_2_out = new HashMap<>();
//            // тут хожу по x и нахожу два y и провожу линию между ними
//            BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, true);
//            BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, true);
//            BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, true);
//            while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
//                int y = borderIterator1.getY();
//                int xLineStart = borderIterator1.getX();
//                int xLineEnd = borderIterator2.getX();
//                it_2_out.put(y, xLineEnd);
//                for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
//                    pw.setColor(x, y, Color.BLACK);
//                }
//                borderIterator1.next();
//                borderIterator2.next();
//            }
//
//            while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
//                int y = borderIterator2.getY();
//                int xLineStart = borderIterator2.getX();
//                int xLineEnd = borderIterator3.getX();
//                it_2_out.put(y, xLineStart);
//                for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
//                    pw.setColor(x, y, Color.BLACK);
//                }
//                borderIterator2.next();
//                borderIterator3.next();
//            }
//            int y = borderIterator2.getY();
//            int xLineStart = borderIterator2.getX();
//            int xLineEnd = borderIterator3.getX();
//            it_2_out.put(y, xLineStart);
//            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
//                pw.setColor(x, y, Color.BLACK);
//            }
////            System.out.println(it_2_out);
//            System.out.println(it_2_out.containsKey(y0));
//            System.out.println(it_2_out.containsKey(y2));
//
//        } else {
//            int tmp;
//            if (x0 > x1) {
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//            }
//            if (x1 > x2) {
//                tmp = x2;
//                x2 = x1;
//                x1 = tmp;
//
//                tmp = y2;
//                y2 = y1;
//                y1 = tmp;
//            }
//            if (x0 > x1) {
//                tmp = x1;
//                x1 = x0;
//                x0 = tmp;
//
//                tmp = y1;
//                y1 = y0;
//                y0 = tmp;
//            }
//            Map<Integer, Integer> it_2_out = new HashMap<>();
//            // тут хожу по x и нахожу два y и провожу линию между ними
//            BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, false);
//            BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, false);
//            BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, false);
//            while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
//                int x = borderIterator1.getX();
//                int yLineStart = borderIterator1.getY();
//                int yLineEnd = borderIterator2.getY();
//                it_2_out.put(x, yLineEnd);
//                for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
//                    pw.setColor(x, y, Color.BLACK);
//                }
//                borderIterator1.next();
//                borderIterator2.next();
//            }
//
//            while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
//                int x = borderIterator2.getX();
//                int yLineStart = borderIterator2.getY();
//                int yLineEnd = borderIterator3.getY();
//                it_2_out.put(x, yLineStart);
//                for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
//                    pw.setColor(x, y, Color.BLACK);
//                }
//                borderIterator2.next();
//                borderIterator3.next();
//            }
//
//            int x = borderIterator2.getX();
//            int yLineStart = borderIterator2.getY();
//            int yLineEnd = borderIterator3.getY();
//            it_2_out.put(x, yLineStart);
//            for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
//                pw.setColor(x, y, Color.BLACK);
//            }
//        }
//    }
//
//    private static Color createColorFromBarycentric(double[] barycentric, Color color1, Color color2, Color color3) {
//        double a = max(0, min(1, barycentric[0]));
//        double b = max(0, min(1, barycentric[1]));
//        double c = max(0, min(1, barycentric[2]));
//
//        return new Color(
//                color1.getRed() * a + color2.getRed() * b + color3.getRed() * c,
//                color1.getGreen() * a + color2.getGreen() * b + color3.getGreen() * c,
//                color1.getBlue() * a + color2.getBlue() * b + color3.getBlue() * c,
//                1);
//
//    }
}
