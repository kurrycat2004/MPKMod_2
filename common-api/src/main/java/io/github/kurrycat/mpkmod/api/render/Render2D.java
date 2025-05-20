package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.service.TypedServiceProvider;

public interface Render2D {
    Render2D INSTANCE = TypedServiceProvider.loadOrThrow(Render2D.class);

    void pushRect(float x, float y, float w, float h, int argb);
}
