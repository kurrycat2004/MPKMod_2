package io.github.kurrycat.mpkmod.api.lwjgl;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface LwjglBackend {
    static LwjglBackend instance() {
        return ServiceManager.instance().get(LwjglBackend.class);
    }

    IGLCaps capabilities();

    IGL20 gl20();
}
