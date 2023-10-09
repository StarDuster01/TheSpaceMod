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
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.screen.FusionReactorScreenHandler;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

public class FusionReactorBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory, EnergyStorage {

    protected final PropertyDelegate propertyDelegate;
    private int tickCount = 0;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final SimpleSidedEnergyContainer energyContainer;

    private static final int INPUT_SLOT = 0;
    // Creating an Energy Storage with a given capacity and charge/decharge rate
    public final SimpleEnergyStorage energyStorage = new SimpleEnergyStorage(512000, 100000, 100000) {
        // These settings are allowing the quarry to run at about 10000 blocks a second
        @Override
        protected void onFinalCommit() {
            markDirty();
            if(world != null)
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    };






    public FusionReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUSION_REACTOR_BE, pos, state);
        this.energyContainer = new SimpleSidedEnergyContainer() {
            @Override
            public long getCapacity() {

                return 512000;
            }

            @Override
            public long getMaxInsert(@Nullable Direction side) {
                return 100000;
            }

            @Override
            public long getMaxExtract(@Nullable Direction side) {

                return 100000;
            }
        };

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                if(index == 0)
                    return (int) energyStorage.amount;
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
    private static final int FUEL_CONSUMPTION_INTERVAL = 1; // The interval of ticks between fuel consumptions



    private boolean hasFuelSource(int inputSlot) {
        Item item = this.getStack(inputSlot).getItem();
        return item == Items.NETHER_STAR;
    }


    public void tick(World world, BlockPos pos, BlockState state) {
        tickCounter++;

        if (!world.isClient) {
            if (hasFuelSource(INPUT_SLOT) && this.energyStorage.amount < this.energyStorage.getCapacity()) {
                generateEnergy(); // Updated method name
                markDirty();
            }

            distributeEnergy();
            markDirty(world, pos, state);

            if (tickCounter >= FUEL_CONSUMPTION_INTERVAL) {
                tickCounter = 0;
            }
        }
    }


    private void generateEnergy() {
        if (hasFuelSource(INPUT_SLOT)) {
            long energyToAdd = 100000;  // Set the energy value for the Nether Star.
            try (Transaction transaction = Transaction.openOuter()) {
                energyStorage.insert(energyToAdd, transaction);
                transaction.commit();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            markDirty();
        }
    }






    // New Method to Distribute Energy
    private void distributeEnergy() {
        // Check if we are on the server side
        if (!world.isClient) {
            for (Direction direction : Direction.values()) {
                // Attempt to find an adjacent EnergyStorage in the given direction.
                @Nullable
                EnergyStorage maybeStorage = EnergyStorage.SIDED.find(world, pos.offset(direction), direction.getOpposite());
                System.out.println("Found storage at direction " + direction + ": " + (maybeStorage != null));  // Log statement

                if (maybeStorage != null) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        long amountToSend = Math.min(energyStorage.amount, maybeStorage.getCapacity() - maybeStorage.getAmount());

                        if (amountToSend > 0) {
                            long extracted = energyStorage.extract(amountToSend, transaction);
                            System.out.println("Energy extracted: " + extracted); // Log the amount extracted
                            long inserted = maybeStorage.insert(extracted, transaction);

                            if (inserted > 0) {
                                transaction.commit(); // Commit transaction if energy is transferred.
                                markDirty();
                            }
                        }
                    }
                }
            }
        }
    }




    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
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
        Inventories.writeNbt(nbt, inventory);
        nbt.putLong("fusion_reactor.energy", energyStorage.amount); // Save the energy amount
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        if(nbt.contains("fusion_reactor.energy")) {
            energyStorage.amount = nbt.getLong("fusion_reactor.energy");
        }
    }

    // The following two functions are used to synchronize server and client for energy stuff
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
