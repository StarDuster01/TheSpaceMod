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
import net.minecraft.entity.TntEntity;
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
import org.example.stardust.spacemod.networking.ModMessages;
import org.example.stardust.spacemod.screen.RangeSpawnerScreenHandler;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RangeSpawnerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(0, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    public boolean isPowered() {
        return energyStorage.getAmount() > 0;
    }


    public class RangeSpawnerEnergyStorage extends SimpleEnergyStorage {
        public RangeSpawnerEnergyStorage(long capacity, long maxInsert, long maxExtract) {
            super(capacity, maxInsert, maxExtract);
        }

        public void setAmountDirectly(long newAmount) {
            this.amount = Math.min(newAmount, this.capacity);
        }
    }


    // Creating an Energy Storage with a given capacity and charge/decharge rate
    public final RangeSpawnerEnergyStorage energyStorage = new RangeSpawnerEnergyStorage(1000000, 10000, 1000000) {
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };


    public RangeSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RANGE_SPAWNER_BLOCK_BE, pos, state);
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

    private void extractEnergy(long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            energyStorage.extract(amount, transaction);
            markDirty();
            transaction.commit();
        }
    }

    public void spawnTNTOnTop() {
        if (!world.isClient) { // Ensure you're on the server side
            if (energyStorage.getAmount() >= 1000000) {
            BlockPos tntPos = pos.up(); // The position right above the current block entity

            // Create a primed TNT entity at that position
            TntEntity tntEntity = new TntEntity(world, tntPos.getX() + 0.5, tntPos.getY(), tntPos.getZ() + 0.5, null);

            // Spawn the primed TNT into the world
            world.spawnEntity(tntEntity);
            extractEnergy(1000000);
        }
        }
    }

    public void airStrike(World world, BlockPos coordinate, int fuseTime) {
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) world;

        // Determine the chunk coordinates
        int chunkX = coordinate.getX() >> 4;
        int chunkZ = coordinate.getZ() >> 4;
        serverWorld.setChunkForced(chunkX, chunkZ, true);
        // The spread distance around the main coordinate
        int spread = 10;  // Change this value as needed
        Random rand = new Random();

        // For this example, I'll spawn 10 TNT entities. Adjust this value as needed.
        for (int i = 0; i < 100; i++) {
            // Calculate random offsets for x, y, and z coordinates
            int xOffset = rand.nextInt(spread * 2) - spread;  // This gives values between -spread to spread
            int yOffset = rand.nextInt(2);  // This will drop TNT from slightly above
            int zOffset = rand.nextInt(spread * 2) - spread;

            // New position for TNT entity
            BlockPos tntPos = coordinate.add(xOffset, yOffset, zOffset);

            // Create and spawn the TNT entity
            TntEntity tnt = new TntEntity(world, tntPos.getX() + 0.5, tntPos.getY() + 10, tntPos.getZ() + 0.5, null);
            tnt.setFuse(fuseTime);
            world.spawnEntity(tnt);
            extractEnergy(1000000);

            scheduler.schedule(() -> {
                if (serverWorld.getChunk(chunkX, chunkZ).isEmpty()) {
                    serverWorld.setChunkForced(chunkX, chunkZ, false);
                }
            }, 5, TimeUnit.MINUTES); // Unload after 5 minutes, for example


        }
    }





    public void tick(World world, BlockPos pos, BlockState state) {
        // Increment the tick counter every tick
        tickCounter++;
        if(!world.isClient) { // Check if on server side
            for (PlayerEntity playerEntity : world.getPlayers()) {
                if (playerEntity instanceof ServerPlayerEntity && playerEntity.squaredDistanceTo(Vec3d.of(pos)) < 20*20) {
                    ModMessages.sendRangeSpawnerUpdate((ServerPlayerEntity) playerEntity, pos, energyStorage.amount);
                }
            }
        }
        // Increase power every tick, if there's room for more energy.
        if (this.energyStorage.getAmount() < this.energyStorage.getCapacity()) {
            markDirty(world, pos, state);
        }
    }


    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }
    @Override
    public Text getDisplayName() {
        return Text.literal("Range Spawner");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RangeSpawnerScreenHandler(syncId, playerInventory, this, propertyDelegate);
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
        nbt.putLong("range_spawner.energy", energyStorage.amount); // Save the energy amount
    }
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory); // If you have inventory
        if(nbt.contains("range_spawner.energy")) {
            energyStorage.amount = nbt.getLong("range_spawner.energy");
        }
    }

}
