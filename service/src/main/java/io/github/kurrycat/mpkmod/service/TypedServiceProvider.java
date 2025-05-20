package io.github.kurrycat.mpkmod.service;

public interface TypedServiceProvider<T> extends ServiceProvider {
    T provide();

    Class<T> type();

    @SuppressWarnings("unchecked")
    static <T> T loadOrThrow(Class<T> providerClass) {
        return (T) CACHE.rawLoadOrThrow(providerClass);
    }
}
