package io.github.kurrycat.mpkmod.api.render.texture;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

public interface TextureManager {
    TextureManager INSTANCE = TypedServiceProvider.loadOrThrow(TextureManager.class);

    void bindTexture(IResource texture);
}
