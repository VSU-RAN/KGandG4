package vsu.org.ran.kgandg4.triangulation.utils;

public enum TriangulationType {
    SIMPLE("Простая"),
    EAR_CUTTING("Отсечение ушей");

    private final String desc;

    TriangulationType(String desc) {
        this.desc = desc;

    }

    @Override
    public String toString() {
        return desc;
    }
}
