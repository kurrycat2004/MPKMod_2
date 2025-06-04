package io.github.kurrycat.mpkmod.log;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;

public final class StdoutLogManager implements LogManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<LogManager> {
        public Provider() {
            super(StdoutLogManager::new, LogManager.class);
        }

        @Override
        public int priority() {
            return -100;
        }
    }

    @Override
    public ILogger createLogger(String name) {
        return new StdoutLogger(name);
    }
}
