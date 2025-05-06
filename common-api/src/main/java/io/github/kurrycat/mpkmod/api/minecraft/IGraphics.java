package io.github.kurrycat.mpkmod.api.minecraft;

public interface IGraphics {
    /**
     * Draws a rectangle on the screen.
     *
     * @param x      The x coordinate of the rectangle.
     * @param y      The y coordinate of the rectangle.
     * @param width  The width of the rectangle.
     * @param height The height of the rectangle.
     * @param color  The color of the rectangle.
     */
    void drawRect(int x, int y, int width, int height, int color);

    /**
     * Draws a string on the screen.
     *
     * @param text  The text to draw.
     * @param x     The x coordinate of the text.
     * @param y     The y coordinate of the text.
     * @param color The color of the text.
     */
    void drawString(String text, int x, int y, int color);
}
