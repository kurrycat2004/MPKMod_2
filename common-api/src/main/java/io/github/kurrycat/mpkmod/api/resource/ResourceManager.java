package io.github.kurrycat.mpkmod.api.resource;

import io.github.kurrycat.mpkmod.api.ModPlatform;
import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceManager {
    ResourceManager INSTANCE = TypedServiceProvider.loadOrThrow(ResourceManager.class);

    IResource resource(String domain, String path);

    default IResource modResource(String path) {
        return resource(ModPlatform.INSTANCE.modInfo().modId(), path);
    }

    InputStream inputStream(IResource resource) throws IOException;
}
