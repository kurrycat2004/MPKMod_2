package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.ComponentHolder;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

@SuppressWarnings("unused")
public abstract class MPKGuiScreen extends ComponentHolder {
    private boolean initialized = false;
    private String id = null;

    private Vector2D screenSize = Vector2D.ZERO;

    public final String getID() {
        return id;
    }

    public final MPKGuiScreen setID(String id) {
        this.id = id;
        return this;
    }

    public Vector2D getScreenSize() {
        return screenSize;
    }

    @Override
    public Vector2D getDisplayedPos() {
        return Vector2D.ZERO.copy();
    }

    @Override
    public Vector2D getDisplayedSize() {
        return getScreenSize();
    }

    public void onGuiInit() {
    }

    public final void onInit() {
        initialized = true;
        screenSize = Renderer2D.getScaledSize();
        onGuiInit();
    }

    public void onGuiClosed() {
    }

    public final void onResize(int width, int height) {
        this.screenSize = new Vector2D(width, height);
        onGuiResized(screenSize);
    }

    public void onGuiResized(Vector2D screenSize) {
    }

    public void onKeyEvent(char keyCode, String key, boolean pressed) {
    }

    public void onMouseClicked(Vector2D mouse, int mouseButton) {
    }

    public void onMouseClickMove(Vector2D mouse, int mouseButton, long timeSinceLastClick) {
    }

    public void onMouseReleased(Vector2D mouse, int mouseButton) {
    }


    /**
     * @param mousePos Mouse position when scrolled
     * @param delta    number of lines to scroll (one scroll tick = 3 per default)<br>
     *                 delta < 0: scrolled down<br>
     *                 delta > 0: scrolled up
     */
    public void onMouseScroll(Vector2D mousePos, int delta) {
    }

    public void drawScreen(Vector2D mouse, float partialTicks) {
    }

    public final void drawDefaultBackground() {
        Renderer2D.drawRect(Vector2D.ZERO, getScreenSize().add(2), new Color(16, 16, 16, 140));
    }

    public boolean shouldCreateKeyBind() {
        return false;
    }

    public boolean resetOnOpen() {
        return true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public final void close() {
        Minecraft.displayGuiScreen(null);
    }
}
