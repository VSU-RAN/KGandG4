package vsu.org.ran.kgandg4.rasterization;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.render_engine.Texture;
import vsu.org.ran.kgandg4.render_engine.TexturedVertex;
import vsu.org.ran.kgandg4.render_engine.Zbuffer;

import java.util.*;

import static java.lang.Math.*;

public class Rasterization {
    public static Map<Integer, List<Integer>> myBresenhamOneY(int x0, int y0, int x1, int y1) {
        Map<Integer, List<Integer>> points = new HashMap<>();
        int dx = abs(x0 - x1);
        int dy = abs(y0 - y1);

        int x = x0, y = y0;
        int error = 0;
        int stepX = 1;
        int stepY = 1;
        if (x1 - x0 < 0) {
            stepX *= -1;
        }
        if (y1 - y0 < 0) {
            stepY *= -1;
        }
        if (dx > dy) {
            // тогда я меняю на каждой итерации x, а для y коплю ошибки
            // тогда я могу равномерно разделить подъем, когда иду по y. То есть на каждом шаге по x надо на какое то нецелое
            // число, которое меньше нуля (dx > dy) менять y. Но это не целое. Поэтому я умножу все на dx,а потом из за
            // того, что 0.5 - ошибка при которой я должен закрасить клетку сверху - я буду умножать еще на два
            for (int i = 0; i <= dx; i++) {
                if (points.containsKey(y)) {
                    points.get(y).add(x);
                } else {
                    points.put(y, new ArrayList<>());
                    points.get(y).add(x);
                }
                error += 2 * dy;
                if (error > dx) {
                    y += stepY;
                    error = -(2 * dx - error);
                }
                x += stepX;
            }
        } else {
            // тогда я меняю на каждой итерации y, а для x коплю ошибки
            for (int i = 0; i <= dy; i++) {
                points.put(y, new ArrayList<>());
                points.get(y).add(x);
                error += 2 * dx;
                if (error > dy) {
                    x += stepX;
                    error = -(2 * dy - error);
                }
                y += stepY;
            }
        }
        return points;
    }

    public static Map<Integer, List<Integer>> myBresenhamOneX(int x0, int y0, int x1, int y1) {
        Map<Integer, List<Integer>> points = new HashMap<>();
        int dx = abs(x0 - x1);
        int dy = abs(y0 - y1);

        int x = x0, y = y0;
        int error = 0;
        int stepX = 1;
        int stepY = 1;
        if (x1 - x0 < 0) {
            stepX *= -1;
        }
        if (y1 - y0 < 0) {
            stepY *= -1;
        }
        if (dx > dy) {
            // тогда я меняю на каждой итерации x, а для y коплю ошибки
            // тогда я могу равномерно разделить подъем, когда иду по y. То есть на каждом шаге по x надо на какое то нецелое
            // число, которое меньше нуля (dx > dy) менять y. Но это не целое. Поэтому я умножу все на dx,а потом из за
            // того, что 0.5 - ошибка при которой я должен закрасить клетку сверху - я буду умножать еще на два
            for (int i = 0; i <= dx; i++) {
//                pw.setColor(x, y, Color.BLACK);
                points.put(x, new ArrayList<>());
                points.get(x).add(y);
                error += 2 * dy;
                if (error > dx) {
                    y += stepY;
                    error = -(2 * dx - error);
                }
                x += stepX;
            }
        } else {
            // тогда я меняю на каждой итерации y, а для x коплю ошибки
            for (int i = 0; i <= dy; i++) {
//                pw.setColor(x, y, Color.BLACK);
                if (points.containsKey(x)) {
                    points.get(x).add(y);
                } else {
                    points.put(x, new ArrayList<>());
                    points.get(x).add(y);
                }
                error += 2 * dx;
                if (error > dy) {
                    x += stepX;
                    error = -(2 * dy - error);
                }
                y += stepY;
            }
        }
        return points;
    }

