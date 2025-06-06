package io.github.kurrycat.mpkmod.api.render;

import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceManager;

public interface CommandReceiver {
    static CommandReceiver instance() {
        return ServiceManager.instance().get(CommandReceiver.class);
    }

    int COMMAND_POOL_BATCH_SIZE = 64;
    int INITIAL_VERTEX_BUFFER_SIZE = 256;
    int INITIAL_INDEX_BUFFER_SIZE = 256;

    int currVtxIdx();

    int currIdx();

    void pushVtx(float x, float y, float z, int argb, float u, float v);

    void pushIdx(int idx);

    void pushDrawCmd(int startIdx, int count, DrawMode mode, IResource texture);

    void flushDrawCommands();
}
