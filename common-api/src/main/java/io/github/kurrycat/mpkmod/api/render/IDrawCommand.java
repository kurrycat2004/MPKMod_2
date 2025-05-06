package io.github.kurrycat.mpkmod.api.render;

import java.util.Objects;

public interface IDrawCommand {
    int startIdx();

    int count();

    RenderMode mode();

    IResourceLocation texture();

    default boolean canMergeBefore(int startIdx, RenderMode mode, IResourceLocation texture) {
        return this.mode() == mode && Objects.equals(this.texture(), texture) && this.startIdx() + this.count() == startIdx;
    }
}
