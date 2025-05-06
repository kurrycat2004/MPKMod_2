package io.github.kurrycat.mpkmod.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ModuleLoadException extends Exception {
    public static final boolean ENABLE_STACKTRACE = Boolean.getBoolean("enableModuleLoadStacktrace");

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
        StringBuilder builder = new StringBuilder();
        builder.append(mainMessage);
        if (!errors.isEmpty()) {
            builder.append(" (")
                    .append(errors.size())
                    .append(" error")
                    .append(errors.size() > 1 ? "s" : "")
                    .append("):\n");
            for (ErrorDetail detail : errors) {
                builder.append(" - ").append(detail.message());
                if (detail.cause() != null) {
                    builder.append(" (caused by ")
                            .append(detail.cause().getClass().getSimpleName())
                            .append(": ")
                            .append(detail.cause().getMessage())
                            .append(")");
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public record ErrorDetail(String message, Throwable cause) {
        public ErrorDetail {
            Objects.requireNonNull(message, "Error message cannot be null");
        }
    }

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

        public Builder addError(String message, Throwable cause) {
            errors.add(new ErrorDetail(message, cause));
            return this;
        }

        public Builder addError(Throwable cause) {
            String exceptionName = cause.getClass().getName();
            String causeMsg = cause.getMessage();
            String msg = (causeMsg != null) ? (exceptionName + ": " + causeMsg) : exceptionName;
            errors.add(new ErrorDetail(msg, cause.getCause()));
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