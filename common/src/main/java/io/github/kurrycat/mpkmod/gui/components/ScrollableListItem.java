package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatibility.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.gui.interfaces.HoverComponent;
import io.github.kurrycat.mpkmod.gui.interfaces.KeyInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseInputListener;
import io.github.kurrycat.mpkmod.gui.interfaces.MouseScrollListener;
import io.github.kurrycat.mpkmod.util.ItrUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public abstract class ScrollableListItem<I extends ScrollableListItem<I>> extends ComponentHolder implements MouseInputListener, KeyInputListener, MouseScrollListener, HoverComponent {
    public static final Color defaultEdgeColor = new Color(255, 255, 255, 95);
    protected ScrollableList<I> parent;

    public ScrollableListItem(ScrollableList<I> parent) {
        this.parent = parent;
        this.parent.passPositionTo(this, PERCENT.NONE, Anchor.TOP_LEFT);
        this.setSize(new Vector2D(parent.getDisplayedSize().getX(), 50));
    }

    public abstract void render(int index, Vector2D pos, Vector2D size, Vector2D mouse);

    public final void renderComponents(Vector2D mouse) {
        this.components.forEach(c -> c.render(mouse));
    }

    public final void renderDefaultBorder(Vector2D pos, Vector2D size) {
        Renderer2D.drawHollowRect(pos.add(1), size.sub(2), 1, defaultEdgeColor);
    }

    public final boolean isVisible() {
        return getDisplayedPos().getY() >= -getHeight() &&
                getDisplayedPos().getY() <= parent.content.getDisplayedPos().getY() +
                        parent.content.getDisplayedSize().getY();
    }

    public int getHeight() {
        return (int) this.getDisplayedSize().getY();
    }

    public void setHeight(double height) {
        this.setSize(new Vector2D(parent.getDisplayedSize().getX(), height));
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        return ItrUtil.orMap(
                ItrUtil.getAllOfType(MouseInputListener.class, components),
                b -> b.handleMouseInput(state, mousePos, button)
        );
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        return ItrUtil.orMap(
                ItrUtil.getAllOfType(MouseScrollListener.class, components),
                b -> b.handleMouseScroll(mousePos, delta)
        );
    }

    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        return ItrUtil.orMap(
                ItrUtil.getAllOfType(KeyInputListener.class, components),
                b -> b.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
        );
    }

    public void renderHover(Vector2D mouse) {

    }
}
