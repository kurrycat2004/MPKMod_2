package io.github.kurrycat.mpkmod.log;

import io.github.kurrycat.mpkmod.api.log.ILogger;

public final class LoggerWrapper implements ILogger {
    private static final boolean FORCE_MODID = Boolean.getBoolean("mpkmod.log.forceModId");

    private final ILogger logger;

    private LoggerWrapper(ILogger logger) {
        this.logger = logger;
    }

    public static ILogger wrap(ILogger logger) {
        if (FORCE_MODID) return new LoggerWrapper(logger);
        return logger;
    }

    @Override
    public String name() {
        return logger.name();
    }

    @Override
    public void log(Level level, String formatString) {
        logger.log(level, "[" + name() + "]: " + formatString);
    }

    @Override
    public void log(Level level, String formatString, Object var1) {
        logger.log(level, "[" + name() + "]: " + formatString, var1);
    }

    @Override
    public void log(Level level, String formatString, Object var1, Object var2) {
        logger.log(level, "[" + name() + "]: " + formatString, var1, var2);
    }

    @Override
    public void log(Level level, String formatString, Object... vars) {
        logger.log(level, "[" + name() + "]: " + formatString, vars);
    }
}
