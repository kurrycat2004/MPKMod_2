package io.github.kurrycat.mpkmod.compatability;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;

import java.awt.*;

public class MainGuiScreen extends MPKGuiScreen {
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        FontRenderer.drawString("TEST", 10, 10, Color.RED, true);
    }
}
