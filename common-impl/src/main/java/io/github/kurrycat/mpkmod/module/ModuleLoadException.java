package io.github.kurrycat.mpkmod.module;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleLoadException extends Exception {
    public static final boolean ENABLE_STACKTRACE = Boolean.getBoolean("mpkmod.module.enableModuleLoadStacktrace");

    private final String mainMessage;
    private final List<ErrorDetail> errors;

    public ModuleLoadException(String mainMessage) {
        this(mainMessage, List.of());
    }

    public ModuleLoadException(String mainMessage, List<ErrorDetail> errors) {
        super(null, null, false, ENABLE_STACKTRACE);
        this.mainMessage = Objects.requireNonNull(mainMessage, "Main message cannot be null");
        this.errors = List.copyOf(Objects.requireNonNull(errors));
    }

    public ModuleLoadException(String mainMessage, Throwable cause) {
        this(new Builder(mainMessage).addError(cause));
    }

    private ModuleLoadException(Builder builder) {
        super(null, null, false, ENABLE_STACKTRACE);
        this.mainMessage = builder.mainMessage;
        this.errors = builder.errors;
    }

    public String mainMessage() {
        return mainMessage;
    }

    public List<ErrorDetail> errors() {
        return errors;
    }

    @Override
    public String getMessage() {
        StringWriter builder = new StringWriter();
        builder.append(mainMessage);
        if (!errors.isEmpty()) {
            builder.append(" (")
                    .append(String.valueOf(errors.size()))
                    .append(" error")
                    .append(errors.size() > 1 ? "s" : "")
                    .append("):\n");
            for (ErrorDetail detail : errors) {
                if (detail.message() != null) {
                    builder.append(detail.message());
                    if (detail.error() != null) builder.append(": ");
                }
                if (detail.error() == null) {
                    builder.append("\n");
                    continue;
                }
                if (!ENABLE_STACKTRACE) {
                    builder.append(detail.error().toString());
                } else {
                    try (PrintWriter pw = new PrintWriter(builder)) {
                        detail.error().printStackTrace(pw);
                    } catch (Exception e) {
                        builder.append(detail.error().toString()).append("\n");
                        builder.append("\t(failed to print stack trace: ").append(e.toString()).append(")");
                    }
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public record ErrorDetail(String message, Throwable error) {}

    public static class Builder {
        private final String mainMessage;
        private final List<ErrorDetail> errors = new ArrayList<>();

        public Builder(String mainMessage) {
            this.mainMessage = Objects.requireNonNull(mainMessage);
        }

        public Builder addError(String message) {
            errors.add(new ErrorDetail(message, null));
            return this;
        }

        public Builder addError(String message, Throwable error) {
            errors.add(new ErrorDetail(message, error));
            return this;
        }

        public Builder addError(Throwable error) {
            errors.add(new ErrorDetail(null, error));
            return this;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public ModuleLoadException build() {
            return new ModuleLoadException(this);
        }
    }
}