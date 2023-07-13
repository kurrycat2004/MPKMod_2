package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.Colors;
import io.github.kurrycat.mpkmod.util.Vector2D;

import java.awt.*;
import java.util.Optional;

public class FontRenderer {
    public static final double DEFAULT_FONT_SIZE = 9;

    /**
     * Draws one centered line of text to the screen
     *
     * @param text   The text to be drawn
     * @param pos    The position of the middle of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     * @see #drawString(String, Vector2D, Color, boolean)
     */
    public static void drawCenteredString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos.sub(getStringSize(text).div(2)), color, shadow);
    }

    /**
     * Draws one line of text to the screen with the {@link FontRenderer#DEFAULT_FONT_SIZE default font size}
     *
     * @param text   The text to be drawn
     * @param pos    The position of the top left corner of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     */
    public static void drawString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos, color, DEFAULT_FONT_SIZE, shadow);
    }

    /**
     * @param text A single line String
     * @return The size of the text when rendered using {@link #drawString(String, Vector2D, Color, boolean)} as a {@link Vector2D} containing the width and the height
     */
    public static Vector2D getStringSize(String text) {
        return Interface.get().map(f -> f.getStringSize(text, DEFAULT_FONT_SIZE)).orElse(Vector2D.ZERO.copy());
    }

    /**
     * Draws one line of text to the screen with the specified font size
     *
     * @param text     The text to be drawn
     * @param pos      The position of the top left corner of the text
     * @param color    The color of the text
     * @param fontSize The height of the string
     * @param shadow   Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     */
    public static void drawString(String text, Vector2D pos, Color color, double fontSize, boolean shadow) {
        Interface.get().ifPresent(f ->
                f.drawString(Colors.RESET.getCode() + text,
                        pos.getX(), pos.getY(), color, fontSize, shadow));
    }

    /**
     * Draws one line of text to the screen with the origin being the top right corner
     *
     * @param text   The text to be drawn
     * @param pos    The position of the middle of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     * @see #drawString(String, Vector2D, Color, boolean)
     */
    public static void drawRightString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos.sub(getStringSize(text)), color, shadow);
    }

    /**
     * Draws one line of text to the screen with the origin being the middle of the right edge
     *
     * @param text   The text to be drawn
     * @param pos    The position of the middle of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     * @see #drawString(String, Vector2D, Color, boolean)
     */
    public static void drawRightCenteredString(String text, Vector2D pos, Color color, boolean shadow) {
        Vector2D s = getStringSize(text);
        drawString(text, pos.sub(s.getX(), s.getY() / 2), color, shadow);
    }

    /**
     * Draws one line of text to the screen with the origin being the middle of the left edge
     *
     * @param text   The text to be drawn
     * @param pos    The position of the middle of the text
     * @param color  The color of the text
     * @param shadow Draws the same text again underneath in {@link Color#BLACK} and a positive offset of 1px for each axis
     * @see #drawString(String, Vector2D, Color, boolean)
     */
    public static void drawLeftCenteredString(String text, Vector2D pos, Color color, boolean shadow) {
        drawString(text, pos.sub(0, getStringSize(text).getY() / 2), color, shadow);
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void drawString(String text, double x, double y, Color color, double fontSize, boolean shadow);

        Vector2D getStringSize(String text, double fontSize);
    }
}
