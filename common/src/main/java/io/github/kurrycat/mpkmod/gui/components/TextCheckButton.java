package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class TextCheckButton extends CheckButton {
    private String text;
    public Color color = Color.WHITE;
    private Vector2D checkboxSize = new Vector2D(11, 11);

    public TextCheckButton(Vector2D pos, String text, boolean checked, CheckButtonCallback checkButtonCallback) {
        super(pos, checked, checkButtonCallback);
        this.text = text;
    }

    public void render(Vector2D mouse) {
        Renderer2D.drawRectWithEdge(getDisplayedPos(), checkboxSize, 1, normalColor, normalColor);

        if (isChecked())
            FontRenderer.drawString(
                    "x",
                    getDisplayedPos().add(new Vector2D(3, 1)),
                    Color.WHITE,
                    false
            );
        FontRenderer.drawString(text, getDisplayedPos().add(checkboxSize.getX() + 2, 2), color, true);
    }

    public String getText() {
        return text;
    }

    public TextCheckButton setText(String text) {
        this.text = text;
        return this;
    }

    public Vector2D getDisplayedSize() {
        Vector2D fontSize = FontRenderer.getStringSize(text);
        return new Vector2D(fontSize.getX() + 3 + checkboxSize.getX(), Math.max(fontSize.getY(), checkboxSize.getY()));
    }

    public Vector2D getSizeForJson() {
        Vector2D fontSize = FontRenderer.getStringSize(text);
        return new Vector2D(fontSize.getX() + 3 + checkboxSize.getX(), Math.max(fontSize.getY(), checkboxSize.getY()));
    }

    public void setCheckboxSize(Vector2D checkboxSize) {
        this.checkboxSize = checkboxSize;
    }
}
