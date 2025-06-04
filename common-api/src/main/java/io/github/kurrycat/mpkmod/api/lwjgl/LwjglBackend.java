package io.github.kurrycat.mpkmod.api.lwjgl;

import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

public interface LwjglBackend {
    LwjglBackend INSTANCE = TypedServiceProvider.loadOrThrow(LwjglBackend.class);

    IGLCaps capabilities();

    IGL20 gl20();
}
