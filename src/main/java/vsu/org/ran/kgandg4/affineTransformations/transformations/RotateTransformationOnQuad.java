package vsu.org.ran.kgandg4.affineTransformations.transformations;

import math.matrix.Matrix4f;
import math.quaternion.Quat4f;

public class RotateTransformationOnQuad implements Transformation {
    private final Quat4f rotation;

    public RotateTransformationOnQuad(Axis axis, float angle) {
        this.rotation = createRotationQuad(axis, angle);
    }

    /**
     * Конструктор с готовым кватернионом
     */
    public RotateTransformationOnQuad(Quat4f quaternion) {
        this.rotation = new Quat4f(quaternion.getX(), quaternion.getY(), quaternion.getZ(), quaternion.getW());
    }

    private Quat4f createRotationQuad(Axis axis, float angle) {
        switch (axis) {
            case X:
                return new Quat4f(
                        (float) -Math.sin(angle/2),
                        0,
                        0,
                        (float) Math.cos(angle/2)
                );
            case Y:
                return new Quat4f(
                        0,
                        (float) Math.sin(angle/2),
                        0,
                        (float) Math.cos(angle/2)
                );
            case Z:
                return new Quat4f(
                        0,
                        0,
                        (float) -Math.sin(angle/2),
                        (float) Math.cos(angle/2)
                );
            default:
                throw new IllegalArgumentException("Unknow axis:" + axis);
        }
    }

    @Override
    public Matrix4f getMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.set(rotation);
        return matrix;
    }
}
