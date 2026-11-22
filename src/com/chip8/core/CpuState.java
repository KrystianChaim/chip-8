package com.chip8.core;

import java.util.Arrays;

public record CpuState(
        int[] registers,
        int indexRegister,
        int programCounter,
        int[] stack,
        int stackPointer,
        int delayTimer,
        int soundTimer
) {
    public CpuState {
        registers = Arrays.copyOf(registers, registers.length);
        stack = Arrays.copyOf(stack, stack.length);
    }

    @Override
    public int[] registers() {
        return Arrays.copyOf(registers, registers.length);
    }

    @Override
    public int[] stack() {
        return Arrays.copyOf(stack, stack.length);
    }
}