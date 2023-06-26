package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRotate
extends Module {
    public NoRotate() {
        super("NoRotate", "Dangerous to use might desync you", Category.PLAYER);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.isCanceled() || NoRotate.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0 && event.getPacket() instanceof SPacketPlayerPosLook) {
            SPacketPlayerPosLook packet = event.getPacket();
            packet.yaw = NoRotate.mc.player.rotationYaw;
            packet.pitch = NoRotate.mc.player.rotationPitch;
        }
    }
}

