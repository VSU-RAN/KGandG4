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
import java.util.Vector;

import static vsu.org.ran.kgandg4.rasterization.Rasterization.*;
import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.*;

public class RenderEngine {
    private static Zbuffer zbuffer;
    private static Texture texture;

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) throws IOException {

        if (zbuffer == null || zbuffer.getWidth() != width || zbuffer.getHeight() != height) {
            zbuffer = new Zbuffer(width, height);
        } else {
            zbuffer.clear();
        }
        if (texture == null) {
            texture = new Texture("C:\\Users\\Merkury\\Desktop\\KGandG4\\textures\\derevo.jpg");
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
        //Как в методичке

        final int nPolygons = mesh.polygons.size();


        for (int polygonIndex = 0; polygonIndex < nPolygons; polygonIndex++) {
            Polygon polygon = mesh.polygons.get(polygonIndex);
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalsIndices = polygon.getNormalIndices();

            if (vertexIndices.size() != 3) {
                System.err.println("Найден полигон не с 3 вершинами");
                continue;
            }

            List<Point2f> screenPoints = new ArrayList<>();
            float[] zValues = new float[3];
            List<Vector2f> textureVertex = new ArrayList<>();
            List<Vector3f> normals = new ArrayList<>();

            for (int i = 0; i < vertexIndices.size(); i++) {
                Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));

                textureVertex.add(mesh.textureVertices.get(textureIndices.get(i)));
                normals.add(mesh.normals.get(normalsIndices.get(i)));

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
            if(textureVertex.size() != 3) {
                System.out.println("ХУЙНЯ");
                continue;
            }
            Point2f p0 = screenPoints.get(0);
            Point2f p1 = screenPoints.get(1);
            Point2f p2 = screenPoints.get(2);

            TexturedVertex v0 = new TexturedVertex(
                    screenPoints.get(0), zValues[0], textureVertex.get(0));
            TexturedVertex v1 = new TexturedVertex(
                    screenPoints.get(1), zValues[1], textureVertex.get(1));
            TexturedVertex v2 = new TexturedVertex(
                    screenPoints.get(2), zValues[2], textureVertex.get(2));

            drawTriangleBresenhamByIterator(graphicsContext.getPixelWriter(), zbuffer, texture, ray, k,
                    (int) p0.x, (int) p0.y, zValues[0], textureVertex.get(0).getX(), textureVertex.get(0).getY(), normals.get(0),
                    (int) p1.x, (int) p1.y, zValues[1], textureVertex.get(1).getX(), textureVertex.get(1).getY(), normals.get(1),
                    (int) p2.x, (int) p2.y, zValues[2], textureVertex.get(2).getX(), textureVertex.get(2).getY(), normals.get(2)

            );
        }
    }

//    public static void testZBuffer(
//            final GraphicsContext graphicsContext,
//            final int width,
//            final int height) {
//
//        if (zbuffer == null || zbuffer.getWidth() != width || zbuffer.getHeight() != height) {
//            zbuffer = new Zbuffer(width, height);
//        } else {
//            zbuffer.clear();
//        }
//
//        drawTriangleBresenhamByIterator(
//                graphicsContext.getPixelWriter(), zbuffer, Color.RED,
//                100, 100, 0.3f,
//                300, 100, 0.3f,
//                200, 300, 0.3f
//
//        );
//
//        drawTriangleBresenhamByIterator(
//                graphicsContext.getPixelWriter(), zbuffer, javafx.scene.paint.Color.BLUE,
//                150, 150, 0.7f,
//                350, 150, 0.7f,
//                250, 350, 0.7f
//        );
//
//        drawTriangleBresenhamByIterator(
//                graphicsContext.getPixelWriter(), zbuffer, Color.GREEN,
//                50, 200, 0.1f,
//                150, 200, 0.1f,
//                100, 300, 0.1f
//        );
//
//        // Что должно получиться:
//        // 1. Зелёный треугольник поверх всех (он ближе всего)
//        // 2. Красный треугольник виден там, где нет зелёного
//        // 3. Синий треугольник НЕ виден там, где есть красный или зелёный
//    }
}