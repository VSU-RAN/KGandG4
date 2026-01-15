package vsu.org.ran.kgandg4.affineTransformations.transformations;

import math.point.Point3f;
import math.matrix.Matrix4f;

public interface Transformation {
    Matrix4f getMatrix();

    default Point3f apply(Point3f point) {
        Point3f result = new Point3f();
        this.getMatrix().transform(point, result);
        return result;
    }
}
