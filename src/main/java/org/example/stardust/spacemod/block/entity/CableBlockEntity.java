package org.example.stardust.spacemod.block.entity;


import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.custom.CableBlock;
import org.jetbrains.annotations.Nullable;
import org.example.stardust.spacemod.misc.IListInfoProvider;
import org.example.stardust.spacemod.misc.IToolDrop;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.ArrayList;
import java.util.List;

public class CableBlockEntity extends BlockEntity
        implements BlockEntityTicker<CableBlockEntity>, IListInfoProvider, IToolDrop, RenderAttachmentBlockEntity {
    final SimpleSidedEnergyContainer energyContainer = new SimpleSidedEnergyContainer() {
        @Override
        public long getCapacity() {
            return 1000000;
        }

        @Override
        public long getMaxInsert(Direction side) {
            if (allowTransfer(side)) return 1000000;
            else return 0;
        }

        @Override
        public long getMaxExtract(Direction side) {
            if (allowTransfer(side)) return 1000000;
            else return 0;
        }
    };
    public long transferRate = 1000000;

    @Nullable
    private BlockState cover = null;
    long lastTick = 0;
    // null means that it needs to be re-queried
    List<CableTarget> targets = null;
    /**
     * Adjacent caches, used to quickly query adjacent cable block entities.
     */
    @SuppressWarnings("unchecked")
    private final BlockApiCache<EnergyStorage, Direction>[] adjacentCaches = new BlockApiCache[6];
    /**
     * Bitmask to prevent input or output into/from the cable when the cable already transferred in the target direction.
     * This prevents double transfer rates, and back and forth between two cables.
     */
    int blockedSides = 0;

    /**
     * This is only used during the cable tick, whereas {@link #blockedSides} is used between ticks.
     */
    boolean ioBlocked = false;

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE_BE, pos, state);
    }
    private boolean allowTransfer(Direction side) {
        return !ioBlocked && (blockedSides & (1 << side.ordinal())) == 0;
    }

    public EnergyStorage getSideEnergyStorage(@Nullable Direction side) {
        return energyContainer.getSideStorage(side);
    }

    public @Nullable BlockState getCover() {
        return cover;
    }

    public long getEnergy() {
        return energyContainer.amount;
    }

    public void setEnergy(long energy) {
        energyContainer.amount = energy;
    }

    private BlockApiCache<EnergyStorage, Direction> getAdjacentCache(Direction direction) {
        if (adjacentCaches[direction.getId()] == null) {
            adjacentCaches[direction.getId()] = BlockApiCache.create(EnergyStorage.SIDED, (ServerWorld) world, pos.offset(direction));
        }
        return adjacentCaches[direction.getId()];
    }

    @Nullable
    BlockEntity getAdjacentBlockEntity(Direction direction) {
        return getAdjacentCache(direction).getBlockEntity();
    }

    void appendTargets(List<OfferedEnergyStorage> targetStorages) {
        ServerWorld serverWorld = (ServerWorld) world;
        if (serverWorld == null) {
            return;
        }

        // Update our targets if necessary.
        if (targets == null) {
            BlockState newBlockState = getCachedState();

            targets = new ArrayList<>();
            for (Direction direction : Direction.values()) {
                boolean foundSomething = false;

                BlockApiCache<EnergyStorage, Direction> adjCache = getAdjacentCache(direction);

                if (adjCache.getBlockEntity() instanceof CableBlockEntity adjCable) {
                    if (adjCable == adjCable) {
                        // Make sure cables are not used as regular targets.
                        foundSomething = true;
                    }
                } else if (adjCache.find(direction.getOpposite()) != null) {
                    foundSomething = true;
                    targets.add(new CableTarget(direction, adjCache));
                }

                newBlockState = newBlockState.with(CableBlock.PROPERTY_MAP.get(direction), foundSomething);
            }

            serverWorld.setBlockState(getPos(), newBlockState);
        }

        // Fill the list.
        for (CableTarget target : targets) {
            EnergyStorage storage = target.find();

            if (storage == null) {
                // Schedule a rebuild next tick.
                // This is just a reference change, the iterator remains valid.
                targets = null;
            } else {
                targetStorages.add(new OfferedEnergyStorage(this, target.directionTo, storage));
            }
        }

        // Reset blocked sides.
        blockedSides = 0;
    }

    // BlockEntity
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        NbtCompound nbtTag = new NbtCompound();
        writeNbt(nbtTag);
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        if (compound.contains("energy")) {
            energyContainer.amount = compound.getLong("energy");
        }
        else {
            cover = null;
        }
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.putLong("energy", energyContainer.amount);
        if (cover != null) {
            compound.put("cover", NbtHelper.fromBlockState(cover));
        }
    }

    public void neighborUpdate() {
        targets = null;
    }

    // BlockEntityTicker
    @Override
    public void tick(World world, BlockPos pos, BlockState state, CableBlockEntity blockEntity) {
        if (world == null || world.isClient) {
            return;
        }

        CableTickManager.handleCableTick(blockEntity);
    }



    @Override
    public @Nullable BlockState getRenderAttachmentData() {
        return cover;
    }

    @Override
    public void addInfo(List<Text> info, boolean isReal, boolean hasData) {

    }

    @Override
    public ItemStack getToolDrop(PlayerEntity p0) {
        return null;
    }

    private record CableTarget(Direction directionTo, BlockApiCache<EnergyStorage, Direction> cache) {

        @Nullable
        EnergyStorage find() {
            return cache.find(directionTo.getOpposite());
        }
    }
}