package vsu.org.ran.kgandg4.IO.objReader;

public class ObjReaderException extends RuntimeException {
    public ObjReaderException(String errorMessage, int lineInd) {
        super("Error parsing OBJ file on line: " + lineInd + ". " + errorMessage);
    }

    // Статические фабричные методы для удобства
    public static ObjReaderException indexOutOfBounds(String elementType, int index, int min, int max, int lineInd) {
        return new ObjReaderException(
                String.format("%s: index %d out of bounds [%d, %d]",
                        elementType, index, min, max),
                lineInd
        );
    }

    public static ObjReaderException countMismatch(String elementType, int count1, int count2, int lineInd) {
        return new ObjReaderException(
                String.format("%s: count mismatch (%d vs %d)",
                        elementType, count1, count2),
                lineInd
        );
    }
}