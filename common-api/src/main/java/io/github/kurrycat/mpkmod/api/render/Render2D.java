package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface Render2D {
    static Render2D instance() {
        return ServiceManager.instance().get(Render2D.class);
    }

    void pushRect(float x, float y, float w, float h, int argb);
}
