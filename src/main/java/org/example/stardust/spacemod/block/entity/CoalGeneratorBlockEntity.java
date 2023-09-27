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
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.item.ModItems;
import org.example.stardust.spacemod.screen.CoalGeneratorScreenHandler;
import org.example.stardust.spacemod.screen.DoomFurnaceScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

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
                return 0;
            }

            @Override
            public long getMaxInsert(@Nullable Direction side) {
                return 0;
            }

            @Override
            public long getMaxExtract(@Nullable Direction side) {
                return 0;
            }
        };

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0)
                    return (int) energyStorage.getAmount(); // Example, assuming you want to display energy amount in GUI at index 0
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

    private void fillUpOnEnergy() {
        if(hasFuelSource(INPUT_SLOT) && this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            try(Transaction transaction = Transaction.openOuter()) {
                int amountInserted = (int) this.energyStorage.insert(2000, transaction);

                if(amountInserted > 0) {
                    transaction.commit();
                }
            }
        }
    }



    private int tickCounter = 0;
    private static final int FUEL_CONSUMPTION_INTERVAL = 200; // The interval of ticks between fuel consumptions



    private boolean hasFuelSource(int inputSlot) {
        return this.getStack(INPUT_SLOT).getItem() == Items.COAL;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        // Increment the tick counter every tick
        tickCounter++;

        // Increase power every tick, if there's room for more energy.
        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            fillUpOnEnergy();
            distributeEnergy();
            markDirty(world, pos, state);

            // Every 200 ticks, consume fuel
            if (tickCounter >= FUEL_CONSUMPTION_INTERVAL) {
                if (hasFuelSource(INPUT_SLOT)) {
                    consumeFuel(); // Assuming you have a method to consume fuel
                }
                tickCounter = 0; // Reset the tick counter
            }
        }
    }

    private void consumeFuel() {
        if(!inventory.get(INPUT_SLOT).isEmpty()) {
            ItemStack fuelStack = inventory.get(INPUT_SLOT);
            fuelStack.decrement(1);
            if(fuelStack.isEmpty()) {
                inventory.set(INPUT_SLOT,ItemStack.EMPTY);
            }
        }

    }


    // New Method to Distribute Energy
    private void distributeEnergy() {
        for (Direction direction : Direction.values()) {
            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));
            if (blockEntity instanceof EnergyStorage) {
                EnergyStorage neighborStorage = (EnergyStorage) blockEntity;
                long amountToSend = Math.min(energyStorage.getAmount(), neighborStorage.getCapacity() - neighborStorage.getAmount());
                if(amountToSend > 0) {
                    try(Transaction transaction = Transaction.openOuter()) {
                        long extracted = energyStorage.extract(amountToSend, transaction);
                        long inserted = neighborStorage.insert(extracted, transaction);
                        if(inserted > 0)
                            transaction.commit();
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
        return energyStorage.insert(maxAmount, transaction);
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return energyStorage.extract(maxAmount, transaction);
    }

    @Override
    public long getAmount() {
        return energyStorage.getAmount();
    }

    @Override
    public long getCapacity() {
        return energyStorage.getCapacity();
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory); // If you have inventory
        nbt.putLong("coal_generator.energy", energyStorage.getAmount()); // Save the energy amount
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory); // If you have inventory
        if(nbt.contains("coal_generator.energy")) {
            energyStorage.amount = nbt.getLong("coal_generator.energy");
        }
    }

}
