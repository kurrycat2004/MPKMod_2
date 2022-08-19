package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.util.StringToInfo;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class InfoLabel extends Label {
    public InfoLabel(String text, Vector2D pos) {
        super(text, pos);
    }

    public String getFormattedText() {
        return StringToInfo.replaceVarsInString(text);
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(getFormattedText(), getDisplayPos(), color, true);
    }

    public Vector2D getSize() {
        return FontRenderer.getStringSize(getFormattedText());
    }
}
