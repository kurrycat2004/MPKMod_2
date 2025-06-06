package io.github.kurrycat.mpkmod.api.render.texture;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface TextureManager {
    static TextureManager instance() {
        return ServiceManager.instance().get(TextureManager.class);
    }

    void bindTexture(IResource texture);
}
