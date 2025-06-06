package io.github.kurrycat.mpkmod.api.resource;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

import java.io.IOException;
import java.io.InputStream;

public interface ResourceManager {
    static ResourceManager instance() {
        return ServiceManager.instance().get(ResourceManager.class);
    }

    IResource resource(String domain, String path);

    InputStream inputStream(IResource resource) throws IOException;
}
