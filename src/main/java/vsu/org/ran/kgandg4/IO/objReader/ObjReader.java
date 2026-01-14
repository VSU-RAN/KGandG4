package vsu.org.ran.kgandg4.IO.objReader;



import math.vector.Vector2f;
import math.vector.Vector3f;
import vsu.org.ran.kgandg4.model.models.Model;
import vsu.org.ran.kgandg4.model.models.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";
	private static final String OBJ_COMMENT_TOKEN = "#";

	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine().trim();
			if (line.isEmpty() || line.startsWith(OBJ_COMMENT_TOKEN)) {
				lineInd++;
				continue;
			}

			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				lineInd++;
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				case OBJ_VERTEX_TOKEN -> {
					if (wordsInLine.size() >= 3) {
						result.vertices.add(parseVertex(wordsInLine, lineInd));
					} else {
						throw new ObjReaderException("Слишком мало аргументов для вершины.", lineInd);
					}
				}
				case OBJ_TEXTURE_TOKEN -> {
					if (wordsInLine.size() >= 2) {
						result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
					} else {
						throw new ObjReaderException("Слишком мало аргументов для текстурной вершины.", lineInd);
					}
				}
				case OBJ_NORMAL_TOKEN -> {
					if (wordsInLine.size() >= 3) {
						result.normals.add(parseNormal(wordsInLine, lineInd));
					} else {
						throw new ObjReaderException("Слишком мало аргументов для нормали.", lineInd);
					}
				}
				case OBJ_FACE_TOKEN -> {
					if (wordsInLine.size() >= 3) {
						result.polygons.add(parseFace(wordsInLine, lineInd));
					} else {
						throw new ObjReaderException("Слишком мало аргументов для полигона.", lineInd);
					}
				}
				default -> {
					// Игнорируем неизвестные токены
				}
			}
		}
		scanner.close();

		return result;
	}

	public static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

			// Дополнительные координаты w (если есть) игнорируем
			return new Vector3f(x, y, z);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось преобразовать значение в число с плавающей точкой.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Слишком мало аргументов для вершины.", lineInd);
		}
	}

	public static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			float u = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float v = Float.parseFloat(wordsInLineWithoutToken.get(1));

			// Дополнительная координата w (если есть) игнорируем
			return new Vector2f(u, v);

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось преобразовать значение в число с плавающей точкой.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Слишком мало аргументов для текстурной вершины.", lineInd);
		}
	}

	public static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			float x = Float.parseFloat(wordsInLineWithoutToken.get(0));
			float y = Float.parseFloat(wordsInLineWithoutToken.get(1));
			float z = Float.parseFloat(wordsInLineWithoutToken.get(2));

			// Нормализуем нормаль
			Vector3f normal = new Vector3f(x, y, z);
			float length = (float) Math.sqrt(x*x + y*y + z*z);
			if (length > 0) {
				normal = new Vector3f(x/length, y/length, z/length);
			}

			return normal;

		} catch(NumberFormatException e) {
			throw new ObjReaderException("Не удалось преобразовать значение в число с плавающей точкой.", lineInd);

		} catch(IndexOutOfBoundsException e) {
			throw new ObjReaderException("Слишком мало аргументов для нормали.", lineInd);
		}
	}

	public static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		ArrayList<Integer> vertexIndices = new ArrayList<>();
		ArrayList<Integer> textureIndices = new ArrayList<>();
		ArrayList<Integer> normalIndices = new ArrayList<>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, vertexIndices, textureIndices, normalIndices, lineInd);
		}

		// Проверка на минимальное количество вершин
		if (vertexIndices.size() < 3) {
			throw new ObjReaderException("Полигон должен иметь как минимум 3 вершины.", lineInd);
		}

		Polygon result = new Polygon();
		result.setVertexIndices(vertexIndices);
		result.setTextureVertexIndices(textureIndices);
		result.setNormalIndices(normalIndices);
		return result;
	}

	public static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> vertexIndices,
			ArrayList<Integer> textureIndices,
			ArrayList<Integer> normalIndices,
			int lineInd) {

		if (wordInLine == null || wordInLine.trim().isEmpty()) {
			throw new ObjReaderException("Пустой элемент полигона.", lineInd);
		}

		try {
			String[] parts = wordInLine.split("/");

			// Обработка разных форматов
			switch (parts.length) {
				case 1: // только вершина: f v1 v2 v3
					vertexIndices.add(parseIndex(parts[0], lineInd) - 1);
					break;

				case 2: // вершина и текстура: f v1/vt1 v2/vt2 v3/vt3
					vertexIndices.add(parseIndex(parts[0], lineInd) - 1);
					if (!parts[1].isEmpty()) {
						textureIndices.add(parseIndex(parts[1], lineInd) - 1);
					}
					break;

				case 3: // возможны два варианта:
					// f v1//vn1 v2//vn2 v3//vn3 (вершина и нормаль)
					// f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3 (вершина, текстура, нормаль)
					vertexIndices.add(parseIndex(parts[0], lineInd) - 1);

					if (!parts[1].isEmpty()) {
						textureIndices.add(parseIndex(parts[1], lineInd) - 1);
					}

					if (!parts[2].isEmpty()) {
						normalIndices.add(parseIndex(parts[2], lineInd) - 1);
					}
					break;

				default:
					throw new ObjReaderException("Неверный формат элемента полигона: " + wordInLine, lineInd);
			}

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Не удалось преобразовать целое число в элементе полигона: " + wordInLine, lineInd);
		}
	}

	private static int parseIndex(String str, int lineInd) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Неверный индекс: " + str, lineInd);
		}
	}
}