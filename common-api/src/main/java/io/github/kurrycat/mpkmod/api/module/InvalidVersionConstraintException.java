package io.github.kurrycat.mpkmod.api.module;

public class InvalidVersionConstraintException extends Exception {
    public InvalidVersionConstraintException(String message) {
        super(message);
    }

    public InvalidVersionConstraintException(String message, Throwable cause) {
        super(message, cause);
    }
}