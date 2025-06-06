package io.github.kurrycat.mpkmod.service;

import io.github.kurrycat.mpkmod.api.service.ServiceProvider;

public final class RawServiceHolder<S> {
    private final Class<S> serviceType;
    private volatile ServiceProvider pending;
    private volatile S impl;

    public RawServiceHolder(Class<S> type, Object initial) {
        serviceType = type;
        impl = type.cast(initial);
    }

    public S current() {
        return impl;
    }

    public void readyForSwitch() {
        if (pending == null) return;
        switchToPending();
    }

    public void switchTo(ServiceProvider provider) {
        pending = provider;
        if (!provider.deferSwitch()) {
            switchToPending();
        }
    }

    private synchronized void switchToPending() {
        if (pending == null) return;
        Object service = pending.provide();
        impl = this.serviceType.cast(service);
        pending = null;
    }
}
