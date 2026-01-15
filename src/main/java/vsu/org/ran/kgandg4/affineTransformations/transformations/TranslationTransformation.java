package vsu.org.ran.kgandg4.affineTransformations.transformations;

import math.vector.Vector3f;
import math.matrix.Matrix4f;

public class TranslationTransformation implements Transformation {
    private final float tx, ty, tz;

    public TranslationTransformation(float tx, float ty, float tz) {
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
    }

    /**
     * Конструктор с Vector3f
     */
    public TranslationTransformation(Vector3f translation) {
        this.tx = translation.getX();
        this.ty = translation.getY();
        this.tz = translation.getZ();
    }

    @Override
    public Matrix4f getMatrix() {
        return new Matrix4f(new float[][]
                {
                        {1, 0, 0, tx},
                        {0, 1, 0, ty},
                        {0, 0, 1, tz},
                        {0, 0, 0, 1}
                }
        );
    }
}
