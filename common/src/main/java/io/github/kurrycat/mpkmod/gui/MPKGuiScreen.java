package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Minecraft;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.components.ComponentHolder;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

@SuppressWarnings("unused")
public abstract class MPKGuiScreen extends ComponentHolder {
    private boolean initialized = false;
    private String id = null;

    public final String getID() {
        return id;
    }

    @SuppressWarnings("UnusedReturnValue")
    public final MPKGuiScreen setID(String id) {
        this.id = id;
        return this;
    }

    public Vector2D getScreenSize() {
        return getDisplayedSize();
    }

    public void onGuiInit() {
    }

    public final void onInit() {
        initialized = true;
        setSize(Renderer2D.getScaledSize());
        setRoot(this);
        onGuiInit();
    }

    public void onGuiClosed() {
    }

    public final void onResize(int width, int height) {
        setSize(new Vector2D(width, height));
        onGuiResized(size);
    }

    public void onGuiResized(Vector2D screenSize) {
    }

    public void onKeyEvent(int keyCode, int scanCode, int modifiers, boolean pressed) {
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
        Renderer2D.drawRect(Vector2D.ZERO, size.add(2), new Color(16, 16, 16, 140));
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
