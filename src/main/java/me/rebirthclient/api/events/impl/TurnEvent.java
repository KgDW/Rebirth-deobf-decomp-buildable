package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class TurnEvent
extends Event {
    private final float yaw;
    private final float pitch;

    public TurnEvent(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
}

