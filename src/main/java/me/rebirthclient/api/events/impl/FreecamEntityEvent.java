package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class FreecamEntityEvent
extends Event {
    Entity entity;

    public FreecamEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}

