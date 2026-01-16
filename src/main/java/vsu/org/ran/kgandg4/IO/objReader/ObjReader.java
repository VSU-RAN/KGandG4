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

	private static final int MIN_VERTEX_COMPONENTS = 3;
	private static final int MIN_TEXTURE_COMPONENTS = 2;
	private static final int MIN_NORMAL_COMPONENTS = 3;
	private static final int MIN_FACE_VERTICES = 3;

	private static int vertexCount = 0;
	private static int textureCount = 0;
	private static int normalCount = 0;

	public static Model read(String fileContent) {
		Model currentModel = new Model();
		vertexCount = 0;
		textureCount = 0;
		normalCount = 0;

		int lineInd = 0;

		// Проверка на пустой файл
		if (fileContent == null || fileContent.trim().isEmpty()) {
			throw new ObjReaderException("Содержимое файла пустое или null", 0);
		}

		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().trim();
			lineInd++;

			// Удаляем комментарии в конце строки
			int commentIndex = line.indexOf('#');
			String cleanLine = line;
			if (commentIndex != -1) {
				cleanLine = line.substring(0, commentIndex).trim();
			}

			if (cleanLine.isEmpty() || cleanLine.startsWith(OBJ_COMMENT_TOKEN)) {
				continue;
			}

			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(cleanLine.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			// Проверка на пустые данные после токена
			if (wordsInLine.isEmpty() && !token.equals(OBJ_COMMENT_TOKEN)) {
				throw new ObjReaderException("Отсутствующие данные после токена: " + token, lineInd);
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
					case OBJ_FACE_TOKEN -> {
						Polygon face = parseFace(wordsInLine, lineInd, vertexCount, textureCount, normalCount);
						currentModel.getPolygons().add(face);
					}
					default -> {
						// Игнорируем неизвестные токены
					}
				}
			} catch (ObjReaderException e) {
				throw e;
			} catch (Exception e) {
				throw new ObjReaderException("Непредвиденная ошибка: " + e.getMessage(), lineInd);
			}
		}
		scanner.close();

		// Пост-валидация модели
		validateModel(currentModel, lineInd);

		return currentModel;
	}

	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.size() < MIN_VERTEX_COMPONENTS) {
				throw new ObjReaderException(
						String.format("Слишком мало аргументов для вершин. Ожидал получить как минимум %d, получил %d",
								MIN_VERTEX_COMPONENTS, wordsInLineWithoutToken.size()),
						lineInd
				);
			}

			float x = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "vertex x", lineInd);
			float y = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "vertex y", lineInd);
			float z = parseFloatWithValidation(wordsInLineWithoutToken.get(2), "vertex z", lineInd);

			return new Vector3f(x, y, z);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Недопустимый формат вершин", lineInd);
		}
	}

	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.isEmpty()) {
				throw new ObjReaderException("Текстурная вершина не имеет аргументов", lineInd);
			}

			float u = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "текстура u", lineInd);
			float v = 0.0f;
			if (wordsInLineWithoutToken.size() >= 2) {
				v = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "текстура v", lineInd);
			}

			return new Vector2f(u, v);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Недопустимый формат вершин текстуры", lineInd);
		}
	}

	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			// Проверка на минимальное количество компонентов
			if (wordsInLineWithoutToken.size() < MIN_NORMAL_COMPONENTS) {
				throw new ObjReaderException(
						String.format("Слишком мало нормальных аргументов. Ожидал хотя бы %d, получил %d",
								MIN_NORMAL_COMPONENTS, wordsInLineWithoutToken.size()),
						lineInd
				);
			}

			float x = parseFloatWithValidation(wordsInLineWithoutToken.get(0), "нормаль x", lineInd);
			float y = parseFloatWithValidation(wordsInLineWithoutToken.get(1), "нормаль y", lineInd);
			float z = parseFloatWithValidation(wordsInLineWithoutToken.get(2), "нормаль z", lineInd);

			return new Vector3f(x, y, z);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Недопустимый формат нормалей", lineInd);
		}
	}

	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd,
									   int vertexCount, int textureCount, int normalCount) {
		// Проверка на минимальное количество вершин в полигоне
		if (wordsInLineWithoutToken.size() < MIN_FACE_VERTICES) {
			throw new ObjReaderException(
					String.format("чСлишком мало вершин на грани. Ожидал увидеть как минимум %d, получил % d",
							MIN_FACE_VERTICES, wordsInLineWithoutToken.size()),
					lineInd
			);
		}

		ArrayList<Integer> onePolygonVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonTextureVertexIndices = new ArrayList<>();
		ArrayList<Integer> onePolygonNormalIndices = new ArrayList<>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, onePolygonVertexIndices, onePolygonTextureVertexIndices,
					onePolygonNormalIndices, lineInd, vertexCount, textureCount, normalCount);
		}

		// Проверка согласованности формата внутри полигона
		validateFaceConsistency(onePolygonVertexIndices, onePolygonTextureVertexIndices,
				onePolygonNormalIndices, lineInd);

		Polygon result = new Polygon();
		result.setVertexIndices(onePolygonVertexIndices);
		if (!onePolygonTextureVertexIndices.isEmpty()) {
			result.setTextureVertexIndices(onePolygonTextureVertexIndices);
		}
		if (!onePolygonNormalIndices.isEmpty()) {
			result.setNormalIndices(onePolygonNormalIndices);
		}

		return result;
	}

	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> onePolygonVertexIndices,
			ArrayList<Integer> onePolygonTextureVertexIndices,
			ArrayList<Integer> onePolygonNormalIndices,
			int lineInd,
			int vertexCount,
			int textureCount,
			int normalCount) {
		try {
			String[] wordIndices = wordInLine.split("/");

			switch (wordIndices.length) {
				case 1 -> {
					Integer vertexIdx = parseIndex(wordIndices[0], vertexCount, "вершина", lineInd);
					if (vertexIdx != null) {
						onePolygonVertexIndices.add(vertexIdx);
					}
				}
				case 2 -> {
					Integer vertexIdx = parseIndex(wordIndices[0], vertexCount, "вершина", lineInd);
					Integer textureIdx = parseIndex(wordIndices[1], textureCount, "текстура", lineInd);

					if (vertexIdx != null) {
						onePolygonVertexIndices.add(vertexIdx);
					}
					if (textureIdx != null) {
						onePolygonTextureVertexIndices.add(textureIdx);
					}
				}
				case 3 -> {
					Integer vertexIdx = parseIndex(wordIndices[0], vertexCount, "вершшина", lineInd);
					Integer textureIdx = wordIndices[1].isEmpty() ? null :
							parseIndex(wordIndices[1], textureCount, "текстура", lineInd);
					Integer normalIdx = parseIndex(wordIndices[2], normalCount, "нормаль", lineInd);

					if (vertexIdx != null) {
						onePolygonVertexIndices.add(vertexIdx);
					}
					if (textureIdx != null) {
						onePolygonTextureVertexIndices.add(textureIdx);
					}
					if (normalIdx != null) {
						onePolygonNormalIndices.add(normalIdx);
					}
				}
				default -> {
					throw new ObjReaderException(
							String.format("Недопустимый формат элемента грани: '%s'", wordInLine),
							lineInd
					);
				}
			}

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось проанализировать целочисленное значение в элементе грани", lineInd);
		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Недопустимый формат элемента грани", lineInd);
		}
	}

	private static Integer parseIndex(String str, int totalCount, String elementType, int lineInd) {
		if (str == null || str.isEmpty()) {
			return null;
		}
		try {
			int idx = Integer.parseInt(str);
			if (idx < 0) {
				// Отрицательный индекс - относительная адресация от текущего конца
				int relativeIdx = totalCount + idx;  // idx уже отрицательный
				if (relativeIdx < 0) {
					throw new ObjReaderException(
							String.format("%s индекс %d за пределами [%d, %d]",
									elementType, idx, -totalCount, -1),
							lineInd
					);
				}
				return relativeIdx;
			}

			// Проверка положительного индекса (1-based to 0-based)
			int zeroBasedIdx = idx - 1;
			if (zeroBasedIdx < 0 || zeroBasedIdx >= totalCount) {
				throw new ObjReaderException(
						String.format("%s индекс %d за пределами [1, %d]",
								elementType, idx, totalCount),
						lineInd
				);
			}

			return zeroBasedIdx;
		} catch (NumberFormatException e) {
			throw new ObjReaderException(
					String.format("Не удалось проанализировать целочисленное значение для индекса %s: '%s'",
							elementType, str),
					lineInd
			);
		}
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
					String.format("Количество индексов текстуры (%d) не совпадает с количеством индексов вершин (%d)",
							textureIndices.size(), vertexIndices.size()),
					lineInd
			);
		}

		if (hasNormal && normalIndices.size() != vertexIndices.size()) {
			throw new ObjReaderException(
					String.format("Количество индексов нормалей (%d) не соответствует количеству индексов вершин (%d)",
							normalIndices.size(), vertexIndices.size()),
					lineInd
			);
		}
	}

	private static void validateModel(Model model, int lineInd) {
		// Проверка, что есть вершины, если есть полигоны
		if (!model.getPolygons().isEmpty() && model.getVertices().isEmpty()) {
			throw new ObjReaderException(
					"Модель содержит полигоны, но без вершин",
					lineInd
			);
		}

		// Проверка всех полигонов на корректность индексов
		for (int i = 0; i < model.getPolygons().size(); i++) {
			Polygon poly = model.getPolygons().get(i);
			validatePolygonIndices(poly, i + 1, model, lineInd);
		}
	}

	private static void validatePolygonIndices(Polygon polygon, int polygonIndex, Model model, int lineInd) {
		// Проверка индексов вершин
		for (int vertexIndex : polygon.getVertexIndices()) {
			if (vertexIndex < 0 || vertexIndex >= model.getVertices().size()) {
				throw new ObjReaderException(
						String.format("Полигон %d: индекс вершины %d выходит за предел [0, %d]",
								polygonIndex, vertexIndex, model.getVertices().size() - 1),
						lineInd
				);
			}
		}

		// Проверка индексов текстур
		if (!polygon.getTextureVertexIndices().isEmpty()) {
			for (int texIndex : polygon.getTextureVertexIndices()) {
				if (texIndex < 0 || texIndex >= model.getTextureVertices().size()) {
					throw new ObjReaderException(
							String.format("Полигон %d: индекс текстуры %d выходит за предел [0, %d]",
									polygonIndex, texIndex, model.getTextureVertices().size() - 1),
							lineInd
					);
				}
			}
		}

		// Проверка индексов нормалей
		if (!polygon.getNormalIndices().isEmpty()) {
			for (int normalIndex : polygon.getNormalIndices()) {
				if (normalIndex < 0 || normalIndex >= model.getNormals().size()) {
					throw new ObjReaderException(
							String.format("Полигон %d: индекс нормаля %d выходит за предел [0, %d]",
									polygonIndex, normalIndex, model.getNormals().size() - 1),
							lineInd
					);
				}
			}
		}
	}

	private static float parseFloatWithValidation(String str, String coordinateName, int lineInd) {
		try {
			float value = Float.parseFloat(str);

			if (Float.isNaN(value)) {
				throw new ObjReaderException(
						String.format("координата %s равна NaN", coordinateName),
						lineInd
				);
			}
			if (Float.isInfinite(value)) {
				throw new ObjReaderException(
						String.format("координата %s бесконечна", coordinateName),
						lineInd
				);
			}

			return value;

		} catch (NumberFormatException e) {
			throw new ObjReaderException(
					String.format("Не удалось проанализировать значение с плавающей точкой для %s: '%s'",
							coordinateName, str),
					lineInd
			);
		}
	}
}