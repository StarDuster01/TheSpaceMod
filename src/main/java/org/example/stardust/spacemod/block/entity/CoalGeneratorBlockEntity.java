package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
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
import org.example.stardust.spacemod.item.ModItems;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.CoalGeneratorScreenHandler;
import org.example.stardust.spacemod.screen.DoomFurnaceScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.List;

public class CoalGeneratorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private int tickCount = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;

    private static final int INPUT_SLOT = 0;
    // Creating an Energy Storage with a given capacity and charge/decharge rate
    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(3600000, 2000, 2000) {
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };






    public CoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COAL_GENERATOR_BE, pos, state);
        this.energyContainer = new SimpleSidedEnergyContainer() {
            @Override
            public long getCapacity() {

                return 3600000;
            }

            @Override
            public long getMaxInsert(@Nullable Direction side) {
                return 2000;
            }

            @Override
            public long getMaxExtract(@Nullable Direction side) {

                return 8000;
            }
        };

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0)
                    return (int) energyStorage.amount;
                return 0; // Or handle other indexes
            }

            @Override
            public void set(int index, int value) {
                // Usually, it's left empty for read-only properties in GUI
            }

            @Override
            public int size() {
                return 1; // Or more, if you have more properties to display
            }
        };


    }
    private int tickCounter = 0;
    private static final int FUEL_CONSUMPTION_INTERVAL = 1; // The interval of ticks between fuel consumptions



    private boolean hasFuelSource(int inputSlot) {
        return this.getStack(INPUT_SLOT).getItem() == Items.COAL;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        tickCounter++;

        if (!world.isClient) {
            if (hasFuelSource(INPUT_SLOT) && this.energyStorage.amount < this.energyStorage.getCapacity()) {
                consumeFuel(); // Assuming that this will burn the coal and add the appropriate amount of energy.
                markDirty();
            }

            distributeEnergy();
            markDirty(world, pos, state);

            if (tickCounter >= FUEL_CONSUMPTION_INTERVAL) {
                tickCounter = 0;
            }
        }
    }

    private void consumeFuel() {
        if (hasFuelSource(INPUT_SLOT)) {
            ItemStack fuelStack = inventory.get(INPUT_SLOT);
            fuelStack.decrement(1);
            try (Transaction transaction = Transaction.openOuter()) { // try-with-resources will auto-close the transaction.
                energyStorage.insert(4000, transaction);
                transaction.commit(); // ensure that the transaction is committed.
                System.out.println("Total energy after insertion: " + energyStorage.amount);
            } catch (IllegalStateException e) {
                // Log the error and handle it appropriately.
                e.printStackTrace();
            }
            if (fuelStack.isEmpty()) {
                inventory.set(INPUT_SLOT, ItemStack.EMPTY);
            }
            markDirty();
        }
    }




    // New Method to Distribute Energy
    private void distributeEnergy() {
        // Check if we are on the server side
        if (!world.isClient) {
            for (Direction direction : Direction.values()) {
                // Attempt to find an adjacent EnergyStorage in the given direction.
                @Nullable
                EnergyStorage maybeStorage = EnergyStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());

                if (maybeStorage != null) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long amountToSend = Math.min(energyStorage.amount, maybeStorage.getCapacity() - maybeStorage.getAmount());

                        if (amountToSend > 0) {
                            long extracted = energyStorage.extract(amountToSend, transaction);
                            System.out.println("Energy extracted: " + extracted); // Log the amount extracted
                            long inserted = maybeStorage.insert(extracted, transaction);

                            if (inserted > 0) {
                                transaction.commit(); // Commit transaction if energy is transferred.
                                markDirty();
                            }
                        }
                    }
                }
            }
        }
    }




    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public boolean isFuel() {
        ItemStack stack = inventory.get(INPUT_SLOT);
        return !stack.isEmpty() && stack.isOf(Items.COAL);
    }

    public boolean isNoFuel() {
        return !isFuel();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Coal Generator");
    }


    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new CoalGeneratorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
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
        Inventories.writeNbt(nbt, inventory); // If you have inventory
        nbt.putLong("coal_generator.energy", energyStorage.amount); // Save the energy amount
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory); // If you have inventory
        if(nbt.contains("coal_generator.energy")) {
            energyStorage.amount = nbt.getLong("coal_generator.energy");
        }
    }

    // The following two functions are used to synchronize server and client for energy stuff
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
