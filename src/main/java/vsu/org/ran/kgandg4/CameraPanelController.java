package vsu.org.ran.kgandg4;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;

import math.vector.Vector3f;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class CameraPanelController implements Initializable {

    @FXML private ListView<Camera> cameraListView;
    @FXML private Label activeCameraLabel;
    @FXML private Spinner<Double> camPosX, camPosY, camPosZ;
    @FXML private Spinner<Double> camTargetX, camTargetY, camTargetZ;
    @FXML private Button removeButton, switchButton;

    @Autowired
    private CameraManager cameraManager;

    private boolean isUpdatingFields = false;

    @Value("${camera.pan_speed}")
    private float PAN_SPEED;

    @Value("${camera.orbit_speed}")
    private float ORBIT_SPEED;

    @Value("${camera.zoom_speed}")
    private float ZOOM_SPEED;

    private boolean shiftPressed = false;

    private Scene scene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initListView();
        initSpinners();

        if (cameraManager != null) {
            initCameraManager();
        }
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        setupKeyboardListeners();
    }

    private void setupKeyboardListeners() {
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                KeyCode code = event.getCode();

                if (code == KeyCode.SHIFT) {
                    shiftPressed = true;
                }

                handleKeyPress(event);
            });

            scene.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
                KeyCode code = event.getCode();

                if (code == KeyCode.SHIFT) {
                    shiftPressed = false;
                }
            });
        }
    }

    private void handleKeyPress(KeyEvent event) {
        KeyCode code = event.getCode();
        if (cameraManager == null || cameraManager.getActiveCamera() == null) {
            return;
        }

        Camera activeCamera = cameraManager.getActiveCamera();

        switch (code) {
            case W:
                if (shiftPressed) {
                    // Shift + W = Pan вперед
                    activeCamera.movePositionAndTarget(new Vector3f(0, 0, PAN_SPEED));
                } else {
                    // W = Orbit вверх
                    activeCamera.orbit(0, ORBIT_SPEED);
                }
                break;

            case S:
                if (shiftPressed) {
                    // Shift + S = Pan назад
                    activeCamera.movePositionAndTarget(new Vector3f(0, 0, -PAN_SPEED));
                } else {
                    // S = Orbit вниз
                    activeCamera.orbit(0, -ORBIT_SPEED);
                }
                break;

            case A:
                if (shiftPressed) {
                    // Shift + A = Pan влево
                    activeCamera.movePositionAndTarget(new Vector3f(-PAN_SPEED, 0, 0));
                } else {
                    // A = Orbit влево
                    activeCamera.orbit(ORBIT_SPEED, 0);
                }
                break;

            case D:
                if (shiftPressed) {
                    // Shift + D = Pan вправо
                    activeCamera.movePositionAndTarget(new Vector3f(PAN_SPEED, 0, 0));
                } else {
                    // D = Orbit вправо
                    activeCamera.orbit(-ORBIT_SPEED, 0);
                }
                break;

            case UP:
                // Стрелка вверх = Pan вперед
                activeCamera.movePositionAndTarget(new Vector3f(0, 0, PAN_SPEED));
                break;

            case DOWN:
                // Стрелка вниз = Pan назад
                activeCamera.movePositionAndTarget(new Vector3f(0, 0, -PAN_SPEED));
                break;

            case LEFT:
                // Стрелка влево = Pan влево
                activeCamera.movePositionAndTarget(new Vector3f(-PAN_SPEED, 0, 0));
                break;

            case RIGHT:
                // Стрелка вправо = Pan вправо
                activeCamera.movePositionAndTarget(new Vector3f(PAN_SPEED, 0, 0));
                break;

            case Q:
                // Q = Pan вверх
                activeCamera.movePositionAndTarget(new Vector3f(0, PAN_SPEED, 0));
                break;

            case E:
                // E = Pan вниз
                activeCamera.movePositionAndTarget(new Vector3f(0, -PAN_SPEED, 0));
                break;

            case EQUALS:
                // - = Zoom In
                activeCamera.zoom(-ZOOM_SPEED);
                break;

            case MINUS:
                // + = Zoom Out
                activeCamera.zoom(ZOOM_SPEED);
                break;
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
        setupSpinner(camPosX, -1000.0, 1000.0, 0.0, 0.5);
        setupSpinner(camPosY, -1000.0, 1000.0, 0.0, 0.5);
        setupSpinner(camPosZ, -1000.0, 1000.0, 100.0, 0.5);
        setupSpinner(camTargetX, -1000.0, 1000.0, 0.0, 0.5);
        setupSpinner(camTargetY, -1000.0, 1000.0, 0.0, 0.5);
        setupSpinner(camTargetZ, -1000.0, 1000.0, 0.0, 0.5);
    }

    private void setupSpinner(Spinner<Double> spinner, double min, double max, double initial, double step) {
        SpinnerValueFactory<Double> factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step);
        spinner.setValueFactory(factory);

        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields) {
                handleApplyChanges();
            }
        });
    }

    private void updateButtonsState() {
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
    }
}