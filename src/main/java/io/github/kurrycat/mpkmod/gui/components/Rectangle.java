package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Rectangle extends Component {
    public Color color;

    public Rectangle(Vector2D pos, Vector2D size, Color color) {
        super(pos);
        this.setSize(size);
        this.color = color;
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), color);
    }
}
