package org.example.stardust.spacemod.block.entity;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.ArrayList;
import java.util.List;

public class ExcavatorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(36, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;

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




private boolean isMiningActive = false;


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


    public void mineBlocks() {
        World currentWorld = this.getWorld();
        if (currentWorld == null) return;
        if (currentWorld.isClient) return; // Execute only on the server side

        // Define mining area around the BlockEntity
        int radius = 1; // for example
        BlockPos start = pos.add(-radius, currentMiningY, -radius);
        BlockPos end = pos.add(radius, currentMiningY, radius);

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
                && !(block instanceof net.minecraft.block.FluidBlock) // Allows to break liquid blocks
                && !state.isAir();
    }




    private void mineBlock(BlockPos pos, BlockState state) {
        // Extract energy for mining operation.
        extractEnergy(ENERGY_PER_BLOCK);
        // Get the drops for the block being broken.
        List<ItemStack> drops = Block.getDroppedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos));
        for (ItemStack drop : drops) {
            boolean inserted = insertItem(drop);
            if (!inserted) {
                // The inventory is full, drop the item in the world
                ItemEntity itemEntity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop);
                world.spawnEntity(itemEntity);
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
        tickCounter++;
        if(!world.isClient) { // Check if on server side
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20*20) {
                    ModMessages.sendExcavatorUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount, isMiningActive);
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
