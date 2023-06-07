package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class TextLabel extends Label {
    private Color backgroundColor = new Color(255, 255, 255, 0);
    private Color borderColor = new Color(255, 255, 255, 0);

    public TextLabel(String text, Vector2D pos) {
        super(text);
        this.setPos(pos);
    }

    @Override
    public void render(Vector2D mouse) {
        drawBackground();
        FontRenderer.drawString(text, getDisplayedPos(), color, true);
    }

    public void drawBackground() {
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);
        Renderer2D.drawHollowRect(getDisplayedPos(), getDisplayedSize(), 1, borderColor);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
}
