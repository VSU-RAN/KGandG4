package vsu.org.ran.kgandg4.IO.objReader;

import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

import math.vector.Vector2f;
import math.vector.Vector3f;

import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";
	private static final String OBJ_COMMENT_TOKEN = "#";

	// Константы для минимального количества данных
	private static final int MIN_VERTEX_COMPONENTS = 3;
	private static final int MIN_TEXTURE_COMPONENTS = 2;
	private static final int MIN_NORMAL_COMPONENTS = 3;
	private static final int MIN_FACE_VERTICES = 3;

	private static Model currentModel;
	private static int vertexCount = 0;
	private static int textureCount = 0;
	private static int normalCount = 0;

	public static Model read(String fileContent) {
		currentModel = new Model();
		vertexCount = 0;
		textureCount = 0;
		normalCount = 0;

		int lineInd = 0;

		// Проверка на пустой файл
		if (fileContent == null || fileContent.trim().isEmpty()) {
			throw new ObjReaderException("File content is empty or null", 0);
		}

		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().trim();
			lineInd++;

			if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) {
				continue;
			}

			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			// Проверка на пустые данные после токена
			if (wordsInLine.isEmpty() && !token.equals(OBJ_COMMENT_TOKEN)) {
				throw new ObjReaderException("Missing data after token: " + token, lineInd);
			}

			try {
				switch (token) {
					case OBJ_VERTEX_TOKEN -> {
						currentModel.getVertices().add(parseVertex(wordsInLine, lineInd));
						vertexCount++;
					}
					case OBJ_TEXTURE_TOKEN -> {
						currentModel.getTextureVertices().add(parseTextureVertex(wordsInLine, lineInd));
						textureCount++;
					}
					case OBJ_NORMAL_TOKEN -> {
						currentModel.getNormals().add(parseNormal(wordsInLine, lineInd));
						normalCount++;
					}
					case OBJ_FACE_TOKEN -> currentModel.getPolygons().add(parseFace(wordsInLine, lineInd));
					default -> {
						// Игнорируем неизвестные токены, но можно логировать
					}
				}
			} catch (ObjReaderException e) {
				throw e;
			} catch (Exception e) {
				throw new ObjReaderException("Unexpected error: " + e.getMessage(), lineInd);
			}
		}
		scanner.close();

		// Пост-валидация модели
		validateModel(lineInd);

		return currentModel;
	}

	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.size() < MIN_VERTEX_COMPONENTS) {
				throw new ObjReaderException(
						String.format("Too few vertex arguments. Expected at least %d, got %d",
								MIN_VERTEX_COMPONENTS, wordsInLineWithoutToken.size()),
						lineInd
				);
			}

			float x = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "vertex x", lineInd);
			float y = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "vertex y", lineInd);
			float z = parseFloatWithValidation(wordsInLineWithoutToken.get(2), "vertex z", lineInd);

			return new Vector3f(x, y, z);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Invalid vertex format", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.size() < MIN_TEXTURE_COMPONENTS) {
				throw new ObjReaderException(
						String.format("Too few texture vertex arguments. Expected at least %d, got %d",
								MIN_TEXTURE_COMPONENTS, wordsInLineWithoutToken.size()),
						lineInd
				);
			}

			float u = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "texture u", lineInd);
			float v = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "texture v", lineInd);

			return new Vector2f(u, v);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Invalid texture vertex format", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.size() < MIN_NORMAL_COMPONENTS) {
				throw new ObjReaderException(
						String.format("Too few normal arguments. Expected at least %d, got %d",
								MIN_NORMAL_COMPONENTS, wordsInLineWithoutToken.size()),
						lineInd
				);
			}

			float x = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "normal x", lineInd);
			float y = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "normal y", lineInd);
			float z = parseFloatWithValidation(wordsInLineWithoutToken.get(2), "normal z", lineInd);

			return new Vector3f(x, y, z);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Invalid normal format", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		// Проверка на минимальное количество вершин в полигоне
		if (wordsInLineWithoutToken.size() < MIN_FACE_VERTICES) {
			throw new ObjReaderException(
					String.format("Too few vertices in face. Expected at least %d, got %d",
							MIN_FACE_VERTICES, wordsInLineWithoutToken.size()),
					lineInd
			);
		}

		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<>();

		// Определяем ожидаемый формат по первому элементу
		String firstWord = wordsInLineWithoutToken.get(0);
		int expectedFormat = detectFaceWordFormat(firstWord, lineInd);
		boolean hasTextureInFirst = firstWord.contains("/") &&
				firstWord.split("/").length > 1 &&
				!firstWord.split("/")[1].isEmpty();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices,
					onePolygonNormalIndices, lineInd, expectedFormat, hasTextureInFirst);
		}

		// Проверка согласованности формата внутри полигона
		validateFaceConsistency(onePolygonVertexIndices, onePolygonTextureVertexIndices,
				onePolygonNormalIndices, lineInd);

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		result.setNormalIndices(onePolygonNormalIndices);

		return result;
	}

	private static int detectFaceWordFormat(String word, int lineInd) {
		if (!word.contains("/")) {
			return 1; // Только вершина
		}

		String[] parts = word.split("/");

		// Проверка на некорректные форматы
		if (parts.length > 3) {
			throw new ObjReaderException(
					"Invalid face element format. Too many '/' separators",
					lineInd
			);
		}

		return parts.length;
	}

	private static void validateFaceConsistency(
			ArrayList<Integer> vertexIndices,
			ArrayList<Integer> textureIndices,
			ArrayList<Integer> normalIndices,
			int lineInd) {

		// Проверяем, что все три списка либо пустые, либо одинаковой длины
		boolean hasTexture = !textureIndices.isEmpty();
		boolean hasNormal = !normalIndices.isEmpty();

		if (hasTexture && textureIndices.size() != vertexIndices.size()) {
			throw new ObjReaderException(
					String.format("Texture indices count (%d) doesn't match vertex indices count (%d)",
							textureIndices.size(), vertexIndices.size()),
					lineInd
			);
		}

		if (hasNormal && normalIndices.size() != vertexIndices.size()) {
			throw new ObjReaderException(
					String.format("Normal indices count (%d) doesn't match vertex indices count (%d)",
							normalIndices.size(), vertexIndices.size()),
					lineInd
			);
		}
	}

	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			int expectedFormat,
			boolean hasTextureInFirst) {

		try {
			// Проверка на пустое слово
			if (wordInLine == null || wordInLine.trim().isEmpty()) {
				throw new ObjReaderException("Empty face element", lineInd);
			}

			String[] wordIndices = wordInLine.split("/");

			// Проверка согласованности формата
			if (wordIndices.length != expectedFormat) {
				throw new ObjReaderException(
						String.format("Inconsistent face format. Expected %d parts, got %d",
								expectedFormat, wordIndices.length),
						lineInd
				);
			}

			// Проверка на относительные индексы (отрицательные числа)
			boolean hasRelativeIndices = false;
			for (String idx : wordIndices) {
				if (!idx.isEmpty() && idx.startsWith("-")) {
					hasRelativeIndices = true;
					break;
				}
			}

			if (hasRelativeIndices) {
				throw new ObjReaderException("Relative indices (negative numbers) are not supported", lineInd);
			}

			switch (wordIndices.length) {
				case 1 -> {
					// Формат: vertex
					int vertexIndex = parseIndex(wordIndices[0], "vertex", lineInd);
					checkIndexNotZero(vertexIndex, "vertex", lineInd);
					onePolygonVertexIndices.add(vertexIndex - 1);
				}
				case 2 -> {
					// Формат: vertex/texture
					int vertexIndex = parseIndex(wordIndices[0], "vertex", lineInd);
					int textureIndex = parseIndex(wordIndices[1], "texture", lineInd);

					checkIndexNotZero(vertexIndex, "vertex", lineInd);
					checkIndexNotZero(textureIndex, "texture", lineInd);

					onePolygonVertexIndices.add(vertexIndex - 1);
					onePolygonTextureVertexIndices.add(textureIndex - 1);
				}
				case 3 -> {
					// Формат: vertex/texture/normal или vertex//normal
					int vertexIndex = parseIndex(wordIndices[0], "vertex", lineInd);
					checkIndexNotZero(vertexIndex, "vertex", lineInd);

					onePolygonVertexIndices.add(vertexIndex - 1);

					// Обработка текстурного индекса (может быть пустым)
					if (!wordIndices[1].isEmpty()) {
						int textureIndex = parseIndex(wordIndices[1], "texture", lineInd);
						checkIndexNotZero(textureIndex, "texture", lineInd);
						onePolygonTextureVertexIndices.add(textureIndex - 1);
					} else if (hasTextureInFirst) {
						// Если в первом элементе был текстура, а в этом нет - ошибка
						throw new ObjReaderException(
								"Inconsistent texture indices in face",
								lineInd
						);
					}

					// Нормальный индекс (обязателен в формате с 3 частями)
					if (wordIndices[2].isEmpty()) {
						throw new ObjReaderException("Empty normal index", lineInd);
					}

					int normalIndex = parseIndex(wordIndices[2], "normal", lineInd);
					checkIndexNotZero(normalIndex, "normal", lineInd);
					onePolygonNormalIndices.add(normalIndex - 1);
				}
				default -> {
					throw new ObjReaderException(
							String.format("Invalid face element format. Expected 1-3 parts, got %d",
									wordIndices.length),
							lineInd
					);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Failed to parse integer value in face element", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Invalid face element format", lineInd);
		}
	}

	private static void validateModel(int lineInd) {
		// Проверка, что есть вершины, если есть полигоны
		if (!currentModel.getPolygons().isEmpty() && currentModel.getVertices().isEmpty()) {
			throw new ObjReaderException(
					"Model contains polygons but no vertices",
					lineInd
			);
		}

		// Проверка всех полигонов на корректность индексов
		for (int i = 0; i < currentModel.getPolygons().size(); i++) {
			Polygon poly = currentModel.getPolygons().get(i);
			validatePolygonIndices(poly, i + 1, lineInd);

			// Проверка на уникальность вершин в полигоне
			if (hasDuplicateVertices(poly)) {
				throw new ObjReaderException(
						String.format("Polygon %d has duplicate vertices", i + 1),
						lineInd
				);
			}
		}
	}

	private static void validatePolygonIndices(Polygon polygon, int polygonIndex, int lineInd) {
		// Проверка индексов вершин
		for (int vertexIndex : polygon.getVertexIndices()) {
			if (vertexIndex < 0 || vertexIndex >= currentModel.getVertices().size()) {
				throw new ObjReaderException(
						String.format("Vertex index %d out of bounds [1, %d]",
								vertexIndex + 1, currentModel.getVertices().size()),
						lineInd
				);
			}
		}

		// Проверка индексов текстур
		if (!polygon.getTextureVertexIndices().isEmpty()) {
			for (int texIndex : polygon.getTextureVertexIndices()) {
				if (texIndex < 0 || texIndex >= currentModel.getTextureVertices().size()) {
					throw new ObjReaderException(
							String.format("Texture index %d out of bounds [1, %d]",
									texIndex + 1, currentModel.getTextureVertices().size()),
							lineInd
					);
				}
			}
		}

		// Проверка индексов нормалей
		if (!polygon.getNormalIndices().isEmpty()) {
			for (int normalIndex : polygon.getNormalIndices()) {
				if (normalIndex < 0 || normalIndex >= currentModel.getNormals().size()) {
					throw new ObjReaderException(
							String.format("Normal index %d out of bounds [1, %d]",
									normalIndex + 1, currentModel.getNormals().size()),
							lineInd
					);
				}
			}
		}
	}

	private static boolean hasDuplicateVertices(Polygon poly) {
		ArrayList<Integer> vertices = poly.getVertexIndices();
		for (int i = 0; i < vertices.size(); i++) {
			for (int j = i + 1; j < vertices.size(); j++) {
				if (vertices.get(i).equals(vertices.get(j))) {
					return true;
				}
			}
		}
		return false;
	}

	private static float parseFloatWithValidation(String str, String coordinateName, int lineInd) {
		try {
			float value = Float.parseFloat(str);

			if (Float.isNaN(value)) {
				throw new ObjReaderException(
						String.format("%s coordinate is NaN", coordinateName),
						lineInd
				);
			}
			if (Float.isInfinite(value)) {
				throw new ObjReaderException(
						String.format("%s coordinate is infinite", coordinateName),
						lineInd
				);
			}

			return value;

		} catch (NumberFormatException e) {
			throw new ObjReaderException(
					String.format("Failed to parse float value for %s: '%s'",
							coordinateName, str),
					lineInd
			);
		}
	}

	private static int parseIndex(String str, String indexType, int lineInd) {
		try {
			int index = Integer.parseInt(str);
			if (index == 0) {
				throw new ObjReaderException(
						String.format("%s index cannot be zero (OBJ uses 1-based indexing)", indexType),
						lineInd
				);
			}
			return index;

		} catch (NumberFormatException e) {
			throw new ObjReaderException(
					String.format("Failed to parse integer value for %s index: '%s'",
							indexType, str),
					lineInd
			);
		}
	}

	private static void checkIndexNotZero(int index, String indexType, int lineInd) {
		if (index == 0) {
			throw new ObjReaderException(
					String.format("%s index cannot be zero (OBJ uses 1-based indexing)", indexType),
					lineInd
			);
		}
	}
}