package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketExp;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiWeak
extends Module {
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 500));
    private final Setting<SwapMode> swapMode = this.add(new Setting<>("SwapMode", SwapMode.Bypass));
    private final Setting<Boolean> onlyCrystal = this.add(new Setting<>("OnlyCrystal", true));
    private final Setting<Boolean> testSync = this.add(new Setting<>("TestSync", true, v -> this.swapMode.getValue() == SwapMode.Bypass));
    private final Setting<Boolean> sync = this.add(new Setting<>("Sync", false, v -> this.swapMode.getValue() == SwapMode.Bypass).setParent());
    private final Setting<Boolean> always = this.add(new Setting<>("Always", false, v -> this.sync.isOpen() && this.swapMode.getValue() == SwapMode.Bypass));
    private int lastSlot = -1;
    private final Timer delayTimer = new Timer();
    private CPacketUseEntity packet = null;

    public AntiWeak() {
        super("AntiWeak", "anti weak", Category.COMBAT);
    }

    @Override
    public String getInfo() {
        return this.swapMode.getValue().name();
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onPacketSend(PacketEvent.Send event) {
        if (AntiWeak.fullNullCheck()) {
            return;
        }
        if (event.isCanceled()) {
            return;
        }
        if (!AntiWeak.mc.player.isPotionActive(MobEffects.WEAKNESS)) {
            return;
        }
        if (AntiWeak.mc.player.getHeldItemMainhand().item instanceof ItemSword) {
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        if (event.getPacket() instanceof CPacketUseEntity && ((CPacketUseEntity)event.getPacket()).getAction() == CPacketUseEntity.Action.ATTACK) {
            Entity attackedEntity = ((CPacketUseEntity)event.getPacket()).getEntityFromWorld(AntiWeak.mc.world);
            if (attackedEntity == null || !(attackedEntity instanceof EntityEnderCrystal) && this.onlyCrystal.getValue()) {
                return;
            }
            this.packet = event.getPacket();
            this.doAnti();
            this.delayTimer.reset();
            event.setCanceled(true);
        }
    }

    private void doAnti() {
        if (this.packet == null) {
            return;
        }
        int strong = this.swapMode.getValue() != SwapMode.Bypass ? InventoryUtil.findHotbarBlock(ItemSword.class) : InventoryUtil.findClassInventorySlot(ItemSword.class, true);
        if (strong == -1) {
            return;
        }
        int old = AntiWeak.mc.player.inventory.currentItem;
        if (this.swapMode.getValue() != SwapMode.Bypass) {
            InventoryUtil.doSwap(strong);
        } else {
            AntiWeak.mc.playerController.windowClick(0, strong, old, ClickType.SWAP, AntiWeak.mc.player);
        }
        AntiWeak.mc.player.connection.sendPacket(this.packet);
        if (this.swapMode.getValue() != SwapMode.Bypass) {
            if (this.swapMode.getValue() != SwapMode.Normal) {
                InventoryUtil.doSwap(old);
            }
        } else {
            AntiWeak.mc.playerController.windowClick(0, strong, old, ClickType.SWAP, AntiWeak.mc.player);
            if (this.sync.getValue() && this.always.getValue()) {
                PacketExp.INSTANCE.throwExp();
            }
            this.lastSlot = strong;
            if (this.testSync.getValue()) {
                AntiWeak.mc.player.connection.sendPacket(new CPacketConfirmTransaction(AntiWeak.mc.player.inventoryContainer.windowId, AntiWeak.mc.player.openContainer.getNextTransactionID(AntiWeak.mc.player.inventory), true));
            }
        }
    }

    @Override
    public void onTick() {
        this.update();
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        this.update();
    }

    @Override
    public void onUpdate() {
        this.update();
    }

    private void update() {
        if (this.lastSlot == -1 || this.always.getValue() || !this.sync.getValue()) {
            return;
        }
        if (!(AntiWeak.mc.player.inventoryContainer.getInventory().get(this.lastSlot).getItem() instanceof ItemSword)) {
            PacketExp.INSTANCE.throwExp();
            this.lastSlot = -1;
        }
    }

    public static enum SwapMode {
        Normal,
        Silent,
        Bypass

    }
}

