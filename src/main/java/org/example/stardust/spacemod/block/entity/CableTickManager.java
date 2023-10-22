package org.example.stardust.spacemod.block.entity;

import blue.endless.jankson.annotation.Nullable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.stardust.spacemod.block.custom.IronGeneratorBlock;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleSidedEnergyContainer;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
class CableTickManager {
    private static final List<CableBlockEntity> cableList = new ArrayList<>();
    private static final List<OfferedEnergyStorage> targetStorages = new ArrayList<>();
    private static final List<EnergyReceiver> energyReceivers = new ArrayList<>();

    private static final Deque<CableBlockEntity> bfsQueue = new ArrayDeque<>();
    private static long tickCounter = 0;

    static {
        ServerTickEvents.START_SERVER_TICK.register(server -> tickCounter++);
    }

    static void handleCableTick(CableBlockEntity startingCable) {
        if (!(startingCable.getWorld() instanceof ServerWorld)) throw new IllegalStateException();

        Transaction outerTransaction = Transaction.openOuter();
        try {
            gatherCables(startingCable, outerTransaction);
            if (cableList.size() == 0) return;

            // Group all energy into the network.
            long networkCapacity = 0;
            long networkAmount = 0;

            for (CableBlockEntity cable : cableList) {
                networkAmount += cable.energyContainer.amount;
                networkCapacity += cable.energyContainer.getCapacity();

                // Update cable connections.
                cable.appendTargets(targetStorages);
                // Block any cable I/O while we access the network amount directly.
                // Some things might try to access cables, for example a p2p tunnel pointing back at a cable.
                // If the cables and the network go out of sync, we risk duping or voiding energy.
                cable.ioBlocked = true;
            }

            System.out.println("Network Power: Amount = " + networkAmount + ", Capacity = " + networkCapacity);

            // Just in case.
            if (networkAmount > networkCapacity) {
                networkAmount = networkCapacity;
            }

            // Pull energy from storages.
            networkAmount += dispatchTransfer(startingCable, EnergyStorage::extract, networkCapacity - networkAmount);
            networkAmount -= dispatchTransfer(startingCable, EnergyStorage::insert, networkAmount);

            // Split energy evenly across cables.
            int cableCount = cableList.size();
            for (CableBlockEntity cable : cableList) {
                cable.energyContainer.amount = networkAmount / cableCount;
                networkAmount -= cable.energyContainer.amount;
                cableCount--;
                cable.markDirty();
                cable.ioBlocked = false;
            }
            for (EnergyReceiver receiver : energyReceivers) {
                long amountToTransfer = Math.min(networkAmount, startingCable.transferRate); // or some other logic
                long transferred = receiver.receiveEnergy(amountToTransfer, outerTransaction);
                networkAmount -= transferred;
            }
        } finally {
            outerTransaction.commit();
            cableList.clear();
            targetStorages.clear();
            bfsQueue.clear();
            energyReceivers.clear();
        }
    }


    private static boolean shouldTickCable(CableBlockEntity current) {
        // Make sure we only gather and tick each cable once per tick.
        if (current.lastTick == tickCounter) return false;
        // Make sure we ignore cables in non-ticking chunks.
        return current.getWorld() instanceof ServerWorld sw && sw.isChunkLoaded(current.getPos());
    }

    /**
     * Perform a BFS to gather all connected ticking cables.
     */
    /**
     * Perform a BFS to gather all connected ticking cables.
     */
    private static void gatherCables(CableBlockEntity start, Transaction outerTransaction) {
        if (!shouldTickCable(start)) return;

        bfsQueue.add(start);
        start.lastTick = tickCounter;
        cableList.add(start);

        while (!bfsQueue.isEmpty()) {
            CableBlockEntity current = bfsQueue.removeFirst();

            for (Direction direction : Direction.values()) {
                BlockPos adjacentPos = current.getPos().offset(direction);
                BlockState adjacentBlockState = current.getWorld().getBlockState(adjacentPos);
                BlockEntity adjacentBlockEntity = current.getWorld().getBlockEntity(adjacentPos);

                if (adjacentBlockEntity instanceof CableBlockEntity adjCable && current.transferRate == adjCable.transferRate) {
                    if (shouldTickCable(adjCable)) {
                        bfsQueue.add(adjCable);
                        adjCable.lastTick = tickCounter;
                        cableList.add(adjCable);
                    }
                }
            }
        }
    }

    public interface EnergyReceiver {
        boolean canReceiveEnergy(Direction direction);
        long receiveEnergy(long amount, Transaction transaction);
    }

    /**
     * Perform a transfer operation across a list of targets.
     */
    private static long dispatchTransfer(CableBlockEntity cableType, TransferOperation operation, long maxAmount) {
        // Build target list.
        List<SortableStorage> sortedTargets = new ArrayList<>();
        for (var storage : targetStorages) {
            sortedTargets.add(new SortableStorage(operation, storage));
        }
        // Shuffle for better average transfer.
        Collections.shuffle(sortedTargets);
        // Sort by lowest simulation target.
        sortedTargets.sort(Comparator.comparingLong(sortableStorage -> sortableStorage.simulationResult));

        try (Transaction transaction = Transaction.openOuter()) {
            long transferredAmount = 0;
            for (int i = 0; i < sortedTargets.size(); ++i) {
                SortableStorage target = sortedTargets.get(i);
                int remainingTargets = sortedTargets.size() - i;
                long remainingAmount = maxAmount - transferredAmount;
                // Limit max amount to the cable transfer rate.
                long targetMaxAmount = Math.min(remainingAmount / remainingTargets, cableType.transferRate);

                long localTransferred = operation.transfer(target.storage.storage(), targetMaxAmount, transaction);
                if (localTransferred > 0) {
                    transferredAmount += localTransferred;
                    // Block duplicate operations.
                    target.storage.afterTransfer();
                }
            }
            transaction.commit();
            return transferredAmount;
        }
    }





    private interface TransferOperation {
        long transfer(EnergyStorage storage, long maxAmount, Transaction transaction);
    }

    private static class SortableStorage {
        private final OfferedEnergyStorage storage;
        private final long simulationResult;


        SortableStorage(TransferOperation operation, OfferedEnergyStorage storage) {
            this.storage = storage;
            try (Transaction tx = Transaction.openOuter()) {
                this.simulationResult = operation.transfer(storage.storage(), Long.MAX_VALUE, tx);
            }
        }
    }
}
