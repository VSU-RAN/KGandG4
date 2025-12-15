package vsu.org.ran.kgandg4.render_engine;


import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX.cross(up, resultZ);
        resultY.cross(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        return new Matrix4f(
                resultX.x, resultX.y, resultX.z, -(resultX.dot(eye)),
                resultY.x, resultY.y, resultY.z, -(resultY.dot(eye)),
                resultZ.x, resultZ.y, resultZ.z, -(resultZ.dot(eye)),
                0, 0, 0, 1
        );
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        float tangensMinusOnDegreeFov = (float) (1.0F / (Math.tan(fov * 0.5F)));

        return new Matrix4f(
                tangensMinusOnDegreeFov, 0, 0, 0,
                0, tangensMinusOnDegreeFov / aspectRatio, 0, 0,
                0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), (2 * farPlane * nearPlane) / (nearPlane - farPlane),
                0, 0, 1, 0
        );
    }

    public static Vector3f getVertexAfterMVPandNormalize(final Matrix4f MVP, final Vector3f vertex) {
        Vector4f vertexWithW = new Vector4f(vertex.x, vertex.y, vertex.z, 1.0F);

        Vector4f result4f = new Vector4f();

        MVP.transform(vertexWithW, result4f);

        return new Vector3f(result4f.x / result4f.w, result4f.y / result4f.w, result4f.z / result4f.w);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }

    public static boolean isValidVertex(Vector3f vertex) {
        if (vertex.x > 1.0F || vertex.x < -1.0F) return false;

        if (vertex.y > 1.0F || vertex.y < -1.0F) return false;

        if (vertex.z > 1.0F || vertex.z < -1.0F) return false;

        return true;
    }
}
