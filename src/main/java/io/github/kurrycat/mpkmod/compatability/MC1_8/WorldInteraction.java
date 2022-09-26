package io.github.kurrycat.mpkmod.compatability.MC1_8;

import io.github.kurrycat.mpkmod.util.BoundingBox3D;
import io.github.kurrycat.mpkmod.util.Vector3D;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldInteraction {
    public static List<BoundingBox3D> getCollisionBoundingBoxes(Vector3D blockPosVec) {
        BlockPos blockPos = new BlockPos(blockPosVec.getX(), blockPosVec.getY(), blockPosVec.getZ());
        World world = Minecraft.getMinecraft().theWorld;
        IBlockState blockState = world.getBlockState(blockPos);
        AxisAlignedBB mask = new AxisAlignedBB(
                blockPosVec.getX() - 1,
                blockPosVec.getY() - 1,
                blockPosVec.getZ() - 1,
                blockPosVec.getX() + 1,
                blockPosVec.getY() + 1,
                blockPosVec.getZ() + 1
        );
        ArrayList<AxisAlignedBB> result = new ArrayList<>();
        blockState.getBlock().addCollisionBoxesToList(world, blockPos, blockState, mask, result, null);

        return result.stream().map(
                (aabb) ->
                        new BoundingBox3D(
                                new Vector3D(aabb.minX, aabb.minY, aabb.minZ),
                                new Vector3D(aabb.maxX, aabb.maxY, aabb.maxZ)
                        )
        ).collect(Collectors.toList());
    }
}
