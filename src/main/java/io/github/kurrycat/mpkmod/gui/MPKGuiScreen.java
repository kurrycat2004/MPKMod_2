package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.util.Vector2D;

@SuppressWarnings("unused")
public abstract class MPKGuiScreen {

    public void onGuiInit() {
    }

    public void onGuiClosed() {
    }

    public void onKeyEvent(int keyCode, String key, boolean pressed) {
    }

    public void onMouseClicked(Vector2D mouse, int mouseButton) {
    }

    public void onMouseClickMove(Vector2D mouse, int mouseButton, long timeSinceLastClick) {
    }

    public void onMouseReleased(Vector2D mouse, int mouseButton) {
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
    }
}
