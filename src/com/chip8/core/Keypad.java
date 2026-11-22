package com.chip8.core;

import java.util.Arrays;
import java.util.OptionalInt;

public class Keypad {
    public static final int KEY_COUNT = 16;

    private final boolean[] keys = new boolean[KEY_COUNT];

    public void reset() {
        Arrays.fill(keys, false);
    }

    public void setKeyState(int keyIndex, boolean pressed) {
        validateKeyIndex(keyIndex);
        keys[keyIndex] = pressed;
    }

    public boolean isKeyPressed(int keyIndex) {
        validateKeyIndex(keyIndex);
        return keys[keyIndex];
    }

    public OptionalInt getFirstPressedKey() {
        for (int i = 0; i < KEY_COUNT; i++) {
            if (keys[i]) {
                return OptionalInt.of(i);
            }
        }
        return OptionalInt.empty();
    }

    private void validateKeyIndex(int keyIndex) {
        if (keyIndex < 0 || keyIndex >= KEY_COUNT) {
            throw new IndexOutOfBoundsException("Invalid keypad index: " + keyIndex);
        }
    }
}