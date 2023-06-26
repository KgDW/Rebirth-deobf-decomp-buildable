package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoFish
extends Module {
    public AutoFish() {
        super("AutoFish", "auto fishing", Category.PLAYER);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onReceivePacket(PacketEvent.Receive event) {
        SPacketSoundEffect packet;
        if (AutoFish.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (event.getPacket() instanceof SPacketSoundEffect && (packet = event.getPacket()).getCategory() == SoundCategory.NEUTRAL && packet.getSound() == SoundEvents.ENTITY_BOBBER_SPLASH && AutoFish.mc.player.getHeldItemMainhand().getItem() == Items.FISHING_ROD) {
            AutoFish.mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            AutoFish.mc.player.swingArm(EnumHand.MAIN_HAND);
            AutoFish.mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            AutoFish.mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }
}

