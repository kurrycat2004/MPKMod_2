package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;
import io.github.kurrycat.mpkmod.lib.joml.Matrix4f;

public interface RenderState {
    RenderState INSTANCE = TypedServiceProvider.loadOrThrow(RenderState.class);

    RenderLayer layer();

    void projectionMatrix(Matrix4f out);
}
