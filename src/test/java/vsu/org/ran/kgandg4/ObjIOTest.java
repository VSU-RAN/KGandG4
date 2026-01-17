package vsu.org.ran.kgandg4;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import math.vector.Vector2f;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.IO.objReader.ObjReader;
import vsu.org.ran.kgandg4.IO.ObjWriter;

public class ObjIOTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadWriteTriangle() throws IOException {
        Model model = new Model();
        model.getVertices().add(new Vector3f(0, 0, 0));
        model.getVertices().add(new Vector3f(1, 0, 0));
        model.getVertices().add(new Vector3f(0, 1, 0));

        Polygon triangle = new Polygon();
        triangle.setVertexIndices(Arrays.asList(0, 1, 2));
        model.getPolygons().add(triangle);

        Path filePath = tempDir.resolve("test_triangle.obj");
        ObjWriter.write(model, filePath);

        String content = Files.readString(filePath);
        Model readModel = ObjReader.read(content);

        assertEquals(3, readModel.getVertices().size());
        assertEquals(1, readModel.getPolygons().size());
    }

    @Test
    void testReadWriteWithTextureAndNormals() throws IOException {
        Model model = new Model();

        model.getVertices().add(new Vector3f(-1, -1, 0));
        model.getVertices().add(new Vector3f(1, -1, 0));
        model.getVertices().add(new Vector3f(0, 1, 0));

        model.getTextureVertices().add(new Vector2f(0, 0));
        model.getTextureVertices().add(new Vector2f(1, 0));
        model.getTextureVertices().add(new Vector2f(0.5f, 1));

        model.getNormals().add(new Vector3f(0, 0, 1));

        Polygon poly = new Polygon();
        poly.setVertexIndices(Arrays.asList(0, 1, 2));
        poly.setTextureVertexIndices(Arrays.asList(0, 1, 2));
        poly.setNormalIndices(Arrays.asList(0, 0, 0));
        model.getPolygons().add(poly);

        Path filePath = tempDir.resolve("test_full.obj");
        ObjWriter.write(model, filePath);

        String content = Files.readString(filePath);
        Model readModel = ObjReader.read(content);

        assertEquals(3, readModel.getVertices().size());
        assertEquals(3, readModel.getTextureVertices().size());
        assertEquals(1, readModel.getNormals().size());
        assertEquals(1, readModel.getPolygons().size());
    }

    @Test
    void testReadWriteMixedPolygon() throws IOException {
        Model model = new Model();

        // 4 вершины для квадрата
        model.getVertices().add(new Vector3f(0, 0, 0));
        model.getVertices().add(new Vector3f(1, 0, 0));
        model.getVertices().add(new Vector3f(1, 1, 0));
        model.getVertices().add(new Vector3f(0, 1, 0));

        // 2 текстурные координаты (для двух треугольников)
        model.getTextureVertices().add(new Vector2f(0, 0));
        model.getTextureVertices().add(new Vector2f(1, 0));
        model.getTextureVertices().add(new Vector2f(1, 1));

        // 1 нормаль
        model.getNormals().add(new Vector3f(0, 0, 1));

        // Квадрат из двух треугольников
        Polygon tri1 = new Polygon();
        tri1.setVertexIndices(Arrays.asList(0, 1, 2));
        tri1.setTextureVertexIndices(Arrays.asList(0, 1, 2));
        tri1.setNormalIndices(Arrays.asList(0, 0, 0));
        model.getPolygons().add(tri1);

        Polygon tri2 = new Polygon();
        tri2.setVertexIndices(Arrays.asList(0, 2, 3));
        tri2.setTextureVertexIndices(Arrays.asList(0, 2, 2)); // Повтор текстуры
        tri2.setNormalIndices(Arrays.asList(0, 0, 0));
        model.getPolygons().add(tri2);

        Path filePath = tempDir.resolve("test_mixed.obj");
        ObjWriter.write(model, filePath);

        String content = Files.readString(filePath);
        Model readModel = ObjReader.read(content);

        assertEquals(4, readModel.getVertices().size());
        assertEquals(3, readModel.getTextureVertices().size());
        assertEquals(1, readModel.getNormals().size());
        assertEquals(2, readModel.getPolygons().size());
    }

    @Test
    void testErrorHandling() {
        Model emptyModel = new Model();
        assertFalse(ObjWriter.canWrite(emptyModel));

        assertFalse(ObjWriter.canWrite(null));

        Model verticesOnly = new Model();
        verticesOnly.getVertices().add(new Vector3f(0, 0, 0));
        assertTrue(ObjWriter.canWrite(verticesOnly));
    }

    @Test
    void testReadFileWithComments() {
        String objContent = """
            # This is a comment
            v 0.0 0.0 0.0
            # Another comment
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3 # Face comment
            """;

        Model model = ObjReader.read(objContent);

        assertEquals(3, model.getVertices().size());
        assertEquals(1, model.getPolygons().size());
    }

    @Test
    void testReadFileWithRelativeIndices() {
        String objContent = """
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 1.0 1.0 0.0
            f 1 2 -1 -2  # Относительные индексы: -1 = 4, -2 = 3
            """;

        Model model = ObjReader.read(objContent);

        assertEquals(4, model.getVertices().size());
        assertEquals(1, model.getPolygons().size());

        List<Integer> vertexIndices = model.getPolygons().get(0).getVertexIndices();
        // Преобразованные индексы: 0, 1, 3, 2 (0-based)
        assertEquals(Arrays.asList(0, 1, 3, 2), vertexIndices);
    }

    @Test
    void testBasicWriteToFile() throws IOException {
        Model model = new Model();
        model.getVertices().add(new Vector3f(0, 0, 0));
        model.getVertices().add(new Vector3f(1, 0, 0));
        model.getVertices().add(new Vector3f(0, 1, 0));

        Polygon triangle = new Polygon();
        triangle.setVertexIndices(Arrays.asList(0, 1, 2));
        model.getPolygons().add(triangle);

        Path filePath = tempDir.resolve("test.obj");

        // Тестируем метод writeToFile (нестатический)
        ObjWriter writer = new ObjWriter();
        writer.writeToFile(model, filePath.toString());

        // Проверяем, что файл создан
        assertTrue(Files.exists(filePath));

        String content = Files.readString(filePath);
        assertTrue(content.contains("v "));
        assertTrue(content.contains("f "));
    }
}