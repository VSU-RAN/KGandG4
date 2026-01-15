package vsu.org.ran.kgandg4.affineTransformations;

import java.util.ArrayList;
import java.util.List;

import math.matrix.Matrix4f;

import vsu.org.ran.kgandg4.affineTransformations.transformations.Transformation;

public class CompositeTransformation implements Transformation {
    private final List<Transformation> transformations;
    private Matrix4f cachedMatrix;
    private boolean isDirty;

    public CompositeTransformation() {
        this.transformations = new ArrayList<>();
        this.cachedMatrix = new Matrix4f(new float[][]
                {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
                }
        );
        this.isDirty = false;
    }

    public void add(Transformation transformation) {
        this.transformations.add(transformation);
        this.isDirty = true;
    }

    private void updateCachedMatrix() {
        Matrix4f result = new Matrix4f(new float[][]
                {
                        {1, 0, 0, 0},
                        {0, 1, 0, 0},
                        {0, 0, 1, 0},
                        {0, 0, 0, 1}
                }
        );


        for (Transformation transformation : transformations) {
//            Matrix4f temp = new Matrix4f();
//            temp.mul(transformation.getMatrix(), result);
//            result.set(temp);
            Matrix4f temp = Matrix4f.multiply(transformation.getMatrix(), result);
            result.set(temp);
        }
        this.cachedMatrix.set(result);
        this.isDirty = false;
    }
    @Override
    public Matrix4f getMatrix() {
        if (isDirty) {
            updateCachedMatrix();
        }
        return new Matrix4f(cachedMatrix.copy());
    }
}
