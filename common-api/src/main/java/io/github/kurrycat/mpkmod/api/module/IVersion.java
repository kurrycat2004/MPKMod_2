package io.github.kurrycat.mpkmod.api.module;

public interface IVersion extends Comparable<IVersion> {
    boolean satisfies(IVersionConstraint range);
}
