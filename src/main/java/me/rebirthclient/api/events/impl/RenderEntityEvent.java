package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class RenderEntityEvent
extends Event {
    private float partialTicks;
    private Entity entity;
    private float yaw;
    private double x;
    private double y;
    private double z;

    public RenderEntityEvent(int stage, Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        super(stage);
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.partialTicks = partialTicks;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}

