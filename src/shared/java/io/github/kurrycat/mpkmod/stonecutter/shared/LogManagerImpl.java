package io.github.kurrycat.mpkmod.stonecutter.shared;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.log.ILogger;
import io.github.kurrycat.mpkmod.api.log.LogManager;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;

public class LogManagerImpl implements LogManager {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<LogManager> {
        public Provider() {
            super(LogManagerImpl::new, LogManager.class);
        }
    }

    @Override
    public ILogger getLogger(String s) {
        return new Log4JLogger(org.apache.logging.log4j.LogManager.getLogger(s));
    }

    private record Log4JLogger(org.apache.logging.log4j.Logger logger) implements ILogger {
        private static final org.apache.logging.log4j.Level[] LEVEL_MAP = {
                org.apache.logging.log4j.Level.TRACE,
                org.apache.logging.log4j.Level.DEBUG,
                org.apache.logging.log4j.Level.INFO,
                org.apache.logging.log4j.Level.WARN,
                org.apache.logging.log4j.Level.ERROR
        };

        @Override
        public void log(Level level, String formatString) {
            logger.log(LEVEL_MAP[level.ordinal()], formatString);
        }

        @Override
        public void log(Level level, String formatString, Object var1) {
            logger.log(LEVEL_MAP[level.ordinal()], formatString, var1);
        }

        @Override
        public void log(Level level, String formatString, Object var1, Object var2) {
            logger.log(LEVEL_MAP[level.ordinal()], formatString, var1, var2);
        }

        @Override
        public void log(Level level, String formatString, Object... vars) {
            logger.log(LEVEL_MAP[level.ordinal()], formatString, vars);
        }
    }
}
