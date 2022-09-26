package io.github.kurrycat.mpkmod.landingblock;

import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;

public class LandingBlock {
    public static LandingMode landingMode = LandingMode.LAND;

    public static boolean isTryingToLandOn(BoundingBox3D bb) {
        if (Player.getLatest() == null) return false;

        BoundingBox3D playerBB = Player.getLatest().getBB();
        BoundingBox3D lastPlayerBB = Player.getLatest().getLastBB();

        if (landingMode != LandingMode.ENTER)
            return playerBB.minY() <= bb.maxY() && lastPlayerBB.minY() > bb.maxY();
        else
            return playerBB.minY() < bb.maxY() && playerBB.minY() >= bb.minY() && playerBB.minY() < lastPlayerBB.minY();
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
