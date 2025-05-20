package io.github.kurrycat.mpkmod.lwjgl;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.RenderMode;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.service.ServiceProvider;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GL33RenderBackend implements RenderBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<RenderBackend> {
        public Provider() {
            super(GL33RenderBackend::new, RenderBackend.class);
        }

        @Override
        public int priority() {
            return 33;
        }

        @Override
        public Optional<String> invalidReason() {
            if (true) {
                //TODO: implement
                return Optional.of("Not yet implemented");
            }
            IGLCaps caps = LwjglBackend.INSTANCE.capabilities();
            if (!caps.OpenGL33()) {
                return Optional.of("OpenGL 3.3 not supported");
            }
            return Optional.empty();
        }
    }

    private static final int[] RENDER_MODES = {
            GL11.GL_TRIANGLES,
            GL11.GL_LINES,
    };

    static {
        assert RENDER_MODES.length == RenderMode.VALUES.length;
    }

    private final int vao;
    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    //private final ShaderProgram shader;

    public GL33RenderBackend() {
        vao = GL30.glGenVertexArrays();
        vboPos = GL15.glGenBuffers();
        vboUV = GL15.glGenBuffers();
        vboCol = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();

        //shader = ShaderManager.load("modern_render"); // you implement this
    }

    @Override
    public void reallocVertexBuffers(int posSizeFloats, int colorSizeBytes, int uvSizeFloats) {
        vertexPositions = BufferUtil.reallocFloat(vertexPositions, posSizeFloats);
        vertexUVs = BufferUtil.reallocFloat(vertexUVs, uvSizeFloats);
        vertexColors = BufferUtil.reallocByte(vertexColors, colorSizeBytes);
    }

    @Override
    public void reallocIndexBuffer(int sizeInts) {
        indices = BufferUtil.reallocInt(indices, sizeInts);
    }

    @Override
    public FloatBuffer vertexPositions() {return vertexPositions;}

    @Override
    public ByteBuffer vertexColors() {return vertexColors;}

    @Override
    public FloatBuffer vertexUVs() {return vertexUVs;}

    @Override
    public IntBuffer indices() {return indices;}

    @Override
    public void flush(List<IDrawCommand> commands) {
        vertexPositions.flip();
        vertexUVs.flip();
        vertexColors.flip();
        indices.flip();

        GL30.glBindVertexArray(vao);

        upload(GL15.GL_ARRAY_BUFFER, vboPos, vertexPositions);
        upload(GL15.GL_ARRAY_BUFFER, vboUV, vertexUVs);
        upload(GL15.GL_ARRAY_BUFFER, vboCol, vertexColors);
        upload(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo, indices);

        //shader.bind();
        setupAttribs();

        TextureManager textureManager = TextureManager.INSTANCE;
        IResource lastTex = null;

        for (IDrawCommand cmd : commands) {
            IResource tex = cmd.texture();
            if (!Objects.equals(tex, lastTex)) {
                textureManager.bindTexture(tex);
                //shader.setUniform("uTextured", tex != null);
                lastTex = tex;
            }

            GL11.glDrawElements(
                    RENDER_MODES[cmd.mode().ordinal()],
                    cmd.count(),
                    GL11.GL_UNSIGNED_INT,
                    (long) cmd.startIdx() * Integer.BYTES
            );
        }

        GL30.glBindVertexArray(0);
        //shader.unbind();

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();
    }

    private void upload(int target, int buffer, Buffer data) {
        GL15.glBindBuffer(target, buffer);
        if (data instanceof FloatBuffer fb) {
            GL15.glBufferData(target, fb, GL15.GL_DYNAMIC_DRAW);
        } else if (data instanceof ByteBuffer bb) {
            GL15.glBufferData(target, bb, GL15.GL_DYNAMIC_DRAW);
        } else if (data instanceof IntBuffer ib) {
            GL15.glBufferData(target, ib, GL15.GL_DYNAMIC_DRAW);
        }
    }

    private void setupAttribs() {
        bindAttrib(vboPos, 0, 3, GL11.GL_FLOAT, false);
        bindAttrib(vboCol, 1, 4, GL11.GL_UNSIGNED_BYTE, true);
        bindAttrib(vboUV, 2, 2, GL11.GL_FLOAT, false);
    }

    private void bindAttrib(int buffer, int index, int size, int type, boolean normalized) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, size, type, normalized, 0, 0L);
    }
}
