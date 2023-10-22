package org.example.stardust.spacemod.block.entity;

import net.minecraft.util.math.Direction;
import team.reborn.energy.api.EnergyStorage;

/**
 * {@link EnergyStorage} adjacent to an energy cable, with some additional info.
 */
record OfferedEnergyStorage(CableBlockEntity sourceCable, Direction direction, EnergyStorage storage) {
    void afterTransfer() {
        // Block insertions from this side.
        sourceCable.blockedSides |= 1 << direction.ordinal();
    }
}
