package io.github.kurrycat.mpkmod.api.service;

public interface TypedServiceProvider<T> extends ServiceProvider {
    T provide();

    Class<T> type();
}
