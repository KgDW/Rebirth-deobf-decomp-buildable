package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class PacketExp
extends Module {
    public static PacketExp INSTANCE;
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 1, 0, 5));
    public final Setting<Boolean> down = this.add(new Setting<>("Down", true));
    public final Setting<Boolean> allowGui = this.add(new Setting<>("allowGui", false));
    public final Setting<Boolean> checkDura = this.add(new Setting<>("CheckDura", true));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Key));
    public final Setting<Bind> throwBind = this.add(new Setting<>("ThrowBind", new Bind(-1), v -> this.mode.getValue() == Mode.Key));
    private final Timer delayTimer = new Timer();

    public PacketExp() {
        super("PacketExp", "Robot module", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @Override
    public void onTick() {
        if (this.isThrow() && this.delayTimer.passedMs(this.delay.getValue() * 20)) {
            this.throwExp();
        }
    }

    public void throwExp() {
        int oldSlot = PacketExp.mc.player.inventory.currentItem;
        int newSlot = InventoryUtil.findHotbarClass(ItemExpBottle.class);
        if (newSlot != -1) {
            PacketExp.mc.player.connection.sendPacket(new CPacketHeldItemChange(newSlot));
            PacketExp.mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            PacketExp.mc.player.connection.sendPacket(new CPacketHeldItemChange(oldSlot));
            this.delayTimer.reset();
        }
    }

    @SubscribeEvent
    public void RotateEvent(MotionEvent event) {
        if (PacketExp.fullNullCheck()) {
            return;
        }
        if (!this.down.getValue()) {
            return;
        }
        if (this.isThrow()) {
            event.setPitch(90.0f);
        }
    }

    public boolean isThrow() {
        if (this.isOff()) {
            return false;
        }
        if (!this.allowGui.getValue() && PacketExp.mc.currentScreen != null) {
            return false;
        }
        if (InventoryUtil.findHotbarClass(ItemExpBottle.class) == -1) {
            return false;
        }
        if (this.checkDura.getValue()) {
            ItemStack helm = PacketExp.mc.player.inventoryContainer.getSlot(5).getStack();
            ItemStack chest = PacketExp.mc.player.inventoryContainer.getSlot(6).getStack();
            ItemStack legging = PacketExp.mc.player.inventoryContainer.getSlot(7).getStack();
            ItemStack feet = PacketExp.mc.player.inventoryContainer.getSlot(8).getStack();
            if (!(!helm.isEmpty && EntityUtil.getDamagePercent(helm) < 100 || !chest.isEmpty && EntityUtil.getDamagePercent(chest) < 100 || !legging.isEmpty && EntityUtil.getDamagePercent(legging) < 100 || !feet.isEmpty && EntityUtil.getDamagePercent(feet) < 100)) {
                return false;
            }
        }
        if (this.mode.getValue() == Mode.Middle && Mouse.isButtonDown(2)) {
            return true;
        }
        return this.mode.getValue() == Mode.Key && this.throwBind.getValue().isDown();
    }

    protected static enum Mode {
        Key,
        Middle

    }
}

