package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class ScrollableList extends Component implements MouseInputListener {
    public Color backgroundColor = Color.DARK_GRAY;

    public ScrollableList(Vector2D pos, Vector2D size) {
        super(pos);
        this.setSize(size);
    }

    @Override
    public void render(Vector2D mouse) {
        Renderer2D.drawRect(getDisplayPos(), getSize(), backgroundColor);
        Renderer2D.drawHollowRect(getDisplayPos().add(1), getSize().sub(2), 1, Color.BLACK);
    }


    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return contains(mousePos);
    }
}
