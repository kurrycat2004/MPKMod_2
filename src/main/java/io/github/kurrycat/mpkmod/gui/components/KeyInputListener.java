package io.github.kurrycat.mpkmod.gui.components;

public interface KeyInputListener {
    boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped);
}
