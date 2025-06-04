package io.github.kurrycat.mpkmod.api.log;

import io.github.kurrycat.mpkmod.api.service.TypedServiceProvider;

public interface LogManager {
    LogManager INSTANCE = TypedServiceProvider.loadOrThrow(LogManager.class);

    ILogger createLogger(String name);
}
