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
        return ResourceManager.INSTANCE.resource(domain, path);
    }

    static IResource ofMc(String path) {
        return ResourceManager.INSTANCE.resource("minecraft", path);
    }

    static IResource ofSelf(String path) {
        return ResourceManager.INSTANCE.resource(ModPlatform.INSTANCE.modInfo().modId(), path);
    }

    default String readUtf8() throws IOException {
        try (InputStream stream = ResourceManager.INSTANCE.inputStream(this)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
