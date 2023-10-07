package org.example.stardust.spacemod.screen.slot;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

public class BigSlot extends Slot {
    private int customStackSize = 128;

    public BigSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.customStackSize = customStackSize;
    }

    @Override
    public int getMaxItemCount() {
        return this.customStackSize; // This line ensures that this slot can contain up to customStackSize items.
    }
}