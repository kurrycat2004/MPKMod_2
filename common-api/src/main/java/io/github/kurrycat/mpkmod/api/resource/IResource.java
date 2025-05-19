package io.github.kurrycat.mpkmod.api.resource;

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
}
