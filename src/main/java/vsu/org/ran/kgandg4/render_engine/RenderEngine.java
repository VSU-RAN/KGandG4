package vsu.org.ran.kgandg4.render_engine;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import vsu.org.ran.kgandg4.math.Vector3f;
import vsu.org.ran.kgandg4.model.Model;
import vsu.org.ran.kgandg4.rasterization.Rasterization;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector4f;

import java.util.ArrayList;

import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height)
    {

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f projectionViewModelMatrixProjectionMatrix = new Matrix4f(projectionMatrix);
        projectionViewModelMatrixProjectionMatrix.mul(viewMatrix);
        projectionViewModelMatrixProjectionMatrix.mul(modelMatrix);
        //Как в методичке

        final int nPolygons = mesh.polygons.size();

        for (int polygonIndex = 0; polygonIndex < nPolygons; polygonIndex++) {
            ArrayList<Integer> vertexIndices = mesh.polygons.get(polygonIndex).getVertexIndices();

            if(vertexIndices.size() != 3) {
                System.err.println("Найден полигон не с 3 вершинами");
                continue;
            }

            ArrayList<Point2f> screenPoints = new ArrayList<>();

            for (int i = 0; i < vertexIndices.size(); i ++) {
                Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.getX(), vertex.getY(), vertex.getZ());

                javax.vecmath.Vector3f vertexAfterMVPandNormalize = getVertexAfterMVPandNormalize(projectionViewModelMatrixProjectionMatrix, vertexVecmath);
                if (!GraphicConveyor.isValidVertex(vertexAfterMVPandNormalize)) {
                    break;
                }

                Point2f screenPoint = vertexToPoint(vertexAfterMVPandNormalize, width, height);

                screenPoints.add(screenPoint);
            }
            if (screenPoints.size() != 3) {
                System.out.println("Скипнут треугольник");
                continue;
            }
            Point2f p0 = screenPoints.get(0);
            Point2f p1 = screenPoints.get(1);
            Point2f p2 = screenPoints.get(2);

            Rasterization.drawTriangle(graphicsContext.getPixelWriter(),
                    (int) p0.x, (int) p0.y,
                    (int) p1.x, (int) p1.y,
                    (int) p2.x, (int) p2.y
            );
        }
    }
}