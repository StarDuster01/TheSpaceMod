package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.screen.MediumCoalGeneratorScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

public class ConductorBlockEntity extends BlockEntity implements EnergyStorage {
    public ConductorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CONDUCTOR_BE, pos, state);
    }

    private static final int INPUT_SLOT = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = Math.min(maxAmount, MAX_ENERGY - currentEnergy);
        System.out.println("Energy inserted: " + inserted);
        currentEnergy += inserted;
        markDirty();
        return inserted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = Math.min(maxAmount, currentEnergy);
        currentEnergy -= extracted;
        markDirty();
        return extracted;
    }

    @Override
    public long getAmount() {
        return currentEnergy;
    }

    @Override
    public long getCapacity() {
        return MAX_ENERGY;
    }

    /////////////////////// PROPERTY DELEGATE ////////////////////////
    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return (int) currentEnergy;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    currentEnergy = value;
                    break;
            }
        }

        @Override
        public int size() {
            return 1;
        }
    };

    ////////////////ALL ADDITIONAL ENERGY FUNCTION HERE //////////////////
    private static final long MAX_ENERGY = 1000;
    private long currentEnergy = 0;
    private int tickCounter = 0;
    // This method will calculate the total available energy without extracting it
    // Accumulate energy without extracting it
    private long calculateTotalEnergy(Direction fromDirection) {
        long totalEnergy = currentEnergy;

        for (Direction direction : Direction.values()) {
            if (fromDirection != null && direction == fromDirection.getOpposite()) {
                continue;
            }

            BlockPos currentPos = pos.offset(direction);
            BlockEntity neighborEntity = world.getBlockEntity(currentPos);

            if (neighborEntity instanceof ConductorBlockEntity) {
                totalEnergy += ((ConductorBlockEntity) neighborEntity).calculateTotalEnergy(direction);
            }
        }

        return totalEnergy;
    }

    private void distributeAccumulatedEnergy(long totalEnergy) {
        for (Direction direction : Direction.values()) {
            BlockPos currentPos = pos.offset(direction);
            BlockEntity neighborEntity = world.getBlockEntity(currentPos);

            if (neighborEntity instanceof ConductorBlockEntity || neighborEntity instanceof MediumCoalGeneratorBlockEntity) {
                continue;
            }

            EnergyStorage neighbor = EnergyStorage.SIDED.find(world, currentPos, direction.getOpposite());
            if (neighbor != null && neighbor.supportsInsertion() && neighbor.getAmount() < neighbor.getCapacity()) {
                try (Transaction transaction = Transaction.openOuter()) {
                    long extracted = extract(Math.min(totalEnergy, 1000), transaction);
                    long remaining = neighbor.insert(extracted, transaction);
                    totalEnergy -= (extracted - remaining);
                    if (totalEnergy <= 0) {
                        break;
                    }
                }
            }
        }
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world == null || world.isClient) return;

    }




    //////////////// NBT DATA /////////////
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putLong("conductor.energy", currentEnergy);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt.contains("conductor.energy")) {
            currentEnergy = nbt.getLong("conductor.energy");
        }
    }
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }
}
