package vsu.org.ran.kgandg4.render_engine;

import math.vector.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.org.ran.kgandg4.camera.Camera;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {
    private Camera camera;

    @BeforeEach
    void setUp() {
        camera = new Camera(
                0,
                new Vector3f(0, 0, 20),
                new Vector3f(0, 0, 0),
                1.0f,
                1.0f,
                0.01f,
                800.0f
        );
    }

    @Test
    void testOrbitTest() {
        Vector3f initialPosition = camera.getPosition();
        Vector3f center = camera.getTarget();
        Vector3f initialOffset = initialPosition.subtract(center);
        float initialDistance = initialOffset.length();

        // Вращаем камеру вокруг цели
        camera.orbitTest(0.1f, 0.1f);

        Vector3f newPosition = camera.getPosition();
        Vector3f newOffset = newPosition.subtract(center);
        float newDistance = newOffset.length();

        // Проверяем, что расстояние до цели сохранилось
        assertEquals(initialDistance, newDistance, 0.01f);

        // Проверяем, что позиция изменилась
        assertNotEquals(initialPosition.getX(), newPosition.getX(), 0.001f);
        assertNotEquals(initialPosition.getY(), newPosition.getY(), 0.001f);
        assertNotEquals(initialPosition.getZ(), newPosition.getZ(), 0.001f);
    }

    @Test
    void testOrbitTestPreservesDistance() {
        Vector3f center = camera.getTarget();
        Vector3f initialPosition = camera.getPosition();
        Vector3f initialOffset = initialPosition.subtract(center);
        float initialDistance = initialOffset.length();

        // Выполняем несколько вращений
        camera.orbitTest(0.5f, 0.3f);
        camera.orbitTest(-0.2f, 0.1f);
        camera.orbitTest(0.1f, -0.2f);

        Vector3f finalPosition = camera.getPosition();
        Vector3f finalOffset = finalPosition.subtract(center);
        float finalDistance = finalOffset.length();

        // Расстояние должно сохраняться
        assertEquals(initialDistance, finalDistance, 0.01f);
    }

    @Test
    void testZoomTest() {
        Vector3f center = camera.getTarget();
        Vector3f initialPosition = camera.getPosition();
        Vector3f initialOffset = initialPosition.subtract(center);
        float initialDistance = initialOffset.length();

        // Увеличиваем расстояние (зум out)
        camera.zoomTest(5.0f);

        Vector3f newPosition = camera.getPosition();
        Vector3f newOffset = newPosition.subtract(center);
        float newDistance = newOffset.length();

        // Проверяем, что расстояние увеличилось
        assertTrue(newDistance > initialDistance);

        // Проверяем, что направление сохранилось (offset должен быть коллинеарен)
        Vector3f normalizedInitial = new Vector3f(initialOffset);
        normalizedInitial.normalize();
        Vector3f normalizedNew = new Vector3f(newOffset);
        normalizedNew.normalize();

        float dotProduct = normalizedInitial.dotProduct(normalizedNew);
        assertTrue(dotProduct > 0.99f); // Почти параллельны
    }

    @Test
    void testZoomTestZoomIn() {
        Vector3f center = camera.getTarget();
        Vector3f initialPosition = camera.getPosition();
        Vector3f initialOffset = initialPosition.subtract(center);
        float initialDistance = initialOffset.length();

        // Уменьшаем расстояние (зум in)
        camera.zoomTest(-5.0f);

        Vector3f newPosition = camera.getPosition();
        Vector3f newOffset = newPosition.subtract(center);
        float newDistance = newOffset.length();

        // Проверяем, что расстояние уменьшилось
        assertTrue(newDistance < initialDistance);
    }

    @Test
    void testZoomTestLimits() {
        Vector3f center = camera.getTarget();

        // Пытаемся зумить слишком далеко
        camera.zoomTest(1000.0f);

        Vector3f position = camera.getPosition();
        Vector3f offset = position.subtract(center);
        float distance = offset.length();

        // Расстояние должно быть ограничено максимумом (50.0f)
        assertTrue(distance <= 50.0f);

        // Пытаемся зумить слишком близко
        camera.zoomTest(-1000.0f);

        position = camera.getPosition();
        offset = position.subtract(center);
        distance = offset.length();

        // Расстояние должно быть ограничено минимумом (0.5f)
        assertTrue(distance >= 0.5f);
    }

    @Test
    void testOrbitTestZeroAngles() {
        Vector3f initialPosition = camera.getPosition();
        Vector3f center = camera.getTarget();

        // Вращаем на нулевые углы
        camera.orbitTest(0.0f, 0.0f);

        Vector3f newPosition = camera.getPosition();

        // Позиция не должна измениться
        assertEquals(initialPosition.getX(), newPosition.getX(), 0.001f);
        assertEquals(initialPosition.getY(), newPosition.getY(), 0.001f);
        assertEquals(initialPosition.getZ(), newPosition.getZ(), 0.001f);
    }
}
