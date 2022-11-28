package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Optional;

public class FontRenderer {
    /**
     * Draws one line of text to the screen
     *
     * @param text   The text to be drawn
     * @param pos    The position of the top left corner of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and an offset of 1px for each axis
     */
    public static void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        Interface.get().ifPresent(f -> f.drawString(text, pos, color, shadow));
    }

    /**
     * Draws one centered line of text to the screen
     *
     * @param text   The text to be drawn
     * @param pos    The position of the middle of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and an offset of 1px for each axis
     * @see #drawString(String, Vector2D, Color, boolean)
     */
    public static void drawCenteredString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos.sub(getStringSize(text).div(2)), color, shadow);

    }

    /**
     * @param text A single line String
     * @return The size of the text when rendered using {@link #drawString(String, Vector2D, Color, boolean)} as a {@link Vector2D} containing the width and the height
     */
    public static Vector2D getStringSize(String text) {
        return Interface.get().map(f -> f.getStringSize(text)).orElse(Vector2D.ZERO.copy());
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void drawString(String text, Vector2D pos, Color color, boolean shadow);

        Vector2D getStringSize(String text);
    }
}
