package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class ScrollableListItem<I extends ScrollableListItem<I>> implements MouseInputListener, KeyInputListener, MouseScrollListener {
    protected int height;
    protected ScrollableList<I> parent;

    public ScrollableListItem(ScrollableList<I> parent) {
        this.parent = parent;
        this.height = 50;
    }

    public int getHeight() {
        return this.height;
    }


    public abstract void render(int index, Vector2D pos, Vector2D size, Vector2D mouse);

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return false;
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        return false;
    }

    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        return false;
    }
}
