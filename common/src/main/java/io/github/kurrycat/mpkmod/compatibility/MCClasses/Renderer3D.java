package io.github.kurrycat.mpkmod.compatibility.MCClasses;

import io.github.kurrycat.mpkmod.compatibility.API;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;

import java.awt.*;
import java.util.Optional;

public class Renderer3D {
    /**
     * Should only be called in an {@link io.github.kurrycat.mpkmod.events.OnRenderWorldOverlayEvent OnRenderWorldOverlayEvent}
     *
     * @param boundingBox bounding box that should be drawn in the world
     * @param color color of the resulting box
     * @param partialTicks partialTicks provided by the Event in which the method was called<br>
     *                     It is needed because minecraft interpolates its camera position by rendering everything at <code>lastPos + (pos - lastPos) * partialTicks</code> to make it seem smoother
     */
    public static void drawBox(BoundingBox3D boundingBox, Color color, float partialTicks) {
        Optional<Interface> renderer = Interface.get();
        renderer.ifPresent(renderer3DInterface -> renderer3DInterface.drawBox(boundingBox, color, partialTicks));
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        void drawBox(BoundingBox3D boundingBox, Color color, float partialTicks);
    }
}
