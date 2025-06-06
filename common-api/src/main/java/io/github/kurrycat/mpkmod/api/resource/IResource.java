package io.github.kurrycat.mpkmod.api.resource;

import io.github.kurrycat.mpkmod.api.ModPlatform;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public interface IResource {
    String domain();

    String path();

    Object backendResource();

    static IResource of(String domain, String path) {
        return ResourceManager.instance().resource(domain, path);
    }

    static IResource ofMc(String path) {
        return ResourceManager.instance().resource("minecraft", path);
    }

    static IResource ofSelf(String path) {
        return ResourceManager.instance().resource(ModPlatform.instance().modInfo().modId(), path);
    }

    default String readUtf8() throws IOException {
        try (InputStream stream = ResourceManager.instance().inputStream(this)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
