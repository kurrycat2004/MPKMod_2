package io.github.kurrycat.mpkmod.render.text;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.RenderMode;
import io.github.kurrycat.mpkmod.api.render.text.GlyphProvider;
import io.github.kurrycat.mpkmod.api.render.text.TextRenderer;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.util.MathUtil;

import java.util.concurrent.ThreadLocalRandom;

public final class TextRendererImpl implements TextRenderer {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<TextRenderer> {
        public Provider() {
            super(TextRendererImpl::new, TextRenderer.class);
        }
    }

    private static final int MISSING_CODEPOINT = 63; // '?'
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final char FORMAT_CODE = '\u00A7'; // '§'

    @Override
    public float drawString(GlyphProvider.GlyphData buffer, float x, float y, int color, int style, CharSequence text, int textOffset, int textLength) {
        if (text == null || text.isEmpty()) return x;
        final int len = text.length();
        textOffset = MathUtil.clamp(textOffset, 0, len);
        textLength = MathUtil.clamp(textLength, 0, len - textOffset);
        if (textLength <= 0) return x;

        final GlyphProvider glyphProvider = GlyphProvider.INSTANCE;
        final CommandReceiver cmd = CommandReceiver.INSTANCE;

        float penX = x;
        buffer.reset();

        final boolean doShadow = (style & Style.SHADOW) != 0;
        final boolean doBold = (style & Style.BOLD) != 0;

        final int end = textOffset + textLength;
        int i = textOffset;
        while (i < end) {
            int codepoint = Character.codePointAt(text, i);
            int charCount = Character.charCount(codepoint);
            i += charCount;

            if (!getGlyph(glyphProvider, codepoint, buffer)) {
                continue;
            }
            if (buffer.xAdvance == 0) continue;

            if (buffer.isAscii && (style & Style.OBFUSCATED) != 0) {
                codepoint = glyphProvider.randomAsciiWithWidth(ThreadLocalRandom.current(), codepoint);
                if (!getGlyph(glyphProvider, codepoint, buffer)) {
                    continue;
                }
            }

            if (buffer.isEmpty) {
                penX += buffer.xAdvance;
                continue;
            }

            float x0 = penX + buffer.xOffset;
            float y0 = y + buffer.yOffset;
            float x1 = x0 + buffer.width;
            float y1 = y0 + buffer.height;

            float skew = ((style & Style.ITALIC) != 0) ? 0.125f * buffer.height : 0f;

            int firstIdx = cmd.currIdx();

            if (doShadow) {
                float dx = buffer.xShadowOffset;
                float dy = buffer.xShadowOffset;
                int shadowColor =
                        (color & 0xFF000000) |
                        ((((color >> 16) & 0xFF) >> 2) << 16) |
                        ((((color >> 8) & 0xFF) >> 2) << 8) |
                        ((color & 0xFF) >> 2);

                int sv0 = cmd.currVtxIdx();
                cmd.pushVtx(x0 + skew + dx, y0 + dy, 0f, shadowColor, buffer.u0, buffer.v0);
                cmd.pushVtx(x0 - skew + dx, y1 + dy, 0f, shadowColor, buffer.u0, buffer.v1);
                cmd.pushVtx(x1 + skew + dx, y0 + dy, 0f, shadowColor, buffer.u1, buffer.v0);
                cmd.pushVtx(x1 - skew + dx, y1 + dy, 0f, shadowColor, buffer.u1, buffer.v1);
                pushRectIdx(cmd, sv0);

                if (doBold) {
                    int bsv0 = cmd.currVtxIdx();
                    cmd.pushVtx(x0 + skew + 2 * dx, y0 + dy, 0f, shadowColor, buffer.u0, buffer.v0);
                    cmd.pushVtx(x0 - skew + 2 * dx, y1 + dy, 0f, shadowColor, buffer.u0, buffer.v1);
                    cmd.pushVtx(x1 + skew + 2 * dx, y0 + dy, 0f, shadowColor, buffer.u1, buffer.v0);
                    cmd.pushVtx(x1 - skew + 2 * dx, y1 + dy, 0f, shadowColor, buffer.u1, buffer.v1);
                    pushRectIdx(cmd, bsv0);
                }
            }

            int mv0 = cmd.currVtxIdx();
            cmd.pushVtx(x0 + skew, y0, 0f, color, buffer.u0, buffer.v0);
            cmd.pushVtx(x0 - skew, y1, 0f, color, buffer.u0, buffer.v1);
            cmd.pushVtx(x1 + skew, y0, 0f, color, buffer.u1, buffer.v0);
            cmd.pushVtx(x1 - skew, y1, 0f, color, buffer.u1, buffer.v1);
            pushRectIdx(cmd, mv0);

            if (doBold) {
                float bx0 = x0 + 1f;
                float bx1 = x1 + 1f;
                int bv0 = cmd.currVtxIdx();
                cmd.pushVtx(bx0 + skew, y0, 0f, color, buffer.u0, buffer.v0);
                cmd.pushVtx(bx0 - skew, y1, 0f, color, buffer.u0, buffer.v1);
                cmd.pushVtx(bx1 + skew, y0, 0f, color, buffer.u1, buffer.v0);
                cmd.pushVtx(bx1 - skew, y1, 0f, color, buffer.u1, buffer.v1);
                pushRectIdx(cmd, bv0);
            }

            int count = cmd.currIdx() - firstIdx;
            if (count > 0) {
                cmd.pushDrawCmd(firstIdx, count, RenderMode.TRIANGLES, buffer.texture);
            }

            penX += buffer.xAdvance + (doBold ? 1f : 0f);
        }

        float runWidth = penX - x;
        float underlineOffset = 9f;
        float underlineThickness = 1f;
        float strikeOffset = 4f;
        float strikeThickness = 1f;

        if ((style & Style.UNDERLINE) != 0) {
            drawLineQuad(cmd, x, y + underlineOffset, runWidth, underlineThickness, color);
        }
        if ((style & Style.STRIKETHROUGH) != 0) {
            drawLineQuad(cmd, x, y + strikeOffset, runWidth, strikeThickness, color);
        }

        return penX;
    }

