package com.chip8.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SamplePrograms {
    private SamplePrograms() {
    }

    public static Map<String, byte[]> getBuiltInPrograms() {
        Map<String, byte[]> programs = new LinkedHashMap<>();
        programs.put("Bouncing Square (Animation)", createBouncingSquareRom());
        programs.put("Fontset Showcase (HEX Digits)", createFontShowcaseRom());
        programs.put("Arithmetic Loop (Sum 1..5 = 15)", createSumLoopRom());
        return programs;
    }

    private static byte[] createBouncingSquareRom() {
        int[] opcodes = {
                0x60, 0x05, // 0x200: LD V0, 5 (X)
                0x61, 0x05, // 0x202: LD V1, 5 (Y)
                0x62, 0x01, // 0x204: LD V2, 1 (DX)
                0x63, 0x01, // 0x206: LD V3, 1 (DY)
                0x00, 0xE0, // 0x208: CLS
                0xA2, 0x26, // 0x20A: LD I, 0x226
                0xD0, 0x14, // 0x20C: DRW V0, V1, 4
                0x80, 0x24, // 0x20E: ADD V0, V2
                0x81, 0x34, // 0x210: ADD V1, V3
                0x40, 0x3C, // 0x212: SNE V0, 60
                0x62, 0xFF, // 0x214: LD V2, -1
                0x40, 0x00, // 0x216: SNE V0, 0
                0x62, 0x01, // 0x218: LD V2, 1
                0x41, 0x1C, // 0x21A: SNE V1, 28
                0x63, 0xFF, // 0x21C: LD V3, -1
                0x41, 0x00, // 0x21E: SNE V1, 0
                0x63, 0x01, // 0x220: LD V3, 1
                0x12, 0x08, // 0x222: JP 0x208
                0x00, 0x00, // 0x224: Padding
                0xF0, 0x90, 0x90, 0xF0 // 0x226: 4x4 Square Sprite
        };
        return toByteArray(opcodes);
    }

    private static byte[] createFontShowcaseRom() {
        int[] opcodes = {
                0x00, 0xE0, // CLS
                0x60, 0x04, // V0 = X = 4
                0x61, 0x0C, // V1 = Y = 12
                0x62, 0x0C, // Digit 'C'
                0xF2, 0x29, // LD F, V2
                0xD0, 0x15, // DRW V0, V1, 5
                0x70, 0x06, // X += 6
                0x62, 0x08, // Digit '8'
                0xF2, 0x29, // LD F, V2
                0xD0, 0x15, // DRW V0, V1, 5
                0x12, 0x14  // JP 0x214 (Halt)
        };
        return toByteArray(opcodes);
    }

    private static byte[] createSumLoopRom() {
        int[] opcodes = {
                0x60, 0x00, // 0x200: LD V0, 0 (accumulator)
                0x61, 0x05, // 0x202: LD V1, 5 (counter)
                0x80, 0x14, // 0x204: ADD V0, V1
                0x71, 0xFF, // 0x206: ADD V1, -1
                0x31, 0x00, // 0x208: SE V1, 0
                0x12, 0x04, // 0x20A: JP 0x204
                0x12, 0x0C  // 0x20C: JP 0x20C (Halt, V0 == 15)
        };
        return toByteArray(opcodes);
    }

    private static byte[] toByteArray(int[] values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) (values[i] & 0xFF);
        }
        return result;
    }
}