package vsu.org.ran.kgandg4.triangulator.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vsu.org.ran.kgandg4.IO.objReader.ObjReader;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;
import vsu.org.ran.kgandg4.triangulation.EarCuttingTriangulator;
import vsu.org.ran.kgandg4.triangulation.Triangulator;
import vsu.org.ran.kgandg4.triangulation.utils.Constants;
import vsu.org.ran.kgandg4.triangulation.utils.PolygonUtil;

import math.vector.Vector2f;  // вместо  //import vsu.org.ran.kgandg4.math.Vector2f;
import math.vector.Vector3f;  // вместо  //import vsu.org.ran.kgandg4.math.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class EarClippingTriangulatorTest {
    private Model oneSimplePolygonModel;
    private Model cubeModel;
    private Model badPolygonsModel;
    private Model alreadyTriangulatedModel;
    private Model bigPlaneModel;
    private final Triangulator earClippingTriangulator = new EarCuttingTriangulator();

    @BeforeEach
    public void setUp() throws IOException {
        setUpOnePolygonModel();
        setUpBadPolygonsModel();
        setUpCube();
        setUpAlreadyTriangulatedModel();
        setUpBigPlaneModel();
    }

    private void setUpOnePolygonModel() throws IOException {
        oneSimplePolygonModel = ObjReader.read(loadFileContent("src/main/resources/mesh/test/oneSimplePolygon.obj"));
    }

    private void setUpCube() throws IOException {
        cubeModel = ObjReader.read(loadFileContent("src/main/resources/mesh/test/cube.obj"));
    }

    private void setUpBadPolygonsModel() throws IOException {
        badPolygonsModel = ObjReader.read(loadFileContent("src/main/resources/mesh/test/badPolygons.obj"));
    }

    private void setUpAlreadyTriangulatedModel() throws IOException {
        alreadyTriangulatedModel = ObjReader.read(loadFileContent("src/main/resources/mesh/test/triangulated.obj"));
    }

    private void setUpBigPlaneModel() throws IOException {
        bigPlaneModel = ObjReader.read(loadFileContent("src/main/resources/mesh/test/bigPlane.obj"));
    }

    private String loadFileContent(String filePath) throws IOException {
        Path path = Path.of(filePath);
        return Files.readString(path);
    }

    @Test
    public void testTriangulationAllTriangles() {
        earClippingTriangulator.triangulateModel(cubeModel);
        for (Polygon polygon : cubeModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size());
        }

        earClippingTriangulator.triangulateModel(oneSimplePolygonModel);
        for (Polygon polygon : oneSimplePolygonModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size());
        }
    }

    @Test
    public void testTriangulatedModelTextures() {
        Model model1 = earClippingTriangulator.createTriangulatedModel(cubeModel);
        Model model2 = earClippingTriangulator.createTriangulatedModel(oneSimplePolygonModel);
        Assertions.assertEquals(cubeModel.textureVertices, model1.textureVertices);
        Assertions.assertEquals(oneSimplePolygonModel.textureVertices, model2.textureVertices);
    }

    @Test
    public void testTriangulatedModelNormals() {
        Model model1 = earClippingTriangulator.createTriangulatedModel(cubeModel);
        Model model2 = earClippingTriangulator.createTriangulatedModel(oneSimplePolygonModel);
        Assertions.assertEquals(cubeModel.normals, model1.normals);
        Assertions.assertEquals(oneSimplePolygonModel.normals, model2.normals);
    }

    @Test
    public void testTriangulateBadModel() {
        Assertions.assertTimeout(Duration.ofSeconds(3), () -> earClippingTriangulator.triangulateModel(badPolygonsModel));

    }

    @Test
    public void testTriangulatedModelChange() {
        Model model = earClippingTriangulator.createTriangulatedModel(cubeModel);
        Assertions.assertEquals(cubeModel.normals, model.normals);
        Assertions.assertEquals(cubeModel.textureVertices, model.textureVertices);

        model.normals.addAll(List.of(
                new Vector3f(5, 4, 3),
                new Vector3f(5, 4, 3),
                new Vector3f(5, 4, 3)
        ));
        model.textureVertices.addAll(List.of(
                new Vector2f(1, 1),
                new Vector2f(1, 2),
                new Vector2f(1, 1)
        ));
        Assertions.assertNotEquals(cubeModel.normals, model.normals);
        Assertions.assertNotEquals(cubeModel.textureVertices, model.textureVertices);
    }

    @Test
    public void testTriangulateAlreadyTriangulatedModel() {
        earClippingTriangulator.triangulateModel(alreadyTriangulatedModel);
        for (Polygon polygon : alreadyTriangulatedModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size());
        }
    }

    @Test
    public void testTriangulateAlreadyTriangulatedNoPolygonsChanges() {
        Model model = earClippingTriangulator.createTriangulatedModel(alreadyTriangulatedModel);
        Assertions.assertEquals(alreadyTriangulatedModel.polygons, model.polygons);

        model.polygons.add(
                new Polygon(
                        List.of(0, 1, 2),
                        List.of(),
                        List.of()
                )
        );
        Assertions.assertNotEquals(alreadyTriangulatedModel.polygons, model.polygons);
    }

    @Test
    public void testTriangulatingSquareTheSame() {
        Model model = earClippingTriangulator.createTriangulatedModel(cubeModel);
        float squareAfterTriangulation = 0;
        for (Polygon polygon : model.polygons) {
            squareAfterTriangulation += PolygonUtil.calcTrianglePolygonSquare(polygon, model);
        }
        Assertions.assertEquals(24, squareAfterTriangulation, Constants.EPS);
    }

    @Test
    public void testTriangulationPerformance() {
        Assertions.assertTimeout(Duration.ofSeconds(2), () -> earClippingTriangulator.triangulateModel(bigPlaneModel));
        for (Polygon polygon : bigPlaneModel.polygons) {
            Assertions.assertEquals(3, polygon.getVertexIndices().size());
        }
    }
}
