package com.chip8.core;

import java.util.Arrays;

public class Memory {
    public static final int RAM_SIZE = 4096;
    public static final int PROGRAM_START_ADDRESS = 0x200;
    public static final int FONTSET_START_ADDRESS = 0x050;
    public static final int MAX_ROM_SIZE = RAM_SIZE - PROGRAM_START_ADDRESS;

    private final byte[] ram = new byte[RAM_SIZE];

    public Memory() {
        reset();
    }

    public void reset() {
        Arrays.fill(ram, (byte) 0);
        loadFontset();
    }

    public int read(int address) {
        validateAddress(address);
        return ram[address] & 0xFF;
    }

    public void write(int address, int value) {
        validateAddress(address);
        ram[address] = (byte) (value & 0xFF);
    }

    public void loadRom(byte[] romData) {
        if (romData == null) {
            throw new IllegalArgumentException("ROM data cannot be null.");
        }
        if (romData.length > MAX_ROM_SIZE) {
            throw new IllegalArgumentException("ROM size exceeds 3584 bytes limit.");
        }
        Arrays.fill(ram, PROGRAM_START_ADDRESS, RAM_SIZE, (byte) 0);
        System.arraycopy(romData, 0, ram, PROGRAM_START_ADDRESS, romData.length);
    }

    private void validateAddress(int address) {
        if (address < 0 || address >= RAM_SIZE) {
            throw new IndexOutOfBoundsException("Memory address out of bounds: " + address);
        }
    }

    private void loadFontset() {
        int[] fontset = {
                0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
                0x20, 0x60, 0x20, 0x20, 0x70, // 1
                0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
                0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
                0x90, 0x90, 0xF0, 0x10, 0x10, // 4
                0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
                0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
                0xF0, 0x10, 0x20, 0x40, 0x40, // 7
                0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
                0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
                0xF0, 0x90, 0xF0, 0x90, 0x90, // A
                0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
                0xF0, 0x80, 0x80, 0x80, 0xF0, // C
                0xE0, 0x90, 0x90, 0x90, 0xE0, // D
                0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
                0xF0, 0x80, 0xF0, 0x80, 0x80  // F
        };
        for (int i = 0; i < fontset.length; i++) {
            ram[FONTSET_START_ADDRESS + i] = (byte) fontset[i];
        }
    }
}