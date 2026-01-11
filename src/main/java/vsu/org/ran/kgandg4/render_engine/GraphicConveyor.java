package vsu.org.ran.kgandg4.render_engine;


import math.point.Point2f;
import math.vector.Vector3f;
import math.vector.Vector4f;
import math.matrix.Matrix4f;


public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        return Matrix4f.identityMatrix();
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX;
        Vector3f resultY;
        Vector3f resultZ;

        resultZ = Vector3f.subtract(target, eye);
        resultX = Vector3f.crossProduct(up, resultZ);
        resultY = Vector3f.crossProduct(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        return new Matrix4f(
                new float[]{resultX.getX(), resultX.getY(), resultX.getZ(), -(resultX.dotProduct(eye)),
                 resultY.getX(), resultY.getY(), resultY.getZ(), -(resultY.dotProduct(eye)),
                 resultZ.getX(), resultZ.getY(), resultZ.getZ(), -(resultZ.dotProduct(eye)),
                 0, 0, 0, 1}
        );
        //Как в методичке
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        float tangensMinusOnDegreeFov = (float) (1.0F / (Math.tan(fov * 0.5F)));

        return new Matrix4f(
                new float[]{tangensMinusOnDegreeFov / aspectRatio, 0, 0, 0,
                0, tangensMinusOnDegreeFov, 0, 0,
                0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), (2 * farPlane * nearPlane) / (nearPlane - farPlane),
                0, 0, 1, 0}
        );
        //Как в методичке
    }

    public static Vector3f getVertexAfterMVPandNormalize(final Matrix4f PVM, final Vector3f vertex) {
        Vector4f vertexWithW = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0F);

        Vector4f result4f = PVM.transformed(vertexWithW); // v' = PVM * v;

        return new Vector3f(result4f.getX() / result4f.getW(),
                result4f.getY() / result4f.getW(), result4f.getZ() / result4f.getW());
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f((float) (width - 1) / 2 * vertex.getX() + (float) (width - 1) / 2, (float) (1 - height) / 2 * vertex.getY() + (float) (height - 1) / 2);
        //Как в методичке
    }

    public static boolean isValidVertex(Vector3f vertex) {
        if (vertex.getX() > 1.0F || vertex.getX() < -1.0F) return false;

        if (vertex.getY() > 1.0F || vertex.getY() < -1.0F) return false;

        if (vertex.getZ() > 1.0F || vertex.getZ() < -1.0F) return false;

        return true;
    }
}
