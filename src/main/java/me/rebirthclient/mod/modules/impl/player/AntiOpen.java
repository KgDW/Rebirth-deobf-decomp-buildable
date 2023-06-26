package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiOpen
extends Module {
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    public boolean needPacket = false;
    private CPacketPlayerTryUseItemOnBlock cPacketPlayerTryUseItemOnBlock = null;

    public AntiOpen() {
        super("AntiOpen", "Anti Chest", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (!this.packet.getValue()) {
            if (AntiOpen.mc.player.openContainer instanceof ContainerChest) {
                AntiOpen.mc.player.closeScreen();
            }
        } else if (this.needPacket && this.cPacketPlayerTryUseItemOnBlock != null) {
            AntiOpen.mc.player.connection.sendPacket(new CPacketEntityAction(AntiOpen.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            AntiOpen.mc.player.connection.sendPacket(this.cPacketPlayerTryUseItemOnBlock);
            AntiOpen.mc.player.connection.sendPacket(new CPacketEntityAction(AntiOpen.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    @Override
    public void onDisable() {
        this.needPacket = false;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event) {
        if (AntiOpen.fullNullCheck() || !this.packet.getValue() || !(event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) || SneakManager.isSneaking || !BlockUtil.canUseList.contains(AntiOpen.mc.world.getBlockState(((CPacketPlayerTryUseItemOnBlock)event.getPacket()).getPos()).getBlock())) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                this.needPacket = false;
            }
            return;
        }
        if (!this.needPacket && !SneakManager.isSneaking) {
            this.cPacketPlayerTryUseItemOnBlock = event.getPacket();
            event.setCanceled(true);
            this.needPacket = true;
        } else {
            this.needPacket = false;
        }
    }
}

