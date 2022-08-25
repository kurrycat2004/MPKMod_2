package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.functions.DrawRectFunction;
import io.github.kurrycat.mpkmod.compatability.functions.GetScaledSizeFunction;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;

public class Renderer2D {
    private static DrawRectFunction drawRectFunction;
    private static GetScaledSizeFunction getScaledSizeFunction;

    public static void registerDrawRect(DrawRectFunction function) {
        drawRectFunction = function;
    }

    public static void registerGetScaledSize(GetScaledSizeFunction f) {
        getScaledSizeFunction = f;
    }

    public static void drawRect(Vector2D pos, Vector2D size, Color color) {
        drawRectFunction.apply(pos, size, color);
    }


    /**
     * @param pos           Top left corner of rectangle
     * @param size          Size of the rectangle (edge goes over size)
     * @param edgeThickness width of the edge (extends outwards from the specified rectangle)
     * @param color         edge color of the rectangle
     */
    public static void drawHollowRect(Vector2D pos, Vector2D size, double edgeThickness, Color color) {
        //TOP
        drawRect(pos.sub(edgeThickness), new Vector2D(size.getX() + edgeThickness * 2, edgeThickness), color);
        //BOTTOM
        drawRect(pos.add(-edgeThickness, size.getY()), new Vector2D(size.getX() + edgeThickness * 2, edgeThickness), color);
        //LEFT
        drawRect(pos.sub(edgeThickness, 0), new Vector2D(edgeThickness, size.getY()), color);
        //RIGHT
        drawRect(pos.add(size.getX(), 0), new Vector2D(edgeThickness, size.getY()), color);
    }

    public static void drawRectWithEdge(Vector2D pos, Vector2D size, double edgeThickness, Color fillColor, Color edgeColor) {
        drawRect(pos, size, fillColor);
        drawHollowRect(pos.add(edgeThickness), size.sub(edgeThickness * 2), edgeThickness, edgeColor);
    }

    public static Vector2D getScaledSize() {
        return getScaledSizeFunction.apply();
    }
}
