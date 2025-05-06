package io.github.kurrycat.mpkmod.api.render;

import java.util.ServiceLoader;

public interface Render2D {
    Render2D INSTANCE = ServiceLoader.load(Render2D.class).findFirst().orElseThrow();

    void pushRect(float x, float y, float w, float h, int argb);
}
