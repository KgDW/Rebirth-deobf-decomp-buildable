package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;

public class AutoTotem
extends Module {
    private final Setting<Float> health = this.add(new Setting<>("Health", 16.0f, 0.0f, 36.0f));
    private final Setting<Boolean> mainHand = this.add(new Setting<>("MainHand", false));
    private final Setting<Boolean> crystal = this.add(new Setting<>("Crystal", true, v -> !this.mainHand.getValue()));

    public AutoTotem() {
        super("AutoTotem", "AutoTotem", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (AutoTotem.mc.player.getHealth() + AutoTotem.mc.player.getAbsorptionAmount() > this.health.getValue()) {
            int crystalSlot;
            if (!AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.END_CRYSTAL) && this.crystal.getValue() && !this.mainHand.getValue() && (crystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, true, true)) != -1) {
                boolean offhandAir = AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.AIR);
                AutoTotem.mc.playerController.windowClick(0, crystalSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, AutoTotem.mc.player);
                if (!offhandAir) {
                    AutoTotem.mc.playerController.windowClick(0, crystalSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                }
            }
            return;
        }
        if (AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.TOTEM_OF_UNDYING) || AutoTotem.mc.player.getHeldItemMainhand().getItem().equals(Items.TOTEM_OF_UNDYING)) {
            return;
        }
        int totemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, true, true);
        if (totemSlot != -1) {
            if (!this.mainHand.getValue()) {
                boolean offhandAir = AutoTotem.mc.player.getHeldItemOffhand().getItem().equals(Items.AIR);
                AutoTotem.mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, AutoTotem.mc.player);
                if (!offhandAir) {
                    AutoTotem.mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                }
            } else {
                InventoryUtil.switchToHotbarSlot(0, false);
                if (AutoTotem.mc.player.inventory.getStackInSlot(0).getItem().equals(Items.TOTEM_OF_UNDYING)) {
                    return;
                }
                boolean mainAir = AutoTotem.mc.player.getHeldItemMainhand().getItem().equals(Items.AIR);
                AutoTotem.mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                AutoTotem.mc.playerController.windowClick(0, 36, 0, ClickType.PICKUP, AutoTotem.mc.player);
                if (!mainAir) {
                    AutoTotem.mc.playerController.windowClick(0, totemSlot, 0, ClickType.PICKUP, AutoTotem.mc.player);
                }
            }
            AutoTotem.mc.player.connection.sendPacket(new CPacketConfirmTransaction(AutoTotem.mc.player.inventoryContainer.windowId, AutoTotem.mc.player.openContainer.getNextTransactionID(AutoTotem.mc.player.inventory), true));
        }
    }
}

