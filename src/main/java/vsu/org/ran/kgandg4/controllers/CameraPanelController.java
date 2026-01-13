package vsu.org.ran.kgandg4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import vsu.org.ran.kgandg4.render_engine.Camera;
import vsu.org.ran.kgandg4.render_engine.CameraManager;

import javax.vecmath.Vector3f;
import java.net.URL;
import java.util.ResourceBundle;

public class CameraPanelController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox cameraPanel;
    @FXML private ListView<Camera> cameraListView;
    @FXML private Label activeCameraLabel;
    @FXML private Spinner<Double> camPosX, camPosY, camPosZ;
    @FXML private Spinner<Double> camTargetX, camTargetY, camTargetZ;
    @FXML private Button removeButton, switchButton, addButton, nextButton;
    @FXML private Spinner<Double> camFovSpinner;

    private CameraManager cameraManager;
    private boolean isUpdatingFields = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initListView();
        initSpinners();
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;

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

    public Parent getCameraPanel() {
        return scrollPane;
    }

    private void setupCameraListeners(Camera camera) {
        camera.positionProperty().addListener((obs, oldPos, newPos) -> {
            if (!isUpdatingFields && newPos != null) {
                isUpdatingFields = true;
                camPosX.getValueFactory().setValue((double) newPos.x);
                camPosY.getValueFactory().setValue((double) newPos.y);
                camPosZ.getValueFactory().setValue((double) newPos.z);
                isUpdatingFields = false;
            }
        });

        camera.targetProperty().addListener((obs, oldTarget, newTarget) -> {
            if (!isUpdatingFields && newTarget != null) {
                isUpdatingFields = true;
                camTargetX.getValueFactory().setValue((double) newTarget.x);
                camTargetY.getValueFactory().setValue((double) newTarget.y);
                camTargetZ.getValueFactory().setValue((double) newTarget.z);
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
    }

    private void initListView() {
        cameraListView.setCellFactory(lv -> new ListCell<Camera>() {
            @Override
            protected void updateItem(Camera camera, boolean empty) {
                super.updateItem(camera, empty);
                setText(empty || camera == null ? null : camera.toString());
            }
        });

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

        setupSpinner(camFovSpinner, 10.0, 120.0, 45.0, 1.0);
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

        camPosX.getValueFactory().setValue((double)camera.getPosition().x);
        camPosY.getValueFactory().setValue((double)camera.getPosition().y);
        camPosZ.getValueFactory().setValue((double)camera.getPosition().z);

        camTargetX.getValueFactory().setValue((double)camera.getTarget().x);
        camTargetY.getValueFactory().setValue((double)camera.getTarget().y);
        camTargetZ.getValueFactory().setValue((double)camera.getTarget().z);

        camFovSpinner.getValueFactory().setValue((double)camera.getFov());

        isUpdatingFields = false;
    }

    @FXML
    private void onAddCameraClick() {
        if (cameraManager == null) return;

        Camera active = cameraManager.getActiveCamera();

        Camera newCamera = cameraManager.addCamera(
                new Vector3f(
                        active.getPosition().x + 50,
                        active.getPosition().y,
                        active.getPosition().z),
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
    }
}