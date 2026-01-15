package vsu.org.ran.kgandg4.affineTransformations.transformations;

import math.matrix.Matrix4f;

public class RotateTransformation implements Transformation {
    private final Matrix4f rotation;

    public RotateTransformation(Axis axis, double angle) {
       this.rotation =  createRotationMatrix(axis, angle);
    }

    private Matrix4f createRotationMatrix(Axis axis, double angle) {
        switch (axis) {
            case X:
                return new Matrix4f(new float[][]
                        {
                        {1, 0, 0, 0},
                        {0, (float) Math.cos(angle), (float) Math.sin(angle), 0},
                        {0, (float) -Math.sin(angle), (float) Math.cos(angle), 0},
                        {0, 0, 0, 1}
                        }
                );
            case Y:
                return new Matrix4f(new float[][]
                        {
                        {(float) Math.cos(angle), 0, (float) Math.sin(angle), 0},
                        {0, 1, 0, 0},
                        {(float) -Math.sin(angle), 0, (float) Math.cos(angle), 0},
                        {0, 0, 0, 1}
                        }
                );
            case Z:
                return new Matrix4f(new float[][]
                        {
                                {(float) Math.cos(angle), (float) Math.sin(angle), 0, 0},
                                {(float) -Math.sin(angle), (float) Math.cos(angle), 0, 0},
                                {0, 0, 1, 0},
                                {0, 0, 0, 1}
                        }
                );
            default:
                throw new IllegalArgumentException("Unknown axis:" + axis);
        }
    }

    @Override
    public Matrix4f getMatrix() {
        Matrix4f matrix = new Matrix4f();
        matrix.set(rotation);
        return matrix;
    }
}
