package io.github.kurrycat.mpkmod.compatability.MCClasses;

import io.github.kurrycat.mpkmod.compatability.API;
import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldInteraction {
    /**
     * @param blockPosVec the block space in which collision bounding boxes will be searched
     * @return a list of all collision bounding boxes inside the specified block
     */
    public static List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec) {
        return Interface.get().map(w -> w.getCollisionBoundingBoxes(blockPosVec)).orElseGet(ArrayList::new);
    }

    /**
     * @return the block position that the player is looking at
     */
    public static Vector3D getLookingAt() {
        return Interface.get().map(Interface::getLookingAt).orElse(null);
    }

    /**
     * @return the block name that the player is looking at
     */
    public static String getLookingAtBlock() {
        return Interface.get().map(Interface::getLookingAtBlock).orElse(null);
    }

    /**
     * @return the result of {@link #getCollisionBoundingBoxes(Vector3D) getCollisionBoundingBoxes}({@link #getLookingAt()})
     */
    public static List<BoundingBox3D> getLookingAtCollisionBoundingBoxes() {
        Vector3D blockPosVec = getLookingAt();
        if(blockPosVec == null) return new ArrayList<>();
        return getCollisionBoundingBoxes(blockPosVec);
    }

    public interface Interface extends FunctionHolder {
        static Optional<Interface> get() {
            return API.getFunctionHolder(Interface.class);
        }

        List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec);

        Vector3D getLookingAt();

        String getLookingAtBlock();
    }
}