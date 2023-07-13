package io.github.kurrycat.mpkmod.gui.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.FontRenderer;
import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Label extends Component {
    @JsonProperty
    public String text;
    @JsonProperty("fontSize")
    public double fontSize = FontRenderer.DEFAULT_FONT_SIZE;
    @JsonProperty
    public Color color = Color.WHITE;
    @JsonProperty
    public Color selectedColor = new Color(255, 170, 0, 100);
    public Color backgroundColor = null;

    public Label(String text) {
        this.text = text;
    }

    public Label(String text, Vector2D pos) {
        this.setPos(pos);
        this.text = text;
    }

    public void render(Vector2D mouse) {
        drawDefaultSelectedBackground();
        if (backgroundColor != null) Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);
        FontRenderer.drawString(text, getDisplayedPos(), color, true);
    }

    public void drawDefaultSelectedBackground() {
        if (selected) {
            Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), selectedColor);
            Renderer2D.drawHollowRect(getDisplayedPos(), getDisplayedSize(), 1, Color.BLACK);
        }
        if (highlighted) Renderer2D.drawDottedRect(getDisplayedPos(), getDisplayedSize(), 1, 1, 1, Color.BLACK);
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

    @Override
    public Vector2D getDisplayedSize() {
        return FontRenderer.getStringSize(text);
    }

    public Vector2D getSizeForJson() {
        return FontRenderer.getStringSize(text);
    }
}
