package io.github.kurrycat.mpkmod.api.service;

import java.util.Optional;
import java.util.ServiceLoader;

public interface ServiceProvider {
    Object provide();

    Class<?> type();

    default Optional<String> invalidReason() {
        return Optional.empty();
    }

    default int priority() {
        return 0;
    }

    class CacheHolder {
        static final Cache CACHE;

        static {
            CACHE = ServiceLoader.load(Cache.class)
                    .findFirst()
                    .orElseThrow();
            CACHE.init();
        }
    }

    interface Cache {
        void init();

        Object rawLoadOrThrow(Class<?> providerClass);
    }
}
