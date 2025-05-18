package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.annotation.OutArg;
import io.github.kurrycat.mpkmod.api.render.ITexture;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.text.GlyphProvider;
import io.github.kurrycat.mpkmod.api.util.ReflectionHelper;
import io.github.kurrycat.mpkmod.lib.fastutil.chars.Char2ShortMap;
import io.github.kurrycat.mpkmod.lib.fastutil.chars.Char2ShortOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.Random;

@AutoService(GlyphProvider.class)
public class GlyphProviderImpl implements GlyphProvider {
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final String MC_ASCII_FONT_CHARS =
            "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130" +
            "\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000" +
            " !\"#$%&'()*+,-./" +
            "0123456789:;<=>?" +
            "@ABCDEFGHIJKLMNO" +
            "PQRSTUVWXYZ[\\]^_" +
            "`abcdefghijklmno" +
            "pqrstuvwxyz{|}~\u0000" +
            "\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5" +
            "\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192" +
            "\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb" +
            "\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510" +
            "\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567" +
            "\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580" +
            "\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229" +
            "\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";

    private static final short[] MC_ASCII_CODEPOINTS = new short[512];
    private static final Char2ShortMap MC_UNI_CODEPOINTS = new Char2ShortOpenHashMap();

    static {
        Arrays.fill(MC_ASCII_CODEPOINTS, (short) -1);
        int i = 0;
        while(i < MC_ASCII_FONT_CHARS.length()) {
            int codepoint = Character.codePointAt(MC_ASCII_FONT_CHARS, i);
            int charCount = Character.charCount(codepoint);

            if (codepoint < MC_ASCII_CODEPOINTS.length) {
                MC_ASCII_CODEPOINTS[codepoint] = (short) i;
            } else {
                MC_UNI_CODEPOINTS.put((char) codepoint, (short) i);
            }

            i += charCount;
        }
    }

    private static final RenderBackend RENDER_BACKEND = RenderBackend.INSTANCE;
    private static final ITexture ASCII_TEXTURE = RENDER_BACKEND.texture("minecraft", "textures/font/ascii.png");
    private static final ITexture[] UNICODE_TEXTURES = new ITexture[256];

    ///  {@link #MC_ASCII_FONT_CHARS} index to char width
    private static final int[] charWidth;
    /// codepoint to glyph width
    private static final byte[] glyphWidth;

    static {
        FontRenderer fr = fontRenderer();
        ReflectionHelper rh = ReflectionHelper.INSTANCE;
        ReflectionHelper.FieldAccessor<FontRenderer, int[]> charWidthHandle =
                rh.lookupField(FontRenderer.class, int[].class, "charWidth", "field_78286_d")
                        .orElseThrow();
        int[] mcCharWidth = charWidthHandle.get(fr);
        charWidth = Arrays.copyOf(mcCharWidth, mcCharWidth.length);
        charWidth[getCodepointIdx(' ')] = 4;

        for (int i = 0; i < MC_ASCII_FONT_CHARS.length(); i++) {
            char c = MC_ASCII_FONT_CHARS.charAt(i);
            int width = c < charWidth.length ? charWidth[c] : -100;
            ModPlatformImpl.LOGGER.info("Character: {}, Codepoint: {}, Width: {}",
                    String.format("U+%04X", (int) c), (int) c, width);
        }

        ReflectionHelper.FieldAccessor<FontRenderer, byte[]> glyphWidthHandle =
                rh.lookupField(FontRenderer.class, byte[].class, "glyphWidth", "field_78287_e")
                        .orElseThrow();
        glyphWidth = glyphWidthHandle.get(fr);
    }

    private static ITexture getUnicodePage(int page) {
        final ITexture texture = UNICODE_TEXTURES[page];
        if (texture != null) return texture;
        return UNICODE_TEXTURES[page] = RENDER_BACKEND.texture(
                "minecraft",
                String.format("textures/font/unicode_page_%02x.png", page)
        );
    }

    private static FontRenderer fontRenderer() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    private static int getCodepointIdx(int codepoint) {
        if (codepoint < MC_ASCII_CODEPOINTS.length) {
            return MC_ASCII_CODEPOINTS[codepoint];
        } else if (codepoint <= Character.MAX_VALUE) {
            return MC_UNI_CODEPOINTS.getOrDefault((char) codepoint, (short) -1);
        }
        return -1;
    }

    private static int getCharWidth(int codepoint) {
        if (codepoint < charWidth.length) {
            final int idx = getCodepointIdx(codepoint);
            return idx < 0 ? 0 : charWidth[idx];
        } else {
            final int w = glyphWidth[codepoint] & 255;
            return (((w & 15) + 1) - (w >>> 4)) / 2 + 1;
        }
    }

    @Override
    public boolean getGlyph(int codepoint, @OutArg GlyphData out) {
        int idx = getCodepointIdx(codepoint);

        if (idx != -1) {
            out.texture = ASCII_TEXTURE;
            out.u0 = (float) ((idx % 16) * 8) / 128f;
            out.v0 = (float) ((idx / 16) * 8) / 128f;
            int width = charWidth[idx];
            out.width = width - 1.01f;
            out.height = 7.99f; // ascii font is 8 high
            out.u1 = out.u0 + out.width / 128f;
            out.v1 = out.v0 + out.height / 128f;

            out.xOffset = 0;
            out.yOffset = 0;

            boolean isSpace = codepoint == ' ' || codepoint == '\u00a0' || codepoint == '\u202f';
            out.isSpace = isSpace;
            out.xAdvance = isSpace ? 4 : width;
            out.isAscii = true;
            out.xShadowOffset = 1;
            out.yShadowOffset = 1;
        } else {
            if (codepoint > glyphWidth.length) return false;
            int w = glyphWidth[codepoint];

            final int page = codepoint / 256;
            if (page >= UNICODE_TEXTURES.length) return false;
            out.texture = getUnicodePage(page);

            float startColumn = (float) (w >>> 4);
            float endColumn = (float) ((w & 15) + 1);
            out.u0 = ((float) (codepoint % 16 * 16) + startColumn) / 256.0f;
            out.v0 = ((float) ((codepoint & 255) / 16 * 16)) / 256.0f;
            float charWidth = endColumn - startColumn - 0.02F;
            float charHeight = 15.98f; // Unicode font is 16 high
            out.width = charWidth / 2.0f;
            out.height = charHeight / 2.0f;
            out.u1 = out.u0 + charWidth / 256.0f;
            out.v1 = out.v0 + charHeight / 256.0f;

            out.xOffset = 0;
            out.yOffset = 0;

            out.xAdvance = (endColumn - startColumn) / 2.0F + 1.0F;
            out.isSpace = codepoint == ' ' || codepoint == '\u00a0' || codepoint == '\u202f';
            out.isAscii = false;
            out.xShadowOffset = 0.5f;
            out.yShadowOffset = 0.5f;
        }

        return true;
    }

    @Override
    public int randomAsciiWithWidth(Random random, int codepoint) {
        final int width = getCharWidth(codepoint);
        int newIdx;
        int newCodepoint;
        do {
            newIdx = random.nextInt(charWidth.length);
            newCodepoint = Character.codePointAt(MC_ASCII_FONT_CHARS, newIdx);
        } while (getCharWidth(newCodepoint) != width);
        return newCodepoint;
    }
}

