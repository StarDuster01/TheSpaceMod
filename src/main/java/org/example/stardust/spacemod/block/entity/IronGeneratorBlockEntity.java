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

public class IronGeneratorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage, CableTickManager.EnergyReceiver {

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

    @Override
    public boolean canReceiveEnergy(Direction direction) {
        return true;
    }

    @Override
    public long receiveEnergy(long amount, Transaction transaction) {
        EnergyStorage sideStorage = this.energyContainer.getSideStorage(null);

        long acceptedEnergy = Math.min(amount, this.energyContainer.getMaxInsert(null));
        return sideStorage.insert(acceptedEnergy, transaction);
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
        return getCachedState().get(IronGeneratorBlock.FACING);
    }

    private void generateIronBlock() {
        if (hasEnoughEnergy()) {
            long requiredEnergy = BLOCK_ENERGY_MAP.getOrDefault(currentResourceType, 2000000);
            long extractedEnergy = energyStorage.extract(requiredEnergy, Transaction.openOuter());
            if (extractedEnergy >= requiredEnergy) {
                BlockPos frontBlockPos = pos.offset(getBlockFacing());
                BlockState frontBlockState = world.getBlockState(frontBlockPos);
                if (frontBlockState.isAir() || frontBlockState.isOf(Blocks.WATER)) {
                    Block blockToPlace;
                    switch (currentResourceType) {
                        case 0:
                            blockToPlace = Blocks.IRON_BLOCK;
                            break;
                        case 1:
                            blockToPlace = Blocks.GOLD_BLOCK;
                            break;
                        case 2:
                            blockToPlace = Blocks.DIAMOND_BLOCK;
                            break;
                        case 3:
                            blockToPlace = Blocks.LAPIS_BLOCK;
                            break;
                        case 4:
                            blockToPlace = Blocks.REDSTONE_BLOCK;
                            break;
                        case 5:
                            blockToPlace = Blocks.EMERALD_BLOCK;
                            break;
                        default:
                            blockToPlace = Blocks.IRON_BLOCK;
                            break;
                    }
                    world.setBlockState(frontBlockPos, blockToPlace.getDefaultState());
                    markDirty();
                }
            }
        }
    }




    private boolean hasEnergyFromCable() {
        return energyStorage.getAmount() < energyStorage.getCapacity();
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
    public void validateChestConnections() {
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof ChestBlock) {
            }
        }
    }

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

        if (!world.isClient) {
            validateChestConnections();
            notifyNearbyPlayers(world, pos);
            markDirty();
            generateIronBlock();
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
