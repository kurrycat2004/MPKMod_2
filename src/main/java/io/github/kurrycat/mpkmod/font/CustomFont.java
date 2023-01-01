package io.github.kurrycat.mpkmod.font;

import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class CustomFont {
    private final int imgSize = 512;
    protected CharData[] charData = new CharData[256];
    protected Font font;
    protected boolean antiAliasing, fractionalMetrics;
    protected int fontHeight = -1, charOffset = 0;
    protected DynamicTexture texture;

    public CustomFont(Font font, boolean antiAliasing, boolean fractionalMetrics) {
        this.font = font;
        this.antiAliasing = antiAliasing;
        this.fractionalMetrics = fractionalMetrics;
        texture = setupTexture(font, antiAliasing, fractionalMetrics, this.charData);
    }

    protected DynamicTexture setupTexture(Font font, boolean antiAliasing, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage img = generateFontImage(font, antiAliasing, fractionalMetrics, chars);

        try {
            return new DynamicTexture(img);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected BufferedImage generateFontImage(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setFont(font);
        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imgSize, imgSize);
        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
        FontMetrics fontMetrics = g.getFontMetrics();
        int charHeight = 0;
        int positionX = 0;
        int positionY = 1;

        for (int i = 0; i < chars.length; i++) {
            char ch = (char) i;
            CharData charData = new CharData();
            Rectangle2D dimensions = fontMetrics.getStringBounds(String.valueOf(ch), g);
            charData.width = (dimensions.getBounds().width + 8);
            charData.height = dimensions.getBounds().height;

            if (positionX + charData.width >= imgSize) {
                positionX = 0;
                positionY += charHeight;
                charHeight = 0;
            }

            if (charData.height > charHeight) {
                charHeight = charData.height;
            }

            charData.storedX = positionX;
            charData.storedY = positionY;

            if (charData.height > this.fontHeight) {
                this.fontHeight = charData.height;
            }

            chars[i] = charData;
            g.drawString(String.valueOf(ch), positionX + 2, positionY + fontMetrics.getAscent());
            positionX += charData.width;
        }

        return bufferedImage;
    }

    public void drawChar(CharData[] chars, char c, float x, float y) throws ArrayIndexOutOfBoundsException {
        try {
            drawQuad(x, y, chars[c].width, chars[c].height, chars[c].storedX, chars[c].storedY, chars[c].width, chars[c].height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void drawQuad(float x, float y, float width, float height, float srcX, float srcY, float srcWidth, float srcHeight) {
        float renderSRCX = srcX / imgSize;
        float renderSRCY = srcY / imgSize;
        float renderSRCWidth = srcWidth / imgSize;
        float renderSRCHeight = srcHeight / imgSize;
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY + renderSRCHeight);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(renderSRCX + renderSRCWidth, renderSRCY);
        GL11.glVertex2d(x + width, y);
    }

    /**
     * @return Font height
     */
    public int getHeight() {
        return (this.fontHeight - 8) / 2;
    }

    /**
     * @param text The string to process
     * @return Width of the provided string
     */
    public int getStringWidth(String text) {
        int width = 0;

        for (char c : text.toCharArray()) {
            if (c < this.charData.length) {
                width += this.charData[c].width - 8 + this.charOffset;
            }
        }

        return width / 2;
    }

    /**
     * @return Whether if Anti-Aliasing is enabled.
     */
    public boolean isAntiAliased() {
        return this.antiAliasing;
    }

    /**
     * Sets whether if Anti-Aliasing should be used.
     * @param antiAliasing Whether to enable Anti-Aliasing.
     */
    public void setAntiAliasing(boolean antiAliasing) {
        if (this.antiAliasing != antiAliasing) {
            this.antiAliasing = antiAliasing;
            texture = setupTexture(this.font, antiAliasing, this.fractionalMetrics, this.charData);
        }
    }

    /**
     * @return Whether if fractional metrics are used. (Floats)
     */
    public boolean isFractionalMetrics() {
        return this.fractionalMetrics;
    }

    /**
     * Sets whether if fractional metrics should be used.
     * @param fractionalMetrics Whether if fractional metrics should be used
     */
    public void setFractionalMetrics(boolean fractionalMetrics) {
        if (this.fractionalMetrics != fractionalMetrics) {
            this.fractionalMetrics = fractionalMetrics;
            texture = setupTexture(this.font, this.antiAliasing, fractionalMetrics, this.charData);
        }
    }

    /**
     * Gets the current {@link java.awt.Font} used.
     * @return Current {@link java.awt.Font} being used.
     */
    public Font getFont() {
        return this.font;
    }

    /**
     * Sets the {@link java.awt.Font} that should be used.
     * Will create a texture for the font with the current parameters.
     * @param font The {@link java.awt.Font} to use.
     */
    public void setFont(Font font) {
        this.font = font;
        texture = setupTexture(font, this.antiAliasing, this.fractionalMetrics, this.charData);
    }

    protected static class CharData {
        public int width, height, storedX, storedY;

        protected CharData() {
        }
    }
}
