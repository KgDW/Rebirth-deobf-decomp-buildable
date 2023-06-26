package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoFall
extends Module {
    public static NoFall INSTANCE = new NoFall();
    private final Setting<Integer> distance = this.add(new Setting<>("Distance", 3, 0, 50));

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.PLAYER);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (NoFall.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof CPacketPlayer) {
            for (ItemStack is : NoFall.mc.player.getArmorInventoryList()) {
                if (!(is.getItem() instanceof ItemElytra)) continue;
                return;
            }
            if (NoFall.mc.player.isElytraFlying()) {
                return;
            }
            if (NoFall.mc.player.fallDistance >= (float) this.distance.getValue()) {
                CPacketPlayer packet = event.getPacket();
                packet.onGround = true;
            }
        }
    }

    @Override
    public String getInfo() {
        return "Packet";
    }
}

