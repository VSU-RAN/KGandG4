package vsu.org.ran.kgandg4;

import org.junit.jupiter.api.Test;
import vsu.org.ran.kgandg4.IO.objReader.ObjReader;
import vsu.org.ran.kgandg4.IO.objReader.ObjReaderException;

import static org.junit.jupiter.api.Assertions.*;

class ObjReaderTest {

    @Test
    void testEmptyFile() {
        String content = "";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("File content is empty"));
    }

    @Test
    void testValidVertex() {
        String content = "v 1.0 2.0 3.0";
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testInvalidVertexFormat() {
        String content = "v 1.0 abc 3.0";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Failed to parse float value"));
    }

    @Test
    void testInsufficientVertexComponents() {
        String content = "v 1.0 2.0";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Too few vertex arguments"));
    }

    @Test
    void testValidTextureVertex() {
        String content = "vt 0.5 0.5";
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testInsufficientTextureComponents() {
        String content = "vt 0.5";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Too few texture vertex arguments"));
    }

    @Test
    void testValidNormal() {
        String content = "vn 0.0 0.0 1.0";
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testInsufficientNormalComponents() {
        String content = "vn 0.0 1.0";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Too few normal arguments"));
    }

    @Test
    void testValidFaceWithOnlyVertices() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            f 1 2 3
            """;
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testValidFaceWithTexture() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 0.0 1.0
            f 1/1 2/2 3/3
            """;
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testValidFaceWithTextureAndNormal() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 0.0 1.0
            vn 0.0 0.0 1.0
            vn 0.0 1.0 0.0
            vn 1.0 0.0 0.0
            f 1/1/1 2/2/2 3/3/3
            """;
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testValidFaceWithVertexAndNormal() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            vn 0.0 0.0 1.0
            vn 0.0 1.0 0.0
            vn 1.0 0.0 0.0
            f 1//1 2//2 3//3
            """;
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }

    @Test
    void testFaceWithTooFewVertices() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Too few vertices in face"));
    }

    @Test
    void testFaceWithOutOfBoundsVertexIndex() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            f 1 2 3
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        // Проверяем, что сообщение содержит информацию о выходе за границы
        assertTrue(exception.getMessage().contains("out of bounds") ||
                exception.getMessage().contains("Error parsing OBJ file"));
    }

    @Test
    void testFaceWithZeroIndex() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            f 0 1 2
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("cannot be zero"));
    }

    @Test
    void testFaceWithNegativeIndex() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            f -1 2 3
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Relative indices") ||
                exception.getMessage().contains("Failed to parse"));
    }

    @Test
    void testInconsistentFaceFormat() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            vt 0.0 0.0
            vt 1.0 0.0
            vt 0.0 1.0
            f 1/1 2 3/3
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("Inconsistent face format"));
    }

    @Test
    void testPolygonWithDuplicateVertices() {
        String content = """
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            f 1 1 2
            """;
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("duplicate vertices"));
    }

    @Test
    void testVertexWithNaN() {
        String content = "v 1.0 NaN 3.0";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("NaN"));
    }

    @Test
    void testVertexWithInfinity() {
        String content = "v 1.0 Infinity 3.0";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("infinite"));
    }

    @Test
    void testModelWithoutVerticesButWithFace() {
        String content = "f 1 2 3";
        Exception exception = assertThrows(ObjReaderException.class, () -> {
            ObjReader.read(content);
        });
        assertTrue(exception.getMessage().contains("no vertices") ||
                exception.getMessage().contains("contains polygons but no vertices"));
    }

    @Test
    void testCompleteModel() {
        String content = """
            # Simple pyramid
            v 0.0 0.0 0.0
            v 1.0 0.0 0.0
            v 0.0 1.0 0.0
            v 0.0 0.0 1.0
            v 0.5 0.5 0.5
            
            f 1 2 3
            f 1 3 4
            f 1 4 2
            f 2 4 3
            f 2 3 4 5
            """;
        assertDoesNotThrow(() -> {
            ObjReader.read(content);
        });
    }
}