    /**
     * Метод для растеризации треугольника с использованием идеи scanline и нахождения границ через алгоритм
     * Брезенхейма.
     */
    public static void drawTriangleBresenham(PixelWriter pw, int x0, int y0, int x1, int y1, int x2, int y2) {
        if (max(y0, max(y1, y2)) - min(y0, max(y1, y2)) > max(x0, max(x1, x2)) - min(x0, max(x1, x2))) {
            int tmp;
            if (y0 > y1) {
                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp = x1;
                x1 = x0;
                x0 = tmp;
            }
            if (y1 > y2) {
                tmp = y2;
                y2 = y1;
                y1 = tmp;

                tmp = x2;
                x2 = x1;
                x1 = tmp;
            }
            if (y0 > y1) {
                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp = x1;
                x1 = x0;
                x0 = tmp;
            }

            Map<Integer, List<Integer>> lineAC = myBresenhamOneY(x0, y0, x2, y2);
            Map<Integer, List<Integer>> lineAB = myBresenhamOneY(x0, y0, x1, y1);
            Map<Integer, List<Integer>> lineBC = myBresenhamOneY(x1, y1, x2, y2);

            for (int y = y0; y <= y2; y++) {
                int borderFirst = lineAC.get(y).get(lineAC.get(y).size() - 1);
                int borderLast;
                if (lineAB.containsKey(y)) {
                    borderLast = lineAB.get(y).get(lineAB.get(y).size() - 1);
                } else {
                    borderLast = lineBC.get(y).get(lineBC.get(y).size() - 1);
                }
                for (int x = min(borderFirst, borderLast); x <= max(borderFirst, borderLast); x++) {

                    pw.setColor(x, y, Color.BLACK);
                }
            }
        } else {
            int tmp;
            if (x0 > x1) {
                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp = y1;
                y1 = y0;
                y0 = tmp;
            }
            if (x1 > x2) {
                tmp = x2;
                x2 = x1;
                x1 = tmp;

                tmp = y2;
                y2 = y1;
                y1 = tmp;
            }
            if (x0 > x1) {
                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp = y1;
                y1 = y0;
                y0 = tmp;
            }

            Map<Integer, List<Integer>> lineAC = myBresenhamOneX(x0, y0, x2, y2);
            Map<Integer, List<Integer>> lineAB = myBresenhamOneX(x0, y0, x1, y1);
            Map<Integer, List<Integer>> lineBC = myBresenhamOneX(x1, y1, x2, y2);

            for (int x = x0; x <= x2; x++) {
                int borderFirst = lineAC.get(x).get(lineAC.get(x).size() - 1);
                int borderLast;
                if (lineAB.containsKey(x)) {
                    borderLast = lineAB.get(x).get(lineAB.get(x).size() - 1);
                } else {
                    borderLast = lineBC.get(x).get(lineBC.get(x).size() - 1);
                }
                for (int y = min(borderFirst, borderLast); y <= max(borderFirst, borderLast); y++) {
                    pw.setColor(x, y, Color.BLACK);
                }
            }
        }
    }

    public static void drawLineBresenham(PixelWriter pixelWriter, int x0, int y0, int x1, int y1) {
        if (abs(x1 - x0) > abs(y1 - y0)) {
            if (x1 < x0) {
                int tmp = x0;
                x0 = x1;
                x1 = tmp;

                tmp = y0;
                y0 = y1;
                y1 = tmp;
            }
            BorderIterator iterator = new BresenhamBorderIterator(x0, y0, x1, y1, true);
            while (iterator.hasNext()) {
                int x = iterator.getX();
                int y = iterator.getY();
                pixelWriter.setColor(x, y, Color.BLACK);
                iterator.next();
            }
        } else {
            if (y1 < y0) {
                int tmp = y0;
                y0 = y1;
                y1 = tmp;

                tmp = x0;
                x0 = x1;
                x1 = tmp;
            }
            BorderIterator iterator = new BresenhamBorderIterator(x0, y0, x1, y1, false);
            while (iterator.hasNext()) {
                int x = iterator.getX();
                int y = iterator.getY();
                pixelWriter.setColor(x, y, Color.BLACK);
                iterator.next();
            }
        }
    }

