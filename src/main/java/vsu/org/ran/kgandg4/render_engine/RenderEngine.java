package vsu.org.ran.kgandg4.render_engine;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.math.Vector2f;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.model.Polygon;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static vsu.org.ran.kgandg4.rasterization.Rasterization.*;
import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.*;

public class RenderEngine {
    private static Zbuffer zbuffer;
    private static Texture texture = new Texture(Color.GRAY); // Дефолтная текстура

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {

        if (zbuffer == null || zbuffer.getWidth() != width || zbuffer.getHeight() != height) {
            zbuffer = new Zbuffer(width, height);
        } else {
            zbuffer.clear();
        }

        javax.vecmath.Vector3f ray = camera.getDirection();
        ray.normalize();
        float k = 0.9f;

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f projectionViewModelMatrixProjectionMatrix = new Matrix4f(projectionMatrix);
        projectionViewModelMatrixProjectionMatrix.mul(viewMatrix);
        projectionViewModelMatrixProjectionMatrix.mul(modelMatrix);

        final int nPolygons = mesh.polygons.size();

        for (int polygonIndex = 0; polygonIndex < nPolygons; polygonIndex++) {
            Polygon polygon = mesh.polygons.get(polygonIndex);
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalsIndices = polygon.getNormalIndices();

            // Пропускаем полигоны с неправильным количеством вершин
            if (vertexIndices.size() < 3) {
                System.err.println("Полигон " + polygonIndex + ": менее 3 вершин");
                continue;
            }

            // Проверяем наличие текстурных координат
            if (textureIndices == null || textureIndices.isEmpty()) {
                // Создаем дефолтные текстурные координаты
                textureIndices = new ArrayList<>();
                for (int i = 0; i < vertexIndices.size(); i++) {
                    textureIndices.add(0); // Используем первую текстуру
                }
            }

            // Проверяем наличие нормалей
            if (normalsIndices == null || normalsIndices.isEmpty()) {
                // Создаем дефолтные нормали
                normalsIndices = new ArrayList<>();
                for (int i = 0; i < vertexIndices.size(); i++) {
                    normalsIndices.add(0); // Используем первую нормаль
                }
            }

            // Если полигон имеет больше 3 вершин, берем только первые 3
            int verticesToProcess = Math.min(3, vertexIndices.size());

            List<Point2f> screenPoints = new ArrayList<>();
            float[] zValues = new float[verticesToProcess];
            List<Vector2f> textureVertex = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();

            for (int i = 0; i < verticesToProcess; i++) {
                // Безопасное получение индексов
                int vertexIdx = vertexIndices.get(i);
                if (vertexIdx < 0 || vertexIdx >= mesh.vertices.size()) {
                    System.err.println("Неверный индекс вершины: " + vertexIdx);
                    break;
                }

                Vector3f vertex = mesh.vertices.get(vertexIdx);

                // Безопасное получение текстурных координат
                int texIdx = (i < textureIndices.size()) ? textureIndices.get(i) : 0;
                if (texIdx < 0 || texIdx >= mesh.textureVertices.size()) {
                    // Используем дефолтные UV координаты
                    textureVertex.add(new Vector2f(0, 0));
                } else {
                    textureVertex.add(mesh.textureVertices.get(texIdx));
                }

                // Безопасное получение нормалей
                int normIdx = (i < normalsIndices.size()) ? normalsIndices.get(i) : 0;
                if (normIdx < 0 || normIdx >= mesh.normals.size()) {
                    // Используем дефолтную нормаль
                    normals.add(new Vector3f(0, 1, 0));
                } else {
                    normals.add(mesh.normals.get(normIdx));
                }

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                javax.vecmath.Vector3f vertexAfterMVPandNormalize = getVertexAfterMVPandNormalize(projectionViewModelMatrixProjectionMatrix, vertexVecmath);
                if (!GraphicConveyor.isValidVertex(vertexAfterMVPandNormalize)) {
                    break;
                }
                zValues[i] = vertexAfterMVPandNormalize.z;

                Point2f screenPoint = vertexToPoint(vertexAfterMVPandNormalize, width, height);

                screenPoints.add(screenPoint);
            }

            if (screenPoints.size() != 3) {
                continue;
            }

            if (textureVertex.size() != 3) {
                // Заполняем недостающие текстурные координаты
                while (textureVertex.size() < 3) {
                    textureVertex.add(new Vector2f(0, 0));
                }
            }

            if (normals.size() != 3) {
                // Заполняем недостающие нормали
                while (normals.size() < 3) {
                    normals.add(new Vector3f(0, 1, 0));
                }
            }

            Point2f p0 = screenPoints.get(0);
            Point2f p1 = screenPoints.get(1);
            Point2f p2 = screenPoints.get(2);

            // Рисуем треугольник
            drawTriangleSimpleBox(graphicsContext.getPixelWriter(), zbuffer, texture, ray, k,
                    (int) p0.x, (int) p0.y, zValues[0],
                    textureVertex.get(0).getX(), textureVertex.get(0).getY(), normals.get(0),
                    (int) p1.x, (int) p1.y, zValues[1],
                    textureVertex.get(1).getX(), textureVertex.get(1).getY(), normals.get(1),
                    (int) p2.x, (int) p2.y, zValues[2],
                    textureVertex.get(2).getX(), textureVertex.get(2).getY(), normals.get(2)
            );
        }
    }
}