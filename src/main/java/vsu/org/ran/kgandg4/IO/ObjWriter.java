package vsu.org.ran.kgandg4.IO;

import java.util.Locale;
import java.nio.file.Path;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import math.vector.Vector2f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

public class ObjWriter {

    private static final DecimalFormat DECIMAL_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        DECIMAL_FORMAT = new DecimalFormat("#.######", symbols);
    }

    public static void write(Model model, Path filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            writeVertices(model, writer);
            writeTextureVertices(model, writer);
            writeNormals(model, writer);
            writeFaces(model, writer);
        }
    }

    private static void writeVertices(Model model, FileWriter writer) throws IOException {
        for (Vector3f vertex : model.vertices) {
            writer.write(String.format("v %s %s %s\n",
                    DECIMAL_FORMAT.format(vertex.getX()),
                    DECIMAL_FORMAT.format(vertex.getY()),
                    DECIMAL_FORMAT.format(vertex.getZ())));
        }
        if (!model.vertices.isEmpty()) {
            writer.write("\n");
        }
    }

    private static void writeTextureVertices(Model model, FileWriter writer) throws IOException {
        for (Vector2f texVertex : model.textureVertices) {
            writer.write(String.format("vt %s %s\n",
                    DECIMAL_FORMAT.format(texVertex.getX()),
                    DECIMAL_FORMAT.format(texVertex.getY())));
        }
        if (!model.textureVertices.isEmpty()) {
            writer.write("\n");
        }
    }

    private static void writeNormals(Model model, FileWriter writer) throws IOException {
        for (Vector3f normal : model.normals) {
            writer.write(String.format("vn %s %s %s\n",
                    DECIMAL_FORMAT.format(normal.getX()),
                    DECIMAL_FORMAT.format(normal.getY()),
                    DECIMAL_FORMAT.format(normal.getZ())));
        }
        if (!model.normals.isEmpty()) {
            writer.write("\n");
        }
    }

    private static void writeFaces(Model model, FileWriter writer) throws IOException {
        for (Polygon polygon : model.polygons) {
            writer.write("f");

            for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                writer.write(" ");

                // Vertex index (обязательный)
                int vertexIndex = polygon.getVertexIndices().get(i) + 1;

                // Texture index (если есть)
                Integer textureIndex = null;
                if (i < polygon.getTextureVertexIndices().size()) {
                    textureIndex = polygon.getTextureVertexIndices().get(i) + 1;
                }

                // Normal index (если есть)
                Integer normalIndex = null;
                if (i < polygon.getNormalIndices().size()) {
                    normalIndex = polygon.getNormalIndices().get(i) + 1;
                }

                // Формируем строку вершины
                if (textureIndex != null && normalIndex != null) {
                    writer.write(String.format("%d/%d/%d", vertexIndex, textureIndex, normalIndex));
                } else if (textureIndex != null) {
                    writer.write(String.format("%d/%d", vertexIndex, textureIndex));
                } else if (normalIndex != null) {
                    writer.write(String.format("%d//%d", vertexIndex, normalIndex));
                } else {
                    writer.write(String.format("%d", vertexIndex));
                }
            }
            writer.write("\n");
        }
    }
}