package vsu.org.ran.kgandg4.render_engine;

import javafx.beans.property.*;
import javafx.beans.property.SimpleFloatProperty;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Objects;

public class Camera {
    private final int id;
    private final ObjectProperty<Vector3f> position = new SimpleObjectProperty<>();
    private final ObjectProperty<Vector3f> target = new SimpleObjectProperty<>();

    // ЗАМЕНИТЬ float fov на FloatProperty
    private final FloatProperty fov = new SimpleFloatProperty();
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
        this.fov.set(fov);  // ИСПОЛЬЗОВАТЬ set()
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    // ДОБАВИТЬ сеттер для FOV
    public void setFov(final float fov) {
        this.fov.set(fov);
    }

    // ДОБАВИТЬ геттер для FOV (возвращает значение из FloatProperty)
    public float getFov() {
        return fov.get();
    }

    // ДОБАВИТЬ свойство для FOV (чтобы можно было отслеживать изменения)
    public FloatProperty fovProperty() {
        return fov;
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
        newPos.add(translation);
        this.position.set(newPos);
    }

    public void moveTarget(final Vector3f translation) {
        this.target.get().add(translation);
    }

    /** Паномиривание */
    public void movePositionAndTarget(Vector3f translation) {
        Vector3f newPos = new Vector3f(this.position.get());
        newPos.add(translation);
        this.position.set(newPos);

        Vector3f newTarget = new Vector3f(this.target.get());
        newTarget.add(translation);
        this.target.set(newTarget);
    }

    /**Орбитальное вращение вокруг target*/
    public void orbit(float horizontalAngle, float verticalAngle) {
        Vector3f center = this.target.get();
        Vector3f cameraPos = this.position.get();

        // Вектор от target к камере
        Vector3f offset = new Vector3f();
        offset.sub(cameraPos, center);

        // Сохраняем расстояние
        float distance = offset.length();

        // Переводим в сферические координаты
        float radius = distance;
        float theta = (float) Math.atan2(offset.x, offset.z); // горизонтальный угол
        float phi = (float) Math.acos(offset.y / radius);     // вертикальный угол

        // Добавляем новые углы
        theta += horizontalAngle;
        phi += verticalAngle;

        // Ограничиваем вертикальный угол
        phi = Math.max(0.1f, Math.min((float)Math.PI - 0.1f, phi));

        // Конвертируем обратно в декартовы координаты
        float x = radius * (float)Math.sin(phi) * (float)Math.sin(theta);
        float y = radius * (float)Math.cos(phi);
        float z = radius * (float)Math.sin(phi) * (float)Math.cos(theta);

        // Новая позиция камеры
        Vector3f newPosition = new Vector3f(x, y, z);
        newPosition.add(center);

        this.position.set(newPosition);
    }

    /** Зум */
    public void zoom(float amount) {
        Vector3f direction = new Vector3f();
        direction.sub(this.target.get(), this.position.get());

        float distance = direction.length();
        direction.normalize();

        // Новое расстояние с ограничениями
        float newDistance = distance + amount;
        newDistance = Math.max(0.5f, Math.min(50.0f, newDistance));

        // Новая позиция камеры
        direction.scale(newDistance);
        Vector3f newPosition = new Vector3f(this.target.get());
        newPosition.sub(direction);

        this.position.set(newPosition);
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position.get(), target.get());
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov.get(), aspectRatio, nearPlane, farPlane);
    }

    Vector3f getDirection() {
        Vector3f result = this.target.get();
        result.sub(this.position.get());
        return result;
    }

    // Обновить equals() и hashCode() для учета FloatProperty
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Camera camera = (Camera) o;
        return id == camera.id &&
                Float.compare(camera.fov.get(), fov.get()) == 0 &&
                Float.compare(camera.aspectRatio, aspectRatio) == 0 &&
                Float.compare(camera.nearPlane, nearPlane) == 0 &&
                Float.compare(camera.farPlane, farPlane) == 0 &&
                Objects.equals(position.get(), camera.position.get()) &&
                Objects.equals(target.get(), camera.target.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position.get(), target.get(), fov.get(), aspectRatio, nearPlane, farPlane);
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

    @Override
    public String toString() {
        return "Camera " + id + " (FOV: " + String.format("%.1f", fov.get()) + "°)";
    }
}