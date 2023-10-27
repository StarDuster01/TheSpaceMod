package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
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
import org.example.stardust.spacemod.screen.FusionReactorScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

public class FusionReactorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private int tickCount = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;

    private static final int INPUT_SLOT = 0;

    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(512000, 100000, 100000) {

        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };

    public FusionReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_BE, pos, state);
        this.energyContainer = new SimpleSidedEnergyContainer() {
            @Override
            public long getCapacity() {
                return 10000000;
            }

            @Override
            public long getMaxInsert(Direction side) {
                return side == Direction.UP ? 100000 : 0;
            }

            @Override
            public long getMaxExtract(Direction side) {
                return 1000000;
            }
        };

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0)
                    return (int) energyStorage.amount;
                return 0;
            }

            @Override
            public void set(int index, int value) {

            }

            @Override
            public int size() {
                return 1;
            }
        };
    }
    private int tickCounter = 0;
    private static final int FUEL_CONSUMPTION_INTERVAL = 20;
    private boolean hasFuelSource(int inputSlot) {
        ItemStack stack = this.inventory.get(inputSlot);
        return !stack.isEmpty() && stack.getItem() == Items.NETHER_STAR;
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        tickCounter++;

        if (!world.isClient) {
            // Energy generation logic
            if (hasFuelSource(INPUT_SLOT) && this.energyStorage.amount < this.energyStorage.getCapacity()) {
                generateEnergy();
                markDirty();
            }


            markDirty(world, pos, state);
            if (tickCounter >= FUEL_CONSUMPTION_INTERVAL) {
                tickCounter = 0;
            }
        }
    }


    private void generateEnergy() {
        if (hasFuelSource(INPUT_SLOT)) {
            long energyToAdd = 10000000;
            try (Transaction transaction = Transaction.openOuter()) {
                energyStorage.insert(energyToAdd, transaction);
                // transaction.commit();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            markDirty();
        }
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = energyStorage.insert(maxAmount, transaction);
        if (inserted > 0) {
            markDirty();
        }
        return inserted;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long extracted = energyStorage.extract(maxAmount, transaction);
        if (extracted > 0) {
            markDirty();
        }
        return extracted;
    }

    @Override
    public long getAmount() {
        return energyStorage.amount;
    }

    @Override
    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putLong("fusion_reactor.energy", energyStorage.amount);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if(nbt.contains("fusion_reactor.energy")) {
            energyStorage.amount = nbt.getLong("fusion_reactor.energy");
        }
    }


    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }

    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public boolean isFuel() {
        ItemStack stack = inventory.get(INPUT_SLOT);
        return !stack.isEmpty() && stack.isOf(Items.NETHER_STAR);
    }

    public boolean isNoFuel() {
        return !isFuel();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Fusion Reactor");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FusionReactorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
}
