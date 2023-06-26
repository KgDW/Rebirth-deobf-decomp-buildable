package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.events.impl.BlockEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Debug
extends Module {
    public Debug() {
        super("Debug", "dev!", Category.MISC);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onClickBlock(BlockEvent event) {
        if (Debug.fullNullCheck()) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (Debug.fullNullCheck() || Debug.mc.player.isCreative() || !(event.getPacket() instanceof CPacketPlayerDigging)) {
            return;
        }
        this.sendMessage(((CPacketPlayerDigging)event.getPacket()).getAction().name());
    }
}

