package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

import java.util.ArrayList;

public abstract class ComponentHolder {
    protected ArrayList<Component> components = new ArrayList<>();

    public abstract Vector2D getDisplayedPos();

    public abstract Vector2D getDisplayedSize();

    public void addChild(Component child) {
        this.addChild(child, false, false, null);
    }

    public void addChild(Component child, boolean percentPos, boolean percentSize, Component.Anchor anchor) {
        addChild(child, percentPos, percentPos, percentSize, percentSize, anchor);
    }

    public void addChild(Component child, boolean percentPosX, boolean percentPosY, boolean percentSizeX, boolean percentSizeY, Component.Anchor anchor) {
        this.components.add(child);
        child.setParent(this, percentPosX, percentPosY, percentSizeX, percentSizeY);
        child.setParentAnchor(anchor);
    }

    public void removeChild(Component child) {
        this.components.remove(child);
    }
}
