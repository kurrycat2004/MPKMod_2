package io.github.kurrycat.mpkmod.render;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.DrawMode;
import io.github.kurrycat.mpkmod.api.render.IDrawCommand;
import io.github.kurrycat.mpkmod.api.render.RenderBackend;
import io.github.kurrycat.mpkmod.api.resource.IResource;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public final class CommandReceiverImpl implements CommandReceiver {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<CommandReceiver> {
        public Provider() {
            super(CommandReceiverImpl::new, CommandReceiver.class);
        }
    }

    public static final Comparator<IDrawCommand> DRAW_COMMAND_COMPARATOR = Comparator.comparing(
            IDrawCommand::texture,
            Comparator.nullsLast(
                    Comparator.comparing(IResource::domain)
                            .thenComparing(IResource::path))
    ).thenComparing(IDrawCommand::startIdx);

    private final Deque<IDrawCommand> commandPool = new ArrayDeque<>();
    private final List<IDrawCommand> commands = new ArrayList<>();

    public CommandReceiverImpl() {
        ensureCommandPoolNonEmpty();
    }

    @Override
    public int currVtxIdx() {
        return RenderBackend.instance().vertexPositions().position() / 3;
    }

    @Override
    public int currIdx() {
        return RenderBackend.instance().indices().position();
    }

    @Override
    public void pushVtx(float x, float y, float z, int argb, float u, float v) {
        final RenderBackend backend = RenderBackend.instance();
        FloatBuffer pos = backend.vertexPositions();
        if (pos.remaining() < 3) {
            backend.reallocVertexBuffers(
                    pos.capacity() * 2,
                    backend.vertexColors().capacity() * 2,
                    backend.vertexUVs().capacity() * 2
            );
        }
        backend.vertexPositions().put(x).put(y).put(z);
        backend.vertexColors()
                .put((byte) (argb >> 16))
                .put((byte) (argb >> 8))
                .put((byte) argb)
                .put((byte) (argb >> 24));
        backend.vertexUVs().put(u).put(v);
    }

    @Override
    public void pushIdx(int idx) {
        final RenderBackend backend = RenderBackend.instance();
        if (backend.indices().remaining() < 1) {
            backend.reallocIndexBuffer(backend.indices().capacity() * 2);
        }
        backend.indices().put(idx);
    }

    private boolean tryMergeCommand(int startIdx, int count, DrawMode mode, IResource texture) {
        if (commands.isEmpty()) return false;
        DrawCommand lastCommand = (DrawCommand) commands.getLast();
        if (lastCommand.startIdx + lastCommand.count != startIdx) return false;
        if (lastCommand.mode != mode) return false;
        if (!Objects.equals(lastCommand.texture, texture)) return false;
        lastCommand.count += count;
        return true;
    }

    private void ensureCommandPoolNonEmpty() {
        if (!commandPool.isEmpty()) return;
        for (int i = 0; i < COMMAND_POOL_BATCH_SIZE; i++) {
            commandPool.push(new DrawCommand(0, 0, DrawMode.TRIANGLES, null));
        }
    }

    @Override
    public void pushDrawCmd(int startIdx, int count, DrawMode mode, IResource texture) {
        if (count == 0) return;
        if (tryMergeCommand(startIdx, count, mode, texture)) return;
        ensureCommandPoolNonEmpty();
        DrawCommand command = (DrawCommand) commandPool.pop();
        command.startIdx = startIdx;
        command.count = count;
        command.mode = mode;
        command.texture = texture;
        commands.add(command);
    }

    @Override
    public void flushDrawCommands() {
        commands.sort(DRAW_COMMAND_COMPARATOR);
        RenderBackend.instance().flush(commands);
        for (IDrawCommand command : commands) {
            commandPool.push(command);
        }
        commands.clear();
    }
}
