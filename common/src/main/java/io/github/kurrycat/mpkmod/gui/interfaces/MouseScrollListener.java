package io.github.kurrycat.mpkmod.gui.interfaces;

import io.github.kurrycat.mpkmod.util.Vector2D;

public interface MouseScrollListener {
    /**
     * See: {@link io.github.kurrycat.mpkmod.gui.MPKGuiScreen#onMouseScroll(Vector2D, int)}
     * @param mousePos position of the mouse on screen
     * @param delta amount scrolled
     * @return whether the event was processed and should be canceled for all following receivers
     */
    boolean handleMouseScroll(Vector2D mousePos, int delta);
}
