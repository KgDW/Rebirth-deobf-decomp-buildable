package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderToolTipEvent
extends Event {
    private final ItemStack stack;
    private final int x;
    private final int y;

    public RenderToolTipEvent(ItemStack stack, int x, int y) {
        this.stack = stack;
        this.x = x;
        this.y = y;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}

