package com.chip8.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

public class Cpu {
    private final Memory memory;
    private final Display display;
    private final Keypad keypad;
    private final List<Chip8Observer> observers = new ArrayList<>();
    private final Random random = new Random();

    private final int[] registers = new int[16];
    private final int[] stack = new int[16];
    private int indexRegister;
    private int programCounter;
    private int stackPointer;
    private int delayTimer;
    private int soundTimer;

    public Cpu(Memory memory, Display display, Keypad keypad) {
        this.memory = memory;
        this.display = display;
        this.keypad = keypad;
        reset();
    }

    public void reset() {
        Arrays.fill(registers, 0);
        Arrays.fill(stack, 0);
        indexRegister = 0;
        programCounter = Memory.PROGRAM_START_ADDRESS;
        stackPointer = 0;
        delayTimer = 0;
        soundTimer = 0;
    }

    public void addObserver(Chip8Observer observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(Chip8Observer observer) {
        observers.remove(observer);
    }

    public CpuState createSnapshot() {
        return new CpuState(registers, indexRegister, programCounter, stack, stackPointer, delayTimer, soundTimer);
    }

    public void restoreSnapshot(CpuState snapshot) {
        System.arraycopy(snapshot.registers(), 0, registers, 0, registers.length);
        System.arraycopy(snapshot.stack(), 0, stack, 0, stack.length);
        indexRegister = snapshot.indexRegister();
        programCounter = snapshot.programCounter();
        stackPointer = snapshot.stackPointer();
        delayTimer = snapshot.delayTimer();
        soundTimer = snapshot.soundTimer();
    }

    public void tickTimers() {
        if (delayTimer > 0) {
            delayTimer--;
        }
        if (soundTimer > 0) {
            soundTimer--;
            if (soundTimer == 0) {
                notifySoundTriggered();
            }
        }
    }

    public void step() {
        int opcode = fetchOpcode();
        decodeAndExecute(opcode);
    }

    private int fetchOpcode() {
        int highByte = memory.read(programCounter);
        int lowByte = memory.read(programCounter + 1);
        programCounter = (programCounter + 2) & 0xFFF;
        return (highByte << 8) | lowByte;
    }

    private void decodeAndExecute(int opcode) {
        int nnn = opcode & 0x0FFF;
        int nn = opcode & 0x00FF;
        int n = opcode & 0x000F;
        int x = (opcode & 0x0F00) >>> 8;
        int y = (opcode & 0x00F0) >>> 4;

        switch (opcode & 0xF000) {
            case 0x0000 -> executeSystemOpcode(opcode);
            case 0x1000 -> programCounter = nnn;
            case 0x2000 -> executeCall(nnn);
            case 0x3000 -> skipIfEqual(registers[x], nn);
            case 0x4000 -> skipIfNotEqual(registers[x], nn);
            case 0x5000 -> executeRegisterEqualSkip(x, y, n);
            case 0x6000 -> registers[x] = nn;
            case 0x7000 -> registers[x] = (registers[x] + nn) & 0xFF;
            case 0x8000 -> executeAluOpcode(opcode, x, y);
            case 0x9000 -> executeRegisterNotEqualSkip(x, y, n);
            case 0xA000 -> indexRegister = nnn;
            case 0xB000 -> programCounter = (nnn + registers[0]) & 0xFFF;
            case 0xC000 -> registers[x] = random.nextInt(256) & nn;
            case 0xD000 -> drawSprite(x, y, n);
            case 0xE000 -> executeKeyOpcode(opcode, x, nn);
            case 0xF000 -> executeMiscOpcode(opcode, x, nn);
            default -> throw new IllegalArgumentException("Unknown opcode: " + Integer.toHexString(opcode));
        }
    }

    private void skipIfEqual(int left, int right) {
        if (left == right) {
            programCounter = (programCounter + 2) & 0xFFF;
        }
    }

    private void skipIfNotEqual(int left, int right) {
        if (left != right) {
            programCounter = (programCounter + 2) & 0xFFF;
        }
    }

    private void executeRegisterEqualSkip(int x, int y, int n) {
        if (n != 0) {
            throw new IllegalArgumentException("Invalid 0x5000 opcode");
        }
        skipIfEqual(registers[x], registers[y]);
    }

    private void executeRegisterNotEqualSkip(int x, int y, int n) {
        if (n != 0) {
            throw new IllegalArgumentException("Invalid 0x9000 opcode");
        }
        skipIfNotEqual(registers[x], registers[y]);
    }

    private void executeSystemOpcode(int opcode) {
        if (opcode == 0x00E0) {
            display.clear();
            notifyScreenUpdated();
        } else if (opcode == 0x00EE) {
            if (stackPointer == 0) {
                throw new IllegalStateException("Call stack underflow.");
            }
            programCounter = stack[--stackPointer];
        } else {
            throw new IllegalArgumentException("Unknown system opcode: " + Integer.toHexString(opcode));
        }
    }

    private void executeCall(int address) {
        if (stackPointer >= stack.length) {
            throw new IllegalStateException("Call stack overflow.");
        }
        stack[stackPointer++] = programCounter;
        programCounter = address;
    }

    private void executeAluOpcode(int opcode, int x, int y) {
        switch (opcode & 0x000F) {
            case 0x0 -> registers[x] = registers[y];
            case 0x1 -> registers[x] = (registers[x] | registers[y]) & 0xFF;
            case 0x2 -> registers[x] = (registers[x] & registers[y]) & 0xFF;
            case 0x3 -> registers[x] = (registers[x] ^ registers[y]) & 0xFF;
            case 0x4 -> addWithCarry(x, y);
            case 0x5 -> subtractWithBorrow(x, y);
            case 0x6 -> shiftRight(x);
            case 0x7 -> reverseSubtractWithBorrow(x, y);
            case 0xE -> shiftLeft(x);
            default -> throw new IllegalArgumentException("Unknown ALU opcode: " + Integer.toHexString(opcode));
        }
    }

    private void addWithCarry(int x, int y) {
        int sum = registers[x] + registers[y];
        registers[x] = sum & 0xFF;
        registers[0xF] = (sum > 0xFF) ? 1 : 0;
    }

    private void subtractWithBorrow(int x, int y) {
        int notBorrow = (registers[x] >= registers[y]) ? 1 : 0;
        registers[x] = (registers[x] - registers[y]) & 0xFF;
        registers[0xF] = notBorrow;
    }

    private void reverseSubtractWithBorrow(int x, int y) {
        int notBorrow = (registers[y] >= registers[x]) ? 1 : 0;
        registers[x] = (registers[y] - registers[x]) & 0xFF;
        registers[0xF] = notBorrow;
    }

    private void shiftRight(int x) {
        int lsb = registers[x] & 0x01;
        registers[x] = (registers[x] >>> 1) & 0xFF;
        registers[0xF] = lsb;
    }

    private void shiftLeft(int x) {
        int msb = (registers[x] >>> 7) & 0x01;
        registers[x] = (registers[x] << 1) & 0xFF;
        registers[0xF] = msb;
    }

    private void executeKeyOpcode(int opcode, int x, int nn) {
        int keyIndex = registers[x] & 0x0F;
        if (nn == 0x9E) {
            if (keypad.isKeyPressed(keyIndex)) {
                programCounter = (programCounter + 2) & 0xFFF;
            }
        } else if (nn == 0xA1) {
            if (!keypad.isKeyPressed(keyIndex)) {
                programCounter = (programCounter + 2) & 0xFFF;
            }
        } else {
            throw new IllegalArgumentException("Unknown key opcode: " + Integer.toHexString(opcode));
        }
    }

    private void executeMiscOpcode(int opcode, int x, int nn) {
        switch (nn) {
            case 0x07 -> registers[x] = delayTimer;
            case 0x0A -> waitForKeyPress(x);
            case 0x15 -> delayTimer = registers[x];
            case 0x18 -> soundTimer = registers[x];
            case 0x1E -> indexRegister = (indexRegister + registers[x]) & 0xFFF;
            case 0x29 -> indexRegister = Memory.FONTSET_START_ADDRESS + (registers[x] & 0x0F) * 5;
            case 0x33 -> storeBcdRepresentation(x);
            case 0x55 -> dumpRegistersToMemory(x);
            case 0x65 -> loadRegistersFromMemory(x);
            default -> throw new IllegalArgumentException("Unknown misc opcode: " + Integer.toHexString(opcode));
        }
    }

    private void waitForKeyPress(int x) {
        OptionalInt key = keypad.getFirstPressedKey();
        if (key.isPresent()) {
            registers[x] = key.getAsInt();
        } else {
            programCounter = (programCounter - 2) & 0xFFF;
        }
    }

    private void storeBcdRepresentation(int x) {
        int val = registers[x];
        memory.write(indexRegister, val / 100);
        memory.write(indexRegister + 1, (val / 10) % 10);
        memory.write(indexRegister + 2, val % 10);
    }

    private void dumpRegistersToMemory(int maxReg) {
        for (int i = 0; i <= maxReg; i++) {
            memory.write(indexRegister + i, registers[i]);
        }
    }

    private void loadRegistersFromMemory(int maxReg) {
        for (int i = 0; i <= maxReg; i++) {
            registers[i] = memory.read(indexRegister + i);
        }
    }

    private void drawSprite(int xReg, int yReg, int height) {
        int startX = registers[xReg];
        int startY = registers[yReg];
        registers[0xF] = 0;

        for (int row = 0; row < height; row++) {
            int spriteByte = memory.read(indexRegister + row);
            for (int col = 0; col < 8; col++) {
                if ((spriteByte & (0x80 >>> col)) != 0) {
                    if (display.togglePixel(startX + col, startY + row)) {
                        registers[0xF] = 1;
                    }
                }
            }
        }
        notifyScreenUpdated();
    }

    private void notifyScreenUpdated() {
        for (Chip8Observer observer : observers) {
            observer.onScreenUpdated();
        }
    }

    private void notifySoundTriggered() {
        for (Chip8Observer observer : observers) {
            observer.onSoundTriggered();
        }
    }

    public int getRegister(int index) {
        if (index < 0 || index >= 16) {
            throw new IndexOutOfBoundsException("Register index out of bounds: " + index);
        }
        return registers[index];
    }

    public void setRegister(int index, int value) {
        if (index < 0 || index >= 16) {
            throw new IndexOutOfBoundsException("Register index out of bounds: " + index);
        }
        registers[index] = value & 0xFF;
    }

    public int getIndexRegister() {
        return indexRegister;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public int getDelayTimer() {
        return delayTimer;
    }

    public int getSoundTimer() {
        return soundTimer;
    }
}