package io.github.kurrycat.mpkmod.lwjgl;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.api.render.DrawMode;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.RenderState;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.lib.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
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
        assert RENDER_MODES.length == DrawMode.VALUES.length;
    }

    private final int vao;
    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    private final int shaderProgram;
    private final int uProjectionLoc;
    private final int uTexturedLoc;

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer projectionBuffer = BufferUtil.allocDirectFloat(16);

    private final GlStateSnapshot snapshot;

    public GL33RenderBackend() {
        vao = GL30.glGenVertexArrays();
        vboPos = GL15.glGenBuffers();
        vboUV = GL15.glGenBuffers();
        vboCol = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();
        snapshot = new GlStateSnapshot();

        GL30.glBindVertexArray(vao);
        bindAttrib(vboPos, 0, 3, GL11.GL_FLOAT, false);
        bindAttrib(vboCol, 1, 4, GL11.GL_UNSIGNED_BYTE, true);
        bindAttrib(vboUV, 2, 2, GL11.GL_FLOAT, false);
        GL30.glBindVertexArray(0);

        try {
            shaderProgram = ShaderUtil.createProgram(
                    IResource.ofSelf("shaders/gl33.vert"),
                    IResource.ofSelf("shaders/gl33.frag")
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shaders", e);
        }

        uProjectionLoc = GL20.glGetUniformLocation(shaderProgram, "uProjection");
        uTexturedLoc = GL20.glGetUniformLocation(shaderProgram, "uTextured");
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

        snapshot.capture();

        GL20.glUseProgram(shaderProgram);

        RenderState.INSTANCE.projectionMatrix(projectionMatrix);
        projectionMatrix.get(projectionBuffer);
        LwjglBackend.INSTANCE.gl20().glUniformMatrix4fv(uProjectionLoc, false, projectionBuffer);

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPos);
        GL20.glEnableVertexAttribArray(0);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPositions, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCol);
        GL20.glEnableVertexAttribArray(1);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexColors, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUV);
        GL20.glEnableVertexAttribArray(2);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexUVs, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_DYNAMIC_DRAW);

        TextureManager textureManager = TextureManager.INSTANCE;
        IResource lastTex = null;

        for (IDrawCommand cmd : commands) {
            IResource tex = cmd.texture();
            if (!Objects.equals(tex, lastTex)) {
                if (tex != null) {
                    GL20.glUniform1i(uTexturedLoc, 1);
                    textureManager.bindTexture(tex);
                } else {
                    GL20.glUniform1i(uTexturedLoc, 0);
                }
                lastTex = tex;
            }

            GL11.glDrawElements(
                    RENDER_MODES[cmd.mode().ordinal()],
                    cmd.count(),
                    GL11.GL_UNSIGNED_INT,
                    (long) cmd.startIdx() * Integer.BYTES
            );
        }

        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);

        snapshot.restore();

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();
    }

    private static void bindAttrib(int buffer, int index, int size, int type, boolean normalized) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        GL20.glEnableVertexAttribArray(index);
        GL20.glVertexAttribPointer(index, size, type, normalized, 0, 0L);
        GL20.glDisableVertexAttribArray(index);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static final class GlStateSnapshot {
        private int shaderProgram;
        private int vao;
        private int arrayBuffer;
        private int elementBuffer;

        void capture() {
            shaderProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            elementBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        }

        void restore() {
            GL20.glUseProgram(shaderProgram);
            GL30.glBindVertexArray(vao);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBuffer);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        }
    }
}
