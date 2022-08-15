package io.github.kurrycat.mpkmod.gui;

import io.github.kurrycat.mpkmod.gui.components.Component;

import java.util.ArrayList;

public abstract class ComponentScreen extends MPKGuiScreen {
    public ArrayList<Component> components = new ArrayList<>();

    public void onGuiInit() {
        super.onGuiInit();
        components = new ArrayList<>();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
    }

    public void onKeyEvent(int keyCode, String key, boolean pressed) {
        super.onKeyEvent(keyCode, key, pressed);
    }

    public void onMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.onMouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.onMouseReleased(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (Component component : components) {
            component.render();
        }
    }
}
