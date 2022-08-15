package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Label extends Component {
    public String text;
    public Color color = Color.WHITE;

    public Label(String text, Vector2D pos) {
        super(pos);
        this.text = text;
    }

    public void render() {
        FontRenderer.drawString(text, pos.getXF(), pos.getYF(), color, true);
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
}
