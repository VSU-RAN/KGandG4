package vsu.org.ran.kgandg4.model;


import math.vector.Vector3f;
import utils.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Triangle {
    List<Vector3f> points;

    public Triangle(Vector3f point1, Vector3f point2, Vector3f point3) {
        points = new ArrayList<>(List.of(point1, point2, point3));
    }

    public boolean isInsideTriangle(Vector3f vector3f) {
        // переносим треугольник в начало координат
        float xA = points.get(0).getX();
        float yA = points.get(0).getY();
        float xB = points.get(1).getX() - xA;
        float yB = points.get(1).getY() - yA;
        float xC = points.get(2).getX() - xA;
        float yC = points.get(2).getY() - yA;
        float xP = vector3f.getX() - xA;
        float yP = vector3f.getY() - yA;

        // xP = m * xB + l * xC и yP аналогично
        double[] res = MathUtil.solveByKramer(
                xB, xC,
                yB, yC, xP, yP
        );
        double m = res[0];
        double l = res[1];

        // проверяем условия
        return m >= 0 && l >= 0 && m + l <= 1;
    }

    public void setPointByIndex(int i, Vector3f point) {
        points.set(i, point);
    }

    public Vector3f getVertex(int vertexIndex) {
        return points.get(vertexIndex);
    }


    public List<Vector3f> getPoints() {
        return Collections.unmodifiableList(points);
    }


    public Vector3f getPointByIndex(int i) {
        return points.get(i);
    }


    public int getPointIndex(Vector3f point) {
        if (point == null) throw new IllegalArgumentException();
        for (int i = 0; i < 3; i++) {
            if (points.get(i).equals(point)) {
                return i;
            }
        }
        return -1;
    }

    public int pointCnt() {
        return points.size();
    }

    @Override
    public String toString() {
        return "Triangle{" + "points=" + points + '}';
    }
}
