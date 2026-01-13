package vsu.org.ran.kgandg4.render_engine;

import vsu.org.ran.kgandg4.dependecyIndjection.annotations.Component;

import java.util.Arrays;

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

    public Zbuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new float[width * height];
        clear();
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
}
