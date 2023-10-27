package org.example.stardust.spacemod.block.entity;

import gravity_changer.api.GravityChangerAPI;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.screen.CoalGeneratorScreenHandler;
import org.example.stardust.spacemod.screen.FusionReactorScreenHandler;
import org.example.stardust.spacemod.screen.MediumCoalGeneratorScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

import java.util.List;

public class MediumCoalGeneratorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {
    public MediumCoalGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEDIUM_COAL_GENERATOR_BE, pos, state);
    }

    private static final int INPUT_SLOT = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);


    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Medium Coal Generator");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new MediumCoalGeneratorScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = Math.min(maxAmount, MAX_ENERGY - currentEnergy);
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
    public static final int ENERGY_PER_TICK = 4000;  // This will be energy for one coal
    private static final int FUEL_CONSUMPTION_INTERVAL = 20;  // Check every 20 ticks
    private boolean hasCoolant = false;
    public boolean isFuel() {
        ItemStack stack = getItems().get(INPUT_SLOT);
        return !stack.isEmpty() && (stack.isOf(Items.COAL) || stack.isOf(Items.COAL_BLOCK));
    }
    public boolean hasCoolant() {
        return this.hasCoolant;
    }
    public void setCoolant(boolean coolant) {
        this.hasCoolant = coolant;
        markDirty(); // Mark entity dirty to ensure it's saved
    }

    public boolean isNoFuel() {
        return !isFuel();
    }

    private static final long MAX_ENERGY = 100000; // for example
    private long currentEnergy = 0;

    private int tickCounter = 0;
  /*  private void distributeEnergy() {

        if (currentEnergy <= 0) {
            System.out.println("Generator Energy Empty. No distribution.");
            return;
        }

        for (Direction direction : Direction.values()) {
            BlockEntity neighborEntity = world.getBlockEntity(pos.offset(direction));
            if (neighborEntity != null) {

                // Case 1: The neighbor block is EnergyStorage.SIDED
                EnergyStorage neighborSided = EnergyStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                if (neighborSided != null && neighborSided.supportsInsertion()) {
                   // System.out.println("Detected SIDED Energy Storage at direction: " + direction);
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = extract(100000, transaction);
                       // System.out.println("Trying to extract " + extracted + " energy units from Generator.");
                        long remaining = neighborSided.insert(extracted, transaction);
                       // System.out.println("Inserted energy to SIDED. Remaining energy: " + remaining);
                        if (remaining < extracted) {
                            insert(extracted - remaining, transaction);
                        //    System.out.println("Reinserted remaining energy back to Generator: " + (extracted - remaining));
                        }
                        transaction.commit();
                    }
                    continue;  // Skip to next iteration since we've handled this direction
                }

                // Case 2: The neighbor block is just EnergyStorage
                if (neighborEntity instanceof EnergyStorage) {
                  //  System.out.println("Detected simple Energy Storage at direction: " + direction);
                    EnergyStorage neighbor = (EnergyStorage) neighborEntity;
                    if (neighbor.supportsInsertion()) {
                        try (Transaction transaction = Transaction.openOuter()) {
                            long extracted = extract(100000, transaction);
                          //  System.out.println("Trying to extract " + extracted + " energy units from Generator.");
                            long remaining = neighbor.insert(extracted, transaction);
                           // System.out.println("Inserted energy to simple Energy Storage. Remaining energy int reactor: " + remaining);
                            if (remaining < extracted) {
                                insert(extracted - remaining, transaction);
                            //    System.out.println("Reinserted remaining energy back to Generator: " + (extracted - remaining));
                            }
                            transaction.commit();
                        }
                    }
                }
            } else {
               // System.out.println("No neighbor detected at direction: " + direction);
            }
        }
    } */
    // ... [rest of the MediumCoalGeneratorBlockEntity class]

    // Recursive function to find the end target of the chain of ConductorBlockEntity blocks
    // Recursive function to find the end target of the chain of ConductorBlockEntity blocks
    private EnergyStorage findEnergyTarget(BlockPos currentPosition, @Nullable Direction fromDirection) {
        EnergyStorage target = null;

        for (Direction direction : Direction.values()) {
            // We don't want to go back to the block we came from
            if (fromDirection != null && direction == fromDirection.getOpposite()) {
                continue;
            }

            BlockPos nextPos = currentPosition.offset(direction);
            BlockEntity nextEntity = world.getBlockEntity(nextPos);

            if (nextEntity instanceof ConductorBlockEntity) {
                // Continue following the chain of ConductorBlockEntity
                target = findEnergyTarget(nextPos, direction);
            } else if (nextEntity != null) {
                // If it's not a ConductorBlockEntity, check if it's a sided energy storage and not a generator
                target = EnergyStorage.SIDED.find(world, nextPos, direction.getOpposite());
                if (target != null && !(nextEntity instanceof MediumCoalGeneratorBlockEntity)) {
                    System.out.println("Found a sided energy storage connected to ConductorBlockEntity at position: " + nextPos);
                    return target;
                }
            }
        }

        return target;
    }


    private void distributeEnergyToTarget() {
        EnergyStorage target = findEnergyTarget(this.pos, null);
        if (target != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                long extracted = extract(100000, transaction);
                long remaining = target.insert(extracted, transaction);
                if (remaining < extracted) {
                    insert(extracted - remaining, transaction);
                }
                transaction.commit();
            }
        }
    }



    public void tick(World world, BlockPos pos, BlockState state) {
        if (world == null || world.isClient) return;  // No operation on client side or if world isn't initialized
        checkForCoolant();
        if (!hasCoolant()) return;
        System.out.println("Current Energy: " + currentEnergy);


        tickCounter++;

        // Check if we should consume fuel
        if (tickCounter % FUEL_CONSUMPTION_INTERVAL == 0) {
            ItemStack fuelStack = inventory.get(INPUT_SLOT);
            if (currentEnergy < MAX_ENERGY && fuelStack.isOf(Items.COAL)) {
                // Consume the coal and generate energy
                fuelStack.decrement(1);
                currentEnergy += ENERGY_PER_TICK;
            } else if (currentEnergy < MAX_ENERGY - 9 * ENERGY_PER_TICK && fuelStack.isOf(Items.COAL_BLOCK)) {
                // Consume the coal block and generate 9 times the energy
                fuelStack.decrement(1);
                currentEnergy +=  ENERGY_PER_TICK;
            }

            // Ensure currentEnergy does not exceed MAX_ENERGY
            if (currentEnergy > MAX_ENERGY) {
                currentEnergy = MAX_ENERGY;
            }
            markDirty();
        }

        System.out.println("Current Energy: " + currentEnergy); // Print current energy

        // Distribute energy to neighboring blocks
        distributeEnergyToTarget();
    }



    private void checkForCoolant() {
        boolean foundWater = false;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.isOf(Blocks.WATER) && neighborState.get(FluidBlock.LEVEL) == 0) { // Level 0 is a source block for water
                foundWater = true;
                break;  // Exit the loop once we found water
            }
        }
        setCoolant(foundWater);  // Update the coolant status
    }


    //////////////// NBT DATA /////////////
    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, getItems());
        nbt.putLong("medium_coal_generator.energy", currentEnergy);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, getItems());
        if (nbt.contains("medium_coal_generator.energy")) {
            currentEnergy = nbt.getLong("medium_coal_generator.energy");
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
