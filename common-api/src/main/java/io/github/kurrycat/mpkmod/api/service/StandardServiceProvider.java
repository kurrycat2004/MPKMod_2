package io.github.kurrycat.mpkmod.api.service;

import java.util.function.Supplier;

public abstract class StandardServiceProvider<T> implements TypedServiceProvider<T> {
    private final Supplier<T> provider;
    private final Class<T> type;

    protected StandardServiceProvider(Supplier<T> provider, Class<T> type) {
        this.provider = provider;
        this.type = type;
    }

    @Override
    public T provide() {
        return provider.get();
    }

    @Override
    public Class<T> type() {
        return type;
    }

    protected final boolean isClassLoaded(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
