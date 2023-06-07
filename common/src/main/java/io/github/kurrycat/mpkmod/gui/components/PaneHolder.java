package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

public interface PaneHolder {
    <T extends PaneHolder> void openPane(Pane<T> p, Vector2D pos);
    <T extends PaneHolder> void closePane(Pane<T> p);
    void removeComponent(Component c);
    void addComponent(Component c);
}
