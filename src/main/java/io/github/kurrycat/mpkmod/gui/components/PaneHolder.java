package io.github.kurrycat.mpkmod.gui.components;

public interface PaneHolder {
    void openPane(Pane p);
    void closePane(Pane p);
    void removeComponent(Component c);
    void addComponent(Component c);
}
