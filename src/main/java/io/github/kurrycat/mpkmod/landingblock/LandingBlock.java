package io.github.kurrycat.mpkmod.landingblock;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.compatability.MCClasses.Player;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LandingBlock {
    public static final int MAX_OFFSETS_SAVED = 500;
    public LandingMode landingMode = LandingMode.LAND;
    public BoundingBox3D boundingBox;
    public boolean enabled = true;
    public boolean highlight = false;

    public List<Vector3D> offsets = new ArrayList<>();
    public Vector3D pb = null;
    public Vector3D pbX = null;
    public Vector3D pbZ = null;

    public long lastTimeOffsetSaved = 0;

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

    public Vector3D saveOffsetIfInRange() {
        if (!isTryingToLandOn()) return null;
        BoundingBox3D playerBB = landingMode.getPlayerBB();
        if (playerBB == null) return null;

        Vector3D offset = boundingBox.distanceTo(playerBB).mult(-1D);
        if (offset.getX() <= -0.3F || offset.getZ() <= -0.3F) return null;

        offsets.add(offset);
        while (offsets.size() > MAX_OFFSETS_SAVED)
            offsets.remove(0);

        if (pb == null) pb = offset;
        else {
            //TODO: find a way to tell which offset is better than another


            //not working
            Vector3D diff = Vector3D.ZERO;
            Vector3D testVec = Math.signum(pb.getX()) != Math.signum(offset.getX()) && Math.signum(pb.getZ()) != Math.signum(offset.getZ())
                    ? pb.mult(-1) : pb;
            if (testVec.signXZ() > 0 && offset.signXZ() > 0)
                diff = offset.sub(testVec);
            else if (Math.signum(offset.getZ()) == Math.signum(testVec.getZ()))
                diff = new Vector3D(offset.getX() - testVec.getX(), 0, 0);
            else if (Math.signum(offset.getX()) == Math.signum(testVec.getX()))
                diff = new Vector3D(0, 0, offset.getZ() - testVec.getZ());

            if (diff.getX() + diff.getZ() > 0)
                pb = offset;
        }
        if (pbX == null || offset.getX() > pbX.getX()) pbX = offset;
        if (pbZ == null || offset.getZ() > pbZ.getZ()) pbZ = offset;

        lastTimeOffsetSaved = API.tickTime;

        return offset.copy();
    }

    @Override
    public int hashCode() {
        return this.boundingBox.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LandingBlock && this.boundingBox.equals(((LandingBlock) obj).boundingBox);
    }

    public enum LandingMode {
        LAND("Land"),
        HIT("Hit"),
        Z_NEO("Z Neo"),
        ENTER("Enter");

        public final String displayString;

        LandingMode(String displayString) {
            this.displayString = displayString;
        }

        public BoundingBox3D getPlayerBB() {
            if (Player.getLatest() == null) return null;

            switch (this) {
                case Z_NEO:
                    if (Player.getBeforeLatest() == null) return null;
                    return Player.getBeforeLatest().getLastBB();
                case HIT:
                case ENTER:
                    return Player.getLatest().getBB();
                case LAND:
                default:
                    return Player.getLatest().getLastBB();
            }
        }

        public LandingMode getNext() {
            return LandingMode.values()[(Arrays.asList(LandingMode.values()).indexOf(this) + 1) % LandingMode.values().length];
        }


        @Override
        public String toString() {
            return displayString;
        }
    }
}
