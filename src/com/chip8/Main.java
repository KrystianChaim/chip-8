package com.chip8;

import com.chip8.ui.DebuggerFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DebuggerFrame frame = new DebuggerFrame();
            frame.setVisible(true);
        });
    }
}