package io.github.kurrycat.mpkmod.compatability.functions;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.BoundingBox;

import java.awt.*;

@FunctionalInterface
public interface DrawBoxFunction {
    void apply(BoundingBox boundingBox, Color color, Player player, float partialTicks);
}
