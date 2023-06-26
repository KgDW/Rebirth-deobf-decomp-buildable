package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RightClickBlockEvent
extends Event {
    public final BlockPos pos;
    public final EnumHand hand;
    public final ItemStack stack;

    public RightClickBlockEvent(BlockPos pos, EnumHand hand, ItemStack stack) {
        this.pos = pos;
        this.hand = hand;
        this.stack = stack;
    }
}

