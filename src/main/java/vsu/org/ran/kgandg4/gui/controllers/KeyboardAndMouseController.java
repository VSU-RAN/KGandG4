package vsu.org.ran.kgandg4.gui.controllers;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import math.vector.Vector3f;

import vsu.org.ran.kgandg4.camera.Camera;
import vsu.org.ran.kgandg4.camera.CameraManager;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Value;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Autowired;
import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class KeyboardAndMouseController {
    @Autowired
    private CameraManager cameraManager;

    @Autowired
    private Scene scene;

    @Value("${camera.keyboard.pan_speed:0.5}")
    private float PAN_SPEED;

    @Value("${camera.keyboard.orbit_speed:0.05}")
    private float ORBIT_SPEED;

    @Value("${camera.keyboard.zoom_speed:0.5}")
    private float ZOOM_SPEED;

    @Value("${camera.mouse.pan_speed:0.01}")
    private float MOUSE_PAN_SENSITIVITY;

    @Value("${camera.mouse.orbit_speed:0.05}")
    private float MOUSE_ORBIT_SENSITIVITY;


    @Value("${camera.mouse.zoom_speed:0.075}")
    private float MOUSE_ZOOM_SENSITIVITY;

    private double mouseX, mouseY;
    private double prevMouseX, prevMouseY;

    private boolean leftMouseButtonPressed;
    private boolean rightMouseButtonPressed;
    private boolean middleMouseButtonPressed;

    private boolean isDragging;

    private boolean shiftPressed = false;
    private boolean altPressed = false;

    public void attachToScene(Scene scene) {
        if (scene == null) return;

        //Обработчики клавиатуры
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        scene.addEventFilter(KeyEvent.KEY_RELEASED, this::handleKeyReleased);

        //Обработчики мыши
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        scene.addEventFilter(ScrollEvent.SCROLL, this::handleScroll);
    }

    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        if (code == KeyCode.SHIFT) {
            shiftPressed = true;
        }

        handleCameraMovement(code);
    }

    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.SHIFT) {
            shiftPressed = false;
        }
    }


    private void handleCameraMovement(KeyCode code) {
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
            case ALT:
                // ALT + ЛКМ
                if (altPressed) {

                } else {

                }
                break;
            case I:
                performCameraInversion(activeCamera);
                break;
        }
    }

    private void handleMousePressed(MouseEvent event) {
        // Запоминаем координаты при нажатии
        mouseX = event.getSceneX();
        mouseY = event.getSceneY();

        prevMouseX = mouseX;
        prevMouseY = mouseY;
        // Определяем, какая кнопка нажата
        leftMouseButtonPressed = event.isPrimaryButtonDown();
        rightMouseButtonPressed = event.isSecondaryButtonDown();
        middleMouseButtonPressed = event.isMiddleButtonDown();
        // Начинаем перетаскивание, если нажата любая кнопка
        isDragging = leftMouseButtonPressed || rightMouseButtonPressed ||
                middleMouseButtonPressed;
    }

    private void handleMouseDragged(MouseEvent event) {
        if (cameraManager == null || cameraManager.getActiveCamera() == null) {
            return;
        }

        Camera activeCamera = cameraManager.getActiveCamera();

        double dx = event.getSceneX() - prevMouseX;
        double dy = event.getSceneY() - prevMouseY;

        prevMouseX = event.getSceneX();
        prevMouseY = event.getSceneY();

        if (leftMouseButtonPressed) {

        } else if (rightMouseButtonPressed) {
            activeCamera.movePositionAndTarget(new Vector3f(
                    (float) dx * MOUSE_PAN_SENSITIVITY,
                    (float) -dy * MOUSE_PAN_SENSITIVITY,
                    0
            ));
        } else if (middleMouseButtonPressed) {
            float orbitX = (float) dx * MOUSE_ORBIT_SENSITIVITY;
            float orbitY = (float) dy * MOUSE_ORBIT_SENSITIVITY;
            activeCamera.orbit(orbitX, orbitY);
        }
    }

    private void performCameraInversion(Camera camera) {
        // 1. Получаем текущие позицию и цель
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();

        // 2. Вычисляем вектор от цели к камере
        Vector3f offset = Vector3f.subtract(position, target);

        // 3. Инвертируем X и Z компоненты (вращение на 180°)
        Vector3f invertedOffset = new Vector3f(
                -offset.getX(),
                -offset.getY(),
                -offset.getZ()
        );

        // 4. Вычисление новой позиции
        Vector3f newPosition = target.add(invertedOffset);

        // 5. Устанавливаем новую позицию камере
        camera.setPosition(newPosition);
    }

    private void handleMouseReleased(MouseEvent event) {
        leftMouseButtonPressed = false;
        rightMouseButtonPressed = false;
        middleMouseButtonPressed = false;
        isDragging = false;
    }

    private void handleScroll(ScrollEvent event) {
        if (cameraManager == null || cameraManager.getActiveCamera() == null) {
            return;
        }

        Camera activeCamera = cameraManager.getActiveCamera();

        double dy = event.getDeltaY();

        activeCamera.zoom((float) -dy * MOUSE_ZOOM_SENSITIVITY);
    }

    public boolean isAltPressed() {
        return altPressed;
    }

    public void setAltPressed(boolean altPressed) {
        this.altPressed = altPressed;
    }
}
