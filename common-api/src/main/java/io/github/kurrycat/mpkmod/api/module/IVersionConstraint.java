package io.github.kurrycat.mpkmod.api.module;

public interface IVersionConstraint {
    boolean isSatisfiedBy(IVersion version);
}
