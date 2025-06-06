package io.github.kurrycat.mpkmod.api.log;

import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface LogManager {
    static LogManager instance() {
        return ServiceManager.instance().get(LogManager.class);
    }

    ILogger createLogger(String name);
}
