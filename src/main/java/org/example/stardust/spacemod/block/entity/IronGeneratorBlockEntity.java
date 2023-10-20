package org.example.stardust.spacemod.block.entity;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.block.custom.IronGeneratorBlock;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.IronGeneratorScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.List;

public class IronGeneratorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;


    private boolean isGeneratorActive = false;

    public boolean isGeneratorActive() {
        return isGeneratorActive;
    }

    public void setGeneratorActive(boolean generatorActive) {
        this.isGeneratorActive = generatorActive;
        markDirty();
    }

    public int getCurrentBlockTypeIndex() {
        List<String> blockTypes = List.of("Iron", "Gold", "Diamond", "Lapis", "Redstone", "Emerald");
        return blockTypes.indexOf(currentBlockType);
    }



    public class IronGeneratorEnergyStorage extends SimpleEnergyStorage {
        public IronGeneratorEnergyStorage(long capacity, long maxInsert, long maxExtract) {
            super(capacity, maxInsert, maxExtract);
        }

        public void setAmountDirectly(long newAmount) {
            this.amount = Math.min(newAmount, this.capacity);
        }
    }
    private String currentBlockType = "Iron";

    public String getCurrentBlockType() {
        return currentBlockType;
    }

    public void setCurrentBlockType(String currentBlockType) {
        this.currentBlockType = currentBlockType;
        markDirty();
    }

    public static final int ENERGY_PER_BLOCK = 2000000;
    public static int getEnergyPerBlock() {

        return ENERGY_PER_BLOCK;
    }
    private Direction getBlockFacing() {
        return getCachedState().get(IronGeneratorBlock.FACING); // Assuming IronGeneratorBlock has a FACING property
    }

    private void generateIronBlock() {
        if (hasEnoughEnergy()) {
            BlockPos frontBlockPos = pos.offset(getBlockFacing());
            BlockState frontBlockState = world.getBlockState(frontBlockPos);
            if (frontBlockState.isAir() || frontBlockState.isOf(Blocks.WATER)) {
                Block blockToPlace = switch(currentBlockType) {
                    case "Iron" -> Blocks.IRON_BLOCK;
                    case "Gold" -> Blocks.GOLD_BLOCK;
                    case "Diamond" -> Blocks.DIAMOND_BLOCK;
                    case "Lapis" -> Blocks.LAPIS_BLOCK;
                    case "Redstone" -> Blocks.REDSTONE_BLOCK;
                    case "Emerald" -> Blocks.EMERALD_BLOCK;
                    default -> Blocks.IRON_BLOCK;  // Default to iron block
                };
                world.setBlockState(frontBlockPos, blockToPlace.getDefaultState());
                extractEnergy(ENERGY_PER_BLOCK);
            }
        }
    }




    public final IronGeneratorEnergyStorage energyStorage = new IronGeneratorEnergyStorage(100000000, 10000, 10000) {
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };



    public IronGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_GENERATOR_BLOCK_BE, pos, state);
        this.energyContainer = new SimpleSidedEnergyContainer() {
            @Override
            public long getCapacity() {
                return 100000;
            }

            @Override
            public long getMaxInsert(@Nullable Direction side) {
                return 10000;
            }

            @Override
            public long getMaxExtract(@Nullable Direction side) {
                return 10000;
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
    public void validateChestConnections() {
        // Reset any cached chest connections here (if you have any)

        // Check connections anew
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof ChestBlock) {
                // Re-establish connection or cache this chest for later use
            }
        }
    }
    // Method to insert an item into the inventory. Returns whether the item was successfully inserted.
    private boolean hasEnoughEnergy() {
        return energyStorage.getAmount() >= ENERGY_PER_BLOCK; // Ensure you have defined ENERGY_PER_BLOCK.
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
        System.out.println("Current Energy is" + this.energyStorage.getAmount());
        if(!world.isClient) { // Check if on server side
            System.out.println("Current Energy is" + this.energyStorage.getAmount());
            validateChestConnections();
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20*20) {
                    ModMessages.sendIronGeneratorUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount, isGeneratorActive());
                }
            }

        }
        // Increase power every tick, if there's room for more energy.
        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            markDirty(world, pos, state);
        }

        if (!world.isClient) { // Only do this on the server side
            generateIronBlock();
        }
    }

    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
    @Override
    public Text getDisplayName() {
        return Text.literal("IronGenerator");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new IronGeneratorScreenHandler(syncId, playerInventory, this, propertyDelegate);
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
        return energyStorage.getCapacity();
    }


    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putLong("iron_generator.energy", energyStorage.amount);
        nbt.putString("currentBlockType", currentBlockType); // Save the current block type
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if (nbt.contains("iron_generator.energy")) {
            energyStorage.amount = nbt.getLong("iron_generator.energy");
        }
        if (nbt.contains("currentBlockType")) {
            currentBlockType = nbt.getString("currentBlockType"); // Load the current block type
        }
    }



}
