package org.example.stardust.spacemod.block.entity;

import gravity_changer.GravityChangerComponents;
import gravity_changer.GravityComponent;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.custom.GravityBlockBlock;

import java.util.*;

public class GravityBlockBlockEntity extends BlockEntity {

    private static final double RADIUS = 5; // The radius in which to check for entities


    public GravityBlockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAVITY_BLOCK_BE, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            return; // Make sure we only process this on the server side
        }

        // Get entities within a certain radius of the block
        Box boundingBox = new Box(pos).expand(RADIUS);
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, boundingBox, (entity) -> true);

        for (Entity entity : nearbyEntities) {
            if(isOutsideSphereOfInfluence(entity, pos)) {
                if(GravityChangerAPI.getGravityDirection(entity) != Direction.DOWN) {
                    GravityChangerAPI.setBaseGravityDirection(entity, Direction.DOWN);
                    System.out.println("Entity " + entity.getEntityName() + " at " + entity.getPos() + " has gravity set to DOWN.");
                }
            } else {
                Direction closestFace = getClosestFace(entity, pos);
                if(GravityChangerAPI.getGravityDirection(entity) != closestFace) {
                    GravityChangerAPI.setBaseGravityDirection(entity, closestFace);
                    System.out.println("Entity " + entity.getEntityName() + " at " + entity.getPos() + " has gravity set to " + closestFace + ".");
                }
                }
            }
        }


    private Direction getClosestFace(Entity entity, BlockPos blockPos) {
        double minDistance = Double.MAX_VALUE;
        Direction closestFace = Direction.UP;

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = blockPos.offset(direction);
            double distance = offsetPos.getSquaredDistance(entity.getPos().x, entity.getPos().y, entity.getPos().z);
            if (distance < minDistance) {
                minDistance = distance;
                closestFace = direction;
            }
        }

        return closestFace.getOpposite();
    }

    private boolean isOutsideSphereOfInfluence(Entity entity, BlockPos blockPos) {
        double squaredDistance = blockPos.getSquaredDistanceFromCenter(entity.getPos().x, entity.getPos().y, entity.getPos().z);
        return squaredDistance > RADIUS * RADIUS;
    }
}
