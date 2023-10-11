package org.example.stardust.spacemod.item.custom;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class ViewFinder extends Item {
    private static final double MAX_DISTANCE = 50.0D;
    private Entity trackedEntity = null;

    public ViewFinder(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);

        if (!world.isClient) {
            HitResult hitResult = customRaycast(world, playerEntity, MAX_DISTANCE);

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
                playerEntity.sendMessage(Text.literal("You're looking at block at position: " + pos), false);
                System.out.println("You're looking at block at position: " + pos);

                if (world.getBlockState(pos).isOf(Blocks.WATER)) {
                    world.setBlockState(pos, Blocks.ICE.getDefaultState());
                    itemStack.damage(1, playerEntity, (p) -> p.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND));
                    return TypedActionResult.success(itemStack);
                }
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                trackedEntity = ((EntityHitResult) hitResult).getEntity();
                System.out.println("You're looking at entity: " + trackedEntity.getEntityName());
            }
        }

        return TypedActionResult.pass(itemStack);
    }

    public void tick() {
        if (trackedEntity != null) {
            System.out.println("Tracked entity is at position: " + trackedEntity.getPos().toString());
        }
    }

    private HitResult customRaycast(World world, LivingEntity entity, double maxDistance) {
        Vec3d start = entity.getEyePos();
        Vec3d end = start.add(entity.getRotationVector().multiply(maxDistance));
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, entity));

        if (blockHitResult.getType() == HitResult.Type.MISS) {
            Box entitySearchBox = new Box(start, end).expand(1);
            List<Entity> entities = world.getEntitiesByClass(Entity.class, entitySearchBox, (targetEntity) -> true);
            Optional<Entity> closestEntity = entities.stream()
                    .filter(targetEntity -> targetEntity.getBoundingBox().intersects(start, end))
                    .min((e1, e2) -> {
                        double d1 = start.distanceTo(e1.getPos());
                        double d2 = start.distanceTo(e2.getPos());
                        return Double.compare(d1, d2);
                    });
            if (closestEntity.isPresent()) {
                Entity entityHit = closestEntity.get();
                Vec3d hitPos = entityHit.getPos().add(0, entityHit.getHeight() / 2, 0); // Assuming center hit for simplicity
                return new EntityHitResult(entityHit, hitPos);
            }
        }

        return blockHitResult;
    }
}
