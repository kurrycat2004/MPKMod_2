package io.github.kurrycat.mpkmod.api.render.text;

import io.github.kurrycat.mpkmod.annotation.OutArg;
import io.github.kurrycat.mpkmod.api.render.ITexture;

import java.util.Random;
import java.util.ServiceLoader;

public interface GlyphProvider {
    GlyphProvider INSTANCE = ServiceLoader.load(GlyphProvider.class).findFirst().orElseThrow();

    final class GlyphData {
        public ITexture texture;
        public float u0, v0, u1, v1;

        public float width, height;
        public float xOffset, yOffset;
        public float xAdvance;

        public boolean isEmpty;
        public boolean isAscii;

        public float xShadowOffset;
        public float yShadowOffset;

        public void reset() {
            texture = null;
            u0 = v0 = u1 = v1 = 0;
            width = height = xOffset = yOffset = xAdvance = 0;
            isEmpty = isAscii = false;
            xShadowOffset = yShadowOffset = 0;
        }
    }

    boolean getGlyph(int codepoint, @OutArg GlyphData out);

    int randomAsciiWithWidth(Random random, int codepoint);
}
