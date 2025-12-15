package vsu.org.ran.kgandg4.render_engine;

import java.util.Arrays;

public class Zbuffer {
    private float[] buffer;
    private int width, height;

    public Zbuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width * height];
        clear();
    }


    public void clear() {
        Arrays.fill(buffer, Float.POSITIVE_INFINITY);
    }

    public boolean testPointAndSet(int x, int y, float z) {
       int index = y * width + x;
        if (z < buffer[index]) {
            buffer[index] = z;
            return true;
        }
        return false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
