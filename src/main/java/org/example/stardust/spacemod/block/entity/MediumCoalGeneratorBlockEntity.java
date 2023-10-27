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

import java.util.ArrayList;
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
    private List<EnergyStorage> findEnergyTargets(BlockPos currentPosition, @Nullable Direction fromDirection) {
        List<EnergyStorage> targets = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (fromDirection != null && direction == fromDirection.getOpposite()) {
                continue;
            }

            BlockPos nextPos = currentPosition.offset(direction);
            BlockEntity nextEntity = world.getBlockEntity(nextPos);

            if (nextEntity instanceof ConductorBlockEntity) {
                targets.addAll(findEnergyTargets(nextPos, direction));
            } else if (nextEntity != null) {
                EnergyStorage target = EnergyStorage.SIDED.find(world, nextPos, direction.getOpposite());
                if (target != null && !(nextEntity instanceof MediumCoalGeneratorBlockEntity)) {
                    targets.add(target);
                }
            }
        }

        return targets;
    }

    private void distributeEnergyToTargets() {
        List<EnergyStorage> targets = findEnergyTargets(this.pos, null);

        System.out.println("Starting energy distribution. Current energy: " + currentEnergy);

        if (!targets.isEmpty() && currentEnergy > 0) {
            long totalEnergyToDistribute = Math.min(currentEnergy, 100000);
            long remainingEnergy = totalEnergyToDistribute;
            long actualExtractedTotal = 0;

            System.out.println("Total energy to distribute: " + totalEnergyToDistribute);

            while (!targets.isEmpty() && remainingEnergy > 0) {
                long energyToEachTarget = remainingEnergy / targets.size(); // equally distribute the remaining energy among the remaining targets
                List<EnergyStorage> incompleteTargets = new ArrayList<>();

                System.out.println("Energy to each target in this iteration: " + energyToEachTarget);

                for (EnergyStorage target : targets) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = extract(energyToEachTarget, transaction);

                        System.out.println("Attempting to send " + extracted);

                        if (extracted > 0) {
                            long remainingForTarget = target.insert(extracted, transaction);

                            System.out.println("Target" + " accepted " + (extracted - remainingForTarget) + " energy. Remaining for target: " + remainingForTarget);

                            // if the target does not accept all the energy, add to incompleteTargets list
                            if (remainingForTarget > 0) {
                                // insert(remainingForTarget, transaction);
                                incompleteTargets.add(target);
                            }

                            actualExtractedTotal += (extracted - remainingForTarget);
                        }
                        transaction.commit();
                    }
                }

                remainingEnergy = totalEnergyToDistribute - actualExtractedTotal;
                targets = incompleteTargets; // update targets list for next iteration
            }

            currentEnergy -= actualExtractedTotal;
            if (currentEnergy < 0) currentEnergy = 0; // Ensure energy doesn't go negative
            markDirty();
        }

        System.out.println("Ending energy distribution. Remaining energy: " + currentEnergy);
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
        distributeEnergyToTargets();
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
