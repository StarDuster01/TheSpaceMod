package org.example.stardust.spacemod.compat;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.stardust.spacemod.SpaceMod;
import org.example.stardust.spacemod.block.ModBlocks;


import java.util.LinkedList;
import java.util.List;
public class DoomFurnaceCategory implements DisplayCategory<BasicDisplay> {
    public static final Identifier TEXTURE =
            new Identifier(SpaceMod.MOD_ID, "textures/gui/doom_furnace_gui.png");
    public static final CategoryIdentifier<DoomFurnaceDisplay> DOOM_FURNACE =
            CategoryIdentifier.of(SpaceMod.MOD_ID, "doom_furnace");

    @Override
    public CategoryIdentifier<? extends BasicDisplay> getCategoryIdentifier() {
        return DOOM_FURNACE;
    }

    @Override
    public Text getTitle() {
        return Text.literal("Doom Furnace");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(ModBlocks.DOOM_FURNACE_BLOCK.asItem().getDefaultStack());
    }

    @Override
    public List<Widget> setupDisplay(BasicDisplay display, Rectangle bounds) {
        Point startPoint = new Point(bounds.getCenterX() - 87, bounds.getCenterY() - 35);
        List<Widget> widgets = new LinkedList<>();
        widgets.add(Widgets.createTexturedWidget(TEXTURE,
                new Rectangle(startPoint.x, startPoint.y, 175, 82)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 80, startPoint.y + 11))
                .entries(display.getInputEntries().get(0)));

        widgets.add(Widgets.createSlot(new Point(startPoint.x + 80, startPoint.y + 59))
                .markOutput().entries(display.getOutputEntries().get(0)));

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }
}
