package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LightningDetect
extends Module {
    public LightningDetect() {
        super("LightningDetect", "EZ", Category.MISC);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSpawnGlobalEntity) {
            String cord = "Lightning Detected! X:" + (int)((SPacketSpawnGlobalEntity)event.getPacket()).getX() + " Y:" + (int)((SPacketSpawnGlobalEntity)event.getPacket()).getY() + " Z:" + (int)((SPacketSpawnGlobalEntity)event.getPacket()).getZ();
            this.sendMessage(cord);
        }
    }
}

