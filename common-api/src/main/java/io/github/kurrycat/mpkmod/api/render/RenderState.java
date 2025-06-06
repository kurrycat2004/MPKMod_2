package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;
import io.github.kurrycat.mpkmod.lib.joml.Matrix4f;

public interface RenderState {
    static RenderState instance() {
        return ServiceManager.instance().get(RenderState.class);
    }

    RenderLayer layer();

    void projectionMatrix(Matrix4f out);
}