    /**
     * Метод рисования линии. Позволяет рисовать ее из точки 1 в точку 2. Использует округление и
     * линейную интерполяцию для того, чтобы получить промежуточные значения и построить путь из 1 в 2.
     *
     * @param pixelWriter объект для рисования
     * @param x1          точка начала
     * @param y1          точка начала
     * @param x2          точка конца
     * @param y2          точка конца
     */
    public static void drawLine(PixelWriter pixelWriter, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (Math.abs(dx) > Math.abs(dy)) {
            if (x1 > x2) {
                double tmp = x2;
                x2 = x1;
                x1 = tmp;

                tmp = y2;
                y2 = y1;
                y1 = tmp;
            }
            int[] points = interpolate(y1, x1, y2, x2);
            for (double x = x1; x < x2; x++) {
                pixelWriter.setColor((int) x, points[(int) (x - x1)], Color.RED);
            }
        } else {
            if (y1 > y2) {
                double tmp = x2;
                x2 = x1;
                x1 = tmp;

                tmp = y2;
                y2 = y1;
                y1 = tmp;
            }
            int[] points = interpolate(x1, y1, x2, y2);
            for (double y = y1; y < y2; y++) {
                pixelWriter.setColor(points[(int) (y - y1)], (int) y, Color.RED);
            }
        }
    }

    /**
     * Вычисляет значения функции d = f(i) от i=i0 до i=i1
     * Использует числа с плавающей точкой и их округление.
     *
     * @param d0 значение функции в начальной координате
     * @param i0 аргумент функции в начальной координате
     * @param d1 значение функции в конечной координате
     * @param i1 аргумент функции в конечной координате
     */
    private static int[] interpolate(double d0, double i0, double d1, double i1) {
        double tmp;
        int[] values = new int[(int) ((i1 - i0) + 1)];
        if (i0 > i1) {
            tmp = i1;
            i1 = i0;
            i0 = tmp;
        }

        double a = (d1 - d0) / (i1 - i0);
        double value = d0;
        for (double i = i0; i <= i1; i++) {
            values[(int) (i - i0)] = (int) Math.round(value);
            value += a;
        }
        return values;
    }

    /**
     * С помощью линий рисует треугольник, однако не заполняет его, а ставит пиксели только на стороны.
     */
    public static void drawWireFrameTriangle(PixelWriter pixelWriter, double x0, double y0, double x1, double y1, double x2, double y2) {
        drawLine(pixelWriter, x0, y0, x1, y1);
        drawLine(pixelWriter, x0, y0, x2, y2);
        drawLine(pixelWriter, x2, y2, x1, y1);
    }

    private static float findThirdOrderDeterminant(
            float a00, float a01, float a02,
            float a10, float a11, float a12,
            float a20, float a21, float a22
    ) {
        return ((a00 * a11 * a22) + (a10 * a21 * a02) + (a01 * a12 * a20)) - ((a02 * a11 * a20) + (a01 * a10 * a22) + (a12 * a21 * a00));
    }

    public static float[] findBarycentricCords(float xCur, float yCur, float x0, float y0, float x1, float y1, float x2, float y2) {
        float mainDet = findThirdOrderDeterminant(
                x0, x1, x2,
                y0, y1, y2,
                1, 1, 1
        );
        if (Math.abs(mainDet) < 0.000001f) return new float[]{0, 0, 0};

        float detForAlpha = findThirdOrderDeterminant(
                xCur, x1, x2,
                yCur, y1, y2,
                1, 1, 1
        );
        float detForBeta = findThirdOrderDeterminant(
                x0, xCur, x2,
                y0, yCur, y2,
                1, 1, 1
        );
        float detForLambda = findThirdOrderDeterminant(
                x0, x1, xCur,
                y0, y1, yCur,
                1, 1, 1
        );
        return new float[]{ detForAlpha / mainDet,  detForBeta / mainDet, detForLambda / mainDet};
    }

    /**
     * Метод для растеризации треугольника через scanline и алгоритм Брезенхейма для нахождения границ,
     * реализованный в виде итератора по границам. Позволяет закрашивать треугольник тремя цветами с
     * интерполяцией между вершинами. Использует барицентрические координаты. Работает медленнее заполнения
     * одним цветом.
     */
    public static void drawInterpolatedTriangleByIterator(
            final PixelWriter pixelWriter,
            int x0, int y0,
            int x1, int y1,
            int x2, int y2,
            Color color0,
            Color color1,
            Color color2
    ) {
        int tmp;
        Color tmpColor;
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;

            tmpColor = color0;
            color0 = color1;
            color1 = tmpColor;
        }
        if (y1 > y2) {
            tmp = y2;
            y2 = y1;
            y1 = tmp;

            tmp = x2;
            x2 = x1;
            x1 = tmp;

            tmpColor = color1;
            color1 = color2;
            color2 = tmpColor;
        }
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;

