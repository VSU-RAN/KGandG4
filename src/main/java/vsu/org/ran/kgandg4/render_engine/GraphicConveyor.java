package vsu.org.ran.kgandg4.render_engine;

import math.point.Point2f;
import math.vector.Vector3f;
import math.vector.Vector4f;
import math.matrix.Matrix4f;
import math.quaternion.Quat4f;

import vsu.org.ran.kgandg4.affineTransformations.AffineBuilder;
import vsu.org.ran.kgandg4.affineTransformations.transformations.TranslationTransformation;
import vsu.org.ran.kgandg4.affineTransformations.transformations.RotateTransformationOnQuad;

public class GraphicConveyor {

    // Параметры аффинных преобразований модели
    private static Vector3f modelTranslation = new Vector3f(0, 0, 0);
    private static Vector3f modelRotation = new Vector3f(0, 0, 0); // Углы Эйлера в радианах
    private static Vector3f modelScale = new Vector3f(1, 1, 1);

    /**
     * Устанавливает параметры преобразований модели
     */
    public static void setModelTransformations(Vector3f translation, Vector3f rotation, Vector3f scale) {
        modelTranslation = translation != null ? translation : new Vector3f(0, 0, 0);
        modelRotation = rotation != null ? rotation : new Vector3f(0, 0, 0);
        modelScale = scale != null ? scale : new Vector3f(1, 1, 1);
    }

    /**
     * Возвращает матрицу преобразований модели (масштабирование, вращение, перенос)
     * Порядок применения: Scale -> Rotate -> Translate
     * В матрицах это T * R * S (применяется справа налево)
     */
    public static Matrix4f rotateScaleTranslate() {
        // Если все преобразования по умолчанию, возвращаем единичную матрицу
        if (modelTranslation.getX() == 0.0f && modelTranslation.getY() == 0.0f && modelTranslation.getZ() == 0.0f &&
            modelRotation.getX() == 0.0f && modelRotation.getY() == 0.0f && modelRotation.getZ() == 0.0f &&
            modelScale.getX() == 1.0f && modelScale.getY() == 1.0f && modelScale.getZ() == 1.0f) {
            return Matrix4f.identityMatrix();
        }

        AffineBuilder affineBuilder = new AffineBuilder();

        // 1. Перенос (применяется последним в матрице, но добавляется первым)
        if (modelTranslation.getX() != 0.0f || modelTranslation.getY() != 0.0f || modelTranslation.getZ() != 0.0f) {
            TranslationTransformation translationTransform = new TranslationTransformation(modelTranslation);
            affineBuilder.addTransformation(translationTransform);
        }

        // 2. Вращение (в порядке Z, Y, X - углы Эйлера)
        if (modelRotation.getX() != 0.0f || modelRotation.getY() != 0.0f || modelRotation.getZ() != 0.0f) {
            // Создаём кватернион для вращения вокруг оси Z
            float halfZ = modelRotation.getZ() * 0.5f;
            Quat4f rotationQuat = new Quat4f(
                    0,
                    0,
                    (float) -Math.sin(halfZ),
                    (float) Math.cos(halfZ)
            );

            // Создаем кватернион для вращения вокруг оси Y
            float halfY = modelRotation.getY() * 0.5f;
            Quat4f tempQuat = new Quat4f(
                    0,
                    (float) Math.sin(halfY),
                    0,
                    (float) Math.cos(halfY)
            );
            rotationQuat.multiplies(tempQuat, rotationQuat);

            // Создаем кватернион для вращения вокруг оси X
            float halfX = modelRotation.getX() * 0.5f;
            tempQuat = new Quat4f(
                    (float) -Math.sin(halfX),
                    0,
                    0,
                    (float) Math.cos(halfX)
            );
            rotationQuat.multiplies(tempQuat, rotationQuat);

            RotateTransformationOnQuad rotateTransform = new RotateTransformationOnQuad(rotationQuat);
            affineBuilder.addTransformation(rotateTransform);
        }

        // 3. Масштабирование (применяется первым в матрице, но добавляется последним)
        Matrix4f result = affineBuilder.build().getMatrix();

        if (modelScale.getX() != 1.0f || modelScale.getY() != 1.0f || modelScale.getZ() != 1.0f) {
            Matrix4f scaleMatrix = Matrix4f.identityMatrix();
            scaleMatrix.setElement(0, 0, modelScale.getX());
            scaleMatrix.setElement(1, 1, modelScale.getY());
            scaleMatrix.setElement(2, 2, modelScale.getZ());
            // Масштабирование применяется первым: result = T * R * S
            result.multiplyV(scaleMatrix);
        }

        return result;
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX;
        Vector3f resultY;
        Vector3f resultZ;

//        resultZ.sub(target, eye);
//        resultX.cross(up, resultZ);
//        resultY.cross(resultZ, resultX);

        resultZ = Vector3f.subtract(target, eye);
        resultX = Vector3f.crossProduct(up, resultZ);
        resultY = Vector3f.crossProduct(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

//        return new Matrix4f(
//                {resultX.x, resultX.y, resultX.z, -(resultX.dot(eye)),
//                 resultY.x, resultY.y, resultY.z, -(resultY.dot(eye)),
//                 resultZ.x, resultZ.y, resultZ.z, -(resultZ.dot(eye)),
//                 0, 0, 0, 1}
//        );
        return new Matrix4f(
                new float[]
                {
                 resultX.getX(), resultX.getY(), resultX.getZ(), -(resultX.dotProduct(eye)),
                 resultY.getX(), resultY.getY(), resultY.getZ(), -(resultY.dotProduct(eye)),
                 resultZ.getX(), resultZ.getY(), resultZ.getZ(), -(resultZ.dotProduct(eye)),
                 0, 0, 0, 1
                }
        );
        //Как в методичке
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {

        float tangentsMinusOnDegreeFov = (float) (1.0F / (Math.tan(Math.toRadians(fov) * 0.5F)));

        return new Matrix4f(
                new float[]{tangentsMinusOnDegreeFov / aspectRatio, 0, 0, 0,
                0, tangentsMinusOnDegreeFov, 0, 0,
                0, 0, (farPlane + nearPlane) / (farPlane - nearPlane), (2 * farPlane * nearPlane) / (nearPlane - farPlane),
                0, 0, 1, 0}
        );
        //Как в методичке
    }

    public static Vector3f getVertexAfterMVPandNormalize(final Matrix4f PVM, final Vector3f vertex) {
        Vector4f vertexWithW = new Vector4f(vertex.getX(), vertex.getY(), vertex.getZ(), 1.0F);

        Vector4f result4f = PVM.transformed(vertexWithW); // v' = PVM * v;

        return new Vector3f(result4f.getX() / result4f.getW(),
                            result4f.getY() / result4f.getW(),
                            result4f.getZ() / result4f.getW());
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f((float) (width - 1) / 2 * vertex.getX() + (float) (width - 1) / 2,
                           (float) (1 - height) / 2 * vertex.getY() + (float) (height - 1) / 2);
        //Как в методичке
    }

    public static boolean isValidVertex(Vector3f vertex) {
        if (vertex.getX() > 1.0F || vertex.getX() < -1.0F) return false;

        if (vertex.getY() > 1.0F || vertex.getY() < -1.0F) return false;

        if (vertex.getZ() > 1.0F || vertex.getZ() < -1.0F) return false;

        return true;
    }
}
