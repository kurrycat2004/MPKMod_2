package io.github.kurrycat.mpkmod.log.slf4j;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.log.LoggerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class SLF4JLogManager implements LogManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<LogManager> {
        public Provider() {
            super(SLF4JLogManager::new, LogManager.class);
        }

        @Override
        public Optional<String> invalidReason() {
            if (!isClassLoaded("org.slf4j.LoggerFactory")) {
                return Optional.of("SLF4J is not available");
            }
            return Optional.empty();
        }

        @Override
        public int priority() {
            return 100;
        }
    }

    @Override
    public ILogger createLogger(String name) {
        return LoggerWrapper.wrap(
                new SLF4JLogger(LoggerFactory.getLogger(name))
        );
    }

    private record SLF4JLogger(Logger logger) implements ILogger {
        @Override
        public String name() {
            return logger.getName();
        }

        @Override
        public void log(Level level, String formatString) {
            switch (level) {
                case TRACE -> logger.trace(formatString);
                case DEBUG -> logger.debug(formatString);
                case INFO -> logger.info(formatString);
                case WARN -> logger.warn(formatString);
                case ERROR -> logger.error(formatString);
            }
        }

        @Override
        public void log(Level level, String formatString, Object var1) {
            switch (level) {
                case TRACE -> logger.trace(formatString, var1);
                case DEBUG -> logger.debug(formatString, var1);
                case INFO -> logger.info(formatString, var1);
                case WARN -> logger.warn(formatString, var1);
                case ERROR -> logger.error(formatString, var1);
            }
        }

        @Override
        public void log(Level level, String formatString, Object var1, Object var2) {
            switch (level) {
                case TRACE -> logger.trace(formatString, var1, var2);
                case DEBUG -> logger.debug(formatString, var1, var2);
                case INFO -> logger.info(formatString, var1, var2);
                case WARN -> logger.warn(formatString, var1, var2);
                case ERROR -> logger.error(formatString, var1, var2);
            }
        }

        @Override
        public void log(Level level, String formatString, Object... vars) {
            switch (level) {
                case TRACE -> logger.trace(formatString, vars);
                case DEBUG -> logger.debug(formatString, vars);
                case INFO -> logger.info(formatString, vars);
                case WARN -> logger.warn(formatString, vars);
                case ERROR -> logger.error(formatString, vars);
            }
        }
    }
}
