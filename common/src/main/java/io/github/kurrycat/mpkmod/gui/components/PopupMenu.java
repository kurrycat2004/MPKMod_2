package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.gui.screens.main_gui.MainGuiScreen;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;

public class PopupMenu extends Pane<MainGuiScreen> {
    public ArrayList<PopupMenu> subMenus = new ArrayList<>();
    private PopupMenu currentlyActive = null;

    public PopupMenu() {
        super(Vector2D.OFFSCREEN, new Vector2D(0, 1));
        this.components.clear();
    }

    @SuppressWarnings("unused")
    public void addSubMenu(Button button, PopupMenu menu) {
        this.addComponent(button);
        button.setButtonCallback(mouseButton -> {
            if (Mouse.Button.LEFT.equals(mouseButton)) {
                currentlyActive = menu;
                menu.setLoaded(true);
            }
        });
        this.subMenus.add(menu);
        passPositionTo(menu);
    }

    public void addComponent(Component c) {
        addComponent(c, PERCENT.NONE);
    }

    public void addComponent(Component c, int percentFlag) {
        c.setPos(new Vector2D(1, getDisplayedSize().getY()));
        addChild(c, percentFlag, Anchor.TOP_LEFT);
        this.setSize(
                new Vector2D(
                        Math.max(c.getDisplayedSize().getX() + 2, this.getDisplayedSize().getX()),
                        getDisplayedSize().getY() + c.getDisplayedSize().getY() + 1
                )
        );
        for (Component comp : components) {
            comp.setSize(new Vector2D(-2, comp.getDisplayedSize().getY()));
        }
    }

    @Override
    public void close() {
        subMenus.forEach(m -> m.setLoaded(false));
        currentlyActive = null;
        super.close();
    }

    @Override
    public void render(Vector2D mousePos) {
        if (currentlyActive == null) {
            super.render(mousePos);
        } else {
            currentlyActive.render(mousePos);
        }
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
    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (currentlyActive != null)
            return currentlyActive.handleMouseScroll(mousePos, delta);
        return super.handleMouseScroll(mousePos, delta);
    }

    @Override
    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        if (currentlyActive != null)
            return currentlyActive.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped);
        return super.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped);
    }

    @Override
    public boolean contains(Vector2D testPos) {
        if (currentlyActive != null) return currentlyActive.contains(testPos);
        return super.contains(testPos);
    }
}
