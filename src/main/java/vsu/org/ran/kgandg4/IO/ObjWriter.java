package vsu.org.ran.kgandg4.IO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import math.vector.Vector2f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

public class ObjWriter {

    private static final String OBJ_EXTENSION = ".obj";
    private static final String COMMENT_PREFIX = "# ";
    private static final String VERTEX_PREFIX = "v ";
    private static final String TEXTURE_PREFIX = "vt ";
    private static final String NORMAL_PREFIX = "vn ";
    private static final String FACE_PREFIX = "f ";
    private static final String FLOAT_FORMAT = "%.6f";

    public static void write(Model model, Path filePath) throws IOException {
        new ObjWriter().writeToFile(model, filePath.toString());
    }

    public void writeToFile(Model model, String fileName) throws IOException {
        try {
            validateInputParameters(model, fileName);
            String normalizedFileName = normalizeFileName(fileName);
            createDirectoriesIfNeeded(normalizedFileName);
            writeModelToFile(model, normalizedFileName);

        } catch (IllegalArgumentException e) {
            throw new IOException("Некорректные параметры: " + e.getMessage(), e);

        } catch (IOException e) {
            throw new IOException("Ошибка ввода-вывода: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new IOException("Непредвиденная ошибка при записи модели: " + e.getMessage(), e);
        }
    }

    private void validateInputParameters(Model model, String fileName) {
        if (model == null) {
            throw new IllegalArgumentException("Модель не может быть null");
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя файла не может быть пустым");
        }
    }

    private String normalizeFileName(String fileName) {
        String trimmedName = fileName.trim(); //удаление пробелов
        if (!trimmedName.toLowerCase().endsWith(OBJ_EXTENSION)) {
            System.out.println("Информация: добавляем расширение .obj к имени файла");
            return trimmedName + OBJ_EXTENSION;
        }
        return trimmedName;
    }

    private void createDirectoriesIfNeeded(String fileName) throws IOException {
        Path filePath = Paths.get(fileName); // строку в path
        Path parentDir = filePath.getParent();

        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
                System.out.println("Создана директория: " + parentDir.toAbsolutePath());

            } catch (IOException e) {
                throw new IOException("Не удалось создать директорию: " + parentDir, e);
            }
        }
    }

    private void writeModelToFile(Model model, String fileName) throws IOException {
        validateModel(model);

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            writeHeader(model, writer);
            writeVertices(model, writer);
            writeTextureVertices(model, writer);
            writeNormals(model, writer);
            writePolygons(model, writer);

            System.out.println("Модель успешно записана в файл: " + fileName);

        } catch (IOException e) {
            throw new IOException("Ошибка при записи в файл '" + fileName + "'", e);
        }
    }

    private void validateModel(Model model) {
        if (model.getVertices() == null || model.getVertices().isEmpty()) {
            throw new IllegalArgumentException("Модель не содержит вершин");
        }

        validatePolygons(model);
    }

    private void validatePolygons(Model model) {
        List<Polygon> polygons = model.getPolygons(); //список
        if (polygons == null || polygons.isEmpty()) {
            return; // Модель без полигонов допустима
        }

        for (int i = 0; i < polygons.size(); i++) { //перебор
            Polygon polygon = polygons.get(i);
            int polygonNumber = i + 1;

            validatePolygon(polygon, polygonNumber, model);
        }
    }

    private void validatePolygon(Polygon polygon, int polygonNumber, Model model) {
        if (polygon == null) {
            throw new IllegalArgumentException("Полигон " + polygonNumber + " равен null");
        }

        List<Integer> vertexIndices = polygon.getVertexIndices();
        validateVertexIndices(vertexIndices, polygonNumber, model.getVertices().size());

        List<Integer> textureIndices = polygon.getTextureVertexIndices();
        if (textureIndices != null && !textureIndices.isEmpty()) {
            validateTextureIndices(textureIndices, vertexIndices, polygonNumber, model.getTextureVertices().size());
        }

        List<Integer> normalIndices = polygon.getNormalIndices();
        if (normalIndices != null && !normalIndices.isEmpty()) {
            validateNormalIndices(normalIndices, vertexIndices, polygonNumber, model.getNormals().size());
        }
    }

    private void validateVertexIndices(List<Integer> vertexIndices, int polygonNumber, int totalVertices) {
        if (vertexIndices == null || vertexIndices.isEmpty()) {
            throw new IllegalArgumentException("Полигон " + polygonNumber + " не содержит индексов вершин");
        }

        if (vertexIndices.size() < 3) {
            throw new IllegalArgumentException("Полигон " + polygonNumber + " имеет только " +
                    vertexIndices.size() + " вершины (нужно минимум 3)");
        }

        for (int j = 0; j < vertexIndices.size(); j++) { //проверка каждого индекса
            int idx = vertexIndices.get(j);
            if (idx < 0 || idx >= totalVertices) {
                throw new IllegalArgumentException("Полигон " + polygonNumber + ", вершина #" + (j + 1) +
                        ": индекс " + idx + " выходит за пределы [0, " + (totalVertices - 1) + "]");
            }
        }
    }

    private void validateTextureIndices(List<Integer> textureIndices, List<Integer> vertexIndices,
                                        int polygonNumber, int totalTextures) {
        if (textureIndices.size() != vertexIndices.size()) {
            throw new IllegalArgumentException("Полигон " + polygonNumber +
                    ": количество текстурных координат (" + textureIndices.size() +
                    ") не совпадает с количеством вершин (" + vertexIndices.size() + ")");
        }

        for (int j = 0; j < textureIndices.size(); j++) {
            int idx = textureIndices.get(j);
            if (idx < 0 || idx >= totalTextures) { //в пределах
                throw new IllegalArgumentException("Полигон " + polygonNumber +
                        ", текстурная координата #" + (j + 1) + ": индекс " + idx +
                        " выходит за пределы [0, " + (totalTextures - 1) + "]");
            }
        }
    }

    private void validateNormalIndices(List<Integer> normalIndices, List<Integer> vertexIndices,
                                       int polygonNumber, int totalNormals) {
        if (normalIndices.size() != vertexIndices.size()) {
            throw new IllegalArgumentException("Полигон " + polygonNumber +
                    ": количество нормалей (" + normalIndices.size() +
                    ") не совпадает с количеством вершин (" + vertexIndices.size() + ")");
        }

        for (int j = 0; j < normalIndices.size(); j++) {
            int idx = normalIndices.get(j);
            if (idx < 0 || idx >= totalNormals) {
                throw new IllegalArgumentException("Полигон " + polygonNumber +
                        ", нормаль #" + (j + 1) + ": индекс " + idx +
                        " выходит за пределы [0, " + (totalNormals - 1) + "]");
            }
        }
    }

    private void writeHeader(Model model, PrintWriter writer) {
        writer.println(COMMENT_PREFIX + "Exported by ObjWriter");
        writer.println(COMMENT_PREFIX + "Vertices: " + model.getVertices().size());
        writer.println(COMMENT_PREFIX + "Texture vertices: " + model.getTextureVertices().size());
        writer.println(COMMENT_PREFIX + "Normals: " + model.getNormals().size());
        writer.println(COMMENT_PREFIX + "Polygons: " + model.getPolygons().size());
        writer.println();
    }

    private void writeVertices(Model model, PrintWriter writer) {
        for (Vector3f vertex : model.getVertices()) {
            String line = VERTEX_PREFIX +
                    formatFloat(vertex.getX()) + " " +
                    formatFloat(vertex.getY()) + " " +
                    formatFloat(vertex.getZ());
            writer.println(line);
        }
        writer.println();
    }

    private void writeTextureVertices(Model model, PrintWriter writer) {
        if (model.getTextureVertices().isEmpty()) {
            return;
        }

        writer.println(COMMENT_PREFIX + "Texture coordinates");
        for (Vector2f texVertex : model.getTextureVertices()) { //для каждой текстуры
            String line = TEXTURE_PREFIX +  // vt
                    formatFloat(texVertex.getX()) + " " +
                    formatFloat(texVertex.getY());
            writer.println(line);
        }
        writer.println();
    }

    private void writeNormals(Model model, PrintWriter writer) {
        if (model.getNormals().isEmpty()) {
            return;
        }

        writer.println(COMMENT_PREFIX + "Normals");
        for (Vector3f normal : model.getNormals()) {
            String line = NORMAL_PREFIX +
                    formatFloat(normal.getX()) + " " +
                    formatFloat(normal.getY()) + " " +
                    formatFloat(normal.getZ());
            writer.println(line);
        }
        writer.println();
    }

    private void writePolygons(Model model, PrintWriter writer) {
        if (model.getPolygons().isEmpty()) {
            return;
        }

        writer.println(COMMENT_PREFIX + "Faces");
        for (Polygon polygon : model.getPolygons()) {
            String faceLine = buildFaceLine(polygon);
            writer.println(faceLine);
        }
    }

    private String buildFaceLine(Polygon polygon) {
        StringBuilder faceBuilder = new StringBuilder(FACE_PREFIX);

        List<Integer> vertexIndices = polygon.getVertexIndices();
        List<Integer> textureVertexIndices = polygon.getTextureVertexIndices();
        List<Integer> normalIndices = polygon.getNormalIndices();

        boolean hasTexture = textureVertexIndices != null && !textureVertexIndices.isEmpty();
        boolean hasNormal = normalIndices != null && !normalIndices.isEmpty();

        for (int i = 0; i < vertexIndices.size(); i++) {
            if (i > 0) {
                faceBuilder.append(" ");
            }

            faceBuilder.append(buildVertexReference(
                    vertexIndices.get(i),
                    hasTexture && i < textureVertexIndices.size() ? textureVertexIndices.get(i) : null,
                    hasNormal && i < normalIndices.size() ? normalIndices.get(i) : null
            ));
        }

        return faceBuilder.toString();
    }

    private String buildVertexReference(int vertexIndex, Integer textureIndex, Integer normalIndex) {
        StringBuilder reference = new StringBuilder();

        reference.append(vertexIndex + 1); // Преобразование в 1-индексацию

        if (textureIndex != null && normalIndex != null) {
            // Формат: v/vt/vn
            reference.append("/").append(textureIndex + 1).append("/").append(normalIndex + 1);
        } else if (textureIndex != null) {
            // Формат: v/vt
            reference.append("/").append(textureIndex + 1);
        } else if (normalIndex != null) {
            // Формат: v//vn
            reference.append("//").append(normalIndex + 1);
        }
        // Если нет ни текстур, ни нормалей - просто v

        return reference.toString();
    }

    private String formatFloat(float value) {
        try {
            String formatted = String.format(Locale.US, FLOAT_FORMAT, value);
            formatted = removeTrailingZeros(formatted);
            return formatted;

        } catch (Exception e) {
            System.err.println("Предупреждение: ошибка форматирования числа " + value +
                    ": " + e.getMessage());
            return String.valueOf(value);
        }
    }

    private String removeTrailingZeros(String formatted) {
        String result = formatted.replaceAll("0+$", "");
        if (result.endsWith(".")) {
            result = result + "0";
        }
        return result;
    }

    public static boolean canWrite(Model model) {
        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            return false;
        }

        try {
            ObjWriter writer = new ObjWriter();
            writer.validateModel(model);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}