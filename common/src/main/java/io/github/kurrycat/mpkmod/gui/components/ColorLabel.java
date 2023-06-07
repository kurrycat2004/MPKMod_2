package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Vector2D;

public class ColorLabel extends Label {
    private final Colors color;

    public ColorLabel(Colors color) {
        super(color.getCode() + color.getName());
        this.color = color;
    }

    public ColorLabel(Colors color, Vector2D pos) {
        this(color);
        this.setPos(pos);
    }

    @Override
    public void render(Vector2D mouse) {
        this.text = contains(mouse) ? color.getName() : color.getCode() + color.getName();
        super.render(mouse);
    }
}