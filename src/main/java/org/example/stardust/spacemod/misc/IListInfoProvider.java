package org.example.stardust.spacemod.misc;

import net.minecraft.text.Text;

import java.util.List;

public interface IListInfoProvider {

    void addInfo(List<Text> info, boolean isReal, boolean hasData);

}
