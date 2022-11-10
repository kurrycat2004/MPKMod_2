package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class TextCheckButton extends CheckButton {
    private String text = "";
    public Color color = Color.WHITE;
    private Vector2D checkboxSize = new Vector2D(11, 11);

    public TextCheckButton(Vector2D pos, String text, boolean checked, CheckButtonCallback checkButtonCallback) {
        super(pos, checked, checkButtonCallback);
        this.text = text;
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayPos(), checkboxSize, 1, normalColor, normalColor);

        if (isChecked())
            FontRenderer.drawString(
                    "x",
                    getDisplayPos().add(new Vector2D(3, 1)),
                    Color.WHITE,
                    false
            );
        FontRenderer.drawString(text, getDisplayPos().add(checkboxSize.getX() + 1, 2), color, true);
    }

    public Vector2D getSize() {
        Vector2D fontSize = FontRenderer.getStringSize(text);
        return new Vector2D(fontSize.getX() + 1 + checkboxSize.getX(), Math.max(fontSize.getY(), checkboxSize.getY()));
    }

    public void setCheckboxSize(Vector2D checkboxSize) {
        this.checkboxSize = checkboxSize;
    }
}
