package io.github.kurrycat.mpkmod.api.render.text;

import io.github.kurrycat.mpkmod.annotation.OutArg;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;

import java.util.Random;

public interface GlyphProvider {
    static GlyphProvider instance() {
        return ServiceManager.instance().get(GlyphProvider.class);
    }

    final class GlyphData {
        public IResource texture;
        public float u0, v0, u1, v1;

        public float width, height;
        public float xOffset, yOffset;
        public float xAdvance;

        public boolean isEmpty;
        public boolean isAscii;

        public float xShadowOffset, yShadowOffset;

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
