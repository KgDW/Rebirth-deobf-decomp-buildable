package me.rebirthclient.api.managers.impl;

import me.rebirthclient.api.events.impl.PacketEvent;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SneakManager {
    public static boolean isSneaking = false;

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketEntityAction) {
            if (((CPacketEntityAction)event.getPacket()).getAction() == CPacketEntityAction.Action.START_SNEAKING) {
                isSneaking = true;
            }
            if (((CPacketEntityAction)event.getPacket()).getAction() == CPacketEntityAction.Action.STOP_SNEAKING) {
                isSneaking = false;
            }
        }
    }
}

