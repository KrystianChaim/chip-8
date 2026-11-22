package com.chip8.ui;

import com.chip8.core.Chip8Observer;
import com.chip8.core.Cpu;
import com.chip8.core.CpuState;
import com.chip8.core.Display;
import com.chip8.core.Keypad;
import com.chip8.core.Memory;
import com.chip8.core.SamplePrograms;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class DebuggerFrame extends JFrame implements Chip8Observer {
    private static final int CYCLES_PER_TICK = 4;

    private final Memory memory = new Memory();
    private final Display display = new Display();
    private final Keypad keypad = new Keypad();
    private final Cpu cpu = new Cpu(memory, display, keypad);

    private final DisplayCanvas displayCanvas = new DisplayCanvas(display);
    private final JLabel[] registerLabels = new JLabel[16];
    private final JLabel pcLabel = new JLabel();
    private final JLabel indexLabel = new JLabel();
    private final JLabel spLabel = new JLabel();
    private final JLabel timersLabel = new JLabel();
    private final JLabel statusLabel = new JLabel("Status: Paused");

    private final Map<String, byte[]> samplePrograms = SamplePrograms.getBuiltInPrograms();
    private final Timer executionTimer;
    private CpuState savedSnapshot;
    private byte[] currentRom;

    public DebuggerFrame() {
        super("Virtual Machine CHIP-8");
        cpu.addObserver(this);

        executionTimer = new Timer(16, e -> executeFrameTick());
        setupLayout();
        setupKeyboardListener();

        String firstProgram = samplePrograms.keySet().iterator().next();
        loadRomBytes(samplePrograms.get(firstProgram), firstProgram);
        refreshInspector();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        add(createTopDisplayPanel(), BorderLayout.NORTH);
        add(createBottomWorkspacePanel(), BorderLayout.CENTER);
    }

    private JPanel createTopDisplayPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(displayCanvas);
        return topPanel;
    }

    private JPanel createBottomWorkspacePanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        bottomPanel.add(createBottomLeftControlsPanel(), BorderLayout.WEST);
        bottomPanel.add(createBottomRightInspectorPanel(), BorderLayout.CENTER);
        return bottomPanel;
    }

    private JPanel createBottomLeftControlsPanel() {
        JPanel stackedPanels = new JPanel();
        stackedPanels.setLayout(new BoxLayout(stackedPanels, BoxLayout.Y_AXIS));
        stackedPanels.add(createProgramSelectionPanel());
        stackedPanels.add(Box.createVerticalStrut(8));
        stackedPanels.add(createExecutionControlPanel());
        return stackedPanels;
    }

    private JPanel createBottomRightInspectorPanel() {
        JPanel rightPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        rightPanel.add(createRegistersPanel());
        rightPanel.add(createKeypadPanel());
        return rightPanel;
    }

    private JPanel createProgramSelectionPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Program & ROM"));

        JComboBox<String> selector = new JComboBox<>(samplePrograms.keySet().toArray(new String[0]));

        JButton loadBtn = new JButton("Load Sample");
        loadBtn.addActionListener(e -> {
            String selected = (String) selector.getSelectedItem();
            if (selected != null) {
                loadRomBytes(samplePrograms.get(selected), selected);
            }
        });

        JButton openBtn = new JButton("Open .ch8 File...");
        openBtn.addActionListener(e -> openExternalRomFile());

        panel.add(selector);
        panel.add(loadBtn);
        panel.add(openBtn);
        return panel;
    }

    private JPanel createExecutionControlPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Execution & State"));

        JButton runBtn = new JButton("Run");
        runBtn.addActionListener(e -> {
            executionTimer.start();
            statusLabel.setText("Status: Running");
        });

        JButton pauseBtn = new JButton("Pause");
        pauseBtn.addActionListener(e -> {
            executionTimer.stop();
            statusLabel.setText("Status: Paused");
            refreshInspector();
        });

        JButton stepBtn = new JButton("Step");
        stepBtn.addActionListener(e -> {
            executionTimer.stop();
            statusLabel.setText("Status: Single Step");
            executeSingleStep();
        });

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            if (currentRom != null) {
                loadRomBytes(currentRom, "Current ROM");
            }
        });

        JButton saveBtn = new JButton("Save Snapshot");
        saveBtn.addActionListener(e -> {
            savedSnapshot = cpu.createSnapshot();
            statusLabel.setText("Status: Snapshot Saved");
        });

        JButton restoreBtn = new JButton("Restore Snapshot");
        restoreBtn.addActionListener(e -> restoreSavedSnapshot());

        panel.add(runBtn);
        panel.add(pauseBtn);
        panel.add(stepBtn);
        panel.add(resetBtn);
        panel.add(saveBtn);
        panel.add(restoreBtn);
        return panel;
    }

    private void restoreSavedSnapshot() {
        if (savedSnapshot != null) {
            cpu.restoreSnapshot(savedSnapshot);
            refreshInspector();
            displayCanvas.render();
            statusLabel.setText("Status: Snapshot Restored");
        }
    }

    private JPanel createRegistersPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 2, 6, 3));
        panel.setBorder(BorderFactory.createTitledBorder("CPU Registers & State"));
        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);

        for (int i = 0; i < 16; i++) {
            registerLabels[i] = new JLabel();
            registerLabels[i].setFont(monoFont);
            panel.add(registerLabels[i]);
        }

        pcLabel.setFont(monoFont);
        indexLabel.setFont(monoFont);
        spLabel.setFont(monoFont);
        timersLabel.setFont(monoFont);
        statusLabel.setFont(monoFont);

        panel.add(pcLabel);
        panel.add(indexLabel);
        panel.add(spLabel);
        panel.add(timersLabel);
        panel.add(statusLabel);
        return panel;
    }

    private JPanel createKeypadPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 4, 4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Hex Keypad (1-4 / Q-R / A-F / Z-V)"));

        int[] keyLayout = {
                0x1, 0x2, 0x3, 0xC,
                0x4, 0x5, 0x6, 0xD,
                0x7, 0x8, 0x9, 0xE,
                0xA, 0x0, 0xB, 0xF
        };

        for (int keyCode : keyLayout) {
            JButton keyBtn = new JButton(String.format("%X", keyCode));
            keyBtn.setFocusable(false);
            keyBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    keypad.setKeyState(keyCode, true);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    keypad.setKeyState(keyCode, false);
                }
            });
            panel.add(keyBtn);
        }
        return panel;
    }

    private void setupKeyboardListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                int chip8Key = mapHostKeyToChip8(e.getKeyCode());
                if (chip8Key != -1) {
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        keypad.setKeyState(chip8Key, true);
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        keypad.setKeyState(chip8Key, false);
                    }
                }
                return false;
            }
        });
    }

    private int mapHostKeyToChip8(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_1 -> 0x1;
            case KeyEvent.VK_2 -> 0x2;
            case KeyEvent.VK_3 -> 0x3;
            case KeyEvent.VK_4 -> 0xC;
            case KeyEvent.VK_Q -> 0x4;
            case KeyEvent.VK_W -> 0x5;
            case KeyEvent.VK_E -> 0x6;
            case KeyEvent.VK_R -> 0xD;
            case KeyEvent.VK_A -> 0x7;
            case KeyEvent.VK_S -> 0x8;
            case KeyEvent.VK_D -> 0x9;
            case KeyEvent.VK_F -> 0xE;
            case KeyEvent.VK_Z -> 0xA;
            case KeyEvent.VK_X -> 0x0;
            case KeyEvent.VK_C -> 0xB;
            case KeyEvent.VK_V -> 0xF;
            default -> -1;
        };
    }

    private void openExternalRomFile() {
        executionTimer.stop();
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] data = Files.readAllBytes(file.toPath());
                loadRomBytes(data, file.getName());
            } catch (IOException | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "ROM Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadRomBytes(byte[] romData, String title) {
        executionTimer.stop();
        currentRom = romData;
        memory.reset();
        display.clear();
        keypad.reset();
        cpu.reset();
        memory.loadRom(romData);
        savedSnapshot = cpu.createSnapshot();
        displayCanvas.render();
        refreshInspector();
        statusLabel.setText("Loaded: " + title);
    }

    private void executeFrameTick() {
        try {
            for (int i = 0; i < CYCLES_PER_TICK; i++) {
                cpu.step();
            }
            cpu.tickTimers();
            refreshInspector();
        } catch (RuntimeException ex) {
            executionTimer.stop();
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executeSingleStep() {
        try {
            cpu.step();
            cpu.tickTimers();
            refreshInspector();
        } catch (RuntimeException ex) {
            statusLabel.setText("Error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshInspector() {
        for (int i = 0; i < 16; i++) {
            int val = cpu.getRegister(i);
            registerLabels[i].setText(String.format("V%X: 0x%02X (%3d)", i, val, val));
        }
        pcLabel.setText(String.format("PC: 0x%03X", cpu.getProgramCounter()));
        indexLabel.setText(String.format("I : 0x%03X", cpu.getIndexRegister()));
        spLabel.setText(String.format("SP: %d", cpu.getStackPointer()));
        timersLabel.setText(String.format("DT:%d ST:%d", cpu.getDelayTimer(), cpu.getSoundTimer()));
    }

    @Override
    public void onScreenUpdated() {
        displayCanvas.render();
    }

    @Override
    public void onSoundTriggered() {
        Toolkit.getDefaultToolkit().beep();
    }
}