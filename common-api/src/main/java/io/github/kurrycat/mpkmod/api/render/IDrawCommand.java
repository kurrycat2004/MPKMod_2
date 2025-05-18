package io.github.kurrycat.mpkmod.api.render;

public interface IDrawCommand {
    int startIdx();

    int count();

    RenderMode mode();

    ITexture texture();
}
