package org.example.stardust.spacemod.block.entity;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.example.stardust.spacemod.block.ModBlocks;
import org.example.stardust.spacemod.block.custom.ExcavatorBlock;
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.CoalGeneratorScreenHandler;
import org.example.stardust.spacemod.screen.ExcavatorScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.ArrayList;
import java.util.List;

public class ExcavatorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;
    private Vector2i miningAreaDimensions = new Vector2i(4, 4);  // Default dimensions of 4x4

    private int chestSearchRadius = 5;  // Default radius of 5



    private boolean isMiningActive = false;

    public void setMiningAreaDimensions(Vector2i vec2i) {
        this.miningAreaDimensions = vec2i;
        markDirty();
    }
    public void setChestSearchRadius(int radius) {
        this.chestSearchRadius = radius;
        markDirty();
    }


    public class ExcavatorEnergyStorage extends SimpleEnergyStorage {
        public ExcavatorEnergyStorage(long capacity, long maxInsert, long maxExtract) {
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
    public final ExcavatorEnergyStorage energyStorage = new ExcavatorEnergyStorage(3600000, 2000, 2000) {
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };



    public ExcavatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXCAVATOR_BLOCK_BE, pos, state);
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

        this.currentMiningY = pos.getY() - 1;
    }

    private int tickCounter = 0;

    private int currentMiningY;

    public boolean isMiningActive() {
        markDirty();
        return isMiningActive;
    }

    public void setMiningActive(boolean miningActive) {
        isMiningActive = miningActive;
        markDirty();
    }

    public boolean tryInsertIntoNeighboringChests(ItemStack itemStack) {
        // Assuming chestSearchRadius is a field in your class that specifies the search radius
        for (int dx = -chestSearchRadius; dx <= chestSearchRadius; dx++) {
            for (int dy = -chestSearchRadius; dy <= chestSearchRadius; dy++) {
                for (int dz = -chestSearchRadius; dz <= chestSearchRadius; dz++) {
                    BlockPos currentPos = pos.add(dx, dy, dz);
                    BlockState currentState = world.getBlockState(currentPos);
                    if (currentState.getBlock() instanceof ChestBlock) {
                        ChestBlockEntity chestBlockEntity = (ChestBlockEntity) world.getBlockEntity(currentPos);
                        if (chestBlockEntity != null) {
                            ChestType chestType = currentState.get(ChestBlock.CHEST_TYPE);
                            if (chestType != ChestType.SINGLE) {
                                // This is a double chest
                                BlockPos otherHalfPos = currentPos.offset(ChestBlock.getFacing(currentState));
                                ChestBlockEntity otherHalf = (ChestBlockEntity) world.getBlockEntity(otherHalfPos);
                                if (otherHalf != null) {
                                    DoubleInventory doubleInventory = new DoubleInventory(chestBlockEntity, otherHalf);
                                    if (tryInsertIntoInventory(doubleInventory, itemStack)) {
                                        return true;
                                    }
                                }
                            } else {
                                // This is a single chest
                                if (tryInsertIntoInventory(chestBlockEntity, itemStack)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;  // Return false if item could not be inserted into any chest within radius
    }


    // Helper method to handle inserting item into an Inventory
    private boolean tryInsertIntoInventory(net.minecraft.inventory.Inventory inventory, ItemStack itemStack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stackInSlot = inventory.getStack(i);
            if (stackInSlot.isEmpty()) {
                inventory.setStack(i, itemStack.copy());
                inventory.markDirty();
                return true;
            } else if (ItemStack.canCombine(stackInSlot, itemStack)) {
                int spaceLeft = stackInSlot.getMaxCount() - stackInSlot.getCount();
                if (spaceLeft >= itemStack.getCount()) {
                    stackInSlot.increment(itemStack.getCount());
                    inventory.markDirty();
                    return true;
                } else if (spaceLeft > 0) {
                    stackInSlot.increment(spaceLeft);
                    itemStack.decrement(spaceLeft);
                    inventory.markDirty();
                }
            }
        }
        return false;
    }





    public void mineBlocks() {
        World currentWorld = this.getWorld();
        if (currentWorld == null) return;
        if (currentWorld.isClient) return; // Execute only on the server side

        Direction facing = getCachedState().get(ExcavatorBlock.FACING); // Assuming ExcavatorBlock has a FACING property

        // Define mining area in front of the BlockEntity based on the facing direction
        int width = miningAreaDimensions.x;
        int depth = miningAreaDimensions.y;
        BlockPos start;
        BlockPos end;

        switch (facing) {
            case NORTH:
                start = pos.add(-width / 2, currentMiningY, 1);  // Changed from -depth to 1
                end = pos.add(width / 2, currentMiningY, depth);  // Changed from -1 to depth
                break;
            case SOUTH:
                start = pos.add(-width / 2, currentMiningY, -depth);  // Changed from 1 to -depth
                end = pos.add(width / 2, currentMiningY, -1);  // Changed from depth to -1
                break;
            case WEST:
                start = pos.add(1, currentMiningY, -width / 2);  // Changed from -depth to 1
                end = pos.add(depth, currentMiningY, width / 2);  // Changed from -1 to depth
                break;
            case EAST:
                start = pos.add(-depth, currentMiningY, -width / 2);  // Changed from 1 to -depth
                end = pos.add(-1, currentMiningY, width / 2);  // Changed from depth to -1
                break;
            default:
                return;  // Return early for invalid facing directions
        }

        boolean allBlocksMined = true; // Assume all blocks are mined initially

        // Loop through each block in the area and mine it
        for (BlockPos currentPos : BlockPos.iterate(start, end)) {
            BlockState state = world.getBlockState(currentPos);

            // Check if the block can be broken and has energy to mine
            if (canBreak(state, currentPos) && hasEnoughEnergy()) {
                mineBlock(currentPos, state);
            } else if (canBreak(state, currentPos)) {
                // This block can be broken but there is not enough energy,
                // so we shouldn't move to the next layer yet.
                allBlocksMined = false;
            }
        }

        // Move to the next layer only if all mineable blocks in the current layer are mined
        if (allBlocksMined) this.currentMiningY--;
    }






    // Improved canBreak Method considering Block hardness
    public boolean canBreak(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        return block != Blocks.BEDROCK
                && block != ModBlocks.EXCAVATOR_BLOCK
                && block != ModBlocks.COAL_GENERATOR_BLOCK
                && block != Blocks.CHEST
                && block != Blocks.ENDER_CHEST
                && block != Blocks.BARREL
                && !(block instanceof net.minecraft.block.FluidBlock) // Allows to break liquid blocks
                && !state.isAir();
    }

    public void validateChestConnections() {
        // Reset any cached chest connections here (if you have any)

        // Check connections anew
        Direction[] directions = Direction.values();
        for (Direction direction : directions) {
            BlockPos neighborPos = pos.offset(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof net.minecraft.block.ChestBlock) {
                // Re-establish connection or cache this chest for later use
            }
        }
    }





    private void mineBlock(BlockPos pos, BlockState state) {
        // Extract energy for mining operation.
        extractEnergy(ENERGY_PER_BLOCK);
        // Get the drops for the block being broken.
        List<ItemStack> drops = Block.getDroppedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos));
        for (ItemStack drop : drops) {
            boolean inserted = insertItem(drop);
            if (!inserted) {
                inserted = tryInsertIntoNeighboringChests(drop);  // Try to insert into neighboring chests
                if (!inserted) {
                    // The inventory and neighboring chests are full, drop the item in the world
                    ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                    world.spawnEntity(itemEntity);
                }
            }
        }

        // Remove the block and play effect
        world.removeBlock(pos, false);
        world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
        markDirty();
    }



    // Method to insert an item into the inventory. Returns whether the item was successfully inserted.
    private boolean insertItem(ItemStack itemStack) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stackInSlot = inventory.get(i);
            if (stackInSlot.isEmpty()) {
                inventory.set(i, itemStack.copy());
                markDirty();
                return true;
            } else if (ItemStack.canCombine(stackInSlot, itemStack)) {
                int spaceLeft = stackInSlot.getMaxCount() - stackInSlot.getCount();
                if (spaceLeft >= itemStack.getCount()) {
                    stackInSlot.increment(itemStack.getCount());
                    markDirty();
                    return true;
                } else if (spaceLeft > 0) {
                    stackInSlot.increment(spaceLeft);
                    itemStack.decrement(spaceLeft);
                    markDirty();
                }
            }
        }
        if(itemStack.getCount() > 0) {
            // If there are still items left, find an empty slot to place the remaining items.
            for(int i = 0; i < inventory.size(); i++) {
                if(inventory.get(i).isEmpty()) {
                    inventory.set(i, itemStack.copy());
                    markDirty();
                    return true;
                }
            }
        }
        return false;
    }




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
        Vector2i dimensions = this.getMiningAreaDimensions();
        tickCounter++;
        if(!world.isClient) { // Check if on server side
            validateChestConnections();
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20*20) {
                    ModMessages.sendExcavatorUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount, isMiningActive);
                    ModMessages.sendExcavatorAreaUpdate((ServerPlayerEntity) playerEntity, pos, dimensions);
                }
            }

        }
        // Increase power every tick, if there's room for more energy.
        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            markDirty(world, pos, state);
        }
        // Check if there is 200 or more energy and if so, mine the block.
        if (isMiningActive && this.energyStorage.getAmount() >= ENERGY_PER_BLOCK) {
            mineBlocks();
            markDirty();
            System.out.println("Excavator energy: " + this.energyStorage.getAmount());

        }
    }

    public Vector2i getMiningAreaDimensions() {
        return miningAreaDimensions;

    }

    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
    @Override
    public Text getDisplayName() {
        return Text.literal("Excavator");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ExcavatorScreenHandler(syncId, playerInventory, this, propertyDelegate);
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
        Inventories.writeNbt(nbt, inventory); // If you have inventory
        nbt.putLong("excavator.energy", energyStorage.amount); // Save the energy amount
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory); // If you have inventory
        if(nbt.contains("excavator.energy")) {
            energyStorage.amount = nbt.getLong("excavator.energy");
        }
    }

}
