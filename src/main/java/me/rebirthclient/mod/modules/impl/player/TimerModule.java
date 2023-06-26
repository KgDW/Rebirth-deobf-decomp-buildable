package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.CatCrystal;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TimerModule
extends Module {
    public static TimerModule INSTANCE = new TimerModule();
    private final Setting<Float> tickNormal = this.add(new Setting<>("Speed", 1.2f, 0.1f, 10.0f));
    public final Setting<Boolean> disableWhenCrystal = this.add(new Setting<>("NoCrystal", true));
    public final Setting<Boolean> packetControl = this.add(new Setting<>("PacketControl", true));
    private final Timer packetListReset = new Timer();
    private int normalLookPos;
    private int rotationMode;
    private int normalPos;
    private float lastPitch;
    private float lastYaw;

    public TimerModule() {
        super("Timer", "Timer", Category.PLAYER);
        INSTANCE = this;
    }

    public static float nextFloat(float startInclusive, float endInclusive) {
        return startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f ? startInclusive : (float)((double)startInclusive + (double)(endInclusive - startInclusive) * Math.random());
    }

    @SubscribeEvent
    public final void onPacketSend(PacketEvent.Send event) {
        if (TimerModule.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayer.Position && this.rotationMode == 1) {
            ++this.normalPos;
            if (this.normalPos > 20) {
                this.rotationMode = 2;
            }
        } else if (event.getPacket() instanceof CPacketPlayer.PositionRotation && this.rotationMode == 2) {
            ++this.normalLookPos;
            if (this.normalLookPos > 20) {
                this.rotationMode = 1;
            }
        }
    }

    @Override
    public void onDisable() {
        TimerModule.mc.timer.tickLength = 50.0f;
    }

    @Override
    public void onEnable() {
        TimerModule.mc.timer.tickLength = 50.0f;
        this.lastYaw = TimerModule.mc.player.rotationYaw;
        this.lastPitch = TimerModule.mc.player.rotationPitch;
        this.packetListReset.reset();
    }

    @Override
    public void onUpdate() {
        if (this.disableWhenCrystal.getValue() && CatCrystal.lastPos != null) {
            TimerModule.mc.timer.tickLength = 50.0f;
            return;
        }
        if (this.packetListReset.passedMs(1000L)) {
            this.normalPos = 0;
            this.normalLookPos = 0;
            this.rotationMode = 1;
            this.lastYaw = TimerModule.mc.player.rotationYaw;
            this.lastPitch = TimerModule.mc.player.rotationPitch;
            this.packetListReset.reset();
        }
        if (this.lastPitch > 85.0f) {
            this.lastPitch = 85.0f;
        }
        if (PacketExp.INSTANCE.isThrow() && PacketExp.INSTANCE.down.getValue()) {
            this.lastPitch = 85.0f;
        }
        TimerModule.mc.timer.tickLength = 50.0f / this.tickNormal.getValue();
    }

    @SubscribeEvent
    public final void RotateEvent(MotionEvent event) {
        if (this.disableWhenCrystal.getValue() && CatCrystal.lastPos != null) {
            return;
        }
        if (this.packetControl.getValue()) {
            switch (this.rotationMode) {
                case 1: {
                    event.setRotation(this.lastYaw, this.lastPitch);
                    break;
                }
                case 2: {
                    event.setRotation(this.lastYaw + TimerModule.nextFloat(1.0f, 3.0f), this.lastPitch + TimerModule.nextFloat(1.0f, 3.0f));
                }
            }
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(this.tickNormal.getValue());
    }
}

