package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class TextRectangle extends Component {
    private String text;
    private final Color color;
    private final Color textColor;

    public TextRectangle(Vector2D pos, Vector2D size, String text, Color color, Color textColor) {
        super(pos);
        this.size = size;
        this.text = text;
        this.color = color;
        this.textColor = textColor;
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRect(getDisplayPos(), getSize(), color);

        FontRenderer.drawCenteredString(
                text,
                getDisplayPos().add(getSize().div(2)).add(new Vector2D(0, 1)),
                textColor,
                false
        );
    }

    public void setText(String text) {this.text = text;}
}
