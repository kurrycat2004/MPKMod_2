package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Pane extends Component implements MouseInputListener, MouseScrollListener, KeyInputListener {
    public Color backgroundColor = new Color(255, 255, 255, 255);

    public PaneHolder paneHolder = null;

    private boolean loaded;

    public Pane(Vector2D pos, Vector2D size) {
        super(pos);
        this.setSize(size);
        this.loaded = false;

        this.addChild(createCloseButton(), false, false, Anchor.TOP_RIGHT);
    }

    public void render(Vector2D mousePos) {
        Renderer2D.drawRect(getDisplayedPos(), getDisplayedSize(), backgroundColor);

        components.forEach(c -> c.render(mousePos));
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (this.loaded) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(MouseInputListener.class, components),
                    b -> b.handleMouseInput(state, mousePos, button)
            );
        }
        return false;
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (this.loaded) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(MouseScrollListener.class, components),
                    b -> b.handleMouseScroll(mousePos, delta)
            );
        }
        return false;
    }

    public void close() {
        if (paneHolder == null) return;
        paneHolder.closePane(this);
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setPaneHolder(PaneHolder p) {
        this.paneHolder = p;
    }

    public Button createCloseButton() {
        return new Button(
                "x",
                new Vector2D(1, 1),
                new Vector2D(10, 10),
                mouseButton -> {
                    close();
                }
        );
    }

    public boolean handleKeyInput(int keyCode, int scanCode, int modifiers, boolean isCharTyped) {
        if (this.loaded) {
            return ArrayListUtil.orMapAll(
                    ArrayListUtil.getAllOfType(KeyInputListener.class, components),
                    b -> b.handleKeyInput(keyCode, scanCode, modifiers, isCharTyped)
            );
        }
        return false;
    }
}