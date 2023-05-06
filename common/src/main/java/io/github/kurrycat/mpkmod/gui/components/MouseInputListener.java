package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Mouse;
import io.github.kurrycat.mpkmod.util.Vector2D;

public interface MouseInputListener {
    boolean handleMouseInput(Mouse.State state, Vector2D mousePos, Mouse.Button button);
}
