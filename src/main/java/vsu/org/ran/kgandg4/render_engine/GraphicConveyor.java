package vsu.org.ran.kgandg4.render_engine;

import math.point.Point2f;
import math.vector.Vector3f;
import math.vector.Vector4f;
import math.matrix.Matrix4f;
import math.quaternion.Quat4f;

import vsu.org.ran.kgandg4.affineTransformations.AffineBuilder;
import vsu.org.ran.kgandg4.affineTransformations.transformations.TranslationTransformation;
import vsu.org.ran.kgandg4.affineTransformations.transformations.RotateTransformationOnQuad;
import vsu.org.ran.kgandg4.camera.Camera;

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
        Vector3f resultZ = Vector3f.subtract(target, eye);
        Vector3f resultX = Vector3f.crossProduct(up, resultZ);
        Vector3f resultY = Vector3f.crossProduct(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        return new Matrix4f(
                new float[]{
                     resultX.getX(), resultX.getY(), resultX.getZ(), -(resultX.dotProduct(eye)),
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

        return new Vector3f(
                result4f.getX() / result4f.getW(),
                result4f.getY() / result4f.getW(),
                result4f.getZ() / result4f.getW()
        );
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(
                (float) (width - 1) / 2 * vertex.getX() + (float) (width - 1) / 2,
                (float) (1 - height) / 2 * vertex.getY() + (float) (height - 1) / 2
        );
        //Как в методичке
    }

    public static boolean isValidVertex(Vector3f vertex) {
        if (vertex.getX() > 1.0F || vertex.getX() < -1.0F) return false;
        if (vertex.getY() > 1.0F || vertex.getY() < -1.0F) return false;
        if (vertex.getZ() > 1.0F || vertex.getZ() < -1.0F) return false;

        return true;
    }

    public static Vector3f screenToModel(
            float screenX, float screenY, float zbufferDepth,
            Camera camera, Matrix4f modelMatrix, int width, int height) {

        if (camera == null) {
            return null;
        }

        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        return screenToModel(
                screenX, screenY, zbufferDepth,
                width, height,
                viewMatrix, projectionMatrix, modelMatrix);

    }

    public static Vector3f screenToModel(
            float screenX, float screenY, float zbufferDepth,
            int width, int height,
            Matrix4f viewMatrix, Matrix4f projectionMatrix, Matrix4f modelMatrix) {

        // 1. Экран → NDC
        float ndcX = (2.0f * screenX) / width - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / height;

        // 2. NDC → Clip Space
        Vector4f clipCoords = new Vector4f(ndcX, ndcY, zbufferDepth, 1.0f);

        // 3. Clip Space → глаз (обратная проекция)
        Matrix4f inverseProj = Mathet.invert(projectionMatrix);
        Vector4f eyeCoords = inverseProj.transformed(clipCoords);

        // Перспективное деление
        if (Math.abs(eyeCoords.getW()) > 0.00001f) {
            eyeCoords = new Vector4f(
                    eyeCoords.getX() / eyeCoords.getW(),
                    eyeCoords.getY() / eyeCoords.getW(),
                    eyeCoords.getZ() / eyeCoords.getW(),
                    1.0f
            );
        }

        // 4. Глаз → мир (обратный вид)
        Matrix4f inverseView = Mathet.invert(viewMatrix);
        if (inverseView == null) {
            System.out.println("Ошибка: не удалось инвертировать видовую матрицу");
            return new Vector3f(eyeCoords.getX(), eyeCoords.getY(), eyeCoords.getZ());
        }
        Vector4f worldCoords = inverseView.transformed(eyeCoords);


        // 5. Мир → модель (обратная модель)
        Matrix4f inverseModel = Mathet.invert(modelMatrix);
        if (inverseModel == null) {
            System.out.println("Ошибка: не удалось инвертировать матрицу модели");
            return new Vector3f(worldCoords.getX(), worldCoords.getY(), worldCoords.getZ());
        }
        Vector4f modelCoords = inverseModel.transformed(worldCoords);

        return new Vector3f(
                modelCoords.getX(),
                modelCoords.getY(),
                modelCoords.getZ()
        );
    }

    public static boolean isFrontFace(Vector3f v0, Vector3f v1, Vector3f v2, Vector3f cameraPosition) {
        Vector3f edge1 = Vector3f.subtract(v1, v0);
        Vector3f edge2 = Vector3f.subtract(v2, v0);
        Vector3f normal = Vector3f.crossProduct(edge1, edge2);
        normal.normalize();

        // Вектор от камеры к центру треугольника
        Vector3f triangleCenter = new Vector3f(
                (v0.getX() + v1.getX() + v2.getX()) / 3.0f,
                (v0.getY() + v1.getY() + v2.getY()) / 3.0f,
                (v0.getZ() + v1.getZ() + v2.getZ()) / 3.0f
        );

        Vector3f viewToTriangle = Vector3f.subtract(triangleCenter, cameraPosition);
        viewToTriangle.normalize();

        float dot = normal.dotProduct(viewToTriangle);

        return dot < 0;
    }
}
