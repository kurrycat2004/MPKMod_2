package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.service.TypedServiceProvider;

public interface CommandReceiver {
    CommandReceiver INSTANCE = TypedServiceProvider.loadOrThrow(CommandReceiver.class);

    int currVtxIdx();

    int currIdx();

    void pushVtx(float x, float y, float z, int argb, float u, float v);

    void pushIdx(int idx);

    void pushDrawCmd(int startIdx, int count, RenderMode mode, IResource texture);

    void flushDrawCommands();
}
