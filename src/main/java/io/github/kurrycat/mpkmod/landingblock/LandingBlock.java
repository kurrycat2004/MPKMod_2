package io.github.kurrycat.mpkmod.landingblock;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LandingBlock {
    public LandingMode landingMode = LandingMode.LAND;
    public BoundingBox3D boundingBox;
    public boolean shouldRender = true;
    public boolean highlight = false;

    public LandingBlock(BoundingBox3D boundingBox) {
        this.boundingBox = boundingBox;
    }

    public static List<LandingBlock> asLandingBlocks(List<BoundingBox3D> collisionBoundingBoxes) {
        return collisionBoundingBoxes.stream().map(LandingBlock::new).collect(Collectors.toCollection(ArrayList<LandingBlock>::new));
    }


    public boolean isTryingToLandOn() {
        if (Player.getLatest() == null) return false;

        BoundingBox3D playerBB = Player.getLatest().getBB();
        BoundingBox3D lastPlayerBB = Player.getLatest().getLastBB();

        if (landingMode != LandingMode.ENTER)
            return playerBB.minY() <= boundingBox.maxY() && lastPlayerBB.minY() > boundingBox.maxY();
        else
            return playerBB.minY() < boundingBox.maxY() && playerBB.minY() >= boundingBox.minY() && playerBB.minY() < lastPlayerBB.minY();
    }

    public enum LandingMode {
        LAND,
        HIT,
        Z_NEO,
        ENTER;

        public BoundingBox3D getPlayerBB() {
            if (Player.getLatest() == null) return null;

            switch (this) {
                case HIT:
                case ENTER:
                    return Player.getLatest().getBB();
                case LAND:
                default:
                    return Player.getLatest().getLastBB();
            }
        }
    }
}
