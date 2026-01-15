package vsu.org.ran.kgandg4.gui.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.Initializable;

import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.gui.PanelController;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class CameraPanelController implements Initializable, PanelController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox cameraPanel;
    @FXML private ListView<Camera> cameraListView;
    @FXML private Label activeCameraLabel;
    @FXML private Spinner<Double> camPosX, camPosY, camPosZ;
    @FXML private Spinner<Double> camTargetX, camTargetY, camTargetZ;
    @FXML private Button removeButton, switchButton, addButton, nextButton;
    @FXML private Spinner<Double> camFovSpinner, camNearSpinner, camFarSpinner;

    @Autowired
    private CameraManager cameraManager;

    @Value("${camera.keyboard.pan_speed:0.5}")
    private float PAN_SPEED;

    @Value("${camera.keyboard.orbit_speed:0.05}")
    private float ORBIT_SPEED;

    @Value("${camera.keyboard.zoom_speed:0.5}")
    private float ZOOM_SPEED;

    private boolean isUpdatingFields = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initListView();
        initSpinners();
        if (cameraManager != null) {
            initCameraManager();
        }
    }

    public void initCameraManager() {
        cameraListView.setItems(cameraManager.getCameras());

        cameraManager.activeCameraProperty().addListener((obs, oldCam, newCam) -> {
            if (newCam != null) {
                updateCameraFields(newCam);
                activeCameraLabel.setText(newCam.toString());
                cameraListView.getSelectionModel().select(newCam);
                updateButtonsState();
                setupCameraListeners(newCam);
            }
        });

        // Начальная инициализация активной камеры гуи
        Camera active = cameraManager.getActiveCamera();
        if (active != null) {
            updateCameraFields(active);
            activeCameraLabel.setText(active.toString());
            cameraListView.getSelectionModel().select(active);
            updateButtonsState();
            setupCameraListeners(active);
        }
    }

    private void setupCameraListeners(Camera camera) {
        camera.positionProperty().addListener((obs, oldPos, newPos) -> {
            if (!isUpdatingFields && newPos != null) {
                isUpdatingFields = true;
                camPosX.getValueFactory().setValue((double) newPos.getX());
                camPosY.getValueFactory().setValue((double) newPos.getY());
                camPosZ.getValueFactory().setValue((double) newPos.getZ());
                isUpdatingFields = false;
            }
        });

        camera.targetProperty().addListener((obs, oldTarget, newTarget) -> {
            if (!isUpdatingFields && newTarget != null) {
                isUpdatingFields = true;
                camTargetX.getValueFactory().setValue((double) newTarget.getX());
                camTargetY.getValueFactory().setValue((double) newTarget.getY());
                camTargetZ.getValueFactory().setValue((double) newTarget.getZ());
                isUpdatingFields = false;
            }
        });

        camera.fovProperty().addListener((obs, oldFov, newFov) -> {
            if (!isUpdatingFields && newFov != null) {
                isUpdatingFields = true;
                camFovSpinner.getValueFactory().setValue((double) newFov.floatValue());
                isUpdatingFields = false;
            }
        });

        camera.nearPlaneProperty().addListener((obs, oldNear, newNear) -> {
            if (!isUpdatingFields && newNear != null) {
                isUpdatingFields = true;
                camNearSpinner.getValueFactory().setValue((double) newNear.floatValue());
                isUpdatingFields = false;
            }
        });

        camera.farPlaneProperty().addListener((obs, oldFar, newFar) -> {
            if (!isUpdatingFields && newFar != null) {
                isUpdatingFields = true;
                camFarSpinner.getValueFactory().setValue((double) newFar.floatValue());
                isUpdatingFields = false;
            }
        });
    }

    //Задаем отображение списка камер, а также для этого списка задаем слушатель на изменение камеры, который будет обновлять поля активной камеры в гуи
    private void initListView() {
        cameraListView.setCellFactory(lv -> new ListCell<Camera>() {
            @Override
            protected void updateItem(Camera camera, boolean empty) {
                super.updateItem(camera, empty);
                setText(empty || camera == null ? null : camera.toString());
            }
        });

        // При выборе камеры обновляем поля
        cameraListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newCam) -> {
                    if (newCam != null) {
                        updateCameraFields(newCam);
                        updateButtonsState();
                    }
                }
        );
    }

    private void initSpinners() {
        setupSpinner(camPosX, -1000.0, 1000.0, cameraManager.getDefaultCameraSourceX(), PAN_SPEED);
        setupSpinner(camPosY, -1000.0, 1000.0, cameraManager.getDefaultCameraSourceY(), PAN_SPEED);
        setupSpinner(camPosZ, -1000.0, 1000.0, cameraManager.getDefaultCameraSourceZ(), PAN_SPEED);
        setupSpinner(camTargetX, -1000.0, 1000.0, cameraManager.getDefaultCameraTargetX(), PAN_SPEED);
        setupSpinner(camTargetY, -1000.0, 1000.0, cameraManager.getDefaultCameraTargetY(), PAN_SPEED);
        setupSpinner(camTargetZ, -1000.0, 1000.0, cameraManager.getDefaultCameraTargetZ(), PAN_SPEED);

        setupSpinner(camFovSpinner, 10.0, 120.0, cameraManager.getDefaultFov(), 1.0);
        setupSpinner(camNearSpinner, 0.01, 100.0, cameraManager.getDefaultNearPlane(), 0.1);
        setupSpinner(camFarSpinner, 1.0, 10000.0, cameraManager.getDefaultFarPlane(), 10.0);
    }

    private void setupSpinner(Spinner<Double> spinner, double min, double max, double initial, double step) {
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step);
        spinner.setValueFactory(factory);

        spinner.setEditable(true);

        TextField editor = spinner.getEditor();
        editor.setOnAction(event -> {
            try {
                String text = editor.getText();
                if (text != null && !text.trim().isEmpty()) {
                    double value = Double.parseDouble(text);
                    if (value < min) value = min;
                    if (value > max) value = max;
                    factory.setValue(value);
                    handleApplyChanges();
                }
            } catch (NumberFormatException e) {
                editor.setText(String.valueOf(factory.getValue()));
            }
        });

        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields) {
                handleApplyChanges();
            }
        });
    }

    public void updateButtonsState() {
        Camera selected = cameraListView.getSelectionModel().getSelectedItem();

        if (selected == null || cameraManager == null) {
            switchButton.setDisable(true);
            removeButton.setDisable(true);
        } else {
            boolean isActive = selected.equals(cameraManager.getActiveCamera());
            switchButton.setDisable(isActive);

            boolean canRemove = cameraManager.getCameras().size() > 1;
            removeButton.setDisable(!canRemove);
        }
    }

    public void updateCameraFields(Camera camera) {
        if (camera == null || isUpdatingFields) return;
        isUpdatingFields = true;

        camPosX.getValueFactory().setValue((double)camera.getPosition().getX());
        camPosY.getValueFactory().setValue((double)camera.getPosition().getY());
        camPosZ.getValueFactory().setValue((double)camera.getPosition().getZ());

        camTargetX.getValueFactory().setValue((double)camera.getTarget().getX());
        camTargetY.getValueFactory().setValue((double)camera.getTarget().getY());
        camTargetZ.getValueFactory().setValue((double)camera.getTarget().getZ());

        camFovSpinner.getValueFactory().setValue((double)camera.getFov());
        camFarSpinner.getValueFactory().setValue((double)camera.getFarPlane());
        camNearSpinner.getValueFactory().setValue((double)camera.getNearPlane());

        isUpdatingFields = false;
    }

    // ===== Обработчики кнопок =====

    @FXML
    private void onAddCameraClick() {
        if (cameraManager == null) return;

        Camera active = cameraManager.getActiveCamera();

        Camera newCamera = cameraManager.addCamera(
            new Vector3f(
                    active.getPosition().getX() + 50,
                    active.getPosition().getY(),
                    active.getPosition().getZ()),
            new Vector3f(active.getTarget()),
            active.getFov(),
            active.getAspectRatio(),
            active.getNearPlane(),
            active.getFarPlane()
        );
        cameraListView.getSelectionModel().select(newCamera);
    }

    @FXML
    private void onRemoveCameraClick() {
        Camera selected = cameraListView.getSelectionModel().getSelectedItem();
        if (selected != null && cameraManager != null) {
            try {
                cameraManager.removeCamera(selected);
            } catch (IllegalArgumentException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                alert.setTitle("Ошибка");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void onSwitchToSelectedCameraClick() {
        Camera selected = cameraListView.getSelectionModel().getSelectedItem();
        if (selected != null && cameraManager != null) {
            cameraManager.switchToCameraById(selected.getId());
        }
    }

    @FXML
    private void onNextCameraClick() {
        if (cameraManager != null) {
            cameraManager.switchToNextCamera();
        }
    }

    public void handleApplyChanges() {
        if (isUpdatingFields || cameraManager == null || cameraManager.getActiveCamera() == null) return;
        Camera active = cameraManager.getActiveCamera();

        float posX = camPosX.getValue().floatValue();
        float posY = camPosY.getValue().floatValue();
        float posZ = camPosZ.getValue().floatValue();
        active.setPosition(new Vector3f(posX, posY, posZ));

        float targetX = camTargetX.getValue().floatValue();
        float targetY = camTargetY.getValue().floatValue();
        float targetZ = camTargetZ.getValue().floatValue();
        active.setTarget(new Vector3f(targetX, targetY, targetZ));

        float fov = camFovSpinner.getValue().floatValue();
        active.setFov(fov);

        float near = camNearSpinner.getValue().floatValue();
        active.setNear(near);

        float far = camFarSpinner.getValue().floatValue();
        active.setFar(far);
    }



    @Override
    public void onPanelShow() {
        refreshCameraInfo();
    }

    private void refreshCameraInfo() {
        if (cameraManager != null) {
            Camera activeCamera = cameraManager.getActiveCamera();
            if (activeCamera != null) {
                updateCameraFields(activeCamera);
                activeCameraLabel.setText(activeCamera.toString());
                cameraListView.getSelectionModel().select(activeCamera);
                updateButtonsState();

                setupCameraListeners(activeCamera);
            }

            cameraListView.refresh();
        }
    }
}