package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.util.math.BlockPos;

public class BreakBlockEvent
extends Event {
    BlockPos pos;

    public BreakBlockEvent(BlockPos blockPos) {
        this.pos = blockPos;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setPos(BlockPos pos) {
        this.pos = pos;
    }
}

