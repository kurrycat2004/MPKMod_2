package io.github.kurrycat.mpkmod.api.service;

import java.util.Optional;

public interface ServiceProvider {
    Object provide();

    Class<?> type();

    default String name() {
        return getClass().getName();
    }

    default Optional<String> invalidReason() {
        return Optional.empty();
    }

    default int priority() {
        return 0;
    }

    /**
     * If this returns true, the switch will be deferred until
     * {@link ServiceManager#readyForSwitch(Class)} is called.
     *
     * @return true if the switch should be deferred, false otherwise
     */
    default boolean deferSwitch() {
        return false;
    }
}
