package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class Div extends Component implements MouseInputListener, MouseScrollListener, KeyInputListener {
    public Div(Vector2D pos, Vector2D size) {
        this.pos = pos;
        this.size = size;
    }

    @Override
    public void render(Vector2D mouse) {
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
