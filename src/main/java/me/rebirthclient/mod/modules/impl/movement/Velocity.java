package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.PushEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.asm.accessors.ISPacketEntityVelocity;
import me.rebirthclient.asm.accessors.ISPacketExplosion;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity
extends Module {
    public static Velocity INSTANCE;
    private final Setting<Float> horizontal = this.add(new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f));
    private final Setting<Float> vertical = this.add(new Setting<>("Vertical", 0.0f, 0.0f, 100.0f));
    private final Setting<Boolean> noWaterPush = this.add(new Setting<>("LiquidPush", true));
    private final Setting<Boolean> blockPush = this.add(new Setting<>("BlockPush", true));
    private final Setting<Boolean> entityPush = this.add(new Setting<>("EntityPush", true));

    public Velocity() {
        super("Velocity", "Cancels all the pushing your player receives", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return MathUtil.round(this.horizontal.getValue(), 1) + "%," + MathUtil.round(this.vertical.getValue(), 1) + "%";
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onReceivePacket(final PacketEvent.Receive event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        final float h = this.horizontal.getValue() / 100.0f;
        final float v = this.vertical.getValue() / 100.0f;
        if (event.getPacket() instanceof EntityFishHook) {
            event.setCanceled(true);
        }
        if (event.getPacket() instanceof SPacketExplosion) {
            final ISPacketExplosion packet = event.getPacket();
            packet.setX(packet.getX() * h);
            packet.setY(packet.getY() * v);
            packet.setZ(packet.getZ() * h);
        }
        if (event.getPacket() instanceof SPacketEntityVelocity) {
            final ISPacketEntityVelocity packet2 = event.getPacket();
            if (packet2.getEntityID() == Velocity.mc.player.getEntityId()) {
                if (this.horizontal.getValue() == 0.0f && this.vertical.getValue() == 0.0f) {
                    event.setCanceled(true);
                }
                else {
                    packet2.setX((int)(packet2.getX() * h));
                    packet2.setY((int)(packet2.getY() * v));
                    packet2.setZ((int)(packet2.getZ() * h));
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getStage() == 0 && this.entityPush.getValue() && event.entity.equals(Velocity.mc.player)) {
            event.x = -event.x * 0.0;
            event.y = -event.y * 0.0;
            event.z = -event.z * 0.0;
        } else if (event.getStage() == 1 && this.blockPush.getValue()) {
            event.setCanceled(true);
        } else if (event.getStage() == 2 && this.noWaterPush.getValue() && Velocity.mc.player != null && Velocity.mc.player.equals(event.entity)) {
            event.setCanceled(true);
        }
    }
}

