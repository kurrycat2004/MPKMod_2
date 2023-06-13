package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class TextRectangle extends Component {
    public boolean leftAligned = false;
    private String text;
    private Color color;
    private Color textColor;
    public Color edgeColor = null;

    public TextRectangle(Vector2D pos, Vector2D size, String text, Color color, Color textColor) {
        this.setPos(pos);
        this.setSize(size);
        this.text = text;
        this.color = color;
        this.textColor = textColor;
    }

    @Override
    public void render(Vector2D mouse) {
        if (color != null)
            Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), color);
        if(edgeColor != null)
            Renderer2D.drawHollowRect(getDisplayedPos().add(1), getDisplayedSize().sub(2), 1, edgeColor);

        if (leftAligned)
            FontRenderer.drawLeftCenteredString(
                    text,
                    getDisplayedPos().add(new Vector2D(3, getDisplayedSize().getY() / 2 + 1)),
                    textColor,
                    false
            );
        else
            FontRenderer.drawCenteredString(
                    text,
                    getDisplayedPos().add(getDisplayedSize().div(2)).add(new Vector2D(0, 1)),
                    textColor,
                    false
            );
    }

    public void setText(String text) {
        this.text = text;
    }

    public TextRectangle setColor(Color color) {
        this.color = color;
        return this;
    }

    public TextRectangle setTextColor(Color textColor) {
        this.textColor = textColor;
        return this;
    }
}
