package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Optional;

public class Renderer2D {
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

    public static void drawRect(Vector2D pos, Vector2D size, Color color) {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(renderer2DInterface -> renderer2DInterface.drawRect(pos, size, color));
    }

    public static Vector2D getScaledSize() {
        return Interface.get().map(Interface::getScaledSize).orElse(new Vector2D(800, 600));
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void drawRect(Vector2D pos, Vector2D size, Color color);

        Vector2D getScaledSize();
    }
}
