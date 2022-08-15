package io.github.kurrycat.mpkmod.compatability;

@SuppressWarnings("unused")
public abstract class MPKGuiScreen {

    public void onGuiInit() {
    }

    public void onGuiClosed() {
    }

    public void onKeyEvent(int keyCode, String key, boolean pressed) {
    }

    public void onMouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }
}
