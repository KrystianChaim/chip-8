package com.chip8.core;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SamplePrograms {
    private SamplePrograms() {
    }

    public static Map<String, byte[]> getBuiltInPrograms() {
        Map<String, byte[]> programs = new LinkedHashMap<>();
        programs.put("Mini-Game: Catch the Star (Keys 4 / 6)", createCatchStarGameRom());
        programs.put("Pixel Painter (2=Up, 8=Down, 4=L, 6=R, 0=Cls)", createPixelPainterRom());
        programs.put("Keypad HEX Echo (Press Any Key 0-F)", createKeypadEchoRom());
        programs.put("Bouncing Square (Animation)", createBouncingSquareRom());
        programs.put("Fontset Showcase (HEX Digits)", createFontShowcaseRom());
        programs.put("Arithmetic Loop (Sum 1..5 = 15)", createSumLoopRom());
        return programs;
    }

    private static byte[] createCatchStarGameRom() {
        int[] opcodes = {
                0x60, 0x1C, // 0x200: LD V0, 28 (Paddle X)
                0x61, 0x1E, // 0x202: LD V1, 30 (Paddle Y)
                0x65, 0x00, // 0x204: LD V5, 0  (Score counter)
                0xC2, 0x3C, // 0x206: RND V2, 0x3C (Random Star X: 0..60)
                0x63, 0x00, // 0x208: LD V3, 0  (Star Y = 0)
                0x00, 0xE0, // 0x20A: CLS
                0x64, 0x04, // 0x20C: LD V4, 4  (Key 4 = Left)
                0xE4, 0xA1, // 0x20E: SKNP V4
                0x70, 0xFE, // 0x210: ADD V0, -2
                0x64, 0x06, // 0x212: LD V4, 6  (Key 6 = Right)
                0xE4, 0xA1, // 0x214: SKNP V4
                0x70, 0x02, // 0x216: ADD V0, 2
                0xA2, 0x40, // 0x218: LD I, 0x240 (Paddle sprite)
                0xD0, 0x11, // 0x21A: DRW V0, V1, 1
                0xA2, 0x42, // 0x21C: LD I, 0x242 (Star sprite)
                0xD2, 0x32, // 0x21E: DRW V2, V3, 2 (Sets VF=1 if caught)
                0x3F, 0x01, // 0x220: SE VF, 1
                0x12, 0x2C, // 0x222: JP 0x22C
                0x75, 0x01, // 0x224: ADD V5, 1 (Increment score)
                0x64, 0x04, // 0x226: LD V4, 4
                0xF4, 0x18, // 0x228: LD ST, V4 (Beep)
                0x12, 0x06, // 0x22A: JP 0x206 (Spawn next star)
                0x73, 0x01, // 0x22C: ADD V3, 1 (Move star down)
                0x33, 0x20, // 0x22E: SE V3, 32
                0x12, 0x34, // 0x230: JP 0x234
                0x12, 0x06, // 0x232: JP 0x206 (Missed -> spawn next star)
                0x64, 0x02, // 0x234: LD V4, 2 (Frame delay)
                0xF4, 0x15, // 0x236: LD DT, V4
                0xF4, 0x07, // 0x238: LD V4, DT
                0x34, 0x00, // 0x23A: SE V4, 0
                0x12, 0x38, // 0x23C: JP 0x238
                0x12, 0x0A, // 0x23E: JP 0x20A
                0xFF, 0x00, // 0x240: 8x1 Paddle sprite + padding
                0xC0, 0xC0  // 0x242: 2x2 Star sprite
        };
        return toByteArray(opcodes);
    }

    private static byte[] createPixelPainterRom() {
        int[] opcodes = {
                0x00, 0xE0, // 0x200: CLS
                0x60, 0x20, // 0x202: LD V0, 32 (Start X)
                0x61, 0x10, // 0x204: LD V1, 16 (Start Y)
                0xA2, 0x42, // 0x206: LD I, 0x242 (1x1 pixel sprite)
                0xD0, 0x11, // 0x208: DRW V0, V1, 1
                0x83, 0x00, // 0x20A: LD V3, V0 (Save previous X)
                0x84, 0x10, // 0x20C: LD V4, V1 (Save previous Y)
                0x62, 0x02, // 0x20E: LD V2, 2  (Up)
                0xE2, 0xA1, // 0x210: SKNP V2
                0x71, 0xFF, // 0x212: ADD V1, -1
                0x62, 0x08, // 0x214: LD V2, 8  (Down)
                0xE2, 0xA1, // 0x216: SKNP V2
                0x71, 0x01, // 0x218: ADD V1, 1
                0x62, 0x04, // 0x21A: LD V2, 4  (Left)
                0xE2, 0xA1, // 0x21C: SKNP V2
                0x70, 0xFF, // 0x21E: ADD V0, -1
                0x62, 0x06, // 0x220: LD V2, 6  (Right)
                0xE2, 0xA1, // 0x222: SKNP V2
                0x70, 0x01, // 0x224: ADD V0, 1
                0x62, 0x00, // 0x226: LD V2, 0  (Clear)
                0xE2, 0xA1, // 0x228: SKNP V2
                0x00, 0xE0, // 0x22A: CLS
                0x90, 0x30, // 0x22C: SNE V0, V3
                0x51, 0x40, // 0x22E: SE V1, V4
                0x12, 0x34, // 0x230: JP 0x234
                0x12, 0x0A, // 0x232: JP 0x20A
                0xD0, 0x11, // 0x234: DRW V0, V1, 1
                0x62, 0x03, // 0x236: LD V2, 3
                0xF2, 0x15, // 0x238: LD DT, V2
                0xF2, 0x07, // 0x23A: LD V2, DT
                0x32, 0x00, // 0x23C: SE V2, 0
                0x12, 0x3A, // 0x23E: JP 0x23A
                0x12, 0x0A, // 0x240: JP 0x20A
                0x80, 0x00  // 0x242: 1x1 pixel sprite + padding
        };
        return toByteArray(opcodes);
    }

    private static byte[] createKeypadEchoRom() {
        int[] opcodes = {
                0x00, 0xE0, // 0x200: CLS
                0x60, 0x1E, // 0x202: LD V0, 30 (Center X)
                0x61, 0x0D, // 0x204: LD V1, 13 (Center Y)
                0xF2, 0x0A, // 0x206: LD V2, K  (Wait for key press)
                0x00, 0xE0, // 0x208: CLS
                0xF2, 0x29, // 0x20A: LD F, V2  (Load font sprite for key V2)
                0xD0, 0x15, // 0x20C: DRW V0, V1, 5
                0x63, 0x03, // 0x20E: LD V3, 3
                0xF3, 0x18, // 0x210: LD ST, V3 (Short beep)
                0xE2, 0x9E, // 0x212: SKP V2    (Wait until key is released)
                0x12, 0x06, // 0x214: JP 0x206
                0x12, 0x12  // 0x216: JP 0x212
        };
        return toByteArray(opcodes);
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