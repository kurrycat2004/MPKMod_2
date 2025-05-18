package io.github.kurrycat.mpkmod.api.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.ServiceLoader;

public interface RenderBackend {
    RenderBackend INSTANCE = ServiceLoader.load(RenderBackend.class).findFirst().orElseThrow();

    ITexture texture(String domain, String path);

    void reallocVertexBuffers(int posSize, int colorSize, int uvSize);

    void reallocIndexBuffer(int size);

    FloatBuffer vertexPositions();

    ByteBuffer vertexColors();

    FloatBuffer vertexUVs();

    IntBuffer indices();

    void flush(List<IDrawCommand> commands);
}
