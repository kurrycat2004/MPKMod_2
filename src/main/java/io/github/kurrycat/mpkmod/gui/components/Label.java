package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatability.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Label extends Component {
    @JsonProperty
    public String text;
    @JsonProperty
    public Color color = Color.WHITE;
    @JsonProperty
    public Color selectedColor = new Color(255, 170, 0, 100);
    public Color backgroundColor = null;

    public Label(String text, Vector2D pos) {
        super(pos);
        this.text = text;
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        if (backgroundColor != null) Renderer2D.drawRect(getDisplayPos(), getSize(), backgroundColor);
        FontRenderer.drawString(text, getDisplayPos(), color, true);
    }

    public void drawDefaultSelectedBackground() {
        if (selected) {
            Renderer2D.drawRect(getDisplayPos(), getSize(), selectedColor);
            Renderer2D.drawHollowRect(getDisplayPos(), getSize(), 1, Color.BLACK);
        }
    }

    @JsonProperty
    public Label setText(String text) {
        this.text = text;
        return this;
    }

    @JsonProperty
    public Label setColor(Color color) {
        this.color = color;
        return this;
    }

    @JsonProperty
    public Label setPos(Vector2D pos) {
        super.setPos(pos);
        return this;
    }

    public Vector2D getSize() {
        return FontRenderer.getStringSize(text);
    }
}
