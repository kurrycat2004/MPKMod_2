package io.github.kurrycat.mpkmod.api.render;

import java.util.ServiceLoader;

public interface CommandReceiver {
    CommandReceiver INSTANCE = ServiceLoader.load(CommandReceiver.class).findFirst().orElseThrow();

    int currVtxIdx();

    int currIdx();

    void pushVtx(float x, float y, float z, int argb, float u, float v);

    void pushIdx(int idx);

    void pushDrawCmd(int startIdx, int count, RenderMode mode, IResourceLocation texture);

    void flushDrawCommands();
}
