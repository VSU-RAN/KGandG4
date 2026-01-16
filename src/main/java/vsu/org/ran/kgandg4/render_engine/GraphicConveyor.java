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
            // Создаем кватернион для вращения вокруг оси Z
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

        float tangensMinusOnDegreeFov = (float) (1.0F / (Math.tan(Math.toRadians(fov) * 0.5F)));

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

    public static Vector3f screenToModel(
            float screenX, float screenY, float zbufferDepth, // Это [0,1]
            int width, int height,
            Matrix4f viewMatrix, Matrix4f projectionMatrix, Matrix4f modelMatrix,
            float near, float far) {

        try {
            System.out.println("\n=== Начало преобразования screenToModel ===");

            // 1. Экран → NDC
            float ndcX = (2.0f * screenX) / width - 1.0f;
            float ndcY = 1.0f - (2.0f * screenY) / height;

            // ВАЖНО: Преобразовать zbufferDepth [0,1] → NDC Z [-1,1]
            float ndcZ = 2.0f * zbufferDepth - 1.0f;

            System.out.printf("Экран: (%.1f, %.1f) → NDC: (%.3f, %.3f, %.3f)%n",
                    screenX, screenY, ndcX, ndcY, ndcZ);
            System.out.printf("Z-буфер: %.6f → NDC Z: %.6f, near=%.2f, far=%.2f%n",
                    zbufferDepth, ndcZ, near, far);

            // УДАЛИТЬ ЭТОТ БЛОК (неправильное преобразование):
            // float C = (far + near) / (far - near);
            // float D = (2 * far * near) / (near - far);
            // float linearDepth = D / (zbufferDepth - C);
            // System.out.printf("Кастомное преобразование: C=%.6f, D=%.6f, linearDepth=%.3f%n",
            //        C, D, linearDepth);

            // 2. NDC → клип (ИСПОЛЬЗУЕМ ndcZ!)
            Vector4f clipCoords = new Vector4f(ndcX, ndcY, ndcZ, 1.0f);
            System.out.printf("Clip: (%.3f, %.3f, %.6f, 1.0)%n",
                    clipCoords.getX(), clipCoords.getY(), clipCoords.getZ());

            // 3. Клип → глаз (обратная проекция) - ДАЛЬШЕ ВСЁ ПРАВИЛЬНО
            System.out.println("Инвертируем проекционную матрицу...");
            Matrix4f inverseProj = GraphicConveyor.invert(projectionMatrix);

            // Умножаем обратную проекционную матрицу на вектор
            Vector4f eyeCoords = inverseProj.transformed(clipCoords);
            System.out.printf("Eye (до деления): (%.3f, %.3f, %.3f, w=%.3f)%n",
                    eyeCoords.getX(), eyeCoords.getY(), eyeCoords.getZ(), eyeCoords.getW());

            // Перспективное деление
            if (Math.abs(eyeCoords.getW()) > 0.00001f) {
                eyeCoords = new Vector4f(
                        eyeCoords.getX() / eyeCoords.getW(),
                        eyeCoords.getY() / eyeCoords.getW(),
                        eyeCoords.getZ() / eyeCoords.getW(),
                        1.0f
                );
            }

            System.out.printf("Eye (после деления): (%.3f, %.3f, %.3f)%n",
                    eyeCoords.getX(), eyeCoords.getY(), eyeCoords.getZ());

            // 4. Глаз → мир (обратный вид)
            System.out.println("Инвертируем видовую матрицу...");
            Matrix4f inverseView = GraphicConveyor.invert(viewMatrix);

            if (inverseView == null) {
                System.out.println("Ошибка: не удалось инвертировать видовую матрицу");
                return new Vector3f(eyeCoords.getX(), eyeCoords.getY(), eyeCoords.getZ());
            }

            // Умножаем обратную видовую матрицу на вектор
            Vector4f worldCoords = inverseView.transformed(eyeCoords);
            System.out.printf("World: (%.3f, %.3f, %.3f)%n",
                    worldCoords.getX(), worldCoords.getY(), worldCoords.getZ());

            // 5. Мир → модель (обратная модель)
            System.out.println("Инвертируем матрицу модели...");
            Matrix4f inverseModel = GraphicConveyor.invert(modelMatrix);

            if (inverseModel == null) {
                System.out.println("Ошибка: не удалось инвертировать матрицу модели");
                return new Vector3f(worldCoords.getX(), worldCoords.getY(), worldCoords.getZ());
            }

            // Умножаем обратную матрицу модели на вектор
            Vector4f modelCoords = inverseModel.transformed(worldCoords);
            System.out.printf("Model: (%.3f, %.3f, %.3f)%n",
                    modelCoords.getX(), modelCoords.getY(), modelCoords.getZ());

            System.out.println("=== Преобразование завершено ===\n");

            return new Vector3f(
                    modelCoords.getX(),
                    modelCoords.getY(),
                    modelCoords.getZ()
            );

        } catch (Exception e) {
            System.err.println("Ошибка в screenToModel: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Matrix4f invert(Matrix4f m) {
        if (m == null) return null;

        float[] inv = new float[16];

         float[] mat = new float[16];
         for (int i = 0; i < 4; i++) {
             for (int j = 0; j < 4; j++) {
                 mat[i*4 + j] = m.getElement(i, j);
             }
         }

        inv[0] = mat[5] * mat[10] * mat[15] -
                mat[5] * mat[11] * mat[14] -
                mat[9] * mat[6] * mat[15] +
                mat[9] * mat[7] * mat[14] +
                mat[13] * mat[6] * mat[11] -
                mat[13] * mat[7] * mat[10];

        inv[4] = -mat[4] * mat[10] * mat[15] +
                mat[4] * mat[11] * mat[14] +
                mat[8] * mat[6] * mat[15] -
                mat[8] * mat[7] * mat[14] -
                mat[12] * mat[6] * mat[11] +
                mat[12] * mat[7] * mat[10];

        inv[8] = mat[4] * mat[9] * mat[15] -
                mat[4] * mat[11] * mat[13] -
                mat[8] * mat[5] * mat[15] +
                mat[8] * mat[7] * mat[13] +
                mat[12] * mat[5] * mat[11] -
                mat[12] * mat[7] * mat[9];

        inv[12] = -mat[4] * mat[9] * mat[14] +
                mat[4] * mat[10] * mat[13] +
                mat[8] * mat[5] * mat[14] -
                mat[8] * mat[6] * mat[13] -
                mat[12] * mat[5] * mat[10] +
                mat[12] * mat[6] * mat[9];

        inv[1] = -mat[1] * mat[10] * mat[15] +
                mat[1] * mat[11] * mat[14] +
                mat[9] * mat[2] * mat[15] -
                mat[9] * mat[3] * mat[14] -
                mat[13] * mat[2] * mat[11] +
                mat[13] * mat[3] * mat[10];

        inv[5] = mat[0] * mat[10] * mat[15] -
                mat[0] * mat[11] * mat[14] -
                mat[8] * mat[2] * mat[15] +
                mat[8] * mat[3] * mat[14] +
                mat[12] * mat[2] * mat[11] -
                mat[12] * mat[3] * mat[10];

        inv[9] = -mat[0] * mat[9] * mat[15] +
                mat[0] * mat[11] * mat[13] +
                mat[8] * mat[1] * mat[15] -
                mat[8] * mat[3] * mat[13] -
                mat[12] * mat[1] * mat[11] +
                mat[12] * mat[3] * mat[9];

        inv[13] = mat[0] * mat[9] * mat[14] -
                mat[0] * mat[10] * mat[13] -
                mat[8] * mat[1] * mat[14] +
                mat[8] * mat[2] * mat[13] +
                mat[12] * mat[1] * mat[10] -
                mat[12] * mat[2] * mat[9];

        inv[2] = mat[1] * mat[6] * mat[15] -
                mat[1] * mat[7] * mat[14] -
                mat[5] * mat[2] * mat[15] +
                mat[5] * mat[3] * mat[14] +
                mat[13] * mat[2] * mat[7] -
                mat[13] * mat[3] * mat[6];

        inv[6] = -mat[0] * mat[6] * mat[15] +
                mat[0] * mat[7] * mat[14] +
                mat[4] * mat[2] * mat[15] -
                mat[4] * mat[3] * mat[14] -
                mat[12] * mat[2] * mat[7] +
                mat[12] * mat[3] * mat[6];

        inv[10] = mat[0] * mat[5] * mat[15] -
                mat[0] * mat[7] * mat[13] -
                mat[4] * mat[1] * mat[15] +
                mat[4] * mat[3] * mat[13] +
                mat[12] * mat[1] * mat[7] -
                mat[12] * mat[3] * mat[5];

        inv[14] = -mat[0] * mat[5] * mat[14] +
                mat[0] * mat[6] * mat[13] +
                mat[4] * mat[1] * mat[14] -
                mat[4] * mat[2] * mat[13] -
                mat[12] * mat[1] * mat[6] +
                mat[12] * mat[2] * mat[5];

        inv[3] = -mat[1] * mat[6] * mat[11] +
                mat[1] * mat[7] * mat[10] +
                mat[5] * mat[2] * mat[11] -
                mat[5] * mat[3] * mat[10] -
                mat[9] * mat[2] * mat[7] +
                mat[9] * mat[3] * mat[6];

        inv[7] = mat[0] * mat[6] * mat[11] -
                mat[0] * mat[7] * mat[10] -
                mat[4] * mat[2] * mat[11] +
                mat[4] * mat[3] * mat[10] +
                mat[8] * mat[2] * mat[7] -
                mat[8] * mat[3] * mat[6];

        inv[11] = -mat[0] * mat[5] * mat[11] +
                mat[0] * mat[7] * mat[9] +
                mat[4] * mat[1] * mat[11] -
                mat[4] * mat[3] * mat[9] -
                mat[8] * mat[1] * mat[7] +
                mat[8] * mat[3] * mat[5];

        inv[15] = mat[0] * mat[5] * mat[10] -
                mat[0] * mat[6] * mat[9] -
                mat[4] * mat[1] * mat[10] +
                mat[4] * mat[2] * mat[9] +
                mat[8] * mat[1] * mat[6] -
                mat[8] * mat[2] * mat[5];

        float det = mat[0] * inv[0] + mat[1] * inv[4] + mat[2] * inv[8] + mat[3] * inv[12];

        if (Math.abs(det) < 0.00001f) {
            System.err.println("Matrix is singular, cannot invert");
            return null;
        }

        det = 1.0f / det;

        for (int i = 0; i < 16; i++) {
            inv[i] *= det;
        }

        return new Matrix4f(inv);
    }
}