    private static boolean getGlyph(GlyphProvider provider, int codepoint, GlyphProvider.GlyphData buffer) {
        if (provider.getGlyph(codepoint, buffer)) return true;
        return provider.getGlyph(MISSING_CODEPOINT, buffer);
    }

    private static void pushRectIdx(CommandReceiver cmd, int idx) {
        cmd.pushIdx(idx);
        cmd.pushIdx(idx + 1);
        cmd.pushIdx(idx + 2);
        cmd.pushIdx(idx + 2);
        cmd.pushIdx(idx + 1);
        cmd.pushIdx(idx + 3);
    }

    private static void drawLineQuad(CommandReceiver cmd, float x, float y, float width, float height, int color) {
        int v0 = cmd.currVtxIdx();
        cmd.pushVtx(x, y, 0f, color, 0f, 0f);
        cmd.pushVtx(x, y + height, 0f, color, 0f, 0f);
        cmd.pushVtx(x + width, y, 0f, color, 0f, 0f);
        cmd.pushVtx(x + width, y + height, 0f, color, 0f, 0f);

        int i0 = cmd.currIdx();
        pushRectIdx(cmd, v0);
        cmd.pushDrawCmd(i0, 6, RenderMode.TRIANGLES, null);
    }

    @Override
    public float drawFormattedString(GlyphProvider.GlyphData buffer, float x, float y, int color, boolean enableShadow, CharSequence text) {
        if (text == null || text.isEmpty()) return x;
        float penX = x;

        final int defaultStyle = enableShadow ? Style.SHADOW : 0;

        int currentColor = color;
        int currentStyle = defaultStyle;

        int len = text.length();
        int runStart = 0;
        int i = 0;
        while (i < len) {
            int codePoint = Character.codePointAt(text, i);
            int charCount = Character.charCount(codePoint);
            if (codePoint != FORMAT_CODE || i + charCount >= len) {
                i += charCount;
                continue;
            }

            final int oldColor = currentColor;
            final int oldStyle = currentStyle;
            final char code = Character.toLowerCase(text.charAt(i + charCount));
            //@formatter:off
            switch (code) {
                case '0': currentColor = 0xFF000000; currentStyle = defaultStyle; break;
                case '1': currentColor = 0xFF0000AA; currentStyle = defaultStyle; break;
                case '2': currentColor = 0xFF00AA00; currentStyle = defaultStyle; break;
                case '3': currentColor = 0xFF00AAAA; currentStyle = defaultStyle; break;
                case '4': currentColor = 0xFFAA0000; currentStyle = defaultStyle; break;
                case '5': currentColor = 0xFFAA00AA; currentStyle = defaultStyle; break;
                case '6': currentColor = 0xFFFFAA00; currentStyle = defaultStyle; break;
                case '7': currentColor = 0xFFAAAAAA; currentStyle = defaultStyle; break;
                case '8': currentColor = 0xFF555555; currentStyle = defaultStyle; break;
                case '9': currentColor = 0xFF5555FF; currentStyle = defaultStyle; break;
                case 'a': currentColor = 0xFF55FF55; currentStyle = defaultStyle; break;
                case 'b': currentColor = 0xFF55FFFF; currentStyle = defaultStyle; break;
                case 'c': currentColor = 0xFFFF5555; currentStyle = defaultStyle; break;
                case 'd': currentColor = 0xFFFF55FF; currentStyle = defaultStyle; break;
                case 'e': currentColor = 0xFFFFFF55; currentStyle = defaultStyle; break;
                case 'f': currentColor = 0xFFFFFFFF; currentStyle = defaultStyle; break;
                case 'k': currentStyle |= Style.OBFUSCATED; break;
                case 'l': currentStyle |= Style.BOLD; break;
                case 'm': currentStyle |= Style.STRIKETHROUGH; break;
                case 'n': currentStyle |= Style.UNDERLINE; break;
                case 'o': currentStyle |= Style.ITALIC; break;
                case 'r': currentColor = color; currentStyle = defaultStyle; break;
                default: i += charCount; continue; // not a valid format code, skip
            }
            //@formatter:on

            if (i > runStart) {
                //noinspection MagicConstant
                penX = drawString(buffer, penX, y, oldColor, oldStyle,
                        text, runStart, i - runStart);
            }
            i += charCount + 1; // skip the format code
            runStart = i;
        }
        if (runStart < len) {
            //noinspection MagicConstant
            penX = drawString(buffer, penX, y, currentColor, currentStyle,
                    text, runStart, len - runStart);
        }
        return penX;
    }
}
