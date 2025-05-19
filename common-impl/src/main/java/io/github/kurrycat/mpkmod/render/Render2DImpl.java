package io.github.kurrycat.mpkmod.render;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.CommandReceiver;
import io.github.kurrycat.mpkmod.api.render.Render2D;
import io.github.kurrycat.mpkmod.api.render.RenderMode;
import io.github.kurrycat.mpkmod.api.service.DefaultServiceProvider;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;

public final class Render2DImpl implements Render2D {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends DefaultServiceProvider<Render2D> {
        public Provider() {
            super(Render2DImpl::new, Render2D.class);
        }
    }

    private final CommandReceiver cmd = CommandReceiver.INSTANCE;

    @Override
    public void pushRect(float x, float y, float w, float h, int argb) {
        int startVtx = cmd.currVtxIdx();
        cmd.pushVtx(x, y, 0.0f, argb, 0, 0);
        cmd.pushVtx(x, y + h, 0.0f, argb, 0, 0);
        cmd.pushVtx(x + w, y, 0.0f, argb, 0, 0);
        cmd.pushVtx(x + w, y + h, 0.0f, argb, 0, 0);
        int startIdx = cmd.currIdx();
        cmd.pushIdx(startVtx);
        cmd.pushIdx(startVtx + 1);
        cmd.pushIdx(startVtx + 2);
        cmd.pushIdx(startVtx + 2);
        cmd.pushIdx(startVtx + 1);
        cmd.pushIdx(startVtx + 3);
        cmd.pushDrawCmd(startIdx, 6, RenderMode.TRIANGLES, null);
    }
}
