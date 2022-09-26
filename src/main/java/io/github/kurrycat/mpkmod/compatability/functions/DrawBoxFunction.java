package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;

import java.awt.*;

@FunctionalInterface
public interface DrawBoxFunction {
    void apply(BoundingBox3D boundingBox, Color color, float partialTicks);
}
