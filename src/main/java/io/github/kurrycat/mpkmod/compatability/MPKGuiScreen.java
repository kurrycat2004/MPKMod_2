package io.github.kurrycat.mpkmod.compatability;

public abstract class MPKGuiScreen {

    public void onGuiInit() {
    }

    public void onGuiClosed() {
    }

    public void onKeyEvent(int keyCode, char key, boolean pressed, boolean repeatKeyEvent) {
    }

    public void onMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }
}
