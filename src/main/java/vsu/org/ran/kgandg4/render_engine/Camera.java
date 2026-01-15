package vsu.org.ran.kgandg4.render_engine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import math.quaternion.Quat4f;
import math.vector.Vector3f;
import math.matrix.Matrix4f;
import math.vector.Vector4f;
import vsu.cs.AffineBuilder;
import vsu.cs.transformations.Axis;
import vsu.cs.transformations.RotateTransformationOnQuad;
import vsu.cs.transformations.Transformation;
import vsu.cs.transformations.TranslationTransformation;

import javax.swing.plaf.basic.BasicBorders;
import java.util.Objects;
import java.util.Vector;

public class Camera {
    private final int id;
    private final ObjectProperty<Vector3f> position = new SimpleObjectProperty<>();
    private final ObjectProperty<Vector3f> target = new SimpleObjectProperty<>();
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    public Camera(
            final int id,
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        this.id = id;
        this.position.set(position);
        this.target.set(target);
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public void setPosition(final Vector3f position) {
        this.position.set(position);
    }

    public void setTarget(final Vector3f target) {
        this.target.set(target);
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public int getId() {
        return id;
    }

    public Vector3f getPosition() {
        return this.position.get();
    }

    public Vector3f getTarget() {
        return target.get();
    }

    public ObjectProperty<Vector3f> positionProperty() {
        return position;
    }

    public ObjectProperty<Vector3f> targetProperty() {
        return target;
    }

    public void movePosition(final Vector3f translation) {
        Vector3f newPos = new Vector3f(this.position.get());
        newPos.addV(translation);
        this.position.set(newPos);
    }

    public void moveTarget(final Vector3f translation) {
        this.target.get().addV(translation);
    }

    /**
     * Панорамирование
     */
    public void movePositionAndTarget(Vector3f translation) {
        Vector3f newPos = new Vector3f(this.position.get());
        newPos.addV(translation);
        this.position.set(newPos);

        Vector3f newTarget = new Vector3f(this.target.get());
        newTarget.addV(translation);
        this.target.set(newTarget);
    }

    // 1. Преобразование кватерниона в матрицу поворота (3x3):
//        Quat4f quat = new Quat4f(0, 0, 0, 1);
//
//        Matrix4f matrix = new Matrix4f();
//        matrix.set(quat);
//
//        // 2. Создание матрицы перемещения (4x4):
//        Matrix4f matrixT = Matrix4f.identityMatrix();
//
//        matrixT.setElement(0, 3, translation.getX());
//        matrixT.setElement(1, 3, translation.getY());
//        matrixT.setElement(2, 3, translation.getZ());
//
//        // 3. Умножение матриц:
//        matrixT.multiplyV(matrix);

    public void movePositionAndTargetTest(Vector3f translation) {
        // Используем аффинные преобразования с TranslationTransformation
        TranslationTransformation translationTransform = new TranslationTransformation(translation);
        AffineBuilder affineBuilder = new AffineBuilder();
        affineBuilder.addTransformation(translationTransform);
        Matrix4f transformationMatrix = affineBuilder.build().getMatrix();

        // Применяем преобразование к позиции камеры
        Vector4f position4 = new Vector4f(
                getPosition().getX(),
                getPosition().getY(),
                getPosition().getZ(),
                1.0f
        );
        Matrix4f.transform(transformationMatrix, position4);

        // Применяем преобразование к цели
        Vector4f target4 = new Vector4f(
                getTarget().getX(),
                getTarget().getY(),
                getTarget().getZ(),
                1.0f
        );
        Matrix4f.transform(transformationMatrix, target4);

        // Устанавливаем новые значения
        setPosition(new Vector3f(position4.getX(), position4.getY(), position4.getZ()));
        setTarget(new Vector3f(target4.getX(), target4.getY(), target4.getZ()));
    }

    /**
     * Орбитальное вращение вокруг target
     */
    public void orbit(float horizontalAngle, float verticalAngle) {
        Vector3f center = this.target.get();
        Vector3f cameraPos = this.position.get();

        // Вектор от target к камере
        Vector3f offset = cameraPos.subtract(center);

        // Сохраняем расстояние
        float distance = offset.length();

        // Переводим в сферические координаты
        float radius = distance;
        float theta = (float) Math.atan2(offset.getX(), offset.getZ()); // горизонтальный угол
        float phi = (float) Math.acos(offset.getY() / radius);     // вертикальный угол

        // Добавляем новые углы
        theta += horizontalAngle;
        phi += verticalAngle;

        // Ограничиваем вертикальный угол
        phi = Math.max(0.1f, Math.min((float) Math.PI - 0.1f, phi));

        // Конвертируем обратно в декартовы координаты
        float x = radius * (float) Math.sin(phi) * (float) Math.sin(theta);
        float y = radius * (float) Math.cos(phi);
        float z = radius * (float) Math.sin(phi) * (float) Math.cos(theta);

        // Новая позиция камеры
        Vector3f newPosition = new Vector3f(x, y, z);
        newPosition.addV(center);

        this.position.set(newPosition);
    }

    public void orbitTest(float horizontalAngle, float verticalAngle) {
        Vector3f center = getTarget();
        Vector3f cameraPos = getPosition();

        // 1. Вычисляем вектор от цели к камере
        Vector3f offset = cameraPos.subtract(center);
        float distance = offset.length();

        if (distance < 0.0001f) {
            return; // Нельзя вращать, если камера находится в центре
        }

        // 2. Нормализуем offset для работы с ним
        Vector3f normalizedOffset = new Vector3f(offset);
        normalizedOffset.normalize();

        // 3. Создаем кватернион для горизонтального вращения вокруг глобальной оси Y
        float hAngle = horizontalAngle * 0.5f;
        Quat4f quatY = new Quat4f(0, (float)Math.sin(hAngle), 0, (float)Math.cos(hAngle));

        // 4. Применяем горизонтальное вращение к нормализованному offset для получения промежуточного направления
        RotateTransformationOnQuad rotateYTransform = new RotateTransformationOnQuad(quatY);
        AffineBuilder builderY = new AffineBuilder();
        builderY.addTransformation(rotateYTransform);
        Matrix4f rotationYMatrix = builderY.build().getMatrix();

        Vector4f offsetAfterY = new Vector4f(
                normalizedOffset.getX(),
                normalizedOffset.getY(),
                normalizedOffset.getZ(),
                1.0f  // Используем 1.0f для правильной работы с матрицей вращения
        );
        Matrix4f.transform(rotationYMatrix, offsetAfterY);
        Vector3f offsetY = new Vector3f(offsetAfterY.getX(), offsetAfterY.getY(), offsetAfterY.getZ());
        offsetY.normalize();

        // 5. Вычисляем локальную ось для вертикального вращения после горизонтального вращения
        // Локальная ось перпендикулярна новому направлению камеры и глобальной оси Y
        Vector3f forward = new Vector3f(offsetY);
        Vector3f up = new Vector3f(0, 1, 0);
        Vector3f right = Vector3f.crossProduct(forward, up);
        float rightLength = right.length();
        
        // Если forward параллелен up, используем ось X
        if (rightLength < 0.0001f) {
            right = new Vector3f(1, 0, 0);
        } else {
            right.normalize();
        }

        // 6. Создаем кватернион для вертикального вращения вокруг локальной оси
        float vAngle = verticalAngle * 0.5f;
        float sinHalf = (float) Math.sin(vAngle);
        float cosHalf = (float) Math.cos(vAngle);
        Quat4f quatX = new Quat4f(
                right.getX() * sinHalf,
                right.getY() * sinHalf,
                right.getZ() * sinHalf,
                cosHalf
        );

        // 7. Комбинируем вращения: сначала Y (горизонтальное), затем X (вертикальное)
        // Порядок: q_result = qX * qY (применяем сначала Y, затем X)
        Quat4f combinedQuat = new Quat4f();
        combinedQuat.multiplies(quatX, quatY);

        // 8. Создаем аффинное преобразование с комбинированным кватернионом
        RotateTransformationOnQuad rotateTransform = new RotateTransformationOnQuad(combinedQuat);
        AffineBuilder affineBuilder = new AffineBuilder();
        affineBuilder.addTransformation(rotateTransform);
        Matrix4f rotationMatrix = affineBuilder.build().getMatrix();

        // 9. Применяем комбинированное вращение к исходному offset (с исходной длиной)
        Vector4f offset4 = new Vector4f(
                offset.getX(),
                offset.getY(),
                offset.getZ(),
                1.0f  // Используем 1.0f для правильной работы с матрицей вращения
        );
        Matrix4f.transform(rotationMatrix, offset4);

        // 10. Получаем новый offset и нормализуем для сохранения расстояния
        Vector3f newOffset = new Vector3f(
                offset4.getX(),
                offset4.getY(),
                offset4.getZ()
        );

        // Сохраняем исходное расстояние - нормализуем и масштабируем обратно
        float newLength = newOffset.length();
        if (newLength > 0.0001f) {
            // Вращение не должно изменять длину вектора, но из-за ошибок округления нормализуем
            newOffset.multiplyV(distance / newLength);
        } else {
            // Если длина стала нулевой, используем исходный offset
            newOffset = new Vector3f(offset);
            newOffset.normalize();
            newOffset.multiplyV(distance);
        }

        // 11. Новая позиция камеры
        Vector3f newPosition = center.add(newOffset);
        setPosition(newPosition);
    }
    /**
     * Зум
     */
    public void zoom(float amount) {
        Vector3f direction = Vector3f.subtract(this.target.get(), this.position.get());

        float distance = direction.length();
        direction.normalize();

        // Новое расстояние с ограничениями
        float newDistance = distance + amount;
        newDistance = Math.max(0.5f, Math.min(50.0f, newDistance));

        // Новая позиция камеры
        direction.multiplyV(newDistance);
        Vector3f newPosition = new Vector3f(this.target.get());
        newPosition.subtractV(direction);

        this.position.set(newPosition);
    }

    public void zoomTest(float amount) {
        Vector3f center = getTarget();
        Vector3f cameraPos = getPosition();

        // Вычисляем вектор от цели к камере
        Vector3f offset = cameraPos.subtract(center);
        float currentDistance = offset.length();

        if (currentDistance < 0.0001f) {
            return; // Нельзя зумить, если камера находится в центре
        }

        // Вычисляем новое расстояние с ограничениями
        float newDistance = currentDistance + amount;
        newDistance = Math.max(0.5f, Math.min(50.0f, newDistance));

        // Вычисляем коэффициент масштабирования
        float scaleFactor = newDistance / currentDistance;

        // Используем аффинные преобразования для масштабирования относительно центра
        // Порядок: перенос в начало координат -> масштабирование -> перенос обратно
        TranslationTransformation translateToOrigin = new TranslationTransformation(
                new Vector3f(-center.getX(), -center.getY(), -center.getZ())
        );
        
        // Создаем матрицу масштабирования
        Matrix4f scaleMatrix = Matrix4f.identityMatrix();
        scaleMatrix.setElement(0, 0, scaleFactor);
        scaleMatrix.setElement(1, 1, scaleFactor);
        scaleMatrix.setElement(2, 2, scaleFactor);
        
        TranslationTransformation translateBack = new TranslationTransformation(center);

        // Применяем преобразования через AffineBuilder
        AffineBuilder affineBuilder = new AffineBuilder();
        affineBuilder.addTransformation(translateToOrigin);
        // Добавляем масштабирование через матрицу
        // Для применения масштабирования создаем комбинированное преобразование
        // T(-center) * S * T(center) применяется к позиции камеры
        Vector4f position4 = new Vector4f(
                cameraPos.getX(),
                cameraPos.getY(),
                cameraPos.getZ(),
                1.0f
        );
        
        // Применяем перенос в начало координат
        Matrix4f translateToOriginMatrix = new AffineBuilder().addTransformation(translateToOrigin).build().getMatrix();
        Matrix4f.transform(translateToOriginMatrix, position4);
        
        // Применяем масштабирование
        Matrix4f.transform(scaleMatrix, position4);
        
        // Применяем перенос обратно
        Matrix4f translateBackMatrix = new AffineBuilder().addTransformation(translateBack).build().getMatrix();
        Matrix4f.transform(translateBackMatrix, position4);
        
        // Устанавливаем новую позицию
        setPosition(new Vector3f(position4.getX(), position4.getY(), position4.getZ()));
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position.get(), target.get());
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    Vector3f getDirection() {
        Vector3f result = this.target.get();
        result.subtract(this.position.get());
        return result;
    }

    @Override
    public String toString() {
        return "Camera " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Camera camera = (Camera) o;
        return Float.compare(fov, camera.fov) == 0 && Float.compare(aspectRatio, camera.aspectRatio) == 0 && Float.compare(nearPlane, camera.nearPlane) == 0 && Float.compare(farPlane, camera.farPlane) == 0 && Objects.equals(id, camera.id) && Objects.equals(position, camera.position) && Objects.equals(target, camera.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, target, fov, aspectRatio, nearPlane, farPlane);
    }

    public float getFov() {
        return fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }
}