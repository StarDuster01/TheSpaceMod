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
import java.util.Map;

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
        markDirty(); // Mark the block entity as dirty to ensure it gets saved
    }

    public static final Map<Integer, Integer> BLOCK_ENERGY_MAP = Map.of(
            0, 2000000, //Iron
            1, 3000000, // Gold
            2, 5000000, //Diamond
            3, 2500000, // Lapis
            4, 2300000, // Redstone
            5, 4000000 // Emerald
    );



    private Direction getBlockFacing() {
        return getCachedState().get(IronGeneratorBlock.FACING); // Assuming IronGeneratorBlock has a FACING property
    }

    private void generateIronBlock() {
        if (hasEnoughEnergy()) {
            BlockPos frontBlockPos = pos.offset(getBlockFacing());
            BlockState frontBlockState = world.getBlockState(frontBlockPos);
            if (frontBlockState.isAir() || frontBlockState.isOf(Blocks.WATER)) {
                Block blockToPlace = switch(currentResourceType) {
                    case 0 -> Blocks.IRON_BLOCK;
                    case 1 -> Blocks.GOLD_BLOCK;
                    case 2 -> Blocks.DIAMOND_BLOCK;
                    case 3 -> Blocks.LAPIS_BLOCK;
                    case 4 -> Blocks.REDSTONE_BLOCK;
                    case 5 -> Blocks.EMERALD_BLOCK;
                    default -> Blocks.IRON_BLOCK;
                };
                world.setBlockState(frontBlockPos, blockToPlace.getDefaultState());
                extractEnergy(BLOCK_ENERGY_MAP.getOrDefault(currentResourceType, 2000000));

                markDirty();
            }
        }
    }



    private int currentResourceType = 0; // default value
    public void setCurrentResourceType(int type) {
        this.currentResourceType = type;
        System.out.println("Setting resource type to: " + type);
        markDirty();
    }

    public int getCurrentResourceType() {
        return this.currentResourceType;
    }




    public final IronGeneratorEnergyStorage energyStorage = new IronGeneratorEnergyStorage(100000000, 10000, 20000000) {
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
        int requiredEnergy = BLOCK_ENERGY_MAP.getOrDefault(currentResourceType, 2000000);
        return energyStorage.getAmount() >= requiredEnergy;
    }

    private void extractEnergy(long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            energyStorage.extract(amount, transaction);
            markDirty();
            transaction.commit();
        }
    }
    public void setResourceType(String type) {
        List<String> blockTypes = List.of("Iron", "Gold", "Diamond", "Lapis", "Redstone", "Emerald");
        if (blockTypes.contains(type)) {
            this.currentBlockType = type;
            markDirty();
        }
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        tickCounter++;

        if (!world.isClient) { // Server side operations
            validateChestConnections();
            notifyNearbyPlayers(world, pos);
            markDirty();
            generateIronBlock();
        }

        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            markDirty();
        }
    }

    private void notifyNearbyPlayers(World world, BlockPos pos) {
        for (PlayerEntity playerEntity : world.getPlayers()) {
            if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20 * 20) {
                ModMessages.sendIronGeneratorUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount, isGeneratorActive(), getResourceType());
            }
        }
    }

    public int getResourceType() {
        switch(currentBlockType) {
            case "Iron": return 0;
            case "Gold": return 1;
            case "Diamond": return 2;
            case "Lapis": return 3;
            case "Redstone": return 4;
            case "Emerald": return 5;
            default: return -1;  // Default to a value indicating an error or unknown type
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
        nbt.putLong("energy", energyStorage.amount); // Corrected key name
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if (nbt.contains("energy")) {
            energyStorage.amount = nbt.getLong("energy"); // Corrected key name
        }

    }




}
