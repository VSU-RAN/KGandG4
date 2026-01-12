package vsu.org.ran.kgandg4.render_engine;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import math.vector.Vector3f;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.PostConstruct;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;

@Component
public class CameraManager {
    private ObservableList<Camera> cameraList = FXCollections.observableArrayList();
    private int nextId = 0;
    private final ReadOnlyObjectWrapper<Camera> activeCameraProperty = new ReadOnlyObjectWrapper<>();

    private float aspectRatio;

    @Value("${camera.default.fov}")
    private float defaultFov;

    @Value("${camera.default.near}")
    private float defaultNearPlane;

    @Value("${camera.default.far}")
    private float defaultFarPlane;

    private Vector3f defaultCameraSource = new Vector3f(0, 0, 20);

    private Vector3f defaultTargetSource = new Vector3f(0, 0, 0);


    @PostConstruct
    public void init() {
        Camera initialCamera = new Camera(
                nextId ++,
                defaultCameraSource,
                defaultTargetSource,
                defaultFov,
                aspectRatio,
                defaultNearPlane,
                defaultFarPlane
        );

        this.activeCameraProperty.set(initialCamera);
        this.cameraList.add(initialCamera);
    }

    public Camera addCamera(
            final Vector3f position,
            final Vector3f target,
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane
    ) {
        Camera camera = new Camera(nextId ++, position, target, fov, aspectRatio, nearPlane, farPlane);
        this.cameraList.add(camera);
        return camera;
    }

    public void removeCamera(Camera camera) {
        if (cameraList.size() <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнюю камеру");
        }
        cameraList.remove(camera);
        if (activeCameraProperty.get() != null && activeCameraProperty.get().equals(camera)) {
            switchToCamera(0);
        }
    }

    public void removeCamera(int id) {
        if (cameraList.size() <= 1) {
            throw new IllegalArgumentException("Нельзя удалить последнюю камеру");
        }

        Camera camera = cameraList.stream().filter(cam -> cam.getId() == id).
                findFirst().orElseThrow(() -> new IllegalArgumentException("Нет камеры с таким ID: " + id));

        cameraList.remove(camera);

        if (activeCameraProperty.get() != null && activeCameraProperty.get().equals(camera)) {
            switchToCamera(0);
        }
    }

    private void switchToCamera(int index) {
        if (index >= 0 && index < cameraList.size()) {
            activeCameraProperty.set(cameraList.get(index));
        }
    }

    public void switchToCameraById(int id) {
        activeCameraProperty.set(getCameraById(id));
    }

    public void switchToNextCamera() {
        Camera activeCamera = activeCameraProperty.get();
        if (activeCamera == null || cameraList.isEmpty()) return;

        int curIndex = cameraList.indexOf(activeCamera);
        int nextIndex = (curIndex + 1) % cameraList.size();
        switchToCamera(nextIndex);
    }

    public ObjectProperty<Camera> activeCameraProperty() {
        return activeCameraProperty;
    }

    public Camera getActiveCamera() {
        return activeCameraProperty.get();
    }

    public Camera getCameraById(int id) {
        return cameraList.stream().filter(cam -> cam.getId() == id).
                findFirst().orElseThrow(() -> new IllegalArgumentException("Нет камеры с таким ID: " + id));
    }

    public ObservableList<Camera> getCameras() {
        return FXCollections.unmodifiableObservableList(this.cameraList);
    }
}
