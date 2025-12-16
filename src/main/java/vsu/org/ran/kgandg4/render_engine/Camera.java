package vsu.org.ran.kgandg4.render_engine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.Objects;

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
        newPos.add(translation);
        this.position.set(newPos);

    }

    public void moveTarget(final Vector3f translation) {
        this.target.get().add(translation);
    }

    Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position.get(), target.get());
    }

    Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
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