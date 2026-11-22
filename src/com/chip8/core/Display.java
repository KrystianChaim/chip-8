package com.chip8.core;

public class Display {
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;

    private final boolean[][] pixels = new boolean[HEIGHT][WIDTH];

    public void clear() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pixels[y][x] = false;
            }
        }
    }

    public boolean getPixel(int x, int y) {
        int wrappedX = Math.floorMod(x, WIDTH);
        int wrappedY = Math.floorMod(y, HEIGHT);
        return pixels[wrappedY][wrappedX];
    }

    public boolean togglePixel(int x, int y) {
        int wrappedX = Math.floorMod(x, WIDTH);
        int wrappedY = Math.floorMod(y, HEIGHT);
        boolean collision = pixels[wrappedY][wrappedX];
        pixels[wrappedY][wrappedX] = !pixels[wrappedY][wrappedX];
        return collision;
    }
}