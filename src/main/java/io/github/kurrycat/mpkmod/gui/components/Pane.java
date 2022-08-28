package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Renderer2D;
import io.github.kurrycat.mpkmod.util.ArrayListUtil;
import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.ArrayList;

public class Pane extends Component implements MouseInputListener, MouseScrollListener {
    public ArrayList<Component> components = new ArrayList<>();
    public Color backgroundColor = new Color(255, 255, 255, 255);

    public PaneHolder parent = null;

    private boolean loaded;

    public Pane(Vector2D pos, Vector2D size) {
        super(pos);
        this.setSize(size);
        this.loaded = false;

        this.components.add(createCloseButton());
    }

    @Override
    public void render(Vector2D mousePos) {
        Renderer2D.drawRect(getDisplayPos(), getSize(), backgroundColor);

        components.forEach(c -> c.render(mousePos));
    }

    public boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button) {
        if (this.loaded) {
            return ArrayListUtil.orMap(
                    ArrayListUtil.getAllOfType(MouseInputListener.class, components),
                    b -> b.handleMouseInput(state, mousePos, button)
            );
        }
        return false;
    }

    public boolean handleMouseScroll(Vector2D mousePos, int delta) {
        if (this.loaded) {
            return ArrayListUtil.orMap(
                    ArrayListUtil.getAllOfType(MouseScrollListener.class, components),
                    b -> b.handleMouseScroll(mousePos, delta)
            );
        }
        return false;
    }

    public void close() {
        if (parent == null) return;
        parent.closePane(this);
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setParent(PaneHolder p) {
        this.parent = p;
    }

    public Button createCloseButton() {
        return new Button(
                "X",
                new Vector2D(this.getPos().getX() + this.getSize().getX() - 10, this.getPos().getY()),
                new Vector2D(10, 10),
                mouseButton -> {
                    close();
                }
        );
    }
}