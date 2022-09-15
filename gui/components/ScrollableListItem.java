package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class ScrollableListItem<I extends ScrollableListItem<I>> {
    protected int height;
    private ScrollableList<I> parent;

    public ScrollableListItem(ScrollableList<I> parent) {
        this.parent = parent;
        this.height = 50;
    }

    public int getHeight() {
        return this.height;
    }


    public abstract void render(Vector2D pos, Vector2D size, Vector2D mouse);
}