            tmpColor = color0;
            color0 = color1;
            color1 = tmpColor;
        }
        BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, true);
        BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, true);
        BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, true);

        while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
            int y = borderIterator1.getY();
            int xLineStart = borderIterator1.getX();
            int xLineEnd = borderIterator2.getX();
            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                float[] barycentric = findBarycentricCords(
                        x + 0.5f, y + 0.5f,
                        x0 + 0.5f, y0 + 0.5f,
                        x1 + 0.5f, y1 + 0.5f,
                        x2 + 0.5f, y2 + 0.5f);
                Color color = createColorFromBarycentric(barycentric, color0, color1, color2);
                pixelWriter.setColor(x, y, color);
            }
            borderIterator1.next();
            borderIterator2.next();
        }

        borderIterator3.next();
        while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
            int y = borderIterator2.getY();
            int xLineStart = borderIterator2.getX();
            int xLineEnd = borderIterator3.getX();
            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                float[] barycentric = findBarycentricCords(
                        x + 0.5f, y + 0.5f,
                        x0 + 0.5f, y0 + 0.5f,
                        x1 + 0.5f, y1 + 0.5f,
                        x2 + 0.5f, y2 + 0.5f);
                Color color = createColorFromBarycentric(barycentric, color0, color1, color2);
                pixelWriter.setColor(x, y, color);
            }
            borderIterator2.next();
            borderIterator3.next();
        }
    }

    public static void drawTriangleBresenhamByIterator(PixelWriter pw, Zbuffer zbuffer, Texture texture,
                                                       int x0, int y0, float z0, float u0, float v0,
                                                       int x1, int y1, float z1, float u1, float v1,
                                                       int x2, int y2, float z2, float u2, float v2) {
        float w0 = 1.0f / z0;
        float w1 = 1.0f / z1;
        float w2 = 1.0f / z2;

        if (max(y0, max(y1, y2)) - min(y0, max(y1, y2)) >= max(x0, max(x1, x2)) - min(x0, max(x1, x2))) {
            int tmp;
            float tmp2;
            if (y0 > y1) {
                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp2 = z1;
                z1 = z0;
                z0 = tmp2;

                tmp2 = u1;
                u1 = u0;
                u0 = tmp2;

                tmp2 = v1;
                v1 = v0;
                v0 = tmp2;

                tmp2 = w1;
                w1 = w0;
                w0 = tmp2;


            }
            if (y1 > y2) {
                tmp = y2;
                y2 = y1;
                y1 = tmp;

                tmp = x2;
                x2 = x1;
                x1 = tmp;

                tmp2 = z2;
                z2 = z1;
                z1 = tmp2;

                tmp2 = u2;
                u2 = u1;
                u1 = tmp2;

                tmp2 = v2;
                v2 = v1;
                v1 = tmp2;

                tmp2 = w2;
                w2 = w1;
                w1 = tmp2;

            }
            if (y0 > y1) {
                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp2 = z1;
                z1 = z0;
                z0 = tmp2;

                tmp2 = u1;
                u1 = u0;
                u0 = tmp2;

                tmp2 = v1;
                v1 = v0;
                v0 = tmp2;

                tmp2 = w1;
                w1 = w0;
                w0 = tmp2;
            }
            Map<Integer, Integer> it_2_out = new HashMap<>();
            // тут хожу по x и нахожу два y и провожу линию между ними
            BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, true);
            BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, true);
            BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, true);
            while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
                int y = borderIterator1.getY();
                int xLineStart = borderIterator1.getX();
                int xLineEnd = borderIterator2.getX();
                it_2_out.put(y, xLineEnd);
                for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                    float[] barycentric = findBarycentricCords(
                            x + 0.5f, y + 0.5f,
                            x0 + 0.5f, y0 + 0.5f,
                            x1 + 0.5f, y1 + 0.5f,
                            x2 + 0.5f, y2 + 0.5f);
                    float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                    if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                        float z = (alpha * z0 + beta * z1 + gamma * z2);
                        if (zbuffer.testPointAndSet(x, y, z)) {
                            float oneOverZ =(alpha * w0 + beta * w1 + gamma * w2);
                            float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                            float vOverZ = (alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                            float z_interpolated = 1.0f / oneOverZ;
                            float u = uOverZ * z_interpolated;
                            float v = vOverZ * z_interpolated;

                            Color color = texture.getColor(u, v);
                            pw.setColor(x, y, color);
                        }
                    }
                }
                borderIterator1.next();
                borderIterator2.next();
            }

            while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
                int y = borderIterator2.getY();
                int xLineStart = borderIterator2.getX();
                int xLineEnd = borderIterator3.getX();
                it_2_out.put(y, xLineStart);
                for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                    float[] barycentric = findBarycentricCords(
                            x + 0.5f, y + 0.5f,
                            x0 + 0.5f, y0 + 0.5f,
                            x1 + 0.5f, y1 + 0.5f,
                            x2 + 0.5f, y2 + 0.5f);
                    float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                    if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                        float z = (alpha * z0 + beta * z1 + gamma * z2);
                        if (zbuffer.testPointAndSet(x, y, z)) {
                            float oneOverZ = (alpha * w0 + beta * w1 + gamma * w2);
                            float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                            float vOverZ =(alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                            float z_interpolated = 1.0f / oneOverZ;
                            float u = uOverZ * z_interpolated;
                            float v = vOverZ * z_interpolated;

                            Color color = texture.getColor(u, v);
                            pw.setColor(x, y, color);
                        }
                    }
                }
                borderIterator2.next();
                borderIterator3.next();
            }
            int y = borderIterator2.getY();
            int xLineStart = borderIterator2.getX();
            int xLineEnd = borderIterator3.getX();
            it_2_out.put(y, xLineStart);
            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                float[] barycentric = findBarycentricCords(
                        x + 0.5f, y + 0.5f,
                        x0 + 0.5f, y0 + 0.5f,
                        x1 + 0.5f, y1 + 0.5f,
                        x2 + 0.5f, y2 + 0.5f);
                float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                    float z =  (alpha * z0 + beta * z1 + gamma * z2);
                    if (zbuffer.testPointAndSet(x, y, z)) {
                        float oneOverZ =(alpha * w0 + beta * w1 + gamma * w2);
                        float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                        float vOverZ =(alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                        float z_interpolated = 1.0f / oneOverZ;
                        float u = uOverZ * z_interpolated;
                        float v = vOverZ * z_interpolated;

                        Color color = texture.getColor(u, v);
                        pw.setColor(x, y, color);
                    }
                }
            }

        } else {
            int tmp;
            float tmp2;
            if (x0 > x1) {
                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp2 = z1;
                z1 = z0;
                z0 = tmp2;

                tmp2 = u1;
                u1 = u0;
                u0 = tmp2;

                tmp2 = v1;
                v1 = v0;
                v0 = tmp2;

                tmp2 = w1;
                w1 = w0;
                w0 = tmp2;
            }
            if (x1 > x2) {
                tmp = x2;
                x2 = x1;
                x1 = tmp;

                tmp = y2;
                y2 = y1;
                y1 = tmp;

                tmp2 = z2;
                z2 = z1;
                z1 = tmp2;

                tmp2 = u2;
                u2 = u1;
                u1 = tmp2;

                tmp2 = v2;
                v2 = v1;
                v1 = tmp2;

                tmp2 = w2;
                w2 = w1;
                w1 = tmp2;
            }
            if (x0 > x1) {
                tmp = x1;
                x1 = x0;
                x0 = tmp;

                tmp = y1;
                y1 = y0;
                y0 = tmp;

                tmp2 = z1;
                z1 = z0;
                z0 = tmp2;

                tmp2 = u1;
                u1 = u0;
                u0 = tmp2;

                tmp2 = v1;
                v1 = v0;
                v0 = tmp2;

                tmp2 = w1;
                w1 = w0;
                w0 = tmp2;
            }
            Map<Integer, Integer> it_2_out = new HashMap<>();
            // тут хожу по x и нахожу два y и провожу линию между ними
            BorderIterator borderIterator1 = new BresenhamBorderIterator(x0, y0, x1, y1, false);
            BorderIterator borderIterator2 = new BresenhamBorderIterator(x0, y0, x2, y2, false);
            BorderIterator borderIterator3 = new BresenhamBorderIterator(x1, y1, x2, y2, false);
            while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
                int x = borderIterator1.getX();
                int yLineStart = borderIterator1.getY();
                int yLineEnd = borderIterator2.getY();
                it_2_out.put(x, yLineEnd);
                for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
                    float[] barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                    float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                    if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                        float z =  (alpha * z0 + beta * z1 + gamma * z2);
                        if (zbuffer.testPointAndSet(x, y, z)) {
                            float oneOverZ = (alpha * w0 + beta * w1 + gamma * w2);
                            float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                            float vOverZ = (alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                            float z_interpolated = 1.0f / oneOverZ;
                            float u = uOverZ * z_interpolated;
                            float v = vOverZ * z_interpolated;

                            Color color = texture.getColor(u, v);
                            pw.setColor(x, y, color);
                        }
                    }
                }
                borderIterator1.next();
                borderIterator2.next();
            }

            while (borderIterator2.hasNext() && borderIterator3.hasNext()) {
                int x = borderIterator2.getX();
                int yLineStart = borderIterator2.getY();
                int yLineEnd = borderIterator3.getY();
                it_2_out.put(x, yLineStart);
                for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
                    float[] barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                    float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                    if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                        float z =  (alpha * z0 + beta * z1 + gamma * z2);
                        if (zbuffer.testPointAndSet(x, y, z)) {
                            float oneOverZ = (alpha * w0 + beta * w1 + gamma * w2);
                            float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                            float vOverZ = (alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                            float z_interpolated = 1.0f / oneOverZ;
                            float u = uOverZ * z_interpolated;
                            float v = vOverZ * z_interpolated;

                            Color color = texture.getColor(u, v);
                            pw.setColor(x, y, color);
                        }
                    }
                }
                borderIterator2.next();
                borderIterator3.next();
            }

            int x = borderIterator2.getX();
            int yLineStart = borderIterator2.getY();
            int yLineEnd = borderIterator3.getY();
            it_2_out.put(x, yLineStart);
            for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
                float[] barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                    float z = (alpha * z0 + beta * z1 + gamma * z2);
                    if (zbuffer.testPointAndSet(x, y, z)) {
                        float oneOverZ = (alpha * w0 + beta * w1 + gamma * w2);
                        float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
                        float vOverZ =(alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));

                        float z_interpolated = 1.0f / oneOverZ;
                        float u = uOverZ * z_interpolated;
                        float v = vOverZ * z_interpolated;

                        Color color = texture.getColor(u, v);
                        pw.setColor(x, y, color);
                    }
                }
            }
        }
    }

    private static Color createColorFromBarycentric(float[] barycentric, Color color1, Color color2, Color color3) {
        double a = max(0, min(1, barycentric[0]));
        double b = max(0, min(1, barycentric[1]));
        double c = max(0, min(1, barycentric[2]));

        return new Color(
                color1.getRed() * a + color2.getRed() * b + color3.getRed() * c,
                color1.getGreen() * a + color2.getGreen() * b + color3.getGreen() * c,
                color1.getBlue() * a + color2.getBlue() * b + color3.getBlue() * c,
                1);

    }

    /**
     * Метод растеризации треугольника через нахождение границ при помощи линейной интерполяции. Использует
     * операции с плавающей точкой.
     */
    public static void drawTriangle(
            final PixelWriter pixelWriter,
            int x0, int y0,
            int x1, int y1,
            int x2, int y2

    ) {
        int tmp;
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;
        }
        if (y1 > y2) {
            tmp = y2;
            y2 = y1;
            y1 = tmp;

            tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        if (y0 > y1) {
            tmp = y1;
            y1 = y0;
            y0 = tmp;

            tmp = x1;
            x1 = x0;
            x0 = tmp;
        }

        int[] x01 = interpolate(x0, y0, x1, y1);
        int[] x12 = interpolate(x1, y1, x2, y2);
        int[] x02 = interpolate(x0, y0, x2, y2);
        int[] x012 = new int[x01.length + x12.length - 1];
        System.arraycopy(x01, 0, x012, 0, x01.length - 1);
        System.arraycopy(x12, 0, x012, x01.length - 1, x12.length);

        int m = x02.length / 2;
        int[] xLeft;
        int[] xRight;
        if (x02[m] < x012[m]) {
            xLeft = x02;
            xRight = x012;
        } else {
            xLeft = x012;
            xRight = x02;
        }

        for (int y = y0; y <= y2; y++) {
            for (int x = xLeft[y - y0]; x <= xRight[y - y0]; x++) {
                pixelWriter.setColor(x, y, Color.RED);
            }
        }
    }

        public static void drawTriangleSimpleBox(
                PixelWriter pw, Zbuffer zbuffer, Texture texture, javax.vecmath.Vector3f ray, float k,
                int x0, int y0, float z0, float u0, float v0, Vector3f n0,
                int x1, int y1, float z1, float u1, float v1, Vector3f n1,
                int x2, int y2, float z2, float u2, float v2, Vector3f n2) {

            // Находим ограничивающий прямоугольник
            int minX = Math.max(0, Math.min(x0, Math.min(x1, x2)));
            int maxX = Math.min(zbuffer.getWidth() - 1, Math.max(x0, Math.max(x1, x2)));
            int minY = Math.max(0, Math.min(y0, Math.min(y1, y2)));
            int maxY = Math.min(zbuffer.getHeight() - 1, Math.max(y0, Math.max(y1, y2)));

//            float w0 = (Math.abs(z0) > 0.000001f) ? 1.0f / z0 : Float.MAX_VALUE;
//            float w1 = (Math.abs(z1) > 0.000001f) ? 1.0f / z1 : Float.MAX_VALUE;
//            float w2 = (Math.abs(z2) > 0.000001f) ? 1.0f / z2 : Float.MAX_VALUE;

            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    float[] barycentric = findBarycentricCords(
                            x + 0.5f, y + 0.5f,
                            x0 + 0.5f, y0 + 0.5f,
                            x1 + 0.5f, y1 + 0.5f,
                            x2 + 0.5f, y2 + 0.5f);
                    float alpha = barycentric[0], beta = barycentric[1], gamma = barycentric[2];
                    if (alpha >= -0.0001f && beta >= -0.0001f && gamma >= -0.0001f) {
                        float z = (alpha * z0 + beta * z1 + gamma * z2);
                        if (zbuffer.testPointAndSet(x, y, z)) {
//                            float oneOverZ =(alpha * w0 + beta * w1 + gamma * w2);
//                            float uOverZ = (alpha * (u0 * w0) + beta * (u1 * w1) + gamma * (u2 * w2));
//                            float vOverZ =(alpha * (v0 * w0) + beta * (v1 * w1) + gamma * (v2 * w2));
                            float u = alpha * u0 + beta * u1 + gamma * u2;
                            float v = alpha * v0 + beta * v1 + gamma * v2;


                            Vector3f n = new Vector3f(
                                    alpha * n0.getX() + beta * n1.getX() + gamma * n2.getX(),
                                    alpha * n0.getY() + beta * n1.getY() + gamma * n2.getY(),
                                    alpha * n0.getZ() + beta * n1.getZ() + gamma * n2.getZ()
                            );
                            n.normalize();

                            float l = Math.max(0, -dotProduct(n, ray));
                            float factor = (1 - k) + (k * l);
                            Color baseColor = texture.getColor(u, v);
                            float r = (float)baseColor.getRed() * factor;
                            float g = (float)baseColor.getGreen() * factor;
                            float b = (float)baseColor.getBlue() * factor;
                            Color colorLightning = new Color(r, g, b, baseColor.getOpacity());
//                            float z_interpolated = 1.0f / oneOverZ;
//                            float u = uOverZ * z_interpolated;
//                            float v = vOverZ * z_interpolated;
                            pw.setColor(x, y, colorLightning);
                        }
                    }
                }
            }
        }

    private static float dotProduct(Vector3f a, javax.vecmath.Vector3f b) {
        return a.getX() * b.x + a.getY() * b.y + a.getZ() * b.z;
    }
}
