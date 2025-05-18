package io.github.kurrycat.mpkmod.render;

import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.ITexture;
import io.github.kurrycat.mpkmod.api.render.RenderMode;

public final class DrawCommand implements IDrawCommand {
    public int startIdx;
    public int count;
    public RenderMode mode;
    public ITexture texture;

    public DrawCommand(
            int startIdx,
            int count,
            RenderMode mode,
            ITexture texture
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
    public RenderMode mode() {return mode;}

    @Override
    public ITexture texture() {return texture;}
}