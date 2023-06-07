package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Div extends Component implements MouseInputListener, MouseScrollListener, KeyInputListener {
    public Color backgroundColor = null;

    public Div() {
    }

    public Div(Vector2D pos, Vector2D size) {
        this.setPos(pos);
        this.setSize(size);
    }

    public void addChildBelow(Component child) {
        addChild(child, PERCENT.NONE, Anchor.TOP_LEFT);
        child.setPos(new Vector2D(1, getDisplayedSize().getY()));
        this.setSize(new Vector2D(
                Math.max(child.getDisplayedSize().getX() + 2, this.getDisplayedSize().getX()),
                getDisplayedSize().getY() + child.getDisplayedSize().getY() + 1
        ));
    }

    @Override
    public void render(Vector2D mouse) {
        if (backgroundColor != null)
            Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);
        components.forEach(c -> c.render(mouse));
    }

    @Override
    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(KeyInputListener.class, components),
                e -> e.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
        );
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(MouseInputListener.class, components),
                e -> e.handleMouseInput(state, mousePos, button)
        );
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        return ArrayListUtil.orMapAll(
                ArrayListUtil.getAllOfType(MouseScrollListener.class, components),
                e -> e.handleMouseScroll(mousePos, delta)
        );
    }
}
