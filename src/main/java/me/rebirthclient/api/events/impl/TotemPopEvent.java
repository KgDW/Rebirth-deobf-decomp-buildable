package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.entity.player.EntityPlayer;

public class TotemPopEvent
extends Event {
    private final EntityPlayer entity;

    public TotemPopEvent(EntityPlayer entity) {
        this.entity = entity;
    }

    public EntityPlayer getEntity() {
        return this.entity;
    }
}

