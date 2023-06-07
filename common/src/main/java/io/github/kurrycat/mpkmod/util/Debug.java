package io.github.kurrycat.mpkmod.util;

import io.github.kurrycat.mpkmod.compatibility.API;

public class Debug {
    public static void stacktrace(Object obj) {
        try {
            throw new Exception();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(obj.toString() + "\n");
            boolean skip = true;
            for (StackTraceElement s : e.getStackTrace()) {
                if(skip) {
                    skip = false;
                    continue;
                }
                sb.append("\tat ").append(s).append("\n");
            }
            API.LOGGER.info(sb.toString());
        }
    }

    public static void stacktraceFrom(Class<?> clazz, Object obj) {
        try {
            throw new Exception();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder(obj.toString() + "\n");
            boolean fromClass = false;
            boolean skip = true;
            for (StackTraceElement s : e.getStackTrace()) {
                if(skip) {
                    skip = false;
                    continue;
                }
                if (s.toString().startsWith(clazz.getName())) fromClass = true;
                sb.append("\tat ").append(s).append("\n");
            }
            if (fromClass)
                API.LOGGER.info(sb.toString());
        }
    }
}
