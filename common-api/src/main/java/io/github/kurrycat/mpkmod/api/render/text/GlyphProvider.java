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

        public int sizeX, sizeY;
        public float renderWidth, renderHeight;
        public float xOffset, yOffset;
        public float xAdvance;

        public boolean isEmpty;
        public boolean isAscii;

        public void reset() {
            texture = null;
            u0 = v0 = u1 = v1 = 0;
            sizeX = sizeY = 0;
            renderWidth = renderHeight = 0;
            xOffset = yOffset = 0;
            xAdvance = 0;
            isEmpty = isAscii = false;
        }
    }

    boolean getGlyph(int codepoint, @OutArg GlyphData out);

    int randomGlyphWithXAdvance(Random random, float xAdvance);
}
