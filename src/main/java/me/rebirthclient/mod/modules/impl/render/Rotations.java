package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Rotations
extends Module {
    public static Rotations INSTANCE;
    private final Setting<Boolean> onlyThird = this.add(new Setting<>("OnlyThird", true));
    private static float renderPitch;
    private static float renderYawOffset;
    private static float prevPitch;
    private static float prevRenderYawOffset;
    private static float prevRotationYawHead;
    private static float rotationYawHead;
    private int ticksExisted;

    public Rotations() {
        super("Rotations", "show rotation", Category.RENDER);
        INSTANCE = this;
    }

    public boolean check() {
        return this.isOn() && (Rotations.mc.gameSettings.thirdPersonView != 0 || !this.onlyThird.getValue());
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && ((CPacketPlayer)event.getPacket()).rotating) {
            this.set(((CPacketPlayer)event.getPacket()).yaw, ((CPacketPlayer)event.getPacket()).pitch);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void invoke(MotionEvent event) {
        this.set(event.getYaw(), event.getPitch());
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.isCanceled() || Rotations.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0 && event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            this.set(packet.yaw, packet.pitch);
        }
    }

    private void set(float yaw, float pitch) {
        if (Rotations.mc.player.ticksExisted == this.ticksExisted) {
            return;
        }
        this.ticksExisted = Rotations.mc.player.ticksExisted;
        prevPitch = renderPitch;
        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = this.getRenderYawOffset(yaw, prevRenderYawOffset);
        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;
        renderPitch = pitch;
    }

    public static float getRenderPitch() {
        return renderPitch;
    }

    public static float getRotationYawHead() {
        return rotationYawHead;
    }

    public static float getRenderYawOffset() {
        return renderYawOffset;
    }

    public static float getPrevPitch() {
        return prevPitch;
    }

    public static float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public static float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float offset;
        float result = offsetIn;
        double xDif = Rotations.mc.player.posX - Rotations.mc.player.prevPosX;
        double zDif = Rotations.mc.player.posZ - Rotations.mc.player.prevPosZ;
        if (xDif * xDif + zDif * zDif > 0.002500000176951289) {
            offset = (float)MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            result = 95.0f < wrap && wrap < 265.0f ? offset - 180.0f : offset;
        }
        if (Rotations.mc.player.swingProgress > 0.0f) {
            result = yaw;
        }
        if ((offset = MathHelper.wrapDegrees(yaw - (result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f))) < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }
        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }
        return result;
    }
}

