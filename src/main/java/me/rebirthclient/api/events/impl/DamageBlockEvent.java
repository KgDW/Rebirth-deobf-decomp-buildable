package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class DamageBlockEvent
extends Event {
    final BlockPos pos;
    final int progress;
    final int breakerId;

    public DamageBlockEvent(BlockPos pos, int progress, int breakerId) {
        this.pos = pos;
        this.progress = progress;
        this.breakerId = breakerId;
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getBreakerId() {
        return this.breakerId;
    }
}

