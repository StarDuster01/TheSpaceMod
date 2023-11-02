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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.custom.FusionReactorBlock;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.FusionReactorScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.ArrayList;
import java.util.List;

import static org.example.stardust.spacemod.block.custom.FusionReactorBlock.STARRED;

public class FusionReactorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    public FusionReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_BE, pos, state);
    }

    public static final int ENERGY_PER_TICK = 50000;
    private static final int FUEL_CONSUMPTION_INTERVAL = 1;  // Check every 20 ticks
    private static final long MAX_ENERGY = 1000000; // for example
    private long currentEnergy = 0;
    private static final int INPUT_SLOT = 0;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);

    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
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
    public long getAmount() { // Returns amount of energy
        return currentEnergy;
    }
    public void setCurrentEnergy(long energy) {
        this.currentEnergy = energy;
        markDirty(); // Mark the block entity as dirty so it gets saved & synced
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

    ////////////////// END PROPERTY DELEGATE ///////////////
    private int tickCounter = 0;
    private boolean hasFuelSource(int inputSlot) {
        ItemStack stack = this.inventory.get(inputSlot);
        return !stack.isEmpty() && stack.getItem() == Items.NETHER_STAR;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world == null || world.isClient) return;  // No operation on client side or if world isn't initialized
        tickCounter++;
        if (tickCounter % FUEL_CONSUMPTION_INTERVAL == 0) {
            ItemStack fuelStack = inventory.get(INPUT_SLOT);
            if (fuelStack.isOf(Items.NETHER_STAR)) {
                switchstar();
            if (currentEnergy < MAX_ENERGY) {
                currentEnergy += ENERGY_PER_TICK;
            }
            }
            if (currentEnergy > MAX_ENERGY) {
                currentEnergy = MAX_ENERGY;
            }
            markDirty();
        }
        if (!world.isClient) {
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20 * 20) {
                    ModMessages.sendFusionReactorUpdate((ServerPlayerEntity) playerEntity, pos, currentEnergy);
                }
            }
        }
        distributeEnergyToTargets();
    }

    public void switchstar() {
        World world = this.getWorld();
        BlockPos pos = this.getPos();
        BlockState currentState = world.getBlockState(pos);
        System.out.println("Current block state: " + currentState);
        if (currentState.getBlock() instanceof FusionReactorBlock) {
            boolean isCurrentlyStarred = currentState.get(STARRED);
            if(!isCurrentlyStarred) {
                BlockState newState = currentState.with(STARRED, true);
                world.setBlockState(pos, newState, 3); // Use flag 3 to prevent re-rendering and notify neighbors
                if(world instanceof ServerWorld) {
                    ((ServerWorld)world).syncWorldEvent(null, 1023, pos, 0); // Play the block update sound
                }
            }
        }
    }



    ///////////////////// ENERGY CODE CUSTOM ///////////////////////
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
                if (target != null && !(nextEntity instanceof MediumCoalGeneratorBlockEntity) && !(nextEntity instanceof FusionReactorBlockEntity)) {
                    targets.add(target);
                }
            }
        }

        return targets;
    }
    private void distributeEnergyToTargets() {
        List<EnergyStorage> targets = findEnergyTargets(this.pos, null);


        if (!targets.isEmpty() && currentEnergy > 0) {
            long totalEnergyToDistribute = Math.min(currentEnergy, 1000000);
            long remainingEnergy = totalEnergyToDistribute;
            long actualExtractedTotal = 0;


            while (!targets.isEmpty() && remainingEnergy > 0) {
                long energyToEachTarget = remainingEnergy / targets.size(); // equally distribute the remaining energy among the remaining targets
                List<EnergyStorage> incompleteTargets = new ArrayList<>();


                for (EnergyStorage target : targets) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long extracted = extract(energyToEachTarget, transaction);



                        if (extracted > 0) {
                            long remainingForTarget = target.insert(extracted, transaction);



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

     //   System.out.println("Ending energy distribution. Remaining energy: " + currentEnergy);
    }



    //////////////////// END CUSTOM ENERGY CODE ///////////////

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
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

}
