package com.chip8.core;

public interface Chip8Observer {
    void onScreenUpdated();
    void onSoundTriggered();
}