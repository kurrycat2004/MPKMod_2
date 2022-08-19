package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Label extends Component {
    public String text;
    public Color color = Color.WHITE;
    public Color selectedColor = new Color(255, 170, 0, 100);

    public Label(String text, Vector2D pos) {
        super(pos);
        this.text = text;
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        FontRenderer.drawString(text, getDisplayPos(), color, true);
    }

    public void drawDefaultSelectedBackground() {
        if (selected) {
            Renderer2D.drawRect(getDisplayPos(), getSize(), selectedColor);
            Renderer2D.drawHollowRect(getDisplayPos(), getSize(), 1, Color.BLACK);
        }
    }

    public Label setText(String text) {
        this.text = text;
        return this;
    }

    public Label setColor(Color color) {
        this.color = color;
        return this;
    }

    public Label setPos(Vector2D pos) {
        super.setPos(pos);
        return this;
    }

    public Vector2D getSize() {
        return FontRenderer.getStringSize(text);
    }
}
