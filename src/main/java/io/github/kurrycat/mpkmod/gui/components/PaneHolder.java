package io.github.kurrycat.mpkmod.gui.components;

public interface PaneHolder {
    <T extends PaneHolder> void openPane(Pane<T> p);
    <T extends PaneHolder> void closePane(Pane<T> p);
    void removeComponent(Component c);
    void addComponent(Component c);
}
