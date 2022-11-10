package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;

public class PopupMenu extends Pane {
    public ArrayList<PopupMenu> subMenus = new ArrayList<>();
    private PopupMenu currentlyActive = null;

    public PopupMenu() {
        super(Vector2D.OFFSCREEN, new Vector2D(0, 1));
        this.components.clear();
        this.backgroundColor = new Color(31, 31, 31, 150);
    }

    public void addSubMenu(Button button, PopupMenu menu) {
        this.addComponent(button);
        button.setButtonCallback(mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                currentlyActive = menu;
                menu.setLoaded(true);
            }
        });
        this.subMenus.add(menu);
    }

    public void addComponent(Component c) {
        c.pos = new Vector2D(1, getDisplayedSize().getY());
        c.setParent(this, false, false, false, false);
        this.components.add(c);
        this.setSize(
                new Vector2D(
                        Math.max(c.getDisplayedSize().getX() + 2, this.getDisplayedSize().getX()),
                        getDisplayedSize().getY() + c.getDisplayedSize().getY() + 1
                )
        );
    }

    @Override
    public void render(Vector2D mousePos) {
        if (currentlyActive == null) {
            int currY = 1;
            for (Component c : components) {
                c.pos = new Vector2D(
                        1,
                        currY
                );
                currY += c.getDisplayedSize().getY() + 1;
            }
            super.render(mousePos);
        } else {
            currentlyActive.pos = this.getDisplayedPos();
            currentlyActive.render(mousePos);
        }
    }

    @Override
    public void close() {
        subMenus.forEach(m -> m.setLoaded(false));
        super.close();
    }

    @Override
    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (this.isLoaded()) {
            if (state == Mouse.State.DOWN && !contains(mousePos)) {
                close();
                return true;
            }
            if (currentlyActive != null)
                return currentlyActive.handleMouseInput(state, mousePos, button);
            return super.handleMouseInput(state, mousePos, button);
        }
        return false;
    }

    @Override
    public boolean handleKeyInput(char keyCode, String key, boolean pressed) {
        if (currentlyActive != null)
            return currentlyActive.handleKeyInput(keyCode, key, pressed);
        return super.handleKeyInput(keyCode, key, pressed);
    }

    @Override
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (currentlyActive != null)
            return currentlyActive.handleMouseScroll(mousePos, delta);
        return super.handleMouseScroll(mousePos, delta);
    }

    @Override
    public boolean contains(Vector2D testPos) {
        if (currentlyActive != null) return currentlyActive.contains(testPos);
        return super.contains(testPos);
    }
}
