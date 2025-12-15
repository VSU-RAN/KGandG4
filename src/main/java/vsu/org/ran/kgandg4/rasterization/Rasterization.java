package vsu.org.ran.kgandg4.rasterization;

import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        double[] barycentric;
        while (borderIterator1.hasNext() && borderIterator2.hasNext()) {
            int y = borderIterator1.getY();
            int xLineStart = borderIterator1.getX();
            int xLineEnd = borderIterator2.getX();
            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
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
                barycentric = findBarycentricCords(x, y, x0, y0, x1, y1, x2, y2);
                Color color = createColorFromBarycentric(barycentric, color0, color1, color2);
                pixelWriter.setColor(x, y, color);
            }
            borderIterator2.next();
            borderIterator3.next();
        }
    }

    public static void drawTriangleBresenhamByIterator(PixelWriter pw, int x0, int y0, int x1, int y1, int x2, int y2) {
        if (max(y0, max(y1, y2)) - min(y0, max(y1, y2)) >= max(x0, max(x1, x2)) - min(x0, max(x1, x2))) {
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
                    pw.setColor(x, y, Color.BLACK);
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
                    pw.setColor(x, y, Color.BLACK);
                }
                borderIterator2.next();
                borderIterator3.next();
            }
            int y = borderIterator2.getY();
            int xLineStart = borderIterator2.getX();
            int xLineEnd = borderIterator3.getX();
            it_2_out.put(y, xLineStart);
            for (int x = min(xLineStart, xLineEnd); x <= max(xLineStart, xLineEnd); x++) {
                pw.setColor(x, y, Color.BLACK);
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
                    pw.setColor(x, y, Color.BLACK);
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
                    pw.setColor(x, y, Color.BLACK);
                }
                borderIterator2.next();
                borderIterator3.next();
            }

            int x = borderIterator2.getX();
            int yLineStart = borderIterator2.getY();
            int yLineEnd = borderIterator3.getY();
            it_2_out.put(x, yLineStart);
            for (int y = min(yLineStart, yLineEnd); y <= max(yLineStart, yLineEnd); y++) {
                pw.setColor(x, y, Color.BLACK);
            }
        }
    }

    private static Color createColorFromBarycentric(double[] barycentric, Color color1, Color color2, Color color3) {
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
}
