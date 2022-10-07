package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;

import java.awt.*;
import java.util.Optional;

public class Renderer3D {
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
