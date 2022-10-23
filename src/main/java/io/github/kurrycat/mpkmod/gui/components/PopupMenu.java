package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class PopupMenu extends Pane {
    public PopupMenu() {
        super(Vector2D.OFFSCREEN, new Vector2D(0, 1));
        this.components.clear();
        this.backgroundColor = new Color(31, 31, 31, 150);
    }

    public void addComponent(Component c) {
        c.pos = new Vector2D(-1, getDisplayPos().getY() + getSize().getY());
        this.components.add(c);
        this.setSize(
                new Vector2D(
                        Math.max(c.getSize().getX() + 2, this.getSize().getX()),
                        getSize().getY() + c.getSize().getY() + 1
                )
        );
    }

    @Override
    public void render(Vector2D mousePos) {
        int currY = getDisplayPos().getYI() + 1;
        for (Component c : components) {
            c.pos.setX(getDisplayPos().getX() + getSize().getX() / 2 - c.getSize().getX() / 2);
            c.pos.setY(currY);
            currY += c.getSize().getY() + 1;
        }
        super.render(mousePos);
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if(this.isLoaded()) {
            if (state == Mouse.State.DOWN && !contains(mousePos)) {
                close();
                return true;
            }
            return super.handleMouseInput(state, mousePos, button);
        }
        return false;
    }
}
