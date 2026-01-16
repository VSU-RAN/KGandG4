package vsu.org.ran.kgandg4.render_engine.render;

import math.matrix.Matrix4f;
import math.point.Point2f;
import math.vector.Vector2f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.model.models.Triangle;
import vsu.org.ran.kgandg4.model.models.TriangulatedModel;
import vsu.org.ran.kgandg4.render_engine.GraphicConveyor;
import vsu.org.ran.kgandg4.render_engine.Lightning;

import static vsu.org.ran.kgandg4.rasterization.Rasterization.*;
import static vsu.org.ran.kgandg4.render_engine.GraphicConveyor.*;

public class RenderEngine {

    private static class TriangleData {
        final Point2f[] screenPoints = new Point2f[3];
        final float[] zValues = new float[3];
        final Vector2f[] texCoords = new Vector2f[3];
        final Vector3f[] normals = new Vector3f[3];
        final Vector3f[] worldVertices = new Vector3f[3];

        private void setVertex(int i, Point2f screenCord, float z, Vector2f texCord, Vector3f normalCord, Vector3f worldCord) {
            screenPoints[i] = screenCord;
            zValues[i] = z;
            texCoords[i] = texCord;
            normals[i] = normalCord;
            worldVertices[i] = worldCord;
        }

        private boolean isValid() {
            for (int i = 0; i < 3; i ++) {
                if (screenPoints[i] == null || texCoords[i] == null || normals[i] == null || worldVertices[i] == null) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void render(RenderContext context) {
        RenderMode mode = context.getMode();

        if (mode.isOnlyWireframe()) {
            renderWireframeOnly(context);
        }
        else {
            renderWithOptions(context);
        }
    }


    private static void renderWireframeOnly(RenderContext context) {
        for (TriangulatedModel model : context.getVisibleModels()) {
            renderSingleModelWireframeOnly(context, model);
        }
    }

    private static void renderWithOptions(RenderContext context) {
        for (TriangulatedModel model : context.getVisibleModels()) {
            renderSingleModelWithOptions(context, model);
        }
    }

    private static void renderSingleModelWireframeOnly(RenderContext context, TriangulatedModel model) {
        if (model == null) {
            return;
        }

        Matrix4f pvmMatrix = context.getPVMMatrix();

        for (Triangle triangle : model.getTriangles()) {
            renderTriangleWireframeOnly(context, model, triangle, pvmMatrix);
        }
    }

    private static void renderSingleModelWithOptions(RenderContext context, TriangulatedModel model) {
        if (model == null) {
            return;
        }

        Lightning lightning = context.getLightning();
        Vector3f ray = context.getCameraDirectionNormalized();
        RenderMode renderMode = context.getMode();
        Matrix4f pvmMatrix = context.getPVMMatrix();

        for (Triangle triangle : model.getTriangles()) {
            renderTriangleWithOptions(context, model, triangle, pvmMatrix, ray, renderMode, lightning);
        }
    }

    private static void renderTriangleWireframeOnly(RenderContext context, TriangulatedModel model, Triangle triangle, Matrix4f pvmMatrix) {
        TriangleData data = new TriangleData();

        for (int i = 0; i < 3; i++) {
            Vector3f worldVertex = triangle.getWorldVertex(i, model);
            Vector3f transformed = getVertexAfterMVPandNormalize(pvmMatrix, worldVertex);

            if (!GraphicConveyor.isValidVertex(transformed)) {
                break;
            }

            Point2f screenPoint = vertexToPoint(transformed, context.getWidth(), context.getHeight());
            data.setVertex(i, screenPoint, transformed.getZ(), new Vector2f(0, 0), new Vector3f(0, 0, 0), worldVertex);
        }

        if (data.isValid()) {
            int x0 = (int) data.screenPoints[0].getX();
            int y0 = (int) data.screenPoints[0].getY();
            float z0 = data.zValues[0];

            int x1 = (int) data.screenPoints[1].getX();
            int y1 = (int) data.screenPoints[1].getY();
            float z1 = data.zValues[1];

            int x2 = (int) data.screenPoints[2].getX();
            int y2 = (int) data.screenPoints[2].getY();
            float z2 = data.zValues[2];

            Vector3f worldVertex0 = data.worldVertices[0];
            Vector3f worldVertex1 = data.worldVertices[1];
            Vector3f worldVertex2 = data.worldVertices[2];
            Vector3f cameraPos = context.getCamera().getPosition();

            // Рисуем только каркас
            if (GraphicConveyor.isFrontFace(worldVertex0, worldVertex1, worldVertex2, cameraPos)) {
                drawWireFrameTriangle(
                        context.getGraphicsContext().getPixelWriter(),
                        context.getZbuffer(),
                        context.getWireframeColor(),
                        x0, y0, z0,
                        x1, y1, z1,
                        x2, y2, z2
                );
            }
        }
    }

    private static void renderTriangleWithOptions(RenderContext context, TriangulatedModel model, Triangle triangle, Matrix4f pvmMatrix, Vector3f ray, RenderMode mode, Lightning lightning) {
        TriangleData data = new TriangleData();

        for (int i = 0; i < 3; i++) {
            Vector3f worldVertex = triangle.getWorldVertex(i, model);
            Vector2f texCord = triangle.getTextureCord(i, model);
            Vector3f normal = triangle.getNormal(i, model);

            Vector3f transformed = getVertexAfterMVPandNormalize(pvmMatrix, worldVertex);

            if (!GraphicConveyor.isValidVertex(transformed)) {
                break;
            }

            Point2f screenPoint = vertexToPoint(transformed, context.getWidth(), context.getHeight());
            data.setVertex(i, screenPoint, transformed.getZ(), texCord, normal, worldVertex);
        }

        if (data.isValid()) {
            int x0 = (int)data.screenPoints[0].getX();
            int y0 = (int)data.screenPoints[0].getY();
            float z0 = data.zValues[0];
            float u0 = data.texCoords[0].getX();
            float v0 = data.texCoords[0].getY();
            Vector3f n0 = data.normals[0];

            int x1 = (int)data.screenPoints[1].getX();
            int y1 = (int)data.screenPoints[1].getY();
            float z1 = data.zValues[1];
            float u1 = data.texCoords[1].getX();
            float v1 = data.texCoords[1].getY();
            Vector3f n1 = data.normals[1];

            int x2 = (int)data.screenPoints[2].getX();
            int y2 = (int)data.screenPoints[2].getY();
            float z2 = data.zValues[2];
            float u2 = data.texCoords[2].getX();
            float v2 = data.texCoords[2].getY();
            Vector3f n2 = data.normals[2];

            drawTriangle(
                    context.getGraphicsContext().getPixelWriter(),
                    context.getZbuffer(),
                    context.getTexture(),
                    ray, lightning,
                    x0, y0, z0, u0, v0, n0,
                    x1, y1, z1, u1, v1, n1,
                    x2, y2, z2, u2, v2, n2
            );

            if (mode.isWireframe()) {
                Vector3f worldVertex0 = data.worldVertices[0];
                Vector3f worldVertex1 = data.worldVertices[1];
                Vector3f worldVertex2 = data.worldVertices[2];

                Vector3f cameraPos = context.getCamera().getPosition();

                if (GraphicConveyor.isFrontFace(worldVertex0, worldVertex1, worldVertex2, cameraPos)) {
                    drawWireFrameTriangle(
                            context.getGraphicsContext().getPixelWriter(),
                            context.getZbuffer(),
                            context.getWireframeColor(),
                            x0, y0, z0,
                            x1, y1, z1,
                            x2, y2, z2
                    );
                }
            }
        }
    }
}