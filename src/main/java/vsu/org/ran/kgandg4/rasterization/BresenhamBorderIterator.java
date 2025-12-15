package vsu.org.ran.kgandg4.rasterization;


import java.util.NoSuchElementException;

import static java.lang.Math.abs;

/**
 * Реализация BorderIterator. Позволяет создавать итерационный путь из точек, следуя из первой во вторую.
 * Путь строится алгоритмом Брезенхейма и включает в себя все положения, в которых может находиться линия.
 */
public class BresenhamBorderIterator implements BorderIterator {
    private final int x0;
    private final int y0;
    private final int x1;
    private final int y1;
    private final int dx;
    private final int dy;
    private int stepX;
    private int stepY;
    private final boolean isScanlineOnX;

    private int currentX;
    private int currentY;
    private int error;

    public BresenhamBorderIterator(int x0, int y0, int x1, int y1, boolean scanlineOnX) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;

        dx = abs(x0 - x1);
        dy = abs(y0 - y1);

        currentX = x0;
        currentY = y0;
        error = 0;
        stepX = 1;
        stepY = 1;
        if (x1 - x0 < 0) {
            stepX *= -1;
        }
        if (y1 - y0 < 0) {
            stepY *= -1;
        }
        isScanlineOnX = scanlineOnX;
    }

    @Override
    public int getX() {
        return currentX;
    }

    @Override
    public int getY() {
        return currentY;
    }

    public void next() {
        if (!hasNext()) throw new NoSuchElementException();
        if (isScanlineOnX) {
            if (dx > dy) {
                boolean wasChange = false;
                while (!wasChange) {
                    error += 2 * dy;
                    if (error > dx) {
                        currentY += stepY;
                        error = -(2 * dx - error);
                        wasChange = true;
                    }
                    currentX += stepX;
                }
            } else {
                error += 2 * dx;
                if (error > dy) {
                    currentX += stepX;
                    error = -(2 * dy - error);
                }
                currentY += stepY;
            }
        } else {
            if (dx > dy) {
                error += 2 * dy;
                if (error > dx) {
                    currentY += stepY;
                    error = -(2 * dx - error);
                }
                currentX += stepX;
            } else {
                boolean wasChange = false;
                while (!wasChange) {
                    error += 2 * dx;
                    if (error > dy) {
                        currentX += stepX;
                        error = -(2 * dy - error);
                        wasChange = true;
                    }
                    currentY += stepY;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        if (isScanlineOnX) return y1 != currentY;
        return currentX != x1;
    }
}