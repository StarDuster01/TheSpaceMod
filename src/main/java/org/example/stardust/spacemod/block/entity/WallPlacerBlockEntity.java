package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.ExcavatorScreenHandler;
import org.example.stardust.spacemod.screen.WallPlacerScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.List;

public class WallPlacerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;
    private boolean isPlacingActive = false;


    private Mode currentMode = Mode.NONE;


    public enum Mode {
        NONE, PLACE_WALL, PLACE_TOWER
    }
    public void setCurrentMode(Mode mode) {
        this.currentMode = mode;
        markDirty();
    }

    public Mode getCurrentMode() {
        return this.currentMode;
    }




    public class WallPlacerEnergyStorage extends SimpleEnergyStorage {
        public WallPlacerEnergyStorage(long capacity, long maxInsert, long maxExtract) {
            super(capacity, maxInsert, maxExtract);
        }

        public void setAmountDirectly(long newAmount) {
            this.amount = Math.min(newAmount, this.capacity);
        }
    }

    public static final int ENERGY_PER_BLOCK = 200;
    public static int getEnergyPerBlock() {
        return ENERGY_PER_BLOCK;
    }
    // Creating an Energy Storage with a given capacity and charge/decharge rate
    public final WallPlacerEnergyStorage energyStorage = new WallPlacerEnergyStorage(3600000, 2000, 2000) {
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };



    public WallPlacerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WALLPLACER_BLOCK_BE, pos, state);
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
                    return (int) energyStorage.amount; // Example, assuming you want to display energy amount in GUI at index 0
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
    public void setPlacingActive(boolean placingActive) {
        isPlacingActive = placingActive;
        markDirty();
    }

    public boolean isPlacingActive() {
        markDirty();
        return isPlacingActive;
    }

    public void placeWall() {
        World currentWorld = this.getWorld();
        if (currentWorld == null) return;
        if (currentWorld.isClient) return; // Execute only on the server side

        int wallLength = 20;  // Define the length of the wall
        int wallHeight = 4;   // Define the height of the wall
        Direction facing = getCachedState().get(Properties.HORIZONTAL_FACING);  // Get the direction the block entity is facing

        for (int i = 0; i < wallLength; i++) {
            for (int j = 0; j < wallHeight; j++) {
                BlockPos targetPos = pos.offset(facing, i).up(j);  // Adjust the vertical position using the .up() method

                // Check if the position is empty and there are blocks in the inventory
                if (currentWorld.isAir(targetPos) && !inventory.isEmpty()) {
                    ItemStack blockStack = findBlockInInventory();
                    if (!blockStack.isEmpty()) {
                        Block block = Block.getBlockFromItem(blockStack.getItem());
                        currentWorld.setBlockState(targetPos, block.getDefaultState());
                        blockStack.decrement(1);  // Decrease the stack size by 1
                        extractEnergy(ENERGY_PER_BLOCK);  // Assume it also costs energy to place blocks
                        markDirty();
                    }
                }
            }
        }
    }

    public void placeTower() {
        World currentWorld = this.getWorld();
        if (currentWorld == null || currentWorld.isClient) return;  // Execute only on the server side

        int towerRadius = 30;  // Define the radius of the tower
        int towerHeight = 100;  // Define the height of the tower
        BlockPos centerPos = this.pos;  // Center position of the tower

        for (int y = 1; y <= towerHeight; y++) {  // Iterate through the height of the tower
            for (int x = -towerRadius; x <= towerRadius; x++) {
                for (int z = -towerRadius; z <= towerRadius; z++) {
                    double distanceSquared = x * x + z * z;  // Calculate the squared distance from the center
                    // Check if the position is on the outer surface of the circular cross-section
                    if (distanceSquared >= (towerRadius - 1) * (towerRadius - 1) && distanceSquared <= towerRadius * towerRadius) {
                        BlockPos targetPos = centerPos.add(x, y, z);  // Calculate the target position

                        // Check if the position is empty and there are blocks in the inventory
                        if (currentWorld.isAir(targetPos) && !inventory.isEmpty()) {
                            ItemStack blockStack = findBlockInInventory();
                            if (!blockStack.isEmpty()) {
                                Block block = Block.getBlockFromItem(blockStack.getItem());
                                currentWorld.setBlockState(targetPos, block.getDefaultState());
                                blockStack.decrement(1);  // Decrease the stack size by 1
                                extractEnergy(ENERGY_PER_BLOCK);  // Assume it also costs energy to place blocks
                                markDirty();
                            }
                        }
                    }
                }
            }
        }
    }




    private void extractEnergy(long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            energyStorage.extract(amount, transaction);
            markDirty();
            transaction.commit();
        }
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        // Increment the tick counter every tick
        tickCounter++;

        // Existing server-side code
        if(!world.isClient) {
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20*20) {
                    ModMessages.sendWallPlacerUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount, isPlacingActive);
                }
            }
        }

        // Increase power every tick, if there's room for more energy.
        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            System.out.println("Energy level: " + this.energyStorage.getAmount());
            System.out.println("Current Mode: " + currentMode);

            markDirty(world, pos, state);
        }

        // Check if there is 200 or more energy and if so, perform the action based on the current mode
        if (this.energyStorage.getAmount() >= ENERGY_PER_BLOCK) {
            switch (currentMode) {
                case PLACE_WALL:
                    if (isPlacingActive) {
                        placeWall();
                        System.out.println("Placed Wall. WallPlacer energy: " + this.energyStorage.getAmount());
                    }
                    break;
                case PLACE_TOWER:
                    if (isPlacingActive) {
                        placeTower();
                        System.out.println("Placed Tower. WallPlacer energy: " + this.energyStorage.getAmount());
                    }
                    break;
                case NONE:
                default:
                    // do nothing
                    break;
            }
            markDirty();
        }
    }




    private ItemStack findBlockInInventory() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }


    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
    @Override
    public Text getDisplayName() {
        return Text.literal("WallPlacer");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new WallPlacerScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }
    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }
    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long inserted = energyStorage.insert(maxAmount, transaction);
        System.out.println("Energy inserted: " + inserted);  // Log statement
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
        return energyStorage.capacity; // Try .capacity
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putLong("wallplacer.energy", energyStorage.amount);
    }
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);  // If you have inventory
        if (nbt.contains("wallplacer.energy")) {
            energyStorage.amount = nbt.getLong("wallplacer.energy");
        }
    }


}
