package io.github.kurrycat.mpkmod.stonecutter.vintage_forge;

import com.google.auto.service.AutoService;
import io.github.kurrycat.mpkmod.api.render.RenderLayer;
import io.github.kurrycat.mpkmod.api.render.RenderState;
import io.github.kurrycat.mpkmod.api.service.ServiceProvider;
import io.github.kurrycat.mpkmod.api.service.StandardServiceProvider;
import io.github.kurrycat.mpkmod.lib.joml.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public final class RenderStateImpl implements RenderState {
    @AutoService(ServiceProvider.class)
    public static final class Provider extends StandardServiceProvider<RenderState> {
        public Provider() {
            super(RenderStateImpl::new, RenderState.class);
        }
    }

    private RenderLayer layer = RenderLayer.UI;

    @Override
    public RenderLayer layer() {
        return layer;
    }

    @Override
    public void projectionMatrix(Matrix4f out) {
        switch (layer) {
            case UI -> {
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                double width = sr.getScaledWidth_double();
                double height = sr.getScaledHeight_double();

                out.setOrtho(
                        0.0f, (float) width,
                        (float) height, 0.0f,
                        -1000.0f, 1000.0f
                );
            }
            case WORLD -> throw new UnsupportedOperationException("World projection matrix not implemented yet");
        }
    }
}
