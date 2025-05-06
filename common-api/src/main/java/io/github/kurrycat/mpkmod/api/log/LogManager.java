package io.github.kurrycat.mpkmod.api.log;

import java.util.ServiceLoader;

public interface LogManager {
    LogManager INSTANCE = ServiceLoader.load(LogManager.class).findFirst().orElseThrow();

    ILogger getLogger(String name);
}
