package io.github.kurrycat.mpkmod.api.render.text;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;
import org.intellij.lang.annotations.MagicConstant;

public interface TextRenderer {
    static TextRenderer instance() {
        return ServiceManager.instance().get(TextRenderer.class);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    final class Style {
        public static final int OBFUSCATED = 1 << 0;
        public static final int BOLD = 1 << 1;
        public static final int STRIKETHROUGH = 1 << 2;
        public static final int UNDERLINE = 1 << 3;
        public static final int ITALIC = 1 << 4;
        public static final int SHADOW = 1 << 5;
    }

    float drawString(GlyphProvider.GlyphData buffer, float x, float y, int color, @MagicConstant(flagsFromClass = Style.class) int style, CharSequence text, int textOffset, int textLength);

    float drawFormattedString(GlyphProvider.GlyphData buffer, float x, float y, int color, boolean enableShadow, CharSequence text);

    default float drawFormattedString(float x, float y, int color, boolean enableShadow, CharSequence text) {
        return drawFormattedString(new GlyphProvider.GlyphData(), x, y, color, enableShadow, text);
    }
}
