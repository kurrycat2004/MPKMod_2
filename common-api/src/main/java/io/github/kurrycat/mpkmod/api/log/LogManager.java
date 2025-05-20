package io.github.kurrycat.mpkmod.api.log;

import io.github.kurrycat.mpkmod.service.TypedServiceProvider;

public interface LogManager {
    LogManager INSTANCE = TypedServiceProvider.loadOrThrow(LogManager.class);

    ILogger getLogger(String name);
}
