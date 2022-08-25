package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

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

    public void drawDefaultBackground() {
        Renderer2D.drawRect(Vector2D.ZERO, Renderer2D.getScaledSize(), new Color(16, 16, 16, 140));
    }
}
