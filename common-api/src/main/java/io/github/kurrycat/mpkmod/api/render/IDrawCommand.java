package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.resource.IResource;

public interface IDrawCommand {
    int startIdx();

    int count();

    RenderMode mode();

    IResource texture();
}
