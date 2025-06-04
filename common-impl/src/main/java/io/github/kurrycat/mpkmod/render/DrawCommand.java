package io.github.kurrycat.mpkmod.render;

import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.render.DrawMode;

public final class DrawCommand implements IDrawCommand {
    public int startIdx;
    public int count;
    public DrawMode mode;
    public IResource texture;

    public DrawCommand(
            int startIdx,
            int count,
            DrawMode mode,
            IResource texture
    ) {
        this.startIdx = startIdx;
        this.count = count;
        this.mode = mode;
        this.texture = texture;
    }

    @Override
    public int startIdx() {return startIdx;}

    @Override
    public int count() {return count;}

    @Override
    public DrawMode mode() {return mode;}

    @Override
    public IResource texture() {return texture;}
}