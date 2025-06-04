package io.github.kurrycat.mpkmod.lwjgl;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.lwjgl.IGLCaps;
import io.github.kurrycat.mpkmod.api.lwjgl.LwjglBackend;
import io.github.kurrycat.mpkmod.api.render.DrawMode;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.texture.TextureManager;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class GL11RenderBackend implements RenderBackend {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<RenderBackend> {
        public Provider() {
            super(GL11RenderBackend::new, RenderBackend.class);
        }

        @Override
        public int priority() {
            return 11;
        }

        @Override
        public Optional<String> invalidReason() {
            IGLCaps caps = LwjglBackend.INSTANCE.capabilities();
            if (!caps.OpenGL11()) {
                return Optional.of("Missing OpenGL 1.1 support");
            }
            if (!caps.OpenGL15() && !caps.GL_ARB_vertex_buffer_object()) {
                return Optional.of("Missing VBO support (OpenGL 1.5 or ARB_vbo)");
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

    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    private final GlStateSnapshot snapshot;

    public GL11RenderBackend() {
        vboPos = GL15.glGenBuffers();
        vboUV = GL15.glGenBuffers();
        vboCol = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();
        snapshot = new GlStateSnapshot();
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
    public FloatBuffer vertexPositions() {
        return vertexPositions;
    }

    @Override
    public ByteBuffer vertexColors() {
        return vertexColors;
    }

    @Override
    public FloatBuffer vertexUVs() {
        return vertexUVs;
    }

    @Override
    public IntBuffer indices() {
        return indices;
    }

    @Override
    public void flush(List<IDrawCommand> commands) {
        vertexPositions.flip();
        vertexUVs.flip();
        vertexColors.flip();
        indices.flip();

        snapshot.capture(); // ensure GlStateManager stays in sync

        IResource lastTex = null;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_FLAT);

        //TODO: check if GlStateManager.resetColor() is needed
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPos);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexPositions, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCol);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexColors, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUV);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexUVs, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_DYNAMIC_DRAW);

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboPos);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);

        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCol);
        GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 0, 0);

        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboUV);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        TextureManager textureManager = TextureManager.INSTANCE;
        for (IDrawCommand cmd : commands) {
            IResource tex = cmd.texture();
            if (!Objects.equals(tex, lastTex)) {
                if (tex != null) {
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    textureManager.bindTexture(tex);
                } else {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
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

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

        snapshot.restore();

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();
    }

    private static final class GlStateSnapshot {
        private boolean texture2D, blend, alphaTest, depthTest;
        private int blendSrc, blendDst;
        private int shadeModel;
        // no texture binding, TextureManager handles that
        private int arrayBufferBinding;
        private int elementArrayBufferBinding;

        public void capture() {
            texture2D = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            blend = GL11.glIsEnabled(GL11.GL_BLEND);
            alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);

            blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC);
            blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST);

            shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);

            arrayBufferBinding = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            elementArrayBufferBinding = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        }

        public void restore() {
            set(GL11.GL_TEXTURE_2D, texture2D);
            set(GL11.GL_BLEND, blend);
            set(GL11.GL_ALPHA_TEST, alphaTest);
            set(GL11.GL_DEPTH_TEST, depthTest);

            GL11.glBlendFunc(blendSrc, blendDst);
            GL11.glShadeModel(shadeModel);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBufferBinding);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementArrayBufferBinding);
        }

        private static void set(int cap, boolean enable) {
            if (enable) GL11.glEnable(cap);
            else GL11.glDisable(cap);
        }
    }
}
