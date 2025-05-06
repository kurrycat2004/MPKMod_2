package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.render.RenderMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

@AutoService(RenderBackend.class)
public class RenderBackendImpl implements RenderBackend {
    private static final int[] RENDER_MODES = {
            GL11.GL_TRIANGLES,
            GL11.GL_LINES,
    };

    static {
        assert RENDER_MODES.length == RenderMode.VALUES.length;
    }

    private final int vboPos, vboUV, vboCol, ebo;
    private FloatBuffer vertexPositions;
    private ByteBuffer vertexColors;
    private FloatBuffer vertexUVs;
    private IntBuffer indices;

    public RenderBackendImpl() {
        vboPos = GL15.glGenBuffers();
        vboUV = GL15.glGenBuffers();
        vboCol = GL15.glGenBuffers();
        ebo = GL15.glGenBuffers();
    }

    private static ByteBuffer allocateDirect(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    @Override
    public void reallocVertexBuffers(int posSizeFloats, int colorSizeBytes, int uvSizeFloats) {
        FloatBuffer oldPos = this.vertexPositions;
        FloatBuffer newPos = allocateDirect(posSizeFloats * Float.BYTES).asFloatBuffer();
        if (oldPos != null) {
            oldPos.flip();
            newPos.put(oldPos);
        }
        this.vertexPositions = newPos;

        FloatBuffer oldUv = this.vertexUVs;
        FloatBuffer newUv = allocateDirect(uvSizeFloats * Float.BYTES).asFloatBuffer();
        if (oldUv != null) {
            oldUv.flip();
            newUv.put(oldUv);
        }
        this.vertexUVs = newUv;

        ByteBuffer oldCol = this.vertexColors;
        ByteBuffer newCol = allocateDirect(colorSizeBytes);
        if (oldCol != null) {
            oldCol.flip();
            newCol.put(oldCol);
        }
        this.vertexColors = newCol;
    }

    @Override
    public void reallocIndexBuffer(int sizeInts) {
        IntBuffer oldIdx = this.indices;
        IntBuffer newIdx = allocateDirect(sizeInts * Integer.BYTES).asIntBuffer();
        if (oldIdx != null) {
            oldIdx.flip();
            newIdx.put(oldIdx);
        }
        this.indices = newIdx;
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

        //TextureManager texMgr = Minecraft.getMinecraft().getTextureManager();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        for (IDrawCommand cmd : commands) {
            //TODO: textures
            //IResourceLocation loc = cmd.texture();
            // assume IResourceLocation is a net.minecraft.util.ResourceLocation
            //texMgr.bindTexture((ResourceLocation) loc);

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

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        vertexPositions.clear();
        vertexUVs.clear();
        vertexColors.clear();
        indices.clear();
    }
}
