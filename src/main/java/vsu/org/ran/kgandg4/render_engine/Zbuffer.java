package vsu.org.ran.kgandg4.render_engine;

import java.util.Arrays;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

@Component
public class Zbuffer {
    private float[] buffer;
    private int width, height;

    public Zbuffer() {
    }

    public void resize(int newWidth, int newHeight) {
        if (width != newWidth || height != newHeight) {
            this.width = newWidth;
            this.height = newHeight;
            this.buffer = new float[width * height];
            clear();
        }
    }


    public void clear() {
        if (buffer != null) {
            Arrays.fill(buffer, Float.POSITIVE_INFINITY);
        }
    }

    public boolean testPointAndSet(int x, int y, float z) {
        if (buffer == null || x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

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

    public float readDepth(double x, double y) {
        int intX = (int)Math.floor(x);
        int intY = (int)Math.floor(y);

        if (intX < 0 || intY < 0 || intX >= width || intY >= height) {
            return Float.POSITIVE_INFINITY;
        }

        float z = buffer[intY * width + intX];

        if (z >= Float.POSITIVE_INFINITY) {
            return 1.0f;
        }

        float normalizedZ = (z + 1.0f) / 2.0f; // [-1,1] → [0,1]

        return Math.max(0, Math.min(1, normalizedZ));
    }
}
