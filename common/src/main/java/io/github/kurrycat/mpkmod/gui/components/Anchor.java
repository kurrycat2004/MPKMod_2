package io.github.kurrycat.mpkmod.gui.components;

import io.github.kurrycat.mpkmod.util.Vector2D;

/**
 * Each {@link Anchor} has an origin point and a direction vector with which the offset will get multiplied
 * Using {@link Anchor#BOTTOM_RIGHT} for both a child and parent anchor will position the
 * bottom right corner of the child offset by posX left and posY up from the bottom right corner of the parent
 */
public enum Anchor {
    /**
     * Use this as both the parentAnchor and child anchor with pos = {@link Vector2D#ZERO} to center a child component
     */
    CENTER(0.5, 0.5, false, false),
    CENTER_LEFT(0, 0.5, false, false),
    CENTER_RIGHT(1, 0.5, true, false),
    /**
     * Default anchor for both parent and child, works exactly as one would expect for 2d rendering
     */
    TOP_LEFT(0, 0, false, false),
    TOP_RIGHT(1, 0, true, false),
    TOP_CENTER(0.5, 0, false, false),
    BOTTOM_LEFT(0, 1, false, true),
    BOTTOM_RIGHT(1, 1, true, true),
    BOTTOM_CENTER(0.5, 1, false, true);

    public final Vector2D multiplier;
    public final Vector2D origin;
    public final boolean invertedX, invertedY;

    Anchor(double originX, double originY, boolean invertedX, boolean invertedY) {
        origin = new Vector2D(originX, originY);
        multiplier = new Vector2D(invertedX ? -1 : 1, invertedY ? -1 : 1);
        this.invertedX = invertedX;
        this.invertedY = invertedY;
    }

    public static Anchor fromPos(Vector2D pos) {
        if (pos.getX() < 0) {
            if (pos.getY() < 0) return BOTTOM_RIGHT;
            else return TOP_RIGHT;
        } else {
            if (pos.getY() < 0) return BOTTOM_LEFT;
            else return TOP_LEFT;
        }
    }

    /**
     * Example: <br>
     * {@code Anchor.TOP_RIGHT.transformVec(new Vector2D(1, 1))} will result in {@code new Vector2D(-1, 1)}
     *
     * @param vec {@link Vector2D} to be transformed
     * @return {@code vec} transformed from relative to {@code this} to relative to {@link Anchor#TOP_LEFT}
     */
    public Vector2D transformVec(Vector2D vec) {
        return vec.mult(multiplier);
    }

    /**
     * @param size component size
     * @return origin pos in component relative to top left corner of component
     */
    public Vector2D getOriginPos(Vector2D size) {
        return size.mult(origin);
    }
}
