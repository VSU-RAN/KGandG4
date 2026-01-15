package vsu.org.ran.kgandg4.affineTransformations.transformations;

import math.matrix.Matrix4f;

public class SaveTransformation implements Transformation{
    private final Matrix4f saveCondition;

    public SaveTransformation(Matrix4f matrix) {
        this.saveCondition = matrix.copy(matrix);  // = (Matrix4f) matrix.clone();
    }

    @Override
    public Matrix4f getMatrix() {
        return new Matrix4f(this.saveCondition.copy());
    }
}
