package io.github.kurrycat.mpkmod.log;

import io.github.kurrycat.mpkmod.Tags;
import io.github.kurrycat.mpkmod.api.log.ILogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record StdoutLogger(String name) implements ILogger {
    public static final StdoutLogger FALLBACK = new StdoutLogger(Tags.MOD_ID);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public ILogger createSubLogger(String name) {
        return new StdoutLogger(this.name + "/" + name);
    }

    @Override
    public void log(Level level, String formatString) {
        print(level, formatString, null);
    }

    @Override
    public void log(Level level, String formatString, Object var1) {
        if (var1 instanceof Throwable t) {
            print(level, formatString, t);
        } else {
            print(level, formatStringWithArgs(formatString, new Object[]{var1}, 1), null);
        }
    }

    @Override
    public void log(Level level, String formatString, Object var1, Object var2) {
        if (var2 instanceof Throwable t) {
            print(level, formatStringWithArgs(formatString, new Object[]{var1}, 1), t);
        } else {
            print(level, formatStringWithArgs(formatString, new Object[]{var1, var2}, 2), null);
        }
    }

    @Override
    public void log(Level level, String formatString, Object... vars) {
        int argCount = vars == null ? 0 : vars.length;
        Throwable throwable = null;
        int usableArgs = argCount;

        if (argCount > 0 && vars[argCount - 1] instanceof Throwable t) {
            throwable = t;
            usableArgs--;
        }

        print(level, formatStringWithArgs(formatString, vars, usableArgs), throwable);
    }

    private void print(Level level, String message, Throwable throwable) {
        String timestamp = LocalTime.now().format(TIME_FORMATTER);
        String threadName = Thread.currentThread().getName();
        System.out.print("[" + timestamp + "] [" + threadName + "/" + level.name() + "] [" + name + "]: " + message + "\n");

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            System.out.print(sw);
        }
    }

    private String formatStringWithArgs(String formatString, Object[] args, int limit) {
        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int lastIndex = 0;
        int nextPlaceholder;

        while ((nextPlaceholder = formatString.indexOf("{}", lastIndex)) != -1 && argIndex < limit) {
            sb.append(formatString, lastIndex, nextPlaceholder);
            sb.append(args[argIndex++]);
            lastIndex = nextPlaceholder + 2;
        }

        sb.append(formatString.substring(lastIndex));
        return sb.toString();
    }
}
