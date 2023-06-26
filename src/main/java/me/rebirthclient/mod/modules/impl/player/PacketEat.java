package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketEat
extends Module {
    Item item;

    public PacketEat() {
        super("PacketEat", "cancel packet", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (PacketEat.mc.player.isHandActive()) {
            this.item = PacketEat.mc.player.getActiveItemStack().getItem();
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging)event.getPacket()).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && this.item instanceof ItemFood) {
            event.setCanceled(true);
        }
    }
}

