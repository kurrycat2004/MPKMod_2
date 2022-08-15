package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

public abstract class Component {
    public Vector2D pos;

    public Component(Vector2D pos) {
        this.pos = pos;
    }

    public abstract void render();

    public Component setPos(Vector2D pos) {
        this.pos = pos;
        return this;
    }
}
