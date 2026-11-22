package com.chip8.ui;

import com.chip8.core.Display;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

public class DisplayCanvas extends Canvas {
    private static final int SCALE = 12;
    private static final Color OFF_COLOR = new Color(18, 22, 26);
    private static final Color ON_COLOR = new Color(80, 230, 140);

    private final Display display;

    public DisplayCanvas(Display display) {
        this.display = display;
        setPreferredSize(new Dimension(Display.WIDTH * SCALE, Display.HEIGHT * SCALE));
        setBackground(OFF_COLOR);
        setFocusable(false);
    }

    @Override
    public void paint(Graphics g) {
        drawPixels(g);
    }

    public void render() {
        if (!isDisplayable()) {
            return;
        }
        BufferStrategy strategy = getBufferStrategy();
        if (strategy == null) {
            createBufferStrategy(2);
            return;
        }
        Graphics g = strategy.getDrawGraphics();
        drawPixels(g);
        g.dispose();
        strategy.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawPixels(Graphics g) {
        g.setColor(OFF_COLOR);
        g.fillRect(0, 0, Display.WIDTH * SCALE, Display.HEIGHT * SCALE);

        g.setColor(ON_COLOR);
        for (int y = 0; y < Display.HEIGHT; y++) {
            for (int x = 0; x < Display.WIDTH; x++) {
                if (display.getPixel(x, y)) {
                    g.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
                }
            }
        }
    }
}