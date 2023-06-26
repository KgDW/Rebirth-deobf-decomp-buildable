package me.rebirthclient.api.events.impl;

import me.rebirthclient.api.events.Event;
import me.rebirthclient.api.util.Wrapper;

public class MotionEvent
extends Event
implements Wrapper {
    public static MotionEvent INSTANCE;
    protected float yaw;
    protected float pitch;
    protected double x;
    protected double y;
    protected double z;
    protected boolean onGround;

    public MotionEvent(int stage, double posX, double posY, double posZ, float y, float p, boolean pOnGround) {
        super(stage);
        INSTANCE = this;
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        this.yaw = y;
        this.pitch = p;
        this.onGround = pOnGround;
    }

    public MotionEvent(int stage, MotionEvent event) {
        this(stage, event.x, event.y, event.z, event.yaw, event.pitch, event.onGround);
    }

    public void setRotation(float yaw, float pitch) {
        this.setYaw(yaw);
        this.setPitch(pitch);
    }

    public void setPostion(double x, double y, double z, boolean onGround) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setOnGround(onGround);
    }

    public void setPostion(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
        this.setYaw(yaw);
        this.setPitch(pitch);
        this.setOnGround(onGround);
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = (float)yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = (float)pitch;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double posX) {
        this.x = posX;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double d) {
        this.y = d;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double posZ) {
        this.z = posZ;
    }

    public boolean isOnGround() {
        return this.onGround;
    }

    public void setOnGround(boolean b) {
        this.onGround = b;
    }
}

