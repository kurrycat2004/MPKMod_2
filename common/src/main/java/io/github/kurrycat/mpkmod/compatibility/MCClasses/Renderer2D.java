package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Renderer2D {
    /**
     * @param pos           top left corner of the rectangle
     * @param size          size of the rectangle (edge is contained within)
     * @param edgeThickness thickness of the edge
     * @param fillColor     fill color of the rectangle
     * @param edgeColor     edge color of the rectangle
     */
    public static void drawRectWithEdge(Vector2D pos, Vector2D size, double edgeThickness, Color fillColor, Color edgeColor) {
        drawRect(pos, size, fillColor);
        drawHollowRect(pos.add(edgeThickness), size.sub(edgeThickness * 2), edgeThickness, edgeColor);
    }

    public static void drawRect(Vector2D pos, Vector2D size, Color color) {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(renderer2DInterface -> renderer2DInterface.drawRect(pos, size, color));
    }

    /**
     * @param pos           Top left corner of rectangle
     * @param size          Size of the rectangle (edge goes over size)
     * @param edgeThickness width of the edge (extends outwards from the specified rectangle)
     * @param color         edge color of the rectangle
     */
    public static void drawHollowRect(Vector2D pos, Vector2D size, double edgeThickness, Color color) {
        //TOP
        drawRect(
                pos.sub(edgeThickness),
                new Vector2D(size.getX() + edgeThickness * 2, edgeThickness),
                color);
        //BOTTOM
        drawRect(
                pos.add(-edgeThickness, size.getY()),
                new Vector2D(size.getX() + edgeThickness * 2, edgeThickness),
                color);
        //LEFT
        drawRect(
                pos.sub(edgeThickness, 0),
                new Vector2D(edgeThickness, size.getY()),
                color);
        //RIGHT
        drawRect(
                pos.add(size.getX(), 0),
                new Vector2D(edgeThickness, size.getY()),
                color);
    }

    /**
     * @param pos           top left corner of the rectangle
     * @param size          size of the rectangle (edge goes over size)
     * @param spacing       spacing in between the dashes
     * @param dashLength    length of a dash (put same as edgeThickness for dots)
     * @param edgeThickness thickness of the edge
     * @param color         edge color of the rectangle
     */
    public static void drawDottedRect(Vector2D pos, Vector2D size, double spacing, double dashLength, double edgeThickness, Color color) {
        Vector2D hDot = new Vector2D(dashLength, edgeThickness);
        Vector2D vDot = new Vector2D(edgeThickness, dashLength);
        for (double i = pos.getX() - edgeThickness; i <= pos.getX() + size.getX() + edgeThickness - dashLength; i += spacing + dashLength) {
            drawRect(new Vector2D(i, pos.getY() - edgeThickness), hDot, color);
        }
        for (double i = pos.getY() - edgeThickness; i <= pos.getY() + size.getY() + edgeThickness - dashLength; i += spacing + dashLength) {
            drawRect(new Vector2D(pos.getX() + size.getX(), i), vDot, color);
        }
        for (double i = pos.getX() + size.getX(); i >= pos.getX(); i -= spacing + dashLength) {
            drawRect(new Vector2D(i, pos.getY() + size.getY()), hDot, color);
        }
        for (double i = pos.getY() + size.getY(); i >= pos.getY(); i -= spacing + dashLength) {
            drawRect(new Vector2D(pos.getX() - edgeThickness, i), vDot, color);
        }
    }

    public static Vector2D getScaledSize() {
        return Interface.get().map(Interface::getScaledSize).orElse(new Vector2D(800, 600));
    }

    public static Vector2D getScreenSize() {
        return Interface.get().map(Interface::getScreenSize).orElse(new Vector2D(800, 600));
    }

    public static void drawLine(Vector2D p1, Vector2D p2, Color color) {
        drawLines(Arrays.asList(p1, p2), color);
    }

    public static void drawLines(Collection<Vector2D> points, Color color) {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(renderer2DInterface -> renderer2DInterface.drawLines(points, color));
    }

    public static void enableScissor(double x, double y, double w, double h) {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(renderer2DInterface -> renderer2DInterface.enableScissor(x, y, w, h));
    }

    public static void disableScissor() {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(Interface::disableScissor);
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void drawRect(Vector2D pos, Vector2D size, Color color);

        void drawLines(Collection<Vector2D> points, Color color);

        Vector2D getScaledSize();

        Vector2D getScreenSize();

        void enableScissor(double x, double y, double w, double h);

        void disableScissor();
    }
}
