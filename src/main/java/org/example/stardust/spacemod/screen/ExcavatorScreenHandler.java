package org.example.stardust.spacemod.screen;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.example.stardust.spacemod.block.entity.ExcavatorBlockEntity;
import org.example.stardust.spacemod.screen.slot.BigSlot;

import java.util.List;

public class ExcavatorScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    final ExcavatorBlockEntity blockEntity;

    public ExcavatorBlockEntity getBlockEntity() {
        return blockEntity;
    }


    public ExcavatorScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()), new ArrayPropertyDelegate(1));
    }





    public ExcavatorScreenHandler(int syncId, PlayerInventory playerInventory,
                                  BlockEntity blockEntity, PropertyDelegate arrayPropertyDelegate) {
        super(ModScreenHandlers.EXCAVATOR_SCREEN_HANDLER, syncId);
        checkSize(((Inventory) blockEntity), 1);
        this.inventory = (Inventory)blockEntity;
        this.propertyDelegate = arrayPropertyDelegate;
        this.blockEntity = ((ExcavatorBlockEntity) blockEntity);
        int invSize = this.inventory.size();
        int slotsPerRow = 9; // Standard number of slots per row, change it as per your GUI design.
        int startX = 8; // starting x coordinate for your slots
        int startY = 4; // starting y coordinate for your slots
        for (int i = 0; i < invSize; i++) {
            int row = i / slotsPerRow;
            int col = i % slotsPerRow;

            int xPosition = startX + col * 18; // x increases to the right
            int yPosition = startY + row * 18; // y increases downwards

            this.addSlot(new BigSlot(inventory, i, xPosition, yPosition));
        }


        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addProperties(arrayPropertyDelegate);
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }
    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public List<Slot> getSlots() {
        return this.slots;
    }

}



